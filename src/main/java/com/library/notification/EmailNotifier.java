package com.library.notification;

import com.library.domain.User;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 // Simple email notifier that records sent messages for verification.
**/
public class EmailNotifier implements ReminderObserver {
  private final List<String> sentMessages = new ArrayList<>();

  @Override
  public void notify(User user, String message) {
    Objects.requireNonNull(user, "user");
    Objects.requireNonNull(message, "message");

    String toEmail = user.getUsername();
    sentMessages.add("To %s: %s".formatted(toEmail, message));

    sendSmtpEmail(toEmail, "Library Reminder", message);
  }

  private void sendSmtpEmail(String toEmail, String subject, String body) {
    String host = getenvTrimmed("LIBRARY_SMTP_HOST");
    String port = getenvTrimmed("LIBRARY_SMTP_PORT");
    String username = getenvTrimmed("LIBRARY_SMTP_USERNAME");
    String password = getenvTrimmed("LIBRARY_SMTP_PASSWORD");
    String from = getenvTrimmed("LIBRARY_SMTP_FROM");
    String startTls = getenvTrimmed("LIBRARY_SMTP_STARTTLS");
    String ssl = getenvTrimmed("LIBRARY_SMTP_SSL");

    // Ease-of-use defaults (Gmail)
    if (isBlank(host)) {
      host = "smtp.gmail.com";
    }

    if (isBlank(from) || isBlank(username) || isBlank(password)) {
      throw new IllegalStateException(
          "SMTP is not configured. Please set environment variables: "
              + "LIBRARY_SMTP_USERNAME, LIBRARY_SMTP_PASSWORD, LIBRARY_SMTP_FROM"
              + " (optional: LIBRARY_SMTP_HOST, LIBRARY_SMTP_PORT, LIBRARY_SMTP_SSL, LIBRARY_SMTP_STARTTLS)."
      );
    }

    Properties props = new Properties();
    props.put("mail.smtp.host", host);
    if (!isBlank(port)) {
      props.put("mail.smtp.port", port);
    }
    props.put("mail.smtp.auth", "true");
    boolean sslEnabled = !isBlank(ssl) && Boolean.parseBoolean(ssl);
    if (sslEnabled) {
      props.put("mail.smtp.ssl.enable", "true");
      if (isBlank(port)) {
        props.put("mail.smtp.port", "465");
      }
    } else {
      props.put("mail.smtp.starttls.enable", isBlank(startTls) ? "true" : startTls);
      if (isBlank(port)) {
        props.put("mail.smtp.port", "587");
      }
    }

    Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
      @Override
      protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
        return new jakarta.mail.PasswordAuthentication(username, password);
      }
    });

    try {
      MimeMessage msg = new MimeMessage(session);
      msg.setFrom(new InternetAddress(from));
      msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
      msg.setSubject(subject, StandardCharsets.UTF_8.name());
      msg.setText(body, StandardCharsets.UTF_8.name());
      Transport.send(msg);
    } catch (MessagingException e) {
      throw new RuntimeException("Failed to send email via SMTP: " + e.getMessage(), e);
    }
  }

  private static String getenvTrimmed(String key) {
    String v = System.getenv(key);
    return v == null ? null : v.trim();
  }

  private static boolean isBlank(String s) {
    return s == null || s.trim().isEmpty();
  }

  public List<String> getSentMessages() {
    return Collections.unmodifiableList(sentMessages);
  }

  public void clear() {
    sentMessages.clear();
  }
}

