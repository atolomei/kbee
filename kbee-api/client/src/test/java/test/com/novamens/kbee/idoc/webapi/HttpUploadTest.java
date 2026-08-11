package test.com.novamens.kbee.idoc.webapi;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.novamens.kbee.idoc.webapi.client.RestObjectMapper;

import kbee.api.model.ITransaction;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.Test;

@SuppressWarnings("deprecation")
public class HttpUploadTest {
	@Test
	public void test() throws IOException {
		

		String server =  "https://test.kbee.io/api";

		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			
			/**
			 * BINARY POST URL
			 * --------------- 
			 */
	
				
			String url = server+"/binfile/upload";
			String url2= "http://localhost:9234/object/upload/dev-test/load-balance-with-eureka-master(1)-2736?fileName=load-balance-with-eureka-master(1).zip";
			HttpPost binaryHttpPost = new HttpPost(url2);
				
			// local file path
			FileBody file = new FileBody(new File("D:\\temp\\odilon1\\v0\\load-balance-with-eureka-master(1).zip"));
	
			// credentials for server domain user
			binaryHttpPost.setHeader("Authorization", "Basic " + getCredentials());
				
			HttpEntity entity = MultipartEntityBuilder.create()
					.addPart("file", file)
					.build();
	
			binaryHttpPost.setEntity(entity);
			// System.out.println("executing request " + binaryHttpPost.getRequestLine());
				
			CloseableHttpResponse binaryPostResponse = httpclient.execute(binaryHttpPost);
				
			// System.out.println("Response Code : " 
			//		+ binaryPostResponse.getStatusLine().getStatusCode());
				
			BufferedReader responseReader = new BufferedReader(
					new InputStreamReader(binaryPostResponse.getEntity().getContent()));
	
			StringBuffer binaryPostResponseText = new StringBuffer();
			String line = "";
			while ((line = responseReader.readLine()) != null) {
				binaryPostResponseText.append(line);
			}
					
			/** 
			 * 
			 * Binary post response is a json with
			 * {id:"transaction id", target:{href:"rppd url of the binary file uploaded"}}";
			 *  	
			 */
			// System.out.println(binaryPostResponseText.toString());

			
			/**
			 * METADATA POST URL 
			 * -----------------
			 * 
			 */
	
			final RestObjectMapper restObjectMapper = new RestObjectMapper();
			restObjectMapper.setSerializationInclusion(Include.NON_NULL);
				
			final ITransaction iTransaction = restObjectMapper.readValue(binaryPostResponseText.toString(), ITransaction.class);
			String resourceurl=iTransaction.getTarget().getHRef();
				
				
			String metainfo = "{"+
					"\"domain\":\"indraiml\"," +
					"\"seededAttributes\":["+
						"{\"attribute\":{\"name\":\"type\"},\"values\":[{\"name\":\"Documento\"}]},"+
						"{\"attribute\":{\"name\":\"referencedate\"},\"values\":[{\"name\":\"2022-04-12\"}]},"+
						"{\"attribute\":{\"name\":\"status\"},\"values\":[{\"name\":\"Final\"}]}],"+
					"\"title\":\"TEST2\","+
					"\"resources\":[{\"href\":\"resourceurl\"}],"+
					"\"className\":\"file\"}";
		
			metainfo = metainfo.replace("resourceurl", resourceurl);
				
			/**
			 *  
			 * METAINFO POST URL 
			 * http://server/api/file/[application]/[domain]/[file id]
			 */
				
			HttpPost metainfoHttpPost = new HttpPost(server+"/file/new");  // 
				
			metainfoHttpPost.setHeader("Authorization", "Basic " + getCredentials());
			metainfoHttpPost.setHeader("Content-type", "application/json");
				
			StringEntity metainfoEntity = new StringEntity(metainfo);
			metainfoHttpPost.setEntity(metainfoEntity);
				
			// System.out.println("executing request " + metainfoHttpPost.getRequestLine());
			
	        DefaultHttpClient httpsclient = httpClientTrustingAllSSLCerts();
			
			CloseableHttpResponse metainfoPostResponse = httpsclient.execute(metainfoHttpPost);
				
			// System.out.println("Response Code : " 
			//		+ metainfoPostResponse.getStatusLine().getStatusCode());
			// System.out.println("Response Code : " 
			//			+ metainfoPostResponse.getStatusLine().getReasonPhrase());
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			httpclient.close();
		}
	}
	
	private String getCredentials(){
		String plainCredentials="odilon:odilon";
		String base64Credentials = new String(Base64.encodeBase64(plainCredentials.getBytes()));
		return base64Credentials;
	}
	
	private DefaultHttpClient httpClientTrustingAllSSLCerts() throws NoSuchAlgorithmException, KeyManagementException {
		 DefaultHttpClient httpclient = new DefaultHttpClient();

		 SSLContext sc = SSLContext.getInstance("SSL");
		 sc.init(null, getTrustingManager(), new java.security.SecureRandom());

		 org.apache.http.conn.ssl.SSLSocketFactory socketFactory = new org.apache.http.conn.ssl.SSLSocketFactory(sc);
	        
	        
		 Scheme sch = new Scheme("https", 443, socketFactory);
		 httpclient.getConnectionManager().getSchemeRegistry().register(sch);
		 return httpclient;
	 }

	 private TrustManager[] getTrustingManager() {
		 TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			 @Override
			 public X509Certificate[] getAcceptedIssuers() {
				return null;
			}
			@Override
			public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
			}
			@Override
			public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
			}
		}};
		return trustAllCerts;
	 }
}
