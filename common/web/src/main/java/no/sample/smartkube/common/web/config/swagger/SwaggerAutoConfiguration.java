package no.sample.smartkube.common.web.config.swagger;

import com.fasterxml.classmate.TypeResolver;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.async.DeferredResult;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.AlternateTypeRule;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.schema.WildcardType;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.not;
import static springfox.documentation.builders.PathSelectors.ant;
import static springfox.documentation.builders.PathSelectors.any;
import static springfox.documentation.schema.AlternateTypeRules.newRule;

@Slf4j
@Configuration
@EnableSwagger2
@ConditionalOnProperty(prefix = "swagger", value = "enabled", havingValue = "true", matchIfMissing = false)
@EnableConfigurationProperties({SwaggerProperties.class})
public class SwaggerAutoConfiguration implements InitializingBean {

    private final SwaggerProperties props;
    private final TypeResolver typeResolver;
    private final BeanFactory beanFactory;

    @Autowired
    public SwaggerAutoConfiguration(SwaggerProperties props, TypeResolver typeResolver, BeanFactory beanFactory) {
        this.props = props;
        this.typeResolver = typeResolver;
        this.beanFactory = beanFactory;
    }

    @Override
    public void afterPropertiesSet() {
        ConfigurableBeanFactory configurableBeanFactory = (ConfigurableBeanFactory) beanFactory;
        if(this.props.getGroupBasePackages()!=null && this.props.getGroupBasePackages().size()>0) {
            this.props.getGroupBasePackages().forEach((group, packageName) -> configurableBeanFactory.registerSingleton(group.concat("-docket"), docket(group, packageName)));
        }else{
            configurableBeanFactory.registerSingleton(this.props.getGroup().concat("-docket"), docket(this.props.getGroup(), this.props.getBasePackage()));
        }
    }

    private Docket docket(String group, String packageName) {
        ApiInfo apiInfo = getApiInfo();
        AlternateTypeRule alternateTypeRule = getAlternateTypeRule();
        List<ResponseMessage> responseMessages = errorResponseMessages();
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName(group)
                .apiInfo(apiInfo)
                .select()
                .apis(Strings.isNullOrEmpty(packageName) ? RequestHandlerSelectors.any() : RequestHandlerSelectors.basePackage(packageName))
                .paths(excludedPathSelector())
                .build()
                .pathMapping("/")
                .directModelSubstitute(Date.class, String.class)
                .directModelSubstitute(org.joda.time.LocalDate.class, java.sql.Date.class)
                .directModelSubstitute(org.joda.time.DateTime.class, java.util.Date.class)
                .genericModelSubstitutes(ResponseEntity.class)
                .alternateTypeRules(alternateTypeRule)
                .useDefaultResponseMessages(false)
                .globalResponseMessage(RequestMethod.GET, responseMessages)
                .globalResponseMessage(RequestMethod.POST, responseMessages)
                .globalResponseMessage(RequestMethod.PATCH, responseMessages)
                .globalResponseMessage(RequestMethod.PUT, responseMessages)
                .globalResponseMessage(RequestMethod.DELETE, responseMessages)
                .forCodeGeneration(true)
                .enableUrlTemplating(true)
                .additionalModels(typeResolver.resolve(FailureModelRef.class));
    }

    private List<ResponseMessage> errorResponseMessages() {
        return Lists.newArrayList(
                    new ResponseMessageBuilder().code(400).message("Bad Request: The request cannot be fulfilled due to bad syntax.").responseModel(new ModelRef("FailureModelRef")).build(),
                    new ResponseMessageBuilder().code(401).message("Unauthorized: The request access denied").responseModel(new ModelRef("FailureModelRef")).build(),
                    new ResponseMessageBuilder().code(403).message("Forbidden: The server understood the request, but is refusing to fulfill it").responseModel(new ModelRef("FailureModelRef")).build(),
                    new ResponseMessageBuilder().code(406).message("Not Acceptable: The requested resource is only capable of generating content not acceptable according to the Accept headers sent in the request").responseModel(new ModelRef("FailureModelRef")).build(),
                    new ResponseMessageBuilder().code(409).message("Conflict: The request could not be completed due to a conflict with the current state of the resource").responseModel(new ModelRef("FailureModelRef")).build(),
                    new ResponseMessageBuilder().code(415).message("Unsupported Media Type: The request entity has a media type which the server or resource does not support").responseModel(new ModelRef("FailureModelRef")).build(),
                    new ResponseMessageBuilder().code(500).message("Server Error: The server encountered an unexpected condition which prevented it from fulfilling the request").responseModel(new ModelRef("FailureModelRef")).build()
        );
    }

    private AlternateTypeRule getAlternateTypeRule() {
        return newRule(
                    typeResolver.resolve(DeferredResult.class, typeResolver.resolve(ResponseEntity.class, WildcardType.class)),
                    typeResolver.resolve(WildcardType.class)
        );
    }

    private ApiInfo getApiInfo() {
        return new ApiInfoBuilder()
                    .title(props.getTitle())
                    .description(props.getDescription())
                    .version(props.getVersion())
                    .termsOfServiceUrl(props.getTermsOfServiceUrl())
                    .contact(new Contact(props.getName(), props.getUrl(), props.getEmail()))
                    .license(props.getLicense())
                    .licenseUrl(props.getLicenseUrl())
                    .build();
    }


    @SuppressWarnings("Guava")
    private Predicate<String> excludedPathSelector() {
        if (Strings.isNullOrEmpty(props.getExcludes())) {
            return any();
        }

        return and(StreamSupport.stream(Splitter.on(',').trimResults().omitEmptyStrings().split(props.getExcludes()).spliterator(), false)
                    .map(s -> not(ant(s.trim())))
                    .collect(Collectors.toList()));
    }
}
