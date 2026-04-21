package pl.coupon.app.mapper;

import org.mapstruct.Mapper;
import pl.coupon.app.frontend.model.CouponResponseRest;
import pl.coupon.app.model.Coupon;

@Mapper(componentModel = "spring")
public interface CouponMapper {
    CouponResponseRest toResponse(Coupon coupon);
}
