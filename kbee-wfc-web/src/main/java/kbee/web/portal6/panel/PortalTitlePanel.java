package kbee.web.portal6.panel;


import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.portal6.model.Area;
import com.novamens.portal6.model.Block;
import com.novamens.portal6.model.PageSection;
import com.novamens.portal6.model.PortalObject;

public class PortalTitlePanel<T extends PortalObject> extends PortalPanel<T> {

	private static final long serialVersionUID = 1L;

	public PortalTitlePanel(String id, IModel<T> model) {
		super(id, model);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		if (getModel().getObject() instanceof PageSection) {
				add(new H2Fragment("title-fragment"));
		}
		else if (getModel().getObject() instanceof Area) {
			add(new H3Fragment("title-fragment"));
		}
		else if (getModel().getObject() instanceof Block) {
			add(new H4Fragment("title-fragment"));
		}
		else
			add(new H3Fragment("title-fragment"));
		
	}
	
	protected IModel<String> getTitle() {
			if (getModel().getObject()==null)
				return new Model<String>();
			return new Model<String>(getModel().getObject().getDisplayName());
	}

	
	public class H2Fragment extends Fragment {
		private static final long serialVersionUID = 1L;
		public H2Fragment(String id) {
			super(id, "h2-fragment", PortalTitlePanel.this);
			 add(new Label("title", getTitle()));	
		}
	}
	

	public class H3Fragment extends Fragment {
		private static final long serialVersionUID = 1L;
		public H3Fragment(String id) {
			super(id, "h3-fragment", PortalTitlePanel.this);
			 add(new Label("title", getTitle()));	
		}
	}

	public class H4Fragment extends Fragment {
		private static final long serialVersionUID = 1L;
		public H4Fragment(String id) {
			super(id, "h4-fragment", PortalTitlePanel.this);
			 add(new Label("title", getTitle()));	
		}
	}

				
}
