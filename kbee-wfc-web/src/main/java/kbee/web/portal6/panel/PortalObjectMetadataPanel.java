package kbee.web.portal6.panel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.portal6.model.PortalObject;
import com.novamens.wicket.model.ListModel;

import kbee.web.error.ErrorPanel;

public class PortalObjectMetadataPanel<T extends PortalObject> extends PortalPanel<T> {
		
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PortalObjectMetadataPanel.class.getName());
	
	public PortalObjectMetadataPanel(String id, IModel<T> model) {
		super(id, model);
	}
	
	
	public List<Entry<String, String>> getList() {
		Map<String, String> ma=getModel().getObject().getGeneralInfo();
		
		if (ma==null)
			ma =new HashMap<String, String>();
		
		List<Entry<String, String>> li=new ArrayList<Entry<String, String>> ();
		li.addAll(ma.entrySet());
		
		li.sort(new Comparator<Entry<String,String>>() {

			@Override
			public int compare(Entry<String, String> o1, Entry<String, String> o2) {
				if (o2.getKey()==null)
					return -1;
				if (o1.getKey()==null)
					return 1;
				return o1.getKey().compareToIgnoreCase(o2.getKey());
			}
			
		});
		return li;
		
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		try {
			
			ListModel<Entry<String, String>> lm = new ListModel<Entry<String, String>>(new Model<Panel>(this), "list");
			
			org.apache.wicket.markup.html.list.ListView<Entry<String, String>> lp = new ListView<Entry<String, String>>("info", lm) {
			
				private static final long serialVersionUID = 1L;
				@Override
				protected void populateItem(ListItem<Entry<String, String>> item) {
					try {
					
						Entry<String, String> object=item.getModelObject();
						
						Label key   = new Label("key", object.getKey());
						Label value = new Label("value", object.getValue());
						
						value.setEscapeModelStrings(false);
						key.setEscapeModelStrings(false);
						
						item.add(key);
						item.add(value);
						item.setOutputMarkupId(true);
					} 
					catch (Exception e) {
						item.addOrReplace(new Label("key", e.getClass().getName()));
						item.addOrReplace(new Label("value", e.getMessage()));
						logger.error(e);
					}	
				}
				
			};
			
			add(lp);
			
	} catch (Exception e) {
		logger.error(e);
		addOrReplace(new ErrorPanel("info", e));
	}
		
		
	}

		
	
	

	
}
