package com.github.xhea1.stashtools.service.party;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.xhea1.stashtools.model.party.PostRecord;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A service for making HTTP requests and dynamically parsing JSON responses.
 * This service is specifically designed to query the `/search_hash/{file_hash}` endpoint
 * and extract the `posts` data, excluding nested fields like `file` and `attachments`.
 */
public class HTTPService {
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String baseUrl;

    /**
     * Constructs an HTTPService with the specified base URL.
     *
     * @param baseUrl The base URL for the API endpoint.
     */
    public HTTPService(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * Retrieves the `posts` data from the `/search_hash/{file_hash}` endpoint.
     *
     * @param fileHash The hash used to query the endpoint.
     * @return A list of {@link PostRecord} containing the `posts` data.
     * @throws IOException If the request fails or the response is invalid.
     */
    public List<PostRecord> getPostsByHash(String fileHash) throws IOException {
        String url = baseUrl + "/search_hash/" + fileHash;

        // Create HTTP GET request
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String jsonResponse = response.body().string();
                JsonNode rootNode = objectMapper.readTree(jsonResponse);

                // Extract `posts` array
                JsonNode posts = rootNode.get("posts");
                if (posts != null && posts.isArray()) {
                    List<PostRecord> postRecords = new ArrayList<>();
                    for (JsonNode post : posts) {
                        PostRecord postRecord = new PostRecord(
                                post.get("file_id").asInt(),
                                post.get("id").asText(),
                                post.get("user").asText(),
                                post.get("service").asText(),
                                post.get("title").asText(),
                                post.get("published").asText(),
                                post.get("substring").asText()
                        );
                        postRecords.add(postRecord);
                    }
                    return postRecords;
                }
            }
        }
        throw new IOException("Failed to retrieve posts or invalid response.");
    }
}
