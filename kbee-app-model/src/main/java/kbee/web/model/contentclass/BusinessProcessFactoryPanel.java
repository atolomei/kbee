package kbee.web.model.contentclass;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

import com.novamens.content.workflow.WorkflowDomainService;
import com.novamens.kbee.content.workflow.KbeeProcedureBean;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.workflow.Procedure;


@SuppressWarnings("serial")
public abstract class BusinessProcessFactoryPanel extends KBPanel {
	private static final long serialVersionUID = 1L;
	
	public class ProxyProcedureModel implements IModel<Procedure> {
	
		Serializable id;
		Serializable bean;
		
		public ProxyProcedureModel(Procedure p) {
			if (p instanceof KbeeProcedureBean)
				bean=((KbeeProcedureBean) p).getBeanName();
			id=p.getId();
		}
		
		@Override
		public Procedure getObject() {
			List <Procedure> pr=getDomain().getService(WorkflowDomainService.class).getProceduresLibrary();
			for (Procedure p:pr) {
				if (p instanceof KbeeProcedureBean) {
					if ( ((KbeeProcedureBean) p).getBeanName().equals(bean))
						return p;
				}
			}
			return null;
		}
	}

	public BusinessProcessFactoryPanel() {
		this("new-business-process");
	}
		
	public BusinessProcessFactoryPanel(String id) {
		super(id);
		
		ContextMenuPanel<Procedure> menu = new ContextMenuPanel<Procedure>(null);
		
		WebMarkupContainer newm = new WebMarkupContainer ("new-multiple-button");
		newm.add(new AttributeModifier("class", "btn-sm btn btn-primary dropdown-toggle atright"));
		newm.add(new AttributeModifier("data-toggle", "dropdown"));
		add(newm);
		
		for (Procedure ci: getProcedures()) {
			menu.addItem(new AbstractMenuItemFactory<Procedure>(new ProxyProcedureModel(ci)) {
				@Override
				public AbstractMenuItemPanelV5<Procedure> getItem(String id) {
					return new AjaxMenuItemPanelV5<Procedure>(id) {
						private static final long serialVersionUID = 1L;
						@Override
						public String getLabel() {
							return  getFactoryModel().getObject().getDisplayName();
						}
						@Override
						public String getTarget() {
							return "_blank";
						}
						@Override
						public void onClick(AjaxRequestTarget target) throws Exception {
							Procedure procedure = getFactoryModel().getObject();
							onCreate(procedure, target);
						}
					};
				}
			});
		}
		
		add(menu);
	}
	
	protected abstract void onCreate(Procedure procedure, AjaxRequestTarget target);
	
	protected List<Procedure> getProcedures() {
		List<Procedure> procedures = new ArrayList<Procedure>();
		for (Procedure procedure : getDomain().getService(WorkflowDomainService.class).getProceduresLibrary()) {
			if (procedure.getLocale().equals(getDomain().getLocale())) {
				procedures.add(procedure);
			}
		}
		return procedures;
	}
}
