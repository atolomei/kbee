package kbee.web.search;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.Response;

import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;

import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.browser.TopPanelEvent;
import com.novamens.kbee.wicket.markup.html.console.panel.ConsoleAjaxSubmitLink;
import com.novamens.kbee.wicket.markup.html.event.FilterSelectorClearAllEvent;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.panel.KBPanel;

import kbee.web.console.ConsoleAjaxIndicatorAppender;

@SuppressWarnings("serial")
public class SearchPanel extends KBPanel {
	private static final long serialVersionUID = 1L;
	
	String indicatorLabel;
	String placeholder;
	private  String name;
	boolean isAdvanced=false;
	boolean isClearAllVisible = true;
	
	
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
				}
			};
			add(indicatorAppender);
		}
		public String getAjaxIndicatorMarkupId() {
			return indicatorAppender.getMarkupId();
		}
	}

	public SearchPanel(String id) {
		this(id, null, null, false, false);
	}

	public SearchPanel(String id, String name) {
			this(id, name, null, false, false);
	}
	
	
	/**
	 * 
	 * @param id
	 * @param placeholder
	 */
	public SearchPanel(String id, String name, String placeholder, boolean isAdvanced, boolean isClearAll) {
		super(id);
		this.isAdvanced=isAdvanced;
		this.isClearAllVisible=isClearAll;
		this.placeholder= (placeholder!=null?placeholder:"");
		this.name=name!=null?name:this.getClass().getName();
	}
	

	
	
	
	public void onInitialize() {
		super.onInitialize();
		
		super.setOutputMarkupId(true);
		
		add(new SearchForm("searchform"));
		
		if (this.isAdvanced)
			add(new AdvancedSearchButtonFragment("advancedsearch", this.name));
		else
			add(new InvisiblePanel("advancedsearch"));
		
		AjaxLink<Void> clearall = new AjaxLink<Void>("clear-all") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				target.add(SearchPanel.this);
				fire(new FilterSelectorClearAllEvent(target));
			}
			
			public boolean isVisible() {
				return SearchPanel.this.isClearAllVisible();
			}
		};
		add(clearall);
	}
	
	
	
	
	
	public void setClearAllVisible(boolean b) {
		this.isClearAllVisible=b;
	}
	
	protected boolean isClearAllVisible() {
		return this.isClearAllVisible;
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
			text = text.replace("/", "");
		}
	}
	
	
	//public String getIndicatorLabel() {
	//	if (indicatorLabel==null)
	//		indicatorLabel=new StringResourceModel("searchaction.workinglabel", SearchPanel.this, null).getString();
	//	return indicatorLabel;
	//}
	
	public Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	
	
	
	
	/**
	 * 
	 * 
	 * 
	 * 
	 *
	 */
	public class AdvancedSearchButtonFragment extends Fragment {
					
		private boolean is_up = true;
		private String name;
		
		public AdvancedSearchButtonFragment  (String id, String name) {
			super(id, "advancedsearch-fragment", SearchPanel.this);
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
			
			AjaxLink<Void> ln = new AjaxLink<Void>("advancedsearch") {
				private static final long serialVersionUID = 1L;
				@Override
				public void onClick(AjaxRequestTarget target) {
					is_up=!is_up;
					target.add(SearchPanel.this);
					fire(new TopPanelEvent(target));
				}
			};
			
			add(ln);
			
			/**WebMarkupContainer icon = new WebMarkupContainer("icon");
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
			**/
		}
	}
	
	
	
	protected KbeeUser getSessionUser() {
		return (KbeeUser) ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();
	}
	
	protected String getPreference(String name, String key) {
		return getSessionUser().getService(PreferencesService.class).getValue( key + "-browser", name);
	}
		
	
	
	
}
