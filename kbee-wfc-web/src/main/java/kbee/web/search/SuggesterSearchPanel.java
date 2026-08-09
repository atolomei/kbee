package kbee.web.search;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.Response;

import com.novamens.content.user.UserService;
import com.novamens.indexer.query.Cursor;
import com.novamens.indexer.query.Suggestion;

import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.ajax.WorkingIndicatorAjaxLinkV5;
import com.novamens.kbee.wicket.markup.html.console.browser.TopPanelEvent;
import com.novamens.kbee.wicket.markup.html.console.panel.ConsoleAjaxSubmitLink;
import com.novamens.kbee.wicket.markup.html.event.FilterSelectorClearAllEvent;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.panel.KBPanel;

import kbee.web.console.ConsoleAjaxIndicatorAppender;

/**
 *  kbee.web.search.SuggesterSearchPanel
 * 
 * 
 *
 */
@SuppressWarnings("serial")
public class SuggesterSearchPanel extends KBPanel {
			
	private static final long serialVersionUID = 1L;
	
	//private  String indicatorLabel;
	private  String placeholder;
	private  String name;
	
	boolean isAdvanced=false;
	boolean isClearAll=false;
	
	IModel<Cursor> cursor_model;

	public SuggesterSearchPanel(String id) {
		super(id);
	}
	
	public SuggesterSearchPanel(String id, String name) {
		this(id, name, null, false, false);
		this.placeholder=new StringResourceModel("search", this, null).getObject();
	}
	
	public SuggesterSearchPanel(String id, String name, String placeholder) {
			this(id, name, placeholder, false, false);
	}
	
	public SuggesterSearchPanel(String id, String name, String placeholder, boolean isAvanced, boolean isClearAll) {
		super(id);
		this.placeholder=placeholder;
		this.isClearAll=isClearAll;
		this.name=name;
		this.isAdvanced=isAvanced;
	}
	
	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);
	}
	

	public void onDetach() {
		super.onDetach();
	
		if (cursor_model!=null)
			cursor_model.detach();
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		super.setOutputMarkupId(true);
		
		
		if (cursor_model==null || cursor_model.getObject()==null)
			add(new InvisiblePanel("navigator"));
		else
			add(new SearcherNavigatorPanel<>("navigator",  cursor_model));
		
		add(new XSearchForm("searchform"));
		
		if (this.isAdvanced)
			add(new AdvancedSearchButtonFragment("advancedsearch", name));
		else
			add(new InvisiblePanel("advancedsearch"));
		
		WorkingIndicatorAjaxLinkV5<Void> clearall = new WorkingIndicatorAjaxLinkV5<Void>("clear-all", new StringResourceModel("clear-all", this, null).getString()) {
			@Override
			public void onClick(AjaxRequestTarget target) {
				target.add(SuggesterSearchPanel.this);
				fire(new FilterSelectorClearAllEvent(target));
			}
			
			@Override
			public boolean isVisible() {
				return SuggesterSearchPanel.this.isClearAll;
			}
			
			@Override
			public String getWorkingLabel() {
				return " ";  
			}
		};
		add(clearall);

		
	}
	

	public String getPlaceHolder() {
		return this.placeholder;
	}

	@SuppressWarnings("unchecked")
	public void setPlaceHolder(String str) {
		this.placeholder=str;
		if (get("searchform:text")!=null) {
			((SuggesterSearchField<String>) get("searchform:text")).setPlaceHolder(new Model<String>(str));
		}
	}

	

	public class XSearchForm extends Form<String> {
		private static final long serialVersionUID = 1L;
		private String text;
			
		public XSearchForm(String id) {
			super(id);
			
			SuggesterSearchField<String> text = new SuggesterSearchField<String>("text", new PropertyModel<String>(this, "text")) {
				@Override
				public void onSearch(AjaxRequestTarget target, Suggestion suggestion) {
					SuggesterSearchPanel.this.onSearch(target, suggestion);
				}
				@Override
				public List<Suggestion> getSuggestions(String pattern) {
					return SuggesterSearchPanel.this.getSuggestions(filter(pattern)); 
				}
				@Override
				protected boolean includeInfo() {
					return SuggesterSearchPanel.this.includeInfo(); 
				}
				@Override
				protected String getInfo(Suggestion suggestion) {
					return SuggesterSearchPanel.this.getInfo(suggestion); 
				}
			};
			
			if (getPlaceHolder()!=null)
				text.setPlaceHolder(new Model<String>(getPlaceHolder()));	
			
			text.setMarkupId(id);
			
			add(text);
			
			AjaxIndicatorSubmitLink searchbutton = new AjaxIndicatorSubmitLink("search", this) {
				@Override
				public void onSubmit(AjaxRequestTarget target) {
					getIndicatorAppender().setShow(true);
					onSearch(target, filter(getText()));
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
			text = filter(text);
		}
	}
	
	public void onSearch(AjaxRequestTarget target, Suggestion suggestion) {
	}
	
	public boolean suggestions() {
		return false;
	}
	
	//public String getIndicatorLabel() {
	//	if (indicatorLabel==null)
	//		indicatorLabel=new StringResourceModel("searchaction.workinglabel", SuggesterSearchPanel.this, null).getString();
	//	return indicatorLabel;
	//}
	
	
	protected List<Suggestion> getSuggestions(String pattern) {
		return new ArrayList<Suggestion>();
	}
	
	protected boolean includeInfo() {
		return false;
	}
	
	protected String getInfo(Suggestion suggestion) {
		return null;
	}
	
	protected String filter(String text) {
		if (text!=null) {
			text = text.replace("[", "");
			text = text.replace("{", "");
			text = text.replace("]", "");
			text = text.replace("}", "");
			if (text.startsWith("-")) 
				text = text.replace("-", " ").trim();
		
			if ("".equals(text.trim()))
				text = null;
		}
		return text;
	}
	
	protected KbeeUser getSessionUser() {
		return (KbeeUser) ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();
	}
	
	protected String getPreference(String name, String key) {
		return getSessionUser().getService(PreferencesService.class).getValue( key + "-browser", name);
	}
	
	
	/**
	 * 
	 * 
	 * 
	 * 
	 *
	 */
	public class AdvancedSearchButtonFragment extends Fragment {
					
		private boolean is_up = false;
		private String name;
		
		public AdvancedSearchButtonFragment  (String id, String name) {
			super(id, "advancedsearch-fragment", SuggesterSearchPanel.this);
			this.name=name;
			String top_preference =	getSessionUser().getService(PreferencesService.class).getValue(this.name+"-browser", "toppanel");
			if (top_preference!=null && !"none".equals(top_preference))
				is_up=false;
			else
				is_up=true;
		}
		
		/**
		 * 
		 * 
		 */
		@Override
		public void onInitialize() {
			super.onInitialize();

			setOutputMarkupId(true);
			
			
			/**
			WorkingIndicatorAjaxLinkV5<Void> ln = new WorkingIndicatorAjaxLinkV5<Void>("advancedsearch",  new StringResourceModel("advancedsearch", this, null).getString()) {
				private static final long serialVersionUID = 1L;
				@Override
				public void onClick(AjaxRequestTarget target) {
					is_up=!is_up;
					target.add(SuggesterSearchPanel.this);
					fire(new TopPanelEvent(target));
				}
				
				@Override
				public String getWorkingLabel() {
					return " ";
				}
			};
			**/
			
			
			AjaxLink<Void> ln = new AjaxLink<Void>("advancedsearch") {
				private static final long serialVersionUID = 1L;
				@Override
				public void onClick(AjaxRequestTarget target) {
					is_up=!is_up;
					target.add(SuggesterSearchPanel.this);
					fire(new TopPanelEvent(target));
				}
			};
			
			add(ln);
			
			/**
			WebMarkupContainer icon = new WebMarkupContainer("icon");
			icon.setVisible(false);
			icon.add( new AttributeModifier("class", new Model<String>() {
				private static final long serialVersionUID = 1L;
				@Override
				public String getObject() {
					if (is_up)
						return "far fa-angle-up";
					else
						return "far fa-angle-down";
				}
			}));
			ln.add(icon);
			*/
		}
	}
	
	
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
					r.write("</span>");
					//r.write(getIndicatorLabel()+"</span>");
				}
			};
		   add(indicatorAppender);
		}
		public String getAjaxIndicatorMarkupId() {
			return indicatorAppender.getMarkupId();
		}
	}
}
