package kbee.web.util;

import java.io.Serializable;

import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;

public abstract class Property<T extends Content> implements Serializable, IDetachable {
	private static final long serialVersionUID = 1L;

	public abstract IModel<String> getLabel();
	
	public abstract IModel<String> getValue(IModel<T> content);
	
	public String getCss() {
		return null;
	}
	
	public boolean isLink() {
		return false;
	}
	
	public void detach() {
		getLabel().detach();
	}
}
