package com.github.xhea1.stashtools.app;

import com.github.xhea1.stashtools.service.party.PartyHTTPService;
import com.github.xhea1.stashtools.service.processing.ProcessingService;
import com.github.xhea1.stashtools.service.stash.StashGraphQLService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

/**
 * Main application to tag scenes in Stash via data fetched from Party.
 * This uses Picocli for command-line input parsing.
 */
@NullMarked
@CommandLine.Command(name = "AddMetadataApp", description = "Updates Stash scenes with data from Party.", mixinStandardHelpOptions = true, version = "1.0")
public class AddMetadataApp implements Callable<Integer> {

    private static final Logger logger = LogManager.getLogger(AddMetadataApp.class);

    @CommandLine.Parameters(index = "0", description = "The path to a file or directory.")
    private Path input;

    @CommandLine.Option(names = {"-url"}, description = "The base URL for the party metadata API. Default is '${DEFAULT-VALUE}'.")
    private String baseUrl = "https://coomer.su/api/v1";

    @CommandLine.Option(names = {"-stash"}, description = "The url for the Stash server.", required = true)
    private String stashUrl;

    @CommandLine.Option(names = {"-basePath"}, description = "The base Path for files. This will be stripped from the file path when querying Stash.")
    @Nullable
    private Path basePath;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new AddMetadataApp()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        try {
            // Validate input
            if (!Files.exists(input)) {
                logger.error("Error: The provided path does not exist.");
                return 1;
            }
            StashGraphQLService stashGraphQLService = new StashGraphQLService(stashUrl);
            PartyHTTPService partyHttpService = new PartyHTTPService(baseUrl);

            ProcessingService processingService = new ProcessingService(partyHttpService, stashGraphQLService, basePath);
            processingService.processPath(input);
            return 0;

        } catch (Exception e) {
            logger.error("Error: {}", e.getMessage(), e);
            return 1;
        }
    }
}

