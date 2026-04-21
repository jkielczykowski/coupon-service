package pl.coupon.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "coupon",
        indexes = {
                @Index(name = "idx_coupon_code", columnList = "code")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private String code;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "max_usage_count", nullable = false)
    private Integer maxUsageCount;

    @Column(name = "current_usage_count", nullable = false)
    private Integer currentUsageCount;

    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode;

    public void redeem() {
        this.currentUsageCount++;
    }

}