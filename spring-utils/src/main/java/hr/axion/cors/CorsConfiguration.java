package hr.axion.cors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(CorsConfigurationProperties.class)
@PropertySource("classpath:/cors-configuration-defaults.properties")
@ConditionalOnProperty(value = "web.cors.enabled", matchIfMissing = true)
public class CorsConfiguration {

    @Bean
    public WebMvcConfigurer corsMappingConfigurer(final CorsConfigurationProperties corsConfigurationProperties) {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(final CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(corsConfigurationProperties.getAllowedOrigins())
                        .allowedMethods(corsConfigurationProperties.getAllowedMethods())
                        .allowedHeaders(corsConfigurationProperties.getAllowedHeaders())
                        .exposedHeaders(corsConfigurationProperties.getExposedHeaders())
                        .maxAge(Long.parseLong(corsConfigurationProperties.getMaxAge()));
            }
        };
    }
}
