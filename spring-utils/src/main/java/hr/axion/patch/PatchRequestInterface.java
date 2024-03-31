package hr.axion.patch;


import java.util.List;

/**
 * PatchRequestInterface - Non-standard [name]Interface on purpose being used (!)
 * @param <T> object type being patched
 */
public interface PatchRequestInterface<T> {
    T getData();

    List<String> getFieldMask();
}
