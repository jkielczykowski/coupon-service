package pl.coupon.app.handler.exception;

public class GeoIpResolutionException extends RuntimeException {

    public GeoIpResolutionException(String message) {
        super(message);
    }

    public GeoIpResolutionException(String message, Throwable cause) {
        super(message, cause);
    }
}