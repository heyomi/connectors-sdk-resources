package com.lucidworks.connector.plugins.aconex.model;

public class Constants {
    public static final String PROJECTS = "projects";
    public static final String REGISTER = "register";
    public static final String MARKEDUP = "markedup";

    public static final String PARAM_SEARCH_TYPE = "search_type";
    public static final String PARAM_PAGE_SIZE = "page_size";
    public static final String PARAM_PAGE_NUMBER = "page_number";
    public static final String PARAM_RETURN_FIELDS = "return_fields";

    public static final int DEFAULT_PAGE_SIZE = 250;
    public static final String SEARCH_TYPE_PAGED = "PAGED";
    public static final String RETURN_FIELDS = "title,filename,author,doctype,fileSize,fileType,confidential,statusid";

    public static final int TIMEOUT_MS = 10000;
    public static final String IMAGE_FILE_TYPE = "jpg,jpeg,png,gif";
    public static final String DOC_FILE_TYPE = "pdf,doc,txt,xls";
}
