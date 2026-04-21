package pl.coupon.app.handler;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.coupon.app.frontend.model.ErrorResponseRest;
import pl.coupon.app.handler.exception.CouponAlreadyExistsException;
import pl.coupon.app.handler.exception.CouponAlreadyRedeemedByUserException;
import pl.coupon.app.handler.exception.CouponCountryNotAllowedException;
import pl.coupon.app.handler.exception.CouponNotFoundException;
import pl.coupon.app.handler.exception.CouponUsageLimitExceededException;
import pl.coupon.app.handler.exception.GeoIpResolutionException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CouponNotFoundException.class)
    public ResponseEntity<ErrorResponseRest> handleCouponNotFound(CouponNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(error("COUPON_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(CouponAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseRest> handleCouponAlreadyExists(CouponAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(error("COUPON_ALREADY_EXISTS", ex.getMessage()));
    }

    @ExceptionHandler(CouponUsageLimitExceededException.class)
    public ResponseEntity<ErrorResponseRest> handleCouponUsageLimitExceeded(CouponUsageLimitExceededException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(error("COUPON_USAGE_LIMIT_EXCEEDED", ex.getMessage()));
    }

    @ExceptionHandler(CouponCountryNotAllowedException.class)
    public ResponseEntity<ErrorResponseRest> handleCouponCountryNotAllowed(CouponCountryNotAllowedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(error("COUPON_COUNTRY_NOT_ALLOWED", ex.getMessage()));
    }

    @ExceptionHandler(CouponAlreadyRedeemedByUserException.class)
    public ResponseEntity<ErrorResponseRest> handleCouponAlreadyRedeemedByUser(CouponAlreadyRedeemedByUserException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(error("COUPON_ALREADY_REDEEMED_BY_USER", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseRest> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getField() + " " + fieldError.getDefaultMessage())
                .orElse("Request validation failed.");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(error("VALIDATION_ERROR", message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseRest> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(error("INVALID_REQUEST", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseRest> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error("INTERNAL_SERVER_ERROR", ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseRest> handleDataIntegrityViolation() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(error("DATA_INTEGRITY_VIOLATION", "Request violates database constraints."));
    }

    @ExceptionHandler(GeoIpResolutionException.class)
    public ResponseEntity<Map<String, Object>> handleGeoIpException(GeoIpResolutionException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "code", "GEO_IP_RESOLUTION_FAILED",
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponseRest> handleOptimisticLock(ObjectOptimisticLockingFailureException ex) {
        ErrorResponseRest response = error(
                "COUPON_CONCURRENT_MODIFICATION",
                "Coupon was modified concurrently. Please retry."
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    private ErrorResponseRest error(String code, String message) {
        return new ErrorResponseRest()
                .code(code)
                .message(message);
    }
}