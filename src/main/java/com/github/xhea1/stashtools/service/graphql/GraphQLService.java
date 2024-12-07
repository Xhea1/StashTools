package com.github.xhea1.stashtools.service.graphql;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.xhea1.stashtools.model.party.PostRecord;
import okhttp3.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for GraphQL operations
 */
public class GraphQLService {

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
    private static final Logger logger = LogManager.getLogger(GraphQLService.class);
    private final String endpoint;

    public GraphQLService(String endpoint) {
        this.endpoint = endpoint + "/graphql";
    }

    // Helper method to convert a Java object to JSON string using Jackson
    private static String toJson(Object object) throws JsonProcessingException {
            return objectMapper.writeValueAsString(object);
    }

    public void findScenesByPathRegex(String path) {
        // Create a Map for the request body
        Map<String, Object> requestBodyMap = new HashMap<>();
        requestBodyMap.put("query", FIND_SCENES_BY_PATH_REGEX_QUERY.formatted(path));

        sendGraphQLRequest(requestBodyMap);
    }

    public void updateScene(String sceneId, PostRecord data) {
        Map<String, Object> requestBodyMap = new HashMap<>();
        requestBodyMap.put("query", UPDATE_SCENE_QUERY);
        Map<String, Object> variables = new HashMap<>();
        requestBodyMap.put("variables", variables);
        Map<String, Object> input = new HashMap<>();
        variables.put("input", input);
        // TODO: smarter logic to decide what needs to be replaced
        input.put("id", sceneId);
        input.put("title", data.title());
        input.put("details", data.substring());
        input.put("date", data.published());

            sendGraphQLRequest(requestBodyMap);
    }

    private void sendGraphQLRequest(Map<String, Object> requestBodyMap)  {
        // Convert the request body Map to JSON
        RequestBody requestBody = null;
        try {
            requestBody = RequestBody.create(toJson(requestBodyMap), MediaType.parse("application/json"));
        } catch (JsonProcessingException e) {
            logger.error("Could not create JSON",e);
        }

        // Create the HTTP request
        Request request = new Request.Builder().url(endpoint).post(requestBody) // Send as POST request
                .addHeader("Content-Type", "application/json") // Set content type to JSON
                .addHeader("ApiKey", System.getenv("STASH_API_KEY")).build();

        // Send the request and handle the response
        try(Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                // Parse the response body (assuming it's JSON) and print it
                String responseBody = response.body().string();
                // TODO: make debug
                logger.info("GraphQL Response: {}" ,responseBody);


            } else {
                logger.error("Error: {}" , response.code());
                logger.error(response.body());
            }
        } catch (IOException e) {
            logger.error("", e);
        }
    }


}
