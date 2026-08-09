package kbee.web.object;

import org.apache.logging.log4j.LogManager;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

import com.novamens.content.user.UserService;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;

@SuppressWarnings("serial")
public class ObjectStatusPanel<T extends com.novamens.dom.Object> extends ModelPanel<T> {
			
	static kbee.util.logging.Logger logger = new kbee.util.logging.Logger(LogManager.getLogger(ObjectStatusPanel.class.getName()));

	private static final long serialVersionUID = 1L;

	public ObjectStatusPanel(String id, IModel<T> model) {
		super(id, model);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer st= new WebMarkupContainer("status") {
			@Override
			public boolean isVisible() {
				return ObjectStatusPanel.this.getModel().getObject().getState()!=ObjectState.ENABLED; 
			}
		};
		
		if (getModel().getObject().getState()!=null) {
			try {
				st.add(new AttributeModifier("class", getModel().getObject().getState().getIcon()));
				st.add(new AttributeModifier("title", 
						getModel().getObject().getState().getLabel(getSessionUser().getLocale()) +" - " + 
						( (getModel().getObject().getLastModifiedUser()!=null) ? (getModel().getObject().getLastModifiedUser().getFirstLastName() + " - " +  getModel().getObject().getLastModifiedOffsetDateTimeColloquial(null)) : "") 
					));
				
			} catch (Exception e) {
				logger.error(e);
				st.add( new AttributeModifier("title", e.getClass().getSimpleName()));
			}
		}
		add(st);
	}
	
	protected User getSessionUser() {
		return  ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();
	}
	
	public boolean isVisible() {
		return getModel().getObject().getState()!=ObjectState.ENABLED;
	}
}