package com.npst.watchlist.service;

import com.npst.watchlist.dto.request.CreateReviewRequest;
import com.npst.watchlist.dto.request.UpdateReviewRequest;
import com.npst.watchlist.dto.response.PagedResponse;
import com.npst.watchlist.dto.response.ReviewResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ReviewService {

    ReviewResponse createReview(UUID movieId, CreateReviewRequest request);

    PagedResponse<ReviewResponse> listReviews(UUID movieId, Pageable pageable);

    ReviewResponse getReview(UUID id);

    ReviewResponse updateReview(UUID id, UpdateReviewRequest request);

    void deleteReview(UUID id);
}
