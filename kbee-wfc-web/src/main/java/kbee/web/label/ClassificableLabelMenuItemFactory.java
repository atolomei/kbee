package kbee.web.label;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.novamens.content.model.Classificable;
import com.novamens.content.model.LabelMember;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;

import kbee.web.event.wicket.ErrorEvent;

public class ClassificableLabelMenuItemFactory<T extends Classificable> implements MenuItemFactory<T> {

	
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ClassificableLabelMenuItemFactory.class.getName());
	
	private IModel<LabelMember> model;
	private IModel<T> object_model;
	
	public ClassificableLabelMenuItemFactory(IModel<LabelMember> model, IModel<T> object_model) {
		this.model = model;
		this.object_model = object_model;
		model.detach();
	}
	
	@Override
	public AbstractMenuItemPanelV5<T> getItem(String id) {
		
		
		return new ClassificableLabelMenuItem<T>(id,  model, object_model) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				try {
					ClassificableLabelMenuItemFactory.this.onUpdate(target);
				} catch (Exception e) {
					logger.error(e);
					fire( new ErrorEvent<>(target, e));
				}
			}
		};
	}

	public void onUpdate(AjaxRequestTarget target) {
	}
}
