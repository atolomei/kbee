package kbee.web.dashboard;

import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.wicket.model.ListModel;

public class DashboardSimpleBottomPanel extends KBPanel {

	private static final long serialVersionUID = 1L;

	private List<Panel> l_panels;
	private List<Panel> r_panels;
	
	private WebMarkupContainer l_c;
	private WebMarkupContainer r_c;
	

	public DashboardSimpleBottomPanel(String id, List<Panel> l_l) {
		super(id);
		l_panels = l_l;
	}
	
	
	public DashboardSimpleBottomPanel(String id, List<Panel> l_l, List<Panel> r_l) {
		super(id);
		l_panels = l_l;
		r_panels = r_l;
	}
	
	
	
	
	@Override
	public void onInitialize() {
			super.onInitialize();

			
			 l_c = new WebMarkupContainer("left-container");
			 r_c = new WebMarkupContainer("right-container");
			 
			 add(l_c);
			 add(r_c);
			
			
			 if (getLeftPanels()!=null && getLeftPanels().size()>0) {
				ListModel<Panel> l_lm = new ListModel<Panel>(new Model<Panel>(this), "leftPanels");
				ListView<Panel> l_items = new ListView<Panel>("items", l_lm) {
					private static final long serialVersionUID = 1L;
	
					@Override
					protected void populateItem(ListItem<Panel> item) {
						item.add(item.getModelObject());
					}
				};
				l_c.add(l_items);
			}
			 else {
				 l_c.setVisible(false);
			 }
			 
			
			 if (getRightPanels()!=null && getRightPanels().size()>0) {
				ListModel<Panel> r_lm = new ListModel<Panel>(new Model<Panel>(this), "rightPanels");
				ListView<Panel> r_items = new ListView<Panel>("items", r_lm) {
					private static final long serialVersionUID = 1L;
					@Override
					protected void populateItem(ListItem<Panel> item) {
						item.add(item.getModelObject());
					}
				};
				r_c.add(r_items);
			 }
			 else {
				 r_c.setVisible(false);
			 }
	}
	
	
	
	public List<Panel> getLeftPanels() {
		return l_panels;
	}

	public void setLeftPanels(List<Panel> p) {
		this.l_panels = p;
	}
	

	
	public List<Panel> getRightPanels() {
		return r_panels;
	}

	public void setRightPanels(List<Panel> p) {
		this.r_panels = p;
	}

}
