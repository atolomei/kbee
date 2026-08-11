package kbee.web.content.workflow;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;


import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.IFormModelUpdateListener;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebApplication;
import org.w3c.dom.Element;

import com.novamens.content.base.Content;
import com.novamens.content.text.AncordResolver;
import com.novamens.content.text.ImageResolver;
import com.novamens.content.text.Text;
import com.novamens.content.text.template.ContentTextTemplate;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.kbee.content.text.KbeeText;
import com.novamens.kbee.content.text.template.ContentIncludeResolver;
import com.novamens.kbee.content.text.template.TemplateData;
import com.novamens.kbee.content.util.ContentVariableResolver;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.WorkflowContext;

import javax.servlet.ServletContext;
import kbee.util.logging.Logger;

@SuppressWarnings("serial")
public class ResolutionLetterPanel<T extends Content> extends ModelPanel<WorkflowContext> implements IFormModelUpdateListener {
	private static final long serialVersionUID = 1L;
	
	static Logger logger =  Logger.getLogger(ResolutionLetterPanel.class.getName());
	
	IModel<ContentTextTemplate> templatemodel;
	IModel<TemplateData> datamodel;
	
	public ResolutionLetterPanel(IModel<WorkflowContext> model, IModel<ContentTextTemplate> templateModel) {
		super("letter", model);
		
		setTemplate(templateModel);
		
		setDataModel(new Model<TemplateData>(getTemplateData(getWorkflowContext())));
		
		add(new LetterEditorPanel<T>(getContentModel(), getDataModel(), templateModel) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				target.add(ResolutionLetterPanel.this.get("preview"));
			}
		});

		add(new LetterPreviewPanel<T>(getContentModel(), getDataModel()) {
			@Override
			protected String getTemplateText() {
				return ResolutionLetterPanel.this.getTemplateText();
			}
		});
	}
	
	public ContentTextTemplate getTemplate() {
		return templatemodel.getObject();
	}
	
	public void setTemplate(IModel<ContentTextTemplate> model) {
		this.templatemodel = model;
	}
	
	public IModel<TemplateData> getDataModel() {
		return datamodel;
	}
	
	public void setDataModel(IModel<TemplateData> model) {
		this.datamodel = model;
	}
	
	@Override
	public void updateModel() {
		Map<String, String> values = getDataModel().getObject().getValues();
		values.put("template", getTemplate().getContentId());
		getWorkflowService().setParameters(values);
		getWorkflowService().setResolution(getTemplateText(), getTemplateTitle());
		((KbeeContext)getModel().getObject()).setParameters(values);
		((KbeeContext)getModel().getObject()).setResolution(getTemplateText());
		((KbeeContext)getModel().getObject()).setResolutionTitle(getTemplateTitle());
	}
	
	protected String getTemplateTitle() {
		return  getTemplate().getTitle();
	}
	
	@SuppressWarnings("unchecked")
	protected T getContent() {
		return (T)((KbeeContext)getWorkflowContext()).getContent();
	}
	
	protected IModel<T> getContentModel() {
		return new ObjectModel<T>(getContent());
	}
	
	protected WorkflowContext getWorkflowContext() {
		return getModelObject();
	}
	protected WorkflowService getWorkflowService() {
		return getContent().getService(WorkflowService.class);
	}
	
	protected TemplateData getTemplateData(WorkflowContext context) {
		TemplateData data = new TemplateData();
		data.setValues(((KbeeContext)context).getParameters());
		return data;
	}
	
	protected String getTemplateText() {
		
		String textstring = getTemplate().getText(new ContentVariableResolver<T>(getContentModel(), getDataModel()), new ContentIncludeResolver());
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