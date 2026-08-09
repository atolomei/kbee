package kbee.web.portal6.panel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.markup.html.panel.Panel;

import com.novamens.beans.BeansService;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.service.ServiceLocator;

public class DirectoryFavoritesHeaderPanel extends Panel {

	private static final long serialVersionUID = 1L;

	static private Logger logger = LogManager.getLogger(DirectoryFavoritesHeaderPanel.class.getName());

	Panel myFavsPanel = new InvisiblePanel("favs");
	Panel sitesPanel = new InvisiblePanel("sites");
	Panel appPanel = new InvisiblePanel("apps");

	private boolean isMyFavsPanel = false;
	private boolean isSitesPanel = false;
	private boolean isAppPanel = false;

	private int selected = -1;

	public DirectoryFavoritesHeaderPanel(String id) {
		super(id);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		add(myFavsPanel);
		add(sitesPanel);
		add(appPanel);
	}

	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		addOrReplace(getUserFavorites("favs"));
	}

	/**
	 * ------------------------------------------------------
	 * 
	 * MySitesContextualMenu2
	 */
	private Panel getUserFavorites(String panelid) {
		try {

			Panel panel = (Panel) ServiceLocator.getService(BeansService.class).getBean("user-mysites", panelid);

			if (panel != null)
				return panel;
			else
				logger.warn("user-mysite is null | getUserFavorites() ");
			return (new Panel(panelid) {
				private static final long serialVersionUID = 1L;

				@Override
				public boolean isVisible() {
					return false;
				}
			});

		} catch (Exception e) {

			logger.error(e.getClass().getName() + " | getUserFavorites() ");

			return (new Panel(panelid) {
				private static final long serialVersionUID = 1L;

				@Override
				public boolean isVisible() {
					return false;
				}
			});
		}
	}

}
