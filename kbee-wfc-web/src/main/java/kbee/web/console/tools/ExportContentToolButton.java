package kbee.web.console.tools;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.entity.Person;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.console.event.SelectionEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;

import kbee.web.console.BaseBrowser;
import kbee.web.query.ListModelQuery;

public abstract class ExportContentToolButton<T extends com.novamens.dom.Object> extends ToolbarItem {
			
	static IModel<String> default_icon = new Model<String> ("far fa-cloud-download-alt fa-fw");
	
	private static final long serialVersionUID = 1L;

	final boolean is_root = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_domain_admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_support = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());

	boolean user_can_not_download=true; 
	
	private IModel<String> icon_css = default_icon;
	private ListModelQuery<T> list;
	
	
	public ExportContentToolButton(BaseBrowser<?> browser, Align align, boolean isicon) {
		super(browser, align, isicon);
		setOutputMarkupId(true);
		add(new WicketEventListener<SelectionEvent>() {
			private static final long serialVersionUID = 1L;
			public void onEvent(SelectionEvent event) {
				event.getRequestTarget().add(ExportContentToolButton.this);
			}
		});
	}
	

	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		if (get("link")==null) {
			addLink();
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ListModelQuery<T> getListModel() {
		if (list==null)
			list = new ListModelQuery(getBrowser().getSelection());
		return list;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (list!=null) {
			for(IModel<T> model:list.getListModel())
				model.detach();
		}
	}
	
	protected void addLink() {
		
		AjaxLink<Void> link = new AjaxLink<Void>("link") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				ExportContentToolButton.this.onClick(target);
			}
			@Override
			public boolean isEnabled() {
				return ExportContentToolButton.this.isEnabled();
			}
		};
		
		
		if (getAnchorTitle()!=null)
			link.add(new AttributeModifier("title", getAnchorTitle()));
		
		WebMarkupContainer icon = new WebMarkupContainer("icon") {
			private static final long serialVersionUID = 1L;
			public boolean isVisible() {
				return isIcon() && (getIconCss()!=null);
			}
		};
	
		if (getIconCss()!=null)
			icon.add(new AttributeModifier("class", getIconCss()));
		link.add(icon);
		add(link);
		
	}

	@Override
	public boolean isEnabled() {
		
		if (super.getBrowser().getSelection().isEmpty())
			return false;
		
		if (!getPerson().getProfile(UserProfile.class).isSendFilesEmail() && !is_root)
			return false;
		
		if (!is_root && is_support)
			return false;
		
		return true;
		
		
	}
	
	protected Person getPerson() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getPerson();
	}

	protected IModel<String> getIconCss() {
		return icon_css; 
	}
	
	protected abstract void onClick(AjaxRequestTarget target);	
	
}
