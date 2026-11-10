package com.kyo.url_shortener.web;

import com.kyo.url_shortener.analytics.InvalidAnalyticsQueryException;
import com.kyo.url_shortener.url.AliasAlreadyTakenException;
import com.kyo.url_shortener.url.InvalidExpiryException;
import com.kyo.url_shortener.url.ShortUrlExpiredException;
import com.kyo.url_shortener.url.ShortUrlNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j  // Lombok: gives you a 'log' field
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // Example, fully written
    @ExceptionHandler(AliasAlreadyTakenException.class)
    ProblemDetail handleAliasTaken(AliasAlreadyTakenException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Alias already taken");
        return problem;
    }

    @ExceptionHandler(InvalidExpiryException.class)
    ProblemDetail handleInvalidExpiry(InvalidExpiryException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Invalid expiry date, it must be greater than NOW");

        return problem;
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error, Something went wrong");
        problem.setTitle("Internal server Error");

        log.error("Unexpected Error", ex);

        return problem;
    }
    @ExceptionHandler(ShortUrlNotFoundException.class)
    ProblemDetail handleNotFound(ShortUrlNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Short URL not found");

        return problem;
    }

    @ExceptionHandler(ShortUrlExpiredException.class)
    ProblemDetail handleExpired(ShortUrlExpiredException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.GONE, ex.getMessage());
        problem.setTitle("Short URL expired");

        return problem;
    }
    @ExceptionHandler(InvalidAnalyticsQueryException.class)
    ProblemDetail handleInvalidAnalyticsQuery(InvalidAnalyticsQueryException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Invalid analytics query");

        return problem;
    }
}