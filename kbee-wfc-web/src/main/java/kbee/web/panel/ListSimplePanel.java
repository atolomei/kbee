package kbee.web.panel;

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.Identifiable;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.panel.KBPanel;


/**
 * 
 * <p>
 * String Title  html
 * String title-meta html 
 * String subtitle html
 * String description html
 * </p>
 * 
 * Link on-click
 * 
 * Menu
 * 
 * @param <T>
 */
@SuppressWarnings("serial")
public class ListSimplePanel<T> extends KBPanel {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ListSimplePanel.class.getName());

	private static final long serialVersionUID = 1L;
	
	private List<IModel<T>> list;
	private IModel<String> title;

	private ListView<IModel<T>> lii;
	
	private boolean isSizeVisible = true;
	private boolean all_expanded = false;
	
	private IModel<String> abstract_str;

	private int start_index = -1;
	private int expanded_index = -1;
	
	private String key;
	
	private boolean isMenu = false;
	private boolean isExpand = false;
	private boolean isicon = false;

	private WebMarkupContainer li_c, bottom;
	private WebMarkupContainer toolbar;
	private WebMarkupContainer expandedContainer;
	
	/** ---------------------------------
	 *
	 */
	public class ElementRowFragment extends Fragment {
		
		private static final long serialVersionUID = 1L;
		
		private IModel<T> model;
		private int index;

		WebMarkupContainer mc;
		Panel menu;


		public ElementRowFragment(String id, IModel<T> model, int index) {
			super(id,"element-row-fragment", ListSimplePanel.this);
			this.model=model;
			this.index=index;
			
			setOutputMarkupId(true);
		}
		
		public IModel<T> getModel() {
			return model;
		}
		
		public T getModelObject() {
			return model.getObject();
		}
		
		public boolean isExpanded() {
			return start_index==index || getExpandedIndex()==this.index;
		}


		@Override
		public void onInitialize() {
			super.onInitialize();

			// Menu ----------------------------------------------------------------------
			//
			//
			mc = new WebMarkupContainer("menu-container") {
				private static final long serialVersionUID = 1L;
				public boolean isVisible() {
					return isMenu();
				}
			};

			menu = getMenu(getModel(), index);
			 
			mc.add(menu);
			mc.setOutputMarkupId(true);
			add(mc);
			
			// Expand ----------------------------------------------------------------------
			//
			//
			WebMarkupContainer e_c = new WebMarkupContainer("expand-container");
			
			e_c.setVisible(ListSimplePanel.this.isExpand() && !isAllExpanded());
			add(e_c);
			
			AjaxLink<Void> al=new AjaxLink<Void>("expand") {
				private static final long serialVersionUID = 1L;
				@Override
				public void onClick(AjaxRequestTarget target) {
					clickExpander(ElementRowFragment.this.index);
					ListSimplePanel.this.onClickExpand(target, index);
				}
				
				public boolean isEnabled() {
					return ListSimplePanel.this.isExpanderEnabled(model);
				}
			};
			
			e_c.add(al);

			WebMarkupContainer ex_i = new WebMarkupContainer("expand-icon");
			ex_i.add( new AttributeModifier("class", new Model<String>() 
			{
				private static final long serialVersionUID = 1L;
				public String getObject() {
					return  isExpanded() ? "far fa-angle-down panel-centered" : "far fa-angle-up panel-centered";
				}
			}));
			al.add(ex_i);
			
			// Icon ----------------------------------------------------------------------
			//
			WebMarkupContainer i_c = new WebMarkupContainer("icon-container");
			add(i_c);
			
			i_c.setVisible(isIcon());
			WebMarkupContainer unread = new WebMarkupContainer("unread-icon");
			i_c.add(unread);
			IModel<String> s=getIconCss(getModel());
			unread.add(new AttributeModifier("class",s));
			unread.add(new AttributeModifier("title", getIconCssTitle(getModel())));
			
			
			
			WebMarkupContainer lt = getItemMainPanel("item-link", getModel(), index, isExpanded() || isAllExpanded());
			add(lt);
			
			 expandedContainer = new WebMarkupContainer("expanded-container");
			 add(expandedContainer);
			 
			if ( isExpanded() ||  isAllExpanded()) {
				WebMarkupContainer panel = getExpandedPanel("expanded-row-container", model);
				panel.setVisible(true);
				expandedContainer.add(panel);
			}
			else {
				expandedContainer.setVisible(false);
				expandedContainer.add(new InvisiblePanel("expanded-row-container"));	
			}
		}
	};
	
	/** ---------------------------------
	 *
	 */
	public class SimpleExpandedRowFragment extends Fragment {
		
		private static final long serialVersionUID = 1L;
		
		private IModel<T> model;
		
		public SimpleExpandedRowFragment(String id, IModel<T> model) {
			super(id,"simple-expanded-row-fragment", ListSimplePanel.this);
			this.model=model;
			setOutputMarkupId(true);
		}
		
		public IModel<T> getModel() {
			return model;
		}
		
		public T getModelObject() {
			return model.getObject();
		}
		
		@Override
		public void onBeforeRender() {
			super.onBeforeRender();
		 
		}

		@Override
		public void onInitialize() {
			super.onInitialize();
			
			setOutputMarkupId(true);
			
			WebMarkupContainer padder = new WebMarkupContainer("padder");
			StringBuilder str = new StringBuilder();
			str.append("width:100%; padding-left:" + getPadder() +"px;");
					
			padder.add(new AttributeModifier("style", str.toString()));
			add(padder);
			
			WebMarkupContainer abs_c = new WebMarkupContainer("item-abstract-container");
			padder.add(abs_c);
			
			IModel<String> ma=getItemAbstract(getModel());
			Label la=new Label("item-abstract", ma);
			la.setEscapeModelStrings(false);
			abs_c.add(la);
			
		}
	}
	
	
	/** ----------------------------------------------------------
	 * @param id
	 * @param list
	 * 
	 * 
	 */
	public ListSimplePanel(String id, String key, List<IModel<T>> list) {
		super(id);
		this.list=list;
		this.key=key;
		setOutputMarkupId(true);
		
		if (this.key!=null && this.isExpand())
			this.start_index=getIntUserPreference("expanded-index", -1);
			 		
		if (this.start_index>-1)
			this.expanded_index = this.start_index;
		
		this.start_index = -1;
	}
	

	public  void setTitle(IModel<String> title) 	{this.title=title;}
	protected IModel<String> getTitle()				{return this.title;}
	public void setAbstract(IModel<String> str) 	{this.abstract_str=str;}
	protected IModel<String> getAbstract() 			{return this.abstract_str;}
	protected String getTitleMeta() 				{return null;}
	public boolean isAllExpanded() 					{return this.all_expanded;}
	public void setAllExpanded(boolean b) 			{this.all_expanded=b;}
	public boolean isSizeVisible() 					{return this.isSizeVisible;}
	
	public void onAfterRender() 					{super.onAfterRender(); start_index = -1;}
	

	public void setBottomPanel(WebMarkupContainer p) {
		if (!p.getId().equals("bottom"))
			throw new IllegalArgumentException( this.getClass().getName() + " -> id must be 'bottom'");
		if (this.bottom!=null) {
			this.bottom=p;
			addOrReplace(this.bottom);
		}
		else
			this.bottom=p;
	}

				
	public void setToolbarPanel(WebMarkupContainer p) {
		if (!p.getId().equals("toolbar"))
			throw new IllegalArgumentException( this.getClass().getName() + " -> id must be 'toolbar'");
		if (this.toolbar!=null) {
			this.toolbar=p;
			addOrReplace(this.toolbar);
		}
		else
			this.toolbar=p;
	}

	
	// columns to Render
	//
	public boolean isExpand() 				{return isExpand;}
	public void setExpand(boolean isExpand) {this.isExpand = isExpand;}
	public void setMenu(boolean b) 			{this.isMenu= b;	}
	public boolean isMenu() 				{return this.isMenu;}
	public void setIcon(boolean b) 			{this.isicon= b;}
	public boolean isIcon() 				{return this.isicon;}
	
	
	protected IModel<String> getLabelContainerCss() {return new Model<String>("label-container");}
	protected boolean isExpanderEnabled(IModel<T> model) {return true;}
	protected void onClickExpand(AjaxRequestTarget target, int index) {target.add(ListSimplePanel.this.li_c);}
	
	public IModel<String> getIconCss(IModel<T> model) {return null;}
	public IModel<String> getIconCssTitle(IModel<T> model) {return getLabel("unread");}
	
	
	public List<IModel<T>> getItems() {return list;}
	public void setItems(List<IModel<T>> items) {list=items;}

	@Override
	public void onDetach() {
		super.onDetach();
		if (list!=null) {
			list.forEach(item->item.detach());
		}
	}
	
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		if (bottom==null)
			bottom = new InvisiblePanel("bottom");
		
		
		if (toolbar==null)
			toolbar = new InvisiblePanel("toolbar");
		
		addOrReplace(toolbar);
		addOrReplace(bottom);
		
		li_c = new WebMarkupContainer("list-items-container");
		li_c.add(new AttributeModifier("class", getListContainerCss() + (isAllExpanded() ? " all-expanded" :"") ));
		li_c.setOutputMarkupId(true);
		add(li_c);
		
		// ----------------
		//
		Label totlabel=new Label("title", getTitle()) {
			private static final long serialVersionUID = 1L;
			public boolean isVisible() {
				return getTitle()!=null;
			}
		};
		
		Label tot=new Label("total", getTitleMeta()) {
			private static final long serialVersionUID = 1L;
			public boolean isVisible() {
				return getTitle()!=null && getTitleMeta()!=null;
			}
		};
		
		WebMarkupContainer tc = new WebMarkupContainer("title-container") {
			private static final long serialVersionUID = 1L;
			public boolean isVisible() {
				
				return getTitle()!=null || getTitleMeta()!=null;
			}
		};
		add(tc);
		tc.add(totlabel);
		tc.add(tot);
		
		
		lii = new ListView<IModel<T>>("list-items", new PropertyModel<List<IModel<T>>>(this, "items")) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(ListItem<IModel<T>> item) {
				item.add(getRowPanel("row-element", item.getModelObject(), item.getIndex()));
				item.setOutputMarkupId(true);
			}
		};

		lii.setOutputMarkupId(true);
		li_c.add(lii);
	}

	
	
	
	
	protected String getListContainerCss() {
		return " onecol";
	}


	protected int getExpandedIndex() {
		return this.expanded_index;
	}
	
	protected void clickExpander(int index) {
		
		if (expanded_index==index) 
			expanded_index=-1;
		else
			expanded_index=index;
		
		if (ListSimplePanel.this.key!=null) {
			setIntUserPreference("expanded-index", expanded_index);
		}
		
	}


	protected Panel getMenu(IModel<T> model, int index) {return new InvisiblePanel("menu");}
	
	
	protected void onClick(IModel<T> modelObject, int index) {
		fire (new ClickItemEvent<T>(modelObject, index));
	}


	
	
	protected WebMarkupContainer getRowPanel		(String id, IModel<T> model, int index) {
		return new ElementRowFragment(id, model, index);
	}
	
	
	protected WebMarkupContainer getExpandedPanel (String id, IModel<T> model) {
		return new SimpleExpandedRowFragment(id, model);
	}

	
	
	public PopupSettings getPopupSettings() {
		return null;
	}
	
	
	protected WebMarkupContainer getItemTags(IModel<T> modelObject) {
		return null;
	}
	
	protected WebMarkupContainer getMoreInfoPanel(IModel<T> modelObject) {
		return null;
	}
	
	public WebMarkupContainer getItemMainPanel(String id, IModel<T> model, int index, boolean is_expanded) {
		ListSimpleItemMainPanel<T> ls = new ListSimpleItemMainPanel<T>(id, model, index, is_expanded) {
			protected void onClick() {
				ListSimplePanel.this.onClick(getModel(), index);				
			}
			@Override
			protected WebMarkupContainer getItemTags(IModel<T> modelObject) {
				return  ListSimplePanel.this.getItemTags(modelObject);
			}
			protected WebMarkupContainer getMoreInfoPanel(IModel<T> modelObject) {
				return  ListSimplePanel.this.getMoreInfoPanel(modelObject);
			}
			protected IModel<String> getItemLabel(IModel<T> modelObject) {
				return ListSimplePanel.this.getItemLabel(modelObject);
			}
			protected IModel<String> getLabelContainerCss() {
				return ListSimplePanel.this.getLabelContainerCss();
			}
			protected IModel<String> getItemLabelMeta(IModel<T> modelObject) {
				return ListSimplePanel.this.getItemLabelMeta(modelObject);
			}
			public PopupSettings getPopupSettings() {
				return ListSimplePanel.this.getPopupSettings();
			}
		};		
		ls.add( new AttributeModifier("style", "float:left; width: calc( 100% - "+String.valueOf(getPadder())+"px )"));
		return ls;
	}

	protected IModel<String> getItemLabel(IModel<T> modelObject) {
		
		try {
			if (modelObject.getObject()==null)
						return new Model<String>("null");
			
			if (modelObject.getObject() instanceof Identifiable) {
				return new Model<String>(((Identifiable) modelObject.getObject()).getDisplayName());
			}
			return new Model<String>(modelObject.getObject().toString());
		
		} catch (Exception e) {
			logger.error(e);
			return new Model<String>(e.getClass().getSimpleName());
		}
		
				
	}
	protected IModel<String> getItemAbstract(IModel<T> modelObject) {return null;}
	protected IModel<String> getItemLabelMeta(IModel<T> modelObject) {return null;}



	protected String getUserPreference(String key) {
		KbeeUser user = getSessionUser();
		if (user!=null)
			return user.getService(PreferencesService.class).getValue(getKey(), key);
		return null;
	}
	
	protected String getUserPreference(String key, String defaultValue) {
		KbeeUser user = getSessionUser();
		if (user!=null)
			return user.getService(PreferencesService.class).getValue(getKey(), key, defaultValue);
		return null;
	}

	protected int getIntUserPreference(String key, int defaultvalue) {
		KbeeUser user = getSessionUser();
		if (user!=null)
			return user.getService(PreferencesService.class).getIntValue(getKey(), key, defaultvalue);
		return 0;
	}

		
	protected KbeeUser getSessionUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	public String getKey() {
		return this.key+"/"+this.getClass().getSimpleName();
	}
	
	protected void setUserPreference(String key, String value) {
		KbeeUser user = getSessionUser();
		if (user==null) 
			return;
		String val =user.getService(PreferencesService.class).getValue(getKey(), key);
		if (val==null || value==null || !val.equals(value))
			user.getService(PreferencesService.class).setValue(getKey(), key, value);
	}

	protected void setIntUserPreference(String key, int value) {
		KbeeUser user = getSessionUser();
		if (user==null) 
			return;
		int val =user.getService(PreferencesService.class).getIntValue(getKey(), key);
		if (val==-1 || val!=value) {
			user.getService(PreferencesService.class).setIntValue(getKey(), key, value);
		}
	}

	private String getPadder() {
		
		int pa=0;
		
		if (isMenu())
			pa+=42;

		if (isExpand())
			pa+=32;
		
		if (isIcon())
			pa+=42;
		
		return String.valueOf(pa);
	}



}


