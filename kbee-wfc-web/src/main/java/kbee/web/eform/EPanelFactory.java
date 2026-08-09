package kbee.web.eform;

import java.io.Serializable;

import org.apache.wicket.markup.html.panel.Panel;

import com.novamens.content.form.EFormComponent;

public interface EPanelFactory extends Serializable {
	public Panel getPanel(EFormComponent component);
	public Panel getPanel(String id, EFormComponent component);
}
