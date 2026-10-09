package com.platepal.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class DatabaseConfiguration {
    @Bean
    DataSource dataSource(
            @Value("${spring.datasource.url}") String configuredUrl,
            @Value("${spring.datasource.username:}") String configuredUsername,
            @Value("${spring.datasource.password:}") String configuredPassword) {
        if (configuredUrl.startsWith("jdbc:postgresql:")) {
            return DataSourceBuilder.create()
                    .url(configuredUrl)
                    .username(configuredUsername)
                    .password(configuredPassword)
                    .build();
        }

        URI uri = URI.create(configuredUrl);
        if (!"postgres".equals(uri.getScheme()) && !"postgresql".equals(uri.getScheme())) {
            throw new IllegalArgumentException("Database URL must use jdbc:postgresql, postgres, or postgresql");
        }

        String jdbcUrl = "jdbc:postgresql://" + uri.getHost()
                + (uri.getPort() > 0 ? ":" + uri.getPort() : "")
                + uri.getRawPath()
                + (StringUtils.hasText(uri.getRawQuery()) ? "?" + uri.getRawQuery() : "");
        String username = configuredUsername;
        String password = configuredPassword;
        String userInfo = uri.getRawUserInfo();
        if (StringUtils.hasText(userInfo)) {
            int separator = userInfo.indexOf(':');
            username = decode(separator < 0 ? userInfo : userInfo.substring(0, separator));
            if (separator >= 0) {
                password = decode(userInfo.substring(separator + 1));
            }
        }
        return DataSourceBuilder.create().url(jdbcUrl).username(username).password(password).build();
    }

    private String decode(String value) {
        return URLDecoder.decode(value.replace("+", "%2B"), StandardCharsets.UTF_8);
    }
}