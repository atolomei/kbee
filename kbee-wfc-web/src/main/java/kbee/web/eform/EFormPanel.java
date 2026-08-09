package kbee.web.eform;

import com.novamens.content.model.Classificable;
import com.novamens.kbee.wicket.editor.Editor;


/**
 * 
 *
 */
public interface EFormPanel {
	public Classificable getObject();
	public Editor<?> getEditor();
}
