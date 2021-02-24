package com.lucidworks.connector.plugins.aconex.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RegisterSearch {
    @XmlElement(name = "SearchResults")
    private final SearchResults searchResults;

    public RegisterSearch(SearchResults searchResults) {
        this.searchResults = searchResults;
    }

    private class SearchResults {
        @XmlElement(name = "Document")
        private final Document document;

        private SearchResults(Document document) {
            this.document = document;
        }
    }

    private class Document {
        private boolean Confidential;
        private String DocumentStatus;
        private String DocumentType;
        private String FileSize;
        private String FileType;
        private String Filename;
        private String Title;
    }
}
