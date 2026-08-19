package com.npst.watchlist.dto.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class MovieResponseSerializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @ParameterizedTest
    @MethodSource("ratingValues")
    void averageRatingShouldSerializeWithExactlyOneDecimalPlace(
            String rating,
            String expectedJsonValue
    ) throws Exception {

        MovieResponse response = new MovieResponse(
                UUID.randomUUID(),
                "Inception",
                "Sci-Fi",
                2010,
                new BigDecimal(rating),
                1L,
                null,
                null
        );

        String json = objectMapper.writeValueAsString(response);

        JsonNode jsonNode = objectMapper.readTree(json);
        JsonNode averageRating = jsonNode.get("averageRating");

        assertTrue(averageRating.isNumber());

        assertEquals(expectedJsonValue, averageRating.toString());

        assertTrue(
                json.contains("\"averageRating\":" + expectedJsonValue)
        );
    }

    private static Stream<Arguments> ratingValues() {
        return Stream.of(
                Arguments.of("3.9", "3.9"),
                Arguments.of("4.5", "4.5"),
                Arguments.of("4.0", "4.0"),
                Arguments.of("0.0", "0.0")
        );
    }
}