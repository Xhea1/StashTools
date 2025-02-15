package com.github.xhea1.stashtools.service.processing;

import com.github.xhea1.partytools.service.PartyHTTPService;
import com.github.xhea1.partytools.model.PostRecord;
import com.github.xhea1.stashtools.service.stash.StashGraphQLService;
import com.github.xhea1.stashtools.service.processing.tagging.TaggingService;
import com.github.xhea1.stashtools.service.util.HashUtil;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Handles the actual processing:
 *  Creates hashes, fetches data from Party and Stash and then calls {@link TaggingService} to update the info in Stash.
 */
@NullMarked
public class ProcessingService {
    private static final Logger logger = LogManager.getLogger(ProcessingService.class);
    private final PartyHTTPService partyHttpService;
    private final StashGraphQLService stashGraphQLService;
    private final TaggingService taggingService;
    @Nullable
    private final Path basePath;

    /**
     * @param partyHttpService service for querying Party
     * @param stashGraphQLService service for querying Stash
     * @param basePath optional base path
     */
    public ProcessingService(PartyHTTPService partyHttpService, StashGraphQLService stashGraphQLService, @Nullable Path basePath) {
        this.partyHttpService = partyHttpService;
        this.stashGraphQLService = stashGraphQLService;
        this.basePath = basePath;
        taggingService = new TaggingService(stashGraphQLService);
    }

    /**
     * Process the given directory/file and fetch posts from Party.
     *
     * @param path path to process can be a directory or a single file
     * @throws IOException if path is a directory and cannot be iterated
     */
    public void processPath(Path path) throws IOException {
        Multimap<Path, PostRecord> postMap = MultimapBuilder.hashKeys().arrayListValues().build();
        // Compute SHA-256 hash of the file/directory and fetch posts from Party
        if (Files.isDirectory(path)) {
            try (var files = Files.list(path)) {
                // TODO: consider parallelizing this
                files.filter(Files::isRegularFile).forEach(file -> {
                    try {
                        for (var post : partyHttpService.getPostsByHash(HashUtil.computeSha256(file.toFile()))) {
                            postMap.put(file, post);
                        }
                    } catch (IOException e) {
                        logger.error("Could not process path {}", file, e);
                    }
                });
            }
        } else {
            for (var post : partyHttpService.getPostsByHash(HashUtil.computeSha256(path.toFile()))) {
                postMap.put(path, post);
            }
        }
        compareWithStash(postMap);
    }

    /**
     * Compare and update the metadata in Stash with the post from Party.
     *
     * @param postMap posts fetched from Party
     */
    private void compareWithStash(Multimap<Path, PostRecord> postMap) {
        // TODO: decide how to handle cases where we found multiple posts for the file
        // TODO: handle images
        postMap.asMap().entrySet().stream().filter(entry -> isVideo(entry.getKey())).filter(entry -> entry.getValue().size() < 2).forEach(entry -> {
            Path path = entry.getKey();

            PostRecord post = entry.getValue().stream().toList().getFirst();
            if (basePath != null) {
                path = basePath.relativize(path);
            }
            // TODO: does this replacement need to be configurable? I.e. someone is running Stash on Windows
            for (var scene : stashGraphQLService.findScenesByPathRegex(path.toString().replace("\\", "/"))) {
                taggingService.tagScene(scene, post);
            }
        });
    }

    /**
     * Check if a file is a video file based on its MIME type
     *
     * @param path file to check
     * @return true if the file is a video file
     */
    private boolean isVideo(Path path) {
        try {
            return Files.probeContentType(path).startsWith("video");
        } catch (IOException e) {
            logger.warn("Could not determine MIME type of file {}", path, e);
            return false;
        }
    }
}
