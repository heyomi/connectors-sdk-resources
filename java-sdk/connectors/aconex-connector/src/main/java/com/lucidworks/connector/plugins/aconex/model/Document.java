package com.lucidworks.connector.plugins.aconex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
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
    private boolean confidential;
    private String documentStatus;
    private String documentType;
    @JacksonXmlProperty(localName = "FileSize")
    private String fileSize;
    @JacksonXmlProperty(localName = "FileType")
    private String fileType;
    @JacksonXmlProperty(localName = "FileName")
    private String filename;
    @JacksonXmlProperty(localName = "Title")
    private String title;
}
