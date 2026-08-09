package kbee.web.object;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.novamens.kbee.wicket.util.DisplayNameExtractor;
import com.novamens.security.Identifiable;

/**
 *  Hay dos SelectionPanel !!!! 
 * 
 * For Object not Content
 * 
 *
 * @param <T>
 */
@SuppressWarnings("serial")
public class SelectionPanel<T> extends Panel {
	private static final long serialVersionUID = 1L;
	
	private List<IModel<T>> selection;
	private Map<Serializable, String> status = new HashMap<Serializable, String>();

	public SelectionPanel(String selectionLabel, List<IModel<T>> selection) {
		super("selection");
		
		setOutputMarkupId(true);
		
		add (new Label("selection-label", selectionLabel));
		
		setSelection(selection);
		
		add(new ListView<IModel<T>>("selection", new PropertyModel<List<IModel<T>>>(this, "selection")) {
			public void populateItem(final ListItem<IModel<T>> item) {
				Object object = item.getModelObject().getObject();
				
				String title;
				
				if (object instanceof Identifiable)
					title = ((Identifiable) object).getDisplayName();
				else
					title = DisplayNameExtractor.get(object);
				
				item.add(new Label("title", title));

				item.add(new Label("status", new Model<String>() {
					public String getObject() {
						String status = SelectionPanel.this.status.get(((Identifiable)item.getModelObject().getObject()).getId());
						status = status==null ? "" : (status.equals("") ? "<p class=\"success\">OK</p>" : "<p class=\"danger\">"+status+"</p>");
						return status;
					}
				}));
				
				((Label)item.get("status")).setEscapeModelStrings(false);
				
				item.add(new AjaxLink<Void>("remove-link") {
					public void onClick(AjaxRequestTarget target) {
						removeSelection(item.getIndex());
						target.add(SelectionPanel.this);
						onUpdate(target);
					}
					public boolean isVisible() {
						return getSelection().size()>1;
					}
				});
			}
		});
	}
	
	public void onUpdate(AjaxRequestTarget target) {
		
	}
	
	 
	public List<IModel<T>> getSelection() {
		return selection;
	}
	
	public void setStatus(T object, String message) {
		status.put(((Identifiable)object).getId(), message);
	}
	
	public boolean hasErrors() {
		for (String xstatus : status.values()) {
			if (!"".equals(xstatus)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		for (IModel<T> model : getSelection()) {
			model.detach();
		}
	}
	

	protected void setSelection(List<IModel<T>> selection) {
		this.selection = selection;
		Collections.sort(this.selection, (new Comparator<IModel<T>>() {
			@Override
			public int compare(IModel<T> a, IModel<T> b) {
				try {
					String alabel = DisplayNameExtractor.get(a);
					String blabel = DisplayNameExtractor.get(b);
					return alabel.compareToIgnoreCase(blabel);
				} 
				catch (Exception e) {
					return 0;
				}
			}
		}));
	}
	

	protected void removeSelection(int index) {
		selection.remove(index);
	}
	

}
