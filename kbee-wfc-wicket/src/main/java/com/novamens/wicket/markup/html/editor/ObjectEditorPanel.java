package com.novamens.wicket.markup.html.editor;


import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.form.IFormModelUpdateListener;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;

import com.novamens.content.form.UpdatedField;
import com.novamens.kbee.wicket.editor.Editor;
import com.novamens.wicket.markup.html.panel.KBPanel;

/**
 * 
 * A Wicket Panel that contains a {@link Editor<T>}
 *
 * @param <T>
 */
@SuppressWarnings("serial")							
public class ObjectEditorPanel<T> extends KBPanel implements IFormModelUpdateListener {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ObjectEditorPanel.class.getName());
	
	private Editor<T> editor;
	private boolean readonly = false;
	
	public ObjectEditorPanel(String id) {
		super(id);
	}
	
	public void updateModel() {
	}
	
	public void update(T object) {
	}
	
	public void cancel() {
	}
	
	public void setEditor(Editor<T> editor) {
		this.editor = editor;
	}
	
	public T getModelObject() {
		return getEditor().getModelObject();
	}
	
	public IModel<T> getModel() {
		return getEditor().getModel();
	}
	
	@SuppressWarnings("unchecked")
	public Editor<T> getEditor() {
		if (editor==null) {
			MarkupContainer parent = getParent();
			Editor<T> editor = null;
			while (editor==null && parent!=null) {
				if (parent instanceof Editor) {
					editor = (Editor<T>)parent;
					setEditor(editor);
				}
				else
					parent = parent.getParent();
			}
		}
		
		if (editor==null) {
			logger.debug("editor is null");
			
		}
		return this.editor;
	}
	
	public boolean isReadOnly() {
		return this.readonly;
	}
	
	public void setReadOnly(boolean re) {
		this.readonly=re;
	}
	
	protected void setUpdatedPart(String part) {
		if (getEditor()!=null)
			getEditor().setUpdatedPart(part);
	}
	
	
	protected void setUpdatedField(UpdatedField update) {
		if (getEditor()!=null) {
			getEditor().setUpdatedField(update);
		}	
	}

	/**
	 * URL from HTTP Request received
	 * Wicket based
	 * 
	 * @return
	 */
	protected String getServerUrl() {
		String protocol =((WebRequest)RequestCycle.get().getRequest()).getUrl().getProtocol();
		String host =((WebRequest)RequestCycle.get().getRequest()).getUrl().getHost();
		Integer iport =((WebRequest)RequestCycle.get().getRequest()).getUrl().getPort(); 
		String port = (iport.equals(80) || iport.equals(443) ? "":  ( ":" + iport.toString()) );
		return protocol +"://" + host + port;
	}
}