package kbee.web.portal6.panel;


import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.kbee.security.KbeeUser;

import com.novamens.portal.service.PortalUserService;
import com.novamens.portal6.model.ViewBK;
import com.novamens.portal6.model.ViewBKContent;
import com.novamens.portal6.model.ViewBKLink;
import com.novamens.portal6.model.ViewBKSite;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.MenuItemPanelV5;

import kbee.web.error.ApplicationErrorPage;


/**
 * This Panel is used by the GlobalHeaderPanel
 */
public class MyActivityContextualMenu extends ContextMenuPanel<ViewBK> {

	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(MyActivityContextualMenu.class.getName());

	private Map<Integer, IModel<ViewBK>> map_model;

	public MyActivityContextualMenu(String id) {
		super(id, null);

		setOutputMarkupId(true);
		addComponents();

	}

	private void addComponents() {

		try {
			for (int n = 0; n < getRecentActivityMap().size(); n++) {

				final int NDX = n;

				addItem(new MenuItemFactory<ViewBK>() {

					private static final long serialVersionUID = 1L;

					public MenuItemPanelV5<ViewBK> getItem(String id) {

						return new MenuItemPanelV5<ViewBK>(id) {

							private static final long serialVersionUID = 1L;

							@Override
							public void onClick() {

								try {
									// View Link sin link no hace nada
									// ---------------------------------------------------------

									ViewBK view = getModel().getObject();

									if (view instanceof ViewBKLink && ((ViewBKLink) view).getLink() == null)
										return;

									try {

										// ServiceLocator.getService(PortalAnalyticsService.class).add(getSessionUser(),
										// lb.getSite(), xmodel, lb.includeInRecentVisited());
										//
										// External Resource -> linkea directo no hay mas nada que hacer
										// -------------------------------------
										//
										if (view instanceof ViewBKLink) {
											setResponsePage(new RedirectPage(((ViewBKLink) view).getLink()));
											return;
										}

										WebPage page = null;

										// Site
										// ---------------------------------------------------------------------------------------------

										if (view instanceof ViewBKSite) {
											throw new KbeeRuntimeException("page = view.getResponsePage();");
											//page = view.getResponsePage();
										}

										// Content
										// ---------------------------------------------------------------------------------------------
										//
										else if (view instanceof ViewBKContent) {
											throw new KbeeRuntimeException("page = ((ViewBKContent) view).getResponsePage(false);");
											
										}

										if (page != null)
											setResponsePage(page);
										else
											setResponsePage(
													new ApplicationErrorPage(new Model<String>("Page not found"), null));

									} catch (org.hibernate.ObjectNotFoundException e) {
										logger.error(e);
										setResponsePage(new ApplicationErrorPage(e));
												

									} catch (Exception e) {
										logger.error(e);
										setResponsePage(new ApplicationErrorPage(e));
									}

								} catch (Exception e) {
									logger.error(e);
									setResponsePage(new ApplicationErrorPage(e));
								}
							}

							@Override
							public String getTarget() {
								return "_blank";
							}

							@Override
							public String getLabel() {
								try {
									if (getRecentActivityMap().get(NDX).getObject().getTitle() != null)
										return getRecentActivityMap().get(NDX).getObject().getTitle();
									return "OId: " + getRecentActivityMap().get(NDX).getObject().getObject().toString();

								} catch (Exception e) {
									return "err";
								}
							}

							@Override
							public IModel<ViewBK> getModel() {
								return getRecentActivityMap().get(NDX);
							}
						};
					}
				});
			}

		} catch (Exception e) {
			logger.error(e.getClass().getName() + " | " + Thread.currentThread().getStackTrace()[1].getMethodName());
		}

	}

	public Map<Integer, IModel<ViewBK>> getRecentActivityMap() {

		if (this.map_model == null) {
			try {
				this.map_model = new HashMap<Integer, IModel<ViewBK>>();
				PortalUserService srv = (PortalUserService) getSessionUser().getService(PortalUserService.class);

				// TOD VAR AT
				//int n = 0;
				//for (ViewBK view : srv.getRecentActivity()) {
				//	this.map_model.put(Integer.valueOf(n++), new ObjectModel<ViewBK>(view));
				//}
			} catch (Exception e) {
				logger.error(e);
			}
		}
		return this.map_model;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		if (this.map_model != null) {
			for (Entry<Integer, IModel<ViewBK>> entry : map_model.entrySet()) {
				entry.getValue().detach();
			}
		}
	}

	protected KbeeUser getSessionUser() {
		return (KbeeUser) ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

}
