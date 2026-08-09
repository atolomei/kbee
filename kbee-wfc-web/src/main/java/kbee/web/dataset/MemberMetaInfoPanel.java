package kbee.web.dataset;

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

import com.novamens.content.model.Classification;
import com.novamens.content.model.DataSetMember;
import com.novamens.dom.DomainType;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.query.MemberQuery;
import com.novamens.kbee.content.repository.MemberRepository;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KeyValue;

import kbee.util.logging.Logger;
import kbee.web.command.panel.CommandAttributePanelV5;

@SuppressWarnings("serial")
public class MemberMetaInfoPanel<T extends DataSetMember> extends ModelPanel<T> {
	private static final long serialVersionUID = 1L;
	
	protected final boolean root  = ServiceLocator.getService(SecurityService.class).isRoot();
	protected final boolean admin 	= root || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	
	private static Logger logger = Logger.getLogger(MemberMetaInfoPanel.class.getName());
 	
	public MemberMetaInfoPanel(String id, IModel<T> model) {
		super(id, model);
		setOutputMarkupId(true);
	}
	
	public void onInitialize() {
		super.onInitialize();
		adContentItems();
	}
	
	protected boolean isFreeVersion() {
		return getDomain().getDomainType()==DomainType.EXPRESS;
	}
	
	protected T getContent() {
		return  getModel().getObject();
	}
	
	private void adContentItems() {
		
		
		DataSetMember member = getModelObject();
		
		add(new Label("dataSet", member.getDataSet().getDisplayName()));
		
		List<KeyValue<String>> map = new ArrayList<KeyValue<String>>();
		//list.add( new KeyValue<String>(getLabel("content-template").getObject(), getContent().getContentTemplate().getDisplayName()));
		//list.add( new KeyValue<String>(getLabel("version").getObject(),  String.valueOf( getContent().getVersion())));
		map.add( new KeyValue<String>(getLabelString("label"), member.getDisplayName()));
		map.add( new KeyValue<String>(getLabelString("id"), String.valueOf(member.getId())));
		map.add( new KeyValue<String>(getLabelString("modified"), member.getCreationOffsetDateTimeColloquial()));
		map.add( new KeyValue<String>(getLabelString("modifiedby"), member.getLastModifiedUser().getDisplayName()));
		
		if (root || admin) {
			map.add( new KeyValue<String>(getLabelString("references"), String.valueOf(getReferences(member))));
		}
		
		Map<String, List<String>> classification = getClassificationAsMapString();
		for (Entry<String, List<String>> entry : classification.entrySet()) {
			StringBuilder str = new StringBuilder();
			int i = 0;
			for (String value : entry.getValue()) {
				str.append(value.length()>200? (value.substring(0,200)+" ..."):value);
				if (++i<entry.getValue().size()) str.append(", ");
			}
			map.add( new KeyValue<String>(	entry.getKey(),	str.toString()));
		}
		
//		if (member.getParent()!=null) {
//			map.add( new KeyValue<String>(getLabelString("parent"), member.getParent().getDisplayName()));
//		}
		
		if (member.getDataSet().isHierachical()) {
			int i = 0;
			List<DataSetMember> childs = getChilds(member);
			String value = "";
			for (DataSetMember child : childs) {
				value  += child.getDisplayName(); 
				if (++i<childs.size()) value +=", ";
			}
			map.add( new KeyValue<String>(getLabelString("childs"),	value));
		}
		
		map.sort(new Comparator<KeyValue<String>>() {
			@Override
			public int compare(KeyValue<String> o1, KeyValue<String> o2) {
				return o1.getKey().toString().compareToIgnoreCase(o2.getKey().toString());
			}
		});
		
		List<Panel> panels = new ArrayList<Panel>();
		
		for (KeyValue<String> kv : map) 
			panels.add(new CommandAttributePanelV5("item", new Model<String>(kv.getKey().toString()), new Model<String>(kv.getValue())));
		
		 add(new ListView<Panel>("member", panels) {
            protected void populateItem(ListItem<Panel> item) {
                item.setOutputMarkupId(true);
                item.add(item.getModelObject());
                item.setVisible(item.getModelObject().isVisible());
            }
        });
	}
	
	public Map<String, List<String>> getClassificationAsMapString() {
		Map<String, List<String>> map = new HashMap<>();

		for (Classification classification : getModelObject().getClassification()) {
			if (classification!=null && classification.getClassifier()!=null) {
				String classifierlabel = classification.getClassifier().getName();
				List<String> values = map.get(classifierlabel);
				if (values == null) {
					values = new ArrayList<String>();
					map.put(classifierlabel, values);
				}
				values.add(classification.getStrValue());
			}
		}
		
		Map<String, List<String>> attributes = getModelObject().getAttributesAsMap();
		for (String name : attributes.keySet()) {
			map.put(name, attributes.get(name));
		}
		
		return map;
	}
	
	protected List<DataSetMember> getChilds(DataSetMember member) {
		return ((MemberRepository)getRepository(DataSetMember.class)).findChilds(member); 
	}
	
	protected Long getReferences(DataSetMember member) {
		long references = 0;
		try {
			Query query = new MemberQuery(getQueryIndex(), member);
			references = Long.valueOf(query.execute().size());
		} 
		catch (Exception e) {
			logger.error(e);
			references = (long) -1;
		}
		return references;
	}
	
	protected Index getQueryIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}
}
