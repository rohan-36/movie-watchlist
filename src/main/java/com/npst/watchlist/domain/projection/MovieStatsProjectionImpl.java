package com.npst.watchlist.domain.projection;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class MovieStatsProjectionImpl implements MovieStatsProjection {

    private final UUID id;
    private final String title;
    private final String genre;
    private final Integer releaseYear;
    private final BigDecimal averageRating;
    private final Long totalReviews;
    private final Instant createdAt;
    private final Instant updatedAt;

    public MovieStatsProjectionImpl(
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

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getGenre() {
        return genre;
    }

    @Override
    public Integer getReleaseYear() {
        return releaseYear;
    }

    @Override
    public BigDecimal getAverageRating() {
        return averageRating;
    }

    @Override
    public Long getTotalReviews() {
        return totalReviews;
    }

    @Override
    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
