package com.npst.watchlist.controller;

import com.npst.watchlist.dto.request.CreateReviewRequest;
import com.npst.watchlist.dto.request.UpdateReviewRequest;
import com.npst.watchlist.dto.response.PagedResponse;
import com.npst.watchlist.dto.response.ReviewResponse;
import com.npst.watchlist.exception.InvalidPaginationException;
import com.npst.watchlist.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Reviews", description = "Movie review management APIs")
public class ReviewController {

    private static final int MAX_PAGE_SIZE = 100;

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Operation(summary = "Create a review", description = "Creates a review for an existing movie.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Review created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Movie not found")
    })
    @PostMapping("/movies/{movieId}/reviews")
    public ResponseEntity<ReviewResponse> createReview(
            @PathVariable UUID movieId,
            @Valid @RequestBody CreateReviewRequest request
    ) {
        ReviewResponse response = reviewService.createReview(movieId, request);
        return ResponseEntity
                .created(URI.create("/api/v1/reviews/" + response.getId()))
                .body(response);
    }

    @Operation(summary = "List reviews", description = "Returns paginated reviews for a movie.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reviews returned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "404", description = "Movie not found")
    })
    @GetMapping("/movies/{movieId}/reviews")
    public ResponseEntity<PagedResponse<ReviewResponse>> listReviews(
            @PathVariable UUID movieId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                reviewService.listReviews(movieId, pageable(page, size))
        );
    }

    @Operation(summary = "Get a review", description = "Returns a review by ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Review found"),
            @ApiResponse(responseCode = "404", description = "Review not found")
    })
    @GetMapping("/reviews/{id}")
    public ResponseEntity<ReviewResponse> getReview(@PathVariable UUID id) {
        return ResponseEntity.ok(reviewService.getReview(id));
    }

    @Operation(summary = "Update a review", description = "Updates an existing review.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Review updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Review not found")
    })
    @PutMapping("/reviews/{id}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateReviewRequest request
    ) {
        return ResponseEntity.ok(reviewService.updateReview(id, request));
    }

    @Operation(summary = "Delete a review", description = "Deletes an existing review.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Review deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Review not found")
    })
    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable UUID id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }


    private Pageable pageable(int page, int size) {
        if (page < 0) {
            throw new InvalidPaginationException("Page must be greater than or equal to 0.");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new InvalidPaginationException("Page size must be between 1 and 100.");
        }
        return PageRequest.of(page, size);
    }
}
