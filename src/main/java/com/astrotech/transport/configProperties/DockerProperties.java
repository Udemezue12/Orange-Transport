package com.astrotech.transport.configProperties;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

import org.springframework.validation.annotation.Validated;

import java.util.List;


@ConfigurationProperties(prefix = "docker.rabbitmq")
@Validated
public record DockerProperties(

        boolean dockerEnabled,
        @NotBlank
        String host,

        @NotBlank
        String virtualHost,


        @NotNull
        Integer port,

        @NotBlank
        String username,

        @NotBlank
        String password,
        List<String> addresses) {

}

