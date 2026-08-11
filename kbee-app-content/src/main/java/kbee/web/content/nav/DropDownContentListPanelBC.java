package kbee.web.content.nav;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.EnumLabel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.base.Content;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.Identifiable;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.wicket.util.DummyBlockPanel;


public class DropDownContentListPanelBC<T extends Identifiable> extends KBPanel {
			
	
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DropDownContentListPanelBC.class.getName());
	
	private IModel<T> model;
	private IModel<String> label;
	private List<IModel<Content>> list;
	
	WebMarkupContainer panel;
	WebMarkupContainer panel_container;
	AjaxLink<T> link;
	
	
	public DropDownContentListPanelBC(String id, IModel<T> model) {
		super(id);
		this.model=model;
		this.label=new Model<String>(model.getObject().getDisplayName());
		setOutputMarkupId(true);
	}
	
	
	public IModel<String> getLabel() {
		return this.label;
	}
	
	public WebMarkupContainer generatePanel(String id) {
		return new DummyBlockPanel(id);
	}
	
	@Override
	public void onDetach() {
			super.onDetach();
			
		if (model!=null) 
			model.detach();
		
		if (list!=null) 
			list.forEach(item->item.detach());
		
	}

	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		panel_container = new WebMarkupContainer("panel-container");
		panel_container.setOutputMarkupId(true);
		addOrReplace(panel_container);

		panel=new InvisiblePanel("menu");
		panel_container.addOrReplace(panel);
		
		
		link = new AjaxLink<T>("title-link", getModel()) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				
				if (panel==null || panel instanceof InvisiblePanel) {
					panel=generatePanel("menu");
					panel_container.addOrReplace(panel);
					target.add( DropDownContentListPanelBC.this );
				}
				else {
					panel_container.setVisible(!panel_container.isVisible());
					target.add( DropDownContentListPanelBC.this );
				}
			}
		};
	
		add(link);
		
		Label title = new Label("title", getLabel());
		title.setEscapeModelStrings(false);
		link.add(title);
	}
	

	public IModel<T> getModel() {
		return model;
	}

	
}
