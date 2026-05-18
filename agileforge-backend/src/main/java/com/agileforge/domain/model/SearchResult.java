package com.agileforge.domain.model;

import java.util.List;

public class SearchResult<T> {

    private List<T> items;
    private long totalCount;
    private int page;
    private int pageSize;

    public SearchResult(List<T> items, long totalCount, int page, int pageSize) {
        this.items = items;
        this.totalCount = totalCount;
        this.page = page;
        this.pageSize = pageSize;
    }

    public int getTotalPages() {
        return (int) Math.ceil((double) totalCount / pageSize);
    }

    public List<T> getItems() { return items; }
    public void setItems(List<T> items) { this.items = items; }

    public long getTotalCount() { return totalCount; }
    public void setTotalCount(long totalCount) { this.totalCount = totalCount; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
}
