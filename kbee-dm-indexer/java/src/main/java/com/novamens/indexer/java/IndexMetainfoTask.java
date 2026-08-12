package com.novamens.indexer.java;

import com.novamens.indexer.service.Index;
import com.novamens.util.KbeeRuntimeException;

public class IndexMetainfoTask extends ObjectIndexTaskServiceRequest {
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(IndexMetainfoTask.class.getName());
	
	boolean onlymetainfo = true;
	
	public IndexMetainfoTask(Object object, Index index) {
		super(object, index);
		setName("IndexMetainfoTask");
	}
	
	@Override
	public void execute() {
		try {
			((KbeeJavaIndex)this.getIndex()).index(getObject(), onlymetainfo);
		}
		catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new KbeeRuntimeException(e);
		}
	}
	
	public boolean isTransactional() {
		return false;
	}
}
