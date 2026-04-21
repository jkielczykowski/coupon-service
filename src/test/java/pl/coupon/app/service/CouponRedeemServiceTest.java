package pl.coupon.app.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import pl.coupon.app.frontend.model.RedeemCouponRequestRest;
import pl.coupon.app.handler.exception.CouponAlreadyRedeemedByUserException;
import pl.coupon.app.handler.exception.CouponNotFoundException;
import pl.coupon.app.model.Coupon;
import pl.coupon.app.model.CouponRedemption;
import pl.coupon.app.repository.CouponRedeemRepository;
import pl.coupon.app.repository.CouponRepository;
import pl.coupon.app.validator.CouponRedeemValidator;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponRedeemServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponRedeemRepository couponRedemptionRepository;

    @Mock
    private CouponRedeemValidator couponRedeemValidator;

    @Mock
    private GeoIpService geoIpService;

    @InjectMocks
    private CouponRedeemService couponRedeemService;

    @Test
    void shouldRedeemCouponSuccessfully() {
        // given
        UUID userId = UUID.randomUUID();
        String clientIp = "8.8.8.8";

        RedeemCouponRequestRest request = new RedeemCouponRequestRest()
                .code("WIOSNA2026")
                .userId(userId);

        Coupon coupon = Coupon.builder()
                .id(UUID.randomUUID())
                .code("WIOSNA2026")
                .countryCode("PL")
                .currentUsageCount(0)
                .maxUsageCount(10)
                .build();

        when(couponRepository.findByCodeForUpdate("WIOSNA2026"))
                .thenReturn(Optional.of(coupon));
        when(geoIpService.resolveCountry(clientIp))
                .thenReturn("PL");

        // when
        var response = couponRedeemService.redeemCoupon(request, clientIp);

        // then
        assertThat(response.getCode()).isEqualTo("WIOSNA2026");
        assertThat(response.getUserId()).isEqualTo(userId.toString());
        assertThat(response.getRemainingUsages()).isEqualTo(9);
        assertThat(response.getRedeemedAt()).isNotNull();

        assertThat(coupon.getCurrentUsageCount()).isEqualTo(1);

        verify(couponRepository).findByCodeForUpdate("WIOSNA2026");
        verify(geoIpService).resolveCountry(clientIp);
        verify(couponRedeemValidator).validateCouponCanBeRedeemed(coupon, "PL");

        ArgumentCaptor<CouponRedemption> redemptionCaptor =
                ArgumentCaptor.forClass(CouponRedemption.class);

        verify(couponRedemptionRepository).saveAndFlush(redemptionCaptor.capture());

        CouponRedemption savedRedemption = redemptionCaptor.getValue();

        assertThat(savedRedemption.getCoupon()).isEqualTo(coupon);
        assertThat(savedRedemption.getUserId()).isEqualTo(userId);
        assertThat(savedRedemption.getRequesterIp()).isEqualTo(clientIp);
        assertThat(savedRedemption.getCountryCode()).isEqualTo("PL");
        assertThat(savedRedemption.getRedeemedAt()).isNotNull();
    }

    @Test
    void shouldThrowCouponNotFoundExceptionWhenCouponDoesNotExist() {
        // given
        RedeemCouponRequestRest request = new RedeemCouponRequestRest()
                .code("UNKNOWN")
                .userId(UUID.randomUUID());

        when(couponRepository.findByCodeForUpdate("UNKNOWN"))
                .thenReturn(Optional.empty());

        // when + then
        assertThatThrownBy(() -> couponRedeemService.redeemCoupon(request, "8.8.8.8"))
                .isInstanceOf(CouponNotFoundException.class);

        verify(couponRepository).findByCodeForUpdate("UNKNOWN");
        verifyNoInteractions(geoIpService);
        verifyNoInteractions(couponRedeemValidator);
        verifyNoInteractions(couponRedemptionRepository);
    }

    @Test
    void shouldThrowCouponAlreadyRedeemedByUserExceptionWhenUniqueConstraintIsViolated() {
        // given
        UUID userId = UUID.randomUUID();
        String clientIp = "8.8.8.8";

        RedeemCouponRequestRest request = new RedeemCouponRequestRest()
                .code("WIOSNA2026")
                .userId(userId);

        Coupon coupon = Coupon.builder()
                .id(UUID.randomUUID())
                .code("WIOSNA2026")
                .countryCode("PL")
                .currentUsageCount(0)
                .maxUsageCount(10)
                .build();

        when(couponRepository.findByCodeForUpdate("WIOSNA2026"))
                .thenReturn(Optional.of(coupon));
        when(geoIpService.resolveCountry(clientIp))
                .thenReturn("PL");
        when(couponRedemptionRepository.saveAndFlush(any(CouponRedemption.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate redemption"));

        // when + then
        assertThatThrownBy(() -> couponRedeemService.redeemCoupon(request, clientIp))
                .isInstanceOf(CouponAlreadyRedeemedByUserException.class);

        assertThat(coupon.getCurrentUsageCount()).isEqualTo(0);

        verify(couponRedeemValidator).validateCouponCanBeRedeemed(coupon, "PL");
        verify(couponRedemptionRepository).saveAndFlush(any(CouponRedemption.class));
    }

    @Test
    void shouldPassResolvedCountryToValidator() {
        // given
        UUID userId = UUID.randomUUID();
        String clientIp = "1.1.1.1";

        RedeemCouponRequestRest request = new RedeemCouponRequestRest()
                .code("SUMMER2026")
                .userId(userId);

        Coupon coupon = Coupon.builder()
                .id(UUID.randomUUID())
                .code("SUMMER2026")
                .countryCode("DE")
                .currentUsageCount(2)
                .maxUsageCount(5)
                .build();

        when(couponRepository.findByCodeForUpdate("SUMMER2026"))
                .thenReturn(Optional.of(coupon));
        when(geoIpService.resolveCountry(clientIp))
                .thenReturn("DE");

        // when
        couponRedeemService.redeemCoupon(request, clientIp);

        // then
        verify(couponRedeemValidator).validateCouponCanBeRedeemed(coupon, "DE");
    }
}