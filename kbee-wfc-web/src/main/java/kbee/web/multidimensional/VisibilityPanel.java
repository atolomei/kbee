package kbee.web.multidimensional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;

import com.novamens.content.library.Library;
import com.novamens.content.library.LibraryService;
import com.novamens.content.multidimensional.FacetService;
import com.novamens.dom.DomainType;
import com.novamens.indexer.query.Facet;
import com.novamens.kbee.content.multidimensional.KbeeFacetWrapper;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;

@SuppressWarnings("serial")
public class VisibilityPanel extends ObjectEditor<Facet> {
																						
	static kbee.util.logging.Logger logger =  kbee.util.logging.Logger.getLogger(VisibilityPanel.class.getName());

	private static final long serialVersionUID = 1L;
	
	private Map<String, Boolean> visibility = new HashMap<String, Boolean>();
	private boolean updated = false;

	private List<String> consoles = null;
	
	
	public VisibilityPanel() {
		this("visibility");
	}
	
	public VisibilityPanel(String id) {
		super(id);
		setOutputMarkupId(true);
	}

	@Override
	public void update(AjaxRequestTarget target) {
		try {
			if (updated) {
				for (String console : getConsoles()) {
					((KbeeFacetWrapper)getModelObject()).setVisibility(console, isVisible(console));
					setUpdatedPart("Visibility");
				}
				getDomain().getService(FacetService.class).update(getModelObject(), getUpdatedParts());
				super.reset();
				target.add(VisibilityPanel.this.getPage());
				updated = false;
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<>(target, e));
		}
	}
	
	/** --------------------------------------------------------------------------------------------
	 */
	public List<String> getConsoles() {
		
		if (consoles!=null)
			return consoles;
		
		consoles = new ArrayList<String>();
		
		boolean is_free_version = (getDomain().getDomainType()==DomainType.EXPRESS);
		
		consoles.add("workspace");
		consoles.add("monitor");
		
		if (!is_free_version)
			consoles.add("pending");
		
		List<Library> libraries  = getDomain().getService(LibraryService.class).getLibraries();
		for (Library library : libraries) {
			consoles.add(library.getKey());
		}
		
		consoles.add("portals");
		
		consoles.add("mydocs");
		
		consoles.add("users");
		
		return consoles;
	}
	

	

	public boolean isVisible(String console) {
		return visibility.get(console) != null ? visibility.get(console) : false;
	}

	
	public void setVisible(String console, boolean value) {
		updated = true;
		visibility.put(console, value);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		setVisibility();
		setEditionEnabled(false);
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		form.add(new ListView<String>("console", getConsoles()) {
			public void populateItem(ListItem<String> item) {
				item.add(new Label("name", item.getModelObject()));
			}
		});
		
		form.add(new ListView<String>("visibility", getConsoles()) {
			public void populateItem(final ListItem<String> item) {
				AjaxLink<?> valueLink = new AjaxLink<Void>("value-link") {
					@Override
					public void onClick(AjaxRequestTarget target) {
						VisibilityPanel.this.setVisible(item.getModelObject(), !VisibilityPanel.this.isVisible(item.getModelObject()));
						target.add(VisibilityPanel.this);
					}
					@Override
					public boolean isEnabled() {
						return isEditionEnabled();
					}
				};
				Label value = new Label("value", new Model<String>() {
					public String getObject() {
						return VisibilityPanel.this.isVisible(item.getModelObject())? "&#x2714;" : "&nbsp;";
					}
				});
				value.setEscapeModelStrings(false);
				valueLink.add(value);
				item.add(valueLink);
			}
		});
		
		add(form);
		
		add(new EditButtonsV5<Facet>(this) {
			@Override
			protected String getCancelClass() {
				return "btn btn-default btn-sm";
			}
			@Override
			protected String getSubmitClass() {
				return "btn btn-primary btn-sm";
			}
			protected String getEditClass() {
				return "btn btn-primary btn-sm";
			}
		});	

	}

//	protected Domain getDomain() {
//		return ServiceLocator.getService(UserService.class).getDomain();
//	}
	
	private void setVisibility() {
		for (String console : getConsoles()) {
			setVisible(console, ((KbeeFacetWrapper)getModelObject()).isVisible(console));
		}
	}
	
	/*
	 * protected IModel<String> getLabel(String key) { return new
	 * StringResourceModel(key, this, null); }
	 */
	
//	protected ContentDao getContentDao() {
//		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
//	}
}
