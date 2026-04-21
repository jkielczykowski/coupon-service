package pl.coupon.app.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.coupon.app.model.Coupon;

import java.util.Optional;
import java.util.UUID;

public interface CouponRepository extends JpaRepository<Coupon, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    select c
    from Coupon c
    where lower(c.code) = lower(:code)
""")
    Optional<Coupon> findByCodeForUpdate(@Param("code") String code);
}
