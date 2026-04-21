package pl.coupon.app.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.coupon.app.frontend.model.RedeemCouponRequestRest;
import pl.coupon.app.frontend.model.RedeemCouponResponseRest;
import pl.coupon.app.model.Coupon;
import pl.coupon.app.model.CouponRedemption;
import pl.coupon.app.repository.CouponRedeemRepository;
import pl.coupon.app.repository.CouponRepository;
import pl.coupon.app.handler.exception.CouponAlreadyRedeemedByUserException;
import pl.coupon.app.handler.exception.CouponNotFoundException;
import pl.coupon.app.validator.CouponRedeemValidator;

import java.time.OffsetDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class CouponRedeemService {

    private final CouponRepository couponRepository;
    private final CouponRedeemRepository couponRedemptionRepository;
    private final CouponRedeemValidator couponRedeemValidator;
    private final GeoIpService geoIpService;

    @Transactional
    public RedeemCouponResponseRest redeemCoupon(RedeemCouponRequestRest request, String clientIp) {
        Coupon coupon = couponRepository.findByCodeForUpdate(request.getCode())
                .orElseThrow(() -> new CouponNotFoundException(request.getCode()));

        String resolvedCountry = geoIpService.resolveCountry(clientIp);

        couponRedeemValidator.validateCouponCanBeRedeemed(
                coupon,
                resolvedCountry
        );

        CouponRedemption redemption = CouponRedemption.builder()
                .coupon(coupon)
                .userId(request.getUserId())
                .redeemedAt(OffsetDateTime.now())
                .requesterIp(clientIp)
                .countryCode(resolvedCountry)
                .build();

        try {
            couponRedemptionRepository.saveAndFlush(redemption);
        } catch (DataIntegrityViolationException ex) {
            throw new CouponAlreadyRedeemedByUserException(coupon.getCode(), request.getUserId());
        }

        coupon.redeem();

        return new RedeemCouponResponseRest()
                .code(coupon.getCode())
                .userId(String.valueOf(request.getUserId()))
                .redeemedAt(redemption.getRedeemedAt())
                .remainingUsages(coupon.getMaxUsageCount() - coupon.getCurrentUsageCount());
    }

}
