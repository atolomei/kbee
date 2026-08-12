package kbee.aerolineas.migration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.ccil.cowan.tagsoup.Parser;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.novamens.kbee.idoc.webapi.client.Base64;

import kbee.api.model.ApiFile;
import kbee.api.model.ApiProxy;
import kbee.api.model.ApiUser;
import kbee.util.logging.Logger;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BcvService {
	
	private String url;
	private String user;
	private String password;
	private Logger logger;
	
	public List<ApiFile> getHistory(String uri) throws IOException {
		List<ApiFile> history = new ArrayList<>();
		
		ApiFile file = getFile(uri);
		history.add(file);
		while (file.getCustomAttributeValue("previous")!=null) {
			file = getFile(file.getCustomAttributeValue("previous"));
			history.add(file);
		}
		
		int v = history.size();
		for (ApiFile version : history) {
			version.setVersion(v);
			version.setExternalId(lastSegment(uri));
			v--;
		}
		
		return history;
	}
	
	public ApiFile getFile(String uri) throws IOException {
		InputStream is = null;
		
		ApiFile file = new ApiFile();
		
		try {

			file.setExternalId(lastSegment(uri));
			
			String fileuri = uri + "/kbee-content";
			
			is = getStream(fileuri);

			Document doc = getDocument(is);
			file.setControlAttribute("uri", uri);
			file.setControlAttribute("fixed", "true");
			file.setTitle(getValue(doc.getElementsByTagNameNS("http://www.novamens.com/kbee", "Title")));
			file.setClassName(getType(uri));
			file.setApplication("bcv");
			file.setAttribute("descripcion", getDescription(uri));
			file.setAttribute("tipodocumento", getType(uri));
			setAttribute(doc, file, "Revision", "Número Revisión");
			
			setDateAttribute(doc, file, "RevisionDate", "Fecha Revisión");
			setDateAttribute(doc, file, "ValidFrom", "Visualización Inicial");
			setDateAttribute(doc, file, "ValidTo", "Visualización Final");
			
			setAttribute(doc, file, "DocumentPartNumber", "Código");
			
			for (String part : getValues(doc.getElementsByTagName("PartNumber"))) {
				file.setAttribute("Parte", part);
			}
			
			file.setCustomAttribute("carpetas", getFolders(doc));
			
			List<ApiProxy> relations = new ArrayList<>();
			
			String documenturl = getUrl(doc, uri, "Document",  "href");
			if (documenturl!=null) {
				relations.add(new ApiProxy(lastSegment(documenturl), null, null, "Documento"));
			}
			file.setRelationships(relations);
			
			NodeList related = doc.getElementsByTagName("Related");
			for (int c=0; c<related.getLength(); c++) {
				org.w3c.dom.Node node = related.item(c);
				if (node instanceof Element && !"".equals(node.getNodeName())) {
					Attr attribute = ((Element)node).getAttributeNode("href");
					relations.add(new ApiProxy(lastSegment(attribute.getTextContent()), null, null, "Relacionado"));
				}
			}
			
			file.setRelationships(relations);
			
			String resourcesuri = getUrl(doc, fileuri, "Files", "href");
			file.setCustomAttribute("files", resourcesuri);
			
			List<String> indexs = getIndexs(resourcesuri);
			String indexsvalue = "";
			for (String index : indexs) {
				if (!"".equals(indexsvalue))
					indexsvalue += "|";
				indexsvalue += index;
			}
			file.setCustomAttribute("index", indexsvalue);
			
			file.setCustomAttribute("previous", getUrl(doc, fileuri, "PreviousVersion", "href"));
			
			
			Map<String, Object> properties = getProperties(uri);

			ApiUser user = getUser((String)properties.get("author"));
			file.setLastModifiedUser(new ApiProxy(user.getDisplayName(),null));
			file.setLastModifiedDate((OffsetDateTime)properties.get("lastmodified"));
			file.setControlAttribute("creation", (String)properties.get("creation"));


			return file;
			
		}
		catch (Exception e) {
			getLogger().info(file.getExternalId() + ", ERROR, "+e.getMessage());
			throw new IOException(e);
		}
		finally {
			close(is);
		}
	}
	
	public ApiUser getUser(String uri) throws IOException {
		InputStream is = null;
		ApiUser user = new ApiUser();
		try {
			is = getStream(uri);
			Document doc = getDocument(is);
			user.setDisplayName(getValue(doc.getElementsByTagName("display-name")));
			return user;
		}	
		catch (Exception e) {
			throw new IOException(e);
		}
		finally {
			close(is);
		}
	}
	
	private String getType(String uri) {
		if (uri.contains("documents/document") || uri.contains("versions/document"))
			return "Documento Corporativo";
		if (uri.contains("documents/shipment") || uri.contains("versions/shipment"))
			return "Documento Corporativo";
		if (uri.contains("documents/manual") || uri.contains("versions/manual"))
			return "Documento Técnico";
		if (uri.contains("documents/iap") || uri.contains("versions/iap"))
			return "IAP";
		return null;
	}
	
	
	private String getDescription(String uri) {
		InputStream is = null;
		try {

			is = getStream(uri+"/description");

			Document doc = getDocument(is, "ISO-8859-1");

		    NodeList html = doc.getElementsByTagName("html");
		    NodeList bodies = doc.getElementsByTagNameNS("*", "body");

			String text = "";
			
			if (html.getLength() > 0) {
				bodies = ((Element)html.item(0)).getElementsByTagName("body");
			}

			if (bodies.getLength() > 0) {
			    text = bodies.item(0).getTextContent();
			    text = new String(
			            text.getBytes(StandardCharsets.ISO_8859_1),
			            StandardCharsets.UTF_8
			        );
			}
			
			text = text.replace("|n", "</br>");

			
			return text;
			
		}
		catch (Exception e) {
			getLogger().info(lastSegment(uri) + ", ERROR, "+e.getMessage());
			e.printStackTrace();
			return null;
		}
		finally {
			close(is);
		}
	}
	
	private String getFolders(Document doc) throws Exception {
			String folders = "";

			XPath xpath = XPathFactory.newInstance().newXPath();

			NodeList targets = (NodeList) xpath.evaluate(
			    "//*[local-name()='Classifier']" +
			    "[*[local-name()='Type' and text()='folder']]" +
			    "/*[local-name()='Target']/@href",
			    doc,
			    XPathConstants.NODESET
			);

			for (int i = 0; i < targets.getLength(); i++) {
				String href = targets.item(i).getNodeValue();
				if (!"".equals(folders)) folders+=",";
				folders+= "kb:"+lastSegment(href);
			}
			
			return folders;
			
	}
	
	private List<String> getIndexs(String uri) {
		InputStream is = null;
		try {
			List<String> values = new ArrayList<>();

			is = getStream(uri);

			Document doc = getDocument(is);
			
			XPath xpath = XPathFactory.newInstance().newXPath();

			NodeList hrefs = (NodeList) xpath.evaluate(
			    "//*[local-name()='Index']/*[local-name()='File']/@href",
			    doc,
			    XPathConstants.NODESET
			);

			for (int i = 0; i < hrefs.getLength(); i++) {
				String index = hrefs.item(i).getNodeValue();
				
				URI relative = new URI(null, null, index, null);
				String url = URI.create(uri+"/")
		                .resolve(relative)
		                .toString();
				
			    values.add(url);
			}
			
			return values;
			
		}
		catch (Exception e) {
			getLogger().info(lastSegment(uri) + ", ERROR, "+e.getMessage());
			e.printStackTrace();
			return null;
		}
		finally {
			close(is);
		}
	}
	
	
	public Map<String, Object> getProperties(String href) throws IOException {
		Map<String, Object> properties = new HashMap<String, Object>();
		try {
		   //href = hrefstring.substring(6, hrefstring.length()-1);
			String uri = href.replace("kbee:", "/webdav");
			String urlstring = getUrl()+uri;
	
	        String body = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"+
	           "<D:propfind xmlns:D=\"DAV:\">"+
	                "<D:allprop/>"+
	            "</D:propfind>";
	           
	
	        HttpClient client = HttpClient.newBuilder()
	                .authenticator(new java.net.Authenticator() {
	                    @Override
	                    protected java.net.PasswordAuthentication getPasswordAuthentication() {
	                        return new java.net.PasswordAuthentication(
	                                user, password.toCharArray());
	                    }
	                })
	                .build();
	
	        HttpRequest request = HttpRequest.newBuilder()
	                .uri(URI.create(urlstring))
	                .method("PROPFIND", HttpRequest.BodyPublishers.ofString(body))
	                .header("Depth", "1")
	                .header("Content-Type", "application/xml")
	                .build();
	
	        HttpResponse<String> response =
	                client.send(request, HttpResponse.BodyHandlers.ofString());
	
	        String bodyvalue = response.body();
	        
	        Document doc = getDocument(new ByteArrayInputStream(bodyvalue.getBytes(StandardCharsets.UTF_8)));
	        
	        XPath xpath = XPathFactory.newInstance().newXPath();

	        String id = xpath.evaluate(
	            "//*[local-name()='id']/text()",
	           doc
	        );
	        
	        properties.put("id", id);
	        
	        
	        String author = xpath.evaluate(
	        	    "//*[local-name()='author']/*[local-name()='href']/text()",
	        	   doc
	        	);

	        properties.put("author", author);
	        
		      
		      String datestring =  xpath.evaluate(
			            "//*[local-name()='last-modified']/text()",
				           doc
				        );
		      
		      DateTimeFormatter formatter =
		    	        DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);

		    	ZonedDateTime zdt = ZonedDateTime.parse(datestring, formatter);
		    	OffsetDateTime dateTime = zdt.toOffsetDateTime();
		      
		      
		      String creationstring =  xpath.evaluate(
			            "//*[local-name()='creationdate']/text()",
				           doc
				        );

		      OffsetDateTime odt = OffsetDateTime.parse(creationstring);

		      String creation = odt.format(
		          DateTimeFormatter.ofPattern("dd/MM/yyyy")
		      );
		      
//		      DateTimeFormatter input =
//		              DateTimeFormatter.RFC_1123_DATE_TIME;
//
//		      DateTimeFormatter output =
//		              DateTimeFormatter.ofPattern("dd/MM/yyyy");
//
//		      String fecha = ZonedDateTime
//		              .parse(datestring, input)
//		              .format(output);

	        properties.put("lastmodified", dateTime);
	        properties.put("creation", creation);
		      
		      return properties;
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
	
	private void setAttribute(Document doc, ApiFile file, String element, String attribute) {
		String value = getValue(doc.getElementsByTagName(element));
		if (value!=null) {
			file.removeAttribute(attribute);
			file.setAttribute(attribute, value);
		}
	}
	
	private void setDateAttribute(Document doc, ApiFile file, String element, String attribute) {
		String value = getValue(doc.getElementsByTagName(element));
		if (value==null) return;
		if (value.charAt(1)=='/') value = "0"+value;
		String fixed = value.replaceFirst(
			    "(\\d{4}-\\d{2}-\\d{2})([+-]\\d{2}:\\d{2})",
			    "$1T00:00$2"
			);
		OffsetDateTime odt = OffsetDateTime.parse(fixed);
		String datevalue = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(odt);
		if (value!=null) {
			file.removeAttribute(attribute);
			file.setAttribute(attribute, datevalue);
		}
	}
	
	private String getValue(NodeList nodes) {
		if (nodes.getLength()>0) {
			org.w3c.dom.Node node = nodes.item(0);
			String value = node.getTextContent();
			if (value!=null && !"".equals(value) && !"-".equals(value)) {
				return value;
			}
		}
		return null;
	}	
	
	private List<String> getValues(NodeList nodes) {
		List<String> values = new ArrayList<>();
		if (nodes.getLength()>0) {
			for (int v=0; v<nodes.getLength(); v++) {
				org.w3c.dom.Node node = nodes.item(0);
				String value = node.getTextContent();
				if (value!=null && !"".equals(value) && !"-".equals(value)) {
					values.add(value);
				}
			}
			return values;
		}
		return values;
	}
	
	private String getUrl(Document doc, String baseUrl, String element,  String attributeName) {
		String relativeUrl =  getAttribute(doc, element, attributeName);
		if (relativeUrl!=null) {
			String url = URI.create(baseUrl)
                .resolve(relativeUrl)
                .toString();
			return url;
		}
		return null;
	}
	
	private String getAttribute(Document doc, String element,  String attributeName) {
		NodeList elements = doc.getElementsByTagName(element);
		if (elements.getLength()>0) {
			org.w3c.dom.Node filesnode = elements.item(0);
			Attr attribute = ((Element)filesnode).getAttributeNode(attributeName);
			String value  = attribute!=null ? attribute.getTextContent() : null;
			return value; 
		}
		return null;
	}
 	
	private Document getDocument(InputStream is) 
	        throws IOException,
            ParserConfigurationException,
            SAXException {
		return getDocument(is, null);
	}

	private Document getDocument(InputStream is, String encoding)
	        throws IOException,
	               ParserConfigurationException,
	               SAXException {

	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    factory.setNamespaceAware(true);

	    DocumentBuilder builder = factory.newDocumentBuilder();

	    InputSource source = new InputSource(is);
	    if (encoding!=null) {
	    	source.setEncoding(encoding);
	    }	
	    Document doc = builder.parse(source);

	    NodeList nodes = doc.getElementsByTagName("*");

	    for (int i = 0; i < nodes.getLength(); i++) {
	        Node node = nodes.item(i);

	        System.out.println(
	            node.getNodeName()
	            + " | local=" + node.getLocalName()
	            + " | ns=" + node.getNamespaceURI()
	        );
	    }

	    return doc;
	}
	
	private String lastSegment(String uri) {
		String s[] = uri.split("/");
		if (s.length>0) {
			return s[s.length-1].trim();
		}
		return "-";
	}
	
	private InputStream getStream(String href) throws IOException {
		URLConnection connection = null;
		InputStream is = null;
			
			String uri = href.replace("kbee:", "/webdav");
			String urlstring = getUrl()+uri;
			URL url = new URL(urlstring);
			connection = url.openConnection();
			
			String base64Credentials = Base64.toString((user+":"+password).getBytes());
			connection.setRequestProperty("Authorization", "Basic " + base64Credentials);
			

			is = connection.getInputStream();
			
			return is;
	}
	
	private void close(InputStream is) {
		if (is!=null) {
			try {
				is.close();
			}
			catch (IOException e) {
				
			}
		}
	}
	
	@SuppressWarnings("unused")
	private void printNodes(Node node, int level) {
	    System.out.println(
	        "  ".repeat(level)
	        + node.getNodeName()
	        + " [" + node.getNodeType() + "]"
	        + " = '" + node.getNodeValue() + "'"
	    );

	    NodeList children = node.getChildNodes();

	    for (int i = 0; i < children.getLength(); i++) {
	        printNodes(children.item(i), level + 1);
	    }
	}
}
