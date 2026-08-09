package kbee.web.console;

import org.apache.wicket.model.IModel;
import com.novamens.content.model.Classificable;
import kbee.web.console.grid.LabelSetPanel;

public class ClassificableNameColumnPanel<T extends Classificable> extends NameColumnPanel<T> {
	private static final long serialVersionUID = 1L;
	public ClassificableNameColumnPanel(String id, IModel<T> model) {
		super(id, model);
		setOutputMarkupId(true); 
	}
	@Override
	public void onInitialize() {
		super.onInitialize();
		add(new LabelSetPanel<T>("labels", getModel(), false, true, false));
	}
}
