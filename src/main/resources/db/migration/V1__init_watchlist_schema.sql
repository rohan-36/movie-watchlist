CREATE TABLE movies (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    genre VARCHAR(100) NOT NULL,
    release_year INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_movies_release_year
        CHECK (release_year >= 1888)
);

CREATE TABLE reviews (
    id UUID PRIMARY KEY,
    movie_id UUID NOT NULL,
    reviewer_name VARCHAR(255) NOT NULL,
    rating INTEGER NOT NULL,
    comment VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_reviews_rating
        CHECK (rating >= 1 AND rating <= 5),

    CONSTRAINT fk_reviews_movie
        FOREIGN KEY (movie_id)
        REFERENCES movies(id)
        ON DELETE CASCADE
);

CREATE UNIQUE INDEX uk_movie_title_year_ci
    ON movies (LOWER(title), release_year);

CREATE INDEX idx_reviews_movie_id
    ON reviews(movie_id);

CREATE INDEX idx_movies_genre
    ON movies(genre);

CREATE INDEX idx_movies_release_year
    ON movies(release_year);
