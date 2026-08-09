package kbee.web.eform;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.content.form.EFormContentData;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;
import com.novamens.wicket.markup.html.panel.KBPanel;

@SuppressWarnings("serial")
public class EHtmlStructRawViewer extends KBPanel {
	private static final long serialVersionUID = 1L;
	
	
	private IModel<EFormData> datamodel;
	private IModel<EFormField<?>> fieldmodel;
	
	WebMarkupContainer container;
	WebMarkupContainer textcontainer;
	
	private static final ResourceReference KBEE_TEXT_CSS = new CssResourceReference(EHtmlStructFieldPanel.class, "EHtmlText.css");
	
	
	public EHtmlStructRawViewer(String id, EFormField<?> field, IModel<EFormData> data) {
		super(id);
		setField(field);
		setData(data);
	}
	
	public void setField(EFormField<?> field) {
		this.fieldmodel = new ComponentModel<EFormField<?>>(field);
	}
	
	public EFormData getData() {
		return getDataModel().getObject();
	}
	
	public void setData(IModel<EFormData> model) {
		this.datamodel = model;
	}
	
	public IModel<EFormData> getDataModel() {
		return datamodel;
	}
	
	public IModel<EFormField<?>> getFieldModel() {
		return fieldmodel;
	}
	
	public EFormField<?> getField() {
		return getFieldModel().getObject();
	}
	
	public IModel<String> getLabel() {
		return getField().getLabel()!=null ?
			new Model<String>(getField().getLabel()) :
			new Model<String>("");	
	}
	
	protected WebMarkupContainer getContainer() {
		return container;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		container = new WebMarkupContainer("container");
		if (getCssClass()!=null) {
			container.add(new AttributeModifier("class", new Model<String>() {
				public String getObject() {
					return getCssClass();
				}
			}));
		}
		//container.add(new Label("label", getLabel()));
		
		add(container);
		
		textcontainer = new WebMarkupContainer("text-container");
		
		//Label label = new Label("label", getField().getLabel());
		
		IModel<String> textmodel = new HtmlStructModel(
			() -> (String)getDataModel().getObject().getData(getField()),
			() -> ((EFormContentData)getData()).getContent(), true);
	
		
		Label text = new Label("text", textmodel);
		
		text.setEscapeModelStrings(false);
		
		//container.add(label);
		textcontainer.add(text);
		
		container.add(textcontainer);
		
	}	
	
	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(KBEE_TEXT_CSS));
//		response.render(JavaScriptHeaderItem.forScript(getFieldJS(), "structhtmlfield"));
	}
	
	protected String getCssClass() {
		String css = "";
		if (getField().getCssClass()!=null) {
			css += getField().getCssClass();
		}
		return "".equals(css.trim()) ? null : css.trim();
	}

	protected String getValueCss() {
		return "eform-html";
	}
	
	
} 
