package kbee.web.eform;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.content.form.EFieldModel;
import com.novamens.content.form.EFormChoice;
import com.novamens.content.form.EFormComponent;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormDataSource;
import com.novamens.content.form.EFormField;
import com.novamens.content.form.EFormMultipleChoice;
import com.novamens.content.form.EFormSection;
import com.novamens.content.form.EHtmlField;
import com.novamens.content.form.EHtmlStructField;
import com.novamens.content.form.EText;
import com.novamens.content.form.ETextField;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.security.Role;
import com.novamens.kbee.content.form.KbeeEMemberAutoCompleteField;
import com.novamens.kbee.content.form.KbeeEMemberAutoCompleteTreeField;
import com.novamens.kbee.content.form.KbeeEMemberAutoCompleteWithPreviewField;
import com.novamens.kbee.content.form.KbeeEMembersListField;
import com.novamens.kbee.content.form.KbeeENumberField;
import com.novamens.kbee.content.form.KbeeERelation;
import com.novamens.kbee.content.form.KbeeEResource;
import com.novamens.kbee.content.form.KbeeEResourceDistribution;
import com.novamens.kbee.content.form.KbeeEResourceSystem;
import com.novamens.kbee.content.form.KbeeEResourceSystemV2;
import com.novamens.kbee.content.form.KbeeEResourceSystemV3;
import com.novamens.kbee.content.form.KbeeEResources;
import com.novamens.kbee.content.form.KbeeERoleComboField;
import com.novamens.kbee.content.form.KbeeEBooleanField;
import com.novamens.kbee.content.form.KbeeECheckField;
import com.novamens.kbee.content.form.KbeeEClassifierSource;
import com.novamens.kbee.content.form.KbeeEDateField;
import com.novamens.kbee.content.form.KbeeEDateTimeField;
import com.novamens.kbee.content.form.KbeeEExternalResources;
import com.novamens.kbee.content.form.KbeeEFormRow;
import com.novamens.kbee.content.form.KbeeEHtmlField;
import com.novamens.kbee.content.form.KbeeEHtmlStructField;
import com.novamens.kbee.content.form.KbeeEMemberComboField;
import com.novamens.kbee.content.form.KbeeEStringField;
import com.novamens.kbee.content.form.KbeeEStringListField;
import com.novamens.kbee.content.form.KbeeETableField;
import com.novamens.kbee.content.form.KbeeETextField;
import com.novamens.kbee.content.form.KbeeETitle;

import kbee.web.error.ErrorPanel;

public class EEditorFactory implements EPanelFactory {
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(EEditorFactory.class.getName());

	private IModel<EFormData> model;
	private Map<String, Panel> panelsmap = new HashMap<String, Panel>();
	
	public EEditorFactory(IModel<EFormData> model) {
		this.model = model;
	}
	
	public Panel getPanel(EFormComponent component) {
		return getPanel("panel", component);
	}
	
	public IModel<EFormData> getDataModel() {
		return model;
	}
	
	public Collection<Panel> getPanels() {
		return panelsmap.values();
	}
	
	public Panel getPanel(String id, EFormComponent component) {
		
		try {
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
			panel = new ETextFieldPanel(id, (KbeeETextField)component, getDataModel());
		}
		else
		if (component instanceof EHtmlStructField) {
			panel =  new EHtmlStructFieldPanel(id, (KbeeEHtmlStructField)component, getDataModel()); 
		}
		else
		if (component instanceof EHtmlField) {
			panel = "Tiny".equals(((KbeeEHtmlField)component).getEditor()) || "FullTiny".equals(((KbeeEHtmlField)component).getEditor()) 
				? new EHtmlFieldPanel(id, (KbeeEHtmlField)component, getDataModel()) 
				: new EFroalaFieldPanel(id, (KbeeEHtmlField)component, getDataModel());	
		}
		else
		if (component instanceof EText) {
			panel = new ETextPanel(id, (EText)component, getDataModel());
		}
		else
		if (component instanceof KbeeEMemberComboField) {
			panel = new EComboPanel<DataSetMember>(id, (KbeeEMemberComboField)component, getDataModel());
		}
		else
		if (component instanceof KbeeERoleComboField) {
			panel = new EComboPanel<Role>(id, (KbeeERoleComboField)component, getDataModel());
		}
		else
		if (component instanceof KbeeEMemberAutoCompleteWithPreviewField) {
			panel = new EAutoCompleteWithPreviewPanel<DataSetMember>(id, (KbeeEMemberAutoCompleteField)component, getDataModel());
		}
		else
		if (component instanceof KbeeEMemberAutoCompleteField) {
			panel = new EAutoCompletePanel<DataSetMember>(id, (KbeeEMemberAutoCompleteField)component, getDataModel());
		}
		else
		if (component instanceof KbeeEMembersListField) {
			if (isHierarchical(component)) {
				panel = new EListTreePanel<DataSetMember>(id, (KbeeEMembersListField)component, getDataModel());
			}
			else {
				panel = new EListPanel<DataSetMember>(id, (KbeeEMembersListField)component, getDataModel());
			}
		}
		else
		if (component instanceof KbeeEStringListField) {
			panel = new EStringListPanel(id, (KbeeEStringListField)component, getDataModel());
		}
		else
		if (component instanceof KbeeEStringField) {
			panel =  new EStringPanel(id, (KbeeEStringField)component, getDataModel());
		}
		else
		if (component instanceof KbeeENumberField) {
			panel =  new ENumberPanel(id, (KbeeENumberField)component, getDataModel());
		}
		else
		if (component instanceof KbeeEDateField) {
			panel =  new EDatePanel(id, (KbeeEDateField)component, getDataModel());
		}
		else
		if (component instanceof KbeeEDateTimeField) {
			panel =  new EDateTimePanel(id, (KbeeEDateTimeField)component, getDataModel());
		}
		else
		if (component instanceof KbeeECheckField) {
			panel =  new ECheckPanel(id, (KbeeECheckField)component, getDataModel());
		}
		else
		if (component instanceof KbeeEBooleanField) {
			panel =  new EBooleanPanel(id, (KbeeEBooleanField)component, getDataModel());
		}
		else
		if (component instanceof KbeeEFormRow) {
			panel =  new ERowPanel(id, (KbeeEFormRow)component, getDataModel());
			((ERowPanel)panel).setPanelFactory(this);
		}
		else
		if (component instanceof KbeeEResources) {
			panel =  new EResourcesPanel(id, (KbeeEResources)component, getDataModel());
		}
		else
		if (component instanceof KbeeEExternalResources) {
			panel =  new EExternalResourcesPanel(id, (KbeeEExternalResources)component, getDataModel());
		}
		else
		if (component instanceof KbeeEResource) {
			panel =  new EResourcePanel(id, (KbeeEResource)component, getDataModel());
		}
		else
		if (component instanceof KbeeEResourceSystem) {
			panel =  new EResourceSystemPanel(id, (KbeeEResourceSystem)component, getDataModel());
		}
		else
		if (component instanceof KbeeEResourceDistribution) {
			panel =  new EResourceDistributionPanel(id, (KbeeEResourceDistribution)component, getDataModel());
		}
		else
		if (component instanceof KbeeEResourceSystemV2) {
			panel =  new EResourceSystemPanelV2(id, (KbeeEResourceSystemV2)component, getDataModel());
		}
		else
		if (component instanceof KbeeEResourceSystemV3) {
			panel =  new EResourceSystemPanelV3(id, (KbeeEResourceSystemV3)component, getDataModel());
		}
		else
		if (component instanceof KbeeERelation) {
			panel =  new ERelationPanel(id, (KbeeERelation)component, getDataModel());
		}
		else
		if (component instanceof KbeeETableField) {
			panel =  new ETablePanel(id, (KbeeETableField)component, getDataModel());
		}
		else
		if (component instanceof KbeeETitle) {
			panel =  new ETitlePanel(id, (KbeeETitle)component);
		}
		else
		if (component instanceof KbeeEMemberAutoCompleteTreeField) {
			panel = new EAutoCompleteTreePanel<DataSetMember>(id, (KbeeEMemberAutoCompleteTreeField)component, getDataModel());
		}
		if (panel == null) {
			logger.error("null eform panel ->  "+component.getClass().getName());
		}
		else {
			panelsmap.put(component.getName(), panel);
		}
		return panel;
		} catch (Exception e) {
			logger.error(e);
			return new ErrorPanel(id, e);
		}
	}
	
	private boolean isHierarchical(EFormComponent component) {
		if (!(component instanceof EFormField<?>)) return false;
		EFieldModel<?> model =  ((EFormField<?>)component).getModel();
		EFormDataSource<?> dataSource = model.getDataSource(null);
		if (!(dataSource instanceof KbeeEClassifierSource)) return false;
		Classifier classifier = ((KbeeEClassifierSource)dataSource).getRelation().getClassifier();
		if (classifier == null) return false;
		return classifier.getDataSet().isHierachical();
	}
}
