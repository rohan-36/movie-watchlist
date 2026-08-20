package com.npst.watchlist.controller;

import com.npst.watchlist.domain.entity.Movie;
import com.npst.watchlist.domain.entity.Review;
import com.npst.watchlist.domain.repository.MovieRepository;
import com.npst.watchlist.domain.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReviewControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
        movieRepository.deleteAll();
    }

    @Test
    void shouldCreateReview() throws Exception {
        Movie movie = movieRepository.save(
                new Movie("Inception", "Sci-Fi", 2010)
        );

        String request = """
                {
                    "reviewerName": "Alice",
                    "rating": 5,
                    "comment": "Excellent movie"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/movies/{movieId}/reviews", movie.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.movieId").value(movie.getId().toString()))
                .andExpect(jsonPath("$.reviewerName").value("Alice"))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value("Excellent movie"));
    }

    @Test
    void shouldReturn404WhenCreatingReviewForMissingMovie() throws Exception {
        UUID movieId = UUID.randomUUID();

        String request = """
                {
                    "reviewerName": "Alice",
                    "rating": 5,
                    "comment": "Excellent movie"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/movies/{movieId}/reviews", movieId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void shouldListReviewsForMovie() throws Exception {
        Movie movie = movieRepository.save(
                new Movie("Inception", "Sci-Fi", 2010)
        );

        reviewRepository.save(
                new Review(movie, "Alice", 5, "Excellent")
        );

        reviewRepository.save(
                new Review(movie, "Bob", 3, "Good")
        );

        mockMvc.perform(
                        get("/api/v1/movies/{movieId}/reviews", movie.getId())
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10));
    }

    @Test
    void shouldRejectInvalidReviewPagination() throws Exception {
        Movie movie = movieRepository.save(
                new Movie("Inception", "Sci-Fi", 2010)
        );

        mockMvc.perform(
                        get("/api/v1/movies/{movieId}/reviews", movie.getId())
                                .param("page", "-1")
                                .param("size", "10")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PAGINATION"));

        mockMvc.perform(
                        get("/api/v1/movies/{movieId}/reviews", movie.getId())
                                .param("page", "0")
                                .param("size", "101")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PAGINATION"));
    }

    @Test
    void shouldReturn404WhenListingReviewsForMissingMovie() throws Exception {
        UUID movieId = UUID.randomUUID();

        mockMvc.perform(
                        get("/api/v1/movies/{movieId}/reviews", movieId)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void shouldGetReview() throws Exception {
        Movie movie = movieRepository.save(
                new Movie("Inception", "Sci-Fi", 2010)
        );

        Review review = reviewRepository.save(
                new Review(movie, "Alice", 5, "Excellent")
        );

        mockMvc.perform(
                        get("/api/v1/reviews/{id}", review.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(review.getId().toString()))
                .andExpect(jsonPath("$.movieId").value(movie.getId().toString()))
                .andExpect(jsonPath("$.reviewerName").value("Alice"))
                .andExpect(jsonPath("$.rating").value(5));
    }

    @Test
    void shouldReturn404WhenReviewDoesNotExist() throws Exception {
        UUID reviewId = UUID.randomUUID();

        mockMvc.perform(
                        get("/api/v1/reviews/{id}", reviewId)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void shouldUpdateReview() throws Exception {
        Movie movie = movieRepository.save(
                new Movie("Inception", "Sci-Fi", 2010)
        );

        Review review = reviewRepository.save(
                new Review(movie, "Alice", 5, "Excellent")
        );

        String request = """
                {
                    "reviewerName": "Alice Updated",
                    "rating": 3,
                    "comment": "Changed my mind"
                }
                """;

        mockMvc.perform(
                        put("/api/v1/reviews/{id}", review.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewerName").value("Alice Updated"))
                .andExpect(jsonPath("$.rating").value(3))
                .andExpect(jsonPath("$.comment").value("Changed my mind"));
    }

    @Test
    void shouldRejectInvalidReviewRequest() throws Exception {
        Movie movie = movieRepository.save(
                new Movie("Inception", "Sci-Fi", 2010)
        );

        String request = """
                {
                    "reviewerName": "",
                    "rating": 6,
                    "comment": ""
                }
                """;

        mockMvc.perform(
                        post("/api/v1/movies/{movieId}/reviews", movie.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    void shouldUpdateMovieStatisticsWhenReviewChanges() throws Exception {
        Movie movie = movieRepository.save(
                new Movie("Inception", "Sci-Fi", 2010)
        );

        Review firstReview = reviewRepository.save(
                new Review(movie, "Alice", 5, "Excellent")
        );

        reviewRepository.save(
                new Review(movie, "Bob", 3, "Good")
        );

        mockMvc.perform(
                        get("/api/v1/movies/{id}", movie.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(4.0))
                .andExpect(jsonPath("$.totalReviews").value(2));

        String updateRequest = """
                {
                    "reviewerName": "Alice",
                    "rating": 1,
                    "comment": "Changed my mind"
                }
                """;

        mockMvc.perform(
                        put("/api/v1/reviews/{id}", firstReview.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateRequest)
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        get("/api/v1/movies/{id}", movie.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(2.0))
                .andExpect(jsonPath("$.totalReviews").value(2));
    }

    @Test
    void shouldDeleteReviewAndUpdateMovieStatistics() throws Exception {
        Movie movie = movieRepository.save(
                new Movie("Inception", "Sci-Fi", 2010)
        );

        Review firstReview = reviewRepository.save(
                new Review(movie, "Alice", 5, "Excellent")
        );

        Review secondReview = reviewRepository.save(
                new Review(movie, "Bob", 3, "Good")
        );

        mockMvc.perform(
                        delete("/api/v1/reviews/{id}", secondReview.getId())
                )
                .andExpect(status().isNoContent());

        mockMvc.perform(
                        get("/api/v1/movies/{id}", movie.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(5.0))
                .andExpect(jsonPath("$.totalReviews").value(1));

        mockMvc.perform(
                        delete("/api/v1/reviews/{id}", firstReview.getId())
                )
                .andExpect(status().isNoContent());

        mockMvc.perform(
                        get("/api/v1/movies/{id}", movie.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(0.0))
                .andExpect(jsonPath("$.totalReviews").value(0));
    }

    @Test
    void shouldReturn404WhenDeletingMissingReview() throws Exception {
        UUID reviewId = UUID.randomUUID();

        mockMvc.perform(
                        delete("/api/v1/reviews/{id}", reviewId)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }
}
