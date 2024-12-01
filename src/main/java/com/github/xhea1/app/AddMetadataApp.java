package com.github.xhea1.app;

import com.github.xhea1.service.party.HTTPService;
import com.github.xhea1.service.party.model.PostRecord;
import com.github.xhea1.service.utils.HashUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Main application to query metadata for files using SHA-256 hash.
 * This uses Picocli for command-line input parsing.
 */
@CommandLine.Command(name = "AddMetadataApp", description = "Queries metadata for a file or directory using its SHA-256 hash.", mixinStandardHelpOptions = true, version = "1.0")
public class AddMetadataApp implements Callable<Integer> {

    private static final Logger logger = LogManager.getLogger(AddMetadataApp.class);

    @CommandLine.Parameters(index = "0", description = "The path to a file or directory.")
    private File input;

    @CommandLine.Option(names = {"-u", "--url"}, description = "The base URL for the metadata API.")
    private String baseUrl = "https://coomer.su/api/v1";

    public static void main(String[] args) {
        int exitCode = new CommandLine(new AddMetadataApp()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        try {
            // Validate input
            if (!input.exists()) {
                logger.error("Error: The provided path does not exist.");
                return 1;
            }
            // Initialize HTTPService and fetch posts
            HTTPService httpService = new HTTPService(baseUrl);

            // Compute SHA-256 hash of the file/directory
            if (input.isDirectory()) {
                try (var files = Files.list(input.toPath())) {
                    files.filter(Files::isRegularFile).forEach(file -> {
                        try {
                            fetchPosts(httpService, file.toFile());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            } else {
                fetchPosts(httpService, input);
            }
            return 0;

        } catch (Exception e) {
            logger.error("Error: {}", e.getMessage(), e);
            return 1;
        }
    }

    private void fetchPosts(@NotNull HTTPService httpService, File file) throws IOException {
        String sha256Hash = getHash(file);
        List<PostRecord> posts = httpService.getPostsByHash(sha256Hash);

        // Print results
        if (posts.isEmpty()) {
            logger.info("No posts found for the given hash.");
        } else {
            logger.info("Posts:");
            for (PostRecord post : posts) {
                logger.info(post);
            }
        }
    }

    private @NotNull String getHash(File file) throws IOException {
        String sha256Hash = HashUtil.computeSha256(file);
        logger.debug("SHA-256 Hash for file {}: {}", file, sha256Hash);
        return sha256Hash;
    }
}

