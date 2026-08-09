package com.novamens.wicket.markup.html.actions;


import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.model.IModel;


/**
 * Action panel para {@link Console} (que se pasara a llamar BrowserConsole) 
 * y otras subclase de {@link Console} 
 * 
 * @param <T> 
 * Elemento de la Consola, tal como {@link Content}, {@link User}, {@link Site}, etc
 */

@Deprecated
public abstract class ActionPanel<T> extends BaseActionPanel {
	private static final long serialVersionUID = 1L;

	private IModel<List<IModel<T>>> selectionModel;
	private kbee.web.console.Console<T> console;
		
	public ActionPanel(String id, IModel<String> label) {
		super(id, label);
		setOutputMarkupId(true);
	}
	
	public ActionPanel(String id, IModel<String> label, kbee.web.console.Console console) {
		super(id, label);
		setOutputMarkupId(true);
		this.console=console;
	}
	
	public void setSelectionModel(IModel<List<IModel<T>>> model) {
		this.selectionModel = model;
	}
	
	public IModel<List<IModel<T>>> getSelectionModel() {
		return selectionModel;
	}
	
	public List<T> getSelection() {
		if (selectionModel==null)
			return null;
		List<T> selection = new ArrayList<T>();
		for (IModel<T> selected : selectionModel.getObject()) {
			selection.add(selected.getObject());
		}
		return selection;
	}
	
	public kbee.web.console.Console<T> getConsole() {
		return console;
	}
	
	public void setConsole(kbee.web.console.Console<T> console) {
		this.console = console;
		super.setBaseConsole(console);
	}
	
	@Override
	public boolean isEnabled() {
		return (super.getEnabled() && getSelectionModel()!=null && !getSelection().isEmpty());
	}
	
	@Override
	public void addListener(com.novamens.wicket.markup.html.actions.ActionEventListener actionEventListener){
	}
	
}
