package kbee.web.eform;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.form.EFormContentData;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.ResourceAdded;
import com.novamens.content.resource.KBFile;
import com.novamens.kbee.content.form.KbeeEResource;
import com.novamens.kbee.wicket.markup.html.console.panel.ViewMode;
import com.novamens.kbee.wicket.markup.html.event.CloseResourceVersionsPanelEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;

import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.DonwloadMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.model.ObjectModel;

import kbee.util.logging.Logger;
import kbee.web.page.InvisibleImage;
import kbee.web.resource.ResourceModel;
import kbee.web.resource.ResourceViewPanel;
import kbee.web.resource.ResourcesPanel;

@SuppressWarnings("serial")
public class EResourceStatelessViewer extends EFieldPanel<KbeeEResource> implements ResourcesPanel {
	private static final long serialVersionUID = 1L;

	static private Logger logger = Logger.getLogger(EResourceStatelessViewer.class.getName());

	private ViewMode viewmode = ViewMode.ICON;
	
	private IModel<Resource> resourceModel;
	private IModel<Content> contentModel;

	private WebMarkupContainer layout;

	public class ControlFragment extends Fragment {
		
		private  boolean fmt_helpvisible = false;
		
		public ControlFragment(String id) {
			super(id, "control-fragment", EResourceStatelessViewer.this);
			
			setOutputMarkupId(true);
			
			addOrReplaceView();
			
			Label s=new Label("subtitle", new Model<String>(getField().getSublabel()));
			s.setEscapeModelStrings(false);
			s.setVisible(getField().getSublabel()!=null);
			add(s);
			
			WebMarkupContainer menu = new WebMarkupContainer("menu-container") {
				public boolean isVisible() {
					return getResourceModel()!=null ;
				}
			};
			menu.setOutputMarkupId(true);
			menu.add(getMenu(getResourceModel()));
			add(menu);
			
			Label helplabel = new Label("help", () -> getHelpText()) {
				public boolean isVisible() {
					return fmt_helpvisible;
				}
			};
			helplabel.setVisible(false);
			AjaxLink<?> helplink = new AjaxLink<Void>("help-link") {
				public void onClick(AjaxRequestTarget target) {
					fmt_helpvisible = !fmt_helpvisible;
					target.add(getContainer());
				}
				public boolean isVisible() {
					return getHelpText()!=null;
				}
			};
			add(helplink);
			add(helplabel);
		}
		public void refresh(AjaxRequestTarget target) {
			addOrReplaceView();
			target.add(this);
		}
		private void addOrReplaceView() {
			WebMarkupContainer view = new WebMarkupContainer("resource-view");
			view.setOutputMarkupId(true);
			if (getResourceModel()!=null && getContentModel()!=null) {
				view.add(new ResourceViewPanel<Content>("view", getResourceModel(), getContentModel()) {
					@Override
					public ViewMode getViewMode() {
						return EResourceStatelessViewer.this.getViewMode();
					}
					@Override
					public boolean isShared() {
						return EResourceStatelessViewer.this.isShared();
					}
				});
			}
			else {
				view.add(new InvisibleImage("view"));				
			}
			view.add(new AttributeModifier("class", new Model<String>() {
				public String getObject() {
					return getViewMode().getListCss();
				}
			})); 
			addOrReplace(view);
		}
	}	
	
	/*** -----------------------------------------------------------
	 * 
	 * @param id
	 * @param field
	 * @param data
	 * 
	 */
	public EResourceStatelessViewer(String id, KbeeEResource field, IModel<EFormData> data) {
		super(id, field, data);
		setOutputMarkupId(true);
	}
	
	public ViewMode getViewMode() {
		return this.viewmode;
	}

	public void setViewMode(ViewMode mode) {
		this.viewmode = mode;
	}
	
	public void setContent(IModel<Content> model) {
		this.contentModel = model;
	}
	
	public void setContent(Content content) {
		this.contentModel = new ObjectModel<Content>(content);
	}
	
	public IModel<Content> getContentModel() {
		return contentModel;
	}
	
	public boolean isReadOnly() {
		return getField().isReadOnly() || !getData().getForm().isEnabled();
	}
	
	public IModel<Resource> getResourceModel() {
		return resourceModel;
	}
	
	public Resource getResource() {
		return resourceModel!=null ? resourceModel.getObject() : null;
	}
	
	@Override
	public void add(Resource resource) {
		setResourceModel(resource);
		getData().setData(getField(), getResourceModel());
		fireScanAll(new EAjaxFormEvent(null, getField(), getData()));
		setUpdatedField(new ResourceAdded(getData().getForm(), getLabel(), resource));
	}

	@Override
	public void addVersion(Resource resource, Resource version) {
		
	}
	
	@Override
	public void addListeners() {
		super.addListeners();

		add(new WicketEventListener<CloseResourceVersionsPanelEvent<?>>() {
			@Override
			public void onEvent(CloseResourceVersionsPanelEvent<?> event) {
				if ( event.getId().equals(getResourceModel().getObject().getId()) &&   EResourceStatelessViewer.this.getContainer().get("versions").isVisible()) {
					EResourceStatelessViewer.this.getContainer().addOrReplace(new InvisiblePanel("versions"));
					event.getRequestTarget().add( EResourceStatelessViewer.this.getContainer());
				}
			}
		});
	}
	
	public String getLabel() {
		return getField().getLabel()!=null ?
			getField().getLabel() :
			"";	
	}
		
	/** --------------------------------------------
	 * 
	 */
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setResourceModel();
		
		
	    viewmode = ViewMode.ICON; 
		
		
		getContainer().add(new InvisiblePanel("versions"));
		
		
		layout = new WebMarkupContainer("horizontal-layout");
		layout.setOutputMarkupId(true);
		getContainer().add(layout);

		if (getDisposition()==Disposition.HORIZONTAL) {
			layout.add(new ControlFragment("control"));
			getContainer().add(new InvisiblePanel("control"));
		}
		else {
			getContainer().add(new ControlFragment("control"));
			layout.add(new InvisiblePanel("control"));
			layout.setVisible(false);
		}
	
		getContainer().add(new Label("label", new Model<String>() {
			public String getObject() {
				return getField().getLabel()!=null ?
					getField().getLabel() :
					"";	
			}
		}));
		
		add(new WicketEventListener<EAjaxRefreshEvent>() {
			@Override
			public void onEvent(EAjaxRefreshEvent event) {
				if (handle(event)) {
					refresh(event.getRequestTarget());
				}
			}
			public boolean handle(EAjaxRefreshEvent event) {
				return EResourceStatelessViewer.this.getMarkupId().equals(event.getComponentId());
			}
		});
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		Component parent = getParent();
		while (parent!=null && !(parent instanceof EFormEditor)) {
			parent = parent.getParent();
		}
		if (parent!=null) {
			for (Behavior behavior : parent.getBehaviors()) {
				behavior.beforeRender(this);
			}
		}	
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (contentModel!=null)
			contentModel.detach();
		if (resourceModel!=null)
			resourceModel.detach();
	}
	
	protected boolean isShared() {
		return false;
	}
	
	protected void setValues(List<?> values) {
		this.resourceModel = null;
		for (Object object : values) {
			if (object instanceof Resource) {
				setResourceModel((Resource)object);
				break;
			}
		}
	}
	
	protected void setResourceModel(Resource resource) {
		this.resourceModel = new ResourceModel(resource);
	}
	
	protected void setResourceModel() {
		Resource resource = (Resource)getData().getData(getField());
		setContent(((EFormContentData)getData()).getContent());
		if (resource!=null) {
			this.resourceModel = new ResourceModel(resource);
 		}
	}
	
	protected Panel getMenu(IModel<Resource> model) {
		
		ContextMenuPanel<Void> menu = new ContextMenuPanel<Void>(null);
		
		menu.addItem(id ->
			new AjaxMenuItemPanelV5<Void>(id) {
				@Override
				public void onClick(AjaxRequestTarget target) {
					setViewMode(ViewMode.ICON);
					refresh(target);
				}	
				@Override
				public boolean isEnabled() {
					return getViewMode()!= ViewMode.ICON;
				}
				@Override
				public String getLabel() {	
					return getLabelString("icon");
				}
		});
	
		menu.addItem(id ->
			new AjaxMenuItemPanelV5<Void>(id) {
				@Override
				public void onClick(AjaxRequestTarget target) {
					setViewMode(ViewMode.THUMBNAIL);
					refresh(target);
				}
				@Override
				public boolean isEnabled() {
					return getViewMode()!= ViewMode.THUMBNAIL;
				}
				@Override
				public String getLabel() {	
					return getLabelString("thumbnail");
				}
		});
	
		menu.addItem(id ->
			new AjaxMenuItemPanelV5<Void>(id) {
				@Override
				public void onClick(AjaxRequestTarget target) {
					setViewMode(ViewMode.THUMBNAIL_LARGE);
					refresh(target);
				}
				@Override
				public boolean isEnabled() {
					return getViewMode()!= ViewMode.THUMBNAIL_LARGE;
				}
				@Override
				public String getLabel() {	
					return getLabelString("image");
				}
		});
		
		menu.addItem(id ->
			new SeparatorMenuItemPanelV5<Void>(id) {
				@Override
				public String getCssClass() {
					return "divider";
				}
				
		});
		
		menu.addItem(id ->
			new AjaxMenuItemPanelV5<Void>(id) {
				@Override
				public void onClick(AjaxRequestTarget target) {
					if (EResourceStatelessViewer.this.getContainer().get("versions").isVisible()) {
						EResourceStatelessViewer.this.getContainer().addOrReplace(new InvisiblePanel("versions"));
					}
					else {
						EResourceStatelessViewer.this.getContainer().addOrReplace(new EResourceVersionsPanel("versions", getResourceModel()));	
					}
					target.add(EResourceStatelessViewer.this.getContainer());
				}
				@Override
				public String getLabel() {	
					return EResourceStatelessViewer.this.getLabel("menu.showversions").getObject();
				}
				@Override
				public boolean isVisible() {
					return true;
				}
				@Override
				public boolean isEnabled() {
					return getResource().getPreviousVersion()!=null;
				}
			});
	
		menu.addItem(id ->
			new DonwloadMenuItemPanelV5<Void>(id) {
				@Override 
				public String getLabel() {
					return  EResourceStatelessViewer.this.getLabel("menu.download").getObject();
				}
				@Override
				protected File getFile() {
					if (getResource() instanceof KBFile) {
						File file;
						try {
							file = ((KBFile) getResource()).getFile();
							if (file==null) 
								logger.error("file is null " + ((KBFile) getResource()).getName());
							return file;
						} 
						catch (IOException e) {
							logger.error(e);
						}
					}
					return null;
				}
				@Override
				public boolean isVisible() {
					return (getResource() instanceof KBFile);
				}
				@Override
				public boolean isEnabled()  {
					return true;
				}
			});
		
		return menu;
	}
	
	protected EFormEditor getEditor(Component component) {
		Component editor = component.getParent();
		while (editor!=null && !(editor instanceof EFormEditor)) {
			editor = editor.getParent();
		}
		return (EFormEditor)editor;
	}
	
	protected void refresh(AjaxRequestTarget target) {
		getControl().refresh(target);
	}
	
	private ControlFragment getControl() {
		return (ControlFragment)this.get("container:control");
	}
}