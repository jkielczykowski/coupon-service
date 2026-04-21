package pl.coupon.app.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import pl.coupon.app.frontend.api.CouponApi;
import pl.coupon.app.frontend.model.CouponResponseRest;
import pl.coupon.app.frontend.model.CreateCouponRequestRest;
import pl.coupon.app.service.CouponService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CouponController implements CouponApi {

    private final CouponService couponService;

    @Override
    public ResponseEntity<CouponResponseRest> createCoupon(CreateCouponRequestRest createCouponRequestRest) {
        CouponResponseRest response = couponService.createCoupon(createCouponRequestRest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<List<CouponResponseRest>> getCoupons() {
        return ResponseEntity.ok(couponService.getCoupons());
    }
}
