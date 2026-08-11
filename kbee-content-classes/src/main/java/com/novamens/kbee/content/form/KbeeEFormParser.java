package com.novamens.kbee.content.form;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novamens.content.form.EFormComponent;

import kbee.util.logging.Logger;

public class KbeeEFormParser extends EFormParser {
	
	static private Logger logger = Logger.getLogger(KbeeEFormParser.class.getName());

	static private ObjectMapper mapper = new ObjectMapper();
	static  {
		//smapper.registerModule(new JavaTimeModule());
		//mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.registerSubtypes(KbeeEFormSection.class,
				KbeeEText.class,
				KbeeEChoice.class,
				KbeeEMultipleChoice.class,
				KbeeEMemberAutoCompleteField.class,
				KbeeEMemberAutoCompleteWithPreviewField.class,
				KbeeEFormRow.class,
				KbeeEResource.class,
				KbeeEResources.class,
				KbeeEResourceSystem.class,
				KbeeEExternalResources.class,
				KbeeEResourceSystemV2.class,
				KbeeEResourceSystemV3.class,
				KbeeEResourceDistribution.class,
				KbeeEResourceFieldModel.class,
				KbeeEResourceSystemFieldModel.class,
				KbeeEResourceDistributionFieldModel.class,
				KbeeERelationResourceFieldModel.class,
				KbeeEConditionValidation.class,
				KbeeEMultipicityValidation.class,
				KbeeENumericValidation.class,
				KbeeERelation.class,
				KbeeERelationFieldModel.class,
				KbeeEDateModel.class,
				KbeeEDateField.class,
				KbeeEDateTimeField.class,
				KbeeEStringField.class,
				KbeeEStringModel.class,
				KbeeEHtmlField.class,
				KbeeEHtmlStructField.class,
				KbeeEStringListField.class,
				KbeeENumberField.class,
				KbeeECheckField.class,
				KbeeEMembersListField.class,
				KbeeEMemberComboField.class,
				KbeeEClassifierFieldModel.class,
				KbeeEDateAttributeModel.class,
				KbeeEStringAttributeModel.class,
				KbeeENumberAttributeModel.class,
				KbeeEBooleanAttributeModel.class,
				KbeeEBooleanModel.class,
				KbeeEBooleanField.class,
				KbeeEContentTitleModel.class,
				KbeeEStringModel.class,
				KbeeEHtmlModel.class,
				KbeeEText.class,
				KbeeETitle.class,
				KbeeETextField.class,
				KbeeETableField.class,
				KbeeETableFieldModel.class);
	}
	
	
	public String getJson(List<EFormComponent> components) {
		try {
			KbeeEFormSection root = new KbeeEFormSection();
			root.setName("form");
			for (EFormComponent component : components) {
				root.add(component);
			};
			String json = getMapper().writeValueAsString(root);
			return json;
		}
		catch (JsonProcessingException e) {
			logger.error(e);
			return null;
		}
	}
	
	public List<EFormComponent> getComponents(String json) {
		try {
			KbeeEFormSection root = getMapper().readValue(json, KbeeEFormSection.class);
			List<EFormComponent> components = root.getComponents();
			//check(root, components, null);
			return components;
		}	
		catch (IOException e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}
	
//	private void check(EFormComponent parent, List<EFormComponent> components, Set<String> names) {
//		if (names==null) names = new HashSet<String>(); 
//		for (EFormComponent component : components) {
//			((EFormAbstractComponent)component).setParent(parent);
//			if (component.getName()==null || "".equals(component.getName().trim()))  {
//				throw new KbeeRuntimeException("Field name is required");
//			}
//			else {
//				if (names.contains(component.getName())) {
//					throw new KbeeRuntimeException("Field name "+ component.getName() + " is duplicated");
//				}
//				else {
//					names.add(component.getName());
//				}
//			}
//			if (component instanceof EFormField<?>)  {
//				if (((EFormField<?>)component).getModel()!=null) {
//					String message =  ((EFormField<?>)component).getModel().getErrorMessage(null);
//					if (message!=null) {
//						throw new KbeeRuntimeException("Field "+ component.getName() + ". " + message);
//					}
//				}
//			}
//			if (component instanceof EFormContainer) {
//				check(component, ((EFormContainer)component).getComponents(), names);
//			}
//		}
// 	}
	
	private ObjectMapper getMapper() {

		/**if (mapper==null) {
			mapp er = new Obje tMapper();
			mapper.setSerializationInclusion(Include.NON_NULL);
			mapper.registerSubtypes(KbeeEFormSection.class,
				KbeeEText.class,
				KbeeEChoice.class,
				KbeeEMultipleChoice.class,
				KbeeEMemberAutoCompleteField.class,
				KbeeEMemberAutoCompleteWithPreviewField.class,
				KbeeEFormRow.class,
				KbeeEResource.class,
				KbeeEResources.class,
				KbeeEResourceSystem.class,
				KbeeEResourceFieldModel.class,
				KbeeEResourceSystemFieldModel.class,
				KbeeERelationResourceFieldModel.class,
				KbeeEConditionValidation.class,
				KbeeEMultipicityValidation.class,
				KbeeENumericValidation.class,
				KbeeERelation.class,
				KbeeERelationFieldModel.class,
				KbeeEDateField.class,
				KbeeEStringField.class,
				KbeeEHtmlField.class,
				KbeeEStringListField.class,
				KbeeENumberField.class,
				KbeeECheckField.class,
				KbeeEMembersListField.class,
				KbeeEMemberComboField.class,
				KbeeEClassifierFieldModel.class,
				KbeeEDateAttributeModel.class,
				KbeeEStringAttributeModel.class,
				KbeeENumberAttributeModel.class,
				KbeeEBooleanAttributeModel.class,
				KbeeEBooleanModel.class,
				KbeeEBooleanField.class,
				KbeeEContentTitleModel.class,
				KbeeEStringModel.class,
				KbeeEText.class,
				KbeeETitle.class,
				KbeeETextField.class,
				KbeeETableField.class,
				KbeeETableFieldModel.class);
		}  
		**/ 
		return mapper;
	}
}