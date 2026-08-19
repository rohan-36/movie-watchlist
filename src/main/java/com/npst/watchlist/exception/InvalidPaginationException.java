package com.npst.watchlist.exception;

public class InvalidPaginationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidPaginationException(String message) {
        super(message);
    }
}
