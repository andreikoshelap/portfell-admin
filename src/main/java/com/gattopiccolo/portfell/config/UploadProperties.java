package com.gattopiccolo.portfell.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@ConfigurationProperties("app.upload")
public record UploadProperties(Path directory) {
}
