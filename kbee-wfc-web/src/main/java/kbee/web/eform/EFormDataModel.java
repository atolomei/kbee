package kbee.web.eform;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceNode;
import com.novamens.content.entity.Person;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormContentData;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;
import com.novamens.content.form.EFormMemberData;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.security.Role;
import com.novamens.security.Identifiable;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.model.SerializableModel;

import kbee.web.resource.ResourceNodeModel;

public class EFormDataModel implements IModel<EFormData> {
	private static final long serialVersionUID = 1L;
	
	private EFormData data;
	
 	public EFormDataModel(EFormData data) {
		if (data instanceof EFormContentData) {
			setObject(new EMemContentData(getModel(data.getForm()), getModel(((EFormContentData) data).getContent())));
			getObject().setSignatures(data.getSignatures());
		}
		else {
			if (data instanceof EFormMemberData) {
				setObject(new EMemMemberData(getModel(data.getForm()), getModel(((EFormMemberData) data).getMember())));
			}
			else {
				// error ??
				//setObject(new EMemData(getModel(data.getForm())));
			}	
		}	
		for (EFormField<?> field : data.getForm().getFields()) {
			getObject().setData(field, getModel(data.getData(field)));
		}
	}
	
	public EFormData getObject() {
		return data;
	}
	
	public void setObject(EFormData data) {
		this.data = data; 
	}
	
	public void detach() {
		if (data instanceof IDetachable) {
			((IDetachable)data).detach();
		}
	}
	
	@SuppressWarnings("unchecked")
	private Object getModel(Object data) {
		if (data instanceof List<?>) {
			List<Object> values = new ArrayList<Object>();
			for (Object value : (List<Object>)data) {
				values.add(getModel(value));
			}
			return values;
		}
		else 
		if (data instanceof Resource) {
			return data instanceof ResourceNode ?
				new ResourceNodeModel((ResourceNode)data) :
				new ObjectModel<Resource>((Resource)data);
		}
		else
		if (data instanceof DataSetMember) {
			return new ObjectModel<DataSetMember>((DataSetMember)data);
		}
		else
		if (data instanceof Content) {
			return new ObjectModel<Content>((Content)data);
		}
		else
		if (data instanceof Role) {
			return new ObjectModel<Role>((Role)data);
		}
		
		if (data instanceof Person) {
			return new ObjectModel<Person>((Person)data);
		}
		
		else { 
			return data;
		}
	}
	
	private IModel<Content> getModel(Content content) {
		return new ObjectModel<Content>(content);
	}
	
	private IModel<DataSetMember> getModel(DataSetMember member) {
		return new ObjectModel<DataSetMember>(member);
	}
	
	private IModel<EForm> getModel(EForm form) {
		if (form instanceof Identifiable) {
			return new ObjectModel<EForm>(form);
		}
		if (form instanceof Serializable) {
			return new SerializableModel<EForm>(form);
		}
		return null;
	}
}