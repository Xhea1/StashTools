package com.github.xhea1.service.graphql;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Query;
import com.apollographql.apollo.api.Mutation;
import com.apollographql.apollo.rx3.Rx3Apollo;

import io.reactivex.rxjava3.core.Single;

public class GraphQLService {

    private final ApolloClient apolloClient;

    public GraphQLService() {
        apolloClient = new ApolloClient.Builder()
                // TODO: load server url from user config file
                .serverUrl("http://your-graphql-endpoint.com/graphql")
                .build();
    }

    // TODO: add method to query metadata for a given image

    // Execute a GraphQL query
    public Single<?> executeQuery(Query<?> query) {
        return Rx3Apollo.single(apolloClient.query(query));
    }

    // Execute a GraphQL mutation
    public Single<?> executeMutation(Mutation<?> mutation) {
        return Rx3Apollo.single(apolloClient.mutation(mutation));
    }
}
