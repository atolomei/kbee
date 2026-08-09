package kbee.web.security;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

import com.novamens.content.base.Content;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.SecuredMember;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.service.DataSetMemberService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.error.ApplicationErrorPage;
import kbee.web.page.ApplicationPage;
import kbee.web.page.PageContentHeaderPanel;

public class SecuredMemberAclPage extends ApplicationPage<DataSetMember> {
	private static final long serialVersionUID = 1L;

	
	public SecuredMemberAclPage() {
	}
	
	public SecuredMemberAclPage(PageParameters parameters) {
		DataSetMember member = getMember(parameters);
		if (member!=null) {
			setModel(new ObjectModel<DataSetMember>(member));
			setTopNavigation(getMainTopbar());  
			setMenu(getMainLaternalMenu());
			setLogVisit(true);
		}
	}

	public SecuredMemberAclPage(IModel<DataSetMember> model) {
		setModel(model);
		setTopNavigation(getMainTopbar());  
		setMenu(getMainLaternalMenu());
		setLogVisit(true);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		if (hasPermissions()) {
			DataSetMember member = getModelObject();
			setPageTitle(member.getDisplayName());
			PageContentHeaderPanel<Content> panel = 
				new PageContentHeaderPanel<Content>(null);
			panel.setTitle(getPageTitle());
			panel.setSubLine(member.getService(DataSetMemberService.class).getSubline());
			setSearchPanel(false);
			setAdvancedSearch(false);
			setSuggester(false);
			setPageContentHeader(panel);
			getPageParameters().add("id", String.valueOf(member.getId()));
			
			add(new SecuredMemberAclEditor("acl-panel", getModel(), false));
			
		} 
		else {
			setResponsePage(
				new ApplicationErrorPage<Content>(new Model<String>("Permission denied")));
		}
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
	}
	
	@Override
	public boolean hasPermissions() {
		return getModel()!=null && 
			getModel().getObject()!=null && 
			ServiceLocator
				.getService(ContentSystemSecurityService.class)
				.isWriteable((SecuredMember)getModel().getObject());
	}
	
	protected DataSetMember getMember(PageParameters parameters) {
		DataSetMember   member = null;		
		StringValue id = parameters.get("id");
		if (!id.isNull() && !id.isEmpty()) { 
			member = getContentDao().findMemberById(id.toLong());
		}	
		return member;
	}
}