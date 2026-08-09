package kbee.web.panel;

import java.util.Optional;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;

import com.googlecode.wicket.jquery.ui.markup.html.link.AjaxFallbackLink;

@SuppressWarnings("serial")
public class ListAjaxItemMainPanel<T> extends ListSimpleItemMainPanel<T> {
	private static final long serialVersionUID = 1L;

	public ListAjaxItemMainPanel(String id) {
		super(id);
	}
	
	public ListAjaxItemMainPanel(String id, IModel<T> model, int index, boolean is_expanded) {
		super(id, model, index, is_expanded);
	}
	
	protected Link<T> getLink() {
		Link<T> link = new AjaxFallbackLink<T> ("item-link", getModel()) {
			@Override
			public void onClick(Optional<AjaxRequestTarget> optional) {
				ListAjaxItemMainPanel.this.onClick(optional.get());
			}
		};
		return link;
	}
}
