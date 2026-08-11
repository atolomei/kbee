package kbee.web.datamanagement;

import com.novamens.wicket.util.BCElement;


public class CacheBC extends BCElement {
	
public CacheBC() {
	super("cache");
}

@Override
public void onClick() {
    setResponsePage( new ThumbnailServicePage());
}


}
