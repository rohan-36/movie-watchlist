package com.npst.watchlist.controller;

import com.npst.watchlist.dto.request.CreateMovieRequest;
import com.npst.watchlist.dto.request.UpdateMovieRequest;
import com.npst.watchlist.dto.response.MovieResponse;
import com.npst.watchlist.dto.response.PagedResponse;
import com.npst.watchlist.exception.InvalidPaginationException;
import com.npst.watchlist.service.MovieService;
import jakarta.validation.Valid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovieControllerTest {

    @Mock
    private MovieService movieService;

    @InjectMocks
    private MovieController movieController;

    private UUID movieId;
    private MovieResponse movieResponse;

    @BeforeEach
    void setUp() {
        movieId = UUID.randomUUID();
        movieResponse = new MovieResponse(
                movieId, "Inception", "Sci-Fi", 2010,
                new BigDecimal("4.5"), 2L,
                Instant.parse("2026-08-20T10:00:00Z"),
                Instant.parse("2026-08-20T10:00:00Z")
        );
    }

    @Test
    void shouldCreateMovieWithCreatedStatus() {
        when(movieService.createMovie(org.mockito.ArgumentMatchers.any()))
                .thenReturn(movieResponse);

        var response = movieController.createMovie(
                new CreateMovieRequest("Inception", "Sci-Fi", 2010)
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation().toString())
                .isEqualTo("/api/v1/movies/" + movieId);
        assertThat(response.getBody()).isEqualTo(movieResponse);
    }

    @Test
    void shouldListMoviesWithRequestedPagination() {
        PagedResponse<MovieResponse> page = new PagedResponse<>(
                List.of(movieResponse), 1, 10, 1, 1, false, true
        );
        when(movieService.listMovies("Sci-Fi", PageRequest.of(1, 10)))
                .thenReturn(page);

        var response = movieController.listMovies("Sci-Fi", 1, 10);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(page);
        verify(movieService).listMovies("Sci-Fi", PageRequest.of(1, 10));
    }

    @Test
    void shouldRejectNegativePage() {
        assertThatThrownBy(() -> movieController.listMovies(null, -1, 20))
                .isInstanceOf(InvalidPaginationException.class);
    }

    @Test
    void shouldRejectPageSizeAboveMaximum() {
        assertThatThrownBy(() -> movieController.listMovies(null, 0, 101))
                .isInstanceOf(InvalidPaginationException.class);
    }

    @Test
    void shouldDeleteMovieWithNoContent() {
        var response = movieController.deleteMovie(movieId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(movieService).deleteMovie(movieId);
    }
}
