package com.lucidworks.connector.plugins.aconex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.lucidworks.connector.plugins.aconex.client.rest.RestApiUriBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.InputStream;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
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
    private Date dateModified;
    @JacksonXmlProperty(localName = "Discipline")
    private String discipline;
    @JacksonXmlProperty(localName = "TrackingId")
    private String trackingId;
    @JacksonXmlProperty(localName = "SelectList1")
    private String select1;
    @JacksonXmlProperty(localName = "SelectList2")
    private String select2;
    @JacksonXmlProperty(localName = "SelectList3")
    private String select3;
    @JacksonXmlProperty(localName = "SelectList4")
    private String select4;
    @JacksonXmlProperty(localName = "SelectList5")
    private String select5;
    @JacksonXmlProperty(localName = "SelectList6")
    private String select6;
    @JacksonXmlProperty(localName = "SelectList7")
    private String select7;
    @JacksonXmlProperty(localName = "SelectList8")
    private String select8;
    @JacksonXmlProperty(localName = "SelectList9")
    private String select9;

    private String url;
    private long lastUpdated;
    private InputStream content;

    public void setUrl(String host, String projectId) {
        this.url = RestApiUriBuilder.buildDocumentViewerUri(host, projectId, this.trackingId);
    }
}
