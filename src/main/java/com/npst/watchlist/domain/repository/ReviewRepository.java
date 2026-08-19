package com.npst.watchlist.domain.repository;

import com.npst.watchlist.domain.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    @Query(
            value = """
                    SELECT r
                    FROM Review r
                    WHERE r.movie.id = :movieId
                    ORDER BY r.createdAt DESC, r.id ASC
                    """,
            countQuery = """
                    SELECT COUNT(r)
                    FROM Review r
                    WHERE r.movie.id = :movieId
                    """
    )
    Page<Review> findByMovieId(
            @Param("movieId") UUID movieId,
            Pageable pageable
    );
}
