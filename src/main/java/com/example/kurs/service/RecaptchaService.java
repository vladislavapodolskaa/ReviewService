package com.example.kurs.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
public class RecaptchaService {

    @Value("${google.recaptcha.key.secret}")
    private String secretKey;

    private static final String GOOGLE_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify?secret=%s&response=%s";

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean validateCaptcha(String captchaResponse) {
        if (captchaResponse == null || captchaResponse.isEmpty()) {
            return false;
        }

        String url = String.format(GOOGLE_VERIFY_URL, secretKey, captchaResponse);
        try {
            Map response = restTemplate.postForObject(url, null, Map.class);
            return response != null && (Boolean) response.get("success");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}