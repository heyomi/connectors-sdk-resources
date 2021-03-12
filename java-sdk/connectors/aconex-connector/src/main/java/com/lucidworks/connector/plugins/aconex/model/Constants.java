package com.lucidworks.connector.plugins.aconex.model;

public class Constants {
    public static final String API = "api";
    public static final String PROJECTS = "projects";
    public static final String REGISTER = "register";
    public static final String MARKEDUP = "markedup";

    public static final String HTTP_HEADER_APPLICATION_KEY = "X-Application-Key";
    public static final String PARAM_SEARCH_TYPE = "search_type";
    public static final String PARAM_SEARCH_RESULT_SIZE = "search_result_size";
    public static final String PARAM_PAGE_SIZE = "page_size";
    public static final String PARAM_PAGE_NUMBER = "page_number";
    public static final String PARAM_RETURN_FIELDS = "return_fields";
    public static final String PARAM_SORT_FIELD = "sort_field";

    public static final int DEFAULT_PAGE_SIZE = 25;
    public static final int DEFAULT_PAGE_SIZE_DIVISOR = 25;
    public static final int DEFAULT_PAGE_NUMBER = 1;
    public static final String DEFAULT_SORT_FIELD = "fileSize";
    public static final String SEARCH_TYPE_PAGED = "PAGED";
    public static final String SEARCH_TYPE_NUMBERED = "PAGED";
    public static final String DEFAULT_RETURN_FIELDS = "title,filename,author,doctype,fileSize,fileType,confidential,statusid";
    public static final String RETURN_FIELDS = "approved,asBuiltRequired,attribute1,attribute2,attribute3,attribute4,author,authorisedBy,category,check1,check2,comments,comments2,confidential,contractDeliverable,contractnumber,contractordocumentnumber,contractorrev,current,date1,date2,discipline,docno,doctype,filename,fileSize,forreview,markupLastModifiedDate,milestonedate,numberOfMarkups,packagenumber,percentComplete,plannedsubmissiondate,printSize,projectField1,projectField2,projectField3,received,reference,registered,reviewed,reviewSource,reviewstatus,revision,revisiondate,scale,statusid,tagNumber,title,toclient,vdrcode,vendordocumentnumber,vendorrev";

    public static final int TIMEOUT_MS = 10000;
    public static final String IMAGE_FILE_TYPE = "jpg,jpeg,png,gif";
    public static final String DOC_FILE_TYPE = "pdf,doc,txt,xls";

    public static final String CHECKPOINT_PREFIX = "checkpoint_prefix";
    public static final String TOTAL_INDEXED = "total_indexed";
    public static final String TOTAL_PAGES = "total_pages";
    public static final String TOTAL_RESULTS = "total_results";
    public static final String TOTAL_ON_PAGE = "total_on_page";
    public static final String PAGE_NUMBER = "page_number";
    public static final String PAGE_SIZE = "page_size";
    public static final String COUNTER_FIELD = "number";
    public static final String TYPE_FIELD = "_aconex_type";
    public static final String PROJECT_ID_FIELD = "_aconex_project_id";
    public static final String PROJECT_NAME_FIELD = "_aconex_project_name";
    public static final String DOCUMENT_ID_FIELD = "_aconex_document_id";
}
