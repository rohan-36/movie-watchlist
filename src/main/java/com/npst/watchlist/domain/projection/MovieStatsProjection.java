//This is an interface projection, not an entity and not a DTO
//Interface Projection → Query-result abstraction
package com.npst.watchlist.domain.projection;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface MovieStatsProjection {

    UUID getId();

    String getTitle();

    String getGenre();

    Integer getReleaseYear();

    BigDecimal getAverageRating();

    Long getTotalReviews();

    Instant getCreatedAt();

    Instant getUpdatedAt();
}
