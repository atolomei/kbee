package kbee.web.eform;


import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.content.form.EFormContentData;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;
import com.novamens.content.text.TextPart;
import com.novamens.kbee.content.text.KbeeText;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.wicket.markup.html.panel.KBPanel;


@SuppressWarnings("serial")
public class EHtmlStructViewer extends KBPanel {
	private static final long serialVersionUID = 1L;
	
	
	private IModel<EFormData> datamodel;
	private IModel<EFormField<?>> fieldmodel;
	
	WebMarkupContainer container;
	WebMarkupContainer textcontainer;
	
	private static final ResourceReference KBEE_TEXT_CSS = new CssResourceReference(EHtmlStructFieldPanel.class, "EHtmlText.css");
	
	
	public class IndexFragment extends Fragment {
		public IndexFragment(String id) {
			super(id, "index-fragment", EHtmlStructViewer.this);
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
					        script="scrollTextTo('"+name+"')";
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
	
	public EHtmlStructViewer(String id, EFormField<?> field, IModel<EFormData> data) {
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
		
		Label label = new Label("label", getField().getLabel());
		
		IModel<String> textmodel = new HtmlStructModel(
			() -> (String)getDataModel().getObject().getData(getField()),
			() -> ((EFormContentData)getData()).getContent());
	
//		IModel<String> textmodel = new Model<String>() {
//			@SuppressWarnings("unchecked")
//			public String getObject() {
//				Text text = KbeeText.textOf((String)getDataModel().getObject().getData(getField()));
//				String strvalue = text.getText(new AncordResolver() {
//					@Override
//					public Element resolve(Element ancord) {
//						String href = ancord.getAttribute("href");
//						if (href.contains("?include")) {
//							Element html = getTextPart(ancord);
//							if (html!=null) {
//							Node i = ancord.getOwnerDocument().importNode(html, true);
//							ancord.getParentNode().replaceChild(i, ancord);
//							}
//						}
//						else {
//							if (href!=null) {
//								href = "/id/"+href;
//								ancord.setAttribute("href", href);
//								ancord.setAttribute("target", "_blank");
//							}
//						}
//						return ancord;
//					}
//				}, new ImageResolver() {
//					@Override
//					public Element resolve(Element image) {
//						String src = image.getAttribute("src");
//						Content content = ((EFormContentData)getData()).getContent();
//						src = "/resource/content/"+(new ContentId(content).toString()) +"/" + src;
//						image.setAttribute("src", src);
//						return image;
//					}
//				});
//				return strvalue;
//			}
//			protected boolean isEmpty(Text text) {
//				if (text==null || text.asString()==null)
//					return true;
//				String value = text.asString();
//				value = value.replace("<p class=\"last\">","");
//				value = value.replace("<p>","");
//				value = value.replace("</p>","");
//				value = value.replace("<br>","");
//				value = value.replace("<br/>","");
//				if ("".equals(value.trim()))
//					return true;
//				return false;
//			}
//		};
		
		Label text = new Label("text", textmodel);
		
		text.setEscapeModelStrings(false);
		
		container.add(label);
		textcontainer.add(text);
		
		container.add(textcontainer);
		
		container.add(new IndexFragment("index"));
	}	
	
	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(KBEE_TEXT_CSS));
		response.render(JavaScriptHeaderItem.forScript(getFieldJS(), "structhtmlfield"));
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
	
	protected String getFieldJS() {
		StringBuffer script = new StringBuffer();

		String id = textcontainer.getMarkupId();
		
        String s =	"function scrollTextTo(id) {"+
        	"$('#"+id+"').scrollTop($('#'+id).offset().top);"+
        "}";
        
		script.append(s);
		
		return script.toString();
	}
	
} 
