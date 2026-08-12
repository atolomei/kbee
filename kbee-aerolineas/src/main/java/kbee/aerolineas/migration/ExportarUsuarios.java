package kbee.aerolineas.migration;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.logging.log4j.LogManager;
import org.springframework.http.HttpStatus;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.novamens.kbee.content.command.AsyncCommand;

import com.novamens.kbee.idoc.webapi.client.Base64;

import kbee.api.service.ApiError;
import kbee.api.service.ApiException;
import kbee.util.PropertiesFactory;
import kbee.util.logging.Logger;

import org.ccil.cowan.tagsoup.Parser;

import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;


public class ExportarUsuarios extends AsyncCommand {
	
	private static Logger logger = new Logger(LogManager.getLogger(ExportarUsuarios.class.getName()));
	
	
	private int i = 0;
    
	private String logsPath;
	private String bcvurl;
	private String bcvuser;
	private String bcvpassword;
	
	public ExportarUsuarios() {
		setName("Exportar Usuarios AA");
	}

	public void executeAsync() {
		
		try {
	 	 	bcvurl = PropertiesFactory.getInstance("kbee").getProperties().getProperty("aerolineas.bcv.url", "http://localhost:8116").trim();
	 	 	bcvuser = PropertiesFactory.getInstance("kbee").getProperties().getProperty("aerolineas.bcv.user", "root").trim();
	 	 	bcvpassword = PropertiesFactory.getInstance("kbee").getProperties().getProperty("aerolineas.bcv.password", "..resurrecto$").trim();
	 		logsPath = PropertiesFactory.getInstance("kbee").getProperties().getProperty("aerolineas.logs", "logs").trim();
			String lote = (String)getParameter("lote");
	 		logsPath = PropertiesFactory.getInstance("kbee").getProperties().getProperty("aerolineas.logs", "logs").trim();
			if (lote==null) {
				setResultComments("el parámetro lote es obligatorio");
				throw new RuntimeException("sin lote");
			}
			
			setLogger(getLoggerName(lote));
			
			read();

			
  			end();
		}
		catch (Exception e) {
			logger.error(e);
			stop();
		}
	}
	
	public boolean read() {
		try {
			
			String urlstring = bcvurl+"/webdav/aerolineas-btv/content/entities/persons/";
			URL url = new URL(urlstring);
			URLConnection connection = url.openConnection();
			
			String base64Credentials = Base64.toString((bcvuser+":"+bcvpassword).getBytes());
			connection.setRequestProperty("Authorization", "Basic " + base64Credentials);


			InputStream is = connection.getInputStream();

			Parser tagsoupParser = new Parser();
			SAXSource source = new SAXSource(tagsoupParser, new InputSource(is));

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();

			Document doc = builder.newDocument();

			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.transform(source, new DOMResult(doc));
			
			
			NodeList foldernodes = doc.getElementsByTagName("a");
			if (foldernodes!=null) {
				for (int n=0; n<foldernodes.getLength(); n++) {
					org.w3c.dom.Node folder = foldernodes.item(n);
					NamedNodeMap attributes = folder.getAttributes();
					String name =attributes.getNamedItem("href").getNodeValue();
					if (name.contains("persons") && !name.endsWith("persons/")) {
						readFolder(name);
					}
				}
			}
			
			
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ApiException(HttpStatus.PRECONDITION_FAILED, ApiError.INTERNAL_ERROR, e.getMessage());
		}
		return true;
	}
	
	public boolean readFolder(String href) {
		try {
			
			String urlstring = bcvurl+href;
			URL url = new URL(urlstring);
			URLConnection connection = url.openConnection();
			
			String base64Credentials = Base64.toString((bcvuser+":"+bcvpassword).getBytes());
			connection.setRequestProperty("Authorization", "Basic " + base64Credentials);
			

			InputStream is = connection.getInputStream();

			Parser tagsoupParser = new Parser();
			SAXSource source = new SAXSource(tagsoupParser, new InputSource(is));

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();

			Document doc = builder.newDocument();

			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.transform(source, new DOMResult(doc));
			
			
			NodeList personrefs = doc.getElementsByTagName("a");
			if (personrefs!=null) {
				for (int n=0; n<personrefs.getLength(); n++) {
					org.w3c.dom.Node personref = personrefs.item(n);
					NamedNodeMap attributes = personref.getAttributes();
					String ref = attributes.getNamedItem("href").getNodeValue();
					if (ref.contains(href) && !ref.equals(href)) {
						String dossier = readEmployee(ref+"employee");
						readPerson(dossier, ref);
					}
				}
			}
			
			
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ApiException(HttpStatus.PRECONDITION_FAILED, ApiError.INTERNAL_ERROR, e.getMessage());
		}
	return true;
	}
	
	public String readEmployee(String href) {
		try {
			
			String urlstring = bcvurl+href;
			URL url = new URL(urlstring);
			URLConnection connection = url.openConnection();
			
			String base64Credentials = Base64.toString((bcvuser+":"+bcvpassword).getBytes());
			connection.setRequestProperty("Authorization", "Basic " + base64Credentials);
			

			InputStream is = connection.getInputStream();

			Parser tagsoupParser = new Parser();
			SAXSource source = new SAXSource(tagsoupParser, new InputSource(is));

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();

			Document doc = builder.newDocument();

			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.transform(source, new DOMResult(doc));
			
			
			NodeList nodes = doc.getElementsByTagName("Dossier");
			if (nodes.getLength()>0) {
				org.w3c.dom.Node node = nodes.item(0);
				String dossier = node.getTextContent();
				return dossier;
			}
			
			return null;
			
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ApiException(HttpStatus.PRECONDITION_FAILED, ApiError.INTERNAL_ERROR, e.getMessage());
		}
	}	
	
	public void readPerson(String dossier, String href) {
		try {
		   String url = bcvurl+href;
		   

	        String body = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"+
	           "<D:propfind xmlns:D=\"DAV:\">"+
	                "<D:allprop/>"+
	            "</D:propfind>";
	           

	        HttpClient client = HttpClient.newBuilder()
	                .authenticator(new java.net.Authenticator() {
	                    @Override
	                    protected java.net.PasswordAuthentication getPasswordAuthentication() {
	                        return new java.net.PasswordAuthentication(
	                                bcvuser, bcvpassword.toCharArray());
	                    }
	                })
	                .build();

	        HttpRequest request = HttpRequest.newBuilder()
	                .uri(URI.create(url))
	                .method("PROPFIND", HttpRequest.BodyPublishers.ofString(body))
	                .header("Depth", "1")
	                .header("Content-Type", "application/xml")
	                .build();

	        HttpResponse<String> response =
	                client.send(request, HttpResponse.BodyHandlers.ofString());

	        String bodyvalue = response.body();
	        
	        String author = null;
	        int a0 = bodyvalue.indexOf("<K:author><K:href>");
	        if (a0>0) {
		        int a1 = bodyvalue.indexOf("</K:href></K:author>",a0);
		        if (a1>0) {
		        	author = bodyvalue.substring(a0+18,a1);
		        	int s = author.lastIndexOf("/");
		        	author = author.substring(s+1);
		        }
	        }
	        
	        if (dossier==null) {
	        	System.out.print("null");
	        }
	        
	        String lastModified = null;
	        int l0 = bodyvalue.indexOf("<K:getlastmodified>");
	        if (l0>0) {
		        int l1 = bodyvalue.indexOf("</K:getlastmodified>",l0);
		        if (l1>0) {
		        	lastModified = bodyvalue.substring(l0+19,l1);
		            ZonedDateTime date = ZonedDateTime.parse(
		                    lastModified,
		                    DateTimeFormatter.RFC_1123_DATE_TIME
		            );
		            lastModified = date.toString();
		        }
	        }
	        
	        getLogger().info(","+dossier+ ","+author+","+lastModified);
        
	        
	        System.out.println(bodyvalue);
		}
		catch (Exception e) {
			
		}
	}

	
	@Override
	public double getProgress() {
		return (double) i/(double) getTotalItems() * 100;
	}
	
	
	@Override
	public long getTotalItems() {
		return 0;
	}
	
	@Override
	public long getTotalItemsProcessed() {
		return i;
	}
	
	
	private String getLoggerName(String lote) {
		String name = logsPath +"/importacion-" + lote.toLowerCase() + "-";
		DateFormat format = new SimpleDateFormat("MM-dd-yyyy");
		name += format.format(new Date());
		name += "-" + String.valueOf(getId()) + ".log";
		return name;
	}
	

}