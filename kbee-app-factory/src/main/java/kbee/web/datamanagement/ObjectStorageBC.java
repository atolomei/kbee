package kbee.web.datamanagement;

import com.novamens.wicket.util.BCElement;

import kbee.web.objectstorage.ObjectStoragePage;

public class ObjectStorageBC extends BCElement {
	
	
public ObjectStorageBC() {
	super("objectstorage");
}

@Override
public void onClick() {
    setResponsePage( new ObjectStoragePage());
}



}
