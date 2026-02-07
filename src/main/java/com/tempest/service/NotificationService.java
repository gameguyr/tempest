package com.tempest.service;

import com.tempest.entity.NotificationType;
import com.tempest.entity.WeatherAlert;
import com.tempest.entity.WeatherReading;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * Service for sending alert notifications via email and SMS.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender mailSender;
    private final TwilioSmsSender smsSender;

    @Value("${tempest.notification.from-email}")
    private String fromEmail;

    @Value("${tempest.notification.from-name}")
    private String fromName;

    /**
     * Send alert notification via configured channels.
     *
     * @param alert       the triggered alert
     * @param reading     the weather reading that triggered the alert
     * @param actualValue the actual metric value
     */
    public void sendAlertNotification(WeatherAlert alert, WeatherReading reading, Double actualValue) {
        String subject = String.format("Weather Alert: %s", alert.getName());
        String htmlMessage = buildAlertMessage(alert, reading, actualValue);
        String textMessage = buildTextAlertMessage(alert, reading, actualValue);

        switch (alert.getNotificationType()) {
            case EMAIL -> sendEmail(alert.getUserEmail(), subject, htmlMessage);
            case SMS -> sendSms(alert.getUserPhone(), textMessage);
            case BOTH -> {
                sendEmail(alert.getUserEmail(), subject, htmlMessage);
                sendSms(alert.getUserPhone(), textMessage);
            }
        }
    }

    /**
     * Send email notification.
     *
     * @param to      recipient email address
     * @param subject email subject
     * @param body    email body (HTML)
     */
    private void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // HTML content

            mailSender.send(message);
            log.info("Email sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}", to, e);
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    /**
     * Send SMS notification.
     *
     * @param to   recipient phone number
     * @param body message content
     */
    private void sendSms(String to, String body) {
        try {
            smsSender.sendSms(to, body);
            log.info("SMS sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send SMS to {}", to, e);
            throw new RuntimeException("Failed to send SMS: " + e.getMessage(), e);
        }
    }

    /**
     * Build HTML email message content.
     *
     * @param alert       the alert
     * @param reading     the weather reading
     * @param actualValue the actual value
     * @return HTML formatted message
     */
    private String buildAlertMessage(WeatherAlert alert, WeatherReading reading, Double actualValue) {
        String stationInfo = alert.getStationId() != null
                ? alert.getStationId()
                : "All Stations";

        String timestamp = reading.getTimestamp().format(
                DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss"));

        return String.format("""
                        <html>
                        <body style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto;">
                            <div style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 30px; text-align: center; border-radius: 8px 8px 0 0;">
                                <h1 style="color: white; margin: 0; font-size: 28px;">⚠️ Weather Alert</h1>
                            </div>
                            <div style="background: #f7f7f7; padding: 30px; border-radius: 0 0 8px 8px;">
                                <div style="background: white; padding: 25px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                                    <h2 style="color: #ff6b6b; margin-top: 0;">%s</h2>
                                    <p style="font-size: 16px; line-height: 1.6; margin: 10px 0;">
                                        <strong>Station:</strong> %s
                                    </p>
                                    <p style="font-size: 16px; line-height: 1.6; margin: 10px 0;">
                                        <strong>Condition:</strong> %s %s %s %s
                                    </p>
                                    <p style="font-size: 18px; line-height: 1.6; margin: 15px 0; padding: 15px; background: #fff3cd; border-left: 4px solid #ffc107; border-radius: 4px;">
                                        <strong>Current Value:</strong> <span style="color: #ff6b6b; font-size: 22px;">%.2f %s</span>
                                    </p>
                                    <p style="font-size: 14px; color: #666; margin: 10px 0;">
                                        <strong>Time:</strong> %s
                                    </p>
                                </div>
                                <hr style="border: none; border-top: 1px solid #ddd; margin: 25px 0;">
                                <p style="font-size: 12px; color: #999; text-align: center; margin: 10px 0;">
                                    This is an automated alert from your Tempest Weather Station system.
                                </p>
                            </div>
                        </body>
                        </html>
                        """,
                alert.getName(),
                stationInfo,
                alert.getMetric().getDisplayName(),
                alert.getOperator().getSymbol(),
                alert.getThreshold(),
                alert.getMetric().getUnit(),
                actualValue,
                alert.getMetric().getUnit(),
                timestamp
        );
    }

    /**
     * Build plain text SMS message content.
     *
     * @param alert       the alert
     * @param reading     the weather reading
     * @param actualValue the actual value
     * @return plain text message
     */
    private String buildTextAlertMessage(WeatherAlert alert, WeatherReading reading, Double actualValue) {
        String stationInfo = alert.getStationId() != null ? alert.getStationId() : "All";

        return String.format("ALERT: %s | %s: %.1f%s %s %.1f%s | Station: %s",
                alert.getName(),
                alert.getMetric().getDisplayName(),
                actualValue,
                alert.getMetric().getUnit(),
                alert.getOperator().getSymbol(),
                alert.getThreshold(),
                alert.getMetric().getUnit(),
                stationInfo
        );
    }
}
