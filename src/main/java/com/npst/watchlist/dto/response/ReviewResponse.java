package com.npst.watchlist.dto.response;

import java.time.Instant;
import java.util.UUID;

public class ReviewResponse {

    private UUID id;
    private UUID movieId;
    private String reviewerName;
    private Integer rating;
    private String comment;
    private Instant createdAt;
    private Instant updatedAt;

    public ReviewResponse() {
    }

    public ReviewResponse(
            UUID id,
            UUID movieId,
            String reviewerName,
            Integer rating,
            String comment,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.movieId = movieId;
        this.reviewerName = reviewerName;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getMovieId() {
        return movieId;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public Integer getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
