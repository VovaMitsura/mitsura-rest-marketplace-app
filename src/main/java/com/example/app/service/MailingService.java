package com.example.app.service;

import com.example.app.model.User;
import freemarker.cache.FileTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Service
public class MailingService {

    private final JavaMailSender mailSender;
    private final Configuration freeMarkerConfig;
    private final Properties subjects;

    public MailingService(JavaMailSender mailSender,
                          Configuration freeMarkerConfig) throws IOException {
        this.mailSender = mailSender;
        this.freeMarkerConfig = freeMarkerConfig;
        this.subjects = new Properties();
        this.subjects.load(this.getClass().getClassLoader().getResourceAsStream("mail-subjects.properties"));
    }

    public void send(User user, String template, Map<String, Object> payloads) throws MessagingException,
            TemplateException, IOException {
        this.send(user, user.getEmail(), template, payloads);
    }

    public void send(User user, String emailToAddress, String template, Map<String, Object> payloads)
            throws MessagingException, IOException, TemplateException {
        Map<String, Object> content = new HashMap<>(payloads);
        content.put("customer", user);

        MimeMessage mailMessage = this.mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mailMessage, true);
        helper.setTo(emailToAddress);
        helper.setSubject(this.subjects.getProperty(template));

        FileTemplateLoader templateLoader = new FileTemplateLoader(new File("src/main/resources/mail-templates"));
        freeMarkerConfig.setTemplateLoader(templateLoader);
        Template tml = this.freeMarkerConfig.getTemplate(template + ".ftl");
        StringWriter writer = new StringWriter();
        tml.process(content, writer);
        helper.setText(writer.toString(), true);

        this.mailSender.send(mailMessage);
    }
}