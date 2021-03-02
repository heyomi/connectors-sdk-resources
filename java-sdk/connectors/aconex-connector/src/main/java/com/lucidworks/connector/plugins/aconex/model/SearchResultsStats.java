package com.lucidworks.connector.plugins.aconex.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SearchResultsStats {
    private int totalResults;
    private int totalResultsOnPage;
    private int totalPages;
    private int pageSize;
    private int currentPage;

    public SearchResultsStats(RegisterSearch registerSearch) {
        this.totalResults = registerSearch.getTotalResults();
        this.totalResultsOnPage = registerSearch.getTotalResultsOnPage();
        this.totalPages = registerSearch.getTotalPages();
        this.pageSize = registerSearch.getPageSize();
        this.currentPage = registerSearch.getCurrentPage();
    }
}
