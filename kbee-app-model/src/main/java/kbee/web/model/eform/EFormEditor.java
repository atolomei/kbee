package kbee.web.model.eform;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;


import org.apache.wicket.AttributeModifier;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.IFormModelUpdateListener;
import org.apache.wicket.markup.html.form.IFormSubmitter;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.ResourceTag;
import com.novamens.content.form.EAttributeModel;
import com.novamens.content.form.EAutoCompleteField;
import com.novamens.content.form.EBooleanField;
import com.novamens.content.form.ECheckField;
import com.novamens.content.form.EClassifierModel;
import com.novamens.content.form.EContentTitleModel;
import com.novamens.content.form.EDateField;
import com.novamens.content.form.EFormAccessLevel;
import com.novamens.content.form.EFieldModel;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormComponent;
import com.novamens.content.form.EFormContainer;
import com.novamens.content.form.EFormAttributeModel;
import com.novamens.content.form.EFormField;
import com.novamens.content.form.EFormSection;
import com.novamens.content.form.EHtmlField;
import com.novamens.content.form.EHtmlStructField;
import com.novamens.content.form.EListField;
import com.novamens.content.form.ENumberField;
import com.novamens.content.form.ERelationModel;
import com.novamens.content.form.ERelationResourceModel;
import com.novamens.content.form.EResourceDistributionModel;
import com.novamens.content.form.EResourceField;
import com.novamens.content.form.EResourceModel;
import com.novamens.content.form.EResourceSystemField;
import com.novamens.content.form.EResourceSystemModel;
import com.novamens.content.form.EResourcesField;
import com.novamens.content.form.EStringField;
import com.novamens.content.form.ETableField;
import com.novamens.content.form.ETableModel;
import com.novamens.content.form.EText;
import com.novamens.content.form.ETextField;
import com.novamens.content.form.ETitle;
import com.novamens.content.form.EValidation;
import com.novamens.content.model.AccessStrategy;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.AttributeType;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.ModelElementTemplate;
import com.novamens.content.model.RelationTemplate;
import com.novamens.content.service.DomService;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.content.workflow.WorkflowDomainService;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.form.ScriptEvaluator;
import com.novamens.kbee.content.form.EFormAbstractComponent;
import com.novamens.kbee.content.form.EFormAbstractField;
import com.novamens.kbee.content.form.KbeeEAttributeFieldModel;
import com.novamens.kbee.content.form.KbeeEBooleanAttributeModel;
import com.novamens.kbee.content.form.KbeeEBooleanField;
import com.novamens.kbee.content.form.KbeeEBooleanModel;
import com.novamens.kbee.content.form.KbeeECheckField;
import com.novamens.kbee.content.form.KbeeEClassifierFieldModel;
import com.novamens.kbee.content.form.KbeeEConditionValidation;
import com.novamens.kbee.content.form.KbeeEContentTitleModel;
import com.novamens.kbee.content.form.KbeeEDateAttributeModel;
import com.novamens.kbee.content.form.KbeeEDateField;
import com.novamens.kbee.content.form.KbeeEDateModel;
import com.novamens.kbee.content.form.KbeeEDateTimeField;
import com.novamens.kbee.content.form.KbeeEExternalResources;
import com.novamens.kbee.content.form.KbeeEForm;
import com.novamens.kbee.content.form.KbeeEFormAttributeModel;
import com.novamens.kbee.content.form.KbeeEFormRow;
import com.novamens.kbee.content.form.KbeeEFormSection;
import com.novamens.kbee.content.form.KbeeEHtmlField;
import com.novamens.kbee.content.form.KbeeEHtmlModel;
import com.novamens.kbee.content.form.KbeeEHtmlStructField;
import com.novamens.kbee.content.form.KbeeEListField;
import com.novamens.kbee.content.form.KbeeEMemberAutoCompleteField;
import com.novamens.kbee.content.form.KbeeEMemberAutoCompleteWithPreviewField;
import com.novamens.kbee.content.form.KbeeEMemberComboField;
import com.novamens.kbee.content.form.KbeeEMembersListField;
import com.novamens.kbee.content.form.KbeeENumberAttributeModel;
import com.novamens.kbee.content.form.KbeeENumberField;
import com.novamens.kbee.content.form.KbeeERelation;
import com.novamens.kbee.content.form.KbeeERelationFieldModel;
import com.novamens.kbee.content.form.KbeeEResource;
import com.novamens.kbee.content.form.KbeeEResourceDistribution;
import com.novamens.kbee.content.form.KbeeEResourceDistributionFieldModel;
import com.novamens.kbee.content.form.KbeeEResourceFieldModel;
import com.novamens.kbee.content.form.KbeeEResourceSystem;
import com.novamens.kbee.content.form.KbeeEResourceSystemFieldModel;
import com.novamens.kbee.content.form.KbeeEResourceSystemV2;
import com.novamens.kbee.content.form.KbeeEResourceSystemV3;
import com.novamens.kbee.content.form.KbeeEResources;
import com.novamens.kbee.content.form.KbeeEStringAttributeModel;
import com.novamens.kbee.content.form.KbeeEStringField;
import com.novamens.kbee.content.form.KbeeEStringListField;
import com.novamens.kbee.content.form.KbeeEStringModel;
import com.novamens.kbee.content.form.KbeeETableField;
import com.novamens.kbee.content.form.KbeeETableFieldModel;
import com.novamens.kbee.content.form.KbeeEText;
import com.novamens.kbee.content.form.KbeeETextField;
import com.novamens.kbee.content.form.KbeeETitle;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.CheckField;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.SortableBehavior;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.modal.InfoDialog;
import com.novamens.wicket.markup.html.tabs.AjaxTabbedPanel;
import com.novamens.wicket.model.ObjectModel;


import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;
import kbee.web.panel.AlertPanel;


@SuppressWarnings("serial")
public class EFormEditor extends ObjectEditor<EForm> {
	private static final long serialVersionUID = 1L;
	
	final boolean is_domain_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_model = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());

	private IModel<ContentTemplate> templatemodel;
	private List<ComponentWrapper> wrappers;
	
	public class ComponentWrapper implements Serializable {
		private int level;
		private EFormComponent component;
		public ComponentWrapper(EFormComponent component, int level) {
			this.component = component;
			this.level = level;
		}
		public int getLevel() {
			return level;
		}
		public void setLevel(int value) {
			this.level = value;
		}
		public EFormComponent getComponent() {
			return component;
		};
	}
	
	public class ComponentsGrid extends Fragment {
		
		public ComponentsGrid() {
			super("components", "eform-grid-fragment", EFormEditor.this);
			
			WebMarkupContainer values = new WebMarkupContainer("values");
			values.setOutputMarkupId(true);
			WebMarkupContainer header = new WebMarkupContainer("header");
			values.add(header);
			WebMarkupContainer body = new WebMarkupContainer("body");
			body.add(new ListView<ComponentWrapper>("component", () -> getWrappers()) {
				protected void populateItem(ListItem<ComponentWrapper> item){
					item.add(new ComponentRowView("component-view", item.getModelObject()));
					item.add(new AttributeModifier("data-id", "value_"+item.getIndex()));
				}
			});
			
			body.add(new SortableBehavior() {
				@Override
				public void onSort(AjaxRequestTarget target, String id, List<String> ids) {
					id = id.replace("value_", "");
					sort(id, ids);
					setUpdatedPart("order");
					target.add(EFormEditor.this);
				}
				@Override
				public String getItemSelector() {
					return "div.value";
				}
			});
			
			values.add(body);
			
			add(values);
			
		}
		protected void sort(String sortedindexstr, List<String> ids) {
			int i = 0;
			
			List<ComponentWrapper> values = getWrappers();
			List<ComponentWrapper> values2 = new ArrayList<ComponentWrapper>();
			values2.addAll(values);
			
			int sortedindex = Integer.valueOf(sortedindexstr);
			
			ComponentWrapper sorted = values2.get(sortedindex);
			sorted.setLevel(-1);
			
			if (values.size()==ids.size()) {
				i =0;
				for (String id : ids) {
					int index = Integer.valueOf(id);
					values.set(i, values2.get(index));
					i++;
				}
			}
			
			ComponentWrapper previous = null;
			for (ComponentWrapper wrapper : values) {
				int level = wrapper.getLevel();
				if (level>0 && previous==null) {
					wrapper.setLevel(0);
				}
				else
				if (level>0 && previous!=null && !(previous.getComponent() instanceof EFormContainer) && level>previous.getLevel()) {
					wrapper.setLevel(previous.getLevel());
				}
				else
	 			if (level<0 && previous!=null && (previous.getComponent() instanceof EFormContainer)) {
					wrapper.setLevel(previous.getLevel()+1);
				}
				else
		 		if (level<0 && previous!=null && !(previous.getComponent() instanceof EFormContainer) && !(wrapper.getComponent() instanceof EFormContainer)) {
					wrapper.setLevel(previous.getLevel());
				}
				else
				if (level<0 && previous==null) {
					wrapper.setLevel(previous==null ? 0 : previous.getLevel());
				}
				else
				if (level<0 && previous!=null) {
					wrapper.setLevel(previous==null || wrapper.getComponent() instanceof EFormSection ? 0 : previous.getLevel());
				}
				previous = wrapper;
			}
		}
	}

	
	/***
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 *
	 */
	public class FactoryPanel extends Fragment {
		
		public FactoryPanel() {
			super("factory", "eform-factory-fragment", EFormEditor.this);
			
			WebMarkupContainer button = new WebMarkupContainer ("new-multiple-button");
			add(button);
			
			ContextMenuPanel<Void> menu = new ContextMenuPanel<Void>(null);
			
			menu.addItem(id -> 
				new AjaxMenuItemPanelV5<Void>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						EFormAbstractField<?> field =  new KbeeEMemberAutoCompleteField();
						field.setModel(new KbeeEClassifierFieldModel());
						addComponent(field);
						target.add(EFormEditor.this);
					}
					@Override
					public String getLabel() {	
						return EFormEditor.this.getLabel("autocomplete").getObject();
					}
			});
			
			menu.addItem(id -> 
				new AjaxMenuItemPanelV5<Void>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						EFormAbstractField<?> field =  new KbeeEMemberAutoCompleteWithPreviewField();
						field.setModel(new KbeeEClassifierFieldModel());
						addComponent(field);
						target.add(EFormEditor.this);
					}
					@Override
					public String getLabel() {	
						return EFormEditor.this.getLabel("autocomplete-panel").getObject();
					}
			});
			
			menu.addItem(id -> 
			new AjaxMenuItemPanelV5<Void>(id) {
				@Override
				public void onClick(AjaxRequestTarget target) {
					EFormAbstractField<?> field =  new KbeeEBooleanField();
					field.setModel(new KbeeEBooleanAttributeModel());
					addComponent(field);
					target.add(EFormEditor.this);
				}
				@Override
				public String getLabel() {	
					return EFormEditor.this.getLabel("boolean").getObject();
				}
		});
			
			menu.addItem(id -> 
			new AjaxMenuItemPanelV5<Void>(id) {
				@Override
				public void onClick(AjaxRequestTarget target) {
					EFormAbstractField<?> field =  new KbeeECheckField();
					field.setModel(new KbeeEBooleanAttributeModel());
					addComponent(field);
					target.add(EFormEditor.this);
				}
				@Override
				public String getLabel() {	
					return EFormEditor.this.getLabel("check").getObject();
				}
			});
			
			menu.addItem(id -> 
				new AjaxMenuItemPanelV5<Void>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						EFormAbstractField<?> field =  new KbeeEMemberComboField();
						field.setModel(new KbeeEClassifierFieldModel());
						addComponent(field);
						target.add(EFormEditor.this);
					}
					@Override
					public String getLabel() {	
						return EFormEditor.this.getLabel("combo").getObject();
					}
			});
			
			menu.addItem(id -> 
				new AjaxMenuItemPanelV5<Void>(id) {
				@Override
				public void onClick(AjaxRequestTarget target) {
					EFormAbstractField<?> field =  new KbeeEDateField();
					KbeeEStringAttributeModel model =new KbeeEStringAttributeModel();
					field.setModel(model);
					addComponent(field);
					target.add(EFormEditor.this);
				}
				@Override
				public String getLabel() {	
					return EFormEditor.this.getLabelString("date");
				}
			});
			
			menu.addItem(id -> 
				new AjaxMenuItemPanelV5<Void>(id) {
				@Override
				public void onClick(AjaxRequestTarget target) {
					EFormAbstractField<?> field =  new KbeeEHtmlField();
					field.setModel(new KbeeEStringAttributeModel());
					addComponent(field);
					target.add(EFormEditor.this);
				}
				@Override
				public String getLabel() {	
					return EFormEditor.this.getLabel("html").getObject();
				}
			});
			
			menu.addItem(id -> 
				new AjaxMenuItemPanelV5<Void>(id) {
				@Override
				public void onClick(AjaxRequestTarget target) {
					EFormAbstractField<?> field =  new KbeeEHtmlStructField();
					field.setModel(new KbeeEHtmlModel());
					addComponent(field);
					target.add(EFormEditor.this);
				}
				@Override
				public String getLabel() {	
					return EFormEditor.this.getLabel("html-struct").getObject();
				}
			});
			
			menu.addItem(id -> 
				new AjaxMenuItemPanelV5<Void>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						EFormAbstractField<?> field =  new KbeeEMembersListField();
						field.setModel(new KbeeEClassifierFieldModel());
						addComponent(field);
						target.add(EFormEditor.this);
					}
					@Override
					public String getLabel() {	
						return EFormEditor.this.getLabel("list").getObject();
					}
			});
			
			
			menu.addItem(id -> 
				new AjaxMenuItemPanelV5<Void>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						EFormAbstractField<?> field =  new KbeeENumberField();
						field.setModel(new KbeeEStringAttributeModel());
						addComponent(field);
						target.add(EFormEditor.this);
					}
					@Override
					public String getLabel() {	
						return EFormEditor.this.getLabel("number").getObject();
					}
			});
			
			menu.addItem(id -> 
				new AjaxMenuItemPanelV5<Void>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						EFormAbstractField<?> field =  new KbeeERelation();
						field.setModel(new KbeeERelationFieldModel());
						addComponent(field);
						target.add(EFormEditor.this);
					}
					@Override
					public String getLabel() {	
						return EFormEditor.this.getLabel("relation").getObject();
					}
				});
			
			menu.addItem(id -> 
				new AjaxMenuItemPanelV5<Void>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						EFormAbstractField<?> field =  new KbeeEResource();
						field.setModel(new KbeeEResourceFieldModel());
						addComponent(field);
						target.add(EFormEditor.this);
					}
					@Override
					public String getLabel() {	
						return EFormEditor.this.getLabel("resource").getObject();
					}
			});
			
			menu.addItem(id -> 
				new AjaxMenuItemPanelV5<Void>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						EFormAbstractField<?> field =  new KbeeEResourceSystem();
						field.setModel(new KbeeEResourceSystemFieldModel());
						addComponent(field);
						target.add(EFormEditor.this);
					}
					@Override
					public String getLabel() {	
						return EFormEditor.this.getLabel("resource-system").getObject();
					}
			});
			
			menu.addItem(id -> 
				new AjaxMenuItemPanelV5<Void>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						EFormAbstractField<?> field =  new KbeeEResourceSystemV2();
						field.setModel(new KbeeEResourceSystemFieldModel());
						addComponent(field);
						target.add(EFormEditor.this);
					}
					@Override
					public String getLabel() {	
						return EFormEditor.this.getLabel("resource-systemv2").getObject();
					}
			});
			
			menu.addItem(id -> 
			new AjaxMenuItemPanelV5<Void>(id) {
				@Override
				public void onClick(AjaxRequestTarget target) {
					EFormAbstractField<?> field =  new KbeeEResourceSystemV3();
					field.setModel(new KbeeEResourceSystemFieldModel());
					addComponent(field);
					target.add(EFormEditor.this);
				}
				@Override
				public String getLabel() {	
					return EFormEditor.this.getLabel("resource-systemv3").getObject();
				}
		});
			
			menu.addItem(id -> 
				new AjaxMenuItemPanelV5<Void>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						EFormAbstractField<?> field =  new KbeeEResources();
						field.setModel(new KbeeEResourceFieldModel());
						addComponent(field);
						target.add(EFormEditor.this);
					}
					@Override
					public String getLabel() {	
						return EFormEditor.this.getLabel("resources").getObject();
					}
			});
			
			menu.addItem(id -> 
				new AjaxMenuItemPanelV5<Void>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						EFormAbstractField<?> field =  new KbeeEExternalResources();
						field.setModel(new KbeeEResourceFieldModel());
						addComponent(field);
						target.add(EFormEditor.this);
					}
					@Override
					public String getLabel() {	
						return EFormEditor.this.getLabel("external-resources").getObject();
					}
			});
			
			menu.addItem(id -> 
				new AjaxMenuItemPanelV5<Void>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						EFormAbstractField<?> field =  new KbeeEResourceDistribution();
						field.setModel(new KbeeEResourceDistributionFieldModel());
						addComponent(field);
						target.add(EFormEditor.this);
					}
					@Override
					public String getLabel() {	
						return EFormEditor.this.getLabel("resource-distribution").getObject();
					}
			});
			
			menu.addItem(id -> 
				new AjaxMenuItemPanelV5<Void>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						KbeeEFormRow row =  new KbeeEFormRow();
						addComponent(row);
						target.add(EFormEditor.this);
					}
					@Override
					public String getLabel() {	
						return EFormEditor.this.getLabel("row").getObject();
					}
			});
			
			menu.addItem(id -> 
				new AjaxMenuItemPanelV5<Void>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						EFormSection section =  new KbeeEFormSection();
						addComponent(section);
						target.add(EFormEditor.this);
					}
					@Override
					public String getLabel() {	
						return EFormEditor.this.getLabel("section").getObject();
					}
			});
			
			menu.addItem(id -> 
				new AjaxMenuItemPanelV5<Void>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						EFormComponent text =  new KbeeEText();
						addComponent(text);
						target.add(EFormEditor.this);
					}
					@Override
					public String getLabel() {	
						return EFormEditor.this.getLabel("static-text").getObject();
					}
				});
			
			menu.addItem(id -> 
			new AjaxMenuItemPanelV5<Void>(id) {
				@Override
				public void onClick(AjaxRequestTarget target) {
					EFormAbstractField<?> field =  new KbeeEStringField();
					field.setModel(new KbeeEStringAttributeModel());
					addComponent(field);
					target.add(EFormEditor.this);
				}
				@Override
				public String getLabel() {	
					return EFormEditor.this.getLabel("string").getObject();
				}
			});
			
			menu.addItem(id -> 
				new AjaxMenuItemPanelV5<Void>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						EFormAbstractField<?> field =  new KbeeEStringListField();
						field.setModel(new KbeeEStringAttributeModel());
						addComponent(field);
						target.add(EFormEditor.this);
					}
					@Override
					public String getLabel() {	
						return EFormEditor.this.getLabel("string-list").getObject();
					}
			});

			menu.addItem(id -> 
				new AjaxMenuItemPanelV5<Void>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						EFormAbstractField<?> field =  new KbeeETableField();
						field.setModel(new KbeeETableFieldModel());
						addComponent(field);
						target.add(EFormEditor.this);
					}
					@Override
					public String getLabel() {	
						return EFormEditor.this.getLabel("table").getObject();
					}
			});
			
			menu.addItem(id -> 
				new AjaxMenuItemPanelV5<Void>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						EFormAbstractField<?> field =  new KbeeETextField();
						field.setModel(new KbeeEStringAttributeModel());
						addComponent(field);
						target.add(EFormEditor.this);
					}
					@Override
					public String getLabel() {	
						return EFormEditor.this.getLabel("text").getObject();
					}
			});
			
			menu.addItem(id -> 
				new AjaxMenuItemPanelV5<Void>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						ETitle title =  new KbeeETitle();
						addComponent(title);
						target.add(EFormEditor.this);
					}
					@Override
					public String getLabel() {	
						return EFormEditor.this.getLabelString("title");
					}
			});
			
			menu.addItem(id -> 
				new AjaxMenuItemPanelV5<Void>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						EFormAbstractField<?> field =  new KbeeEDateTimeField();
						KbeeEStringAttributeModel m=new KbeeEStringAttributeModel();
						field.setModel(m);
						addComponent(field);
						target.add(EFormEditor.this);
					}
					@Override
					public String getLabel() {	
						return EFormEditor.this.getLabelString("timestamp");
					}
			});
				
			add(menu);
		}
		@Override
		public boolean isVisible() {
			return isEditionEnabled();
		}
		
	}
	
	public class ComponentRowView extends Fragment {
		private boolean isexpanded = false;
		public ComponentRowView(String id, ComponentWrapper wrapper) {
			super(id, "component-rowview-fragment", EFormEditor.this);
			
			setOutputMarkupId(true);
			
			add(new AjaxSubmitLink("expander") {
				public void onSubmit(AjaxRequestTarget target) {
					expand(target);
				}
				protected void onError(AjaxRequestTarget target) {
					expand(target);
				}
			});
			
			((MarkupContainer)get("expander")).add(new WebMarkupContainer("icon"));
			get("expander:icon").add(new AttributeModifier("class", new Model<String>() {
				public String getObject() {
					return isexpanded ? "far fa-angle-down" : "far fa-angle-up";
				}
			}));
			
			WebMarkupContainer menucell = new WebMarkupContainer("menu-cell") {
				public boolean isVisible() {
					return isEditionEnabled();
				}
			};
			WebMarkupContainer menulink = new WebMarkupContainer("menulink");
			menucell.add(menulink);
			Panel menu = getMenu(new Model<ComponentWrapper>(wrapper));
			menu.add(new AttributeModifier("aria-labelledby", String.valueOf(menulink.getMarkupId()))); 
			menucell.add(menu);
			add(menucell);
						
			add(new Label("type", new Model<String>() {
				public String getObject() {
					return ((EFormAbstractComponent)wrapper.getComponent()).getTypeLabel();
				}
			}));
			
			int padding = wrapper.getLevel() * 20;
			if (padding>0) {
				get("type").add(new AttributeModifier("style", "padding-left:"+String.valueOf(padding)+"px;"));
			}
			
			add(new Label("label", new Model<String>() {
				public String getObject() {
					EFormComponent component = wrapper.getComponent();
					String label =  component.getLabel();
					if (component instanceof ETitle || component instanceof EFormSection) {
						label = "<h2 style=\"margin: 0px;padding-top: 6px;padding-bottom: 6px;\">"+label+"</h2>";
					}
					return label;
				}
			}));
			
		
			((Label)get("label")).setEscapeModelStrings(false);
			
			
			add( ( new Label("model", new Model<String>() {
				public String getObject() {
					return getModel(wrapper);
				}
			})).setEscapeModelStrings(false));
			
			get("model").add(new AttributeModifier("style", new Model<String>() {
				public String getObject() {
					return getError(wrapper)!=null ? "color:red;font-weight: bold;": "";
				}
			}));
		
			WebMarkupContainer expanded = new WebMarkupContainer("expanded-row") {
				public boolean isVisible() {
					return isexpanded;
				}
			};
			expanded.add(new ComponentEditor(wrapper) {
				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					target.add(ComponentRowView.this);
				}
			});
			
			add(expanded);
		}
		
		public void edit(AjaxRequestTarget target) {
			if (!isexpanded) {
				expand(target);
			}
		}
		
		public void expand(AjaxRequestTarget target) {
			isexpanded = !isexpanded;
			target.add(this);
		}
		
		public void collapse() {
			isexpanded = false;
		}
		
		protected String getModel(ComponentWrapper wrapper) {

			String modeldescription = "";
			
			Locale locale = getSessionUser().getLocale();
			
			if (wrapper.getComponent() instanceof EFormField<?>) {
				EFieldModel<?> model =((EFormField<?>)wrapper.getComponent()).getModel();
				

				if (model instanceof EClassifierModel && ((EClassifierModel<?>)model).getClassifier()!=null) {
					modeldescription   = "<a class=\"btn-link\" href=\"/model/classifiers/" +
							((EClassifierModel<?>) model).getClassifier().getId().toString() + "\" target=\"_blank\" >"  + 
							model.getDescription(locale) +
							"</a>";
				}
				else if (model instanceof EAttributeModel && ((EAttributeModel<?>) model).getAttribute()!=null) {
						modeldescription   = "<a class=\"btn-link\" href=\"/model/attributes/" +
								((EAttributeModel<?>) model).getAttribute().getId().toString() + "\" target=\"_blank\" >"  + 
							model.getDescription(locale) +
							"</a>";
				}
					
				else if (model!=null) { 
					modeldescription = model.getDescription(locale);
				}
				else 
					modeldescription = "null";
				
			}
			return modeldescription;
		}
		
		protected String getError(ComponentWrapper wrapper) {
			String message = null;
			if (wrapper.getComponent() instanceof EFormField<?>) {
				EFieldModel<?> model =((EFormField<?>)wrapper.getComponent()).getModel();
				message = model!=null ? model.getErrorMessage(null) : null;
			}
			return message;
		}
		
		protected Panel getMenu(IModel<ComponentWrapper> model) {
			ContextMenuPanel<ComponentWrapper> menu = new ContextMenuPanel<ComponentWrapper>(model);
			menu.addItem(id -> 
				new AjaxMenuItemPanelV5<ComponentWrapper>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						removeComponent(getModelObject());
						target.add(EFormEditor.this);
					}	
					@Override
					public String getLabel() {	
						return "Delete";
					}
				}
			);
			return menu;
		}
	}
	
	public class ComponentEditor extends Fragment implements IFormModelUpdateListener {
		DataEditor dataeditor = null;
		public ComponentEditor(ComponentWrapper wrapper) {
			super("editor", "component-editor-fragment", EFormEditor.this);
			
			List<ITab> tabs = new ArrayList<ITab>();
			
			dataeditor = new DataEditor("panel", wrapper)  {
				@Override
				public void onUpdate(AjaxRequestTarget target) {
					super.onUpdate(target);
					ComponentEditor.this.onUpdate(target);
				}
			};
			
			tabs.add(new AbstractTab(getLabel(wrapper)) {
				@Override
				public WebMarkupContainer getPanel(String panelId) {
					return dataeditor;
				}
			});
			if (wrapper.getComponent() instanceof EFormField) {
				tabs.add(new AbstractTab(new Model<String>("Model")) {
					@Override
					public WebMarkupContainer getPanel(String panelId) {
						return getModelEditor(panelId, wrapper);
					}
				});
				tabs.add(new AbstractTab(new Model<String>("On Update")) {
					@Override
					public WebMarkupContainer getPanel(String panelId) {
						return new OnUpdateEditor(panelId, wrapper);
					}
				});
				if (((EFormField<?>)wrapper.getComponent()).isCalculable()) {
					tabs.add(new AbstractTab(new Model<String>("Calculation")) {
						@Override
						public WebMarkupContainer getPanel(String panelId) {
							return new CalculationEditor(panelId, wrapper);
						}
					});
				}
				tabs.add(new AbstractTab(new Model<String>("Validation")) {
					@Override
					public WebMarkupContainer getPanel(String panelId) {
						return new ValidationEditor(panelId, wrapper);
					}
				});
				tabs.add(new AbstractTab(new Model<String>("Help")) {
					@Override
					public WebMarkupContainer getPanel(String panelId) {
						return new HelpEditor(panelId, wrapper);
					}
				});
			}
			tabs.add(new AbstractTab(new Model<String>("Visibility Condition")) {
				@Override
				public WebMarkupContainer getPanel(String panelId) {
					return new VisibilityEditor(panelId, wrapper);
				}
			});
			tabs.add(new AbstractTab(new Model<String>("Enabled Condition")) {
				@Override
				public WebMarkupContainer getPanel(String panelId) {
					return new ActivationEditor(panelId, wrapper);
				}
			});
			
			final AjaxTabbedPanel<ITab> tabbedpanel = new AjaxTabbedPanel<ITab>("tabs", tabs) {
				protected String getNavCss() {
					return "nav nav-tabs";
				}
				protected void onAjaxUpdate(AjaxRequestTarget target) {
					target.add(this);
					
				}
				protected WebMarkupContainer newLink(final String linkId, final int index) {
					return new AjaxSubmitLink(linkId) {
						@Override
						public void onSubmit(AjaxRequestTarget target) 	{
			 				setSelectedTab(index);
							onAjaxUpdate(target);
						}			
					};
				}	
			};
			
			add(tabbedpanel);
		}
		public void updateModel() {
		}
		protected void onUpdate(AjaxRequestTarget target) {
		}
		WebMarkupContainer getModelEditor(String id, ComponentWrapper wrapper) {
			EFieldModel<?>  model = ((EFormField<?>)wrapper.getComponent()).getModel();
			if (model==null) {
				if (wrapper.getComponent() instanceof ETextField) {
					model = new KbeeEStringAttributeModel();
					((EFormAbstractField<?>)wrapper.getComponent()).setModel(model);
				}
			}
			if (EClassifierModel.GetTypeLabel().equals(model.getTypeLabel())) {
				return new ClassifierModelEditor(id, wrapper) {
					@Override
					public void onUpdate(AjaxRequestTarget target) {
						super.onUpdate(target);
						ComponentEditor.this.onUpdate(target);
					}
				};
			};
			if (EAttributeModel.GetTypeLabel().equals(model.getTypeLabel()) ||
				EFormAttributeModel.GetTypeLabel().equals(model.getTypeLabel())) {
				return new AttributeModelEditor(id, wrapper) {
					@Override
					public void onUpdate(AjaxRequestTarget target) {
						super.onUpdate(target);
						ComponentEditor.this.onUpdate(target);
					}
				};
			};
			if (ERelationModel.GetTypeLabel().equals(model.getTypeLabel())) {
				return new RelationModelEditor(id, wrapper) {
					@Override
					public void onUpdate(AjaxRequestTarget target) {
						super.onUpdate(target);
						ComponentEditor.this.onUpdate(target);
					}
				};
			}
			if (EResourceDistributionModel.GetTypeLabel().equals(model.getTypeLabel())) {
				return new ResourceDistributionEditor(id, wrapper) {
					@Override
					public void onUpdate(AjaxRequestTarget target) {
						super.onUpdate(target);
						ComponentEditor.this.onUpdate(target);
					}
				};
			}
			if (EResourceModel.GetTypeLabel().equals(model.getTypeLabel()) ||
				EResourceSystemModel.GetTypeLabel().equals(model.getTypeLabel())) {
				return new ResourcesModelEditor(id, wrapper) {
					@Override
					public void onUpdate(AjaxRequestTarget target) {
						super.onUpdate(target);
						ComponentEditor.this.onUpdate(target);
					}
				};
			}
			return new ModelEditor(id, wrapper) {
				@Override
				public void onUpdate(AjaxRequestTarget target) {
					super.onUpdate(target);
					ComponentEditor.this.onUpdate(target);
				}
			};
		}
		private IModel<String> getLabel(ComponentWrapper wrapper) {
			return wrapper.getComponent() instanceof EFormField ? 
				new Model<String>("Field") : 
				new Model<String>(((EFormAbstractComponent)wrapper.getComponent()).getTypeLabel());
		}
	}
	
	public class DataEditor extends Fragment {
		public DataEditor(String id, ComponentWrapper wrapper) {
			super(id, "data-editor-fragment", EFormEditor.this);
			add(new TextField<String>("name", new PropertyModel<String>(wrapper.getComponent(), "name")) {
				public void updateModel() {
					if (isEditionEnabled()) super.updateModel();
				}	
			});
			add(new TextField<String>("label", new PropertyModel<String>(wrapper.getComponent(), "label")) {
				@Override
				public void onUpdate(AjaxRequestTarget target) {
					super.onUpdate(target);
					updateModel();
					DataEditor.this.onUpdate(target);
				}
				public void updateModel() {
					if (isEditionEnabled()) super.updateModel();
				}	
			});
			if (wrapper.getComponent() instanceof EFormField) {
				add(new TextField<String>("sublabel", new PropertyModel<String>(wrapper.getComponent(), "sublabel")));
				add(new BooleanField("required", new PropertyModel<Boolean>(wrapper.getComponent(), "required")));
				add(new BooleanField("readOnly", new PropertyModel<Boolean>(wrapper.getComponent(), "readOnly")));
			}
			else {
				add(new InvisiblePanel("sublabel"));
				add(new InvisiblePanel("required"));
				add(new InvisiblePanel("readOnly"));
			}
			add(new TextField<String>("cssClass", new PropertyModel<String>(wrapper.getComponent(), "cssClass")) {
				public void updateModel() {
					if (isEditionEnabled()) super.updateModel();
				}	
			});
			if  (wrapper.getComponent() instanceof EText) {
				add(new TextAreaField<String>("text", new PropertyModel<String>(wrapper.getComponent(), "text")));
			}
			else {
				add(new InvisiblePanel("text"));
			}
			if  (wrapper.getComponent() instanceof EHtmlField) {
				add(new TextField<String>("editor", new PropertyModel<String>(wrapper.getComponent(), "editor")));
			}
			else {
				add(new InvisiblePanel("editor"));
			}
			if  (wrapper.getComponent() instanceof KbeeEResource) {
				add(new BooleanField("toolbar", new PropertyModel<Boolean>(wrapper.getComponent(), "toolbar")) {
					public void updateModel() {
						if (isEditionEnabled()) super.updateModel();
					}	
				});
			}
			else {
				add(new InvisiblePanel("toolbar"));
			}

			if  (wrapper.getComponent() instanceof KbeeEListField || wrapper.getComponent() instanceof EAutoCompleteField) {
				add(new TextField<String>("valueTemplate", new PropertyModel<String>(wrapper.getComponent(), "valueTemplate")) {
					public void updateModel() {
						if (isEditionEnabled()) super.updateModel();
					}	
				});
				add(new TextField<String>("infoTemplate", new PropertyModel<String>(wrapper.getComponent(), "infoTemplate")) {
					public void updateModel() {
						if (isEditionEnabled()) super.updateModel();
					}	
				});
			}
			else {
				add(new InvisiblePanel("valueTemplate"));
				add(new InvisiblePanel("infoTemplate"));
			}
		}
		public void onUpdate(AjaxRequestTarget target) {
		}
	}
	
	public abstract class BaseModelEditor extends Fragment implements IFormModelUpdateListener {
		private EFormField<?> field;
		private String type;
		public BaseModelEditor(String id, String markupid, ComponentWrapper wrapper) {
			super(id, markupid, EFormEditor.this);
			
			setOutputMarkupId(true);
			
			setField((EFormField<?>)wrapper.getComponent());
			setType(getModel().getTypeLabel());
			
			add(new ChoiceField<String>("type", new PropertyModel<String>(this, "type"), ()->getTypes(wrapper)) {
				@Override
				public void onUpdate(AjaxRequestTarget target) {
					super.onUpdate(target);
					updateModel();
					BaseModelEditor.this.onUpdate(target);
				}
			});
		}
		public String getType() {
			return type; 
		}
		public void setType(String type) {
			this.type = type;
		}
		public void setField(EFormField<?> field) {
			this.field = field;
		}
		public EFormField<?> getField() {
			return this.field;
		}
		public EFieldModel<?> getModel() {
			return getField().getModel();
		}
		protected void onUpdate(AjaxRequestTarget target) {
			updateModel();
		}
		protected List<String> getTypes(ComponentWrapper wrapper) {
			return null;
		}
	}
	
	public class ModelEditor extends BaseModelEditor {
		public ModelEditor(String id, ComponentWrapper wrapper) {
			super(id, "model-editor-fragment", wrapper);
		}
		@Override
		public List<String> getTypes(ComponentWrapper wrapper) {
			List<String> types = new ArrayList<String>();
			EFormField<?> field = (EFormField<?>)wrapper.getComponent();
			if (field instanceof  ECheckField) {
				types.add(EFormAttributeModel.GetTypeLabel());
			}
			if (field instanceof ETableField) {
				types.add(ETableModel.GetTypeLabel());
			}
			return types;
		}
		@Override
		public void updateModel() {
		}
	}
	
	public class AttributeModelEditor extends BaseModelEditor {
		private IModel<AttributeTemplate> attributemodel;
		public AttributeModelEditor(String id, ComponentWrapper wrapper) {
			super(id, "attribute-model-editor-fragment", wrapper);
			if (EAttributeModel.GetTypeLabel().equals(getType()))
				setTemplateModel(getTemplate(((EAttributeModel<?>)getModel())));
			add(new ChoiceField<IModel<AttributeTemplate>>("attribute", new PropertyModel<IModel<AttributeTemplate>>(this, "templateModel"), ()->getAttributes((EFormField<?>)wrapper.getComponent())) {
				@Override
				public void onUpdate(AjaxRequestTarget target) {
					super.onUpdate(target);
					updateModel();
					AttributeModelEditor.this.onUpdate(target);
				}
				@Override
				public boolean isVisible() {
					return EAttributeModel.GetTypeLabel().equals(getType());
				}
			});
		}
		public IModel<AttributeTemplate> getTemplateModel() {
			return attributemodel;
		}
		public void setAttribute(AttributeTemplate attribute) {
			this.attributemodel = new ObjectModel<AttributeTemplate>(attribute);
		}
		public void setTemplateModel(IModel<AttributeTemplate> model) {
			this.attributemodel = model;
		}
		public List<String> getTypes(ComponentWrapper wrapper) {
			List<String> types = new ArrayList<String>();
			types.add(EAttributeModel.GetTypeLabel());
			types.add(EFormAttributeModel.GetTypeLabel());
			if (getField() instanceof EStringField) {
				types.add(EContentTitleModel.GetTypeLabel());
			}
			return types;
		}
		@Override
		public void updateModel() {
			((EFormAbstractField<?>)getField()).setModel(getEditedModel());
		}
		private EFieldModel<?> getEditedModel() {
			if (EAttributeModel.GetTypeLabel().equals(getType())) {
				AttributeTemplate template = getTemplateModel()!=null ? 
					getTemplateModel().getObject() : 
					null;
				KbeeEAttributeFieldModel<?> model = null;
				if (getField() instanceof EDateField) {
					model = new KbeeEDateAttributeModel();
				}
				if (getField() instanceof EHtmlStructField) {
					model = new KbeeEHtmlModel();
				}
				else
				if (getField() instanceof EStringField ||
					getField() instanceof ETextField ||
					getField() instanceof EHtmlField ||
					getField() instanceof EListField) {
					model = new KbeeEStringAttributeModel();
				}
				if ((getField() instanceof ENumberField)) {
					model = new KbeeENumberAttributeModel();
				}
				if ((getField() instanceof EBooleanField)) {
					model = new KbeeEBooleanAttributeModel();
				}
				if (model!=null && template!=null) {
					model.setAttribute(template.getAttribute());
					model.setParentClassifier((Classifier)template.getParent());
				}
				return model;
			}
			else 
			if (EFormAttributeModel.GetTypeLabel().equals(getType())) {
				KbeeEFormAttributeModel<?> model = null;
				if (getField() instanceof EDateField) {
					model = new KbeeEDateModel();
				}
				if ((getField() instanceof EBooleanField)) {
					model = new KbeeEBooleanModel();
				}
				if (getField() instanceof EStringField ||
						getField() instanceof EHtmlField ||
						getField() instanceof ETextField) {
					model = new KbeeEStringModel();
				}
				return model;
			}	
			else 
			if (EContentTitleModel.GetTypeLabel().equals(getType())) {
				KbeeEContentTitleModel model = new KbeeEContentTitleModel();
				return model;
				
			}
			return null;
		}
		private List<IModel<AttributeTemplate>> getAttributes(EFormField<?> field) {
			List<IModel<AttributeTemplate>> attributes = EFormEditor.this.getAttributes(field);
			if (getTemplateModel()!=null) {
				attributes.add(getTemplateModel());
			}	
			Collections.sort(attributes, new Comparator<IModel<AttributeTemplate>>() {
				@Override
				public int compare(IModel<AttributeTemplate> a, IModel<AttributeTemplate> b) {
					if (a.getObject().getDisplayName()==null||b.getObject().getDisplayName()==null) 
						return 0;
					return a.getObject().getDisplayName().compareTo(b.getObject().getDisplayName());
				}
			});
			return attributes;
		}
		private IModel<AttributeTemplate> getTemplate(EAttributeModel<?> emodel)	{
			for (ModelElementTemplate template : EFormEditor.this.getContentTemplate().getAttributes()) {
				if (template.getElement().equals(emodel.getAttribute())) {
					if (template.getParent()!=null) {
						if (template.getParent().equals(emodel.getParentClassifier())) {
							return new ObjectModel<AttributeTemplate>((AttributeTemplate)template);
						}
					}
					else {
						if (emodel.getParentClassifier()==null) {
							return new ObjectModel<AttributeTemplate>((AttributeTemplate)template);
						}
					}
				}
			}
			return null;
		}
	}
	
	public class ResourcesModelEditor extends BaseModelEditor {
		private IModel<ResourceTag> tag;
		public ResourcesModelEditor(String id, ComponentWrapper wrapper) {
			this(id, "model-resources-fragment", wrapper);
		}	
		public ResourcesModelEditor(String id, String fragment, ComponentWrapper wrapper) {
			super(id, fragment, wrapper);
			
			if (wrapper.getComponent() instanceof EResourceSystemField) {
				setType(EResourceSystemModel.GetTypeLabel());
			}
			
			setTag(((EResourceModel<?>)getModel()).getTag());

			add(new ChoiceField<IModel<ResourceTag>>("tag", new PropertyModel<IModel<ResourceTag>>(this, "tag"), ()->getResourceTags()) {
				@Override
				public void onUpdate(AjaxRequestTarget target) {
					super.onUpdate(target);
					updateModel();
					ResourcesModelEditor.this.onUpdate(target);
				}
			});
		}	
		public IModel<ResourceTag> getTag() {
			return tag;
		}
		public void setTag(ResourceTag tag) {
			this.tag = tag!=null ? new ObjectModel<ResourceTag>(tag) : null;
		}
		public void setTag(IModel<ResourceTag> model) {
			this.tag = model;
		}
		@Override
		public List<String> getTypes(ComponentWrapper wrapper) {
			List<String> types = new ArrayList<String>();
			EFormField<?> field = (EFormField<?>)wrapper.getComponent();
			if (field instanceof EResourceField) {
				types.add(EResourceModel.GetTypeLabel());
				types.add(ERelationResourceModel.GetTypeLabel());
			}
			if (field instanceof EResourcesField) {
				types.add(EResourceModel.GetTypeLabel());
			}
			if (field instanceof EResourceSystemField) {
				types.add(EResourceSystemModel.GetTypeLabel());
			}
			return types;
		}
		@Override
		public void updateModel() {
			if (EResourceModel.GetTypeLabel().equals(getType()) ||
				EResourceSystemModel.GetTypeLabel().equals(getType())) {
				((EFormAbstractField<?>)getField()).setModel(getEditedModel());
			}
		}
		private EFieldModel<?> getEditedModel() {
			if (EResourceModel.GetTypeLabel().equals(getType())) {
				KbeeEResourceFieldModel model = new KbeeEResourceFieldModel();
				model.setTag(getTag()!=null?getTag().getObject():null);
				return model;
			}
			if (EResourceSystemModel.GetTypeLabel().equals(getType())) {
				KbeeEResourceSystemFieldModel model = new KbeeEResourceSystemFieldModel();
				model.setTag(getTag()!=null?getTag().getObject():null);
				return model;
			}
			return null;
		}
	}
	
	public class ResourceDistributionEditor extends ResourcesModelEditor {
		private List<String> launchers = new ArrayList<>();
		private IModel<ResourceTag> targetTag, doneTag;
		public ResourceDistributionEditor(String id, ComponentWrapper wrapper) {
			super(id, "model-resource-distribution-fragment", wrapper);
			setTargetTag(((KbeeEResourceDistributionFieldModel)getModel()).getTargetTag());
			setDoneTag(((KbeeEResourceDistributionFieldModel)getModel()).getDoneTag());
			setLaunchers(((KbeeEResourceDistributionFieldModel)getModel()).getLaunchersId());
			add(new ListView<ProcessLauncher>("launcher", ()->getLaunchers()) {
				public void populateItem(ListItem<ProcessLauncher> item) {
					String label = item.getModelObject().getLabel();
					String id = String.valueOf(item.getModelObject().getId());
					IModel<Boolean> checkmodel = new Model<Boolean>() {
						public Boolean getObject() {
							return launchers.contains(id);
						}
						public void setObject(Boolean value) {
							if (value) {
								if (!launchers.contains(id)) {
									launchers.add(id);
								}
							}
							else {
								launchers.remove(id);
							}
						}
					};
					CheckField field = new CheckField("check", checkmodel) {
						@Override
						public IModel<String> getLabel() {
							return new Model<String>(label);
						}
					};
					item.add(field);
				}
			});
			add(new ChoiceField<IModel<ResourceTag>>("targetTag", new PropertyModel<IModel<ResourceTag>>(this, "targetTag"), ()->getResourceTags()) {
				@Override
				public void onUpdate(AjaxRequestTarget target) {
					super.onUpdate(target);
					updateModel();
					ResourceDistributionEditor.this.onUpdate(target);
				}
			});
			add(new ChoiceField<IModel<ResourceTag>>("doneTag", new PropertyModel<IModel<ResourceTag>>(this, "doneTag"), ()->getResourceTags()) {
				@Override
				public void onUpdate(AjaxRequestTarget target) {
					super.onUpdate(target);
					updateModel();
					ResourceDistributionEditor.this.onUpdate(target);
				}
			});
		}
		@Override
		public List<String> getTypes(ComponentWrapper wrapper) {
			List<String> types = new ArrayList<String>();
			EFormField<?> field = (EFormField<?>)wrapper.getComponent();
			if (field instanceof KbeeEResourceDistribution) {
				types.add(EResourceDistributionModel.GetTypeLabel());
			}
			else {
				types.addAll(super.getTypes(wrapper));
			}	
			return types;
		}
		public void setLaunchers(List<String> launchers) {
			if (launchers!=null)
			this.launchers.addAll(launchers);
		}
		public List<ProcessLauncher> getLaunchers() {
			List<ProcessLauncher> launchers = new ArrayList<>();
			List<ProcessLauncher> list = getDomain().getService(WorkflowDomainService.class).getLaunchers();
			for (ProcessLauncher launcher: list) {
				if (launcher.isEnabled() && 
					launcher.executeable() && 
					launcher.getContentTemplate()!=null && 
					launcher.getContentTemplate().getState()==ObjectState.ENABLED) { 
					launchers.add(launcher);
				}	
			}
			return launchers;
		}
		public IModel<ResourceTag> getTargetTag() {
			return targetTag;
		}
		public void setTargetTag(ResourceTag tag) {
			this.targetTag = tag!=null ? new ObjectModel<ResourceTag>(tag) : null;
		}
		public void setTargetTag(IModel<ResourceTag> model) {
			this.targetTag = model;
		}
		public IModel<ResourceTag> getDoneTag() {
			return doneTag;
		}
		public void setDoneTag(ResourceTag tag) {
			this.doneTag = tag!=null ? new ObjectModel<ResourceTag>(tag) : null;
		}
		public void setDoneTag(IModel<ResourceTag> model) {
			this.doneTag = model;
		}
		@Override
		public void updateModel() {
			((EFormAbstractField<?>)getField()).setModel(getEditedModel());
		}
		private EFieldModel<?> getEditedModel() {
			KbeeEResourceDistributionFieldModel model = new KbeeEResourceDistributionFieldModel();
			model.setTag(getTag()!=null?getTag().getObject():null);
			model.setTargetTag(getTargetTag()!=null?getTargetTag().getObject():null);
			model.setDoneTag(getDoneTag()!=null?getDoneTag().getObject():null);
			model.setLaunchersId(launchers);
			return model;
		}
	}	
	
	public class ClassifierModelEditor extends BaseModelEditor {
		private IModel<ClassifierTemplate> classifiermodel;
		private AccessStrategy accessibility;
		private String iql;
		public ClassifierModelEditor(String id, ComponentWrapper wrapper) {
			super(id, "classifier-model-editor-fragment", wrapper);
			
			setTemplateModel(getTemplate(((EClassifierModel<?>)getModel())));
			setAccessibility(((EClassifierModel<?>)getModel()).getAccessStrategy());
			setIql(((EClassifierModel<?>)getModel()).getIql());
			
			if (getAccessibility()==null && getTemplateModel()!=null) {
				setAccessibility(getTemplateModel().getObject().getAccessibility());
			}	

			add(new ChoiceField<IModel<ClassifierTemplate>>("classifier", new PropertyModel<IModel<ClassifierTemplate>>(this, "templateModel"), ()->getClassifiers()) {
				@Override
				public void onUpdate(AjaxRequestTarget target) {
					super.onUpdate(target);
					updateModel();
					setAccessibility(getTemplateModel().getObject().getAccessibility());
					ClassifierModelEditor.this.onUpdate(target);
				}
			});
			
			add(new ChoiceField<AccessStrategy>("accessibility", new PropertyModel<AccessStrategy>(this, "accessibility"), ()->getAccessibilities()) {
				@Override
				public void onUpdate(AjaxRequestTarget target) {
					super.onUpdate(target);
					updateModel();
					ClassifierModelEditor.this.onUpdate(target);
				}
			});
			
			add(new TextField<String>("iql", new PropertyModel<String>(this, "iql")) {
				@Override
				public void onUpdate(AjaxRequestTarget target) {
					super.onUpdate(target);
					updateModel();
					ClassifierModelEditor.this.onUpdate(target);
				}
				@Override
				public boolean isVisible() {
					return AccessStrategy.Iql.equals(getAccessibility());
				}
			});
		}
		public IModel<ClassifierTemplate> getTemplateModel() {
			return classifiermodel;
		}
		public void setTemplate(ClassifierTemplate classifier) {
			this.classifiermodel = new ObjectModel<ClassifierTemplate>(classifier);
		}
		public void setTemplateModel(IModel<ClassifierTemplate> model) {
			this.classifiermodel = model;
		}
		public AccessStrategy getAccessibility() {
			return accessibility;
		}
		public void setAccessibility(AccessStrategy accessibility) {
			this.accessibility = accessibility;
		}
		public String getIql() {
			return iql;
		}
		public void setIql(String iql) {
			this.iql = iql;
		}
		public List<AccessStrategy> getAccessibilities() {
			List<AccessStrategy> accessibilities = new ArrayList<AccessStrategy>();
			accessibilities.add(AccessStrategy.All);
			accessibilities.add(AccessStrategy.Roles);
			accessibilities.add(AccessStrategy.Iql);
			accessibilities.add(AccessStrategy.Script);
			if (classifiermodel!=null) {
				Classifier classifier = classifiermodel.getObject().getClassifier();
				DataSet dataset = classifier.getDataSet();
				if (dataset.getDataSetType().equals(DataSetType.SECURED)) {
					accessibilities.add(AccessStrategy.Writeables);
					if (classifier.isHierarchical()) {
						accessibilities.add(AccessStrategy.ChildsEnabled);
					}
				}
			}
			return accessibilities;
		}
		public List<String> getTypes(ComponentWrapper wrapper) {
			List<String> types = new ArrayList<String>();
			types.add(EClassifierModel.GetTypeLabel());
			return types;
		}
		@Override
		public void updateModel() {
			((EFormAbstractField<?>)getField()).setModel(getEditedModel());
		}
		private EFieldModel<?> getEditedModel() {
			ClassifierTemplate template = getTemplateModel()!=null ? 
				getTemplateModel().getObject() : 
				null;
			KbeeEClassifierFieldModel model = new KbeeEClassifierFieldModel();
			if (template!=null) {
				model.setClassifier(template.getClassifier());
				model.setParentClassifier((Classifier)template.getParent());
				model.setAccessStrategy(getAccessibility());
				model.setIql(getIql());
			}
			return model;
		}
		private List<IModel<ClassifierTemplate>> getClassifiers() {
			List<IModel<ClassifierTemplate>> classifiers = EFormEditor.this.getClassifiers();
			if (getTemplateModel()!=null) {
				final Classifier classifier = getTemplateModel().getObject().getClassifier(); 
				classifiers.add(getTemplateModel());
				classifiers.removeIf(model -> classifier.equals(model.getObject().getParent()));
			}	
			Collections.sort(classifiers, new Comparator<IModel<ClassifierTemplate>>() {
				@Override
				public int compare(IModel<ClassifierTemplate> a, IModel<ClassifierTemplate> b) {
					if (a.getObject().getDisplayName()==null||b.getObject().getDisplayName()==null) 
						return 0;
					return a.getObject().getDisplayName().compareTo(b.getObject().getDisplayName());
				}
			});
			return classifiers;
		}
		private IModel<ClassifierTemplate> getTemplate(EClassifierModel<?> emodel)	{
			for (ModelElementTemplate template : EFormEditor.this.getContentTemplate().getClassifiers()) {
				if (template.getElement().equals(emodel.getClassifier())) {
					if (template.getParent()!=null) {
						if (template.getParent().equals(emodel.getParentClassifier())) {
							return new ObjectModel<ClassifierTemplate>((ClassifierTemplate)template);
						}
					}
					else {
						if (emodel.getParentClassifier()==null) {
							return new ObjectModel<ClassifierTemplate>((ClassifierTemplate)template);
						}
					}
				}
			}
			return null;
		}
	}
	
	public class RelationModelEditor extends BaseModelEditor {
		private IModel<RelationTemplate> relation;
		public RelationModelEditor(String id, ComponentWrapper wrapper) {
			super(id, "relation-model-editor-fragment", wrapper);
			setRelation(((ERelationModel<?>)getModel()).getRelation());
			add(new ChoiceField<IModel<RelationTemplate>>("relation", new PropertyModel<IModel<RelationTemplate>>(this, "relation"), ()->getRelations()) {
				@Override
				public void onUpdate(AjaxRequestTarget target) {
					super.onUpdate(target);
					updateModel();
					RelationModelEditor.this.onUpdate(target);
				}
				@Override
				protected String getDisplayValue(IModel<RelationTemplate> value) {
					RelationTemplate template = value.getObject();
					if (template.getSourceTemplate().equals(getContentTemplate())) {
						return template.getTargetLabel();
					}
					else {
						return template.getReverseLabel() + " (Reverse)";
					}
				}
			});
		}
		public IModel<RelationTemplate> getRelation() {
			return relation;
		}
		public void setRelation(RelationTemplate relation) {
			this.relation = new ObjectModel<RelationTemplate>(relation);
		}
		public void setRelation(IModel<RelationTemplate> model) {
			this.relation = model;
		}
		public List<String> getTypes(ComponentWrapper wrapper) {
			List<String> types = new ArrayList<String>();
			types.add(ERelationModel.GetTypeLabel());
			return types;
		}
		public boolean isReverse() {
			return relation!=null && !relation.getObject().getSourceTemplate().equals(getContentTemplate());
		}
		@Override
		public void updateModel() {
			((EFormAbstractField<?>)getField()).setModel(getEditedModel());
		}
		private EFieldModel<?> getEditedModel() {
			RelationTemplate template = getRelation()!=null ? 
				getRelation().getObject() : 
				null;
			KbeeERelationFieldModel model = new KbeeERelationFieldModel();
			model.setRelation(template);
			model.setReverse(isReverse());
			return model;
		}
	}	

	
	public class HelpEditor extends Fragment {
		public HelpEditor(String id, ComponentWrapper wrapper) {
			super(id, "help-editor-fragment", EFormEditor.this);
			add(new TextAreaField<String>("text", new PropertyModel<String>(wrapper.getComponent(), "helpText")));
		}
	}
	
	public class ValidationEditor extends Fragment implements IFormModelUpdateListener {
		private EFormField<?> field;
		private String script, message;
		public ValidationEditor(String id, ComponentWrapper wrapper) {
			super(id, "validation-editor-fragment", EFormEditor.this);
			setField((EFormField<?>)wrapper.getComponent());
			setValidation(wrapper.getComponent());
			add(new TextAreaField<String>("script", new PropertyModel<String>(this, "script")));
			add(new TextField<String>("message", new PropertyModel<String>(this, "message")));
		}
		public String getScript() {
			return this.script;
		}
		public String getMessage() {
			return this.message;
		}
		public void setScript(String code) {
			this.script = code;
		}
		public void setMessage(String message) {
			this.message = message;
		}
		public void setField(EFormField<?> field) {
			this.field = field;
		}
		public EFormField<?> getField() {
			return this.field;
		}
		@Override
		public void updateModel() {
			EValidation validation = getValidation();
			((EFormAbstractField<?>)getField()).clearValidations();
			if (validation!=null) {
				((EFormAbstractField<?>)getField()).addValidation(validation);
			}
		}
		private EValidation getValidation() {
			if (null!=getScript() && !"".equals(getScript())) {
				KbeeEConditionValidation validation = new KbeeEConditionValidation();
				validation.setCondition(getScript());
				validation.setMessage(getMessage());
				return validation;
			}
			else {
				return null;
			}
		}
		private void setValidation(EFormComponent component) {
			for (EValidation validation : ((EFormField<?>)component).getValidations()) {
				if (validation instanceof KbeeEConditionValidation) {
					setScript(((KbeeEConditionValidation)validation).getCondition());
					setMessage(((KbeeEConditionValidation)validation).getMessage());
					break;
				}
			}
		}
	}
	
	public class VisibilityEditor extends Fragment {
		public VisibilityEditor(String id, ComponentWrapper wrapper) {
			super(id, "visibility-editor-fragment", EFormEditor.this);
			add(new TextAreaField<String>("condition", new PropertyModel<String>(wrapper.getComponent(), "visibleCondition")) {
				@Override
				public boolean isHelpInfo() {
					return true;
				}
				@Override
				public void onHelp(AjaxRequestTarget target) {
					getHelpModal().open(target, () -> { return "How to write a Script"; }, getScriptHelp());
				}
			});
		}
	}
	
	public class ActivationEditor extends Fragment {
		public ActivationEditor(String id, ComponentWrapper wrapper) {
			super(id, "activation-editor-fragment", EFormEditor.this);
			add(new TextAreaField<String>("condition", new PropertyModel<String>(wrapper.getComponent(), "enabledCondition")) {
				@Override
				public boolean isHelpInfo() {
					return true;
				}
				@Override
				public void onHelp(AjaxRequestTarget target) {
					getHelpModal().open(target, () -> { return "How to write a Script"; }, getScriptHelp());
				}
			});
		}
	}
	
	public class CalculationEditor extends Fragment {
		public CalculationEditor(String id, ComponentWrapper wrapper) {
			super(id, "calculation-editor-fragment", EFormEditor.this);
			add(new TextAreaField<String>("calculation", new PropertyModel<String>(wrapper.getComponent(), "calculation")) {
				@Override
				public boolean isHelpInfo() {
					return true;
				}
				@Override
				public void onHelp(AjaxRequestTarget target) {
					getHelpModal().open(target, () -> { return "How to write a Script"; }, getScriptHelp());
				}
			});
		}
	}
	
	public class OnUpdateEditor extends Fragment {
		public OnUpdateEditor(String id, ComponentWrapper wrapper) {
			super(id, "onupdate-editor-fragment", EFormEditor.this);
			add(new TextAreaField<String>("onUpdate", new PropertyModel<String>(wrapper.getComponent(), "onUpdate")) {
				@Override
				public boolean isHelpInfo() {
					return true;
				}
				@Override
				public void onHelp(AjaxRequestTarget target) {
					getHelpModal().open(target, () -> { return "How to write a Script"; }, getScriptHelp());
				}
			});
		}
	}


	/*** ----------------------------------------------------
	 * 
	 * @param id
	 * @param templatemodel
	 * @param model
	 */
	public EFormEditor(String id, IModel<ContentTemplate> templatemodel, IModel<EForm> model) {
		super(id, model);
		
		setTemplate(templatemodel);
		setEditionEnabled(false);
		
		Label title = new Label("title", new StringResourceModel("eform-title", this, null).setParameters( new Object[] {  EFormEditor.this.getModel().getObject().getName() }));
		add(title);
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL) {
			@Override
			public void process(IFormSubmitter submitter) {
				super.process(submitter);
			}
		};

		AlertPanel<Void> pa=new AlertPanel<Void>("template-structure",AlertPanel.INFO,  
				null,
				null,
				new StringResourceModel("template-structure", this, null).setParameters( new Object[] { EFormEditor.this.getContentTemplate().getDisplayName(),getServerUrl()+"/model/contentclass/" +	EFormEditor.this.getContentTemplate().getId().toString()+"?tab=structure"}));
		pa.setIcon(AlertPanel.HELP_INFO);
		form.add(pa);
		
		form.add(new TextField<String>("name"));
		form.add(new TextField<String>("displayName"));
		form.add(new TextField<String>("cssClass"));
		form.add(new ChoiceField<EFormAccessLevel>("formAccessLevel", new PropertyModel<List<EFormAccessLevel>>(this, "accessLevels")));
		form.add(new BooleanField("useInline"));
		form.add(new BooleanField("fileContainer"));
		form.add(new ComponentsGrid());
		
		add(form);
		
		form.add(new FactoryPanel());

		
		add(new EditButtonsV5<EForm>(this) {
			public void onSubmitClick(AjaxRequestTarget target)  {
				super.onSubmitClick(target);
			}
			public void onCancelClick(AjaxRequestTarget target)  {
				super.onCancelClick(target);
			}
			@Override
			public boolean isVisible() {
				return true;
			}
			@Override
			public boolean isEnabled() {
				return is_domain_admin || is_model;
			}
		});
		
		add(new InfoDialog("help-modal"));
		
		wrappers = getComponents();
	}
	
	public List<ComponentWrapper> getWrappers() {
		return wrappers;
	}
	
	public IModel<ContentTemplate> getTemplateModel() {
		return templatemodel;
	}
	
	public void setTemplate(IModel<ContentTemplate> model) {
		this.templatemodel = model;
	}
	
	public ContentTemplate getContentTemplate() {
		return templatemodel.getObject();
	}
	
	public List<ComponentWrapper> getComponents() {
		return getComponents(getModelObject().getComponents(), 0);
	}
	
	public List<EFormAccessLevel> getAccessLevels() {
		List<EFormAccessLevel> usages = new ArrayList<EFormAccessLevel>();
		
		usages.add(EFormAccessLevel.GENERAL);  			// toda la app
		usages.add(EFormAccessLevel.WORKFLOW); 			// solo en las tareas
		usages.add(EFormAccessLevel.INTERNAL_INFO); 	// info interna
		usages.add(EFormAccessLevel.GENERAL_LIBRARY); 
		usages.add(EFormAccessLevel.GENERAL_PORTAL);
		usages.add(EFormAccessLevel.PROCESS_LAUNCHER);
		 
		return usages;
	}
	
	@Override
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				KbeeEForm eform = (KbeeEForm)getModelObject();
				eform.setComponents(getUpdatedComponents());
				eform.getService(DomService.class).update(getUpdatedParts());
				reset();
			}
		}
		catch (Exception e) {
			fire(new ErrorEvent<>(target, e));
		}
	}
	
	private void addComponent(EFormComponent component) {
		setUpdatedPart("New "+((EFormAbstractComponent)component).getTypeLabel());
		getWrappers().add(new ComponentWrapper(component, 0));
	}
	
	private void removeComponent(ComponentWrapper wrappertodelete) {
		getWrappers().remove(wrappertodelete);
		int level = 0;
		EFormComponent prev = null;
		for (ComponentWrapper wrapper : getWrappers()) {
			if (wrapper.getLevel()>level) {
				if (prev!=null && prev instanceof EFormContainer) {
					if (wrapper.getLevel()>level+1) {
						wrapper.setLevel(level+1);
					}
					level = wrapper.getLevel();
				}
				else {
					wrapper.setLevel(level);
				}
			}
			else {
				level = wrapper.getLevel();
			}
			prev = wrapper.getComponent();
		}
		setUpdatedPart("remove "+((EFormAbstractComponent)wrappertodelete.getComponent()).getTypeLabel());
	}
	
	private List<ComponentWrapper> getComponents(List<EFormComponent> components, int level) {
		List<ComponentWrapper> wrappers = new ArrayList<ComponentWrapper>();
		for (EFormComponent component : components) {
			wrappers.add(new ComponentWrapper(component, level));
			if (component instanceof EFormContainer) {
				wrappers.addAll(getComponents(((EFormContainer)component).getComponents(), level+1));
			}
		}
		return wrappers;
	}
	
	private List<EFormComponent> getUpdatedComponents() {
		List<EFormComponent> components = new ArrayList<EFormComponent>();
		
		EFormContainer container = null;
		List<ComponentWrapper> containers = new ArrayList<ComponentWrapper>();
		List<EFormComponent> containercomponents = new ArrayList<EFormComponent>();
		List<List<EFormComponent>> containerscomponents = new ArrayList<List<EFormComponent>>();
		
		KbeeEFormSection sectionroot = new KbeeEFormSection();
		sectionroot.setName("form");
		container = sectionroot;
		int containerlevel = -1;
		containers.add(new ComponentWrapper(sectionroot, -1));
		containerscomponents.add(containercomponents);
		
		for (ComponentWrapper wrapper : getWrappers()) {
			EFormComponent component = wrapper.getComponent();
			
			while (true) {
				if (wrapper.getLevel()>containerlevel) {
					containercomponents.add(component);
					break;
				}	
				else {
					if (container!=null) {
						container.setComponents(containercomponents);
						containers.remove(0);
						containerscomponents.remove(0);
						if (!containers.isEmpty()) {
							container = (EFormContainer)containers.get(0).getComponent();
							containercomponents = containerscomponents.get(0);
							containerlevel = containers.get(0).getLevel();
						}
						else {
							containerlevel = -1;
							container = null;
						}
					}
				}
			}
			
			if (component instanceof EFormContainer) {
				containers.add(0, wrapper);
				container = (EFormContainer)component;
				containercomponents = new ArrayList<EFormComponent>();
				containerscomponents.add(0, containercomponents);
				containerlevel =  wrapper.getLevel();
			}	
		}
		
		while (container!=null) {
			container.setComponents(containercomponents);
			containers.remove(0);
			containerscomponents.remove(0);
			if (!containers.isEmpty()) {
				container = (EFormContainer)containers.get(0).getComponent();
				containercomponents = containerscomponents.get(0);
			}
			else {
				components.addAll(container.getComponents());
				container = null;
			}
		}
		
		return components;
	}
	
	private List<IModel<ClassifierTemplate>> getClassifiers() {
		List<IModel<ClassifierTemplate>> models = new ArrayList<IModel<ClassifierTemplate>>();
		for (ModelElementTemplate elementtemplate : getContentTemplate().getStructure()   ) {
			if (elementtemplate instanceof ClassifierTemplate) {
				if (!contains(((ClassifierTemplate)elementtemplate).getClassifier())) {
					Classifier parent = (Classifier)((ClassifierTemplate)elementtemplate).getParent();
					if (parent==null || contains(parent)) {
						models.add(new ObjectModel<ClassifierTemplate>((ClassifierTemplate)elementtemplate));
					}
				}
			}
		}
		return models;
	}
	
	private boolean contains(Classifier classifier) {
		if (classifier!=null)
		for (ComponentWrapper wrapper : getWrappers()) {
			if (wrapper.getComponent() instanceof EFormField) {
				EFieldModel<?> model = ((EFormField<?>)wrapper.getComponent()).getModel(); 
				if (model instanceof EClassifierModel) {
					if (classifier.equals(((EClassifierModel<?>)model).getClassifier())) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private List<IModel<AttributeTemplate>> getAttributes(EFormField<?> field) {
		List<IModel<AttributeTemplate>> models = new ArrayList<IModel<AttributeTemplate>>();
		for (ModelElementTemplate template : getContentTemplate().getStructure()) {
			if (template instanceof AttributeTemplate && !contains(((AttributeTemplate)template).getAttribute())) {
				AttributeType type = ((AttributeTemplate)template).getAttribute().getType();
				Classifier parent = (Classifier)((AttributeTemplate)template).getParent();
				if (parent==null || contains(parent)) {
					if (field instanceof EDateField) {
						if (AttributeType.TIMESTAMP.equals(type)) {
							models.add(new ObjectModel<AttributeTemplate>((AttributeTemplate)template));
						}
						else 
						if (AttributeType.DATE.equals(type) || 
							AttributeType.VALIDITY_FROM.equals(type) ||
							AttributeType.VALIDITY_TO.equals(type)) {
							models.add(new ObjectModel<AttributeTemplate>((AttributeTemplate)template));
						}
					}
					if ((field instanceof EStringField || 
							field instanceof EHtmlField || 
							field instanceof EHtmlStructField || 
							field instanceof ETextField || 
							field instanceof EListField) && AttributeType.STRING.equals(type)) {
						models.add(new ObjectModel<AttributeTemplate>((AttributeTemplate)template));
					}
					if ((field instanceof EHtmlField || field instanceof ETextField) && AttributeType.TEXT.equals(type)) {
						models.add(new ObjectModel<AttributeTemplate>((AttributeTemplate)template));
					}
					if ((field instanceof EHtmlStructField) && AttributeType.HTML.equals(type)) {
						models.add(new ObjectModel<AttributeTemplate>((AttributeTemplate)template));
					}
					if (field instanceof ENumberField && AttributeType.NUMBER.equals(type)) {
						models.add(new ObjectModel<AttributeTemplate>((AttributeTemplate)template));
					}
					if (field instanceof ENumberField && AttributeType.FLOAT.equals(type)) {
						models.add(new ObjectModel<AttributeTemplate>((AttributeTemplate)template));
					}
					if (field instanceof EBooleanField && AttributeType.BOOLEAN.equals(type)) {
						models.add(new ObjectModel<AttributeTemplate>((AttributeTemplate)template));
					}
				}
			}
		}
		return models;
	}
	
	private boolean contains(Attribute attribute) {
		if (attribute!=null)
		for (ComponentWrapper wrapper : getWrappers()) {
			if (wrapper.getComponent() instanceof EFormField) {
				EFieldModel<?> model = ((EFormField<?>)wrapper.getComponent()).getModel(); 
				if (model instanceof EAttributeModel) {
					if (attribute.equals(((EAttributeModel<?>)model).getAttribute())) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private IModel<String> getScriptHelp() {
		return new Model<String>(ScriptEvaluator.GetHelpText(getModelObject()));
	}
	
	private List<IModel<ResourceTag>> getResourceTags() {
		List<IModel<ResourceTag>> models = new ArrayList<IModel<ResourceTag>>();
		for (ResourceTag tag : getContentTemplate().getResourceTags()) {
			models.add(new ObjectModel<ResourceTag>(tag));
		}
		return models;
	}
	
	private List<IModel<RelationTemplate>> getRelations() {
		List<IModel<RelationTemplate>> models = new ArrayList<IModel<RelationTemplate>>();
		for (RelationTemplate template : getContentTemplate().getRelations()) {
			models.add(new ObjectModel<RelationTemplate>(template));
		}
		for (RelationTemplate template : getContentTemplate().getReverseRelations()) {
			models.add(new ObjectModel<RelationTemplate>(template));
		}
		return models;
	}
	
	private InfoDialog getHelpModal() {
		return (InfoDialog) get("help-modal");
	}
	
	private User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
}
