package kbee.web.eform;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.novamens.content.base.Content;
import com.novamens.content.form.EFormComponent;
import com.novamens.content.form.EFormContainer;
import com.novamens.content.form.EFormContentData;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;

public class EFormChecker {
	
	private EFormData data;
	
	public EFormChecker(EFormData data) {
		this.data = data;
	}
	
	public void setData(EFormData data) {
		this.data = data;
	}
	
	public EFormData getData() {
		return this.data;
	}
	
	public String check() {
		return check(getData().getForm().getComponents(), null);
	}
	
	private String check(List<EFormComponent> components, Set<String> names) {
		if (names==null) names = new HashSet<String>(); 
		for (EFormComponent component : components) {
			if (component.getName()==null || "".equals(component.getName().trim()))  {
				String message = "Field name is required";
				return message;
			}
			else {
				if (names.contains(component.getName())) {
					String message = "Field name "+ component.getName() + " is duplicated";
					return message;
				}
				else {
					names.add(component.getName());
				}
			}
			if (component instanceof EFormField<?>)  {
				if (((EFormField<?>)component).getModel()!=null) {
					if (getData() instanceof EFormContentData) {
						Content content = ((EFormContentData)getData()).getContent(); 
						String message =  ((EFormField<?>)component).getModel().getErrorMessage(content);
						if (message!=null) {
							return message;
						}
					}
				}
			}
			if (component instanceof EFormContainer) {
				String message = check(((EFormContainer)component).getComponents(), names);
				if (message!=null) {
					return message;
				}
			}
		}
		return null;
 	}
}
