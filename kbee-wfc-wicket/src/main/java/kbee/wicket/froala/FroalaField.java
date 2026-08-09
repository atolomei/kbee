package kbee.wicket.froala;

import java.util.MissingResourceException;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;

import com.novamens.content.base.Content;
import com.novamens.wicket.markup.html.form.HtmlField;

@SuppressWarnings("serial")
public class FroalaField extends HtmlField {
	private static final long serialVersionUID = 1L;
	
	private boolean initialized = false;
	private boolean open = false;
	

	public FroalaField(String id) {
		super(id);
	}
	
	public FroalaField(String id, IModel<String> model) {
		super(id, model);
		setOutputMarkupId(true);
	}
	
	public void setOpen(boolean value) {
		this.open = value;
	}
	
	@Override
	public String getFocusScript() {
		return null;
	}

	@Override
	public String getCloseScript() {
		return null;
	}
	
	public void updateAjaxCloseAttributes(AjaxRequestAttributes attributes) {
		String markupId = getInput().getMarkupId();
		attributes.getDynamicExtraParameters().add("var content = top.editor"+markupId+".html.get(true); return { text: content };");
	}
	
	public void onClose(AjaxRequestTarget target) {
		Request request = RequestCycle.get().getRequest();
		String text = request.getRequestParameters().getParameterValue("text").toString("");
		setValue(text);
		FroalaField.this.onUpdate(target);
		target.add(FroalaField.this);
		initialized=false;
		open = false;
	}
	
	public void onOpen(AjaxRequestTarget target) {
		
	}
	
	protected TextArea<?>  getTextField() {
		
		
		TextArea<String> texteditor = new TextArea<String>("input", new PropertyModel<String>(this, "value")) {
			@Override
			public boolean isVisible() {
				return true;
			}
			@Override
			public void updateModel() {
				FroalaField.this.updateModel();
				super.updateModel();
			}
		};
		
	
		texteditor.add(new FroalaBehavior() {
			@Override
			public void onInit(AjaxRequestTarget target) {
				initialized=true;
				target.add(FroalaField.this.get("froala-toolbar"));
				if (isEditionEnabled())
				onOpen(target);
			}
			@Override
			public void beforeRender(Component component) {
			}
			@Override
			public boolean initOnClick() {
				return !open;
			}
			@Override
			protected String getImagesUrl() {
				return FroalaField.this.getImagesUrl();
			}
			@Override
			public boolean isEditionEnabled() {
				return FroalaField.this.isEditionEnabled();
			}
			@Override
			protected String getPlaceHolderText() {
				try {
					return (new StringResourceModel("placeHolder", FroalaField.this, null)).getObject();
				}
				catch (MissingResourceException e) {
					return null;
				}
			}
		});
		
		texteditor.setOutputMarkupId(true);
		
		return texteditor;
	}
	
	public boolean isEditionEnabled() {
		return (getEditor()!=null && getEditor().isEditionEnabled()) || getEditor()==null;
	}
	
	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		initialized=false;
		WebMarkupContainer toolbar = new WebMarkupContainer("froala-toolbar");
		WebMarkupContainer linkcontainer = new WebMarkupContainer("link-container") {
			@Override
			public boolean isVisible() {
				return initialized && includeClose() && isEditionEnabled();
			}
		};
		linkcontainer.add(new AjaxLink<Void>("close-link") {
			public void onClick(AjaxRequestTarget target) {
				onClose(target);
			}
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				updateAjaxCloseAttributes(attributes);
			}
		});
		toolbar.add(linkcontainer);
		toolbar.setOutputMarkupId(true);
		addOrReplace(toolbar);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
	}
	
	protected String getImagesUrl() {
		return null;
	}
	
	protected boolean includeClose() {
		return true;
	}
	
	protected Content getContent() {
		return null;
	}
}
