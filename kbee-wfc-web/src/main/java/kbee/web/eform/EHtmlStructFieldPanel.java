package kbee.web.eform;

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.content.base.Content;
import com.novamens.content.form.EFormContentData;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.ValueUpdated;
import com.novamens.content.model.ContentId;
import com.novamens.content.text.TextPart;
import com.novamens.kbee.content.form.KbeeEHtmlField;
import com.novamens.kbee.content.text.KbeeText;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.portal6.model.Site;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.form.Field;
import com.novamens.wicket.markup.html.form.TextField;

import kbee.web.searcher.page.SearcherDetailDocumentPage;
import kbee.wicket.tinymce.TinyField;

@SuppressWarnings("serial")
public class EHtmlStructFieldPanel extends EFieldPanel<KbeeEHtmlField> {
	private static final long serialVersionUID = 1L;
	
	
	private static final ResourceReference KBEE_TEXT_CSS = new CssResourceReference(EHtmlStructFieldPanel.class, "EHtmlText.css");

	private boolean html = false;
	private boolean index = false;
	private WebMarkupContainer textcontainer;
	
	public class IndexFragment extends Fragment {
		public IndexFragment(String id) {
			super(id, "index-fragment", EHtmlStructFieldPanel.this);
			setOutputMarkupId(true);
		}
		@Override
		public void onBeforeRender() {
			super.onBeforeRender();
			addOrReplace(getView());
		}
		@Override
		public void onInitialize() {
			super.onInitialize();
			add(new WicketEventListener<EAjaxFormEvent>() {
				@Override
				public void onEvent(EAjaxFormEvent event) {
					if (event.getRequestTarget()!=null) {
						event.getRequestTarget().add(IndexFragment.this);
					}
				}
			});
			
		}
		protected Component getView() {
			return new ListView<TextPart>("index-view", getParts()) {
				public void populateItem(ListItem<TextPart> item) {
					AjaxLink<Void> link = new AjaxLink<Void>("link") {
						public void onClick(AjaxRequestTarget target) {
							String script;
							String name = item.getModelObject().getName();
							if (html) {
								script = "setCursor('#"+ name + "');";
								script +="$(tinymce.activeEditor.getBody()).find('#"+name+"').get(0).scrollIntoView(false);";
							}
							else {
						        script="scrollTextTo('"+name+"')";
							}
							target.appendJavaScript(script);
						}
					};
					link.add(new Label("title", item.getModelObject().getTitle()));
					item.add(link);
				}
			};
		}
		protected List<TextPart> getParts() {
			String text = (String)getData().getData(getField());
			KbeeText ktext = new KbeeText(text);
			List<TextPart> parts = ktext.getParts();
			return parts;
		}
	}
			

	public EHtmlStructFieldPanel(String id, KbeeEHtmlField field, IModel<EFormData> data) {
		super(id, field, data);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer fieldcontainer = new WebMarkupContainer("field-container");
		
		fieldcontainer.add(new TinyField("field", new FieldDataModel<KbeeEHtmlField, String>(getFieldModel(), getDataModel()), 60, 80) {
			@Override
			public IModel<String> getLabel() {
				return new Model<String>("");	
			}
			@Override
			public IModel<String> getSubtitle() {
				return new Model<String>(getField().getSublabel() ); 
			}
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				super.onUpdate(target);
				updateModel();
				fireScanAll(new EAjaxFormEvent(target, getField(), getData()));
			}
			public void onClose(AjaxRequestTarget target) {
				html = false;
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
			public boolean isInputEnabled() {
				return super.isInputEnabled() && 
					!getField().isReadOnly() && 
					getField().isEnabled(getData()) && 
					getData().getForm().isEnabled();
			}
			@Override
			public boolean isRequired() {
				return getField().isRequired();
			}
			@Override
			public Disposition getDisposition() {
				return EHtmlStructFieldPanel.this.getDisposition();
			}
			@Override
			public boolean isHelpVisible() {
				return getField().getModel().getMetainfoMessage()!=null;
			}
			@Override
			protected String getBaseUrl() {
				return getFormObject() instanceof Content 
					? "/resource/content/"+ (new ContentId((Content)getFormObject())).toString() +"/"
					: null;
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
		
		fieldcontainer.add(new AttributeModifier("class", new Model<String> () {
			public String getObject() {
				return index ? "col-lg-9 col-md-7 col-xs-12" : "col-lg-12 col-md-12 col-xs-12";
			}
		}));
		
		getContainer().add(fieldcontainer);
		
		getContainer().add(new IndexFragment("index") {
			public boolean isVisible() {
				return index;
			}
		});
		
		IModel<String> textmodel = new HtmlStructModel(
				() -> ((Field<String>)getInput()).getValue(),
				() -> ((EFormContentData)getData()).getContent());
		
		WebMarkupContainer toolbar = new WebMarkupContainer("toolbar");
		
		toolbar.add(new AjaxLink<Void>("index-switch") {
			public void onClick(AjaxRequestTarget target) {
				index = !index;
				target.add(getContainer());
			}
		});
		

		Link<Void>  ln = new Link<Void>("portalpreview") {
			@Override
			public void onClick() {
				// IModel<T> model
				//PageParameters pa = new PageParameters();
				//pa.add("oid", "2765952");
				//pa.add("siteurl", "bcv");
				//setResponsePage(new SearcherDetailDocumentPage(pa));
				setResponsePage(new RedirectPage("http://localhost:8080/portal/bcv/doc/2765952"));
			}
		};
		
		toolbar.add(ln);
		
		
		
		textcontainer = new WebMarkupContainer("text-container") {
			@Override
			public boolean isVisible() {
				return !html;
			}
		};
		
		textcontainer.add(new AttributeModifier("class", new Model<String> () {
			public String getObject() {
				return index ? "col-lg-9 col-md-7 col-xs-12" : "col-lg-12 col-md-12 col-xs-12";
			}
		}));
		
		Label label = new Label("label", getField().getLabel());
		
		Label text = new Label("text", textmodel);
		
		text.add(new AjaxEventBehavior("click") {
			@Override
			protected void onEvent(AjaxRequestTarget target) {
				if (!getField().isReadOnly() && 
						getField().isEnabled(getData()) && 
						getData().getForm().isEnabled()) {
					html = true;
					//target.appendJavaScript("setTimeout(\"tinyMCE.activeEditor.focus();\", 800)");
					target.add(getContainer());
				}
			}
		});
		
		text.setEscapeModelStrings(false);
		
		getContainer().add(label);
		textcontainer.add(text);
		
		getContainer().add(toolbar);
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
	
	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(KBEE_TEXT_CSS));
		response.render(JavaScriptHeaderItem.forScript(getFieldJS(), "structhtmlfield"));
	}
	
	public Field<?> getInput() {
		return (Field<?>)getContainer().get("field-container:field");
	}

	
	protected String getFieldJS() {
		StringBuffer script = new StringBuffer();

		String id = textcontainer.getMarkupId();
		
        String s =	"function scrollTextTo(id) {"+
        	//"$('html, body').scrollTop($('#'+id).offset().top-60);"+
        	"$('#"+id+"').scrollTop($('#'+id).offset().top-500);"+
        "}";
        
		script.append(s);
		
		s= "function fonte(e){"+
		"	let elemento = $(\"#"+id+"\");"+
		"	let fonte = elemento.css('font-size');"+
		"	if (e == 'a') {"+
		"		elemento.css(\"fontSize\", parseInt(fonte) + 1);"+
		"	} else if('d'){"+
		"		elemento.css(\"fontSize\", parseInt(fonte) - 1);"+
		"	}"+
		"}";
		script.append(s);
		
		return script.toString();
	}
}