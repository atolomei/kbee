package kbee.web.portal6.panel;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.kbee.wicket.util.InvisiblePanel;

public class Site6NonHibernateHitPanel<T> extends PO6HitPanel<T> {

	
	private static final long serialVersionUID = 1L;
	
	Panel editor;
	boolean is_editor_created = false;
	
	public Site6NonHibernateHitPanel(String id, IModel<T> model) {
		super(id, model);
		setOutputMarkupId(true);
		add(new InvisiblePanel("editor"));
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
	}
	
	public void setEditor(Panel panel) {
		editor=panel;
		is_editor_created = true;
		addOrReplace(editor);
	}
	

	
	
}
