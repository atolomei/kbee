package kbee.web.model.procedure;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.ListModel;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.Role;
import com.novamens.kbee.content.security.KbeeAbstractRole;
import com.novamens.kbee.content.workflow.KbeeProcedure;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.Procedure;

import kbee.web.panel.AlertPanel;

public class ProcedureRolesPanel extends ModelPanel<Procedure> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ProcedureRolesPanel(String id, IModel<Procedure> model) {
		super(id, model);
	}

	
	
	public List<Role> getRoles() {
		List<Role> li=  getContentSecurityDao().getRoles(((KbeeProcedure) getModel().getObject()).getDomain());
		List<Role> res= new ArrayList<Role>();
		for (Role r:li) {
			
			if (r instanceof KbeeAbstractRole) {
				
				
				// for (Launcher la: ((KbeeAbstractRole) r).getLaunchers()) {
				//	
				// }
				
			}
			//r.getLa
			//res.add(r);
		}
		return res;
	}
	
	
	 
	
	public void onInitialize() {
		super.onInitialize();
		
		String msg ="this panel is readonly";
		add(new AlertPanel<Procedure>("alert-readonly",  AlertPanel.INFO, getModel(),  null, new Model<String>(msg)));
		
		

		ListModel<Role> lm = new ListModel<Role>(getRoles());
		ListView<Role> lv = new ListView<Role>("roles", lm) {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<Role> item) {
				
				Label name = new Label("role", item.getModel().getObject().getDisplayName());
				Label permissions = new Label("permissions", item.getModel().getObject().getDisplayName());
				item.add(name);
				item.add(permissions);
			}
		};
		add(lv);
		
		
	}
	
	private ContentSecurityDao getContentSecurityDao() {
		return (ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
	
	
	protected ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
//	
//	private List<ProcessLauncher> getLaunchers() {
//		List<ProcessLauncher>  list = ((KbeeProcedure) getModel().getObject()).getDomain().getService(WorkflowDomainService.class).getLaunchers();
//		return list;
//	}
}


