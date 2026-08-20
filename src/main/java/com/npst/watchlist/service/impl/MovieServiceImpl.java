package com.npst.watchlist.service.impl;

import com.npst.watchlist.domain.entity.Movie;
import com.npst.watchlist.domain.projection.MovieStatsProjection;
import com.npst.watchlist.domain.repository.MovieRepository;
import com.npst.watchlist.dto.request.CreateMovieRequest;
import com.npst.watchlist.dto.request.UpdateMovieRequest;
import com.npst.watchlist.dto.response.MovieResponse;
import com.npst.watchlist.dto.response.PagedResponse;
import com.npst.watchlist.exception.DuplicateResourceException;
import com.npst.watchlist.exception.ResourceNotFoundException;
import com.npst.watchlist.service.MovieService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;

    public MovieServiceImpl(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @Override
    @Transactional
    public MovieResponse createMovie(CreateMovieRequest request) {
        if (movieRepository.existsByTitleIgnoreCaseAndReleaseYear(
                request.getTitle(),
                request.getReleaseYear()
        )) {
            throw new DuplicateResourceException(
                    "Movie with the same title and release year already exists."
            );
        }

        Movie movie = new Movie(
                request.getTitle(),
                request.getGenre(),
                request.getReleaseYear()
        );

        Movie savedMovie = movieRepository.save(movie);

        return getMovieStatsResponse(savedMovie.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public MovieResponse getMovieById(UUID id) {
        return getMovieStatsResponse(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<MovieResponse> listMovies(
            String genre,
            Pageable pageable
    ) {
        Page<MovieStatsProjection> page = movieRepository.findMovieStats(
                genre,
                pageable
        );

        return new PagedResponse<>(
                page.getContent().stream()
                        .map(this::toMovieResponse)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    @Override
    @Transactional
    public MovieResponse updateMovie(UUID id, UpdateMovieRequest request) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found."));

        if (movieRepository.existsByTitleIgnoreCaseAndReleaseYearAndIdNot(
                request.getTitle(),
                request.getReleaseYear(),
                id
        )) {
            throw new DuplicateResourceException(
                    "Movie with the same title and release year already exists."
            );
        }

        movie.setTitle(request.getTitle());
        movie.setGenre(request.getGenre());
        movie.setReleaseYear(request.getReleaseYear());

        movieRepository.save(movie);

        return getMovieStatsResponse(id);
    }

    @Override
    @Transactional
    public void deleteMovie(UUID id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found."));

        movieRepository.delete(movie);
    }

    private MovieResponse getMovieStatsResponse(UUID id) {
        MovieStatsProjection projection = movieRepository.findMovieStatsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found."));

        return toMovieResponse(projection);
    }

    private MovieResponse toMovieResponse(MovieStatsProjection projection) {
        return new MovieResponse(
                projection.getId(),
                projection.getTitle(),
                projection.getGenre(),
                projection.getReleaseYear(),
                projection.getAverageRating(),
                projection.getTotalReviews(),
                projection.getCreatedAt(),
                projection.getUpdatedAt()
        );
    }
}
