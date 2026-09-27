package com.graphql.automation.tests;

import com.graphql.automation.client.GraphQLClient;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

@Feature("GraphQL Query Testing")
public class GraphQLQueryTest {

    private static final Logger logger =
            LoggerFactory.getLogger(GraphQLQueryTest.class);

    @Test
    @Story("Basic Query")
    @Description("Verify querying a single country by code returns correct fields")
    @Severity(SeverityLevel.CRITICAL)
    public void testGetCountryByCode() {
        String query = """
                query {
                    country(code: "IN") {
                        name
                        capital
                        currency
                    }
                }
                """;

        Response response = GraphQLClient.executeQuery(query);

        Assert.assertEquals(response.statusCode(), 200);

        String name = response.jsonPath().getString("data.country.name");
        String capital = response.jsonPath().getString("data.country.capital");

        Assert.assertEquals(name, "India");
        Assert.assertEquals(capital, "New Delhi");

        logger.info("Country query verified: {} — {}", name, capital);
    }

    @Test
    @Story("Nested Field Selection")
    @Description("Verify querying nested fields — country's continent and its languages")
    @Severity(SeverityLevel.CRITICAL)
    public void testNestedFieldSelection() {
        String query = """
                query {
                    country(code: "IN") {
                        name
                        continent {
                            name
                        }
                        languages {
                            name
                        }
                    }
                }
                """;

        Response response = GraphQLClient.executeQuery(query);

        Assert.assertEquals(response.statusCode(), 200);

        String continentName = response.jsonPath()
                .getString("data.country.continent.name");

        Assert.assertEquals(continentName, "Asia");

        logger.info("Nested field query verified — continent: {}", continentName);
    }

    @Test
    @Story("Query with Variables")
    @Description("Verify passing a country code as a GraphQL variable instead of hardcoding it")
    @Severity(SeverityLevel.NORMAL)
    public void testQueryWithVariables() {
        String query = """
                query GetCountry($code: ID!) {
                    country(code: $code) {
                        name
                        emoji
                    }
                }
                """;

        Map<String, Object> variables = new HashMap<>();
        variables.put("code", "US");

        Response response = GraphQLClient.executeQuery(query, variables);

        Assert.assertEquals(response.statusCode(), 200);

        String name = response.jsonPath().getString("data.country.name");
        Assert.assertEquals(name, "United States");

        logger.info("Variable-based query verified: {}", name);
    }

    @Test
    @Story("Error Handling")
    @Description("Verify querying an invalid country code returns a GraphQL error, not an HTTP error")
    @Severity(SeverityLevel.NORMAL)
    public void testInvalidCountryCode() {
        String query = """
                query {
                    country(code: "ZZ") {
                        name
                    }
                }
                """;

        Response response = GraphQLClient.executeQuery(query);

        // GraphQL convention: HTTP 200 even on logical errors —
        // errors live inside the response body, not the status code
        Assert.assertEquals(response.statusCode(), 200);

        String countryData = response.jsonPath().getString("data.country");
        Assert.assertNull(countryData, "Invalid country code should return null data");

        logger.info("Invalid code correctly returned null country data");
    }

    @Test
    @Story("Malformed Query")
    @Description("Verify a syntactically invalid GraphQL query returns an errors array")
    @Severity(SeverityLevel.NORMAL)
    public void testMalformedQuery() {
        String query = """
                query {
                    country(code: "IN") {
                        nonExistentField
                    }
                }
                """;

        Response response = GraphQLClient.executeQuery(query);

        Assert.assertEquals(response.statusCode(), 400);

        String errorMessage = response.jsonPath().getString("errors[0].message");
        Assert.assertNotNull(errorMessage, "Malformed query should return an errors array");

        logger.info("Malformed query correctly returned error: {}", errorMessage);
    }
}