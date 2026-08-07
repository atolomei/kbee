package com.novamens.content.web.text.template.markup;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebApplication;
import org.w3c.dom.Element;

import com.novamens.content.base.Content;
import com.novamens.content.text.AncordResolver;
import com.novamens.content.text.ImageResolver;
import com.novamens.content.text.Text;
import com.novamens.content.text.template.ContentTextTemplate;
import com.novamens.kbee.content.text.KbeeText;
import com.novamens.kbee.content.text.template.ContentIncludeResolver;
import com.novamens.kbee.content.text.template.TemplateData;
import com.novamens.kbee.content.util.ContentVariableResolver;
import com.novamens.kbee.wicket.model.ModelPanel;

//import jakarta.servlet.ServletContext;
import javax.servlet.ServletContext;

/**
 * @param <T>
 */
@SuppressWarnings("serial")
public class TemplateEditorMainPanel<T extends Content> extends ModelPanel<T> {

	static kbee.util.logging.Logger logger =  kbee.util.logging.Logger.getLogger(TemplateEditorMainPanel.class.getName());


	private static final long serialVersionUID = 1L;
	
	private IModel<TemplateData> datamodel = new Model<TemplateData>(new TemplateData());
	private ContentTextTemplate template;

	public TemplateEditorMainPanel(IModel<T> model, IModel<TemplateData> datamodel, ContentTextTemplate template) {
		super("editor");
		
		setModel(model);
		this.datamodel = datamodel;
		setTemplate(template);
		
		add(new TemplateEditorPanel<T>(model, getDataModel(), template) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				target.add(TemplateEditorMainPanel.this.get("preview"));
				TemplateEditorMainPanel.this.onUpdate(target);
			}
		});
		
		add(new PreviewPanel<T>(model, getDataModel(), template) {
			@Override
			protected String getTemplateText() {
				return TemplateEditorMainPanel.this.getTemplateText();
			}
		});
	}
	
	public ContentTextTemplate getTemplate() {
		return template;
	}
	
	public void setTemplate(ContentTextTemplate template) {
		this.template = template;
	}
	
	public IModel<TemplateData> getDataModel() {
		return datamodel;
	}
	
	public TemplateData getData() {
		return getDataModel().getObject();
	}
	
	
	protected String getTemplateTitle() {
		return  getTemplate().getTitle();
	}
	
	
	protected String getTemplateText() {
		
		String textstring = getTemplate().getText(new ContentVariableResolver<T>(getModel(), getDataModel()), new ContentIncludeResolver());
		//String textstring = "";
		Text text = new KbeeText(textstring);
		
		String strvalue = text.getText(new AncordResolver() {
			@Override
			public Element resolve(Element ancord) {
				return ancord;
			}
		}, new ImageResolver() {
			@Override
			public Element resolve(Element image) {
				String src = image.getAttribute("src");
				if (!src.contains("resource"))
				src = "/resource/content/"+getTemplate().getContentId() +"/" + src;
				image.setAttribute("src", src);
				return image;
			}
		});
		
		
		String cssText = getCssText();
		
		if (cssText!=null && !"".equals(cssText)) {
			strvalue = "<html><head><style>"+cssText+"</style></head><body>"+getBody(strvalue)+"</body></html>";
		}
		
		return strvalue;
	}
	
	protected void onUpdate(AjaxRequestTarget target) {
		
	}
	
	protected String getCssText() {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(getCssPath()));
			sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
		}
		catch (IOException e) {
			logger.error(e);
		}
		finally {
			try {
				br.close();
			}
			catch (IOException e) {
				logger.error(e);
			}
		}
		return sb.toString();
	}
	
	protected String getCssPath() {
		WebApplication webApplication = WebApplication.get();
		ServletContext servletContext = webApplication.getServletContext();
		String path = servletContext.getRealPath("/css/response.css");
		return path;
	}
	
	protected String getBody(String html) {
		String bodytag = "<BODY xmlns=\"http://www.w3.org/1999/xhtml\">";
		String endbodytag = "</BODY>";
		int i1 = html.indexOf(bodytag);
		int i2 = html.indexOf(endbodytag);
		String body = html;
		if (i1>0 && i2>0) {
			body = html.substring(i1+bodytag.length(), i2);
		}
		return body;
	}
}
