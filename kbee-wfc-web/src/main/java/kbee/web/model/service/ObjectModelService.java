package kbee.web.model.service;

import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.service.BusinessSystemService;

public interface ObjectModelService extends BusinessSystemService {

	public IModel<?> getObjectModel(Content content);
}
