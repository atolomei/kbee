package kbee.web.workflow.task;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;

import com.novamens.wicket.markup.html.panel.KBPanel;

public class PageTaskToolbar<T> extends KBPanel {
	private static final long serialVersionUID = 1L;
	
	private IModel<T> model;
										
	private List<WebMarkupContainer> l_list = new ArrayList<WebMarkupContainer>();
	private List<WebMarkupContainer> r_list = new ArrayList<WebMarkupContainer>();
	
	public PageTaskToolbar(String id, IModel<T> model) {
		super(id, model);
		this.model=model;
 	}
	
	public PageTaskToolbar(String id, IModel<T> model, List<WebMarkupContainer> l_list, List<WebMarkupContainer> r_list) {
		super(id, model);
		this.model=model;
		this.l_list=l_list;
		this.r_list=r_list;
 	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (model!=null)
			model.detach();
		
		if (l_list!=null)
			l_list.forEach(item -> item.detach());
		
		if (r_list!=null)
			r_list.forEach(item -> item.detach());
		
	}
	
	public void setLeftPanels( List<WebMarkupContainer> panels) {
		l_list=panels;
		if (this.isInitialized())
			addPanels();
	}
	
	public void addLeftPanel(WebMarkupContainer pa) {
		l_list.add(pa);

		if (this.isInitialized())
			addPanels();
	}

	private void addPanels() {
        
		addOrReplace(new ListView<WebMarkupContainer>("left-item-element", this.l_list) {
            private static final long serialVersionUID = 1L;

            protected void populateItem(ListItem<WebMarkupContainer> item) {
                item.setOutputMarkupId(true);
                
                WebMarkupContainer element = item.getModelObject();
				item.add(element);
                
                //item.setVisible(item.getModelObject().isVisible());
            }
        });
		
															
		addOrReplace(new ListView<WebMarkupContainer>("right-item-element", this.r_list) {
            private static final long serialVersionUID = 1L;

            protected void populateItem(ListItem<WebMarkupContainer> item) {
                item.setOutputMarkupId(true);
                
                WebMarkupContainer element = item.getModelObject();
				item.add(element);
                
                //item.setVisible(item.getModelObject().isVisible());
            }
        });


		
	}
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		addPanels();
								
		
		
		
		
		
		
		
		
		
		
	}
	
}
