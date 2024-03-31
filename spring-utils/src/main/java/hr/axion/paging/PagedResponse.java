package hr.axion.paging;

import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;

import java.util.List;

@Data
public class PagedResponse<T> {

    private List<T> data;
    private Integer page;
    private Integer nextPage;
    private Integer pageSize;
    private Long totalCount;

    public PagedResponse() {
    }

    public PagedResponse(List<T> data, Slice<?> page) {
        this.data = data;
        this.pageSize = page.getSize();
        this.page = page.getNumber() + 1;
        this.nextPage = page.hasNext() ? this.page + 1: null;
        if(page instanceof Page<?>) {
            this.totalCount = ((Page<?>)page).getTotalElements();
        }
    }
}
