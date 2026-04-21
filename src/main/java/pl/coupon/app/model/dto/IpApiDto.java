package pl.coupon.app.model.dto;

import lombok.Data;

@Data
public class IpApiDto {
    private String status;
    private String message;
    private String countryCode;
}
