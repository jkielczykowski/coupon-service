package pl.coupon.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "coupon_redemption",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_coupon_redemption_coupon_user",
                        columnNames = {"coupon_id", "user_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponRedemption {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "redeemed_at", nullable = false)
    private OffsetDateTime redeemedAt;

    @Column(name = "requester_ip", nullable = false)
    private String requesterIp;

    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode;
}