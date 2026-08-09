package kbee.web.eform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceFolder;
import com.novamens.content.base.ResourceNode;
import com.novamens.content.form.EFormChoice;
import com.novamens.content.form.EFormComponent;
import com.novamens.content.form.EFormContentData;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormMultipleChoice;
import com.novamens.content.form.EFormSection;
import com.novamens.content.form.EHtmlField;
import com.novamens.content.form.EHtmlStructField;
import com.novamens.content.form.EText;
import com.novamens.content.form.ETextField;
import com.novamens.content.model.DataSetMember;
import com.novamens.kbee.content.form.KbeeEMemberAutoCompleteField;
import com.novamens.kbee.content.form.KbeeEMemberAutoCompleteWithPreviewField;
import com.novamens.kbee.content.form.KbeeEMembersListField;
import com.novamens.kbee.content.form.KbeeENumberField;
import com.novamens.kbee.content.form.KbeeERelation;
import com.novamens.kbee.content.form.KbeeEResource;
import com.novamens.kbee.content.form.KbeeEResourceSystem;
import com.novamens.kbee.content.form.KbeeEResourceSystemV2;
import com.novamens.kbee.content.form.KbeeEResources;
import com.novamens.kbee.content.form.KbeeEBooleanField;
import com.novamens.kbee.content.form.KbeeECheckField;
import com.novamens.kbee.content.form.KbeeEDateField;
import com.novamens.kbee.content.form.KbeeEDateTimeField;
import com.novamens.kbee.content.form.KbeeEExternalResources;
import com.novamens.kbee.content.form.KbeeEFormRow;
import com.novamens.kbee.content.form.KbeeEHtmlField;
import com.novamens.kbee.content.form.KbeeEMemberComboField;
import com.novamens.kbee.content.form.KbeeEStringField;
import com.novamens.kbee.content.form.KbeeEStringListField;
import com.novamens.kbee.content.form.KbeeETextField;
import com.novamens.kbee.content.form.KbeeETitle;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.SiteService;

import kbee.web.resource.ResourceModel;

@SuppressWarnings("serial")
public class EViewerFactory implements EPanelFactory {
	
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(EViewerFactory.class.getName());

	private IModel<Site> sitemodel;
	private IModel<EFormData> model;
	private Map<String, Panel> panelsmap = new HashMap<String, Panel>();
	
	public EViewerFactory(IModel<EFormData> model) {
		this.model = model;
	}
	
	public EViewerFactory(IModel<EFormData> model, IModel<Site> sitemodel) {
		this.model = model;
		this.sitemodel = sitemodel;
	}
	
	public Panel getPanel(EFormComponent component) {
		return getPanel("panel", component);
	}
	
	public IModel<EFormData> getDataModel() {
		return model;
	}
	
	public IModel<Site> getSiteModel() {
		return sitemodel;
	}
	
	public Site getSite() {
		return getSiteModel()!=null ? getSiteModel().getObject() : null;
	}

	public Collection<Panel> getPanels() {
		return panelsmap.values();
	}
	
	public Panel getPanel(String id, EFormComponent component) {
		
		Panel panel = panelsmap.get(component.getName());
		
		if (panel!=null) {
			return panel;
		}
		else
		if (component instanceof EFormSection) {
			panel = new ESectionPanel(id, (EFormSection)component, getDataModel());
			((ESectionPanel)panel).setPanelFactory(this);
		}
		else
		if (component instanceof EFormMultipleChoice) {
			panel = new EMultipleChoicePanel(id, (EFormMultipleChoice)component, getDataModel());
			((EMultipleChoicePanel)panel).setPanelFactory(this);
		}
		else
		if (component instanceof EFormChoice) {
			panel = new EChoiceFieldPanel(id, (EFormChoice)component, getDataModel());
		}
		else
		if (component instanceof ETextField) {
			//panel = new ETextFieldPanel(id, (KbeeETextField)component, getDataModel());
			panel = new EValueViewer(id, (KbeeETextField)component, getDataModel());
		}
		else
		if (component instanceof EText) {
			panel = new ETextPanel(id, (EText)component, getDataModel());
		}
		else
		if (component instanceof EHtmlStructField) {
			panel = new EHtmlStructViewer(id, (KbeeEHtmlField)component, getDataModel());
		}
		else
		if (component instanceof EHtmlField) {
			panel = new EHtmlViewer(id, (KbeeEHtmlField)component, getDataModel());
		}
		else
		if (component instanceof KbeeEMemberComboField) {
			panel = new EValueViewer(id, (KbeeEMemberComboField)component, getDataModel());
		}
		else
		if (component instanceof KbeeEMemberAutoCompleteWithPreviewField) {
			panel = new EAutoCompleteWithPreviewPanel<DataSetMember>(id, (KbeeEMemberAutoCompleteField)component, getDataModel());
		}
		else
		if (component instanceof KbeeEMemberAutoCompleteField) {
			panel = new EValueViewer(id, (KbeeEMemberAutoCompleteField)component, getDataModel());
		}
		else
		if (component instanceof KbeeEMembersListField) {
			panel = new EListViewer<DataSetMember>(id, (KbeeEMembersListField)component, getDataModel());
		}
		else
		if (component instanceof KbeeEStringField) {
			panel = new EValueViewer(id, (KbeeEStringField)component, getDataModel());
		}
		else
		if (component instanceof KbeeEStringListField) {
			panel = new EStringListPanel(id, (KbeeEStringListField)component, getDataModel());
		}
		else
		if (component instanceof KbeeENumberField) {
			panel =  new ENumberPanel(id, (KbeeENumberField)component, getDataModel());
		}
		else
		if (component instanceof KbeeEDateField) {
			panel = new EValueViewer(id, (KbeeEDateField)component, getDataModel());
		}
		else
		if (component instanceof KbeeEDateTimeField) {
			panel = new EValueViewer(id, (KbeeEDateTimeField)component, getDataModel());
		}
		else
		if (component instanceof KbeeECheckField) {
			panel =  new ECheckPanel(id, (KbeeECheckField)component, getDataModel());
		}
		else
		if (component instanceof KbeeEBooleanField) {
			panel = new EValueViewer(id, (KbeeEBooleanField)component, getDataModel());
		}
		else
		if (component instanceof KbeeEFormRow) {
			panel =  new ERowViewer(id, (KbeeEFormRow)component, getDataModel());
			((ERowPanel)panel).setPanelFactory(this);
		}
		else
		if (component instanceof KbeeEResourceSystem) {
			panel =  new EResourceSystemPanel(id, (KbeeEResourceSystem)component, getDataModel()) {
				@Override
				public boolean isReadOnly() {
					return true;
				}
				@Override
				public boolean isShared() {
					return true;
				}
			};
		}
		else
		if (component instanceof KbeeEResourceSystemV2) {
			boolean hasFolders = false, hasIndex=false;
			List<?> resources = (List<?>)getDataModel().getObject().getData((KbeeEResourceSystemV2)component);
			if (resources!=null)
			for (Object resource : resources) {
				if (resource instanceof ResourceNode && ((ResourceNode)resource).isIndex()) {
					if (! (((ResourceNode)resource).getResource() instanceof ResourceFolder)) {
						hasIndex = true;
						break;
					}
				}
			}
			if (resources!=null)
			for (Object resource : resources) {
				if (resource instanceof ResourceNode && ((ResourceNode)resource).getResource() instanceof ResourceFolder) {
					hasFolders = true;
					break;
				}
			}
			if (hasFolders && !hasIndex) { // or size > alho ??
				panel =  new EResourceSystemPanelV2(id, (KbeeEResourceSystemV2)component, getDataModel()) {
					@Override
					public boolean isReadOnly() {
						return true;
					}
					@Override
					public boolean isShared() {
						return true;
					}
				};
			}
			else {
				KbeeEResources field = new KbeeEResources();
				field.setName(component.getName());
				field.setVisibleCondition(component.getVisibleCondition());
				field.setLabel(component.getLabel());
				panel =  new EResourcesViewer(id, field, getDataModel()) {
					protected void setResources() {
						List<?> resourcesdata = (List<?>)getData().getData(getField());
						setContent(((EFormContentData)getData()).getContent());
						if (resourcesdata!=null) {
							List<Resource> resources = new ArrayList<>();
							List<Resource> indexes = new ArrayList<>();
							for (Object resource : resourcesdata) {
								if (resource instanceof ResourceNode) {
									if (((ResourceNode)resource).isIndex()) {
										indexes.add(((ResourceNode)resource).getResource());
									}
									resources.add(((ResourceNode)resource).getResource());
								}
							}
							if (!indexes.isEmpty()) resources = indexes;
							for (Resource resource : resources) {
								add(resource);
							}
						}
					}
				};
			}
		}
		else
		if (component instanceof KbeeEResources) {
			panel =  new EResourcesViewer(id, (KbeeEResources)component, getDataModel());
		}
		else
		if (component instanceof KbeeEResource) {
			panel =  new EResourcePanel(id, (KbeeEResource)component, getDataModel()) {
				@Override
				public boolean isReadOnly() {
					return true;
				}
			};
		}
		else
		if (component instanceof KbeeEExternalResources) {
			panel =  new EExternalResourcesPanel(id, (KbeeEExternalResources)component, getDataModel()) {
				@Override
				public boolean isReadOnly() {
					return true;
				}
			};
		}
		else
		if (component instanceof KbeeERelation) {
			panel =  new ERelationPanel(id, (KbeeERelation)component, getDataModel()) {
				public boolean isViewer() {
					return true;
				}
				@Override
				protected String getUrl(Content content) {
					return getSite()!=null ?
						getSite().getService(SiteService.class).getUrl(content) :
						super.getUrl(content);	
				}
			};
		}
		else
		if (component instanceof KbeeETitle) {
			panel =  new ETitlePanel(id, (KbeeETitle)component);
		}
		if (panel == null) {
			logger.error("null eform panel ->  "+component.getClass().getName());
		}
		else {
			panelsmap.put(component.getName(), panel);
		}
		return panel;
	}
	
	protected Map<String, Panel> getMap() {
		return panelsmap;
	}
}
