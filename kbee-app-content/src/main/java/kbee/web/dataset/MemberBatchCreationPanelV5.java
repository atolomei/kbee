package kbee.web.dataset;


import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.user.UserService;
import com.novamens.content.web.command.batch.markup.BatchCommandStatusPanel;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.command.CommandService;


import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;

import com.novamens.wicket.util.MenuBreadCrumbPanel;
			
public class MemberBatchCreationPanelV5 extends Panel {
			
	private static final long serialVersionUID = 1L;

	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(MemberBatchCreationPanelV5.class.getName());

	private BatchCreationForm form;
	
	private IModel<DataSet> model;
	private IModel<DataSetMember> dm_member_builtin_model;

	
	public IModel<DataSetMember> getDataSetMembeBuiltinModel() {
		return dm_member_builtin_model;
	}


	public void setDataSetMembeBuiltinModel(IModel<DataSetMember> dm_member_builtin_model) {
		this.dm_member_builtin_model = dm_member_builtin_model;
	}


	private List<DataSet> dlist = null;
	
	private State command_state = State.PREPARING;
	

	public enum State {
		PREPARING 		(1, "preparing"), 
		EXECUTING 		(2, "executing"),
		TERMINATED		(3, "terminated"); 

		private String label;
		private int id;
		
		private  State(int code, String label) {this.label = label;this.id = code;}
		public String toString() {return ("id: " + getId() + "  label: "+ getLabel());} 
		public String getLabel() {return label;}
		public int getId() {return id;}
	}
	

	public IModel<DataSet> getModel() {
		return this.model;
	}


	protected void setModel(IModel<DataSet> mdataset) {
		this.model=mdataset;
	}

	
	@Override
	public void onDetach() {

		if (dm_member_builtin_model!=null)
			dm_member_builtin_model.detach();
		
		if (model!=null)
			 model.detach();
		
		if (form!=null)
			form.detach();
		
		dlist=null;
			
		super.onDetach();
		
	}
	
	public MemberBatchCreationPanelV5(String id, IModel<DataSet> mdataset) {
		this(id, mdataset, null);
	}
	
	/**
	 * @param id
	 * @param mdataset
	 */
	public MemberBatchCreationPanelV5(String id, IModel<DataSet> mdataset, IModel<DataSetMember> datasetmember_builtin) {
		super(id);
		setModel(mdataset);
		
		setDataSetMembeBuiltinModel(datasetmember_builtin);
		
		add(new MenuBreadCrumbPanel<DataSet>("breadcrumb", getModel(), new BCElement(new StringResourceModel("dataset-values",  MemberBatchCreationPanelV5.this, null)) {
				private static final long serialVersionUID = 1L;
				@Override
				public void onClick() {
					try {
						IModel<DataSet> dm =  new ObjectModel<DataSet>(MemberBatchCreationPanelV5.this.getModel().getObject());
						setResponsePage(new DataSetMembersPage(dm));
					} catch (Exception e) {
						logger.error(e);
					}
				}
			}));
		
		form = new BatchCreationForm ("form");

		add(form);
		setOutputMarkupId(true);
		
		add (new Panel("status") {
			private static final long serialVersionUID = 1L;
			public boolean isVisible() {
				return false;
			}
		});
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		
	}
	/**
	 * 
	 */
	public class BatchCreationForm extends Form<Void> {
		 
		private static final long serialVersionUID = 1L;
		private String elements;
		
		/**
		 * @param id
		 */
		@SuppressWarnings("serial")
		public BatchCreationForm(String id) {
			super(id);

			final boolean is_root = ServiceLocator.getService(SecurityService.class).isRoot();
			final boolean is_domain_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
			final boolean is_model = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());
			final boolean has_permission = is_root || is_domain_admin || is_model;				
			
			setOutputMarkupId(true);
			
			ChoiceField<DataSet> f_data = new ChoiceField<DataSet>("datasets",  MemberBatchCreationPanelV5.this.getModel(), new PropertyModel<List<DataSet>>(this, "dataSets")) {
				@Override
				public boolean isEnabled() {
					return getDataSets().size()>0;
				}
				
				@Override
				public boolean isVisible() {
					return getDataSetMembeBuiltinModel()==null;
				}
				
				public String getDisplayValue(DataSet value) {
					return value.getDisplayName();
				}
			};
			
			f_data.setMarkupId("dataset"+getMarkupId());
			add(f_data);
			
			WebMarkupContainer bic =new WebMarkupContainer("builtin-container");
			bic.setVisible(getDataSetMembeBuiltinModel()!=null);
			add(bic);
			
			bic.add(new Label("builtin-dataset",   MemberBatchCreationPanelV5.this.getModel()!=null? MemberBatchCreationPanelV5.this.getModel().getObject().getName():""));
			bic.add(new Label("builtin-datasetmember", getDataSetMembeBuiltinModel()!=null?
					(getDataSetMembeBuiltinModel().getObject().getName()+" ("+ getDataSetMembeBuiltinModel().getObject().getDataSet().getName()+")"):
					""));
			
			
			TextArea<String> statement = new TextArea<String>("elements") {
				private static final long serialVersionUID = 1L;
				@Override
				public boolean isEnabled() {
					return true;
				}
			};
			statement.setModel(new PropertyModel<String>(this,"elements"));
			add(statement);
			
			add(new AjaxButton("submit-button", this) {
				@Override
				protected void onSubmit(AjaxRequestTarget target) {
					try {

						if (MemberBatchCreationPanelV5.this.getModel()!=null && getElements()!=null) {
							Domain domain = MemberBatchCreationPanelV5.this.getModel().getObject().getDomain();
							
							if (domain!=null) {
								
								MemberBatchCreationCommand cmd;
								cmd = new MemberBatchCreationCommand(
										MemberBatchCreationPanelV5.this.getModel().getObject().getId(), 
										MemberBatchCreationPanelV5.this.getModel().getObject().getDomain().getId(), 
										getSessionUser().getId(), getElements(), 
										(getDataSetMembeBuiltinModel()!=null?getDataSetMembeBuiltinModel().getObject().getId(): null)
										);
								
								if (cmd!=null) {	
									CommandService service = ServiceLocator.getService(CommandService.class);
									service.add(cmd);
									
									setState(State.EXECUTING);
									BatchCommandStatusPanel panel = new BatchCommandStatusPanel("status", (long) cmd.getId(), false) {
										private static final long serialVersionUID = 1L;
										@Override
										public void onAfterExecution(AjaxRequestTarget target) {
											setState(State.TERMINATED);
											target.add(MemberBatchCreationPanelV5.this);
										}
									};
									
									MemberBatchCreationPanelV5.this.replace(panel);
									logger.debug("Sending "+ cmd.getId().toString());
									target.add(MemberBatchCreationPanelV5.this);
								}
							}
							else {
								error("Domain is null.");
							}
						}
					}
						catch (Exception e) {
							logger.error(e);
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							PrintStream ps = new PrintStream(baos);
							e.printStackTrace(ps);
							String message =  baos.toString();
							form.error(message);
							
						}
					
					target.add(BatchCreationForm.this);
				}
				
				@Override
				public boolean isVisible() {
					return getState()==State.PREPARING;
				}

				@Override
				public boolean isEnabled() {
					return has_permission && getState()!=State.EXECUTING;
				}
			});
			
			AjaxButton cb = new AjaxButton("close-button", this) {
				private static final long serialVersionUID = -5848063566372226285L;
				
				@Override
				protected void onSubmit(AjaxRequestTarget target) {
						MemberBatchCreationPanelV5.this.onClose();
				}
				@Override
				public boolean isVisible() {
					return getState()!=State.EXECUTING;
				}
				@Override
				public boolean isEnabled() {
					return getState()!=State.EXECUTING;
				}
			};
			
			Label cbl = new Label("close", new Model<String>() {
				private static final long serialVersionUID = 1L;
					@Override
					public String getObject() {
							return new StringResourceModel((getState()==State.TERMINATED?"close":"cancel"), MemberBatchCreationPanelV5.this, null).getObject();
					}
			});
			
			cb.add(cbl);
			add(cb);

			add(new AjaxButton("stop-button", this) {
				
				
				@Override
				protected void onSubmit(AjaxRequestTarget target) {
					((BatchCommandStatusPanel) MemberBatchCreationPanelV5.this.get("status")).stop(target);
				}
				
				@Override
				public boolean isVisible() {
					return getState()==State.EXECUTING;
				}

				@Override
				public boolean isEnabled() {
					return getState()==State.EXECUTING;
				}
			});

			add(new FeedbackPanel("feedback"));
		}
		
		
		/**
		 * 
		 */
		public List<DataSet> getdataSets() {
			return getDataSets();
		}

		/**
		 * 
		 */

		public List<DataSet> getDataSets() {
			if (dlist != null)
				return dlist;
			dlist = new ArrayList<DataSet>(); 
			List<DataSet> datasets = getContentDao().getDataSets(ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain());
			
			
			for (DataSet dataset: datasets) {
				
				if (    !dataset.isReadonly() 		&& 
						!dataset.isAggregation() 	&&
						(dataset.getDataSetType()==DataSetType.STRING || dataset.getDataSetType()==DataSetType.ENTITY) && dataset.getState()!=ObjectState.DELETED) {
					
						dlist.add(dataset);
					}
			}
			return dlist;
		}

		public String getElements() {
			return this.elements;
		}
		
		public void setElements(String elements) {
			this.elements = elements;
		}
	}

	
	public State getState() { 
		return this.command_state;
	}

	public void setState(State state) {
		this.command_state = state;
	}

	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

	private User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}


	protected void onClose() {
	}
}
