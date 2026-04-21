package pl.coupon.app;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import pl.coupon.app.frontend.model.RedeemCouponRequestRest;
import pl.coupon.app.model.Coupon;
import pl.coupon.app.model.CouponRedemption;
import pl.coupon.app.repository.CouponRedeemRepository;
import pl.coupon.app.repository.CouponRepository;
import pl.coupon.app.service.GeoIpService;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CouponRedeemControllerIT extends AbstractIT {

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponRedeemRepository couponRedeemRepository;

    @MockBean
    private GeoIpService geoIpService;

    @Test
    void shouldRedeemCoupon() throws Exception {
        // given
        Coupon coupon = couponRepository.save(Coupon.builder()
                .code("WIOSNA2026")
                .maxUsageCount(5)
                .currentUsageCount(0)
                .countryCode("PL")
                .build());

        given(geoIpService.resolveCountry("10.20.30.40")).willReturn("PL");

        RedeemCouponRequestRest request = new RedeemCouponRequestRest()
                .code("WIOSNA2026")
                .userId(UUID.randomUUID());

        // when
        var result = mockMvc.perform(post("/coupons/redeem")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("X-Forwarded-For", "10.20.30.40, 172.16.0.1"));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("WIOSNA2026"))
                .andExpect(jsonPath("$.userId").value(request.getUserId().toString()))
                .andExpect(jsonPath("$.redeemedAt").exists())
                .andExpect(jsonPath("$.remainingUsages").value(4));

        Coupon updatedCoupon = couponRepository.findById(coupon.getId()).orElseThrow();
        assertThat(updatedCoupon.getCurrentUsageCount()).isEqualTo(1);

        assertThat(couponRedeemRepository.findAll()).hasSize(1);
        CouponRedemption redemption = couponRedeemRepository.findAll().get(0);
        assertThat(redemption.getRequesterIp()).isEqualTo("10.20.30.40");
        assertThat(redemption.getCountryCode()).isEqualTo("PL");
        assertThat(redemption.getUserId()).isEqualTo(request.getUserId());
    }

    @Test
    void shouldRedeemCouponUsingRemoteAddrWhenXForwardedForHeaderIsMissing() throws Exception {
        // given
        Coupon coupon = couponRepository.save(Coupon.builder()
                .code("LATO2026")
                .maxUsageCount(3)
                .currentUsageCount(0)
                .countryCode("PL")
                .build());

        given(geoIpService.resolveCountry("192.168.1.77")).willReturn("PL");

        RedeemCouponRequestRest request = new RedeemCouponRequestRest()
                .code(coupon.getCode())
                .userId(UUID.randomUUID());

        // when
        var result = mockMvc.perform(post("/coupons/redeem")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(req -> {
                    req.setRemoteAddr("192.168.1.77");
                    return req;
                }));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(coupon.getCode()))
                .andExpect(jsonPath("$.userId").value(request.getUserId().toString()))
                .andExpect(jsonPath("$.remainingUsages").value(2));

        assertThat(couponRedeemRepository.findAll()).hasSize(1);
        CouponRedemption redemption = couponRedeemRepository.findAll().get(0);
        assertThat(redemption.getRequesterIp()).isEqualTo("192.168.1.77");
    }

    @Test
    void shouldReturnNotFoundWhenCouponDoesNotExist() throws Exception {
        // given
        given(geoIpService.resolveCountry("194.204.159.1")).willReturn("PL");

        RedeemCouponRequestRest request = new RedeemCouponRequestRest()
                .code("NIEISTNIEJE")
                .userId(UUID.randomUUID());

        // when
        var result = mockMvc.perform(post("/coupons/redeem")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isNotFound());

        assertThat(couponRedeemRepository.findAll()).isEmpty();
    }

    @Test
    void shouldReturnBadRequestWhenCouponCountryIsNotAllowed() throws Exception {
        // given
        Coupon coupon = couponRepository.save(Coupon.builder()
                .code("JESIEN2026")
                .maxUsageCount(10)
                .currentUsageCount(0)
                .countryCode("DE")
                .build());

        given(geoIpService.resolveCountry("192.168.1.77")).willReturn("PL");

        RedeemCouponRequestRest request = new RedeemCouponRequestRest()
                .code(coupon.getCode())
                .userId(UUID.randomUUID());

        // when
        var result = mockMvc.perform(post("/coupons/redeem")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isForbidden());

        Coupon savedCoupon = couponRepository.findAll().get(0);
        assertThat(savedCoupon.getCurrentUsageCount()).isEqualTo(0);
        assertThat(couponRedeemRepository.findAll()).isEmpty();
    }

    @Test
    void shouldReturnBadRequestWhenCouponUsageLimitExceeded() throws Exception {
        // given
        Coupon coupon = couponRepository.save(Coupon.builder()
                .code("LIMIT2026")
                .maxUsageCount(1)
                .currentUsageCount(1)
                .countryCode("PL")
                .build());

        given(geoIpService.resolveCountry("194.204.159.1")).willReturn("PL");

        RedeemCouponRequestRest request = new RedeemCouponRequestRest()
                .code(coupon.getCode())
                .userId(UUID.randomUUID());

        // when
        var result = mockMvc.perform(post("/coupons/redeem")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isForbidden());

        assertThat(couponRedeemRepository.findAll()).isEmpty();
    }

    @Test
    void shouldReturnConflictWhenCouponAlreadyRedeemedByUser() throws Exception {
        // given
        UUID userId = UUID.randomUUID();

        Coupon coupon = couponRepository.save(Coupon.builder()
                .code("ONCE2026")
                .maxUsageCount(5)
                .currentUsageCount(1)
                .countryCode("PL")
                .build());

        couponRedeemRepository.save(CouponRedemption.builder()
                .coupon(coupon)
                .userId(userId)
                .redeemedAt(OffsetDateTime.now())
                .requesterIp("127.0.0.1")
                .countryCode("PL")
                .build());

        given(geoIpService.resolveCountry(anyString())).willReturn("PL");

        RedeemCouponRequestRest request = new RedeemCouponRequestRest()
                .code(coupon.getCode())
                .userId(userId);

        // when
        var result = mockMvc.perform(post("/coupons/redeem")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isConflict());

        Coupon updatedCoupon = couponRepository.findById(coupon.getId()).orElseThrow();
        assertThat(updatedCoupon.getCurrentUsageCount()).isEqualTo(1);
        assertThat(couponRedeemRepository.findAll()).hasSize(1);
    }
}