package com.npst.watchlist.controller;

import com.npst.watchlist.dto.request.CreateReviewRequest;
import com.npst.watchlist.dto.request.UpdateReviewRequest;
import com.npst.watchlist.dto.response.PagedResponse;
import com.npst.watchlist.dto.response.ReviewResponse;
import com.npst.watchlist.exception.InvalidPaginationException;
import com.npst.watchlist.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private ReviewController reviewController;

    private UUID movieId;
    private UUID reviewId;
    private ReviewResponse reviewResponse;

    @BeforeEach
    void setUp() {
        movieId = UUID.randomUUID();
        reviewId = UUID.randomUUID();
        reviewResponse = new ReviewResponse(
                reviewId, movieId, "Alice", 5, "Excellent",
                Instant.parse("2026-08-20T10:00:00Z"),
                Instant.parse("2026-08-20T10:00:00Z")
        );
    }

    @Test
    void shouldCreateReviewWithCreatedStatus() {
        when(reviewService.createReview(any(), any())).thenReturn(reviewResponse);

        var response = reviewController.createReview(
                movieId,
                new CreateReviewRequest("Alice", 5, "Excellent")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation().toString())
                .isEqualTo("/api/v1/reviews/" + reviewId);
        assertThat(response.getBody()).isEqualTo(reviewResponse);
    }

    @Test
    void shouldListReviewsWithRequestedPagination() {
        PagedResponse<ReviewResponse> page = new PagedResponse<>(
                List.of(reviewResponse), 0, 20, 1, 1, true, true
        );
        when(reviewService.listReviews(movieId, PageRequest.of(0, 20)))
                .thenReturn(page);

        var response = reviewController.listReviews(movieId, 0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(page);
        verify(reviewService).listReviews(movieId, PageRequest.of(0, 20));
    }

    @Test
    void shouldRejectInvalidReviewPagination() {
        assertThatThrownBy(() -> reviewController.listReviews(movieId, 0, 101))
                .isInstanceOf(InvalidPaginationException.class);
    }

    @Test
    void shouldDeleteReviewWithNoContent() {
        var response = reviewController.deleteReview(reviewId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(reviewService).deleteReview(reviewId);
    }
}
