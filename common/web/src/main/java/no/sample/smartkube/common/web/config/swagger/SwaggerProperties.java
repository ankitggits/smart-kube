package no.sample.smartkube.common.web.config.swagger;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "swagger")
public class SwaggerProperties {

    private boolean enabled;

    private String group = "api";

    private String title = "[set a api title via 'swagger.title']";

    private String description = "[add your api description via 'swagger.description']";

    private String version = "[add your api version via 'swagger.version']";

    private String termsOfServiceUrl = "[set termsOfServiceUrl via 'swagger.termsOfServiceUrl']";

    private String name = "[set name via 'swagger.name']";

    private String url;

    private String email;

    private String basePackage = "no.sample";

    private String license;

    private String licenseUrl;

    private String excludes;

    private Map<String, String> groupBasePackages = new HashMap<>();

    private SwaggerSecurityProperties security;

    @Getter
    @Setter
    @ConfigurationProperties
    public static class SwaggerSecurityProperties {

        private boolean enabled = false;
        private String clientId;
    }
}
