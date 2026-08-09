package kbee.web.nav;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;

import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;


import com.novamens.wicket.markup.html.actions.AbstractMenuItemFactory;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.HREFMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.HeaderMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemWithModelPanel;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.panel.BCTitleElementBC;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.wicket.util.AjaxBCElement;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.HREFBCElement;
import com.novamens.wicket.util.IBCElement;

/**
 * NOTE. It does not work to add elements in onIntialize()
 * we must add elements in constructor
 */
@SuppressWarnings("serial")
public class DropDownMenuBC<T> extends KBPanel {
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DropDownMenuBC.class.getName());
	
	private IModel<T> model;
	
	private IModel<String> tmodel;
	
	private List<IModel<IBCElement>> list;
	
	private int index_selected = 0;
	private boolean is_open =false;
	
	public DropDownMenuBC(IModel<T> model) {
		this("bc-menu-item", model);
	}
	
	public DropDownMenuBC(String id) {
		this (id, null);
	}

	public DropDownMenuBC() {
		this ("bc-menu-item", null);
	}
	
	public DropDownMenuBC(String id, IModel<T> model) {
		super(id);
		this.model=model;
		setOutputMarkupId(true);
		this.list=new ArrayList<IModel<IBCElement>>();
	}
	
	public IModel<T> getModel() {
		return this.model;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		
		try {
			
			if (this.model!=null)
				this.model.detach();
			
			if (this.list!=null) {
	
				for (IModel<IBCElement> c: this.list) {
					c.detach();
					if (c.getObject() instanceof IDetachable)
						((IDetachable) c.getObject()).detach();
				}
			}
		} 
		catch (Exception e) {
			logger.error(e);
			throw(e);
		}

	}
	
	public void setTitle(IModel<String> tm) {
		this.tmodel=tm;
	}
	
	public IModel<String> getTitle() {
		return tmodel;
	}
	
	public void addElement(IBCElement e, boolean selected) {
		this.list.add(new Model<IBCElement>(e));
		if (selected)
			this.index_selected=list.size()-1;
	}
	
	public void addElement(IBCElement e) {
		this.list.add(new Model<IBCElement>(e));
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();

		ContextMenuPanel<IBCElement> menu = new ContextMenuPanel<IBCElement>(null);
		add(menu);
		
		int index = 0;
		for (IModel<IBCElement> ibc_model: this.list) {
		
			if (index++!=this.index_selected) {
				if (ibc_model.getObject() instanceof SeparatorBC) {
					menu.addItem(new MenuItemFactory<IBCElement>() {
						@Override
						public AbstractMenuItemPanelV5<IBCElement> getItem(String id) {
							return new SeparatorMenuItemPanelV5<IBCElement>(id) {
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
				}
				else if (ibc_model.getObject() instanceof AjaxBCElement) {
					menu.addItem(new AbstractMenuItemFactory<IBCElement>(ibc_model) {
						
						private static final long serialVersionUID = 1L;
						@Override
						public AbstractMenuItemPanelV5<IBCElement> getItem(String id) {
							
							logger.debug(getFactoryModel().getObject().toString());
							
							AjaxMenuItemPanelV5<IBCElement> aj = new AjaxMenuItemPanelV5<IBCElement>(id, getFactoryModel(), null) {
								
								private static final long serialVersionUID = 1L;
								@SuppressWarnings("unchecked")
								@Override
								public void onClick(AjaxRequestTarget target) {
									((AjaxBCElement<T>) getModel().getObject()).onClick(target);
								}
								
								@Override
								public String getLabel() {
										return getModelSec().getObject().getLabel().getObject();
									
									
									
								}
							};
							
							aj.setModel(ibc_model);
							return aj;
					}});
				}
				else if (ibc_model.getObject() instanceof  BCTitleElementBC) {
					
					final String sss=ibc_model.getObject().getLabel().getObject(); 
					
					menu.addItem(new AbstractMenuItemFactory<IBCElement>(ibc_model) {
						public AbstractMenuItemPanelV5<IBCElement> getItem(String id) {
							return new  HeaderMenuItemPanelV5<IBCElement>(id) {
								@Override
								public String getLabel() {
									return sss;
								}
							};
							
						}
					});
				}
				
				else if (ibc_model.getObject() instanceof HREFBCElement) {
					menu.addItem(new AbstractMenuItemFactory<IBCElement>(ibc_model) {
						public AbstractMenuItemPanelV5<IBCElement> getItem(String id) {
							// only works with the parameters in constrctor
							return new HREFMenuItemPanelV5<IBCElement>(id, ((HREFBCElement) ibc_model.getObject()).getUrl(), ibc_model.getObject().getLabel());
						}
					});
				}
				
				else {
					menu.addItem(new AbstractMenuItemFactory<IBCElement>(ibc_model) {
						@Override
						public AbstractMenuItemPanelV5<IBCElement> getItem(String id) {
							return new MenuItemWithModelPanel<IBCElement>(id, getModel()) {
								@Override
								public void onClick() {
									((BCElement) getModel().getObject()).onClick();
								}
								@Override
								public String getLabel() {
									if (getModel().getObject().getLabel()!=null)
										return getModel().getObject().getLabel().getObject();
									return getModel().getObject().getClass().getSimpleName();
								}
								@Override
								public String getBeforeClick() {
									return null;
								}
								
								@Override
								public String getTarget() {
									return getModel().getObject().isNewTab()?"_blank":null;
								}
							};
					}});
				}
			}
		}
										
		Link<Void> link = new Link<Void>("title-link") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick() {
				// TODO Auto-generated method stub
			}
		};
		
		Label ti = new Label("title", new Model<String>() {
			private static final long serialVersionUID = 1L;
			public String getObject() {
				if(getSelected()!=null)
					return getSelected().getLabel().getObject();
				return "";
			}
		});
		
		ti.setEscapeModelStrings(false);
		link.add(ti);
		add(link);
	}
	
	protected void setExpanded(boolean b) {
		this.is_open=b;
	}

	protected boolean isExpanded() {
		return this.is_open;
	}

	public IBCElement getSelected() {
		if (list.isEmpty())
			return null;
		return list.get(index_selected).getObject();
	}
}