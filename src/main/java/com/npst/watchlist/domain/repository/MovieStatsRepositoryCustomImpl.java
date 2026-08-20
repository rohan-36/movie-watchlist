package com.npst.watchlist.domain.repository;

import com.npst.watchlist.domain.projection.MovieStatsProjection;
import com.npst.watchlist.domain.projection.MovieStatsProjectionImpl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class MovieStatsRepositoryCustomImpl
        implements MovieStatsRepositoryCustom {

    private final EntityManager entityManager;

    public MovieStatsRepositoryCustomImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    private static final String BASE_QUERY = """
        SELECT
            m.id AS id,
            m.title AS title,
            m.genre AS genre,
            m.release_year AS releaseYear,
            COALESCE(
                ROUND(
                    CAST(AVG(r.rating) AS numeric),
                    1
                ),
                0.0
            ) AS averageRating,
            COUNT(r.id) AS totalReviews,
            m.created_at AS createdAt,
            m.updated_at AS updatedAt
        FROM movies m
        LEFT JOIN reviews r ON r.movie_id = m.id
        WHERE (
            CAST(:genre AS VARCHAR) IS NULL
            OR LOWER(m.genre) = LOWER(CAST(:genre AS VARCHAR))
        )
        GROUP BY
            m.id,
            m.title,
            m.genre,
            m.release_year,
            m.created_at,
            m.updated_at
        ORDER BY m.created_at DESC, m.id ASC
        """;

    private static final String COUNT_QUERY = """
        SELECT COUNT(m.id)
        FROM movies m
        WHERE (
            CAST(:genre AS VARCHAR) IS NULL
            OR LOWER(m.genre) = LOWER(CAST(:genre AS VARCHAR))
        )
        """;

    @Override
    public Page<MovieStatsProjection> findMovieStats(
            String genre,
            Pageable pageable
    ) {
        List<Tuple> tuples = entityManager
                .createNativeQuery(BASE_QUERY, Tuple.class)
                .setParameter("genre", genre)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        List<MovieStatsProjection> projections = tuples.stream()
                .<MovieStatsProjection>map(tuple -> new MovieStatsProjectionImpl(
                        tuple.get("id", UUID.class),
                        tuple.get("title", String.class),
                        tuple.get("genre", String.class),
                        tuple.get("releaseYear", Integer.class),
                        tuple.get("averageRating", BigDecimal.class),
                        tuple.get("totalReviews", Long.class),
                        tuple.get("createdAt", Instant.class),
                        tuple.get("updatedAt", Instant.class)
                ))
                .toList();

        long totalElements = ((Number) entityManager
                .createNativeQuery(COUNT_QUERY)
                .setParameter("genre", genre)
                .getSingleResult())
                .longValue();

        return new PageImpl<>(
                projections,
                pageable,
                totalElements
        );
    }
}