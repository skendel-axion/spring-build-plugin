package hr.axion.paging;

import jakarta.validation.constraints.Min;
import lombok.Data;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
public class PagedRequest {

    @Min(value = 1, message = "Page must be greater then zero")
    private Integer page = 1;

    @Min(value = 1, message = "Page size must be greater than zero")
    private Integer pageSize = 10;

    private final static Sort.Direction defaultSort = Sort.Direction.ASC;

    public PagedRequest() {
    }

    public PagedRequest(Integer page, Integer pageSize) {
        this.page = page;
        this.pageSize = pageSize;
    }

    public Pageable toPageable() {
        final Sort sort = buildSort();
        return PageRequest.of(page - 1, pageSize, sort);
    }

    @SuppressWarnings("All")
    private Sort buildSort() {
        final Optional<Field> orderByField = FieldUtils.getFieldsListWithAnnotation(getClass(), OrderByField.class)
                .stream().findFirst();
        List<String> orderByList = null;
        if (orderByField.isPresent()) {
            try {
                final PropertyDescriptor pdInput = new PropertyDescriptor(orderByField.get().getName(), getClass());
                orderByList = (List<String>) pdInput.getReadMethod().invoke(this);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (IntrospectionException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        if (orderByList == null || orderByList.isEmpty()) {
            return Sort.unsorted();
        }
        // build sort order list
        final List<Sort.Order> sortOrderList = new ArrayList<>();
        orderByList.forEach(orderBy -> {
            final String[] split = orderBy.split(OrderByField.DELIMETER);
            final Sort.Direction direction = split.length == 1 ?
                    defaultSort : Sort.Direction.fromString(split[1]);
            sortOrderList.add(new Sort.Order(direction, split[0]));
        });
        return Sort.by(sortOrderList);
    }
}
