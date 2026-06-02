package com.axiserp.auth.ports.output;

public interface EmailSenderPort {

    void sendPasswordResetEmail(String toEmail, String resetToken);
}
