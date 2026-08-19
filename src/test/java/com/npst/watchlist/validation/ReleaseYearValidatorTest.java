package com.npst.watchlist.validation;

import org.junit.jupiter.api.Test;

import java.time.Year;

import static org.junit.jupiter.api.Assertions.*;

class ReleaseYearValidatorTest {

    private final ReleaseYearValidator validator =
            new ReleaseYearValidator();

    @Test
    void shouldRejectYearBefore1888() {
        assertFalse(validator.isValid(1887, null));
    }

    @Test
    void shouldAccept1888() {
        assertTrue(validator.isValid(1888, null));
    }

    @Test
    void shouldAcceptCurrentYear() {
        int currentYear = Year.now().getValue();

        assertTrue(validator.isValid(currentYear, null));
    }

    @Test
    void shouldRejectFutureYear() {
        int currentYear = Year.now().getValue();

        assertFalse(validator.isValid(currentYear + 1, null));
    }

    @Test
    void shouldAllowNullForNotNullToHandle() {
        assertTrue(validator.isValid(null, null));
    }
}
