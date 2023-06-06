package com.example.app.service;

import freemarker.cache.FileTemplateLoader;
import freemarker.template.Configuration;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class MailingServiceTest {

    @Autowired
    Configuration freeMarkerConfig;

    @SneakyThrows
    @Test
    void testTemplatesArePresent() {
        FileTemplateLoader templateLoader = new FileTemplateLoader(new File("src/main/resources/mail-templates"));
        freeMarkerConfig.setTemplateLoader(templateLoader);

        Assertions.assertNotNull(freeMarkerConfig.getTemplate(OrderService.MAIL_TEMPLATE_ORDER_IS_PAYED + ".ftl"));
        Assertions.assertNotNull(freeMarkerConfig.getTemplate(OrderService.MAIL_TEMPLATE_ORDER_IS_NOT_PAYED + ".ftl"));
    }

}