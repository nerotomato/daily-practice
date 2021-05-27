package com.nerotomato.separate.config;

import com.google.common.collect.Sets;
import io.swagger.models.auth.In;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.boot.SpringBootVersion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

/**
 * @EnableOpenApi注解，启用swagger配置 Created by nero on 2021/4/28.
 */
@Configuration
@EnableOpenApi
public class SwaggerConfiguration implements WebMvcConfigurer {

    private final SwaggerProperties swaggerProperties;

    public SwaggerConfiguration(SwaggerProperties swaggerProperties) {
        this.swaggerProperties = swaggerProperties;
    }

    /**
     * 一个Docket是一个组，组的划分可以通过paths()或apis()来实现，
     * 组名称可以作为api搜索条件。组包含多个tags，tag可以包含多个api；
     * apiInfo():配置文档描述
     * apis():api过滤规则，可以自定义为那些接口生成文档
     * useDefaultResponseMessages():是否使用默认的响应信息
     * paths()::可以根据请求路径过滤api
     * globalResponseMessage():：所有接口统一定义响应信息
     * produces(): 为所有接口设置响应类型
     * consumes():(): 为所有接口设置请求类型
     * protocols(): 为所有接口设置支持的协议
     * groupName（）：组名称
     * tags：设置标签
     */
    @Bean
    public Docket createRestApi() {
        //DocumentationType.OAS_30: openApi3.0
        return new Docket(DocumentationType.OAS_30)
                .pathMapping("/")
                // 定义是否开启swagger，false为关闭，可以通过变量控制
                .enable(swaggerProperties.getEnable())
                // 将api的元信息设置为包含在json ResourceListing响应中。
                .apiInfo(getApiInfo())
                // 接口调试地址
                .host(swaggerProperties.getTryHost())
                // 选择哪些接口作为swagger的doc发布
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build()

                // 支持的通讯协议集合
                .protocols(Sets.newHashSet("https", "http"))
                // 授权信息设置，必要的header token等认证信息
                .securitySchemes(securitySchemes())
                // 授权信息全局应用
                .securityContexts(securityContext());
    }

    /**
     * API页面上半部分的展示信息
     */
    private ApiInfo getApiInfo() {
        return new ApiInfoBuilder().title(swaggerProperties.getApplicationName() + " Api Doc")
                .description(swaggerProperties.getApplicationDescription())
                .contact(new Contact("com/nerotomato/shardingspherejdbc", null, "guanhaowen@outlook.com"))
                .version("Application Version: " + swaggerProperties.getApplicationVersion() + ", Spring Boot Version: " + SpringBootVersion.getVersion())
                .build();
    }

    /**
     * 设置授权信息
     */
    private List<SecurityScheme> securitySchemes() {
        ApiKey apiKey = new ApiKey("BASE_TOKEN", "token", In.HEADER.toValue());
        return Collections.singletonList(apiKey);
    }

    /**
     * 授权信息全局应用
     */
    private List<SecurityContext> securityContext() {
        return Collections.singletonList(
                SecurityContext
                        .builder()
                        .securityReferences(Collections.singletonList(
                                new SecurityReference("BASE_TOKEN",
                                        new AuthorizationScope[]{
                                                new AuthorizationScope("global", "")
                                        })))
                        .build()
        );
    }

    /**
     * 通用拦截器排除swagger设置，所有拦截器都会自动加swagger相关的资源排除信息
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        try {
            Field registrationsField = FieldUtils.getField(InterceptorRegistry.class, "registrations", true);
            List<InterceptorRegistration> registrations = (List<InterceptorRegistration>) ReflectionUtils.getField(registrationsField, registry);

            if (registrations != null) {
                for (InterceptorRegistration interceptorRegistration : registrations) {
                    interceptorRegistration
                            .excludePathPatterns("/swagger**/**")
                            .excludePathPatterns("/webjars/**")
                            .excludePathPatterns("/v3/**")
                            .excludePathPatterns("/doc.html");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
