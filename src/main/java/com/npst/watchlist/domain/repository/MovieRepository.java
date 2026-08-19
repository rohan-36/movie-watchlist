package com.npst.watchlist.domain.repository;

import com.npst.watchlist.domain.entity.Movie;
import com.npst.watchlist.domain.projection.MovieStatsProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface MovieRepository extends JpaRepository<Movie, UUID> {

    boolean existsByTitleIgnoreCaseAndReleaseYear(
            String title,
            Integer releaseYear
    );

    boolean existsByTitleIgnoreCaseAndReleaseYearAndIdNot(
            String title,
            Integer releaseYear,
            UUID id
    );

    @Query(value = """
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
            WHERE m.id = :id
            GROUP BY
                m.id,
                m.title,
                m.genre,
                m.release_year,
                m.created_at,
                m.updated_at
            """, nativeQuery = true)
    Optional<MovieStatsProjection> findMovieStatsById(
            @Param("id") UUID id
    );

    @Query(
            value = """
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
                    WHERE (:genre IS NULL OR LOWER(m.genre) = LOWER(:genre))
                    GROUP BY
                        m.id,
                        m.title,
                        m.genre,
                        m.release_year,
                        m.created_at,
                        m.updated_at
                    ORDER BY m.created_at DESC, m.id ASC
                    """,
            countQuery = """
                    SELECT COUNT(m.id)
                    FROM movies m
                    WHERE (:genre IS NULL OR LOWER(m.genre) = LOWER(:genre))
                    """,
            nativeQuery = true
    )
    Page<MovieStatsProjection> findMovieStats(
            @Param("genre") String genre,
            Pageable pageable
    );
}