package com.npst.watchlist.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Year;

public class ReleaseYearValidator
        implements ConstraintValidator<ValidReleaseYear, Integer> {

    private static final int MIN_RELEASE_YEAR = 1888;

    @Override
    public boolean isValid(
            Integer releaseYear,
            ConstraintValidatorContext context) {

        if (releaseYear == null) {
            return true;
        }

        int currentYear = Year.now().getValue();

        return releaseYear >= MIN_RELEASE_YEAR
                && releaseYear <= currentYear;
    }
}
