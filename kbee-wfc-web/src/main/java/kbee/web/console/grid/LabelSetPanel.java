package kbee.web.console.grid;


import java.util.ArrayList;
import java.util.List;


import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.LabelMember;
import com.novamens.content.model.LabelSet;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.wicket.markup.html.event.RemoveLabelEvent;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.event.wicket.LabelEvent;
import kbee.web.label.ClassificableLabelMenuItemFactory;


@SuppressWarnings("serial")
public class LabelSetPanel<T extends Classificable> extends KBPanel {
			
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(LabelSetPanel.class.getName());
	
	private List<IModel<LabelMember>> all_labels;
	private List<IModel<LabelMember>> labels;
	private IModel<T> model;
	
	private boolean is_remove_enabled = false;
	private boolean is_label_list = true;
	private boolean is_dropdownmenu = false;

	/**
	 * 
	 * 
	 * @param id
	 * @param model
	 * @param is_remove_enabled
	 */
	
	public LabelSetPanel(String id, IModel<T> model, boolean is_remove_enabled) {
		this(id, model, is_remove_enabled, true, true);
	}
	
	public LabelSetPanel(String id, IModel<T> model, 
			boolean is_remove_enabled, 
			boolean is_label_list,
			boolean is_dropdownmenu) {
		
		super(id, model);
		this.model=model; 
		this.is_remove_enabled=is_remove_enabled;
		this.is_label_list= is_label_list;
		this.is_dropdownmenu =is_dropdownmenu;
		
		setOutputMarkupId(true);
	}
	
	public IModel<T> getModel() {
		return model;
	}
	
	public void addListeners() {
		super.addListeners();
	}
	
	@Override  
	public void onInitialize() {
		super.onInitialize();

		WebMarkupContainer atc = new WebMarkupContainer("add-tag-container");
		add(atc);
		
		atc.setVisible(this.is_dropdownmenu);
			
			if (this.is_dropdownmenu) {
				ContextMenuPanel<T> menu = new ContextMenuPanel<T>( getModel());
				atc.add(menu);
				
				for (IModel<LabelMember> label: getAllLabels())  {
					menu.addItem(new ClassificableLabelMenuItemFactory<T>(label, getModel()) {
						@Override
						public void onUpdate(AjaxRequestTarget target) {
							 fire(new LabelEvent(target));
						}
				});
			
			}
		}
		else {
			atc.add( new InvisiblePanel("menu"));
		}
		addLabelList();
	}
	
	
	@Override
	public void onDetach() {
		super.onDetach();
		
		if (labels!=null) {
			for (IModel<LabelMember> label: getLabels()) 
				label.detach();
		}
		
		
		if (all_labels!=null) {
			for (IModel<LabelMember> label: all_labels) 
				label.detach();
		}
		
		
		if (this.model!=null)
			this.model.detach();
		
		
		
	}
						
	protected List<IModel<LabelMember>> getLabels() {
		
		if (this.labels==null) {
			this.labels=new ArrayList<IModel<LabelMember>>();
			try {
				for (com.novamens.content.model.Classification  ca: model.getObject().getClassification()) {
					if (ca!=null && ca.getDataSetMember()!=null && ca.getDataSetMember().getDataSet() instanceof LabelSet) 
						this.labels.add(new ObjectModel<LabelMember>( (LabelMember) ca.getDataSetMember()));
				}
			} catch (Exception e) {
				logger.error(e);
			}
		}
		return this.labels;
	}

	
	
	/**
	 * 
	 * @return
	 */
	protected List<IModel<LabelMember>> getAllLabels() {
		
		if (this.all_labels!=null)
			return this.all_labels;
		
		
		DataSet ds = null;
		List<Classifier> list = getModel().getObject().getClassifiers();
		for (Classifier c:list) {
				 if (c.getDataSet() !=null && c.getDataSet() instanceof LabelSet) {
					 ds=c.getDataSet(); 
					 break;
			}
		}

		if (ds==null)
			return new ArrayList<IModel<LabelMember>>();
		
		List<DataSetMember> dm_l = getContentDao().getMembers(ds, "strvalue", ObjectState.ENABLED, 200);

		this.all_labels = new ArrayList<IModel<LabelMember>>();
		
		for (DataSetMember dm: dm_l) 
			this.all_labels.add( new ObjectModel<LabelMember> ((LabelMember) dm));

		return this.all_labels;
		
	}

	
	
	private void addLabelList() {
		
		
		if (!this.isLabelList()) {
			add(new InvisiblePanel("label-list"));
			return;
		}
			
		WebMarkupContainer list = new WebMarkupContainer("label-list"); 
		add(list);
		list.setOutputMarkupId(true);
		
		List<IModel<LabelMember>> lab_list = getLabels();
		
		list.add(new ListView<IModel<LabelMember>>("label-element", 
				lab_list ) {
					private static final long serialVersionUID = 1L;
						protected void populateItem(ListItem<IModel<LabelMember>> item) {
							try {
								WebMarkupContainer icon = new WebMarkupContainer("icon");
								icon.add(new AttributeModifier("class", "far fa-tag " + item.getModel().getObject().getObject().getLabelColor().getKey()));
								item.add(icon);
								Label label = new Label("label", item.getModel().getObject().getObject().getStrValue());
								item.add(label);
								AjaxLink<Void> re = new AjaxLink<Void>("remove") {
									@Override
									public void onClick(AjaxRequestTarget target) {
										fireScanAll(new RemoveLabelEvent<T>(target,  LabelSetPanel.this.getModel(), item.getModel().getObject() ));
									}
								};
								re.setVisible(isRemoveEnabled());
								item.add(re);
								
								
							} catch (Exception e) {
								logger.error(e);
								WebMarkupContainer icon = new WebMarkupContainer("icon");
								icon.setVisible(false);
								item.add(icon);
								item.add((new Label("label", e.getClass().getName())));
							}
				}
		 });
	 }
	
	
	 
	public boolean isRemoveEnabled() {
		return this.is_remove_enabled;
	}
						
	public boolean isLabelList() {
		return this.is_label_list;
	}
	
	
	protected ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
}
