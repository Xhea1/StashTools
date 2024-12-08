package com.github.xhea1.stashtools.service.stash;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.xhea1.stashtools.model.stash.SceneRecord;
import okhttp3.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.*;

/**
 * Service for GraphQL operations
 */
@NullMarked
public class StashGraphQLService {

    @Nullable
    private static final String STASH_API_KEY = System.getenv("STASH_API_KEY");
    private static final String FIND_SCENES_BY_PATH_REGEX_QUERY = "{ findScenesByPathRegex(filter: {q: \"%s\", per_page: -1}) { count duration filesize scenes { id title details urls date} } }";
    private static final String UPDATE_SCENE_QUERY = """
            mutation sceneUpdate($input: SceneUpdateInput!) {
              sceneUpdate(input: $input) {
                id
                title
                details
                date
                urls
              }
            }
            """;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final OkHttpClient client = new OkHttpClient();
    private static final Logger logger = LogManager.getLogger(StashGraphQLService.class);
    private final String endpoint;

    /**
     * Create a new service for querying Stash via GraphQL.
     *
     * @param endpoint stash endpoint to connect to
     */
    public StashGraphQLService(String endpoint) {
        this.endpoint = endpoint + "/graphql";
    }

    /**
     * Helper method to convert a Java object to JSON string using Jackson
     *
     * @param object Object to convert
     * @return JSON string
     * @throws JsonProcessingException on error
     */
    private static String toJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    /**
     * Find all scenes for the specified path.
     *
     * @param path path to query
     * @return all scenes found for the given path
     */
    public List<SceneRecord> findScenesByPathRegex(String path) {
        // Create a Map for the request body
        Map<String, Object> requestBodyMap = new HashMap<>();
        requestBodyMap.put("query", FIND_SCENES_BY_PATH_REGEX_QUERY.formatted(path));

        var response = sendGraphQLRequest(requestBodyMap);
        List<SceneRecord> scenes = new ArrayList<>();
        if (response.isPresent()) {
            // TODO: error handling
            var data = response.get().get("data");
            var findScenes = data.get("findScenesByPathRegex");
            for (JsonNode scene : findScenes.get("scenes")) {
                scenes.add(parseScene(scene));
            }
        } else {
            logger.error("Found no scene in Stash for path '{}'", path);
        }
        return scenes;
    }

    public Optional<SceneRecord> updateScene(int sceneId, @Nullable String title, @Nullable String details, @Nullable String date) {
        Map<String, Object> requestBodyMap = new HashMap<>();
        requestBodyMap.put("query", UPDATE_SCENE_QUERY);
        Map<String, Object> variables = new HashMap<>();
        requestBodyMap.put("variables", variables);
        Map<String, Object> input = new HashMap<>();
        variables.put("input", input);
        input.put("id", sceneId);
        if (title != null) input.put("title", title);
        if (details != null) input.put("details", details);
        if (date != null) input.put("date", date);
        var response = sendGraphQLRequest(requestBodyMap);
        if (response.isPresent()) {
            // TODO: error handling
            var data = response.get().get("data");
            return Optional.of(parseScene(data.get("sceneUpdate")));
        } else {
            logger.error("Update of scene {} failed.", sceneId);
        }
        return Optional.empty();
    }

    /**
     * Send the actual request.
     *
     * @param requestBodyMap map of String keys to Object. Will be serialized into JSON and send to Stash.
     * @return Optional containing the response or empty optional on errors.
     */
    private Optional<JsonNode> sendGraphQLRequest(Map<String, Object> requestBodyMap) {
        // Convert the request body Map to JSON
        RequestBody requestBody = null;
        try {
            requestBody = RequestBody.create(toJson(requestBodyMap), MediaType.parse("application/json"));
        } catch (JsonProcessingException e) {
            logger.error("Could not create JSON", e);
            return Optional.empty();
        }

        // Create the HTTP request
        var builder = new Request.Builder().url(endpoint).post(requestBody) // Send as POST request
                .addHeader("Content-Type", "application/json"); // Set content type to JSON
        if (STASH_API_KEY != null) builder = builder.addHeader("ApiKey", STASH_API_KEY);
        Request request = builder.build();

        // Send the request and handle the response
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                // Parse the response body (assuming it's JSON)
                String responseBody = response.body().string();
                logger.debug("GraphQL Response: {}", responseBody);
                return Optional.of(objectMapper.readTree(responseBody));
            } else {
                logger.error("Error: {}", response.code());
                logger.error(response.body());
            }
        } catch (IOException e) {
            logger.error("", e);
        }
        return Optional.empty();
    }

    /**
     * Parse a {@link SceneRecord} from JSON
     *
     * @param scene JSON to parse
     * @return created {@link SceneRecord}
     */
    private SceneRecord parseScene(JsonNode scene) {
        return new SceneRecord(scene.get("id").asInt(), scene.get("title").asText(), scene.get("details").asText(), parseJSONArray(scene.get("urls")), scene.get("date").asText(null));
    }

    /**
     * Parse a JSON array into a List of Strings
     *
     * @param array JSON array
     * @return list of all strings contained in the array
     */
    private List<String> parseJSONArray(JsonNode array) {
        List<String> list = new ArrayList<>();
        if (array.isArray()) {
            for (JsonNode entry : array) {
                list.add(entry.asText());
            }
        }
        return list;
    }

}
