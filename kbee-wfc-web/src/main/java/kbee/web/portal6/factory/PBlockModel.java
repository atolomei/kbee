package kbee.web.portal6.factory;

import org.apache.wicket.model.IModel;

import com.novamens.portal6.model.Block;

public interface PBlockModel {

	public void setBlockModel (IModel<Block> model);
	public IModel<Block> getBlockModel (IModel<Block> model);
	
}
