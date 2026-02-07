package com.tempest.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Wrapper service for sending SMS notifications via Twilio.
 */
@Component
@Slf4j
public class TwilioSmsSender {

    @Value("${tempest.twilio.account-sid:}")
    private String accountSid;

    @Value("${tempest.twilio.auth-token:}")
    private String authToken;

    @Value("${tempest.twilio.from-number:}")
    private String fromNumber;

    @Value("${tempest.twilio.enabled:false}")
    private boolean enabled;

    /**
     * Initialize Twilio client on component startup.
     */
    @PostConstruct
    public void init() {
        if (enabled) {
            if (accountSid == null || accountSid.isEmpty() ||
                    authToken == null || authToken.isEmpty() ||
                    fromNumber == null || fromNumber.isEmpty()) {
                log.warn("Twilio SMS is enabled but credentials are not fully configured");
                return;
            }
            try {
                Twilio.init(accountSid, authToken);
                log.info("Twilio SMS service initialized successfully");
            } catch (Exception e) {
                log.error("Failed to initialize Twilio SMS service", e);
            }
        } else {
            log.info("Twilio SMS service is disabled");
        }
    }

    /**
     * Send an SMS message.
     *
     * @param to   the recipient phone number in E.164 format (e.g., +15551234567)
     * @param body the message content
     * @throws RuntimeException if SMS service is disabled or send fails
     */
    public void sendSms(String to, String body) {
        if (!enabled) {
            log.warn("Twilio SMS is disabled, skipping SMS to {}", to);
            throw new RuntimeException("SMS service is not enabled");
        }

        try {
            // Truncate message to 160 characters for SMS (standard length)
            String truncatedBody = body.length() > 160
                    ? body.substring(0, 157) + "..."
                    : body;

            Message message = Message.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(fromNumber),
                    truncatedBody
            ).create();

            log.info("SMS sent successfully to {}: SID={}", to, message.getSid());
        } catch (Exception e) {
            log.error("Failed to send SMS to {} via Twilio", to, e);
            throw new RuntimeException("Failed to send SMS: " + e.getMessage(), e);
        }
    }
}
