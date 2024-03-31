package hr.axion.cors;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("web.cors")
public class CorsConfigurationProperties {
    private boolean enabled;
    private String[] allowedOrigins;
    private String[] allowedMethods;
    private String[] allowedHeaders;
    private String[] exposedHeaders;
    private String maxAge;

    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    public String[] getAllowedOrigins() {
        return allowedOrigins;
    }
    public String[] getAllowedMethods() {
        return allowedMethods;
    }
    public String[] getAllowedHeaders() {
        return allowedHeaders;
    }
    public String[] getExposedHeaders() {
        return exposedHeaders;
    }
    public String getMaxAge() {
        return maxAge;
    }

    public void setAllowedOrigins(String[] allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public void setAllowedMethods(String[] allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    public void setAllowedHeaders(String[] allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
    }

    public void setExposedHeaders(String[] exposedHeaders) {
        this.exposedHeaders = exposedHeaders;
    }

    public void setMaxAge(String maxAge) {
        this.maxAge = maxAge;
    }
}