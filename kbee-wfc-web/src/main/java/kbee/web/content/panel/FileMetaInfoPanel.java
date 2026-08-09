package kbee.web.content.panel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.base.Content;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.dom.DomainType;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KeyValue;

import kbee.web.command.panel.CommandAttributePanelV5;


@SuppressWarnings("serial")
public class FileMetaInfoPanel<T extends Content> extends ModelPanel<T> {
	private static final long serialVersionUID = 1L;

//	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(FileMetaInfoPanel.class.getName());
 	
	protected final boolean root		     = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
	final boolean role_admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	
	public FileMetaInfoPanel(IModel<T> model) {
		this("file-metadata-info", model);
	}
	
	public FileMetaInfoPanel(String id, IModel<T> model) {
		super(id, model);
		setOutputMarkupId(true);
	}
	
	public void onInitialize() {
		super.onInitialize();
		adContentItems();
	}

	protected boolean isAuthorizedPrivateNotes() {											
		return ServiceLocator.getService(ContentSystemSecurityService.class).isPrivateEnabled(getContent());
	}
	
	protected boolean isFreeVersion() {
		return getDomain().getDomainType()==DomainType.EXPRESS;
	}
	
	protected T getContent() {
		return  getModel().getObject();
	}
	
	private void adContentItems() {
		
		List<KeyValue<String>> list = new ArrayList<KeyValue<String>>();
		
		list.add( new KeyValue<String>(getLabel("content-template").getObject(), getContent().getContentTemplate().getDisplayName()));
		list.add( new KeyValue<String>(getLabel("version").getObject(),  String.valueOf( getContent().getVersion())));
		list.add( new KeyValue<String>(getLabel("id-info").getObject(),  getContent().getIdInfo()));
		
		if (root || role_admin) {
			Map<String, List<String>> map = getContent().getClassificationAsMapString();
			for (Entry<String, List<String>> entry: map.entrySet()) {
				StringBuilder str = new StringBuilder();
				int i = 0;
				for (String value : entry.getValue()) {
					str.append(value.length()>200? (value.substring(0,200)+" ..."):value);
					if (++i<entry.getValue().size()) str.append(", ");
				}
				list.add( new KeyValue<String>(	entry.getKey(),	str.toString()));
			}
		}
		
		list.sort(new Comparator<KeyValue<String>>() {
			@Override
			public int compare(KeyValue<String> o1, KeyValue<String> o2) {
				return o1.getKey().toString().compareToIgnoreCase(o2.getKey().toString());
			}
		});
		
		List<Panel> panels = new ArrayList<Panel>();
		
		for ( KeyValue<String> kv:list) 
			panels.add(new CommandAttributePanelV5("item", new Model<String>(kv.getKey().toString()), new Model<String>(kv.getValue())));
		
		 add(new ListView<Panel>("content", panels) {
            protected void populateItem(ListItem<Panel> item) {
                item.setOutputMarkupId(true);
                item.add(item.getModelObject());
                item.setVisible(item.getModelObject().isVisible());
            }
        });
	}
}
