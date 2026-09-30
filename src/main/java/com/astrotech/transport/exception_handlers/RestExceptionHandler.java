package com.astrotech.transport.exception_handlers;

import com.astrotech.transport.exceptions.*;
import com.astrotech.transport.responseBuilder.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.*;

@RestControllerAdvice(annotations = RestController.class)
@Slf4j
public class RestExceptionHandler {
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleConflictException(
            ConflictException ex
    ) {
        ApiErrorResponse response = ApiErrorResponseMapper.createError(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex
    ) {
        ApiErrorResponse response = ApiErrorResponseMapper.createError(
                HttpStatus.FORBIDDEN.value(),
                "Access Denied",
                ex.getMessage(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(response);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUsernameNotFound(
            UsernameNotFoundException ex
    ) {
        ApiErrorResponse response = ApiErrorResponseMapper.createError(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiErrorResponse> handleOptimisticLockingFailure(
            ObjectOptimisticLockingFailureException ex, HttpServletRequest request) {


        ApiErrorResponse response = ApiErrorResponseMapper.createError(
                HttpStatus.CONFLICT.value(), "Conflict",
                "The data you are trying to update was modified by another transaction. Please refresh and try again.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        var response = ApiErrorResponseMapper.createError(HttpStatus.FORBIDDEN.value(), "Forbidden",
                ex.getMessage(),
                null);
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(response);
    }

    @ExceptionHandler(UnAuthenticatedUserException.class)
    public ResponseEntity<ApiErrorResponse> handleUnAuthenticatedUser(UnAuthenticatedUserException ex) {

        var response = ApiErrorResponseMapper.createError(HttpStatus.UNAUTHORIZED.value(), "Unauthenticated", ex.getMessage(), null);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(UnAuthorizedUserException.class)
    public ResponseEntity<ApiErrorResponse> handleUnAuthorizedUser(UnAuthorizedUserException ex) {

        var response = ApiErrorResponseMapper.createError(
                HttpStatus.FORBIDDEN.value(), "Forbidden",
                ex.getMessage(),
                null);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }


    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        var response = ApiErrorResponseMapper.createError(
                HttpStatus.NOT_FOUND.value(), "NOT_FOUND",
                ex.getMessage(),
                null);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        var response = ApiErrorResponseMapper.createError(
                HttpStatus.UNAUTHORIZED.value(),
                "UNAUTHORIZED",
                "The email or password provided is incorrect.",
                null);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler({
            PessimisticLockException.class,
            CannotAcquireLockException.class
    })
    public ResponseEntity<ApiErrorResponse> handleLockExceptions(Exception ex) {
        var response = ApiErrorResponseMapper.createError(
                HttpStatus.CONFLICT.value(),
                "CONFLICT",
                "Ticket is currently being purchased. Please retry in few minutes, if not successful.",
                null);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationExceptions(ValidationException ex) {
        var response = ApiErrorResponseMapper.createError(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "VALIDATION ERROR",
                ex.getMessage(),
                null);

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }


    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(BadRequestException ex) {
        var response = ApiErrorResponseMapper.createError(
                HttpStatus.BAD_REQUEST.value(),
                "BAD_REQUEST",
                ex.getMessage(),
                null);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiErrorResponse> handleAppException(AppException ex) {
        var response = ApiErrorResponseMapper.createError(
                HttpStatus.BAD_REQUEST.value(),
                "BAD_REQUEST",
                ex.getMessage(),
                null);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicate(DuplicateResourceException ex) {
        var response = ApiErrorResponseMapper.createError(
                HttpStatus.CONFLICT.value(),
                "CONFLICT",
                ex.getMessage(),
                null);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException exception) {
        var errors = new HashMap<String, String>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }


    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleRateLimit(RateLimitExceededException ex) {
        var response = ApiErrorResponseMapper.createError(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                "TOO_MANY_REQUESTS",
                ex.getMessage(),
                null);

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ApiErrorResponse> handlePayment(PaymentException ex) {
        Throwable root = ex.getCause();
        if (root != null) {
            log.error("Underlying payment provider error", root);
        }

        var response = ApiErrorResponseMapper.createError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                "Payment processing failed",
                null);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(Exception ex) {
        log.error("Unhandled exception", ex);

        var response = ApiErrorResponseMapper.createError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                "An error occurred, please try again later",
                null);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ApiErrorResponse> handleWebClientException(WebClientResponseException ex) {
        String responseBody = ex.getResponseBodyAsString();
        log.error("Upstream Service Error: Status {} - Body: {}", ex.getStatusCode(), responseBody);

        var response = ApiErrorResponseMapper.createError(
                ex.getStatusCode().value(),
                "PAYMENT_GATEWAY_ERROR",
                "An error occurred with an upstream service.",
                responseBody);

        return ResponseEntity.status(ex.getStatusCode()).body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        int statusCode = HttpStatus.BAD_REQUEST.value();
        String errorCode = "DATA_CONFLICT";
        String message = "Invalid data operation.";

        if (ex.getCause() instanceof org.hibernate.exception.ConstraintViolationException cve) {
            if ("uk_trip_seat".equals(cve.getConstraintName())) {
                statusCode = HttpStatus.CONFLICT.value();
                errorCode = "SEAT_ALREADY_RESERVED";
                message = "This seat has already been selected by another user.";
            }
        }

        var response = ApiErrorResponseMapper.createError(statusCode, errorCode, message, null);
        return ResponseEntity.status(statusCode).body(response);
    }

    @ExceptionHandler(TicketGenerationPendingException.class)
    public ResponseEntity<ApiErrorResponse> handlePendingTicket(TicketGenerationPendingException ex) {
        var response = ApiErrorResponseMapper.createError(
                HttpStatus.ACCEPTED.value(),
                "GENERATING",
                "Ticket compilation started for ID: " + ex.getTicketId(),
                null);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidRequestBody(HttpMessageNotReadableException ex) {
        log.error("HTTP message not readable", ex);

        var response = ApiErrorResponseMapper.createError(
                HttpStatus.BAD_REQUEST.value(),
                "BAD_REQUEST",
                "Invalid request body",
                null);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }


}
