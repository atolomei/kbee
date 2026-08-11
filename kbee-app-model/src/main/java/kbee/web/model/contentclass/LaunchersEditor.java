package kbee.web.model.contentclass;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.Multiplicity;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.content.workflow.WorkflowDao;
import com.novamens.content.workflow.WorkflowDomainService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.kbee.content.workflow.KbeeProcedure;
import com.novamens.kbee.content.workflow.KbeeProcedureBean;
import com.novamens.kbee.content.workflow.KbeeProcessLauncher;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.LinkMenuItemPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.modal.ErrorDialog;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.Procedure;

import kbee.web.form.RelationEditor;
import kbee.web.model.procedure.ProcedurePage;




@SuppressWarnings("serial")
public class LaunchersEditor extends RelationEditor<ContentTemplate, ProcessLauncher> {	
	private static final long serialVersionUID = 1L;

	private IModel<ContentTemplate> model;
	

	public class BeanModel implements IModel<Procedure> {
		String bean;
		private KbeeProcedureBean procedure = null;
		public BeanModel(String bean) {
			this.bean = bean;
		}
		public BeanModel(KbeeProcedureBean procedure) {
			this.bean = procedure.getBeanName();
		}
		public Procedure getObject() {
			if (procedure==null) {
				procedure = (KbeeProcedureBean)ServiceLocator.getService(BeansService.class).getBean(bean);
			}
			return procedure;
		}
		public void setObject(Procedure procedure) {
		}
		public void detach() {
			procedure = null;
		}
	}

	
	public class ProcedureModel implements IModel<Procedure> {
		private Procedure procedure;
		private IModel<Procedure> model;
		private String id;
		public ProcedureModel(Procedure procedure) {
			this.procedure = procedure;
			this.id = String.valueOf(procedure.getId());
			if (!library(procedure)) {
				model = new ObjectModel<Procedure>(procedure);
			}
		}
		public Procedure getObject() {
			if (this.procedure==null) {
				if (model!=null) {
					this.procedure = model.getObject();
				}
				else {
					for (Procedure procedure : getLibrary()) {
						if (String.valueOf(procedure.getId()).equals(id)) {
							this.procedure = procedure;
						}
					}
				}
			}	
			return this.procedure;
		}
		public void setObject(Procedure procedure) {
			this.procedure = procedure;
			this.id = String.valueOf(procedure.getId());
		}

		public void detach() {
			if (model!=null)
				model.detach();
			procedure=null;
		}
	}

	
	public LaunchersEditor(IModel<ContentTemplate> model) {
		super("processLaunchers");
		add(new ErrorDialog("error-dialog"));
		this.model=model;
	}
	
	
	public IModel<ContentTemplate> getModel() {
		return model;
	}
	
	
	public void onDetach() {
		if (model!=null)
			model.detach();
		super.onDetach();
	}
	
	
	@Override
	protected List<Property<?>> getProperties() {
		
		List<Property<?>> properties = new ArrayList<Property<?>>();
		
		properties.add(new Property<Classifier>() {
			@Override
 			public String getName() {
				return "label";
			}
			@Override
			public boolean getTitle() {
				return true;
			}
		});

		properties.add(new Property<Boolean>() {
			@Override
			public String getName() {
				return "enabled";
			}
			@Override
			public boolean isBoolean() {
				return true;
			}
		});
		
		properties.add(new Property<Boolean>() {
			@Override
			public String getName() {
				return "enabledContext";
			}
			@Override
			public boolean isBoolean() {
				return true;
			}
		});
		
		properties.add(new Property<Procedure>() {
			@Override
			public String getName() {
				return "procedure";
			}
			@Override
			public List<Procedure> getChoices() {
				return getLibrary();
			}
			@Override
			public IModel<Procedure> getModel(Procedure procedure) {
				if (procedure == null)
					return null;
				if (procedure instanceof KbeeProcedureBean) {
					return new BeanModel((KbeeProcedureBean)procedure);
				}
				else {
					if (procedure.getId()!=null)
						return new ObjectModel<Procedure>(procedure);
					else {
						if (((KbeeProcedure)procedure).getBean()!=null)
							return new BeanModel(((KbeeProcedure)procedure).getBean());
						else
							return null;
					}
				}
			}
			@Override
			public Multiplicity getMultiplicity() {
				return Multiplicity.M01;
			}
		});
		
		return properties;
	}

	
	protected boolean library(Procedure procedure) {
		for (Procedure libraryprocedure : getLibrary())  {
			if (libraryprocedure.getId().equals(procedure.getId()))
				return true;
		}
		return false;
	}


	protected List<Procedure> getLibrary() {
		return getDomain().getService(WorkflowDomainService.class).getProceduresLibrary();
	}


	protected Domain getDomain() {
		return getEditor().getModelObject().getDomain();
	}
	

	@Override
	protected ProcessLauncher getNewValue() {
		KbeeProcessLauncher launcher = new KbeeProcessLauncher();
		launcher.setDomain(getDomain());
		launcher.setContentTemplate(getModel().getObject());
		launcher.setAcl(getModel().getObject().getAcl());
		List<Procedure> list = getLibrary();
		if (!list.isEmpty())
			launcher.setProcedure(list.get(0));
		return launcher;
	}
	

	@Override
	protected void custom(ContextMenuPanel<ProcessLauncher> menu) {
	
		
		
		final boolean free_version = model.getObject().getDomain().getDomainType()==DomainType.EXPRESS;
		
		menu.addItem(new MenuItemFactory<ProcessLauncher>() {
			@Override
			public AbstractMenuItemPanelV5<ProcessLauncher> getItem(String id) {
				return new SeparatorMenuItemPanelV5<ProcessLauncher>(id) {
					@Override
					public String getCssClass() {
						return "divider";
					}
				};
			}
		});
		
		menu.addItem(new MenuItemFactory<ProcessLauncher>() {
			@Override
			public AbstractMenuItemPanelV5<ProcessLauncher> getItem(String id) {
				return new LinkMenuItemPanel<ProcessLauncher>(id) {
					@Override
					public void onClick() {
						
						IModel<Procedure> pm = new ProcedureModel(getModelObject().getProcedure());
						//IModel<ProcessLauncher>  lm= getModel();
						
						setResponsePage(new ProcedurePage(pm));
					}
					@Override
					public String getLabel() {			
						return LaunchersEditor.this.getStringLabel("menu.procedure");
					}
					@Override
					public boolean isEnabled() {
							return !free_version;
					}
				};
			}
		});
	}
	
	@Override
	protected boolean isExtraInfo() {
		return true;
	}
	
	@Override
	protected void onExtraInfoClick(IModel<ProcessLauncher> model) {
		
		IModel<Procedure> pm = new ProcedureModel( model.getObject().getProcedure());
		//IModel<ProcessLauncher>  lm= model;
		setResponsePage(new ProcedurePage(pm));
		
		
	}

	@Override
	protected boolean deleteable(AjaxRequestTarget target, IModel<ProcessLauncher> model) {
		if (getWorkflowDao().hasProcesses(model.getObject().getProcedure())) {
			((ErrorDialog)get("error-dialog")).open(target, new StringResourceModel("launchers.error.constraint", LaunchersEditor.this, null));
			return false;
		};
		return true;
	}

	protected WorkflowDao getWorkflowDao() {
		return (WorkflowDao)ServiceLocator.getService(BeansService.class).getBean("WorkflowDao");
	}
}
