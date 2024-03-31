package hr.axion.logbook;

import org.zalando.logbook.HttpMessage;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;

/**
 * Custom Logbook conditions that can be used to exclude/include logging of specific requests
 */
public final class CustomLogbookConditions {
    private CustomLogbookConditions() {
    }

    @SafeVarargs
    public static <T extends HttpMessage> Predicate<T> include(final Predicate<T>... predicates) {
        return include((Collection) Arrays.asList(predicates));
    }

    public static <T extends HttpMessage> Predicate<T> include(final Collection<Predicate<T>> predicates) {
        return predicates.stream().reduce(Predicate::or).orElse(($) -> true);
    }
}