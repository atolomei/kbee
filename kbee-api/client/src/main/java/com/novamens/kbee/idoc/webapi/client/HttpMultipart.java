package com.novamens.kbee.idoc.webapi.client;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLConnection;

public class HttpMultipart extends HttpRequest {

    private String boundary = "===" + System.currentTimeMillis() + "===";

    public HttpMultipart(String url, String credentials, ProgressListener listener) {
        super(url, credentials, listener);
    }

    @Override
    protected void write(HttpEntity entity) throws IOException {
        writeStart(entity);
        super.write(entity);
        writeEnd(entity);
    }

    @Override
    protected String getRequestMethod() {
        return "POST";
    }

    @Override
    protected boolean getDoOutput() {
        return true;
    }

    @Override
    protected String getContentType() {
        return "multipart/form-data;boundary=" + boundary;
    }

    protected void writeStart(HttpEntity requestEntity) throws IOException {
    	
    	//
    	// System.out.println("WRITE START 1");
    	// System.out.println(getChunk());
    	//
    	
        File file = ((HttpFileEntity) requestEntity).getFile();
        String fieldName = "file";
        String fileName = file.getName();
        PrintWriter writer = getWriter();
        writer.append("--" + boundary).append(LINE_FEED);

        writer.append("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"");
        writer.append(LINE_FEED);
        writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(fileName));
        writer.append(LINE_FEED);
        writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
        writer.append(LINE_FEED);
        //writer.append("Content-Length:"+requestEntity.getSize()).append(LINE_FEED);
       // writer.append(LINE_FEED);
        writer.flush();
    	//System.out.println("WRITE START 2");
    }

    protected void writeEnd(HttpEntity requestEntity) throws IOException {
    	//System.out.println("WRITE END 1");
        PrintWriter writer = getWriter();
        writer.append(LINE_FEED);
        writer.flush();
        writer.append(LINE_FEED).flush();
        writer.append("--" + boundary + "--").append(LINE_FEED);
        writer.flush();
    	//System.out.println("WRITE END 2");
    }
}
