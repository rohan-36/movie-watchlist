package com.npst.watchlist.domain.repository;

import com.npst.watchlist.domain.entity.Movie;
import com.npst.watchlist.domain.entity.Review;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ReviewRepositoryIntegrationTest {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    private Movie movieWithReviews;
    private Movie movieWithoutReviews;
    private Movie anotherMovie;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
        movieRepository.deleteAll();

        movieWithReviews = movieRepository.save(
                new Movie("Inception", "Sci-Fi", 2010)
        );

        movieWithoutReviews = movieRepository.save(
                new Movie("Interstellar", "Sci-Fi", 2014)
        );

        anotherMovie = movieRepository.save(
                new Movie("The Dark Knight", "Action", 2008)
        );
    }

    @Test
    void shouldReturnOnlyReviewsBelongingToRequestedMovie() {
        Review firstReview = reviewRepository.save(
                new Review(
                        movieWithReviews,
                        "Alice",
                        5,
                        "Excellent"
                )
        );

        reviewRepository.save(
                new Review(
                        anotherMovie,
                        "Bob",
                        3,
                        "Good"
                )
        );

        Page<Review> result = reviewRepository.findByMovieId(
                movieWithReviews.getId(),
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent())
                .hasSize(1)
                .extracting(Review::getId)
                .containsExactly(firstReview.getId());

        assertThat(result.getContent().get(0).getMovie().getId())
                .isEqualTo(movieWithReviews.getId());
    }

    @Test
    void shouldOrderReviewsByCreatedAtDescendingAndIdAscending() {
        reviewRepository.save(
                new Review(
                        movieWithReviews,
                        "Alice",
                        4,
                        "First review"
                )
        );

        reviewRepository.save(
                new Review(
                        movieWithReviews,
                        "Bob",
                        5,
                        "Second review"
                )
        );

        reviewRepository.save(
                new Review(
                        movieWithReviews,
                        "Charlie",
                        3,
                        "Third review"
                )
        );

        Page<Review> result = reviewRepository.findByMovieId(
                movieWithReviews.getId(),
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(3);

        for (int i = 0; i < result.getContent().size() - 1; i++) {
            Review current = result.getContent().get(i);
            Review next = result.getContent().get(i + 1);

            assertThat(current.getCreatedAt())
                    .isAfterOrEqualTo(next.getCreatedAt());

            if (current.getCreatedAt().equals(next.getCreatedAt())) {
                assertThat(current.getId())
                        .isLessThanOrEqualTo(next.getId());
            }
        }
    }

    @Test
    void shouldReturnCorrectPageMetadata() {
        for (int i = 0; i < 5; i++) {
            reviewRepository.save(
                    new Review(
                            movieWithReviews,
                            "Reviewer " + i,
                            (i % 5) + 1,
                            "Review " + i
                    )
            );
        }

        Page<Review> result = reviewRepository.findByMovieId(
                movieWithReviews.getId(),
                PageRequest.of(1, 2)
        );

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(2);
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.isFirst()).isFalse();
        assertThat(result.isLast()).isFalse();
    }

    @Test
    void shouldReturnEmptyPageWhenMovieHasNoReviews() {
        Page<Review> result = reviewRepository.findByMovieId(
                movieWithoutReviews.getId(),
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getNumber()).isZero();
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getTotalPages()).isZero();
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isTrue();
    }
}
