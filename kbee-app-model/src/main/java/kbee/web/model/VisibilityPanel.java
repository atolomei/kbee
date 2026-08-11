package kbee.web.model;

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

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.library.Library;
import com.novamens.content.library.LibraryService;
import com.novamens.content.model.ModelElement;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.kbee.wicket.editor.Editor;
import com.novamens.service.ServiceLocator;


@SuppressWarnings("serial")
public class VisibilityPanel<T extends ModelElement> extends com.novamens.wicket.markup.html.editor.ObjectEditorPanel<T> {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(VisibilityPanel.class.getName());

	private static final long serialVersionUID = 1L;

	private Map<String, Boolean> visibility = new HashMap<String, Boolean>();
	private boolean updated = false;

	private List<String> consoles = null;
	
	public VisibilityPanel(Editor<T> editor) {
		super("visibility");
		
		setOutputMarkupId(true);
		setEditor(editor);
		setVisibility();
		
		add(new ListView<String>("console", getConsoles()) {
			public void populateItem(ListItem<String> item) {
				item.add(new Label("name", item.getModelObject()));
			}
		});
		
		add(new ListView<String>("visibility", getConsoles()) {
			public void populateItem(final ListItem<String> item) {
				AjaxLink<?> valueLink = new AjaxLink<Void>("value-link") {
					@Override
					public void onClick(AjaxRequestTarget target) {
						VisibilityPanel.this.setVisible(item.getModelObject(), !VisibilityPanel.this.isVisible(item.getModelObject()));
						target.add(VisibilityPanel.this);
					}
					@Override
					public boolean isEnabled() {
						return getEditor().isEditionEnabled();
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
	}
	
	
	@Override
	public void updateModel() {
		
		if (!updated) 
			return;
		
		for (String console : getConsoles()) {
			getEditor().getModelObject().setVisibility(console, isVisible(console));
			getEditor().setUpdatedPart("Visibility");
		}
		updated = false;
	}
	
	public List<String> getConsoles() {
		
		if (consoles!=null)
			return consoles;
		
		consoles = new ArrayList<String>();
		
		
		
		consoles.add("workspace");
		consoles.add("monitor");

		if (!isFreeVersion())
			consoles.add("pending");
		
		List<Library> libraries  = getDomain().getService(LibraryService.class).getLibraries();
		for (Library library : libraries) {
			consoles.add(library.getKey());
		}
		
		consoles.add("portals");
		return consoles;
	}

	public boolean isVisible(String console) {
		return visibility.get(console) != null ? visibility.get(console) : false;
	}

	public void setVisible(String console, boolean value) {
		updated = true;
		visibility.put(console, value);
	}

	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}

	private void setVisibility() {
		for (String console : getConsoles()) {
			setVisible(console, getEditor().getModelObject().isVisible(console));
		}
	}

	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	protected boolean isFreeVersion() {
		try {
			return getDomain().getDomainType()==DomainType.EXPRESS;
		}
		 catch (Exception e) {
			 logger.error(e);
			 return false;
		 }
	}
}
