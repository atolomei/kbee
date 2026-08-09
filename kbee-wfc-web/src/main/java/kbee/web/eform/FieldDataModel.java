package kbee.web.eform;

import java.io.Serializable;

import org.apache.wicket.model.IModel;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.util.Assert;

import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;
import com.novamens.security.Identifiable;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.model.ProxyModel;
import com.novamens.wicket.model.SerializableModel;

public class FieldDataModel<T extends EFormField<?>, S> implements IModel<S> {
	private static final long serialVersionUID = 1L;
	
	IModel<T> model;
	IModel<EFormData> datamodel;
	
	public FieldDataModel(IModel<T> model, IModel<EFormData> datamodel) {
		this.model = model;
		this.datamodel = datamodel;
	}
	
	@SuppressWarnings("unchecked")
	public S getObject() {
		return (S)datamodel.getObject().getData(model.getObject());
	}
	
	public void setObject(S value) {
		datamodel.getObject().setData(model.getObject(), getModel(value));
	}
	
	@Override
	public void detach() {
		model.detach();
		datamodel.detach();
	}
	
	protected IModel<S> getModel(S value) {
		if (value instanceof HibernateProxy) {
			return new ProxyModel<S>(value);
		}
		if (value instanceof Serializable) {
			return new SerializableModel<S>(value);
		}
		if (value instanceof Identifiable) {
			return new ObjectModel<S>(value);
		}
		if (value == null) {
			return null;
		}
		Assert.isTrue(true, "no model");
		return null;
	}
}