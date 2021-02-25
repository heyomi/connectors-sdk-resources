package com.lucidworks.connector.plugins.aconex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class RegisterSearch {
    @JacksonXmlProperty(localName = "SearchResults")
    private SearchResults searchResults;
    @JacksonXmlProperty(localName = "TotalResults")
    private int totalResult;
    @JacksonXmlProperty(localName = "TotalResultsOnPage")
    private int totalResultsOnPage;
    @JacksonXmlProperty(localName = "TotalPages")
    private int totalPages;
    @JacksonXmlProperty(localName = "PageSize")
    private int pageSize;
    @JacksonXmlProperty(localName = "CurrentPage")
    private int currentPage;

}
