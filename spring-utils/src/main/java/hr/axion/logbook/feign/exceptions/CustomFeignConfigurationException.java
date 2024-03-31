package hr.axion.logbook.feign.exceptions;

public class CustomFeignConfigurationException extends RuntimeException {

    public CustomFeignConfigurationException(String message) {
        super(message);
    }
}
