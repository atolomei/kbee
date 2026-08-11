package com.novamens.kbee.idoc.webapi.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;

import kbee.api.model.IError;
import kbee.api.service.ApiError;
import kbee.api.service.ApiException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.springframework.http.HttpStatus;

@JsonInclude(Include.NON_NULL)

public class HttpRequest {
	

	private int chunk = 0;
    HttpURLConnection conn = null;
    String url;
    String credentials;
    String device;
    String apiToken;
    PrintWriter writer;
    BufferedReader reader;
    String LINE_FEED = "\r\n";
    ProgressListener listener;

    public HttpRequest(String url, String credentials, String device) {
        setUrl(url);
        setCredentials(credentials);
        setDevice(device);
        conn = null;
    }

    public HttpRequest(String url, String credentials, ProgressListener listener) {
        setUrl(url);
        setCredentials(credentials);
        setListener(listener);
        conn = null;
    }

    public ProgressListener getListener() {
        return listener;
    }

    public void setListener(ProgressListener listener) {
        this.listener = listener;
    }
 
    public int getChunk() {
		return chunk;
	}

	public void setChunk(int chunk) {
		this.chunk = chunk;
	}

	protected <T> T exchange(TypeReference<T> responseType) {
    	int responseCode = 0;
        try {
	        T response = read(responseType);
	        return response;
        }    
        catch (IOException e) {
            throw new ApiException(HttpStatus.valueOf(responseCode), ApiError.CLIENT_ERROR, e.getMessage());
        }
        finally {
       		close();
        }
    }

    protected <T> T exchange(HttpEntity requestEntity, TypeReference<T> responseType) {
    	int responseCode = 0;
        try {
            write(requestEntity);
            responseCode = getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return read(responseType);
            }
            else {
                throw getApiException();
            }
        }
        catch (IOException e) {
        	e.printStackTrace();
        	if (responseCode>0)
        		throw new ApiException(HttpStatus.valueOf(responseCode), ApiError.CLIENT_ERROR, e.getMessage());
        	else
                throw new RuntimeException(e);
        }
        catch (Exception e) {
        	e.printStackTrace();
        	throw e;
        }	
        finally {
       		close();
        }
    }

    protected void close() {
        conn = null;
    }

    public PrintWriter getWriter() throws IOException  {
        if (writer==null) {
            OutputStream outputStream = getConnection().getOutputStream();
            writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);
        }
        return writer;
    }

    public BufferedReader getReader() throws IOException {
        if (reader==null) {
            try {
                reader = new BufferedReader(new InputStreamReader(getConnection().getInputStream()));
            }
            catch (IOException e) {
                throw getApiException();
            }
            catch (Exception e) {
                throw new IOException(e.getMessage());
            }
            catch (Throwable e) {
                throw new IOException(e.getMessage());
            }
        }
        return reader;
    }

    public BufferedReader getErrorReader() throws IOException {
        BufferedReader reader = null;
        if (reader==null) {
            try {
                reader = new BufferedReader(new InputStreamReader(getConnection().getErrorStream()));
            }
            catch (IOException e) {
                throw new ApiException(HttpStatus.valueOf(getResponseCode()), ApiError.CLIENT_ERROR, e.getMessage());
            }
            catch (Exception e) {
                throw new IOException(e.getMessage());
            }
            catch (Throwable e) {
                throw new IOException(e.getMessage());
            }
        }
        return reader;
    }


    public int getResponseCode() throws IOException {
        return getConnection().getResponseCode();
    }

    public HttpURLConnection getConnection() throws IOException {
        if (conn == null) {
            setConnection(openConnection());
        }
        return conn;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }

    public URL getUrl() throws MalformedURLException {
        return new URL(this.url);
    }

    public String getCredentials() {
        return credentials;
    }

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public void setConnection(HttpURLConnection connection) {
        this.conn = connection;
    }

    protected void write(HttpEntity entity) throws IOException {
        InputStream inputStream = entity.getStream();
        OutputStream outputStream = getConnection().getOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead = -1;
        long bytesWrited = 0;
        long totalBytes = entity.getSize();
        int progress = 0;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
        	System.out.println("WRITE BODY "+progress);
            outputStream.write( buffer, 0, bytesRead);
            bytesWrited += bytesRead;
            progress = totalBytes>0 ? (int)((double)bytesWrited/(double)totalBytes * 100) : 0;
            if (getListener()!=null) getListener().onUpdate(progress);
        }
        outputStream.flush();
        inputStream.close();
    	System.out.println("WRITE BODY FLUSH");
    }

    protected <T> T read(com.fasterxml.jackson.core.type.TypeReference<T> responseType) throws IOException {
		RestObjectMapper restObjectMapper = new RestObjectMapper();
		restObjectMapper.setSerializationInclusion(Include.NON_NULL);
		T response = (T)restObjectMapper.readValue(getResponse(), responseType);
        return response;
    }

    protected String getResponse() throws  IOException {
        String response = null, line;
        while ((line = getReader().readLine()) != null) {
            response = response == null ? line : response + LINE_FEED + line;
        }
        return response;
    }

    protected ApiException getApiException() {
    	int responseCode = 0;
    	try {
	        responseCode = getResponseCode();
	        String errorCode = "", message = "";
	        if (responseCode!=401) {
	            IError apiError = getApiError();
	            if (apiError!=null) {
	            	errorCode = apiError.getCode();
	            	message = apiError.getMessage();
	            }
	        }
	        else {
	            errorCode = String.valueOf(ApiError.ACCESS_DENIED.getCode());
	            message = String.valueOf(ApiError.ACCESS_DENIED.getMessage());
	        }
	        return new ApiException(HttpStatus.valueOf(responseCode), errorCode, message);
    	}
    	catch (IOException e) {
            return new  ApiException(HttpStatus.valueOf(responseCode), ApiError.CLIENT_ERROR, e.getMessage());
    	}
    }
    
    protected IError getApiError() {
        try {
			RestObjectMapper restObjectMapper = new RestObjectMapper();
			restObjectMapper.setSerializationInclusion(Include.NON_NULL);
			IError applicationError = restObjectMapper.readValue(getErrorResponse(), IError.class);
            return applicationError;
        }
        catch (Exception e) {
            return null;
        }
    }

    protected String getErrorResponse() throws  IOException {
        String response = null, line;
        while ((line = getErrorReader().readLine()) != null) {
            response = response == null ? line : response + LINE_FEED + line;
        }
        return response;
    }

    protected String getRequestMethod() {
        return "GET";
    }

    protected String getContentType() {
        return "application/json";
    }

    protected boolean getDoOutput() {
        return false;
    }

    protected HttpURLConnection openConnection() throws IOException {
        HttpURLConnection conn = (HttpURLConnection) getUrl().openConnection();
        conn.setUseCaches(false);
        conn.setDoInput(true);
        conn.setDoOutput(getDoOutput());
        conn.setConnectTimeout(3000);
        conn.setRequestMethod(getRequestMethod());
        conn.setRequestProperty("Content-Type", getContentType());
        conn.setRequestProperty("User-Agent", "CodeJava Agent");
        //conn.setChunkedStreamingMode(1024 * 1024 * 10);
        conn.setChunkedStreamingMode(4096);
        if (getChunk()>0) {
        	conn.setChunkedStreamingMode(getChunk());
        }
        String base64Credentials = Base64.toString(getCredentials().getBytes());
        if (getApiToken()!=null) {
            conn.setRequestProperty("Authorization", "Bearer " + getApiToken());
        }
        else {
            conn.setRequestProperty("Authorization", "Basic " + base64Credentials);
        }
        return conn;
    }
}