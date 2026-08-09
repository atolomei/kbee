package kbee.web.searcher;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.event.Event;

import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.ExtendedChoiceField;

public class SearchSortSelectorPanel extends Panel {

	private static final long serialVersionUID = 1L;

	private String source;

	private static String relevance = "relevance";
	private static String title = "title";
	private static String date = "lastmodified";

	public SearchSortSelectorPanel(String id) {
		super(id);
		setOutputMarkupId(true);
		addChoices();
	}

	public List<String> getSources() {
		List<String> sources = new ArrayList<String>();
		sources.add(relevance);
		sources.add(title);
		sources.add(date);
		return sources;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getSource() {
		return source;
	}

	protected void addChoices() {

		add(new ExtendedChoiceField<String>("source", new PropertyModel<String>(this, "source"),
				new PropertyModel<List<String>>(this, "sources")) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onUpdate(AjaxRequestTarget target) {
				String source = getValue();
				SearchSortSelectorPanel.this.onUpdate(source, target);
			}

			@Override
			public String getIdValue(String value) {
				return value.toLowerCase();
			}

			@Override
			public String getDisplayValue(String value) {
				return (new StringResourceModel(value, SearchSortSelectorPanel.this, null)).getObject();
			}
		});
	}

	protected void onUpdate(String source2, AjaxRequestTarget target) {
	}
	
	public void fire(Event event) {
		fire(event, true);
	}

	@SuppressWarnings("unchecked")
	public void fire(Event event, boolean only_one_consumer) {

		for (WicketEventListener<Event> listener : getPage().getBehaviors(WicketEventListener.class)) {
			if (listener.handle(event)) {
				listener.onEvent(event);
				if (only_one_consumer)
					return;
			}
		}
		fire(event, getPage().iterator(), only_one_consumer);
	}

	protected boolean fire(Event event, Iterator<Component> components) {
		return fire(event, components, true);
	}

	@SuppressWarnings("unchecked")
	protected boolean fire(Event event, Iterator<Component> components, boolean only_one_consumer) {
		boolean handled = false;
		while (components.hasNext()) {
			Component component = components.next();
			for (WicketEventListener<Event> listener : component.getBehaviors(WicketEventListener.class)) {
				if (listener.handle(event)) {
					listener.onEvent(event);
					handled = true;

					if (only_one_consumer) {
						break;
					}
				}
			}

			if (!only_one_consumer || !handled) {
				if (component instanceof MarkupContainer) {	
					handled = fire(event, ((MarkupContainer) component).iterator(), only_one_consumer);
				}
			} else {
				break;
			}
		}
		return handled;
	}

	protected void addListeners() {
		// TODO Auto-generated method stub
	}

	/**
	 * Domain of the Session User
	 */
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}

	protected User getSessionUser() {
		try {
			return ServiceLocator.getService(SecurityService.class).getSessionUser();
		} catch (Exception e) {
			return null;
		}
	}

}
