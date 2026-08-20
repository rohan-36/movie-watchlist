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
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MovieControllerIntegrationTest {

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
    void shouldCreateMovie() throws Exception {
        String request = """
                {
                    "title": "Inception",
                    "genre": "Sci-Fi",
                    "releaseYear": 2010
                }
                """;

        mockMvc.perform(
                        post("/api/v1/movies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Inception"))
                .andExpect(jsonPath("$.genre").value("Sci-Fi"))
                .andExpect(jsonPath("$.releaseYear").value(2010))
                .andExpect(jsonPath("$.averageRating").value(0.0))
                .andExpect(jsonPath("$.totalReviews").value(0));
    }

    @Test
    void shouldGetMovieWithDynamicStatistics() throws Exception {
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
                        get("/api/v1/movies/{id}", movie.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Inception"))
                .andExpect(jsonPath("$.averageRating").value(4.0))
                .andExpect(jsonPath("$.totalReviews").value(2));
    }

    @Test
    void shouldReturn404WhenMovieDoesNotExist() throws Exception {
        UUID movieId = UUID.randomUUID();

        mockMvc.perform(
                        get("/api/v1/movies/{id}", movieId)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void shouldListMoviesWithPagination() throws Exception {
        movieRepository.save(
                new Movie("Movie 1", "Drama", 2001)
        );
        movieRepository.save(
                new Movie("Movie 2", "Drama", 2002)
        );
        movieRepository.save(
                new Movie("Movie 3", "Drama", 2003)
        );

        mockMvc.perform(
                        get("/api/v1/movies")
                                .param("page", "0")
                                .param("size", "2")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void shouldFilterMoviesByGenre() throws Exception {
        movieRepository.save(
                new Movie("Inception", "Sci-Fi", 2010)
        );
        movieRepository.save(
                new Movie("Interstellar", "Sci-Fi", 2014)
        );
        movieRepository.save(
                new Movie("The Dark Knight", "Action", 2008)
        );

        mockMvc.perform(
                        get("/api/v1/movies")
                                .param("genre", "sCi-Fi")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void shouldRejectInvalidPagination() throws Exception {
        mockMvc.perform(
                        get("/api/v1/movies")
                                .param("page", "-1")
                                .param("size", "10")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PAGINATION"));

        mockMvc.perform(
                        get("/api/v1/movies")
                                .param("page", "0")
                                .param("size", "101")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PAGINATION"));
    }

    @Test
    void shouldRejectInvalidMovieRequest() throws Exception {
        String request = """
                {
                    "title": "",
                    "genre": "",
                    "releaseYear": 1800
                }
                """;

        mockMvc.perform(
                        post("/api/v1/movies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    void shouldRejectDuplicateMovie() throws Exception {
        movieRepository.save(
                new Movie("Inception", "Sci-Fi", 2010)
        );

        String request = """
                {
                    "title": "inception",
                    "genre": "Sci-Fi",
                    "releaseYear": 2010
                }
                """;

        mockMvc.perform(
                        post("/api/v1/movies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_RESOURCE"));
    }

    @Test
    void shouldUpdateMovie() throws Exception {
        Movie movie = movieRepository.save(
                new Movie("Inception", "Sci-Fi", 2010)
        );

        String request = """
                {
                    "title": "Inception Updated",
                    "genre": "Drama",
                    "releaseYear": 2011
                }
                """;

        mockMvc.perform(
                        put("/api/v1/movies/{id}", movie.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Inception Updated"))
                .andExpect(jsonPath("$.genre").value("Drama"))
                .andExpect(jsonPath("$.releaseYear").value(2011));
    }

    @Test
    void shouldDeleteMovieAndItsReviews() throws Exception {
        Movie movie = movieRepository.save(
                new Movie("Inception", "Sci-Fi", 2010)
        );

        reviewRepository.save(
                new Review(movie, "Alice", 5, "Excellent")
        );

        mockMvc.perform(
                        delete("/api/v1/movies/{id}", movie.getId())
                )
                .andExpect(status().isNoContent());

        mockMvc.perform(
                        get("/api/v1/movies/{id}", movie.getId())
                )
                .andExpect(status().isNotFound());

        org.assertj.core.api.Assertions.assertThat(
                reviewRepository.findByMovieId(
                        movie.getId(),
                        org.springframework.data.domain.PageRequest.of(0, 10)
                ).getTotalElements()
        ).isZero();
    }
}
