package com.bondoc.song;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class DataSourceConfig {

    @Value("${DATABASE_URL:}")
    private String databaseUrl;

    @Value("${DB_USERNAME:}")
    private String dbUsername;

    @Value("${DB_PASSWORD:}")
    private String dbPassword;

    @Bean
    public DataSource dataSource() {
        String url = normalizeDatabaseUrl(databaseUrl);

        DataSourceBuilder<?> builder = DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .driverClassName("org.postgresql.Driver")
                .url(url);

        if (StringUtils.hasText(dbUsername)) {
            builder.username(dbUsername);
        }
        if (StringUtils.hasText(dbPassword)) {
            builder.password(dbPassword);
        }

        return builder.build();
    }

    private String normalizeDatabaseUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return "jdbc:postgresql://localhost:5432/db_song";
        }

        String trimmed = url.trim();

        if (trimmed.startsWith("postgres://") || trimmed.startsWith("postgresql://")) {
            return rewriteToJdbcUrl(trimmed);
        }

        if (trimmed.startsWith("jdbc:postgresql://")) {
            return rewriteToJdbcUrl(trimmed);
        }

        return trimmed;
    }

    private String rewriteToJdbcUrl(String url) {
        try {
            String uriText = url.startsWith("jdbc:") ? url.substring(5) : url;
            URI uri = new URI(uriText);

            StringBuilder builder = new StringBuilder("jdbc:postgresql://");
            builder.append(uri.getHost());
            if (uri.getPort() != -1) {
                builder.append(':').append(uri.getPort());
            }
            String path = uri.getPath();
            if (StringUtils.hasText(path)) {
                builder.append(path);
            }

            String query = uri.getQuery();
            if (uri.getUserInfo() != null) {
                String userInfo = uri.getUserInfo();
                String[] parts = userInfo.split(":", 2);
                String user = parts[0];
                String password = parts.length > 1 ? parts[1] : "";
                query = appendQueryParameter(query, "user", user);
                query = appendQueryParameter(query, "password", password);
            }

            if (StringUtils.hasText(query)) {
                builder.append('?').append(query);
            }

            return builder.toString();
        } catch (URISyntaxException ex) {
            return url;
        }
    }

    private String appendQueryParameter(String query, String key, String value) {
        if (!StringUtils.hasText(value)) {
            return query;
        }

        String encoded = key + "=" + value;
        if (!StringUtils.hasText(query)) {
            return encoded;
        }
        return query + "&" + encoded;
    }
}
