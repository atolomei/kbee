package kbee.web.util;

import javax.servlet.http.HttpServletRequest;

public class MultipartUtil {
    public  static final String MULTIPART_FORM_DATA = "multipart/form-data";
    public  static final String CONTENT_TYPE        = "Content-Type";

    public static boolean isMultipartFormData(HttpServletRequest req) {
        String contentType        = null;
        String headerContentType  = req.getHeader(CONTENT_TYPE);
        String requestContentType = req.getContentType();

        if(headerContentType == null && requestContentType != null) {
            contentType = requestContentType;
        } else if(requestContentType == null && headerContentType != null) {
            contentType = headerContentType;
        } else if(headerContentType != null && requestContentType != null) {
            contentType = headerContentType.length() <= requestContentType.length() ? requestContentType : headerContentType;
        }

        return contentType != null && contentType.toLowerCase().startsWith(MULTIPART_FORM_DATA);
    }
}
