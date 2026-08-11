package kbee.web.security.role;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.EntitySet;
import com.novamens.content.user.UserService;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.security.KbeeDomainRole;
import com.novamens.kbee.content.security.KbeeEntityRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.HeaderMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.model.ObjectModel;

@SuppressWarnings("serial")
public abstract class RoleFactoryPanel extends Panel {
	
	private static final long serialVersionUID = 1L;

	public RoleFactoryPanel() {
		this("new-rule");
	}
		
	public RoleFactoryPanel(String id) {
		super(id);
		
		ContextMenuPanel<Classifier> menu = new ContextMenuPanel<Classifier>(null);
		
		WebMarkupContainer newm = new WebMarkupContainer ("new-multiple-button");
		newm.add(new AttributeModifier("class", "btn-md btn btn-primary dropdown-toggle"));
		newm.add(new AttributeModifier("data-toggle", "dropdown"));
		add(newm);
		
		menu.addItem(new MenuItemFactory<Classifier>() {
			@Override
			public AbstractMenuItemPanelV5<Classifier> getItem(String id) {
				return new MenuItemPanelV5<Classifier>(id) {
					@Override
					public void onClick() {
						onCreate(KbeeDomainRole.TYPE, null);
					}
					@Override
					public String getLabel() {
						return new StringResourceModel("domainrole", RoleFactoryPanel.this, null).getObject();
					}
					@Override
					public String getTarget() {
						return "_blank";
					}
				};
			}
		});
		
		
		
		menu.addItem(new MenuItemFactory<Classifier>() {
			@Override
			public AbstractMenuItemPanelV5<Classifier> getItem(String id) {
				return new SeparatorMenuItemPanelV5<Classifier>(id) {
					@Override
					public String getCssClass() {
						return "divider";
					}
					@Override
					public boolean isVisible() {
						return  true;
					}
				};
			}
		});

		
		int n = 0;
		
		
		menu.addItem(new MenuItemFactory<Classifier>() {
			@Override
			public AbstractMenuItemPanelV5<Classifier> getItem(String id) {
					return new HeaderMenuItemPanelV5<Classifier>(id) {
						@Override
						public String getLabel() {
							return  new StringResourceModel("entityrole",RoleFactoryPanel.this, null).getObject();
						}
					};
				}
		});
		
		for (IModel<Classifier> ci: getClassifiers()) {
			
			final Integer index = Integer.valueOf(n++);
			final String name = ci.getObject().getName();
			
			menu.addItem(new MenuItemFactory<Classifier>() {
				@Override
				public AbstractMenuItemPanelV5<Classifier> getItem(String id) {
					return new MenuItemPanelV5<Classifier>(id) {
						@Override
						public void onClick() {
							onCreate(KbeeEntityRole.TYPE, getClassifiers().get(index.intValue()));
						}
						@Override
						public String getLabel() {
							return name;
						}
						@Override
						public String getTarget() {
							return "_blank";
						}
					};
				}
			});
		}
		
		
				
		add(menu);
	}


	protected abstract void onCreate(int type, IModel<Classifier> model);
	
	public List<IModel<Classifier>> list = null;
	
	public void onDetach() {
		super.onDetach();
		if (list!=null) {
			for (IModel<Classifier> m: list)
				m.detach();
		}
	}

	
	public List<IModel<Classifier>> getClassifiers() {
		
		if (list!=null)
			return list;
		
		list = new ArrayList<IModel<Classifier>>();
		
		for (Classifier classifier : getContentDao().getClassifiers(getDomain())) {
			if (classifier.getDataSet() instanceof EntitySet && classifier.getState()==ObjectState.ENABLED) {
					list.add(new ObjectModel<Classifier>(classifier));
			}
		}
		
		list.sort( new Comparator<IModel<Classifier>>() {
			@Override
			public int compare(IModel<Classifier> o1, IModel<Classifier> o2) {
				try	 {
					return o1.getObject().getName().compareToIgnoreCase(o2.getObject().getName());
				}
				catch (Exception e) {
					return 0;
				}
			}
		});
		
		return list;
	}

	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	private Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}

}
