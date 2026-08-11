package kbee.web.model.contentclass;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.ModelElementTemplate;
import com.novamens.content.model.ModelSection;
import com.novamens.content.service.DOMObjectService;
import com.novamens.kbee.content.model.KbeeContentTemplate;
import com.novamens.kbee.content.model.KbeeModelSection;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.model.ObjectModel;

import kbee.util.logging.Logger;
import kbee.web.editor.DomainObjectEditor;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;

@SuppressWarnings("serial")
public class ContentClassStructureEditor extends DomainObjectEditor<ContentTemplate> {
	private static final long serialVersionUID = 1L;
					
	private static Logger logger = Logger.getLogger(ContentClassStructureEditor.class.getName());
	
	private List<Panel> panels = null;
	private List<IModel<ModelSection>> deleted = null;
	
	private class NewSectionModel implements IModel<ModelSection> {
		IModel<ContentTemplate> templatemodel;
		boolean isDefault;
		ModelSection section = null;
		public NewSectionModel(ModelSection section) {
			this.section = section;
			this.templatemodel = new ObjectModel<ContentTemplate>(((KbeeModelSection)section).getContentTemplate());
		}
		public ModelSection getObject() {
			if (section==null) {
				section = new KbeeModelSection(templatemodel.getObject());
				((KbeeModelSection)section).setDefault(isDefault);
			}
			return section;
		}
		public void setObject(ModelSection section) {
			
		}
		public void detach() {
			templatemodel.detach();
			if (section!=null)
			isDefault = ((KbeeModelSection)section).isDefault();
			section=null;
		}
	}
	
	/**
	 * @param id
	 * @param model
	 */
	public ContentClassStructureEditor(String id, IModel<ContentTemplate> model) {
		super(id, model);
		
		final boolean role_admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
		
		setOutputMarkupId(true);
		setEditionEnabled(false);
		
		add(new Label("title", new StringResourceModel("title", this, null).setParameters(getModel().getObject().getDisplayName())));
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);

		
		/**
		AjaxLink<Void> s=new AjaxLink<Void> ("sort") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				ContentClassStructureEditor.this.sort(target);
			}
		};
		
	 form.add(s);
	 **/
				
				
		
		
		form.add(new ListView<Panel>("section", getSectionsPanels()) {
			protected void populateItem(ListItem<Panel> item){
//				Label sn = new Label("section-name", ((ContentClassSectionEditor) item.getModelObject()).getSectionModel().getObject().getName());
//				item.add(sn);
//				Panel menuPanel = getMenu(item.getIndex());
//				WebMarkupContainer menulink = new WebMarkupContainer("menulink") {
//					public boolean isVisible() {
//						return isEditionEnabled();
//					}
//				};
//				item.add(menulink);
//				item.add(menuPanel);
				item.addOrReplace(item.getModelObject());
				item.detach();
			}
		});	
		
		add(form);
		
//		add(new AjaxLink<Void>("new-section") {
//			@Override
//			public boolean isVisible() {
//				return isEditionEnabled();
//			}
//			@Override
//			public void onClick(AjaxRequestTarget target) {
//				getSectionsPanels();
//				ModelSection section = new KbeeModelSection(ContentClassStructureEditor.this.getModelObject());
//				panels.add(new ContentClassSectionEditor("editor", ContentClassStructureEditor.this.getModel(section)) {
//					protected List<ModelElementTemplate> getTemplateStructure() {
//						return ContentClassStructureEditor.this.getTemplateStructure();
//					}
//				});
//				target.add(ContentClassStructureEditor.this);
//			}
//		});
		
		add(new EditButtonsV5<ContentTemplate>(this) {
			@Override
			public boolean isEnabled() {
				if (isRoot())
					return true;
				
				if (getModel().getObject().isOnlyRootEdit())
					return false;

				
				return (role_admin && !isExpressVersion());
			}
		});
	}

	protected void sort(AjaxRequestTarget target) {

		
		
		
	}

	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				List<ModelSection> sections = new ArrayList<ModelSection>();
				for (Panel panel : getSectionsPanels()) {
					ContentClassSectionEditor editor = (ContentClassSectionEditor)panel;
					ModelSection section = (ModelSection)editor.getSectionModel().getObject();
					if (((KbeeModelSection)section).isDefault()) {
						((KbeeModelSection)section).setStructure(section.getStructure());
					}
					sections.add(section);
				}
				((KbeeContentTemplate)getModelObject()).setSections(sections);
				if (deleted!=null && !deleted.isEmpty()) {
					deleted.clear();
				}
				getModelObject().getService(DOMObjectService.class).update();
				super.reset();
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<Void>(target, e));
		}
	}
	
	
	protected List<ModelElementTemplate> getTemplateStructure() {
		return getTemplateStructure(true); 
	}
	
	
	/**
	 * @param sort
	 * @return
	 */
	protected List<ModelElementTemplate> getTemplateStructure(boolean sort) {
		
		List<ModelElementTemplate> structure = new ArrayList<ModelElementTemplate>();
		for (Panel sectionPanel : getSectionsPanels()) {
			structure.addAll(((ContentClassSectionEditor)sectionPanel).getStructure());
		}
		
		
		if (sort) {
			structure.sort(new Comparator<ModelElementTemplate> () {
				@Override
				public int compare(ModelElementTemplate o1, ModelElementTemplate o2) {
					try {
							return o1.getDisplayName().compareToIgnoreCase(o2.getDisplayName());
					} catch (Exception e) {
						logger.error(e);
						return 0;
					}
				}
			});
			
			//for (ModelElementTemplate m:structure) {
			//	
			//}
		}
		
		
		
		
		return structure;
	}
	
	
	
	private List<Panel> getSectionsPanels() {
		
		if (panels!=null) 
			return panels;
		
		panels = new ArrayList<Panel>();
		
		for (ModelSection section : getModelObject().getSections()) {
			if (section!=null)
			panels.add(new ContentClassSectionEditor("editor", getModel(section)) {
				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					//target.add(ContentClassStructureEditor.this);
				}
				protected List<ModelElementTemplate> getTemplateStructure() {
					return ContentClassStructureEditor.this.getTemplateStructure();
				}
			});
		}
		return panels;
	}
	
	private IModel<ModelSection> getModel(ModelSection section) {
		if (((KbeeModelSection)section).getId()==null)
			return new NewSectionModel(section);
		else
			return new ObjectModel<ModelSection>(section);
	}
	
	private Panel getMenu(final int index) {
		
		ContentClassSectionEditor editor = (ContentClassSectionEditor)getSectionsPanels().get(index);
		
		ContextMenuPanel<ModelSection> menu = new ContextMenuPanel<ModelSection>(editor.getSectionModel());
		
		menu.addItem((id)->
			new AjaxMenuItemPanelV5<ModelSection>(id) {
				@Override
				public void onClick(AjaxRequestTarget target) {
					Panel downpanel = getSectionsPanels().get(index-1);
					Panel uppanel = getSectionsPanels().get(index);
					getSectionsPanels().set(index, downpanel);
					getSectionsPanels().set(index-1, uppanel);
					target.add(ContentClassStructureEditor.this);
					setUpdatedPart("sections order");
				}
				@Override
				public String getLabel() {	
					return new StringResourceModel("menu.up", ContentClassStructureEditor.this, null).getString();
				}
				@Override
				public boolean isVisible() {
					return index>0;
				}
			}	
		);
		
		menu.addItem((id)->
			new AjaxMenuItemPanelV5<ModelSection>(id) {
				@Override
				public void onClick(AjaxRequestTarget target) {
					Panel uppanel = getSectionsPanels().get(index+1);
					Panel downpanel = getSectionsPanels().get(index);
					getSectionsPanels().set(index, uppanel);
					getSectionsPanels().set(index+1, downpanel);
					target.add(ContentClassStructureEditor.this);
					setUpdatedPart("sections order");
				}
				@Override
				public String getLabel() {
					return new StringResourceModel("menu.down", ContentClassStructureEditor.this, null).getString();
				}
				@Override
				public boolean isVisible() {
					return index<getSectionsPanels().size()-1;
				}
			}	
		);
		
		menu.addItem((id)->
			new AjaxMenuItemPanelV5<ModelSection>(id) {
				@Override
				public void onClick(AjaxRequestTarget target) {
					if (deleted==null) 
						deleted = new ArrayList<IModel<ModelSection>>();
					deleted.add(getModel());
					getSectionsPanels().removeIf((panel) -> ((ContentClassSectionEditor)panel).getSectionModel().getObject().equals(getModelObject()));
					target.add(ContentClassStructureEditor.this);
					setUpdatedPart("delete section");
				}
				@Override
				public String getLabel() {	
					return new StringResourceModel("menu.delete", ContentClassStructureEditor.this, null).getString();
				}
				
				@Override
				public boolean isVisible() {
					return getModelObject().getStructure().isEmpty();
				}
			}	
		);
		
		return menu;
	}	

}

