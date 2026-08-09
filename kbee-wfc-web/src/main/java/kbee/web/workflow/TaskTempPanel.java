package kbee.web.workflow;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.kbee.wicket.util.InvisiblePanel;

public class TaskTempPanel<T> extends ModelPanel<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Panel panel1;
	Panel panel2;
	public TaskTempPanel(String id, IModel<T> model, Panel panel1, Panel panel2) {
		super(id, model);
				this.panel1=panel1;
				this.panel2=panel2;
	}
	
	public void onInitialize() {
		super.onInitialize();
		
		add(panel1!=null?panel1:new InvisiblePanel("panel1"));
		add(panel2!=null?panel2:new InvisiblePanel("panel2"));
	}

}

