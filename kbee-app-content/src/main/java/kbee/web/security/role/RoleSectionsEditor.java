package kbee.web.security.role;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

import com.novamens.beans.BeansService;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.EntitySet;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.Role;
import com.novamens.content.service.SecurityContentMgmtService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.security.KbeeAbstractRole;
import com.novamens.kbee.security.acl.KbeeGroup;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeArea;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.BrandingService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;
import kbee.web.panel.AlertPanel;

public class RoleSectionsEditor extends ObjectEditor<Role> {
	
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(RoleEditor.class.getName());

	final boolean role_admin	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_security	= role_admin || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId());

	private  GlobalRolesPanel wr;
	private  GlobalRolesPanel cr;
	private  Form<?> form;

	
	public RoleSectionsEditor(String id, IModel<Role> model) {
		super(id, model);
	}

	public RoleSectionsEditor(IModel<Role> model) {
		this("editor", model, false);
	}
	
	public RoleSectionsEditor(String id, IModel<Role> model, boolean isnew) {
		super(id, model);
		setOutputMarkupId(true);
		setIsNew(isnew);
		setEditionEnabled(isnew);
	}

	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
					   form	= new Form<Void>("form", Disposition.VERTICAL);
						 wr	= new GlobalRolesPanel(KbeeArea.WORKFLOW); 		form.add(wr);
						 cr	= new GlobalRolesPanel(KbeeArea.CONTENT);		form.add(cr);
		GlobalRolesPanel gr = new GlobalRolesPanel(KbeeArea.SETTINGS);		form.add(gr); gr.setSettingsGroups(true);		
		GlobalRolesPanel ar = new GlobalRolesPanel(KbeeArea.ADMIN);			form.add(ar);

		
		
		
		
		AlertPanel<Void> pa=new AlertPanel<Void>("main-sections",AlertPanel.INFO,  null, 
				null, 
				getLabel("main-sections"));
		pa.setIcon(AlertPanel.HELP_INFO);
		form.addOrReplace(pa);
		
		
		
		
		
		
		
		
		
		
		AjaxLink<Void> addall_w	= new AjaxLink<Void>("add-all-workflow") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				onAddAllWorkflow(target);
			}
			public boolean isVisible() {
				return false;
				// return isEditionEnabled();

			}
		};
		
		form.add(addall_w);
		
		AjaxLink<Void> addall_c	= new AjaxLink<Void>("add-all-library") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
					onAddAllLibrary(target);
			}
			public boolean isVisible() {
				return false;
				// return isEditionEnabled();
			}

			
		};
		form.add(addall_c);
		
		WebMarkupContainer fa_c= new WebMarkupContainer("factory-container") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public boolean isVisible() {
				return getDomain().getName().equals("kbee");
			}
		};
		form.add(fa_c);
		GlobalRolesPanel fa=new GlobalRolesPanel(KbeeArea.FACTORY);
		fa_c.add(fa);
		
		add(form);
		
		form.add(new EditButtonsV5<Role>(this) {
			private static final long serialVersionUID = 1L;
			@Override
			protected String getCancelClass() {
				return "btn btn-default btn-sm";
			}
			@Override
			protected String getSubmitClass() {
				return "btn btn-primary btn-sm";
			}
			protected String getEditClass() {
				return "btn btn-primary btn-sm";
			}
			@Override
			public boolean isVisible() {
				return true;
			}
			@Override
			public boolean isEnabled()  {
				return true;
			}
		});		
	}

	protected void onAddAllLibrary(AjaxRequestTarget target) {

		//boolean update=false;
		
		for (Group group: getContentSecurityDao().getCanonicalGroups(getDomain())) {
			if (group.isEnabled() && (group.getAreaCode()!=null) && (group.getAreaCode().equals(KbeeArea.CONTENT.getCode()))		) {
				if (!getModel().getObject().getGroups().contains(group)) {
					((KbeeAbstractRole) getModel().getObject()).addGroup(group);
					logger.debug("add canonical group -> " + group.getDisplayName());
					setUpdatedPart("add " + group.getDisplayName());
					//update=true;
				}
			}
	}
		
		cr	= new GlobalRolesPanel(KbeeArea.CONTENT);
	 form.addOrReplace(cr);

		target.add(this);
	}

	protected void onAddAllWorkflow(AjaxRequestTarget target) {
			
		//boolean update=false;
		
		for (Group group: getContentSecurityDao().getCanonicalGroups(getDomain())) {
			
					if (group.isEnabled() && (group.getAreaCode()!=null) && (group.getAreaCode().equals(KbeeArea.WORKFLOW.getCode()))	) 
					{
						if (!getModel().getObject().getGroups().contains(group)) {
							((KbeeAbstractRole) getRole()).addGroup(group);
							logger.debug("add canonical group -> " + group.getDisplayName());
							setUpdatedPart("add " + group.getDisplayName());
							//update=true;
						}
					}
			}

			wr	= new GlobalRolesPanel(KbeeArea.WORKFLOW);
		   form.addOrReplace(wr);

		target.add(this);
		
			
	}
	
	public void onClose(AjaxRequestTarget target) {
	}

	@Override
	public void cancel(AjaxRequestTarget target) {
		if (isNew()) {
			try {
				ServiceLocator.getService(SecurityContentMgmtService.class).delete(getModelObject());
			}
			catch (Exception e) {
				if (logger.isDebugEnabled()) {
					logger.error(e);
				}
				else {
					logger.error(e);
				}	
			}
			onClose(target);
		}
		
		onCancel(target);
	}

	public void edit(AjaxRequestTarget target) {
		super.edit(target);
	}
	

	@Override
	public void onDetach() {
		super.onDetach();
	}

	/**
	 * 
	 */
	@Override
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				KbeeAbstractRole role = getRole();
				ServiceLocator.getService(SecurityContentMgmtService.class).update(role, getUpdatedParts());
				super.reset();
				target.add(RoleSectionsEditor.this.getPage());
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<>(target, e));

		}
	}
	
	
	public KbeeAbstractRole getRole() {
		return (KbeeAbstractRole) getModelObject();
	}
	
	
	public List<Classifier> getClassifiers() {
		List<Classifier> classifiers = new ArrayList<Classifier>();
		for (Classifier classifier : getContentDao().getClassifiers(getDomain())) {
			if (classifier.getDataSet() instanceof EntitySet) {
				classifiers.add(classifier);
			}
		}
		return classifiers;
	}

	protected void onCancel(AjaxRequestTarget target) {
	}

	protected void onAfterSubmit(AjaxRequestTarget target) {
		setEditionEnabled(false);
		target.add(this);
	}

	protected void onUpdate(AjaxRequestTarget target) {
	}
	
//	private Domain getDomain() {
//		return ServiceLocator.getService(UserService.class).getDomain();
//	}

	private Map< Serializable, Serializable> complete_subset;

	/**
	 * @param area
	 * @return
	 */
	public List<Group> getGroups(KbeeArea area) {
		
		List<Group> base = new ArrayList<Group>();
		
		this.complete_subset = new HashMap<Serializable ,  Serializable>();
		
		for (Group group : getContentSecurityDao().getCanonicalGroups(getDomain())) {
			
			boolean isok = false;
			
			if (group.isEnabled()) {
			
				if (!group.isOnlyInternalUse()) {
					if (group instanceof KbeeGroup && area.equals(((KbeeGroup) group).getArea())) {
						isok = true;
					}						
					if (isok) {
						if (group.isOnlyPortal()) {
							 if (isPortalEnabled()) 
								 base.add(group);
						}
						else {
							base.add(group);
						}
					}
				}
			}
		}
		
		List<Group> groups = new ArrayList<Group>();
									 										
		base.forEach(item -> this.complete_subset.put(item.getId(), item.getId()));
		
		Map<Serializable ,  Serializable> subset = new HashMap<Serializable ,  Serializable>();
		
		// getValues().forEach(item -> subset.put(item.getObject().getId(), item.getObject().getId()));
		
		for (Group group : base) {
 			if (!subset.containsKey(group.getId()))
 				groups.add(group);
		}
		
		Collections.sort(groups, new Comparator<Group>() {
			@Override
			public int compare(Group a, Group b) {
				try {
					if (getStringValue(a) == null)
						return (getStringValue(b)!=null?1:0);
					else if(getStringValue(b)==null)
						return -1;
					return  getStringValue(a).compareToIgnoreCase(getStringValue(b));
				} 
				catch (Exception e) {
					logger.error(e);
					return 0;
				}
			}
		});
		
		return groups;
	}

	private ContentSecurityDao getContentSecurityDao() {
		return (ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
	
	private boolean isPortalEnabled() {
		return ServiceLocator.getService(BrandingService.class).isPortalEnabled();
	}
	
	protected String getStringValue(Object value) {
		return value instanceof Group ? ((Group)value).getDisplayName() : value.toString();
	}
	
	
}
