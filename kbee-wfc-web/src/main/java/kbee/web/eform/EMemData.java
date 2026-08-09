package kbee.web.eform;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.SignedData;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;
import com.novamens.content.form.EListField;
import com.novamens.wicket.model.ObjectModel;

import kbee.util.logging.Logger;

// datos del eform detachables
public abstract class EMemData implements EFormData, Serializable, IDetachable {
	private static final long serialVersionUID = 1L;
			
	private static Logger logger = Logger.getLogger(EMemData.class.getName());
	
	private IModel<EForm> model;
	private Map<String, Object> data = new HashMap<String, Object>();
	private List<IModel<SignedData>> signaturesmodel;
	
	public EMemData(IModel<EForm> model) {
		this.model = model;
	}
	
	@Override
	public Object getData(String name) {
		return data.get(name);
	}
	
	@Override
	public Object getObject(String name) {
		Object object = data.get(name);
		if (object instanceof IModel) {
			object = ((IModel<?>)object).getObject();
		}
		return object;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> List<T> getValues(EListField<T> field) {
		ParameterizedType t = (ParameterizedType)field.getClass().getGenericSuperclass();
		Type type = t.getActualTypeArguments()[0];
		Class<T> c = (Class<T>)type;
		List<T> values = new ArrayList<T>();
		
		Object value =  data.get(field.getName());
		
		if (value instanceof List<?>) {
			for (Object object : (List<?>)value) {
				if (object instanceof IModel<?>) {
					object = ((IModel<?>)object).getObject();
				}
				if (c.isInstance(object)) {
					values.add((T)object);
				}
			}
		}
		else if (value instanceof IModel<?>) {
			Object object = ((IModel<?>)value).getObject();
			if (c.isInstance(value)) {
				values.add((T)object);
			}
		}
		else if (c.isInstance(value)) {
			values.add((T)value);
		}
		return values;
	}
	
	@Override
	public Object getData(EFormField<?> field) {
		
		Object value =  data.get(field.getName());
		
		if (value==null)
			return value;
				
		if (value instanceof IModel) {
			value = ((IModel<?>)value).getObject();
		}
		
		else if (value instanceof List<?>) {
			List<Object> values = new ArrayList<java.lang.Object>();
			for (Object object : (List<?>)value) {
				if (object instanceof IModel) {
					object = ((IModel<?>)object).getObject();
				}
				values.add(object);
			}
			value = values;
		}
		else if (value instanceof Map<?,?>) {
			logger.error("MAP");
		}
		
		
		return value;
	}
	
	public void setData(String name, Object value) {
		if (value==null || (value instanceof List<?> && ((List<?>)value).isEmpty())) 
			data.remove(name);
		else 	
			data.put(name, value);
	}
	
	public void setData(EFormField<?> field, Object value) {
		setData(field.getName(), value);
	}
	
	@Override
	public EForm getForm() {
		return model.getObject();
	}
	
	public IModel<EForm> getModel() {
		return model;
	}
	
	@Override
	public boolean isEmpty() {
		return data.isEmpty();
	}
	
	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}

	@Override
	public boolean isSigned() {
		return getSignatures()!=null && !getSignatures().isEmpty();
	}
	
	@Override
	public List<SignedData> getSignatures() {
		List<SignedData> signatures = new ArrayList<SignedData>();
		if (signaturesmodel!=null) {
			for (IModel<SignedData> model : signaturesmodel) {
				signatures.add(model.getObject());
			}
		}
		return signatures;
	}
	
	@Override
	public void setSignatures(List<SignedData> signatures) {
		signaturesmodel = new ArrayList<IModel<SignedData>>();
		if (signatures!=null)
		for (SignedData signature : signatures) {
			signaturesmodel.add(new ObjectModel<SignedData>(signature));
		}
	}
	
	@Override
	public void setSignature(SignedData signature) {
		if (signaturesmodel==null)
			signaturesmodel = new ArrayList<IModel<SignedData>>();
		signaturesmodel.add(new ObjectModel<SignedData>(signature));
	}
	
	@Override
	public void clearSignatures() {
		this.signaturesmodel.clear();
	}
	
	@Override
	public void detach() {
		
		if (model!=null)
			model.detach();
		
		if (signaturesmodel!=null) {
			for (IModel<SignedData> m:signaturesmodel) {
				m.detach();
			}
		}
		
		for (Object value : data.values()) {
			
			
			if (value instanceof IDetachable) {
				((IDetachable)value).detach();
			}
			
			if (value instanceof List<?>) {
				for (Object object : ((List<?>)value)) {
					if (object instanceof IDetachable) {
						((IDetachable)object).detach();
					}
				}
			}
			else if (value instanceof Map<?,?>) {
				for (Entry<?,?> entry :  (((Map<?,?>) value)).entrySet()) {
					if (entry.getValue() instanceof IDetachable) {
						((IDetachable) entry.getValue()).detach();
					}
				}
			}
		}
	}
	
	public abstract EFormData clone();
	
	@Override
	public String getObjectTitle() {
		return null;
	}	
}