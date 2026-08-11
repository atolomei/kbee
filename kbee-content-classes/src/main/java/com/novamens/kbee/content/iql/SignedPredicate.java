package com.novamens.kbee.content.iql;

import com.novamens.content.base.Content;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormData;
import com.novamens.indexer.iql.CalculatedPredicate;
import com.novamens.solr.indexer.iql.SolrAbstractPredicate;

public class SignedPredicate extends SolrAbstractPredicate implements CalculatedPredicate {
	
	public SignedPredicate() {
		setName("signed");
		setValueTypeDescription("");
	}

	@Override
	public String getHelpValueTypeDescription() {
		return 	"signed";
	}
	
	public String getCode(String argument) {
		return null;
	}
	
	public boolean evaluate(Object object, Object argument) {
		if (!(object instanceof Content) || argument==null) return false;
		
		Content content = (Content)object;
		String formname = argument.toString();

		EForm eform = null;
		for (EForm f : content.getContentTemplate().getForms()) {
			if (formname.equals(f.getName())) {
				eform = f;
				break;
			}
		}
		
		if (eform==null) return false;
		
		EFormData data = content.getKbeeData(eform);
		
		boolean signed = data.isSigned();

		return signed;
	}
	
	public boolean isCanonical() {
		return true;
	}
}
