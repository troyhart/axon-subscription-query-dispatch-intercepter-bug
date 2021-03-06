package com.example.demo;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

import org.axonframework.axonserver.connector.command.AxonServerRemoteCommandHandlingException;
import org.axonframework.axonserver.connector.query.AxonServerRemoteQueryHandlingException;
import org.axonframework.modelling.command.ConflictingAggregateVersionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * @author troyh
 *
 */
@ControllerAdvice
public class ExceptionControllerAdvice extends ResponseEntityExceptionHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionControllerAdvice.class);

  @ExceptionHandler(value = {AxonServerRemoteQueryHandlingException.class})
  protected ResponseEntity<Object> handleAxonServerRemoteHandlingException(AxonServerRemoteQueryHandlingException ex,
      WebRequest request) {
    String token = UUID.randomUUID().toString();
    log(ex, token);
    LOGGER.info("Server: {}", ex.getServer());
    LOGGER.info("Error Code: {}", ex.getErrorCode());
    return handleExceptionInternal(ex,
        toTokenizedMessage(token,
            ex.getExceptionDescriptions().stream().collect(Collectors.joining("\n * ", "\n * ", ""))),
        new HttpHeaders(),
        // ErrorCodes -> SEE: org.axonframework.axonserver.connector.ErrorCode
        ex.getErrorCode().equals("AXONIQ-5001") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST, request);
  }

  @ExceptionHandler(value = {AxonServerRemoteCommandHandlingException.class})
  protected ResponseEntity<Object> handleAxonServerRemoteHandlingException(AxonServerRemoteCommandHandlingException ex,
      WebRequest request) {
    String token = UUID.randomUUID().toString();
    log(ex, token);
    LOGGER.info("Origin Server: {}", ex.getOriginServer());
    LOGGER.info("Error Code: {}", ex.getErrorCode());
    return handleExceptionInternal(ex,
        toTokenizedMessage(token,
            ex.getExceptionDescriptions().stream().collect(Collectors.joining("\n * ", "\n * ", ""))),
        new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
  }

  @ExceptionHandler(value = {IllegalArgumentException.class, IllegalStateException.class,
      ConflictingAggregateVersionException.class, MethodArgumentTypeMismatchException.class})
  protected ResponseEntity<Object> handleBadRequest(Exception ex, WebRequest request) {
    String token = UUID.randomUUID().toString();
    log(ex, token);
    return handleExceptionInternal(ex, toTokenizedMessage(token, ex.getMessage()), new HttpHeaders(),
        HttpStatus.BAD_REQUEST, request);
  }

  @ExceptionHandler(value = {NoSuchElementException.class})
  protected ResponseEntity<Object> handleNoSuchElementException(NoSuchElementException ex, WebRequest request) {
    String token = UUID.randomUUID().toString();
    log(ex, token);
    return handleExceptionInternal(ex, toTokenizedMessage(token, ex.getMessage()), new HttpHeaders(),
        HttpStatus.NOT_FOUND, request);
  }

  void log(Throwable t, String token) {
    LOGGER.info(toTokenizedMessage(token, "Request failed with exception: "), t);
    Arrays.stream(t.getSuppressed()).forEach(suppressedException -> {
      LOGGER.debug("Suppressed Exception", suppressedException);
    });
  }

  String toTokenizedMessage(String token, String message) {
    return "[log-token: " + token + "] " + message;
  }
}
