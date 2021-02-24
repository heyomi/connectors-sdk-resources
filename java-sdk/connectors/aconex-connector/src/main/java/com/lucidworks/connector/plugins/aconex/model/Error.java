package com.lucidworks.connector.plugins.aconex.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Error {
    private String code;
    private String description;
    private String id;
}
