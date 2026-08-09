package com.novamens.kbee.wicket.markup.html.searcher;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.ajax.markup.html.AjaxIndicatorAppender;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.Response;

import com.novamens.indexer.query.Query;


@SuppressWarnings("serial")
public abstract class SearchPanel extends Panel implements IAjaxIndicatorAware {
	private static final long serialVersionUID = 1L;
	private QueryBuilder queryBuilder;

	private String dt;
	private String dl;

	private AjaxIndicatorAppender indicatorAppender = new AjaxIndicatorAppender() {
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
	
	public class AjaxIndicatorSubmitLink extends AjaxSubmitLink implements IAjaxIndicatorAware {
		public AjaxIndicatorSubmitLink(String id, final Form<?> form) {
			super(id, form);
			add(indicatorAppender);
		}
		public String getAjaxIndicatorMarkupId() {
			return indicatorAppender.getMarkupId();
		}
	}
	
	public String getAjaxIndicatorMarkupId() {
		return indicatorAppender.getMarkupId();
	}
	
	public SearchPanel() {
		super("search-panel");
		setOutputMarkupId(true);
		add(new SearchForm("form"));
	}
	
	public class SearchForm extends Form<String> {

		private String text;
		private boolean first = true;
		
		public SearchForm(String id) {
			super(id);
			TextField<String> paneltext = new TextField<String>("text", new PropertyModel<String>(this, "text"))
					 {
				@Override
				 public void onInitialize() {
					super.onInitialize();
					setText(getDefaultText());
					add(new AttributeModifier("class", "text-default"));
				 };
			};
			
			paneltext.add(new AjaxFormComponentUpdatingBehavior("onfocus") {
				  @Override
				  protected void onUpdate(AjaxRequestTarget target) {
		
					  if (first) {
						first=false;
						getParent().add(new AttributeModifier("class", "text-input"));
						setText("");
						target.add(getParent());
					}
				  }
				});
	
			paneltext.add(new AjaxFormComponentUpdatingBehavior("focusout") {
				  @Override
				  protected void onUpdate(AjaxRequestTarget target) {
					  if (!first && getText()==null) {
						   setText(dt);
						   first=true;
						   getParent().add(new AttributeModifier("class", "text-default"));
						   target.add(getParent());
					   }
					}
				  });
			
			paneltext.add(new AttributeModifier("value", "Search"));
			add(paneltext);
			
			AjaxSubmitLink searchLink = new AjaxIndicatorSubmitLink("search", this) {
				@Override
				public void onSubmit(AjaxRequestTarget target) {
					if (!first) 
						onSearch(target, getQuery());
	            }
			};
			searchLink.add(indicatorAppender);
			add(searchLink);
		}
		
		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}
		
		public Query getQuery() {
			Query query = SearchPanel.this.getNewQuery(getText());
			query.getParameters().put("sort", "relevancia");
			return query;
		}
	}

	public Query getNewQuery(String text) {
		return getQueryBuilder().getNewQuery(text);
	}
	
	public void setQueryBuilder(QueryBuilder querybuilder) {
		this.queryBuilder = querybuilder;
	}
	
	public QueryBuilder getQueryBuilder() {
		return this.queryBuilder;
	}

	public String getDefaultText() {
		if (dt==null)
		dt=new StringResourceModel("searchpanel.defaulttext", SearchPanel.this, null).getString();
		return dt;
	}
	
	public String getIndicatorLabel() {
		if (dl==null)								
			dl=new StringResourceModel("searchpanel.workinglabel", SearchPanel.this, null).getString();
			return dl;
	}
	
	public void onSearch(AjaxRequestTarget target, Query query) {
	}
}
