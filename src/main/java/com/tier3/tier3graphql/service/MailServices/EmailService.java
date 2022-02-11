package com.tier3.tier3graphql.service.MailServices;

import com.tier3.tier3graphql.model.User;
import freemarker.template.Configuration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;

@Service @RequiredArgsConstructor @Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final Configuration freemarkerConfiguration;
    private final String baseUrl = "https://vumah-web.d8raj0ahkc24d.amplifyapp.com";

    public void sendVerificationToken(String token, User user) {
        final String confirmationUrl = baseUrl + "/email-verification/" + token;
        sendHtmlEmail("Verify your Email", "Thank you for registering with Tier3 Engineers.", confirmationUrl, user, "Verify your email address", "Please click the link below to activate your account. This is a one time usage link and will expire in 24 hours.");
    }

    public void sendConfirmPasswordChangeToken(String token, User user) {
        final String confirmationUrl = baseUrl + "/verify-password-change/" + token;
        sendHtmlEmail("New Password Confirmation", "Password confirmation for your account", confirmationUrl, user, "Confirm Password Change", "Please click the link below to confirm the new password. This is a one the usage link and will expire in 24 hours.");
    }

    public void sendForgotPasswordToken(String token, User user) {
        final String confirmationUrl = baseUrl + "/forgot-password/" + token;
        sendHtmlEmail("Forgot Password", "Password Reset for your account", confirmationUrl, user, "Reset Password", "Please click the link below to enter new password. This is a one the usage link and will expire in 24 hours.");
    }

    private String geFreeMarkerTemplateContent(Map<String, Object> model, String templateName) {
        StringBuffer content = new StringBuffer();
        try {
            content.append(FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerConfiguration.getTemplate(templateName), model));
            return content.toString();
        } catch (Exception e) {
            System.out.println("Exception occurred while processing template:" + e.getMessage());
        }
        return "";
    }

    private void sendHtmlEmail(String subject, String subTitle, String link, User user, String buttonText, String msg) {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("name", user.getFirstName() + " " + user.getLastName());
        model.put("link", link);
        model.put("title", subject);
        model.put("subTitle", subTitle);
        model.put("msg", msg);
        model.put("btn", buttonText);
        try {
            sendHtmlMail("kanyeallanz@gmail.com", user.getUsername(), subject, geFreeMarkerTemplateContent(model, "mail/verification.ftl"));
        } catch (MessagingException e) {
            log.error("Failed to send mail", e);
        }
    }

    private void sendHtmlMail(String from, String to, String subject, String body) throws MessagingException {
        MimeMessage mail = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mail, true, "UTF-8");


        helper.setFrom(from);
        if (to.contains(",")) {
            helper.setTo(to.split(","));
        } else {
            helper.setTo(to);
        }
        helper.setSubject(subject);
        helper.setText(body, true);
        helper.addInline("logo.png", new ClassPathResource("logo.png"));

        mailSender.send(mail);
        log.info("Sent mail: {0}", subject);
    }

}
