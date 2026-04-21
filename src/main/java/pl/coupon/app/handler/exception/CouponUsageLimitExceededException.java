package pl.coupon.app.handler.exception;

public class CouponUsageLimitExceededException extends RuntimeException {

    public CouponUsageLimitExceededException(String code) {
        super("Coupon with code '%s' has reached its usage limit.".formatted(code));
    }
}
