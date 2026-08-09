package kbee.web.dashboard;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;

import com.novamens.wicket.markup.html.panel.KBPanel;

public abstract class LinkLabelPanel<T> extends KBPanel {
			
	private static final long serialVersionUID = 1L;
	Link<T> link;
	IModel<T> model;
	IModel<String> label;
	
	public LinkLabelPanel(String id, IModel<T> model, IModel<String> label) {
		super(id, model);
		this.model=model;
		this.label=label;
	}
	
	public void setLabel(IModel<String> label) {
		this.label=label;
	}
	
	public void onInitialize() {
		super.onInitialize();
		
		link= new Link<T>("link", getModel()) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick() {
				LinkLabelPanel.this.onClick();
			}
			
		};
		
		Label w_label=new Label("label", label); 
		w_label.setEscapeModelStrings(false);
		
	}
	
	abstract protected void onClick();
	
	public void onDetach() {
		super.onDetach();
		if (model!=null)
			model.detach();
	}
	
	public IModel<T> getModel() {
		return model;
	}
	public void setModel(IModel<T> model) {
		this.model=model;
	}

}
