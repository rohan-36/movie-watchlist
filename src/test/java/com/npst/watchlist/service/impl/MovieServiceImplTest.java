package com.npst.watchlist.service.impl;

import com.npst.watchlist.domain.entity.Movie;
import com.npst.watchlist.domain.projection.MovieStatsProjection;
import com.npst.watchlist.domain.repository.MovieRepository;
import com.npst.watchlist.dto.request.CreateMovieRequest;
import com.npst.watchlist.dto.request.UpdateMovieRequest;
import com.npst.watchlist.exception.DuplicateResourceException;
import com.npst.watchlist.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovieServiceImplTest {

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private MovieServiceImpl movieService;

    private UUID movieId;
    private MovieStatsProjection projection;

    @BeforeEach
    void setUp() {
        movieId = UUID.randomUUID();
        projection = new MovieStatsProjection() {
            @Override
            public UUID getId() { return movieId; }
            @Override
            public String getTitle() { return "Inception"; }
            @Override
            public String getGenre() { return "Sci-Fi"; }
            @Override
            public Integer getReleaseYear() { return 2010; }
            @Override
            public BigDecimal getAverageRating() { return new BigDecimal("4.0"); }
            @Override
            public Long getTotalReviews() { return 3L; }
            @Override
            public Instant getCreatedAt() { return Instant.parse("2026-08-20T10:00:00Z"); }
            @Override
            public Instant getUpdatedAt() { return Instant.parse("2026-08-20T10:00:00Z"); }
        };
    }

    @Test
    void shouldCreateMovieAndReturnComputedStats() {
        CreateMovieRequest request = new CreateMovieRequest("Inception", "Sci-Fi", 2010);
        Movie savedMovie = new Movie("Inception", "Sci-Fi", 2010);

        when(movieRepository.existsByTitleIgnoreCaseAndReleaseYear("Inception", 2010))
                .thenReturn(false);
        when(movieRepository.save(any(Movie.class))).thenReturn(savedMovie);
        when(movieRepository.findMovieStatsById(any())).thenReturn(Optional.of(projection));

        var response = movieService.createMovie(request);

        assertThat(response.getId()).isEqualTo(movieId);
        assertThat(response.getAverageRating()).isEqualByComparingTo("4.0");
        assertThat(response.getTotalReviews()).isEqualTo(3L);
        verify(movieRepository).save(any(Movie.class));
    }

    @Test
    void shouldRejectDuplicateMovie() {
        CreateMovieRequest request = new CreateMovieRequest("Inception", "Sci-Fi", 2010);

        when(movieRepository.existsByTitleIgnoreCaseAndReleaseYear("Inception", 2010))
                .thenReturn(true);

        assertThatThrownBy(() -> movieService.createMovie(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Movie with the same title and release year already exists.");
    }

    @Test
    void shouldReturnMovieStats() {
        when(movieRepository.findMovieStatsById(movieId)).thenReturn(Optional.of(projection));

        var response = movieService.getMovieById(movieId);

        assertThat(response.getTitle()).isEqualTo("Inception");
        assertThat(response.getAverageRating()).isEqualByComparingTo("4.0");
        assertThat(response.getTotalReviews()).isEqualTo(3L);
    }

    @Test
    void shouldThrowWhenMovieStatsAreMissing() {
        when(movieRepository.findMovieStatsById(movieId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieService.getMovieById(movieId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Movie not found.");
    }

    @Test
    void shouldUpdateMovieAfterCheckingDuplicate() {
        Movie movie = new Movie("Old Title", "Drama", 2000);
        UpdateMovieRequest request = new UpdateMovieRequest("New Title", "Sci-Fi", 2010);

        when(movieRepository.findById(movieId)).thenReturn(Optional.of(movie));
        when(movieRepository.existsByTitleIgnoreCaseAndReleaseYearAndIdNot(
                "New Title", 2010, movieId
        )).thenReturn(false);
        when(movieRepository.save(movie)).thenReturn(movie);
        when(movieRepository.findMovieStatsById(movieId)).thenReturn(Optional.of(projection));

        var response = movieService.updateMovie(movieId, request);

        assertThat(movie.getTitle()).isEqualTo("New Title");
        assertThat(movie.getGenre()).isEqualTo("Sci-Fi");
        assertThat(movie.getReleaseYear()).isEqualTo(2010);
        assertThat(response.getId()).isEqualTo(movieId);
    }

    @Test
    void shouldRejectDuplicateMovieUpdate() {
        Movie movie = new Movie("Old Title", "Drama", 2000);
        UpdateMovieRequest request = new UpdateMovieRequest("Inception", "Sci-Fi", 2010);

        when(movieRepository.findById(movieId)).thenReturn(Optional.of(movie));
        when(movieRepository.existsByTitleIgnoreCaseAndReleaseYearAndIdNot(
                "Inception", 2010, movieId
        )).thenReturn(true);

        assertThatThrownBy(() -> movieService.updateMovie(movieId, request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void shouldDeleteExistingMovie() {
        Movie movie = new Movie("Inception", "Sci-Fi", 2010);
        when(movieRepository.findById(movieId)).thenReturn(Optional.of(movie));

        movieService.deleteMovie(movieId);

        verify(movieRepository).delete(movie);
    }
}
