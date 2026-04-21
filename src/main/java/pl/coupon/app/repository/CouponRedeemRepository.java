package pl.coupon.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.coupon.app.model.CouponRedemption;

import java.util.UUID;

public interface CouponRedeemRepository extends JpaRepository<CouponRedemption, UUID> {
}
