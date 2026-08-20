package com.npst.watchlist.service;

import com.npst.watchlist.dto.request.CreateMovieRequest;
import com.npst.watchlist.dto.request.UpdateMovieRequest;
import com.npst.watchlist.dto.response.MovieResponse;
import com.npst.watchlist.dto.response.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MovieService {

    MovieResponse createMovie(CreateMovieRequest request);

    MovieResponse getMovieById(UUID id);

    PagedResponse<MovieResponse> listMovies(String genre, Pageable pageable);

    MovieResponse updateMovie(UUID id, UpdateMovieRequest request);

    void deleteMovie(UUID id);
}
