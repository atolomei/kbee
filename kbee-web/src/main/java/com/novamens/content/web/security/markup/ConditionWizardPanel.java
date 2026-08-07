package com.novamens.content.web.security.markup;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSetType;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.model.KbeeAttribute;
import com.novamens.kbee.content.model.KbeeClassifier;
import com.novamens.service.ServiceLocator;

import com.novamens.wicket.model.ObjectModel;

import kbee.util.logging.Logger;

@SuppressWarnings("serial")
public abstract class ConditionWizardPanel<T> extends com.novamens.wicket.markup.html.editor.ObjectEditorPanel<T> {
	private static final long serialVersionUID = -1L;

	private static Logger logger = Logger.getLogger(ConditionWizardPanel.class.getName());
	
	private List<Panel> memberspanels;
	private IModel<T> model;

	
	public ConditionWizardPanel() {
		super("condition-editor");
		setOutputMarkupId(true);
	}
	
	@Override
	public void updateModel() {
	}
	
	@SuppressWarnings("unchecked")
	public String getCondition() {
		StringBuffer condition = new StringBuffer();
		int p = 0;
		for (Panel panel : getMembersPanels()) {
			if (panel instanceof ClassifierConditionEditor) {
				String classifierCondition = ((ClassifierConditionEditor<T>)panel).getClassifierCondition();
				if (!"".equals(classifierCondition)) {
					if (p>0) {
						condition.append(" and ");
					}
					condition.append(classifierCondition);
					p++;
				}
			}
			else 
			if (panel instanceof AttributeConditionEditor) {
					String attributeCondition = ((AttributeConditionEditor<T>)panel).getAttributeCondition();
					if (!"".equals(attributeCondition)) {
						if (p>0) {
							condition.append(" and ");
						}
						condition.append(attributeCondition);
						p++;
					}
				}
			else 
			if (panel instanceof MonitorConditionEditor) {
				String monitorCondition = ((MonitorConditionEditor<T>)panel).getCondition();
				if (!"".equals(monitorCondition)) {
					if (p>0) {
						condition.append(" and ");
					}
					condition.append(monitorCondition);
					p++;
				}
			}
		}
		return condition.toString();
	}
	
	public String getObjectCondition() {
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public String getDescription() {
		try {
			StringBuffer condition = new StringBuffer();
			int p = 0;
			for (Panel panel : getMembersPanels()) {
				if (panel instanceof ClassifierConditionEditor) {
					String classifierCondition = ((ClassifierConditionEditor<T>)panel).getDescription();
					if (!"".equals(classifierCondition)) {
						if (p>0) 
							condition.append("<span class= \"logical-operator\" >" +" and "+"</span>");
						condition.append(classifierCondition);
						p++;
					}
				}
				else 
				if (panel instanceof AttributeConditionEditor) {
					String attributeCondition = ((AttributeConditionEditor<T>)panel).getDescription();
					if (!"".equals(attributeCondition)) {
						if (p>0) 
							condition.append("<span class= \"logical-operator\" >" +" and "+"</span>");
						condition.append(attributeCondition);
						p++;
					}
				}
				else 
				if (panel instanceof MonitorConditionEditor) {
					String monitorCondition = ((MonitorConditionEditor<T>)panel).getDescription();
					if (!"".equals(monitorCondition)) {
						if (p>0) 
							condition.append("<span class= \"logical-operator\" >" +" and "+"</span>");
						condition.append(monitorCondition);
						p++;
					}
				}
			}
			return condition.toString();
		} 
		catch (Exception e) {
			logger.error(e);
			return e.getClass().getName();
		}
	}

	public List<Panel> getMembersPanels() {	
		
		if (this.memberspanels!=null)
			return this.memberspanels;		
		
		this.memberspanels = new ArrayList<Panel>();
		
		for (Classifier classifier : getClassifiers()) {
			try {								
				if (classifier.getDataSet()!=null && classifier.getDataSetType()!=null && !classifier.getDataSetType().equals(DataSetType.DATE) &&  ((KbeeClassifier)classifier).getPredicate()!=null) {
					ClassifierConditionEditor<T> panel = new ClassifierConditionEditor<T>("members", getEditor(), new ObjectModel<Classifier>(classifier)) {
						@Override
						public String getCondition() {
							return ConditionWizardPanel.this.getObjectCondition();
						}	
						@Override
						public void onUpdate(AjaxRequestTarget target) {
							super.onUpdate(target);
							target.add(ConditionWizardPanel.this.get("form-container"));
						}
						@Override
						public boolean isVisible() {
							return getClassifier().isRuleCondition() && (((KbeeClassifier)getClassifier()).getState()==ObjectState.ENABLED);
						}
					};
					this.memberspanels.add(panel);
				}
			} 
			catch (Exception e) {
				logger.error(e);
			}
		}
		
		for (Attribute attribute : getAttributes()) {
			if (!"".equals(attribute.getPredicate()) && attribute.getPredicate()!=null && !attribute.isDate()) {
				AttributeConditionEditor<T> panel = new AttributeConditionEditor<T>("members", new ObjectModel<Attribute>(attribute)) {
					@Override
					public String getCondition() {
						return ConditionWizardPanel.this.getObjectCondition();
					}	
					@Override
					public void onUpdate(AjaxRequestTarget target) {
						super.onUpdate(target);
						target.add(ConditionWizardPanel.this.get("form-container"));
					}
					@Override
					public boolean isVisible() {
						return getAttribute().isRuleCondition() && (((KbeeAttribute)getAttribute()).getState()==ObjectState.ENABLED);
					}
				};
				this.memberspanels.add(panel);
			}
		}
		

		
		//for (ContentTemplate template : getContentTemplates()) {
		//	if (!"".equals(template.getPredicate()) && template.getPredicate()!=null) {
				
		/**
		ContentTemplateConditionEditor<T> panel = new ContentTemplateConditionEditor<T>("members") {
					@Override
					public String getCondition() {
						return ConditionWizardPanel.this.getObjectCondition();
					}	
					@Override
					public void onUpdate(AjaxRequestTarget target) {
						super.onUpdate(target);
						target.add(ConditionWizardPanel.this.get("form-container"));
					}
					@Override
					public boolean isVisible() {
						return getAttribute().isRuleCondition() && (((KbeeAttribute)getAttribute()).getState()==ObjectState.ENABLED);
					}
				};
				this.memberspanels.add(panel);
				
			// }
		//}
		 * **
		 */

		
		
		if (includeMonitorCondition()) {
			MonitorConditionEditor<T> panel = new MonitorConditionEditor<T>("members", getEditor()) {
				@Override
				public String getCondition() {
					return ConditionWizardPanel.this.getObjectCondition();
				}	
				@Override
				public void onUpdate(AjaxRequestTarget target) {
					super.onUpdate(target);
					target.add(ConditionWizardPanel.this.get("form-container"));
				}
			};
			this.memberspanels.add(panel);
		}
		
		
		
		
		
		
		
		
		
		
		
		return memberspanels;
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		if (get("form-container")==null) {
			setModel(getEditor().getModel());
			addComponents();
		}
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		
		for (Panel panel : getMembersPanels()) {
			panel.detach();
		}
		if (this.model!=null)
			this.model.detach();
	}
	
	protected boolean includeMonitorCondition() {
		return false;
	}
	
	protected List<Classifier> getClassifiers() {
		return getContentDao().getClassifiers(getDomain().getId());
	}
	
	protected List<Attribute> getAttributes() {
		return getContentDao().getAttributes(getDomain());
	}
	
	protected List<ContentTemplate> getContentTemplates() {
		return getContentDao().getContentTemplates(getDomain());
	}
	
	
	protected void addComponents() {
		
		
		IModel<String> help = getHelpText();
		
		if (help!=null && help.getObject()!=null)
			add( (new Label ("help", help)).setEscapeModelStrings(false));
		else
			add((new Label ("help", "")).setVisible(false));
		
		
		WebMarkupContainer form_container = new WebMarkupContainer("form-container");
		form_container.setOutputMarkupId(true);
		add(form_container);

		
		Label description = new Label("description", new Model<String>() {
			public String getObject() {
				return getDescription();
			}
			
		}) {
			@Override
			public boolean isVisible() {
				return getDescription()!=null && getDescription().length()>0;
			}

		};
		
		description.add(new AttributeModifier("class", new Model<String>() {
			public String getObject() {
				return ConditionWizardPanel.this.getEditor().isEditionEnabled() ? "alert alert-info" : "alert alert-nobck";  
			}
		}));
		
		
		description.add(new AttributeModifier("style", new Model<String>() {
			public String getObject() {
				return ConditionWizardPanel.this.getEditor().isEditionEnabled() ? "" : "padding-top:0; margin-bottom:0; border-color:transparent;";  
			}
		}));
		
		description.setOutputMarkupId(true);
		description.setEscapeModelStrings(false);
		
		form_container.add(description);
		
		ListDataProvider<Panel> membersPanelsProvider = new ListDataProvider<Panel>() {
			public List<Panel> getData() {
				return getMembersPanels();
			}
		};
		
		WebMarkupContainer cond_container = new WebMarkupContainer("condition-container") {
			@Override
			public boolean isVisible() {
				return ConditionWizardPanel.this.getEditor().isEditionEnabled(); 
			}
		};
				
		cond_container.setOutputMarkupId(true);
		
		form_container.add(cond_container);
		
		DataView<Panel> conditionview = new DataView<Panel>("condition", membersPanelsProvider) {
			private boolean isfirst=true;
			@Override
			public void onBeforeRender() {
				super.onBeforeRender();
				isfirst=true;
			}
			@Override
			public boolean isVisible() {
				return true;
			}
			protected void populateItem(final Item<Panel> item){
				item.add(item.getModelObject());
				item.setOutputMarkupId(true); 
				if (isfirst && item.getModelObject().isVisible()) {	
					item.add(new AttributeModifier("class", "component first"));
					isfirst=false;
				}
				item.setVisible(item.getModelObject().isVisible());
				item.detach();
			}
		};
		
		cond_container.add(conditionview);	
	}
	
	protected IModel<String> getHelpText() {
		IModel<String> model = new StringResourceModel("condition.help",  ConditionWizardPanel.this, null);
		try {
			model.getObject();
			return model;
		}
		catch (MissingResourceException e) {
			return null;
		}
	}
	
	private void setModel(IModel<T> model) {
		this.model = model;
	}
	
//	private Domain getDomain() {
//		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain();
//	}
//	
//	private ContentDao getContentDao() {
//		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
//	}
}
