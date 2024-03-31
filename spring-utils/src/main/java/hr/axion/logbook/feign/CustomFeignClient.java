package hr.axion.logbook.feign;

import feign.Contract;
import feign.Feign;
import feign.RequestInterceptor;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import feign.okhttp.OkHttpClient;
import hr.axion.logbook.feign.exceptions.CustomFeignConfigurationException;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.zalando.logbook.*;
import org.zalando.logbook.core.DefaultSink;
import org.zalando.logbook.core.SplunkHttpLogFormatter;
import org.zalando.logbook.okhttp.GzipInterceptor;
import org.zalando.logbook.okhttp.LogbookInterceptor;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static hr.axion.logbook.CustomLogbookConditions.include;
import static org.zalando.logbook.core.Conditions.exclude;
import static org.zalando.logbook.core.HeaderFilters.replaceHeaders;

/**
 * Custom Feign client builder that allows to configure logbook and other feign properties.
 *
 * @param <T> type of the API
 */
@Slf4j
public class CustomFeignClient<T> {

    private final Class<T> apiType;
    private final String baseApiUrl;
    private final Encoder encoder;
    private final Decoder decoder;
    private final Contract contract;
    private final Optional<ErrorDecoder> errorDecoder;
    private final List<RequestInterceptor> requestInterceptors;
    private final List<String> headersToMask;
    private final List<QueryFilter> queryFilters;
    private final List<HeaderFilter> headerFilters;
    private final Optional<Boolean> avoidSslVerification;
    private final List<Predicate> excludeConditions;
    private final List<Predicate> includeConditions;
    private final CorrelationId correlationId;

    private CustomFeignClient(Class<T> apiType,
                              String baseApiUrl,
                              Encoder encoder,
                              Decoder decoder,
                              Contract contract,
                              Optional<ErrorDecoder> errorDecoder,
                              List<RequestInterceptor> requestInterceptors,
                              List<String> headersToMask,
                              List<QueryFilter> queryFilters,
                              List<HeaderFilter> headerFilters,
                              Optional<Boolean> avoidSslVerification,
                              List<Predicate> excludeConditions,
                              List<Predicate> includeConditions,
                              CorrelationId correlationId) {
        this.apiType = apiType;
        this.baseApiUrl = baseApiUrl;
        this.encoder = encoder;
        this.decoder = decoder;
        this.contract = contract;
        this.errorDecoder = errorDecoder;
        this.requestInterceptors = requestInterceptors;
        this.headersToMask = headersToMask;
        this.queryFilters = queryFilters;
        this.headerFilters = headerFilters;
        this.avoidSslVerification = avoidSslVerification;
        this.excludeConditions = excludeConditions;
        this.includeConditions = includeConditions;
        this.correlationId = correlationId;
    }

    public Class<T> getApiType() {
        return apiType;
    }

    public String getBaseApiUrl() {
        return baseApiUrl;
    }

    public Encoder getEncoder() {
        return encoder;
    }

    public Decoder getDecoder() {
        return decoder;
    }

    public Contract getContract() {
        return contract;
    }

    public Optional<ErrorDecoder> getErrorDecoder() {
        return errorDecoder;
    }

    public List<RequestInterceptor> getRequestInterceptors() {
        return requestInterceptors;
    }

    public List<String> getHeadersToMask() {
        return headersToMask;
    }

    public List<QueryFilter> getQueryFilters() {
        return queryFilters;
    }

    public List<HeaderFilter> getHeaderFilters() {
        return headerFilters;
    }

    public Optional<Boolean> getAvoidSslVerification() {
        return avoidSslVerification;
    }

    public List<Predicate> getExcludeConditions() {
        return excludeConditions;
    }

    public List<Predicate> getIncludeConditions() {
        return includeConditions;
    }

    public CorrelationId getCorrelationId() {
        return correlationId;
    }

    public static <T> Builder<T> builder(Class<T> apiType) {
        return new Builder<>(apiType);
    }

    public static class Builder<T> {

        private static final String DATA_MASK = "********";

        private final Class<T> apiType;
        private String baseApiUrl;
        private Encoder encoder;
        private Decoder decoder;
        private Contract contract;
        private Optional<ErrorDecoder> errorDecoder = Optional.empty();
        private List<RequestInterceptor> requestInterceptors = new ArrayList<>();
        private List<String> headersToMask = new ArrayList<>();
        private List<QueryFilter> queryFilters = new ArrayList<>();
        private List<HeaderFilter> headerFilters = new ArrayList<>();
        private Optional<Boolean> avoidSslVerification = Optional.empty();
        private List<Predicate> excludeConditions = new ArrayList<>();
        private List<Predicate> includeConditions = new ArrayList<>();
        private CorrelationId correlationId;

        public Builder(Class<T> apiType) {
            this.apiType = apiType;
        }

        public Builder<T> baseApiUrl(String baseApiUrl) {
            this.baseApiUrl = baseApiUrl;
            return this;
        }

        public Builder<T> encoder(Encoder encoder) {
            this.encoder = encoder;
            return this;
        }

        public Builder<T> decoder(Decoder decoder) {
            this.decoder = decoder;
            return this;
        }

        public Builder<T> contract(Contract contract) {
            this.contract = contract;
            return this;
        }

        public Builder<T> errorDecoder(ErrorDecoder errorDecoder) {
            this.errorDecoder = Optional.ofNullable(errorDecoder);
            return this;
        }

        public Builder<T> requestInterceptor(RequestInterceptor requestInterceptor) {
            this.requestInterceptors.add(requestInterceptor);
            return this;
        }

        public Builder<T> requestInterceptors(List<RequestInterceptor> requestInterceptors) {
            this.requestInterceptors.addAll(requestInterceptors);
            return this;
        }

        public Builder<T> addHeaderToMask(String header) {
            this.headersToMask.add(header);
            return this;
        }

        public Builder<T> addHeadersToMask(List<String> headers) {
            this.headersToMask.addAll(headers);
            return this;
        }

        public Builder<T> addQueryFilter(QueryFilter queryFilter) {
            this.queryFilters.add(queryFilter);
            return this;
        }

        public Builder<T> addQueryFilters(List<QueryFilter> queryFilters) {
            this.queryFilters.addAll(queryFilters);
            return this;
        }

        public Builder<T> addHeaderFilter(HeaderFilter headerFilter) {
            this.headerFilters.add(headerFilter);
            return this;
        }

        public Builder<T> addHeaderFilters(List<HeaderFilter> headerFilters) {
            this.headerFilters.addAll(headerFilters);
            return this;
        }

        public Builder<T> avoidSslVerification(boolean avoidSslVerification) {
            this.avoidSslVerification = Optional.of(avoidSslVerification);
            return this;
        }

        public Builder<T> addExcludeCondition(final Predicate excludeCondition) {
            this.excludeConditions.add(excludeCondition);
            return this;
        }

        public Builder<T> addExcludeConditions(final List<Predicate> excludeConditions) {
            this.excludeConditions.addAll(excludeConditions);
            return this;
        }

        public Builder<T> addIncludeCondition(final Predicate includeCondition) {
            this.includeConditions.add(includeCondition);
            return this;
        }

        public Builder<T> addIncludeConditions(final List<Predicate> includeConditions) {
            this.includeConditions.addAll(includeConditions);
            return this;
        }

        public Builder<T> correlationId(CorrelationId correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public T build() {
            CustomFeignClient<T> config = new CustomFeignClient(
                    apiType,
                    baseApiUrl,
                    encoder,
                    decoder,
                    contract,
                    errorDecoder,
                    requestInterceptors,
                    headersToMask,
                    queryFilters,
                    headerFilters,
                    avoidSslVerification,
                    excludeConditions,
                    includeConditions,
                    correlationId);
            return createFeignClient(config);
        }

        /**
         * Creates a Feign client with the given configuration.
         *
         * @param customFeignConfig the configuration to use
         * @return the Feign client
         */
        private T createFeignClient(CustomFeignClient<T> customFeignConfig) {
            HttpLogWriter logWriter = new HttpLogWriter() {
                @Override
                public void write(@NotNull Precorrelation precorrelation, @NotNull String request) {
                    log.info(request);
                }

                @Override
                public void write(@NotNull Correlation correlation, @NotNull String response) {
                    log.info(response);
                }
            };

            LogbookCreator.Builder logbookCreatorBuilder = Logbook.builder()
                    .sink(new DefaultSink(new SplunkHttpLogFormatter(), logWriter));

            customFeignConfig.getHeaderFilters().addAll(customFeignConfig.getHeadersToMask().stream()
                    .map(header -> replaceHeaders(header::equalsIgnoreCase, DATA_MASK))
                    .collect(Collectors.toList()));

            logbookCreatorBuilder.queryFilters(customFeignConfig.getQueryFilters());
            logbookCreatorBuilder.headerFilters(customFeignConfig.getHeaderFilters());

            // check conditions
            if (conditionNotEmpty(customFeignConfig.getExcludeConditions()) && conditionNotEmpty(customFeignConfig.getIncludeConditions())) {
                throw new CustomFeignConfigurationException("Both include and exclude conditions are defined. Please specify only one.");
            }

            if (conditionNotEmpty(customFeignConfig.getExcludeConditions())) {
                logbookCreatorBuilder.condition(exclude(customFeignConfig.getExcludeConditions().toArray(Predicate[]::new)));
            }

            if (conditionNotEmpty(customFeignConfig.getIncludeConditions())) {
                logbookCreatorBuilder.condition(include(customFeignConfig.getIncludeConditions().toArray(Predicate[]::new)));
            }

            okhttp3.OkHttpClient.Builder okHttpClientBuilder = new okhttp3.OkHttpClient.Builder()
                    .retryOnConnectionFailure(false)
                    .addNetworkInterceptor(new LogbookInterceptor(logbookCreatorBuilder.build()))
                    .addNetworkInterceptor(new GzipInterceptor());

            customFeignConfig.getAvoidSslVerification().ifPresent(avoidSslVerification -> {
                try {
                    SSLContext trustAllSslContext = createTrustAllSslContext();
                    okHttpClientBuilder.sslSocketFactory(trustAllSslContext.getSocketFactory(), (X509TrustManager) getTrustManagers()[0]);
                    okHttpClientBuilder.hostnameVerifier((hostname, session) -> true);
                } catch (NoSuchAlgorithmException | KeyManagementException e) {
                    log.error("Error creating SSL context", e);
                }
            });

            Feign.Builder builder = Feign.builder();
            builder
                    .client(new OkHttpClient(okHttpClientBuilder.build()))
                    .encoder(customFeignConfig.getEncoder())
                    .decoder(customFeignConfig.getDecoder())
                    .contract(customFeignConfig.getContract())
                    .requestInterceptors(requestInterceptors);

            customFeignConfig.getErrorDecoder().ifPresent(errorDecoder -> builder.errorDecoder(errorDecoder));

            return (T) builder
                    .target(customFeignConfig.getApiType(), customFeignConfig.getBaseApiUrl());
        }

        private SSLContext createTrustAllSslContext() throws NoSuchAlgorithmException, KeyManagementException {
            // avoid SSL checks for Client API
            TrustManager[] trustAllCerts = getTrustManagers();
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            return sslContext;
        }

        @org.jetbrains.annotations.NotNull
        private TrustManager[] getTrustManagers() {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };
            return trustAllCerts;
        }

        private boolean conditionNotEmpty(Collection<?> collection) {
            return collection != null && !collection.isEmpty();
        }
    }
}
