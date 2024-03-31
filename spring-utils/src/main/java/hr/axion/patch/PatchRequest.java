package hr.axion.patch;

import jakarta.validation.constraints.NotNull;

import java.beans.JavaBean;
import java.util.ArrayList;
import java.util.List;


@JavaBean
public class PatchRequest<T> implements PatchRequestInterface<T> {

    private @NotNull T data;

    private @NotNull List<String> fieldMask = new ArrayList<>();

    public T getData() {
        return this.data;
    }

    public List<String> getFieldMask() {
        return this.fieldMask;
    }

    public void setData(final T data) {
        this.data = data;
    }

    public void setFieldMask(final List<String> fieldMask) {
        this.fieldMask = fieldMask;
    }

    public String toString() {
        return "PatchRequest(data=" + this.getData() + ", fieldMask=" + this.getFieldMask() + ")";
    }

    public PatchRequest() {
    }

}
