package pl.coupon.app.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.coupon.app.mapper.CouponMapper;
import pl.coupon.app.frontend.model.CouponResponseRest;
import pl.coupon.app.frontend.model.CreateCouponRequestRest;
import pl.coupon.app.model.Coupon;
import pl.coupon.app.repository.CouponRepository;

import java.util.List;
import java.util.Locale;

@Service
@Transactional
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponMapper couponMapper;

    public CouponResponseRest createCoupon(CreateCouponRequestRest request) {
        String normalizedCode = request.getCode().trim().toUpperCase(Locale.ROOT);

        Coupon coupon = Coupon.builder()
                .code(normalizedCode)
                .currentUsageCount(0)
                .maxUsageCount(request.getMaxUsages())
                .countryCode(request.getCountry())
                .build();

        Coupon savedCoupon = couponRepository.saveAndFlush(coupon);
        return couponMapper.toResponse(savedCoupon);
    }

    public List<CouponResponseRest> getCoupons() {
        return couponRepository.findAll()
                .stream()
                .map(couponMapper::toResponse)
                .toList();
    }

}
