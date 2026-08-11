package kbee.replica;

import java.util.ArrayList;
import java.util.List;

import com.novamens.content.base.ResourceTag;
import com.novamens.content.form.EComponentType;
import com.novamens.content.form.EFieldModel;
import com.novamens.content.form.EFormAccessLevel;
import com.novamens.content.form.EFormComponent;
import com.novamens.content.form.EFormContainer;
import com.novamens.content.form.EFormField;
import com.novamens.content.form.EModelType;
import com.novamens.content.form.EText;
import com.novamens.content.model.AccessStrategy;
import com.novamens.content.service.ObjectFactoryService;
import com.novamens.kbee.content.base.KbeeResourceTag;
import com.novamens.kbee.content.form.EFormAbstractComponent;
import com.novamens.kbee.content.form.EFormAbstractField;
import com.novamens.kbee.content.form.KbeeEBooleanAttributeModel;
import com.novamens.kbee.content.form.KbeeEBooleanField;
import com.novamens.kbee.content.form.KbeeEBooleanModel;
import com.novamens.kbee.content.form.KbeeEClassifierFieldModel;
import com.novamens.kbee.content.form.KbeeEDateAttributeModel;
import com.novamens.kbee.content.form.KbeeEDateField;
import com.novamens.kbee.content.form.KbeeEDateModel;
import com.novamens.kbee.content.form.KbeeEForm;
import com.novamens.kbee.content.form.KbeeEFormRow;
import com.novamens.kbee.content.form.KbeeEFormSection;
import com.novamens.kbee.content.form.KbeeEHtmlField;
import com.novamens.kbee.content.form.KbeeEMemberAutoCompleteField;
import com.novamens.kbee.content.form.KbeeEMemberComboField;
import com.novamens.kbee.content.form.KbeeEMembersListField;
import com.novamens.kbee.content.form.KbeeENumberAttributeModel;
import com.novamens.kbee.content.form.KbeeENumberField;
import com.novamens.kbee.content.form.KbeeEResource;
import com.novamens.kbee.content.form.KbeeEResourceFieldModel;
import com.novamens.kbee.content.form.KbeeEResourceSystem;
import com.novamens.kbee.content.form.KbeeEResourceSystemFieldModel;
import com.novamens.kbee.content.form.KbeeEResources;
import com.novamens.kbee.content.form.KbeeEStringAttributeModel;
import com.novamens.kbee.content.form.KbeeEStringField;
import com.novamens.kbee.content.form.KbeeEStringModel;
import com.novamens.kbee.content.form.KbeeEText;
import com.novamens.kbee.content.form.KbeeETextField;
import com.novamens.kbee.content.form.KbeeETitle;
import com.novamens.service.ServiceLocator;

import kbee.api.model.ApiProxy;
import kbee.api.model.IComponent;
import kbee.api.model.IForm;
import kbee.api.model.IResourceTag;

public class EFormReplicaHandler extends AbstractReplicaHandler<IForm, KbeeEForm> {

	public EFormReplicaHandler(Replica replica, IForm iform) {
		super(replica, iform);
	}
	
	@Override
	protected void replicateIn(KbeeEForm local) throws ReplicaException {
		IForm remote = getObject();
		local.setName(remote.getName());
		local.setDisplayName(remote.getDisplayName());
		local.setFormAccessLevel(EFormAccessLevel.valueOf(remote.getDisplayLevel()));
		local.setComponents(getComponents(remote.getComponents()));
		local.setFileContainer(remote.isFileContainer());
		local.setViewer(remote.getViewer());
	}

	@Override
	protected KbeeEForm createLocal() {
		return (KbeeEForm)ServiceLocator.getService(ObjectFactoryService.class).createEForm(null);
	}
	
	private List<EFormComponent> getComponents(List<IComponent> icomponents) throws ReplicaException {
		List<EFormComponent> components = new ArrayList<EFormComponent>();
		for (IComponent icomponent : icomponents) {
			EFormAbstractComponent component = (EFormAbstractComponent)getComponent(icomponent);
			if (component!=null) {
				component.setName(icomponent.getName());
				component.setLabel(icomponent.getLabel());
				component.setCssClass(icomponent.getCss());
				component.setVisibleCondition(icomponent.getVisible());
				component.setEnabledCondition(icomponent.getEnabled());
				if (component instanceof EFormField) {
					((EFormAbstractField<?>)component).setSublabel(icomponent.getSublabel());
					((EFormAbstractField<?>)component).setModel(getModel(icomponent));
					((EFormAbstractField<?>)component).setCalculation(icomponent.getCalculation());
				}
				if (component instanceof EText) {
					((KbeeEText)component).setText(icomponent.getText());
				}
				if (component instanceof EFormContainer && icomponent.getChilds()!=null) {
					((EFormContainer)component).setComponents(getComponents(icomponent.getChilds()));
				}
				components.add(component);
			}
		}
		return components;
	}
	
	private EFormComponent getComponent(IComponent icomponent) {
		if (EComponentType.ROW.getLabel().equals(icomponent.getType())) {
			return new KbeeEFormRow();
		}
		if (EComponentType.SECTION.getLabel().equals(icomponent.getType())) {
			return new KbeeEFormSection();
		}
		if (EComponentType.COMBO.getLabel().equals(icomponent.getType())) {
			return new KbeeEMemberComboField();
		}
		if ("AutoComplete".equals(icomponent.getType())) {
			return new KbeeEMemberAutoCompleteField();
		}
		if (EComponentType.STRING.getLabel().equals(icomponent.getType())) {
			return new KbeeEStringField();
		}
		if (EComponentType.DATE.getLabel().equals(icomponent.getType())) {
			return new KbeeEDateField();
		}
		if (EComponentType.RESOURCE_SYSTEM.getLabel().equals(icomponent.getType())) {
			return new KbeeEResourceSystem();
		}
		if (EComponentType.RESOURCE.getLabel().equals(icomponent.getType())) {
			return new KbeeEResource();
		}
		if (EComponentType.RESOURCES.getLabel().equals(icomponent.getType())) {
			return new KbeeEResources();
		}
		if (EComponentType.HTML.getLabel().equals(icomponent.getType())) {
			return new KbeeEHtmlField();
		}
		if (EComponentType.TEXT.getLabel().equals(icomponent.getType())) {
			return new KbeeETextField();
		}
		if (EComponentType.LIST.getLabel().equals(icomponent.getType())) {
			return new KbeeEMembersListField();
		}
		if (EComponentType.STATIC_TEXT.getLabel().equals(icomponent.getType())) {
			return new KbeeEText();
		}
		if (EComponentType.TITLE.getLabel().equals(icomponent.getType())) {
			return new KbeeETitle();
		}
		if (EComponentType.NUMBER.getLabel().equals(icomponent.getType())) {
			return new KbeeENumberField();
		}
		if (EComponentType.BOOLEAN.getLabel().equals(icomponent.getType())) {
			return new KbeeEBooleanField();
		}
		return null;
	}
	
	private EFieldModel<?> getModel(IComponent icomponent) throws ReplicaException {
		if (EModelType.CLASSIFIER.getLabel().equals(icomponent.getModel())) {
			KbeeEClassifierFieldModel model = new KbeeEClassifierFieldModel();
			model.setClassifier(getClassifier(icomponent.getClassifier()));
			model.setAccessStrategy(AccessStrategy.All);
			return model;
		}
		if ("Attribute".equals(icomponent.getModel()) && EComponentType.STRING.getLabel().equals(icomponent.getType())) {
			KbeeEStringAttributeModel model = new KbeeEStringAttributeModel();
			model.setAttribute(getAttribute(icomponent.getAttribute()));
			return model;
		}
		if ("Attribute".equals(icomponent.getModel()) && "Date".equals(icomponent.getType())) {
			KbeeEDateAttributeModel model = new KbeeEDateAttributeModel();
			model.setAttribute(getAttribute(icomponent.getAttribute()));
			return model;
		}
		if ("Attribute".equals(icomponent.getModel()) && "Boolean".equals(icomponent.getType())) {
			KbeeEBooleanAttributeModel model = new KbeeEBooleanAttributeModel();
			model.setAttribute(getAttribute(icomponent.getAttribute()));
			return model;
		}
		if ("Attribute".equals(icomponent.getModel()) && "Html".equals(icomponent.getType())) {
			KbeeEStringAttributeModel model = new KbeeEStringAttributeModel();
			model.setAttribute(getAttribute(icomponent.getAttribute()));
			return model;
		}
		if ("Attribute".equals(icomponent.getModel()) && "Text".equals(icomponent.getType())) {
			KbeeEStringAttributeModel model = new KbeeEStringAttributeModel();
			model.setAttribute(getAttribute(icomponent.getAttribute()));
			return model;
		}
		if ("Attribute".equals(icomponent.getModel()) && "Number".equals(icomponent.getType())) {
			KbeeENumberAttributeModel model = new KbeeENumberAttributeModel();
			model.setAttribute(getAttribute(icomponent.getAttribute()));
			return model;
		}
		if ("Resource".equals(icomponent.getModel())) {
			KbeeEResourceFieldModel model = new KbeeEResourceFieldModel();
			model.setTag(getResourceTag(icomponent.getResourceTag()));
			return model;
		}
		if (EModelType.RESOURCE_SYSTEM.getLabel().equals(icomponent.getModel())) {
			KbeeEResourceSystemFieldModel model = new KbeeEResourceSystemFieldModel();
			model.setTag(getResourceTag(icomponent.getResourceTag()));
			return model;
		}
		if (EModelType.FORM_ATTRIBUTE.getLabel().equals(icomponent.getModel()) && "String".equals(icomponent.getType())) {
			KbeeEStringModel model = new KbeeEStringModel();
			return model;
		}
		if (EModelType.FORM_ATTRIBUTE.getLabel().equals(icomponent.getModel()) && "Date".equals(icomponent.getType())) {
			KbeeEDateModel model = new KbeeEDateModel();
			return model;
		}
		if (EModelType.FORM_ATTRIBUTE.getLabel().equals(icomponent.getModel()) && EComponentType.BOOLEAN.getLabel().equals(icomponent.getType())) {
			KbeeEBooleanModel model = new KbeeEBooleanModel();
			return model;
		}
		if (EModelType.FORM_ATTRIBUTE.getLabel().equals(icomponent.getModel()) && "Html".equals(icomponent.getType())) {
			KbeeEStringModel model = new KbeeEStringModel();
			return model;
		}
		if (EModelType.FORM_ATTRIBUTE.getLabel().equals(icomponent.getModel()) && "Text".equals(icomponent.getType())) {
			KbeeEStringModel model = new KbeeEStringModel();
			return model;
		}
		if (EModelType.FORM_ATTRIBUTE.getLabel().equals(icomponent.getModel()) && "Number".equals(icomponent.getType())) {
			KbeeEStringModel model = new KbeeEStringModel();
			return model;
		}
		return null;
	}
	
	private ResourceTag getResourceTag(ApiProxy proxy) {
		IResourceTag remote = getReplicaApi().getResourceTag(proxy.getId());
		ResourceTag local = getLocal(KbeeResourceTag.class, remote);
		return local;
	}
}