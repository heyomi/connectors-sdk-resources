package com.lucidworks.connector.plugins.aconex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.lucidworks.connector.plugins.aconex.client.rest.RestApiUriBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Document {
    @JacksonXmlProperty(localName = "DocumentId")
    private String id;
    @JacksonXmlProperty(localName = "Title")
    private String title;
    @JacksonXmlProperty(localName = "Confidental")
    private boolean confidential;
    @JacksonXmlProperty(localName = "DocumentStatus")
    private String documentStatus;
    @JacksonXmlProperty(localName = "DocumentType")
    private String documentType;
    @JacksonXmlProperty(localName = "FileSize")
    private int fileSize;
    @JacksonXmlProperty(localName = "FileType")
    private String fileType;
    @JacksonXmlProperty(localName = "Filename")
    private String filename;
    @JacksonXmlProperty(localName = "Category")
    private String category;
    @JacksonXmlProperty(localName = "DateModified")
    private String dateModified;
    @JacksonXmlProperty(localName = "Discipline")
    private String discipline;
    @JacksonXmlProperty(localName = "SelectListList1")
    private String select1;
    @JacksonXmlProperty(localName = "SelectListList2")
    private String select2;
    @JacksonXmlProperty(localName = "SelectListList3")
    private String select3;
    @JacksonXmlProperty(localName = "SelectListList4")
    private String select4;
    @JacksonXmlProperty(localName = "SelectListList5")
    private String select5;
    @JacksonXmlProperty(localName = "SelectListList6")
    private String select6;
    @JacksonXmlProperty(localName = "SelectListList7")
    private String select7;
    @JacksonXmlProperty(localName = "SelectListList8")
    private String select8;
    @JacksonXmlProperty(localName = "SelectListList9")
    private String select9;

    private String url;

    public void setUrl(String projectId) {
        this.url = RestApiUriBuilder.buildDocumentViewerUri(projectId, id);
    }
}
