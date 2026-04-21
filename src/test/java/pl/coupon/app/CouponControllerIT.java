package pl.coupon.app;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import pl.coupon.app.frontend.model.CreateCouponRequestRest;
import pl.coupon.app.repository.CouponRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CouponControllerIT extends AbstractIT {

    @Autowired
    private CouponRepository couponRepository;

    @Test
    void shouldCreateCoupon() throws Exception {
        // given
        CreateCouponRequestRest request = new CreateCouponRequestRest()
                .code("WIOSNA2026")
                .maxUsages(100)
                .country("PL");

        // when
        var result = mockMvc.perform(post("/coupons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("WIOSNA2026"))
                .andExpect(jsonPath("$.maxUsageCount").value(100))
                .andExpect(jsonPath("$.currentUsageCount").value(0))
                .andExpect(jsonPath("$.countryCode").value("PL"))
                .andExpect(jsonPath("$.createdAt").exists());

        assertThat(couponRepository.findAll()).hasSize(1);
    }

    @Test
    void shouldReturnConflictWhenCreatingDuplicateCouponCode() throws Exception {
        // given
        CreateCouponRequestRest request = new CreateCouponRequestRest()
                .code("WIOSNA2026")
                .maxUsages(100)
                .country("PL");

        // when
        mockMvc.perform(post("/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("WIOSNA2026"))
                .andExpect(jsonPath("$.maxUsageCount").value(100))
                .andExpect(jsonPath("$.currentUsageCount").value(0))
                .andExpect(jsonPath("$.countryCode").value("PL"))
                .andExpect(jsonPath("$.createdAt").exists());

        CreateCouponRequestRest duplicateRequest = new CreateCouponRequestRest()
                .code("wiosna2026")
                .maxUsages(100)
                .country("PL");

        // when
        mockMvc.perform(post("/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                // then
                .andExpect(status().isConflict());

        assertThat(couponRepository.findAll()).hasSize(1);
    }

    @Test
    void shouldReturnCoupons() throws Exception {
        // given
        CreateCouponRequestRest request1 = new CreateCouponRequestRest()
                .code("WIOSNA2026")
                .maxUsages(100)
                .country("PL");

        CreateCouponRequestRest request2 = new CreateCouponRequestRest()
                .code("LATO2026")
                .maxUsages(50)
                .country("DE");

        mockMvc.perform(post("/coupons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)));

        mockMvc.perform(post("/coupons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)));

        // when
        var result = mockMvc.perform(get("/coupons"));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].code").exists())
                .andExpect(jsonPath("$[1].code").exists());
    }
}