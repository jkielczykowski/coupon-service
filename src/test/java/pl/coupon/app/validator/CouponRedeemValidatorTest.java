package pl.coupon.app.validator;

import org.junit.jupiter.api.Test;
import pl.coupon.app.handler.exception.CouponCountryNotAllowedException;
import pl.coupon.app.handler.exception.CouponUsageLimitExceededException;
import pl.coupon.app.model.Coupon;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CouponRedeemValidatorTest {

    private final CouponRedeemValidator validator = new CouponRedeemValidator();

    @Test
    void shouldPassValidationWhenCouponCanBeRedeemed() {
        // given
        Coupon coupon = Coupon.builder()
                .code("WIOSNA2026")
                .countryCode("PL")
                .currentUsageCount(0)
                .maxUsageCount(10)
                .build();

        // when + then
        assertThatCode(() -> validator.validateCouponCanBeRedeemed(coupon, "PL"))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldPassValidationWhenCountryCodeHasDifferentCase() {
        // given
        Coupon coupon = Coupon.builder()
                .code("WIOSNA2026")
                .countryCode("pl")
                .currentUsageCount(0)
                .maxUsageCount(10)
                .build();

        // when + then
        assertThatCode(() -> validator.validateCouponCanBeRedeemed(coupon, "PL"))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldThrowExceptionWhenCountryIsNotAllowed() {
        // given
        Coupon coupon = Coupon.builder()
                .code("WIOSNA2026")
                .countryCode("PL")
                .currentUsageCount(0)
                .maxUsageCount(10)
                .build();

        // when + then
        assertThatThrownBy(() -> validator.validateCouponCanBeRedeemed(coupon, "DE"))
                .isInstanceOf(CouponCountryNotAllowedException.class);
    }

    @Test
    void shouldThrowExceptionWhenCouponUsageLimitIsReached() {
        // given
        Coupon coupon = Coupon.builder()
                .code("WIOSNA2026")
                .countryCode("PL")
                .currentUsageCount(10)
                .maxUsageCount(10)
                .build();

        // when + then
        assertThatThrownBy(() -> validator.validateCouponCanBeRedeemed(coupon, "PL"))
                .isInstanceOf(CouponUsageLimitExceededException.class);
    }

    @Test
    void shouldThrowExceptionWhenCouponUsageLimitIsExceeded() {
        // given
        Coupon coupon = Coupon.builder()
                .code("WIOSNA2026")
                .countryCode("PL")
                .currentUsageCount(11)
                .maxUsageCount(10)
                .build();

        // when + then
        assertThatThrownBy(() -> validator.validateCouponCanBeRedeemed(coupon, "PL"))
                .isInstanceOf(CouponUsageLimitExceededException.class);
    }
}
