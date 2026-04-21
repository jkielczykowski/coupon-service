package pl.coupon.app.controller;


import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import pl.coupon.app.frontend.api.CouponRedeemApi;
import pl.coupon.app.frontend.model.RedeemCouponRequestRest;
import pl.coupon.app.frontend.model.RedeemCouponResponseRest;
import pl.coupon.app.service.CouponRedeemService;


@RestController
@RequiredArgsConstructor
public class CouponRedeemController implements CouponRedeemApi {

    private final CouponRedeemService couponRedeemService;
    private final HttpServletRequest httpServletRequest;

    @Override
    public ResponseEntity<RedeemCouponResponseRest> redeemCoupon(RedeemCouponRequestRest redeemCouponRequestRest) {
        String clientIp = extractClientIp(httpServletRequest);
        RedeemCouponResponseRest response = couponRedeemService.redeemCoupon(redeemCouponRequestRest, clientIp);

        return ResponseEntity.ok(response);
    }

    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}