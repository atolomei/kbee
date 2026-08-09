package kbee.web.model.service;

import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.communication.OrganizationalText;
import com.novamens.content.document.IDoc;
import com.novamens.content.document.TreeIDoc;
import com.novamens.kbee.content.communication.KbeeOrganizationalText;
import com.novamens.kbee.content.document.KbeeIDoc;
import com.novamens.kbee.content.document.KbeeTreeIDoc;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.wicket.model.ObjectModel;

public class KbeeObjectModelService implements ObjectModelService {

	@Override
	public IModel<?> getObjectModel(Content content) {
		
		if (content instanceof OrganizationalText) {
			return new ObjectModel<KbeeOrganizationalText>((KbeeOrganizationalText) content);
		}
		else if (content instanceof IDoc) {
			return new ObjectModel<KbeeIDoc>((KbeeIDoc) content);
		}
		else if (content instanceof TreeIDoc) {
			return new ObjectModel<KbeeTreeIDoc>((KbeeTreeIDoc) content);
		}
		else
			throw new KbeeRuntimeException ( content.getClass().getName() +  " not supported " );
	}
}