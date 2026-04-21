package pl.coupon.app.handler.exception;

import java.util.UUID;

public class CouponAlreadyRedeemedByUserException extends RuntimeException {

    public CouponAlreadyRedeemedByUserException(String code, UUID userId) {
        super("User '%s' has already redeemed coupon '%s'.".formatted(userId, code));
    }
}
