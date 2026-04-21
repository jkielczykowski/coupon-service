package pl.coupon.app.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.coupon.app.model.Coupon;
import pl.coupon.app.handler.exception.CouponCountryNotAllowedException;
import pl.coupon.app.handler.exception.CouponUsageLimitExceededException;

@Component
@RequiredArgsConstructor
public class CouponRedeemValidator {


    public void validateCouponCanBeRedeemed(Coupon coupon, String resolvedCountry) {
        validateCountry(coupon, resolvedCountry);
        validateUsageLimit(coupon);
    }

    private void validateCountry(Coupon coupon, String resolvedCountry) {
        if (!coupon.getCountryCode().equalsIgnoreCase(resolvedCountry)) {
            throw new CouponCountryNotAllowedException(
                    coupon.getCode(),
                    coupon.getCountryCode(),
                    resolvedCountry
            );
        }
    }

    private void validateUsageLimit(Coupon coupon) {
        if (coupon.getCurrentUsageCount() >= coupon.getMaxUsageCount()) {
            throw new CouponUsageLimitExceededException(coupon.getCode());
        }
    }

}
