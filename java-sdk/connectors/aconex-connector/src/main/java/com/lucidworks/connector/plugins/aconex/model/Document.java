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
    private String fileSize;
    private String fileType;
    private String filename;
    private String title;
}
