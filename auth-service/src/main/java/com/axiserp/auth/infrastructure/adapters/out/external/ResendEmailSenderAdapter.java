package com.axiserp.auth.infrastructure.adapters.out.external;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.axiserp.auth.ports.output.EmailSenderPort;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

import java.io.UnsupportedEncodingException;

@Component
@RequiredArgsConstructor
public class ResendEmailSenderAdapter implements EmailSenderPort {

    private static final Logger log = LoggerFactory.getLogger(ResendEmailSenderAdapter.class);

    private final JavaMailSender mailSender;

    @Value("${app-frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app-email-from:onboarding@resend.dev}")
    private String fromEmail;

    @Value("${app-email-from-name:AxisERP}")
    private String fromName;

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        String resetLink = frontendUrl + "/reset-password?token=" + resetToken;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject("AxisERP - Restablecer tu contraseña");
            helper.setText(buildHtmlEmail(resetLink), true);

            mailSender.send(message);
            log.info("password_reset_email_sent to={}", toEmail);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("password_reset_email_failed to={} error={}", toEmail, e.getMessage());
            throw new RuntimeException("Error al enviar el correo de recuperación", e);
        }
    }

    private String buildHtmlEmail(String resetLink) {
        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Restablecer contraseña</title>
            </head>
            <body style="margin:0; padding:0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background-color: #f3f4f6;">
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background-color: #f3f4f6; padding: 40px 0;">
                    <tr>
                        <td align="center">
                            <table role="presentation" width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 1px 3px rgba(0,0,0,0.1);">

                                <!-- Header -->
                                <tr>
                                    <td style="background: linear-gradient(135deg, #1e3a5f 0%%, #2563eb 100%%); padding: 32px 40px; text-align: center;">
                                        <h1 style="margin: 0; color: #ffffff; font-size: 24px; font-weight: 700; letter-spacing: -0.5px;">AxisERP</h1>
                                        <p style="margin: 8px 0 0; color: #93c5fd; font-size: 14px;">Sistema de Gestión Empresarial</p>
                                    </td>
                                </tr>

                                <!-- Body -->
                                <tr>
                                    <td style="padding: 40px;">
                                        <h2 style="margin: 0 0 16px; color: #1f2937; font-size: 20px; font-weight: 600;">Restablecer tu contraseña</h2>
                                        <p style="margin: 0 0 24px; color: #4b5563; font-size: 15px; line-height: 1.6;">
                                            Hemos recibido una solicitud para restablecer la contraseña de tu cuenta en AxisERP.
                                            Haz clic en el botón de abajo para crear una nueva contraseña.
                                        </p>

                                        <!-- Button -->
                                        <table role="presentation" width="100%%" cellpadding="0" cellspacing="0">
                                            <tr>
                                                <td align="center" style="padding: 8px 0;">
                                                    <a href="%s"
                                                       style="display: inline-block; background-color: #2563eb; color: #ffffff; text-decoration: none; padding: 14px 32px; border-radius: 6px; font-size: 15px; font-weight: 600;">
                                                        Restablecer contraseña
                                                    </a>
                                                </td>
                                            </tr>
                                        </table>

                                        <p style="margin: 24px 0 0; color: #6b7280; font-size: 14px; line-height: 1.6;">
                                            Si no solicitaste este cambio, puedes ignorar este correo. Tu contraseña actual seguirá vigente.
                                        </p>
                                        <p style="margin: 8px 0 0; color: #6b7280; font-size: 13px;">
                                            Este enlace expirará en 24 horas por seguridad.
                                        </p>
                                    </td>
                                </tr>

                                <!-- Footer -->
                                <tr>
                                    <td style="background-color: #f9fafb; padding: 24px 40px; border-top: 1px solid #e5e7eb; text-align: center;">
                                        <p style="margin: 0; color: #9ca3af; font-size: 12px;">
                                            Este es un correo automático, por favor no respondas a este mensaje.
                                        </p>
                                        <p style="margin: 8px 0 0; color: #9ca3af; font-size: 12px;">
                                            &copy; 2026 AxisERP. Todos los derechos reservados.
                                        </p>
                                    </td>
                                </tr>

                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(resetLink);
    }
}
