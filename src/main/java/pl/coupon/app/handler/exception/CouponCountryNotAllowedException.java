package pl.coupon.app.handler.exception;

public class CouponCountryNotAllowedException extends RuntimeException {

    public CouponCountryNotAllowedException(String code, String expectedCountry, String actualCountry) {
        super("Coupon with code '%s' is restricted to country '%s', but request came from '%s'."
                .formatted(code, expectedCountry, actualCountry));
    }
}