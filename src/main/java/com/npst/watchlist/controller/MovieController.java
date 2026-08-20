package com.npst.watchlist.controller;

import com.npst.watchlist.dto.request.CreateMovieRequest;
import com.npst.watchlist.dto.request.UpdateMovieRequest;
import com.npst.watchlist.dto.response.MovieResponse;
import com.npst.watchlist.dto.response.PagedResponse;
import com.npst.watchlist.exception.InvalidPaginationException;
import com.npst.watchlist.service.MovieService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/movies")
@Tag(name = "Movies", description = "Movie management APIs")
public class MovieController {

    private static final int MAX_PAGE_SIZE = 100;

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @Operation(
            summary = "Create a movie",
            description = "Creates a new movie."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Movie created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "Movie already exists")
    })
    @PostMapping
    public ResponseEntity<MovieResponse> createMovie(
            @Valid @RequestBody CreateMovieRequest request
    ) {
        MovieResponse response = movieService.createMovie(request);
        return ResponseEntity
                .created(URI.create("/api/v1/movies/" + response.getId()))
                .body(response);
    }

    @Operation(
            summary = "Get a movie",
            description = "Returns a movie with its current average rating and review count."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movie found"),
            @ApiResponse(responseCode = "404", description = "Movie not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<MovieResponse> getMovieById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(movieService.getMovieById(id));
    }

    @Operation(
            summary = "List movies",
            description = "Returns paginated movies with optional genre filtering."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movies returned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    @GetMapping
    public ResponseEntity<PagedResponse<MovieResponse>> listMovies(
            @RequestParam(required = false) String genre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                movieService.listMovies(genre, pageable(page, size))
        );
    }

    @Operation(
            summary = "Update a movie",
            description = "Updates an existing movie."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movie updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Movie not found"),
            @ApiResponse(responseCode = "409", description = "Movie already exists")
    })
    @PutMapping("/{id}")
    public ResponseEntity<MovieResponse> updateMovie(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateMovieRequest request
    ) {
        return ResponseEntity.ok(movieService.updateMovie(id, request));
    }

    @Operation(
            summary = "Delete a movie",
            description = "Deletes a movie and its associated reviews."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Movie deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Movie not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable UUID id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }

    private Pageable pageable(int page, int size) {
        if (page < 0) {
            throw new InvalidPaginationException(
                    "Page must be greater than or equal to 0."
            );
        }

        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new InvalidPaginationException(
                    "Page size must be between 1 and 100."
            );
        }

        return PageRequest.of(page, size);
    }
}
