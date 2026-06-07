package ru.vstu.clothstock.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendLowStockAlert(String productName, int stock) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("system@clothstock.ru");
        message.setTo("manager@vstu.ru");
        message.setSubject("Алерт: Дефицит товара " + productName);
        message.setText("Остаток товара " + productName + " опустился до " + stock + " шт.");

        try {
            mailSender.send(message);
        } catch (Exception ignored) {
        }
    }
}