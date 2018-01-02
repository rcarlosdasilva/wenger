package io.github.rcarlosdasilva.wenger.feature;

import com.jarvis.cache.autoconfigure.AutoloadCacheAutoConfigure;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration
@EnableAutoConfiguration(exclude = {ThymeleafAutoConfiguration.class, AutoloadCacheAutoConfigure.class,
    MongoDataAutoConfiguration.class, MongoAutoConfiguration.class})
public class TestBoostrap {
}
