package kbee.web.eform;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.form.EDisposition;
import com.novamens.content.form.EFormComponent;
import com.novamens.content.form.EFormData;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.kbee.content.form.EFormAbstractComponent;
import com.novamens.kbee.content.form.KbeeEFormRow;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.panel.KBPanel;

@SuppressWarnings("serial")
public class EComponentPanel<T extends EFormComponent> extends KBPanel {
	private static final long serialVersionUID = 1L;
	
	private IModel<EFormData> datamodel;
	private IModel<T> componentmodel;
	private WebMarkupContainer container;
	private EPanelFactory panelfactory;
		
	public EComponentPanel(String id, T field, IModel<EFormData> data) {
		super(id);
		setComponent(field);
		setData(data);
	}
	
	public EComponentPanel(String id, T field) {
		super(id);
		setComponent(field);
	}
	
	public T getComponent() {
		return getComponentModel().getObject();
	}
	
	
	public void setComponent(T field) {
		this.componentmodel = new ComponentModel<T>(field);
	}
	
	public EFormData getData() {
		return getDataModel()!=null ? getDataModel().getObject() : null;
	}
	
	public void setData(IModel<EFormData> model) {
		this.datamodel = model;
	}
	
	public IModel<EFormData> getDataModel() {
		return datamodel;
	}
	
	public IModel<T> getComponentModel() {
		return componentmodel;
	}
	
	public void setPanelFactory(EPanelFactory factory) {
		this.panelfactory = factory;
	}
	
	public Disposition getDisposition() {
		
		if (getData().getForm().getDisposition()==null)
			return Disposition.VERTICAL;
			
		return getData().getForm().getDisposition().equals(EDisposition.VERTICAL) 
			? Disposition.VERTICAL
			: Disposition.HORIZONTAL;
	}
	
	@Override
	public boolean isVisible() {
		return getComponent().isVisible(getData());
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		if (container==null) {
			container = new WebMarkupContainer("container");
			add(container);
			getContainer().setOutputMarkupId(true);
			if (getCssClass()!=null) {
				getContainer().add(new AttributeModifier("class", new Model<String>() {
					public String getObject() {
						return getCssClass();
					}
				}));
			}
			else {
				getContainer().add(new AttributeModifier("style", "float:left; width:100%;"));
			}
		}
	}
	
	public void onDetach() {
		super.onDetach();
		if (datamodel!=null)
			datamodel.detach();
		if (componentmodel!=null)
			componentmodel.detach();
	}
	
	protected WebMarkupContainer getContainer() {
		return container;
	}
	
	protected Panel getPanel(EFormComponent component) {
		return getPanelFactory().getPanel(component);
	}
	
	protected Panel getPanel(String id, EFormComponent component) {
		return getPanelFactory().getPanel(id, component);
	}
	
	protected String getCssClass() {
		String css = "";
		if (getComponent().getCssClass()!=null) {
			css += getComponent().getCssClass();
		}
		return "".equals(css.trim()) ? null : css.trim();
	}
	
	protected boolean rowParent(EFormComponent component) {
		EFormComponent parent;
		if (component!=null && component instanceof EFormAbstractComponent && (parent=((EFormAbstractComponent)component).getParent())!=null) {
			if (parent instanceof KbeeEFormRow) {
				return true;
			}
			else {
				return rowParent(parent);
			}
		}
		else {
			return false;
		}
	}
	
	protected EPanelFactory getPanelFactory() {
		if (panelfactory==null) {
			panelfactory = new EEditorFactory(getDataModel());
		}
		return panelfactory;
	}
	
	/**
	 * SessionUser may be null in SharedPages
	 * 
	 * @return
	 */
	protected KbeeUser getSessionUser() {
		UserProfile up = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		if (up!=null)
			return (KbeeUser) up.getUser();
		return null;
	}	

	public void setPreference(String key, String value) {
		if (getSessionUser()!=null)
			((com.novamens.kbee.security.KbeeUser) getSessionUser()).getService(PreferencesService.class).setValue(this.getClass().getName(), key, value);
	}
	
	public void setIntPreference(String key, int value) {
		if (getSessionUser()!=null)
			((com.novamens.kbee.security.KbeeUser) getSessionUser()).getService(PreferencesService.class).setIntValue(this.getClass().getName(), key, value);
	}
	
	public String getPreference(String key, String defaultValue) {
		if (getSessionUser()!=null)
		return ((com.novamens.kbee.security.KbeeUser) getSessionUser()).getService(PreferencesService.class).getValue( this.getClass().getName(), key, defaultValue);
		return defaultValue;
	}

	public int getIntPreference(String key, int defaultValue) {
		if (getSessionUser()!=null)		
			return ((com.novamens.kbee.security.KbeeUser) getSessionUser()).getService(PreferencesService.class).getIntValue(this.getClass().getName(), key, defaultValue);
		return defaultValue;
	}
}