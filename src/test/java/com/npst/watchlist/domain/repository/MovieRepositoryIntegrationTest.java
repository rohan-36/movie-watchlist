package com.npst.watchlist.domain.repository;

import com.npst.watchlist.domain.entity.Movie;
import com.npst.watchlist.domain.entity.Review;
import com.npst.watchlist.domain.projection.MovieStatsProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MovieRepositoryIntegrationTest {

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
    void shouldReturnZeroRatingAndZeroReviewsForMovieWithNoReviews() {
        Movie movie = movieRepository.save(
                new Movie(
                        "Interstellar",
                        "Sci-Fi",
                        2014
                )
        );

        MovieStatsProjection stats =
                movieRepository.findMovieStatsById(movie.getId())
                        .orElseThrow();

        assertThat(stats.getId())
                .isEqualTo(movie.getId());

        assertThat(stats.getTitle())
                .isEqualTo("Interstellar");

        assertThat(stats.getAverageRating())
                .isEqualByComparingTo(BigDecimal.ZERO);

        assertThat(stats.getTotalReviews())
                .isZero();
    }

    @Test
    void shouldReturnCorrectAverageRatingAndTotalReviews() {
        Movie movie = movieRepository.save(
                new Movie(
                        "Inception",
                        "Sci-Fi",
                        2010
                )
        );

        reviewRepository.save(
                new Review(
                        movie,
                        "Alice",
                        5,
                        "Excellent movie"
                )
        );

        reviewRepository.save(
                new Review(
                        movie,
                        "Bob",
                        4,
                        "Very good"
                )
        );

        reviewRepository.save(
                new Review(
                        movie,
                        "Charlie",
                        3,
                        "Good movie"
                )
        );

        MovieStatsProjection stats =
                movieRepository.findMovieStatsById(movie.getId())
                        .orElseThrow();

        assertThat(stats.getAverageRating())
                .isEqualByComparingTo(new BigDecimal("4.0"));

        assertThat(stats.getTotalReviews())
                .isEqualTo(3);
    }

    @Test
    void shouldFilterMoviesByGenreCaseInsensitively() {
        movieRepository.save(
                new Movie(
                        "Inception",
                        "Sci-Fi",
                        2010
                )
        );

        movieRepository.save(
                new Movie(
                        "Interstellar",
                        "Sci-Fi",
                        2014
                )
        );

        movieRepository.save(
                new Movie(
                        "The Dark Knight",
                        "Action",
                        2008
                )
        );

        Page<MovieStatsProjection> result =
                movieRepository.findMovieStats(
                        "sCi-Fi",
                        PageRequest.of(0, 10)
                );

        assertThat(result.getTotalElements())
                .isEqualTo(2);

        assertThat(result.getContent())
                .hasSize(2);

        assertThat(result.getContent())
                .extracting(MovieStatsProjection::getGenre)
                .containsOnly("Sci-Fi");
    }

    @Test
    void shouldReturnCorrectPaginationMetadata() {
        for (int i = 1; i <= 5; i++) {
            movieRepository.save(
                    new Movie(
                            "Movie " + i,
                            "Drama",
                            2000 + i
                    )
            );
        }

        Page<MovieStatsProjection> result =
                movieRepository.findMovieStats(
                        null,
                        PageRequest.of(1, 2)
                );

        assertThat(result.getContent())
                .hasSize(2);

        assertThat(result.getNumber())
                .isEqualTo(1);

        assertThat(result.getSize())
                .isEqualTo(2);

        assertThat(result.getTotalElements())
                .isEqualTo(5);

        assertThat(result.getTotalPages())
                .isEqualTo(3);

        assertThat(result.isFirst())
                .isFalse();

        assertThat(result.isLast())
                .isFalse();
    }

    @Test
    void shouldOrderMoviesByCreatedAtDescendingAndIdAscending() {
        movieRepository.save(
                new Movie(
                        "Movie A",
                        "Drama",
                        2001
                )
        );

        movieRepository.save(
                new Movie(
                        "Movie B",
                        "Drama",
                        2002
                )
        );

        movieRepository.save(
                new Movie(
                        "Movie C",
                        "Drama",
                        2003
                )
        );

        Page<MovieStatsProjection> result =
                movieRepository.findMovieStats(
                        null,
                        PageRequest.of(0, 10)
                );

        List<MovieStatsProjection> movies = result.getContent();

        assertThat(movies)
                .hasSize(3);

        for (int i = 0; i < movies.size() - 1; i++) {
            MovieStatsProjection current = movies.get(i);
            MovieStatsProjection next = movies.get(i + 1);

            assertThat(current.getCreatedAt())
                    .isAfterOrEqualTo(next.getCreatedAt());

            if (current.getCreatedAt().equals(next.getCreatedAt())) {
                assertThat(current.getId())
                        .isLessThanOrEqualTo(next.getId());
            }
        }
    }
}