package com.graphql.automation.client;

import com.graphql.automation.config.ConfigManager;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class GraphQLClient {

    private static final Logger logger =
            LoggerFactory.getLogger(GraphQLClient.class);

    private static final String ENDPOINT =
            ConfigManager.getInstance().get("graphql.endpoint");

    public static Response executeQuery(String query) {
        return executeQuery(query, new HashMap<>());
    }

    public static Response executeQuery(String query, Map<String, Object> variables) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("query", query);
        requestBody.put("variables", variables);

        logger.info("Executing GraphQL query with {} variable(s)", variables.size());

        return RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post(ENDPOINT)
                .then()
                .extract()
                .response();
    }
}