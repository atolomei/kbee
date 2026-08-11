package com.novamens.kbee.content.text;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.xerces.xni.parser.XMLDocumentFilter;

import org.cyberneko.html.HTMLConfiguration;
import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.novamens.content.text.AncordResolver;
import com.novamens.content.text.ImageResolver;
import com.novamens.content.text.Text;
import com.novamens.content.text.TextChange;
import com.novamens.content.text.TextPart;
import com.novamens.util.KbeeRuntimeException;

public class KbeeText implements Text{
	
	private String text;

	static final private org.apache.logging.log4j.Logger logger = LogManager.getLogger(KbeeText.class.getName());
	
	public KbeeText(String text) {
		this.text = text;
	}

	 

	public String asString() {
		return text;
	}

	 

	public String getUri(String attach) {
		return null;
	}

	public String getText(AncordResolver ancordResolver) {
		return getText(null, null);
	}
	 
	public String getText(AncordResolver ancordResolver, ImageResolver imageResolver) {
		String strResult = "";
		
		Element rootElement = getDom(ancordResolver, imageResolver);

		strResult = dom2String(rootElement);
		return strResult;
	}
	
	public Element getHtmlBody(AncordResolver ancordResolver, ImageResolver imageResolver) {
		Element rootElement = getDom(ancordResolver, imageResolver);

		XPath xpath = XPathFactory.newInstance().newXPath();
		XPathExpression exp;
		Element body = null;
		try {
			exp = xpath.compile("//*[name() = 'BODY']");
			body = (Element) exp.evaluate(rootElement, XPathConstants.NODE);
		} 
		catch (XPathExpressionException e) {
			throw new KbeeRuntimeException(e);
		}
		return body;
	}


	public List<TextPart> getParts() {
		
		List<TextPart> parts = new ArrayList<TextPart>();
		
		try {
			Element rootElement = getDom(null, null);
			XPath xpath = XPathFactory.newInstance().newXPath();
			XPathExpression exp;
			exp = xpath.compile("//*[self::H1 or self::H2 or self::H3 or self::H4]");
			NodeList nodeList= (NodeList) exp.evaluate(rootElement, XPathConstants.NODESET);
			for (int i=0; i<nodeList.getLength(); i++) {
				Element h = (Element)nodeList.item(i);
				KbeeTextPart part = new KbeeTextPart();
				Attr id = h.getAttributeNode("id");
				if (id!=null)
				part.setName(id.getNodeValue());
				part.setTitle(h.getTextContent());
				
				int level =1 ;
				if (h.getNodeName().equals("H1"))
					level = 1;
				if (h.getNodeName().equals("H2"))
					level = 2;
				if (h.getNodeName().equals("H3"))
					level = 3;
				if (h.getNodeName().equals("H4"))
					level = 4;
				part.setLevel(level);
				parts.add(part);
			}
		
		} 
		catch (XPathExpressionException e) {
			throw new KbeeRuntimeException(e);
		}
		
		return parts;
	}
	 
	public TextPart getPart(String name) {
		if (name==null) return null;
		for (TextPart part : getParts()) {
			if (name.equals(part.getName())) {
				return part;
			}
		}
		return null;
	}
	
	public Element getPartElement(String name) {
		
		List<TextPart> parts = new ArrayList<TextPart>();
		
		try {
			Element rootElement = getDom(null, null);
			XPath xpath = XPathFactory.newInstance().newXPath();
			XPathExpression exp;
			exp = xpath.compile("//*[self::H1 or self::H2 or self::H3 or self::H4]");
			NodeList nodeList= (NodeList) exp.evaluate(rootElement, XPathConstants.NODESET);
			KbeeTextPart part = null, nextpart = null;
			Element partelement = null, nextpartelement = null;
			for (int i=0; i<nodeList.getLength() && nextpart==null; i++) {
				
				Element h = (Element)nodeList.item(i);
				KbeeTextPart p = new KbeeTextPart();
				Attr id = h.getAttributeNode("id");
				if (id!=null)
				p.setName(id.getNodeValue());
				p.setTitle(h.getTextContent());
				int level =1 ;
				if (h.getNodeName().equals("H1"))
					level = 1;
				if (h.getNodeName().equals("H2"))
					level = 2;
				if (h.getNodeName().equals("H3"))
					level = 3;
				if (h.getNodeName().equals("H4"))
					level = 4;
				p.setLevel(level);
				
				if (name.equals(p.getName()) ) {
					part = p;
					partelement = h;
				}
				else {
					if (part!=null) {
						if (p.getLevel()<=part.getLevel()) {
							nextpart = p;
							nextpartelement = h;
						}
					}
				}
				parts.add(part);
			}

			xpath = XPathFactory.newInstance().newXPath();
			Element body = null;
			try {
				exp = xpath.compile("//*[name() = 'BODY']");
				body = (Element) exp.evaluate(rootElement, XPathConstants.NODE);
			} 
			catch (XPathExpressionException e) {
				throw new KbeeRuntimeException(e);
			}
			
			boolean start = false, end=false;
			nodeList= body.getChildNodes();
			for (int i=0; i<nodeList.getLength(); i++) {
				Node h = (Node)nodeList.item(i);
				if (h.equals(nextpartelement))
					end = true;
				if ((!h.equals(partelement) && !start) || end) {
					body.removeChild(h);
				}
				else {
					if (h.equals(partelement)) {
						start = true;
					}
				}
			}
			
			//String text = dom2String(body);
			return body;
		} 
		catch (XPathExpressionException e) {
			throw new KbeeRuntimeException(e);
		}
		
	}
	
	

	
	public List<TextChange> getChanges(KbeeText text) {
		
		List<TextChange> changes = new ArrayList<>();
		
		List<TextPart> selfparts = getParts();
		List<TextPart> textparts = text.getParts();
		
		for (TextPart selfpart : selfparts) {
			boolean found = false;
			for (TextPart textpart : textparts) {
				if (textpart.getName().equals(selfpart.getName())) {
					found = true;
					break;
				}
			}
			if (!found) {
				KbeeTextChange change = new KbeeTextChange();
				change.setType(TextChange.ADD);
				change.setPart(selfpart);
				changes.add(change);
			}
		}

		int i = 0;
		for (TextPart selfpart : selfparts) {
			int level = selfpart.getLevel();
			if (i<selfparts.size() && (i==selfparts.size()-1 || selfparts.get(i+1).getLevel()<=level)) {
				Element selfpartelement = getPartElement(selfpart.getName());
				boolean found = false;
				for (TextPart textpart : textparts) {
					if (textpart.getName().equals(selfpart.getName())) {
						found = true;
						break;
					}
				}
				if (found) {
					Element textpartelement = text.getPartElement(selfpart.getName());
					String selfparttext = dom2String(selfpartelement);
					//selfparttext.length();
					selfparttext = selfparttext.replace("\r", "");
					selfparttext = selfparttext.replace("\n", "");
					selfparttext = selfparttext.replace("\t", "");
					String textparttext = dom2String(textpartelement);
					textparttext = textparttext.replace("\r", "");
					textparttext = textparttext.replace("\n", "");
					textparttext = textparttext.replace("\t", "");
					
//					textparttext.trim().length();
//					for (int c=0; c<selfparttext.length(); c++) {
//						if  (!(selfparttext.charAt(c)==textparttext.charAt(c))) {
//							System.out.print(selfparttext.substring(0,c));
//						}
//					}
					
					if (!selfparttext.equals(textparttext)) {
						KbeeTextChange change = new KbeeTextChange();
						change.setType(TextChange.UPDATE);
						change.setPart(selfpart);
						List<String> paragraphs = new ArrayList<>();
						for (Element p : compareTexts(selfpartelement, textpartelement)) {
							paragraphs.add(p.getTextContent());
						}
						change.setNotes(paragraphs);
						changes.add(change);
					}
				}
			}
			i++;
		}
		
		for (TextPart textpart : textparts) {
			boolean found = false;
			for (TextPart selfpart : selfparts) {
				if (textpart.getName().equals(selfpart.getName())) {
					found = true;
					break;
				}
			}
			if (!found) {
				KbeeTextChange change = new KbeeTextChange();
				change.setType(TextChange.DELETE);
				change.setPart(textpart);
				changes.add(change);
			}
		}

		
		
//		para cada parte
//			si no esta 
//				es un alta
//				
//		para dacar parte
//			si no esta 
//			 en un baja
//		
//		para cada parte
//			si no tiene hijos
//				comparar
//				
//				
//		si los camnios son las de la mitad
//		
		return changes;
	}

	public static KbeeText textOf(String text) {
		return new KbeeText(cleanHtml(text, 1));
	}
	
	/***
	 *
	 * 
	 * @param text
	 * @param encoding
	 * @return
	 */
	public static String cleanText(String text, String encoding) {
		
		String strResult = "";
		
		if (text.equals("")) 
			return text;
		
		InputSource source = new InputSource(new ByteArrayInputStream(text.getBytes()));
		DOMParser parser = new DOMParser();
		try {
			parser.setFeature("http://cyberneko.org/html/features/insert-namespaces", true);
			parser.setFeature("http://cyberneko.org/html/features/override-namespaces", true);
			parser.setProperty("http://cyberneko.org/html/properties/names/elems", "lower");
			parser.setProperty("http://cyberneko.org/html/properties/default-encoding", encoding);  
			parser.parse(source);
		} 
		catch (SAXException e) {
			throw new KbeeRuntimeException(e);
		}
		catch (IOException e) {
			throw new KbeeRuntimeException(e);
		}
		
		Element rootElement = parser.getDocument().getDocumentElement();
		XPath xpath = XPathFactory.newInstance().newXPath();
		XPathExpression exp;
		Element body = null;
		try {
			exp = xpath.compile("//*[name() = 'body']");
			body = (Element) exp.evaluate(rootElement, XPathConstants.NODE);
		} 
		catch (XPathExpressionException e) {
			throw new KbeeRuntimeException(e);
		}
		
		strResult = dom2String(body);
		strResult = strResult.replaceFirst("<body xmlns=\"http://www.w3.org/1999/xhtml\">", "");
		strResult = strResult.replaceFirst("</body>", "");
		return strResult;
	}

 
	/***
	 * 
	 * 
	 * @param xtx
	 * @return
	 */
	public static String cleanSnippet(String xtx) {

		String strResult = "";
		
		if (xtx==null || xtx.length()==0) 
			return xtx;
		
		String text = xtx;
		
		InputSource source = new InputSource(new ByteArrayInputStream(text.getBytes()));
		
		ElementRemover remover = new ElementRemover();
		
		remover.acceptElement("body", null);
		remover.acceptElement("html", null);
		
		remover.acceptElement("br", null);
		remover.acceptElement("em", null);
		
		
		remover.acceptElement("span", 	new String[] { "class", "id", "style"});
		
		remover.removeElement("div");
		remover.removeElement("strong");
		
		remover.removeElement("h1");
		remover.removeElement("h2");
		remover.removeElement("h3");
		remover.removeElement("h4");
		remover.removeElement("h5");
		remover.removeElement("h6");
		
		remover.removeElement("ul");
		remover.removeElement("li");
		
		remover.removeElement("b");
		remover.removeElement("i");
		remover.removeElement("u");
		// remover.acceptElement("img", new String[] { "class", "src", "style", "width", "alt",  "height" });
		remover.removeElement("a");
		remover.removeElement("script");
		remover.removeElement("img");
		remover.removeElement("embed");
		
		org.cyberneko.html.filters.Writer writer =  new org.cyberneko.html.filters.Writer();
		
		XMLDocumentFilter[] filters = {
				remover,
				writer,
		};
		
		DOMParser parser = new DOMParser();
		try {
			parser.setProperty("http://cyberneko.org/html/properties/filters", filters);
			parser.setFeature("http://cyberneko.org/html/features/insert-namespaces", false);
			parser.setFeature("http://cyberneko.org/html/features/override-namespaces", true);
			parser.setProperty("http://cyberneko.org/html/properties/names/elems", "lower");
			parser.setProperty("http://cyberneko.org/html/properties/default-encoding", "UTF-8");  
			parser.parse(source);
		} 
		catch (SAXException e) {
			throw new KbeeRuntimeException(e);
		} 
		catch (IOException e) {
			throw new KbeeRuntimeException(e);
		}
		
		Element rootElement = parser.getDocument().getDocumentElement();
		XPath xpath = XPathFactory.newInstance().newXPath();
		XPathExpression exp;
		Element body = null;
		try {
			exp = xpath.compile("//*[name() = 'BODY']");
			body = (Element) exp.evaluate(rootElement, XPathConstants.NODE);
		} 
		catch (XPathExpressionException e) {
			throw new KbeeRuntimeException(e);
		}

		strResult = dom2String(body);
		strResult = strResult.replaceFirst("<body>", "");
		strResult = strResult.replaceFirst("</body>", "");
		return strResult;
	}

	 
	
	/**
	 * 
	 * 
	 * @param text
	 * @param style
	 * @return
	 */
	public static String cleanHtml(String text, int style) {
		String strResult = "";
		
		if (text==null || text.length()==0) 
			return text;
		
		InputSource source = new InputSource(new ByteArrayInputStream(text.getBytes()));
		
		ElementRemover remover = new ElementRemover();
		
		remover.acceptElement("body", null);
		remover.acceptElement("html", null);

		remover.acceptElement("strong", null);
		remover.acceptElement("em", null);
		remover.acceptElement("br", null);

		// remover.acceptElement("span", new String[] { "class", "style"});
		// remover.acceptElement("div", new String[] { "class", "style" });
		
		remover.acceptElement("span", 	new String[] { "class", "id", "style"});
		remover.acceptElement("div", 	new String[] { "class", "id", "style"});
		remover.acceptElement("a", 		new String[] { "class", "id"});
		remover.acceptElement("i", 		new String[] { "class", "id"});
		
		remover.acceptElement("p", new String[] { "class", "style" });
		
		remover.acceptElement("h1", new String[] { "class", "id" });
		remover.acceptElement("h2", new String[] { "class", "id"});
		remover.acceptElement("h3", new String[] { "class", "id" });
		remover.acceptElement("h4", new String[] { "class", "id" });
		remover.acceptElement("h5", new String[] { "class", "id" });
		remover.acceptElement("h6", new String[] { "class", "id" });
		
		remover.acceptElement("ul", new String[] { "class", "id", "style" });
		remover.acceptElement("ol", new String[] { "class", "id", "style" });
		remover.acceptElement("li", new String[] { "class", "id" });
		
		remover.acceptElement("dt", new String[] { "class", "id", "style" });
		remover.acceptElement("dd", new String[] { "class", "id", "style" });
		remover.acceptElement("dl", new String[] { "class", "id", "style" });
		
		remover.acceptElement("b", null);
		remover.acceptElement("i", null);
		remover.acceptElement("u", null);
		
		remover.acceptElement("img", 	new String[] { "class", "src", "style", "width", "alt",  "height" });
		remover.acceptElement("a", 		new String[] { "href", "target" });
		remover.acceptElement("object", new String[] { "classid", "codebase", "id", "width", "align",  "height", "type", "data" });
		remover.acceptElement("param", 	new String[] { "name", "value" });
		remover.acceptElement("embed", 	new String[] { "name", "bgcolor", "play", "allowscriptaccess", "align", "pluginspage", "type", "width", "height", "src", "flashvars" });
		
		remover.acceptElement("video", 	new String[] { "width", "height", "poster", "controls" });
		remover.acceptElement("source", new String[] { "src", "type" });
		remover.acceptElement("audio", 	new String[] { "src", "controls" });
		
		remover.acceptElement("table", 	new String[] { "style", "cellspacing", "cellpadding", "width", "height", "border", "id"});
		remover.acceptElement("tr", 	new String[] { "bgcolor", "width", "height", "style"});
		remover.acceptElement("td", 	new String[] { "bgcolor", "width", "height", "style", "rowspan"});
		
		remover.acceptElement("thead", null);
		remover.acceptElement("tbody", null);

		if (style==0) {
			remover.removeElement("script");
			remover.removeElement("img");
			remover.removeElement("embed");
		}	
		else 
			if (style==1) {
				remover.removeElement("script");
				remover.removeElement("embed");
		}
		
		org.cyberneko.html.filters.Writer writer =  new org.cyberneko.html.filters.Writer();
		
		XMLDocumentFilter[] filters = {
				remover,
				writer,
		};
		
		org.apache.xerces.parsers.DOMParser parser = new org.apache.xerces.parsers.DOMParser(new HTMLConfiguration());
		try {
			parser.setProperty("http://cyberneko.org/html/properties/filters", filters);
			//parser.setFeature("http://cyberneko.org/html/features/insert-namespaces", false);
			//parser.setFeature("http://cyberneko.org/html/features/override-namespaces", true);
			parser.setProperty("http://cyberneko.org/html/properties/names/elems", "lower");
			parser.setProperty("http://cyberneko.org/html/properties/default-encoding", "UTF-8");  
			parser.parse(source);
		} 
		catch (SAXException e) {
			throw new KbeeRuntimeException(e);
		} 
		catch (IOException e) {
			throw new KbeeRuntimeException(e);
		}
		
		Element rootElement = parser.getDocument().getDocumentElement();

		strResult = dom2String(rootElement);
		strResult = strResult.replaceFirst("<body xmlns=\"http://www.w3.org/1999/xhtml\">", "");
		strResult = strResult.replaceFirst("<body>", "");
		strResult = strResult.replaceFirst("</body>", "");
		strResult = strResult.replaceFirst("<html>", "");
		strResult = strResult.replaceFirst("</html>", "");
		
		return strResult.trim();
	}
	
	/**- 
	 * @param text
	 * @return
	 */
	public static Document getCleanDom(String text) {
		
		if (text==null || text.length()==0) 
			return null;
		
		InputSource source = new InputSource(new ByteArrayInputStream(text.getBytes()));
		
		ElementRemover remover = new ElementRemover();
		
		remover.acceptElement("body", null);
		remover.acceptElement("head", null);
        remover.acceptElement("link",  new String[] { "rel", "type", "href", "media" });
		remover.acceptElement("html", null);

		remover.acceptElement("strong", null);
		remover.acceptElement("em", null);
		remover.acceptElement("br", null);
		
		remover.acceptElement("span", new String[] { "class", "style" });
		remover.acceptElement("input", new String[] { "class", "type", "name"});
		remover.acceptElement("div", new String[] { "class", "style" });
				
		remover.acceptElement("p", new String[] { "class", "style" });
		
		remover.acceptElement("h1", new String[] { "class" });
		remover.acceptElement("h2", new String[] { "class" });
		remover.acceptElement("h3", new String[] { "class" });
		remover.acceptElement("h4", new String[] { "class" });
		remover.acceptElement("h5", new String[] { "class" });
		remover.acceptElement("h6", new String[] { "class" });
		
		remover.acceptElement("ul", new String[] { "class" });
		remover.acceptElement("li", new String[] { "class" });
		
		remover.acceptElement("b", null);
		remover.acceptElement("i", null);
		remover.acceptElement("u", null);
		remover.acceptElement("img", new String[] { "class", "src", "style", "width", "alt",  "height" });
		remover.acceptElement("a", new String[] { "href" });
		
		remover.removeElement("script");
		remover.removeElement("embed");
		
		org.cyberneko.html.filters.Writer writer =  new org.cyberneko.html.filters.Writer();
		
		XMLDocumentFilter[] filters = {
				remover,
				writer,
		};
		
		org.apache.xerces.parsers.DOMParser parser = new org.apache.xerces.parsers.DOMParser(new HTMLConfiguration());
		try {
			parser.setProperty("http://cyberneko.org/html/properties/filters", filters);
			parser.setFeature("http://cyberneko.org/html/features/insert-namespaces", false);
			parser.setFeature("http://cyberneko.org/html/features/override-namespaces", true);
			parser.setProperty("http://cyberneko.org/html/properties/names/elems", "lower");
			parser.setProperty("http://cyberneko.org/html/properties/default-encoding", "UTF-8");  
			parser.parse(source);
		} 
		catch (SAXException e) {
			throw new KbeeRuntimeException(e);
		} 
		catch (IOException e) {
			throw new KbeeRuntimeException(e);
		}
		
		Document document = parser.getDocument();
		
		return document;
	}
	
	public static String dom2String(Element element) {
		StringWriter output = new StringWriter();
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.transform(new DOMSource(element), new StreamResult(output));
			String xml = output.toString();
			return xml;	
		}
		catch (TransformerException e) {
			logger.error(e.getStackTrace());
			return "ERROR dom2String";
		}
	}
	
	private List<Element> compareTexts(Element text1, Element text2) {
		List<Element> diff = new ArrayList<>();
		try {
			
			XPath xpath = XPathFactory.newInstance().newXPath();
			XPathExpression exp;
			exp = xpath.compile("//*[self::P]");
			NodeList nodeList1 = (NodeList) exp.evaluate(text1, XPathConstants.NODESET);
			NodeList nodeList2 = (NodeList) exp.evaluate(text2, XPathConstants.NODESET);
			
			List<String> p1s = new ArrayList<>();
			List<String> p2s = new ArrayList<>();
			
			for (int p=0; p<nodeList1.getLength(); p++) {
				p1s.add(dom2String((Element)nodeList1.item(p)));
			}
			
			for (int p=0; p<nodeList2.getLength(); p++) {
				p2s.add(dom2String((Element)nodeList2.item(p)));
			}
			
			for (int p1=0; p1<p1s.size(); p1++) {
				boolean found = false;
				for (int p2=0; p2<p2s.size(); p2++) {
					if (p1s.get(p1).equals(p2s.get(p2))) {
						found = true;
						break;
					}
				}
				if (!found) {
					diff.add((Element)nodeList1.item(p1));
				}
			}
		} 
		catch (XPathExpressionException e) {
		}
		return diff;
		
	}
	
	private Element getDom(AncordResolver ancordResolver, ImageResolver imageResolver) {
		
		if (text==null || text.equals("")) 
			return null;
		
		InputSource source = new InputSource(new ByteArrayInputStream(text.getBytes()));
		DOMParser parser = new DOMParser();
		try {
			parser.setFeature("http://cyberneko.org/html/features/insert-namespaces", false);
			parser.setFeature("http://cyberneko.org/html/features/override-namespaces", true);
			parser.setProperty("http://cyberneko.org/html/properties/names/elems", "lower");
			parser.setProperty("http://cyberneko.org/html/properties/default-encoding", "UTF-8");  
			
			parser.parse(source);
			
		} 
		catch (SAXException e) {
			throw new KbeeRuntimeException(e);
		}
		catch (IOException e) {
			throw new KbeeRuntimeException(e);
		}
		
		Element rootElement = parser.getDocument().getDocumentElement();
		
		if (imageResolver!=null) {
			try {
				XPath xpath = XPathFactory.newInstance().newXPath();
				XPathExpression exp;
				
				exp = xpath.compile("//*[name() = 'IMG']");
				NodeList nodeList= (NodeList) exp.evaluate(rootElement, XPathConstants.NODESET);
				for (int s=0; s<nodeList.getLength(); s++) {
					imageResolver.resolve((Element)nodeList.item(s));
				}
				
				exp = xpath.compile("//*[name() = 'img']");
				nodeList= (NodeList) exp.evaluate(rootElement, XPathConstants.NODESET);
				for (int s=0; s<nodeList.getLength(); s++) {
					imageResolver.resolve((Element)nodeList.item(s));
				}
				
				exp = xpath.compile("//*[name() = 'source']");
				nodeList= (NodeList) exp.evaluate(rootElement, XPathConstants.NODESET);
				for (int s=0; s<nodeList.getLength(); s++) {
					imageResolver.resolve((Element)nodeList.item(s));
				}
				
				exp = xpath.compile("//*[name() = 'audio']");
				nodeList= (NodeList) exp.evaluate(rootElement, XPathConstants.NODESET);
				for (int s=0; s<nodeList.getLength(); s++) {
					imageResolver.resolve((Element)nodeList.item(s));
				}

				exp = xpath.compile("//*[name() = 'embed']");
				nodeList= (NodeList) exp.evaluate(rootElement, XPathConstants.NODESET);
				for (int s=0; s<nodeList.getLength(); s++) {
					imageResolver.resolve((Element)nodeList.item(s));
				}
			} 
			catch (XPathExpressionException e) {
				throw new KbeeRuntimeException(e);
			}
		}
		
		if (ancordResolver!=null) {
			try {
				XPath xpath = XPathFactory.newInstance().newXPath();
				XPathExpression exp;
				
				exp = xpath.compile("//*[name() = 'A']");
				NodeList nodeList= (NodeList) exp.evaluate(rootElement, XPathConstants.NODESET);
				for (int s=0; s<nodeList.getLength(); s++) {
					Element ancord = (Element)nodeList.item(s);
					ancordResolver.resolve(ancord);
	
				}
			} 
			catch (XPathExpressionException e) {
				throw new KbeeRuntimeException(e);
			}
		}
		
		return rootElement;
	}
}
