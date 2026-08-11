package kbee.replica;

import java.util.ArrayList;
import java.util.List;

import com.novamens.content.base.ContentClass;
import com.novamens.content.base.ResourceTag;
import com.novamens.content.form.EForm;
import com.novamens.content.model.ModelElementTemplate;
import com.novamens.content.model.Multiplicity;
import com.novamens.content.service.ObjectFactoryService;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.base.KbeeResourceTag;
import com.novamens.kbee.content.form.KbeeEForm;
import com.novamens.kbee.content.model.KbeeAttributeTemplate;
import com.novamens.kbee.content.model.KbeeClassifierTemplate;
import com.novamens.kbee.content.model.KbeeContentTemplate;
import com.novamens.kbee.content.model.KbeeExtractionMacro;
import com.novamens.kbee.content.workflow.KbeeContentProcedure;
import com.novamens.service.ServiceLocator;

import kbee.api.model.ApiProcedure;
import kbee.api.model.ApiProxy;
import kbee.api.model.IForm;
import kbee.api.model.IModelElement;
import kbee.api.model.IResourceTag;
import kbee.api.model.ITemplate;

public class TemplateReplicaHandler extends AbstractReplicaHandler<ITemplate, KbeeContentTemplate> {

	public TemplateReplicaHandler(Replica replica, ITemplate itemplate) {
		super(replica, itemplate);
	}
	
	public TemplateReplicaHandler(Replica replica, ITemplate itemplate, boolean forceUpdate) {
		super(replica, itemplate);
		setForceUpdate(forceUpdate);
	}
	
	@Override
	protected void replicateIn(KbeeContentTemplate local) throws ReplicaException {
		ITemplate remote = getObject();
		local.setName(remote.getDisplayName());
		local.setState(ObjectState.valueOf(remote.getState()));
		local.setContentClass(getContentClass(remote.getBaseClass()));
		local.setTitleEditable(remote.isTitleEditable());
		local.setOnlyRootEdit(remote.isOnlyRoot());
		if (remote.getTitleRule()!=null) {
			local.setTitleRule(new KbeeExtractionMacro(remote.getTitleRule()));
		}
		local.setConsoleSubtitleRule(remote.getConsoleSubline());
		local.setPortalsSubtitleRule(remote.getPortalSubline());
		local.setTitleEditable(remote.isTitleEditable());
		
		if (remote.getStructure()!=null) {
			List<ModelElementTemplate> elements = new ArrayList<>();
			for (IModelElement element : remote.getStructure()) {
				ModelElementTemplate template;
				if (CLASSIFIER_REL.equals(element.getAttribute().getRel())) {
					template = new KbeeClassifierTemplate(getClassifier(element.getAttribute()));
					((KbeeClassifierTemplate)template).setMultiplicity(Multiplicity.valueOf((element.getMutiplicity())));
				}
				else {
					template = new KbeeAttributeTemplate(getAttribute(element.getAttribute()));
					((KbeeAttributeTemplate)template).setMultiplicity(Multiplicity.valueOf((element.getMutiplicity())));
				}
				elements.add(template);
			}
			local.setStructure(elements);
			getContentDao().flush();
			update(local);
		}
		
		if (remote.getResourceTags()!=null) {
			List<ResourceTag> tags = new ArrayList<>();
			for (ApiProxy proxy : remote.getResourceTags()) {
				IResourceTag itag = getReplicaApi().getResourceTag(proxy.getId());
				ResourceTag tag = replicated(KbeeResourceTag.class, itag);
				if (tag!=null) {
					tags.add(tag);
				}
			}
			local.setResourceTags(tags);
		}
		
		if (remote.getForms()!=null) {
			List<EForm> forms = new ArrayList<>();
			for (ApiProxy proxy : remote.getForms()) {
				IForm iform = getReplicaApi().getForm(proxy.getId());
				KbeeEForm eform = replicated(KbeeEForm.class, iform);
				forms.add(eform);
			}
			local.setForms(forms);
		}
		
		if (remote.getProcedures()!=null) {
			for (ApiProxy procedureproxy : remote.getProcedures()) {
				ApiProcedure iprocedure = getReplicaApi().getProcedure(procedureproxy.getId());
				KbeeContentProcedure procedure = replicated(KbeeContentProcedure.class, iprocedure);
				local.addProcedure(procedure);
			}
		}	
	}

	@Override
	protected KbeeContentTemplate createLocal() {
		return (KbeeContentTemplate)ServiceLocator.getService(ObjectFactoryService.class).createTemplate(false);
	}
	
	private ContentClass getContentClass(String name) {
		return getContentDao().findContentClassByName(name);
	}
}