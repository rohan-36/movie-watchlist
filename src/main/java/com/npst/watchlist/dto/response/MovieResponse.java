package com.npst.watchlist.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class MovieResponse {

    private UUID id;
    private String title;
    private String genre;
    private Integer releaseYear;
    private BigDecimal averageRating;
    private Long totalReviews;
    private Instant createdAt;
    private Instant updatedAt;

    public MovieResponse() {
    }

    public MovieResponse(
            UUID id,
            String title,
            String genre,
            Integer releaseYear,
            BigDecimal averageRating,
            Long totalReviews,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.releaseYear = releaseYear;
        this.averageRating = averageRating;
        this.totalReviews = totalReviews;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getGenre() {
        return genre;
    }

    public Integer getReleaseYear() {
        return releaseYear;
    }

    public BigDecimal getAverageRating() {
        return averageRating;
    }

    public Long getTotalReviews() {
        return totalReviews;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
