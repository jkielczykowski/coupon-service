package pl.coupon.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import pl.coupon.app.model.dto.IpApiDto;
import pl.coupon.app.handler.exception.GeoIpResolutionException;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class GeoIpService {

    private final RestTemplateBuilder restTemplateBuilder;

    @Value("${geoip.url}")
    private String geoIpUrl;

    @Value("${geoip.timeout.connect}")
    private Duration connectTimeout;

    @Value("${geoip.timeout.read}")
    private Duration readTimeout;

    public String resolveCountry(String ip) {
        if (ip == null || ip.isBlank()) {
            throw new GeoIpResolutionException("Client IP must not be blank.");
        }

        RestTemplate restTemplate = restTemplateBuilder
                .setConnectTimeout(connectTimeout)
                .setReadTimeout(readTimeout)
                .build();

        try {
            ResponseEntity<IpApiDto> response =
                    restTemplate.getForEntity(geoIpUrl, IpApiDto.class, ip);

            IpApiDto body = response.getBody();
            if (body == null) {
                throw new GeoIpResolutionException("GeoIP service returned empty response.");
            }

            if (!"success".equalsIgnoreCase(body.getStatus())) {
                throw new GeoIpResolutionException(
                        "GeoIP resolution failed for IP '%s': %s"
                                .formatted(ip, body.getMessage())
                );
            }

            if (body.getCountryCode() == null || body.getCountryCode().isBlank()) {
                throw new GeoIpResolutionException(
                        "GeoIP service returned empty country code for IP '%s'."
                                .formatted(ip)
                );
            }

            return body.getCountryCode().toUpperCase();
        } catch (RestClientException ex) {
            throw new GeoIpResolutionException(
                    "Failed to resolve country for IP '%s'.".formatted(ip), ex
            );
        }
    }
}