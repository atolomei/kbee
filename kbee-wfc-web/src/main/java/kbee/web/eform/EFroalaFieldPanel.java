package kbee.web.eform;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.w3c.dom.Element;

import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.form.EFormContentData;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.ValueUpdated;
import com.novamens.content.model.ContentId;
import com.novamens.content.text.AncordResolver;
import com.novamens.content.text.ImageResolver;
import com.novamens.content.text.Text;
import com.novamens.kbee.content.form.KbeeEHtmlField;
import com.novamens.kbee.content.text.KbeeText;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.form.Field;
import com.novamens.wicket.markup.html.form.TextField;

import kbee.web.resource.ResourcesPanel;
import kbee.wicket.froala.FroalaField;

@SuppressWarnings("serial")
public class EFroalaFieldPanel extends EFieldPanel<KbeeEHtmlField> implements ResourcesPanel {
	private static final long serialVersionUID = 1L;
	
	boolean html = true;

	public EFroalaFieldPanel(String id, KbeeEHtmlField field, IModel<EFormData> data) {
		super(id, field, data);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		getContainer().add(new FroalaField("field", new FieldDataModel<KbeeEHtmlField, String>(getFieldModel(), getDataModel())) {
			@Override
			public IModel<String> getLabel() {
				return getField().getLabel()!=null ?
					new Model<String>(getField().getLabel()) :
					new Model<String>("");	
			}
			
		 
			
			
			
			
			@Override
			public IModel<String> getSubtitle() {
				return getField().getSublabel()!=null ? new Model<String>(getField().getSublabel()) : null; 
			}
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				super.onUpdate(target);
				updateModel();
				fireScanAll(new EAjaxFormEvent(target, getField(), getData()));
			}
			public void onClose(AjaxRequestTarget target) {
				super.onClose(target);
				updateModel();
				target.add(getContainer());
			}
			@Override
			protected void onKey(AjaxRequestTarget target, String jsKeycode) {
				fireScanAll(new EFocusEvent(target, getField()));
			}
			@Override
			protected IModel<String> getHelpText() {
				return new Model<String>(getField().getModel().getMetainfoMessage());
			}
			@Override
			public boolean isEditionEnabled() {
				return isInputEnabled();
			}
			public boolean isInputEnabled() {
				return super.isInputEnabled() && 
					!getField().isReadOnly() && 
					getField().isEnabled(getData()) && 
					getData().getForm().isEnabled() &&
					!getData().isSigned();
			}
			@Override
			public boolean isRequired() {
				return getField().isRequired();
			}
			@Override
			public Disposition getDisposition() {
				return EFroalaFieldPanel.this.getDisposition();
			}
			@Override
			public boolean isHelpVisible() {
				return getField().getModel().getMetainfoMessage()!=null;
			}
			@Override
			protected String getImagesUrl() {
				if (getContent()==null) return null;
				String url = "/froala/images";
				url += "?content="+String.valueOf(getContent().getId());
				url += "&tag=archivo";
				return url;
			}
			@Override
			protected Content getContent() {
				return getFormObject() instanceof Content ? (Content)getFormObject() : null;
			}
			@Override
			public boolean isVisible() {
				return html;
			}
			@Override
			protected void onUpdate(String oldvalue, String newvalue) {
				String label = getField().getLabel()!=null  ? getField().getLabel() : getField().getName();
				setUpdatedField(new ValueUpdated(getData().getForm(), label, oldvalue, newvalue));
				fireScanAll(new EAjaxFormEvent(null, getField(), getData()));
			}
		});
		
		IModel<String> textmodel = new Model<String>() {
			@SuppressWarnings("unchecked")
			public String getObject() {
				Text text = KbeeText.textOf(((Field<String>)getInput()).getValue());
				if (isEmpty(text) && getEditor().isEditionEnabled()) {
					return getLabel("click_me").getObject();
				}	
				String strvalue = text.getText(new AncordResolver() {
					@Override
					public Element resolve(Element ancord) {
						return ancord;
					}
				}, new ImageResolver() {
					@Override
					public Element resolve(Element image) {
						String src = image.getAttribute("src");
						Content content = ((EFormContentData)getData()).getContent();
						src = "/resource/content/"+(new ContentId(content).toString()) +"/" + src;
						image.setAttribute("src", src);
						return image;
					}
				});
				return strvalue;
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
		};
		
		WebMarkupContainer textcontainer = new WebMarkupContainer("text-container") {
			@Override
			public boolean isVisible() {
				return !html;
			}
		};
		
		//Label label = new Label("label", getField().getLabel());
		
		Label text = new Label("text", textmodel);
		
//		text.add(new AjaxEventBehavior("click") {
//			@Override
//			protected void onEvent(AjaxRequestTarget target) {
//				if (!getField().isReadOnly() && 
//						getField().isEnabled(getData()) && 
//						getData().getForm().isEnabled()) {
//					html = true;
//					target.add(getContainer());
//				}
//			}
//		});
		
		
			
		text.setEscapeModelStrings(false);
		
		//getContainer().add(label);
		//textcontainer.add(label);
		textcontainer.add(text);
		
		getContainer().add(textcontainer);
	}
	
	public boolean isEnabled() {
		return true;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void setValues(List<?> values) {
		String value = !values.isEmpty() ? values.get(0).toString() : null;
		getData().setData(getField(), value);
		((TextField<String>)get("container:field")).setValue(value);
	}
	
	public void add(Resource resource) {
		
	}
	
	public void addVersion(Resource resource, Resource version) {
		
	}
}