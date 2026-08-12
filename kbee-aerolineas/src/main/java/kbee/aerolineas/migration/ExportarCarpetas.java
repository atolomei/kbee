package kbee.aerolineas.migration;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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


public class ExportarCarpetas extends AsyncCommand {
	
	private static Logger logger = new Logger(LogManager.getLogger(ExportarCarpetas.class.getName()));
	
	
	private int i = 0;
    
	private String logsPath;
	private String bcvurl;
	private String bcvuser;
	private String bcvpassword;
	
	public ExportarCarpetas() {
		setName("Exportar Carpetas AA");
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
			
			String urlstring = bcvurl+"/webdav/aerolineas-btv/content/bcv/documents/folder/";
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
					String name = attributes.getNamedItem("href").getNodeValue();
					if (name.contains("folder") && !name.endsWith("folder/")) {
						readFolder(name);
					};
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
			
			
			NodeList folderrefs = doc.getElementsByTagName("a");
			if (folderrefs!=null) {
				for (int n=0; n<folderrefs.getLength(); n++) {
					org.w3c.dom.Node folderref = folderrefs.item(n);
					NamedNodeMap attributes = folderref.getAttributes();
					String ref = attributes.getNamedItem("href").getNodeValue();
					String ref2 = ref.endsWith("/") 
						? ref.substring(0, ref.length()-1)
						: ref;		
					String refname = ref2.substring(ref2.lastIndexOf("/")+1);
					if (ref.contains(href) && !ref.equals(href)) {
						if (refname.length()>4) {
							readFolderNotes(ref+"notes");
						}
						else {
							readFolder(ref);
						}
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
	
	public String readFolderNotes(String href) {
	    try {

	        String urlstring = bcvurl + href;
	        URL url = new URL(urlstring);
	        URLConnection connection = url.openConnection();
	        
	        String segments[] = href.split("/");
	        String id = segments[segments.length-2];

	        
			String base64Credentials = Base64.toString((bcvuser+":"+bcvpassword).getBytes());
			connection.setRequestProperty("Authorization", "Basic " + base64Credentials);

			String notes;
	        try (InputStream is = connection.getInputStream()) {

	            notes = new String(is.readAllBytes(), StandardCharsets.UTF_8);

	        }
	        notes = notes.replace(",", " ");
	        getLogger().info(","+id+","+notes);
	        return notes;
	    } catch (Exception e) {
	        getLogger().error(e);
	        return null;
	        //e.printStackTrace();
			//throw new ApiException(HttpStatus.PRECONDITION_FAILED, ApiError.INTERNAL_ERROR, e.getMessage());
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