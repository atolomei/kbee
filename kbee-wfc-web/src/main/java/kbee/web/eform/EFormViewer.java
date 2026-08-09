package kbee.web.eform;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.beans.BeansService;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormComponent;
import com.novamens.content.form.EFormData;
import com.novamens.content.model.Classificable;
import com.novamens.kbee.wicket.editor.Editor;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.kbee.wicket.viewer.Viewer;
import com.novamens.portal6.model.Site;
import com.novamens.service.ServiceLocator;

import kbee.web.error.ErrorPanel;

/**
 *  eform-audit
 *  eform-inline
 *  eform-library
 *  eform-editor
 *  eform-library-home
  */
@SuppressWarnings("serial")
public class EFormViewer extends ModelPanel<EFormData> implements EFormPanel {
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(EFormViewer.class.getName());
										

	private IModel<Site> sitemodel;
	private EPanelFactory panelfactory;
	private String error = null;
	
	public EFormViewer(String id, IModel<EFormData> model) {
		super(id, model);
	}
	
	public EFormViewer(String id, IModel<EFormData> model, IModel<Site> sitemodel) {
		super(id, model);
		this.sitemodel = sitemodel;
	}
	
	public EForm getForm() {
		return getModelObject().getForm();
	}
	
	public EPanelFactory getPanelFactory() {
		if (panelfactory==null) {
			panelfactory = new EViewerFactory(getModel(), getSiteModel());
		}
		return panelfactory;
	}
	
	public IModel<Site> getSiteModel() {
		return sitemodel;
	}
	
	@Override
	public Classificable getObject() {
		Viewer<?> viewer = getViewer();
		if (viewer !=null ) {
			Classificable classificable = (Classificable)viewer.getModelObject();
			return classificable;
		}
		return null;
	}
	
	public Viewer<?> getViewer() {
		MarkupContainer parent = this;
		Viewer<?> viewer = null;
		while (viewer==null && parent!=null) {
			if (parent instanceof Viewer) {
				viewer = (Viewer<?>)parent;
			}
			else
				parent = parent.getParent();
		}
		return viewer;
	}
	
	public Editor<?> getEditor() {
		return null;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		checkForm();
		addBehaviors();
		addComponents();
	}
	
	protected void checkForm() {
		error = (new EFormChecker(getModelObject())).check();
	}
	
	protected void addComponents() {
		add(new ListView<EFormComponent>("component", getForm().getComponents()) {
			@Override
			public void populateItem(ListItem<EFormComponent> item) {
				EFormComponent c = item.getModelObject();
				Panel panel = getPanel("panel", c);
				if (panel!=null) {
					item.add(panel);
				}
				else {
					item.add(new InvisiblePanel("panel"));
				}
			}
			@Override
			public boolean isVisible() {
				return error==null;
			}
		});
		add (new ErrorPanel("error", new Model<String>(error)) {
			@Override
			public boolean isVisible() {
				return error!=null;
			}
		});
	}
	
	protected void addBehaviors() {
		for (String behaviorbean : getForm().getBehaviors()) {
			try {
				Object bean = ServiceLocator.getService(BeansService.class).getBean(behaviorbean);
				if (bean instanceof Behavior) {
					add((Behavior)bean);
				}
			}
			catch(Exception e) {
				logger.error(e);
			}
		}
	}
	
	protected Panel getPanel(String id, EFormComponent component) {
		return getPanelFactory().getPanel(id, component);
	}
}