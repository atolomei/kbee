package kbee.web.security;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import com.novamens.content.base.Content;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.error.ApplicationErrorPage;
import kbee.web.page.ApplicationPage;
import kbee.web.page.PageContentHeaderPanel;

public class AclPage extends ApplicationPage<Content> {
	private static final long serialVersionUID = 1L;

	private IModel<Content> model;
	
	public AclPage() {
	}
	
	public AclPage(PageParameters parameters) {
		Content idoc = getContent(parameters);
		if (idoc!=null) {
			setModel(new ObjectModel<Content>(idoc));
			setTopNavigation(getMainTopbar());  
			setMenu(getMainLaternalMenu());
			setLogVisit(true);
		}
	}

	public AclPage(IModel<Content> model) {
		setModel(model);
		setTopNavigation(getMainTopbar());  
		setMenu(getMainLaternalMenu());
		setLogVisit(true);
	}

	
	public IModel<Content> getModel() {
		return model;
	}

	public void setModel(IModel<Content> model) {
		this.model = model;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		if (hasPermissions()) {
			setPageTitle(new Model<String>(getModel().getObject().getTitle()));
			PageContentHeaderPanel<Content> panel=new PageContentHeaderPanel<Content>(null);
			panel.setTitle(getPageTitle());
			panel.setSubLine(getPageDescription());
			setSearchPanel(false);
			setAdvancedSearch(false);
			setSuggester(false);
			setPageContentHeader(panel);
			getPageParameters().add("oid", String.valueOf(getModel().getObject().getOId()));
			getPageParameters().add("id", String.valueOf(getModel().getObject().getId()));
			
			add(new AclPanel<Content>("acl-panel", getModel()));
			
		} 
		else {
			setResponsePage(
				new ApplicationErrorPage<Content>(new Model<String>("Permission denied")));
		}
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (this.model!=null)
			this.model.detach();
	}
	
	@Override
	public boolean hasPermissions() {
		return getModel()!=null && 
			getModel().getObject()!=null && 
			ServiceLocator
				.getService(ContentSystemSecurityService.class)
				.isWriteable(getModel().getObject());
	}
	
	protected Content getContent(PageParameters parameters) {
		Content content = null;		
		StringValue oid = parameters.get("oid");
		if (!oid.isNull() && !oid.isEmpty()) { 
			StringValue id = parameters.get("id");
			if (id.isNull() || id.isEmpty())  
				content = (Content) getContentDao().findContentByOId(Long.valueOf(oid.toString()));
			else {
					content = (Content) getContentDao().findContentById(Long.valueOf(id.toString()));
					if (content.getDomain().getId().equals(getDomain().getId()))
						return content;
					else
						return null;
			}
		}	
		return null;
	}
}