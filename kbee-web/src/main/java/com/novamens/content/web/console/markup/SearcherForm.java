package com.novamens.content.web.console.markup;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.Response;

import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.indexer.query.Suggestion;
import com.novamens.kbee.wicket.markup.html.console.panel.ConsoleAjaxSubmitLink;
import com.novamens.service.ServiceLocator;

import kbee.web.console.ConsoleAjaxIndicatorAppender;

@SuppressWarnings("serial")
public class SearcherForm extends Panel {
	private static final long serialVersionUID = 1L;

	String indicatorLabel;
	String placeholder;
	
	private class AjaxIndicatorSubmitLink extends ConsoleAjaxSubmitLink implements IAjaxIndicatorAware {
		private ConsoleAjaxIndicatorAppender indicatorAppender;
		
		public ConsoleAjaxIndicatorAppender getIndicatorAppender() {
			return indicatorAppender;
		}
		public AjaxIndicatorSubmitLink(String id, final Form<?> form) {
			super(id, form);
			indicatorAppender = new ConsoleAjaxIndicatorAppender() {
				@Override
				public void afterRender(final Component component)	{
					final Response r = component.getResponse();
					r.write("<span  class=\"working-indicator\"");
					r.write(getSpanClass());
					r.write("\" ");
					r.write("id=\"");
					r.write(getMarkupId());
					r.write("\">");
					r.write(getIndicatorLabel()+"</span>");
				}
			};
		   add(indicatorAppender);
		}
		public String getAjaxIndicatorMarkupId() {
			return indicatorAppender.getMarkupId();
		}
	}

	public SearcherForm(String id, String placeholder) {
		super(id);
		this.placeholder=placeholder;
		add(new SearchForm("searchform"));
	}

	public String getPlaceHolder() {
		return this.placeholder;
	}

	public void setPlaceHolder(String str) {
		this.placeholder=str;
	}

	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		if(getPlaceHolder()!=null)
			(get("searchform:text")).add(new AttributeModifier("placeholder", getPlaceHolder()));
	}

	public class SearchForm extends Form<String> {
		private static final long serialVersionUID = 1L;
		private String text;
			
		public SearchForm(String id) {
			super(id);
			TextField<String> text = new TextField<String>("text", new PropertyModel<String>(this, "text"));
			
			text.setMarkupId(id);
			
			add(text);
			
			AjaxIndicatorSubmitLink searchbutton = new AjaxIndicatorSubmitLink("search", this) {
				@Override
				public void onSubmit(AjaxRequestTarget target) {
					getIndicatorAppender().setShow(true);
					onSearch(target, getText());
				}
			};
			setDefaultButton(searchbutton);
			add(searchbutton);
		}
		public String getText() {
			return text;
		}
		public void setText(String text) {
			this.text = text;
		}
	}
	
	public void onSearch(AjaxRequestTarget target, String text) {
		if (text!=null) {
			text = text.replace("[", "");
			text = text.replace("{", "");
			text = text.replace("]", "");
			text = text.replace("}", "");
			text = text.replace("-", "");
		}
	}
	
	public void onSearch(AjaxRequestTarget target, Suggestion suggestion) {
	}
	
	public String getIndicatorLabel() {
		if (indicatorLabel==null)
			indicatorLabel=new StringResourceModel("searchaction.workinglabel", SearcherForm.this, null).getString();
		return indicatorLabel;
	}
	
	public Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
}
