package com.npst.watchlist.domain.repository;

import com.npst.watchlist.domain.projection.MovieStatsProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MovieStatsRepositoryCustom {

    Page<MovieStatsProjection> findMovieStats(
            String genre,
            Pageable pageable
    );
}
