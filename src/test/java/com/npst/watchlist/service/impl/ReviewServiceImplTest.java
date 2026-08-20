package com.npst.watchlist.service.impl;

import com.npst.watchlist.domain.entity.Movie;
import com.npst.watchlist.domain.entity.Review;
import com.npst.watchlist.domain.repository.MovieRepository;
import com.npst.watchlist.domain.repository.ReviewRepository;
import com.npst.watchlist.dto.request.CreateReviewRequest;
import com.npst.watchlist.dto.request.UpdateReviewRequest;
import com.npst.watchlist.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private UUID movieId;
    private UUID reviewId;
    private Movie movie;

    @BeforeEach
    void setUp() throws Exception {
        movieId = UUID.randomUUID();
        reviewId = UUID.randomUUID();
        movie = new Movie("Inception", "Sci-Fi", 2010);
        setId(movie, movieId);
    }

    @Test
    void shouldCreateReviewForExistingMovie() {
        CreateReviewRequest request = new CreateReviewRequest(
                "Alice", 5, "Excellent movie"
        );
        Review review = new Review(movie, "Alice", 5, "Excellent movie");

        when(movieRepository.findById(movieId)).thenReturn(Optional.of(movie));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        var response = reviewService.createReview(movieId, request);

        assertThat(response.getMovieId()).isEqualTo(movieId);
        assertThat(response.getReviewerName()).isEqualTo("Alice");
        assertThat(response.getRating()).isEqualTo(5);
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void shouldRejectReviewForMissingMovie() {
        CreateReviewRequest request = new CreateReviewRequest("Alice", 5, "Excellent movie");
        when(movieRepository.findById(movieId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.createReview(movieId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Movie not found.");
    }

    @Test
    void shouldListReviewsOnlyForExistingMovie() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(movieRepository.findById(movieId)).thenReturn(Optional.of(movie));
        when(reviewRepository.findByMovieId(movieId, pageable))
                .thenReturn(new PageImpl<>(java.util.List.of(), pageable, 0));

        var response = reviewService.listReviews(movieId, pageable);

        assertThat(response.getContent()).isEmpty();
        assertThat(response.getPage()).isZero();
        assertThat(response.getSize()).isEqualTo(10);
        verify(reviewRepository).findByMovieId(movieId, pageable);
    }

    @Test
    void shouldReturnNotFoundWhenListingReviewsForMissingMovie() {
        when(movieRepository.findById(movieId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.listReviews(
                movieId,
                PageRequest.of(0, 10)
        )).isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Movie not found.");
    }

    @Test
    void shouldGetReview() throws Exception {
        Review review = new Review(movie, "Alice", 5, "Excellent movie");
        setId(review, reviewId);
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        var response = reviewService.getReview(reviewId);

        assertThat(response.getId()).isEqualTo(reviewId);
        assertThat(response.getMovieId()).isEqualTo(movieId);
        assertThat(response.getReviewerName()).isEqualTo("Alice");
        assertThat(response.getRating()).isEqualTo(5);
        assertThat(response.getComment()).isEqualTo("Excellent movie");
    }

    @Test
    void shouldUpdateReview() throws Exception {
        Review review = new Review(movie, "Alice", 5, "Excellent movie");
        setId(review, reviewId);
        UpdateReviewRequest request = new UpdateReviewRequest(
                "Bob", 4, "Very good"
        );

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(reviewRepository.save(review)).thenReturn(review);

        var response = reviewService.updateReview(reviewId, request);

        assertThat(review.getReviewerName()).isEqualTo("Bob");
        assertThat(review.getRating()).isEqualTo(4);
        assertThat(review.getComment()).isEqualTo("Very good");
        assertThat(response.getReviewerName()).isEqualTo("Bob");
        assertThat(response.getMovieId()).isEqualTo(movieId);
    }

    @Test
    void shouldDeleteExistingReview() throws Exception {
        Review review = new Review(movie, "Alice", 5, "Excellent movie");
        setId(review, reviewId);
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        reviewService.deleteReview(reviewId);

        verify(reviewRepository).delete(review);
    }

    private void setId(Object entity, UUID id) throws Exception {
        Field field = entity.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
    }
}
