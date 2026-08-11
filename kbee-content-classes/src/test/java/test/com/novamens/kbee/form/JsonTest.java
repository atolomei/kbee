  package test.com.novamens.kbee.form;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novamens.content.form.AbstractUpdatedField;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;
import com.novamens.content.form.ResourceAdded;
import com.novamens.content.form.UpdatedField;
import com.novamens.content.form.ValueUpdated;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.KbeeModelElement;
import com.novamens.kbee.content.form.ScriptEvaluator;
import com.novamens.kbee.content.form.KbeeEChoice;
import com.novamens.kbee.content.form.KbeeEForm;
import com.novamens.kbee.content.form.KbeeEFormSection;
import com.novamens.kbee.content.form.KbeeEMemData;
import com.novamens.kbee.content.form.KbeeEMemForm;
import com.novamens.kbee.content.form.KbeeEMultipleChoice;
import com.novamens.kbee.content.form.KbeeEStringField;
import com.novamens.kbee.content.form.KbeeEStringAttributeModel;
import com.novamens.kbee.content.form.KbeeEText;
import com.novamens.kbee.content.form.KbeeETextField;
import com.novamens.kbee.content.resource.KBFileImpl;
import com.novamens.kbee.content.form.KbeeEConditionValidation;
import com.novamens.kbee.content.form.*;



public class JsonTest {
	
	
	//@Test
	public void test01() {
		try {
			
			OffsetDateTime today = OffsetDateTime.now();
			int d = today.getDayOfMonth();
			String sd = String.valueOf(d);
			//if (sd.length()==1) sd = "0" + sd;
			int y = today.getYear();
			String sy = String.valueOf(y);
			int m = today.getMonthValue();
			String sm = String.valueOf(m);
			//if (sm.length()==1) sm = "0" + sm;
			String value = sy+"-"+sm+"-"+sd;
			LocalDateTime time = LocalDateTime.parse(value+ " 00:00:00", DateTimeFormatter.ofPattern("yyyy-M-d HH:mm:ss"));
			ZoneId zone = ZoneId.systemDefault();
			ZoneOffset zoneOffSet = zone.getRules().getOffset(time);
			OffsetDateTime offsetDateTime = time.atOffset(zoneOffSet);
			
			
			KbeeEMultipleChoice multiplechoice = new KbeeEMultipleChoice();
			multiplechoice.setName("multiplechoice");
		//	multiplechoice.setText("Multiple Choice");
			
			KbeeEChoice choice1 = new KbeeEChoice();
			choice1.setName("choice1");
			choice1.setText("Choice 1");
			multiplechoice.addChoice(choice1);
			
			KbeeEChoice choice2 = new KbeeEChoice();
			choice2.setName("choice2");
			choice2.setText("Choice 2");
			multiplechoice.addChoice(choice2);
			
			KbeeEChoice choice3 = new KbeeEChoice();
			choice3.setName("choice3");
			choice3.setText("Choice 3");
			multiplechoice.addChoice(choice3);
			

			KbeeETextField text = new KbeeETextField();
			text.setName("date");
			text.setLabel("date");
			//text.setEnabledCondition("choice1");
			text.addValidation(new KbeeEConditionValidation("condition", "message"));
			
			
			ObjectMapper mapper = new ObjectMapper();
			mapper.setSerializationInclusion(Include.NON_NULL);
			mapper.registerSubtypes(KbeeEFormSection.class,
				KbeeEText.class,
				KbeeEMultipleChoice.class,
				KbeeEChoice.class,
				KbeeEConditionValidation.class,
				KbeeETextField.class);
			
			KbeeEFormSection root = new KbeeEFormSection();
			root.setName("form");
			
			root.add(multiplechoice);
			root.add(text);

			String result = mapper.writeValueAsString(root);
			
			KbeeEMemForm form = new KbeeEMemForm();
			form.setComponents(root.getComponents());

			ScriptEvaluator eval = new ScriptEvaluator();
			KbeeEMemData data = new KbeeEMemContentData(form, null);
			OffsetDateTime date = OffsetDateTime.now().plusDays(1);
			//OffsetDateTime today = OffsetDateTime.now();
			eval.setBinding(text, date);
			Object e = eval.evaluate("!date || date.isAfter(today) && today.plusYears(1).isAfter(date)", data);
			
			
			KbeeEFormSection s = mapper.readValue(result, KbeeEFormSection.class);
			
			// System.out.println(s);
		}
		catch (HttpClientErrorException e) {
			// System.out.println(e.getResponseBodyAsString());
			// System.out.println(e.getMessage());
		}
		catch (HttpServerErrorException e) {
			// System.out.println(e.getResponseBodyAsString());
			// System.out.println(e.getMessage());
		}
		catch (Exception e) {
			e.printStackTrace();
			// System.out.println(e.getMessage());
		}
	}
	
	//@Test
	public void test02() {
		try {
			
			
			
			KbeeEStringField string = new KbeeEStringField();
			string.setName("string");
			string.setLabel("string");
			string.setModel(new KbeeEStringAttributeModel());
			
			ObjectMapper mapper = new ObjectMapper();
			mapper.setSerializationInclusion(Include.NON_NULL);
			mapper.registerSubtypes(KbeeEFormSection.class,
				KbeeEStringField.class,
				KbeeEStringAttributeModel.class);
			
			KbeeEFormSection root = new KbeeEFormSection();
			root.setName("form");
			
			root.add(string);

			String result = mapper.writeValueAsString(root);
			
			KbeeEMemForm form = new KbeeEMemForm();
			form.setComponents(root.getComponents());
			
			
			KbeeEFormSection s = mapper.readValue(result, KbeeEFormSection.class);
			
			// System.out.println(s);
		}
		catch (HttpClientErrorException e) {
			// System.out.println(e.getResponseBodyAsString());
			// System.out.println(e.getMessage());
		}
		catch (HttpServerErrorException e) {
			// System.out.println(e.getResponseBodyAsString());
			// System.out.println(e.getMessage());
		}
		catch (Exception e) {
			e.printStackTrace();
			// System.out.println(e.getMessage());
		}
	}
	
	//@Test
	public void test03() {
		try {
			
			
			String json ="{\"type\":\"section\",\"name\":\"form\",\"components\":["+
					"{\"type\":\"member combo\",\"model\": { \"type\":\"classifier\", \"classifierId\":\"507\"}, \"name\":\"property\"},"+
					"{\"type\":\"string\",\"model\": { \"type\":\"string attribute\", \"attributeId\":\"700\"}, \"name\":\"lastname\", \"validations\": [{ \"type\":\"condition\", \"condition\":\"eee\", \"message\":\"message\"}]}"+
				"]}";
			
			
			ObjectMapper mapper = new ObjectMapper();
			mapper.setSerializationInclusion(Include.NON_NULL);
			mapper.registerSubtypes(KbeeEFormSection.class,
				KbeeEStringField.class,
				KbeeEMemberComboField.class,
				KbeeEConditionValidation.class,
				KbeeEClassifierFieldModel.class,
				KbeeEStringAttributeModel.class);
			
			KbeeEFormSection s = mapper.readValue(json, KbeeEFormSection.class);
			
			// System.out.println(s);
		}
		catch (HttpClientErrorException e) {
			// System.out.println(e.getResponseBodyAsString());
			// System.out.println(e.getMessage());
		}
		catch (HttpServerErrorException e) {
			// System.out.println(e.getResponseBodyAsString());
			// System.out.println(e.getMessage());
		}
		catch (Exception e) {
			e.printStackTrace();
			// System.out.println(e.getMessage());
		}
	}
	
	@Test
	public void test04() {
		try {
			KBFileImpl file = new KBFileImpl();
			file.setName("file.pdf");
			file.setId((long)1524);
			ResourceAdded  value = new ResourceAdded(null, "field", file);
			
			ValueUpdated value2 = new ValueUpdated(null, "date", OffsetDateTime.now(), null);
			
			ObjectMapper mapper = new ObjectMapper();
			mapper.setSerializationInclusion(Include.NON_NULL);
			mapper.registerSubtypes(ValueUpdated.class,ResourceAdded.class);
			
			List<UpdatedField> fields = new ArrayList<UpdatedField>();
			fields.add(value);
			fields.add(value2);
			
			String result = mapper.writeValueAsString(fields);
		
			List<AbstractUpdatedField> s = mapper.readValue(result, new TypeReference<List<AbstractUpdatedField>>(){});
			// System.out.println(s);
		}
		catch (Exception e) {
			e.printStackTrace();
			// System.out.println(e.getMessage());
		}
	}



	

}
