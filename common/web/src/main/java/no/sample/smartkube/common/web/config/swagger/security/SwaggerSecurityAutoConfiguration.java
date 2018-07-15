package no.sample.smartkube.common.web.config.swagger.security;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import no.sample.smartkube.common.web.config.swagger.SwaggerProperties;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.AuthorizationCodeGrantBuilder;
import springfox.documentation.builders.OAuthBuilder;
import springfox.documentation.service.*;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.SecurityConfigurationBuilder;

import java.util.Arrays;
import java.util.Collections;

@Getter
@Setter
@Slf4j
@Configuration
@ConditionalOnProperty(prefix="swagger.security", value = "enabled", havingValue = "true", matchIfMissing = false)
@EnableConfigurationProperties(KeycloakProperties.class)
public class SwaggerSecurityAutoConfiguration implements InitializingBean{

    private final KeycloakProperties keycloakProperties;
    private final SwaggerProperties swaggerProperties;
    private final BeanFactory beanFactory;

    public SwaggerSecurityAutoConfiguration(SwaggerProperties swaggerProperties, KeycloakProperties keycloakProperties, BeanFactory beanFactory){
        this.swaggerProperties = swaggerProperties;
        this.keycloakProperties = keycloakProperties;
        this.beanFactory = beanFactory;
    }

    @Override
    public void afterPropertiesSet(){
        log.debug("Configuring swagger security");
        String[] beanNamesForType = ((DefaultListableBeanFactory) beanFactory).getBeanNamesForType(Docket.class);
        Arrays.stream(beanNamesForType).forEach(docketNames -> {
            beanFactory.getBean(docketNames, Docket.class)
                .securityContexts(Collections.singletonList(securityContext()))
                .securitySchemes(Collections.singletonList(securityScheme()));
        });
    }

    @Bean
    public SecurityConfiguration security() {
        return SecurityConfigurationBuilder.builder()
                .clientId(swaggerProperties.getSecurity().getClientId())
                .scopeSeparator(" ")
                .useBasicAuthenticationWithAccessCodeGrant(true)
                .build();
    }

    private SecurityScheme securityScheme() {
        GrantType grantType = new AuthorizationCodeGrantBuilder()
                .tokenEndpoint(new TokenEndpoint(keycloakProperties.getAuthServerUrl() + "/realms/"+keycloakProperties.getRealm()+"/protocol/openid-connect/token", "refresh-token"))
                .tokenRequestEndpoint(new TokenRequestEndpoint(keycloakProperties.getAuthServerUrl() + "/realms/"+keycloakProperties.getRealm()+"/protocol/openid-connect/auth", swaggerProperties.getSecurity().getClientId(), ""))
                .build();

        return new OAuthBuilder().name("spring_oauth")
                .grantTypes(Collections.singletonList(grantType))
                .scopes(Arrays.asList(scopes()))
                .build();
    }

    private AuthorizationScope[] scopes() {
        return new AuthorizationScope[]{
                new AuthorizationScope("openid", "openid")};
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
                .securityReferences(Collections.singletonList(new SecurityReference("spring_oauth", scopes())))
                .build();
    }

}
