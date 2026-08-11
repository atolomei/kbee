package test.com.novamens.kbee.content;


import org.junit.jupiter.api.Test;

import com.novamens.kbee.content.document.KbeeIDoc;
import com.novamens.kbee.text.KbeeTextTemplate;

public class JXPathTest  {
	
	
	@Test
	public void test01() {
		try {
			
			KbeeTextTemplate template = new KbeeTextTemplate("hola ${id?upper_case} property ${property} property.id ${property.id}");
			String text = template.process(new KbeeIDoc());
			// System.out.println(text);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
}
