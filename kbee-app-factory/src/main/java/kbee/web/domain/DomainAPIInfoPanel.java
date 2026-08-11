package kbee.web.domain;


import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.base.SecurityRule;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.library.Library;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.IQLRule;
import com.novamens.dao.SecurityDao;
import com.novamens.dom.Domain;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.repository.DomRepositoryService;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KeyValue;
 
public class DomainAPIInfoPanel extends Panel {
		
	
	
	private static final long serialVersionUID = 1L;
	static private Logger logger = LogManager.getLogger(DomainAPIInfoPanel.class.getName());
	
	
	private IModel<Domain> model;
	
	/** -----------------------------------------------------------------------------------------------------------
	 * @param id
	 * @param name
	 * @param console
	 */
	public DomainAPIInfoPanel(String id, IModel<Domain> model) {
		super(id);
		
		setModel(model);

		setOutputMarkupId(true);
		
	}
	
	public void setModel(IModel<Domain> model) {
		this.model = model;
	}
	

	public IModel<Domain> getModel() {
		return this.model;
	}
	
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();

		if (get("api-info")!=null) {
			get("api-info").detach();
		}
		else {
			DataView<KeyValue<String>> application_info = new DataView<KeyValue<String>>("api-info",new ListDataProvider<KeyValue<String>>(apiInfo())) {
				private static final long serialVersionUID = 1L;
				protected void populateItem(final Item<KeyValue<String>> item){
					item.add( new Label("label", item.getModelObject().getDisplayName()));
					item.add( (new Label("value", item.getModelObject().value)).setEscapeModelStrings(false));
	  			}			
			};
			
			add(application_info);

		}
		
	}
	
	/** -----------------------------------------------------------------------------------------------------------
	 * @return
	 */
	private List<KeyValue<String>> apiInfo() {
		List<KeyValue<String>> data = new ArrayList<KeyValue<String>>();
		try {
			
				data.add(new KeyValue<String>("API Enabled", 
									getModel().getObject().isAPIEnabled() ? "Yes": "No"
								));
				
				boolean found = false;
				Library cabi = null;
				for (Library cabinet: getLibraries(getModel().getObject())) {
					if (cabinet.getKey().equals(Library.EXTERNAL)) {
						cabi = cabinet;
						break;
					}
				}
				
				
				String dataset_name = getContentDao().findSystemParameterValueByKey("dataset_secureaccess.name", "Secured Access");
				DataSet da = null;
				for (DataSet ds: getContentDao().getDataSets(getModel().getObject())) {
						if (ds.getName().equals(dataset_name)) {
							da=ds;
							break;
						}
				}
				
				
				data.add(new KeyValue<String>("Secure Access DataSet",
									
									da!=null?da.getName(): ("[ no Dataset "+dataset_name+" found ]")
									
						));
				
				
				if (da!=null) {
					StringBuilder str = new StringBuilder();
					for (DataSetMember dm: getContentDao().getMembers(da, "strvalue")) {
						if (str.length()>0)
							str.append(" | ");
						str.append(dm.getName());
					}
					data.add(new KeyValue<String>(	"Secure Access DataSet",
											str.toString()
									));	
				}
				
				Classifier ca = null;
				for (Classifier cs: getContentDao().getClassifiers(getModel().getObject())) {
						if (cs.getName()!=null && cs.getName().equals(dataset_name)) {
							ca=cs;
							break;
						}
				}

				data.add(new KeyValue<String>("Secure Access Classifier", 		 ca!=null?ca.getName(): ("[ no Classifier "+dataset_name+" found ]")));
				
				data.add(new KeyValue<String>("External Cabinet. Name", 		 (cabi!=null?cabi.getDisplayName():"[ ] ")));
				data.add(new KeyValue<String>("External Cabinet. Read Only", 	 (cabi!=null?(cabi.isReadOnly()?"Yes":"No"): "[ ]")));
				
				// -------------------------------------------------------
				// Secure Access Group for API
				//

				found = false;
				Group g= null;
				for (Group gr: getContentSecurityDao().getGroups(getModel().getObject()) ) {
					
					if (gr.getName()!=null && gr.getName().equals(dataset_name)) {
						data.add(new KeyValue<String>("Security. Secure Access Group",   gr.getName()));
						found=true;
						g=gr;
						break;
					}
				}
				
				if (found) {
					int n = 1;
					for  (IQLRule rule: getContentSecurityDao().getRules(getModel().getObject())) {
						if (rule instanceof SecurityRule) {
							if (	(((SecurityRule) rule).getCondition()!=null ) && 
									(((SecurityRule) rule).getCondition().toLowerCase().startsWith("secureacc")))  {
								data.add(new KeyValue<String>("Security. Secure Access Rule " + String.valueOf(n),  rule.getName() + " [ " + ((SecurityRule) rule).getCondition() + " ] "));
								n++;
							}
							else if (	(((SecurityRule) rule).getCondition()!=null ) && 
										(((SecurityRule) rule).getCondition().toLowerCase().startsWith("not secureacc")))  {
								data.add(new KeyValue<String>("Security. Secure Access Rule " + String.valueOf(n),  rule.getName() + " [ " + ((SecurityRule) rule).getCondition() + " ] ")) ;
								n++;
							}
						}
					}
				}
				else {
					data.add(new KeyValue<String>("Security. Secure Access Group",   "[ ]"));
				}
				
				User user1 = ServiceLocator.getService(SecurityService.class).findUserByUsername( getContentDao().findSystemParameterValueByKey("onesitedm.application", "onesitedm")+"@"+getModel().getObject().getName());
				User user2 = ServiceLocator.getService(SecurityService.class).findUserByUsername( getContentDao().findSystemParameterValueByKey("tibco.username", "tibco")+"@"+getModel().getObject().getName());
				User user3 = ServiceLocator.getService(SecurityService.class).findUserByUsername( getContentDao().findSystemParameterValueByKey("sso.username", "sso")+"@"+getModel().getObject().getName());
																																												
				data.add(new KeyValue<String>("Security. User OneSite", user1!=null?  user1.getUserName() :" [  ]" ));
				data.add(new KeyValue<String>("Security. User TIBCO",  user2!=null?  user2.getUserName() :" [  ]" ));
				data.add(new KeyValue<String>("Security. User SSO",  user3!=null?  user3.getUserName() :" [  ]" ));
				
			
		} catch (RuntimeException e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return data;
	}
	
	public List<Library> getLibraries(Domain domain) {
		return ServiceLocator.getService(DomRepositoryService.class).getRepository(Library.class).findAll(domain);
	}

	private ContentSecurityDao getContentSecurityDao() {
		return (ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}

