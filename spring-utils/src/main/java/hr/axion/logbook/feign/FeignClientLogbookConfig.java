package hr.axion.logbook.feign;

import feign.Contract;
import feign.codec.Decoder;
import feign.codec.Encoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Base class for Feign client configuration with Logbook
 *
 * @param <T> type of Feign client
 */
@Slf4j
@Import(FeignClientsConfiguration.class)
public abstract class FeignClientLogbookConfig<T> {

    public abstract T provideFeignClient(String baseApiUrl,
                                         Encoder encoder,
                                         Decoder decoder,
                                         Contract contract);
}
