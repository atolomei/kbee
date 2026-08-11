package kbee.web.portal6;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;

import com.novamens.dom.ObjectState;
import com.novamens.portal.model.diagrammablesite.DiagrammableSite;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.SiteType;

import kbee.web.portal6.panel.PortalPanel;

public abstract class SiteSelectorPanel extends PortalPanel {

	static final private org.apache.logging.log4j.Logger logger = LogManager
			.getLogger(SiteSelectorPanel.class.getName());

	public static final int APPLICATIONS = 1;
	public static final int SITES = 2;
	public static final int ALL = 3;

	private static final long serialVersionUID = 1L;

	private int type;

	private SiteListProviderModel listmodel;

	public class SiteListProviderModel implements IModel<SiteListProvider> {

		private static final long serialVersionUID = 1L;
		SiteListProvider object;

		public SiteListProviderModel(SiteListProvider object) {
			setObject(object);
		}

		@Override
		public void detach() {
			object.detach();
		}

		@Override
		public SiteListProvider getObject() {
			return object;
		}

		@Override
		public void setObject(SiteListProvider object) {
			this.object = object;
		}
	}

	public class SiteListProvider implements Serializable {
		private List<Site> list = null;

		private int type = SITES;

		private static final long serialVersionUID = 1L;

		public SiteListProvider(int type) {
			this.type = type;
		}

		public void detach() {
			list = null;
		}

		public List<Site> getSites() {

			try {
				if (list == null) {
					
					// TODO VER AT SITE
					//if (type == APPLICATIONS)
					//	list = getPortalDao().getSites(getDomain(), SiteType.APPLICATION, ObjectState.ENABLED);
					//else if (type == SITES)
					//	list = getPortalDao().getSitesNotSiteType(getDomain(), SiteType.APPLICATION,ObjectState.ENABLED, false);
					//else
					list = getPortalDao().getSites(getDomain(), ObjectState.ENABLED);
				}

			} catch (javax.persistence.EntityNotFoundException e) {
				logger.error(e);
				list = new ArrayList<Site>();
			}

			return list;
		}
	}

	/**
	 * Applications or Sites
	 * 
	 * @return
	 */
	public int getType() {
		return this.type;
	}

	public void setType(int type) {
		this.type = type;
	}

	/**
	 * IModel<Person> model
	 * 
	 */
	public SiteSelectorPanel(String id, int type) {
		super(id);
		setType(type);
		addComponents();
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	/**
	 * Lista de Sitios
	 */
	private void addComponents() {

		this.listmodel = new SiteListProviderModel(new SiteListProvider(getType()));

		com.novamens.wicket.model.ListModel<Site> lm = new com.novamens.wicket.model.ListModel<Site>(this.listmodel,
				"sites");

		ListView<Site> results = new ListView<Site>("result", lm) {
			private static final long serialVersionUID = 1L;

			protected void populateItem(ListItem<Site> item) {
				Link<Site> link = new Link<Site>("link", item.getModel()) {
					private static final long serialVersionUID = 1L;

					@Override
					public void onClick() {
						SiteSelectorPanel.this.onClick(getModel());
					}
				};

				item.add(link);
				try {
					Label title = new Label("title", item.getModel().getObject().getTitle());
					link.add(title);
				} catch (Exception e) {
					logger.error(e.getStackTrace());
					link.add(new Label("title", "err"));
				}
			}
		};

		add(results);
	}

	protected abstract void onClick(IModel<Site> model);

	protected abstract void onClick(AjaxRequestTarget target, IModel<Site> model);

}
