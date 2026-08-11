package kbee.web.dashboard;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.EntityMember;
import com.novamens.content.security.EntityRole;
import com.novamens.content.security.Role;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserRole;
import com.novamens.content.user.UserService;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.dataset.DashboardDataSetMembersHomePage;
import kbee.web.error.ErrorPanel;
import kbee.web.help.InlineHelpWebService;

public class DashboardDatasetEntititesWidgetPanel extends DashboardDatasetMembersWidgetPanel {
			
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DashboardDatasetEntititesWidgetPanel.class.getName());
	
	static final private int MAX=30;
	
	private List<IModel<DataSet>> entitiessets;

	public DashboardDatasetEntititesWidgetPanel(String id, String preferences_key) {
		super(id, preferences_key);
	}
	
	public List<IModel<DataSet>> getDataSets() {
		if (entitiessets==null) {
			entitiessets = new ArrayList<IModel<DataSet>>();
			for (DataSet ds : getContentDao().getDataSets(getDomain().getId(), ObjectState.ENABLED)) {
				if (ds.getDataSetType()==DataSetType.ENTITY && hasHome(ds) && hasRole(ds))
					entitiessets.add( new ObjectModel<DataSet>(ds));
			} 
		}
		return entitiessets;
	}
	 
	

	protected WebMarkupContainer getHelpPanel() {

		InlineHelpWebService se = ServiceLocator.getService(InlineHelpWebService.class);
		
			WebMarkupContainer pa = se.getPanel("help", getLocale(), InlineHelpWebService.HOME_DATASETMEMBERS_ENTITIES);
			if (pa!=null) return pa;
			return new ErrorPanel("help", new Model<String>(InlineHelpWebService.HOME_DATASETMEMBERS_ENTITIES));
	
	}
	
	
	
	protected String getItemLabel(IModel<DataSet> value) {
			return getLabel("mini-site", value.getObject().getDisplayName()).getObject();
	}
	
	
	public void onDetach() {
		super.onDetach();
		if (entitiessets!=null) 
			entitiessets.forEach(item -> item.detach());
	}
	
	public void onInitialize() {
		super.onInitialize();
		setBottomPanel(new InvisiblePanel("base-bottom"));
	}

	


	@Override
	protected void setItems() {
		List<IModel<DataSetMember>> items = new ArrayList<IModel<DataSetMember>>();
		KbeeUser us = (KbeeUser) getSessionUser();
		for (DataSetMember member : us.getService(UserDashboardService.class).getDataSetMembers(getDataSet(), MAX)) {
			if (member instanceof EntityMember) {
				if (hasRole((EntityMember)member)) items.add(new ObjectModel<DataSetMember>(member));
			}
		}
		setSize(items.size());
		setItems(items);
	}
	
	@Override
	protected void onClick(IModel<DataSetMember> model, int index) {
		DataSetMember entity = model.getObject();
		String uri ="/entityhome/"+String.valueOf(entity.getId())+"/";
		for (Classifier classifier : getContentDao().getClassifiers(entity.getDomain())) {
			if (classifier.getDataSet().equals(entity.getDataSet()) && classifier.hasHome()) {
				uri += String.valueOf(classifier.getId());
				break;
			}
		}
		setResponsePage(new RedirectPage(uri));
	}
	
	private boolean hasHome(DataSet ds) {
		for (Classifier classifier : getContentDao().getClassifiers(ds.getDomain())) {
			if (classifier.getDataSet()!=null && classifier.getDataSet().equals(ds) && classifier.hasHome()) {
				return true;
			}
		}
		return false;
	}
	
	// El usuario tiene algun rol en entidades del dataset
	private boolean hasRole(DataSet ds) {
		if (ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId())) {
			return true;
		};
		UserProfile usersessionprofile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		for (UserRole userrole : usersessionprofile.getRoles()) {
			Role role = userrole.getRole();
			if (role.isEntity()) {
				EntityRole entityrole = (EntityRole)getContentDao().unproxy(role); 
				if (entityrole.getClassifier().getDataSet().equals(ds) && entityrole.getClassifier().hasHome()) {
					return true;
				}
			}
		}
		return false;
	}
	
	// El usuario tiene algun rol en la entidad
	private boolean hasRole(EntityMember entity) {
		if (ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId())) {
			return true;
		};
		UserProfile usersessionprofile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		for (UserRole userrole : usersessionprofile.getRoles()) {
			Role role = userrole.getRole();
			if (role.isEntity()) {
				if (userrole.getEntity().equals(entity)) {
					return true;
				}
			}
		}
		return false;
	}
}