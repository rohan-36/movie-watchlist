package com.npst.watchlist.service.impl;

import com.npst.watchlist.domain.entity.Movie;
import com.npst.watchlist.domain.entity.Review;
import com.npst.watchlist.domain.repository.MovieRepository;
import com.npst.watchlist.domain.repository.ReviewRepository;
import com.npst.watchlist.dto.request.CreateReviewRequest;
import com.npst.watchlist.dto.request.UpdateReviewRequest;
import com.npst.watchlist.dto.response.PagedResponse;
import com.npst.watchlist.dto.response.ReviewResponse;
import com.npst.watchlist.exception.ResourceNotFoundException;
import com.npst.watchlist.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final MovieRepository movieRepository;
    private final ReviewRepository reviewRepository;

    public ReviewServiceImpl(
            MovieRepository movieRepository,
            ReviewRepository reviewRepository
    ) {
        this.movieRepository = movieRepository;
        this.reviewRepository = reviewRepository;
    }

    @Override
    @Transactional
    public ReviewResponse createReview(
            UUID movieId,
            CreateReviewRequest request
    ) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found."));

        Review review = new Review(
                movie,
                request.getReviewerName(),
                request.getRating(),
                request.getComment()
        );

        return toReviewResponse(reviewRepository.save(review));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReviewResponse> listReviews(
            UUID movieId,
            Pageable pageable
    ) {
        movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found."));

        Page<Review> page = reviewRepository.findByMovieId(movieId, pageable);

        return new PagedResponse<>(
                page.getContent().stream()
                        .map(this::toReviewResponse)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponse getReview(UUID id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found."));

        return toReviewResponse(review);
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(
            UUID id,
            UpdateReviewRequest request
    ) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found."));

        review.setReviewerName(request.getReviewerName());
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        return toReviewResponse(reviewRepository.save(review));
    }

    @Override
    @Transactional
    public void deleteReview(UUID id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found."));

        reviewRepository.delete(review);
    }

    private ReviewResponse toReviewResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getMovie().getId(),
                review.getReviewerName(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
