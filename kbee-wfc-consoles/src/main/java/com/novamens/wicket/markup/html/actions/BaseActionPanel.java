package com.novamens.wicket.markup.html.actions;


import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * Panel Base de las acciones que se integran en los
 * menpues de las Consolas. Las acciones son
 * de dos tipos:
 * 
 * Acciones de menúes de Console<T> donde se pueden seleccionar uno o más elementos T
 * Acciones de menúes globales donde no se lleva registro de la selección, y en ciertos casos
 * ni siquiera se trata de Console sino que pueden ser otras consolas 
 * que no contienen Browser<T>
 * 
 */
public class BaseActionPanel extends Panel implements Action {

	private static final long serialVersionUID = 1L;

	private IModel<String> labelmodel;
	private boolean enabled = true;
	private kbee.web.console.Console<?> console;

	public BaseActionPanel(String id, IModel<String> label) {
		super(id);
		this.labelmodel = label;
	}

	public BaseActionPanel(String id, IModel<String> label,kbee.web.console.Console<?> console) {
		super(id);
		this.labelmodel = label;
		setBaseConsole(console);
	}

	
	@Override
	public void addListener(ActionEventListener listener) {
	}

	public String getLabel() {
		if (labelmodel==null)
			return null;
		return labelmodel.getObject();
	}
	
	public void setLabel(String label) {
		if (this.labelmodel==null)
			this.labelmodel = new Model<String>();
		this.labelmodel.setObject(label);
	}
	
	public IModel<String> getLabelModel() {
		return labelmodel;
	}
	
	public void setLabel(IModel<String> label) {
		this.labelmodel=label;
	}

	public void setActionEnabled(boolean value) {
		this.enabled = value;
	}

	protected boolean getEnabled() {
		return enabled;
	}
	
	public void setBaseConsole(kbee.web.console.Console<?> console) {
		this.console = console;
	}
	
	public String getCssClassName() {
		return null;
	}
	
	public ResourceReference getIcon() {
		return null;
	}
	
	public kbee.web.console.Console<?> getBaseConsole() {
		return this.console;
	}

	protected void execute() {
	}


	
}
