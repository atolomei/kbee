package com.novamens.kbee.kbfs;

import org.xmlpull.v1.XmlPullParserException;

import com.novamens.kbfs.FileServerException;

import io.minio.messages.Item;

public class KFSItem  {

	Item item;
	
	public KFSItem () throws FileServerException {
	    this(null, false);
	  }

	  /**
	   * Constructs a new Item object with given object name and IsDir flag.
	   */
	  public KFSItem(String objectName, boolean isDir) throws FileServerException  {
		  try {
			  item=new Item(objectName, isDir);
		  } catch (XmlPullParserException e) {
			  throw new FileServerException(e);
		  }
	  }
}
