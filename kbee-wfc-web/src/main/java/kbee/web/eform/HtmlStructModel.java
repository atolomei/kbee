package kbee.web.eform;

import org.apache.wicket.model.IModel;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.ContentId;
import com.novamens.content.service.ContentService;
import com.novamens.content.service.UrlService;
import com.novamens.content.text.AncordResolver;
import com.novamens.content.text.ImageResolver;
import com.novamens.content.text.Text;
import com.novamens.kbee.content.text.KbeeText;
import com.novamens.service.ServiceLocator;

public class HtmlStructModel implements IModel<String> {
	private static final long serialVersionUID = 1L;
	
	private IModel<String> textmodel;
	private IModel<Content> contentmodel;
	private boolean shared = false;
	
	public HtmlStructModel(IModel<String> textmodel, IModel<Content> contentmodel) {
		this(textmodel, contentmodel, false);
	}
	
	public HtmlStructModel(IModel<String> textmodel, IModel<Content> contentmodel, boolean shared) {
		this.textmodel = textmodel;
		this.shared = shared;
		this.contentmodel = contentmodel;
	}
	
	public String getObject() {
		Text text = KbeeText.textOf(getText());
		String strvalue = text.getText(new AncordResolver() {
			@Override
			public Element resolve(Element ancord) {
				String href = ancord.getAttribute("href");
				if (href.contains("?include")) {
					Element html = getTextPart(ancord);
					if (html!=null) {
						boolean remove = true;
						while (remove) {
							remove = false;
							NodeList childs = html.getChildNodes();
							for (int n=0; n<childs.getLength(); n++) {
								Node child = childs.item(n);
								if ("#text".equals(child.getNodeName())) {
									html.removeChild(child);
									remove = true;
									break;
								}
								if ("H3".equals(child.getNodeName())) {
									html.removeChild(child);
									remove = true;
									break;
								}
							}
						}
						Node i = ancord.getOwnerDocument().importNode(html, true);
						NodeList childs = i.getChildNodes();
						for (int n=0; n<childs.getLength(); n++) {
							Node child = childs.item(n);
							if ("#text".equals(child.getNodeName())) {
								i.removeChild(child);
							}
							if ("H3".equals(child.getNodeName())) {
								i.removeChild(child);
							}
						}
						Node parent = ancord.getParentNode();
						ancord.getParentNode().replaceChild(i, ancord);
						childs = parent.getChildNodes();
						remove=true;
						while (remove) {
							remove = false;
							for (int n=0; n<childs.getLength(); n++) {
								Node child = childs.item(n);
								if ("BODY".equals(child.getNodeName())) {
									((Element)child).setAttribute("class", "include");
								}
								if ("#text".equals(child.getNodeName())) {
									parent.removeChild(child);
									remove = true;
								}
							}
						}

					}
				}
				else {
					if (href!=null) {
						href = "/id/"+href;
						ancord.setAttribute("href", href);
						ancord.setAttribute("target", "_blank");
					}
				}
				return ancord;
			}
		}, new ImageResolver() {
			@Override
			public Element resolve(Element image) {
				String src = image.getAttribute("src");
				if (shared) {
					Resource resource = ((ResourceContainer)getContent()).getResource(src);
					src = resource.getService(UrlService.class).getPublicUrl();
				}	
				else {
					src = "/resource/content/"+(new ContentId(getContent()).toString()) +"/" + src;
				}
				image.setAttribute("src", src);
				return image;
			}
		});
		strvalue = strvalue.replace("<BODY", "<DIV");
		strvalue = strvalue.replace("</BODY", "</DIV");
		return strvalue;
	}
	
	public String getText() {
		return textmodel.getObject();
	}
	
	public Content getContent() {
		return contentmodel.getObject();
	}
	
	protected boolean isEmpty(Text text) {
		if (text==null || text.asString()==null)
			return true;
		String value = text.asString();
		value = value.replace("<p class=\"last\">","");
		value = value.replace("<p>","");
		value = value.replace("</p>","");
		value = value.replace("<br>","");
		value = value.replace("<br/>","");
		if ("".equals(value.trim()))
			return true;
		return false;
	}
	
	private Element getTextPart(Element anchor) {
		try {
			String href = anchor.getAttribute("href");
			int i = href.indexOf("#");
			int a = href.indexOf("?");
			if (i<0) return null;
			String contentId = href.substring(0, i);
			Content content = getContentDao().findContentByOId(Long.valueOf(contentId));
			if (content==null) return null;
			KbeeText ktext = (KbeeText)content.getService(ContentService.class).getText();
			if (ktext==null) return null;
			String partname = a>0 ? href.substring(i+1, a) : href.substring(i);
			Element part = ktext.getPartElement(partname);
			return part;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	

}
