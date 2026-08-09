package kbee.web.portal6.editor;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.portal.model.KbeeBlock;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal.service.SiteFactoryService;
import com.novamens.portal6.model.Area;
import com.novamens.portal6.model.AreaSection;
import com.novamens.portal6.model.AreaType;
import com.novamens.portal6.model.Block;
import com.novamens.portal6.model.Page;
import com.novamens.portal6.model.PageSection;
import com.novamens.portal6.model.PageSectionDisposition;
import com.novamens.portal6.model.PageSectionType;
import com.novamens.portal6.model.PortalObject;
import com.novamens.portal6.model.PortalViewMode;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.SiteService;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.DummyBlockPanel;

import kbee.web.error.ApplicationErrorPage;

import kbee.web.event.wicket.ErrorEvent;
import kbee.web.portal6.IPortalWebPanel;
import kbee.web.portal6.PortalObjectDataProviderService;
import kbee.web.portal6.PortalObjectViewerRenderService;
import kbee.web.portal6.event.PortalAddAjaxEvent;
import kbee.web.portal6.event.PortalAjaxEvent;
import kbee.web.portal6.event.PortalAjaxShowPayloadEvent;
import kbee.web.portal6.event.PortalAjaxStructureShowControllerEvent;
import kbee.web.portal6.event.PortalAjaxStructureShowDeletedEvent;
import kbee.web.portal6.event.PortalAjaxStructureShowArchivedEvent;
import kbee.web.portal6.event.PortalAjaxStructureShowHierarchyEvent;
import kbee.web.portal6.event.PortalArchiveAjaxEvent;
import kbee.web.portal6.event.PortalDeleteAjaxEvent;
import kbee.web.portal6.event.PortalEditAjaxEvent;
import kbee.web.portal6.event.PortalMoveDownAjaxEvent;
import kbee.web.portal6.event.PortalMoveUpAjaxEvent;
import kbee.web.portal6.event.PortalRestoreAjaxEvent;
import kbee.web.portal6.panel.PortalErrorPanel;
import kbee.web.portal6.panel.PortalIWebPanel;
import kbee.web.portal6.panel.PortalPanel;
import kbee.web.searcher.page.SearcherResultsPage;
import net.bytebuddy.asm.Advice.This;

public class PortalPageStructurePanel extends PortalPanel<Page> {
			
	private static final long serialVersionUID = 1L;

	static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PortalPageStructurePanel.class.getName());

	//private static final ResourceReference KBEE_SEARCHER_CSS = new CssResourceReference(SearcherResultsPage.class, "searcher.css");
	
	private WebMarkupContainer main_panel_contaniner;
	private WebMarkupContainer editor_panel_contaniner;

	private Panel error_panel;
	private Panel add_panel;

	private Boolean is_editor_visible = Boolean.valueOf(true);
	 
	
	private Serializable selected_object = null;
	
	//int viewMode;
	
	private int showHierarchy;
	private int showArchived;
	private int showDeleted;
	private int showController;
	private int pin_editor = PortalAjaxEvent.PIN_EDITOR_YES;
	
	private int show_payload = PortalAjaxEvent.SHOW_PAYLOAD_NO;
	
	private int portal_editor_disposition = PortalAjaxEvent.EDITOR_DISPOSITION_LEFT;
	
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		//response.render(CssHeaderItem.forReference(KBEE_SEARCHER_CSS));
	}

	
	
	public PortalPageStructurePanel(String id, IModel<Page> model) {
		super(id, model);
		setParameters(new  HashMap<String, String> ());
		
		this.show_payload 				= getIntPreference(PortalAjaxEvent.PAYLOAD_VISIBLE,		PortalAjaxEvent.SHOW_PAYLOAD_NO); 		    	getParameters().put(PortalAjaxEvent.PAYLOAD_VISIBLE, String.valueOf(this.show_payload));
		this.showHierarchy 				= getIntPreference(PortalAjaxEvent.STRUCTURE_VIEW,		PortalAjaxEvent.VIEW_HIERARCHY_NO); 		    getParameters().put(PortalAjaxEvent.STRUCTURE_VIEW, String.valueOf(this.showHierarchy));
		this.showArchived 				= getIntPreference(PortalAjaxEvent.ARCHIVED_VISIBLE,	PortalAjaxEvent.SHOW_ARCHIVED_NO); 			    getParameters().put(PortalAjaxEvent.ARCHIVED_VISIBLE, String.valueOf(this.showArchived));
		this.showController				= getIntPreference(PortalAjaxEvent.CONTROLLER_VISIBLE, 	PortalAjaxEvent.SHOW_CONTROLLER_YES);			getParameters().put(PortalAjaxEvent.CONTROLLER_VISIBLE, String.valueOf(this.showController));
		this.showDeleted				= getIntPreference(PortalAjaxEvent.DELETED_VISIBLE, 	PortalAjaxEvent.SHOW_DELETED_YES);			    getParameters().put(PortalAjaxEvent.DELETED_VISIBLE, String.valueOf(this.showDeleted));
		this.pin_editor  				= getIntPreference(PortalAjaxEvent.PIN_EDITOR, 			PortalAjaxEvent.PIN_EDITOR_YES); 			    getParameters().put(PortalAjaxEvent.PIN_EDITOR, String.valueOf(this.pin_editor));
		this.portal_editor_disposition  = getIntPreference(PortalAjaxEvent.EDITOR_DISPOSITION, 	PortalAjaxEvent.EDITOR_DISPOSITION_LEFT); 		getParameters().put(PortalAjaxEvent.EDITOR_DISPOSITION, String.valueOf(this.portal_editor_disposition));
	}
	

	

	protected void reload() {
		this.selected_object=null;
		this.onBeforeRender();
	}
	

	public void onBeforeRender() {
		super.onBeforeRender();

		long start = System.currentTimeMillis();
		
		if (selected_object!=null) {
			
			Iterator<Component> it =this.iterator();
			while (it.hasNext()) {
				Component c = it.next();
				if (c instanceof WebMarkupContainer) {
					 boolean done=walk((WebMarkupContainer) c);
					 if (done)
						 break;
				}
			}
		
			is_editor_visible = Boolean.valueOf(true);
			editor_panel_contaniner.setVisible(isEditorVisible());
			
			main_panel_contaniner.add( new AttributeModifier("class", "col-md-8 col-lg-9 col-xs-12 main-panel " + (isEditorLeft() ? " fright": " fleft")));
			editor_panel_contaniner.add( new AttributeModifier("class", "col-md-4 col-lg-3 col-xs-12 editor-panel " + (isEditorLeft() ? " fleft": " fright")));
	
		}
		else {
	
			is_editor_visible = Boolean.valueOf(false);
			editor_panel_contaniner.setVisible(isEditorVisible());
			
			if( pin_editor == PortalAjaxEvent.PIN_EDITOR_YES) {
				main_panel_contaniner.add( new AttributeModifier("class", "col-md-8 col-lg-9 col-xs-12 main-panel " + (isEditorLeft() ? " fright": " fleft")));
				editor_panel_contaniner.add( new AttributeModifier("class", "col-md-4 col-lg-3 col-xs-12 editor-panel " + (isEditorLeft() ? " fleft": " fright")));
				editor_panel_contaniner.addOrReplace(new InvisiblePanel("editor-panel"));	
			}
			else {
				main_panel_contaniner.add( new AttributeModifier("class", "col-md-12 col-lg-12 col-xs-12 main-panel"));
				editor_panel_contaniner.addOrReplace(new InvisiblePanel("editor-panel"));
			}
		}
		
		
		
		long end = System.currentTimeMillis();
		logger.debug("Total render -> " + String.valueOf(end-start)+" ms");
	}
	
	/***
	 * 
	 * 
	 * 
	 * 
	 */
	@Override
	public void addListeners() {
		super.addListeners();

		// -------------------------
		// open "Add" Editor
		// -------------------------
		add(new WicketEventListener<PortalAjaxStructureAddEvent<PortalObject>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(PortalAjaxStructureAddEvent<PortalObject> event) {
				try {
					
					main_panel_contaniner.setVisible(!main_panel_contaniner.isVisible());
					editor_panel_contaniner.setVisible(!editor_panel_contaniner.isVisible());
					
					event.getRequestTarget().add(PortalPageStructurePanel.this);
					
				} catch (Exception e) {
					logger.error(e);
					fire( new ErrorEvent<PortalObject>(event.getRequestTarget(), event.getModel(), e));
				}
			}
		});

		

		
		
		// -------------------------
		// Pin Editor
		// -------------------------
		add(new WicketEventListener<PortalAjaxPinEditorEvent<PortalObject>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(PortalAjaxPinEditorEvent<PortalObject> event) {
				try {
					
					// main_panel_contaniner.setVisible(!main_panel_contaniner.isVisible());
					// editor_panel_contaniner.setVisible(!editor_panel_contaniner.isVisible());	
						
					pin_editor = event.getPinEditor(); 
					event.getRequestTarget().add(PortalPageStructurePanel.this);
					
					
				} catch (Exception e) {
					logger.error(e);
					fire( new ErrorEvent<PortalObject>(event.getRequestTarget(), event.getModel(), e));
				}
			}
		});


		
		
		
		// -------------------------
		// show payload
		// -------------------------
		add(new WicketEventListener<PortalAjaxShowPayloadEvent<PortalObject>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(PortalAjaxShowPayloadEvent<PortalObject> event) {
				try {
					
					// main_panel_contaniner.setVisible(!main_panel_contaniner.isVisible());
					// editor_panel_contaniner.setVisible(!editor_panel_contaniner.isVisible());	
						
					setShowPayload(event.getShowPayloadMode());
					event.getRequestTarget().add(PortalPageStructurePanel.this);
					
					
				} catch (Exception e) {
					logger.error(e);
					fire( new ErrorEvent<PortalObject>(event.getRequestTarget(), event.getModel(), e));
				}
			}
		});

		
		
		
		
		// Show Controller
		//
	 	add(new WicketEventListener<PortalAjaxStructureShowControllerEvent<PortalObject>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(PortalAjaxStructureShowControllerEvent<PortalObject> event) {
				try {
					setShowController(event.getEditMode());
					//getParameters().put("reload", "yes");
					//setAllParameters();
					
					event.getRequestTarget().add(PortalPageStructurePanel.this);
				} catch (Exception e) {
					logger.error(e);
					fire( new ErrorEvent<PortalObject>(event.getRequestTarget(), event.getModel(), e));
				}
			}	
		});

		
	 	
		// Show Hierarchy
		//
		add(new WicketEventListener<PortalAjaxStructureShowHierarchyEvent<PortalObject>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(PortalAjaxStructureShowHierarchyEvent<PortalObject> event) {
				try {
					setShowHierarchy(event.getShowHierarchy());
					setAllParameters();
					event.getRequestTarget().add(PortalPageStructurePanel.this);
				} catch (Exception e) {
					logger.error(e);
					fire( new ErrorEvent<PortalObject>(event.getRequestTarget(), event.getModel(), e));
				}
			}
		});


		
		
		
		// Show Archived
		//
	 	add(new WicketEventListener<PortalAjaxStructureShowArchivedEvent<PortalObject>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(PortalAjaxStructureShowArchivedEvent<PortalObject> event) {
				try {
					setShowArchived(event.getShowArchived());
					//getParameters().put("reload", "yes");
					//setAllParameters();
					load();
					event.getRequestTarget().add(PortalPageStructurePanel.this);
					
				} catch (Exception e) {
					logger.error(e);
					fire( new ErrorEvent<PortalObject>(event.getRequestTarget(), event.getModel(), e));
					// setResponsePage(new ErrorPage<PortalObject>(e));
				}
			}
		});

	 	
		// Show Deleted
		//
	 	add(new WicketEventListener<PortalAjaxStructureShowDeletedEvent<PortalObject>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(PortalAjaxStructureShowDeletedEvent<PortalObject> event) {
				try {
					setShowDeleted(event.getShowDeleted());
					//getParameters().put("reload", "yes");
					setAllParameters();
					load();
					event.getRequestTarget().add(PortalPageStructurePanel.this);
				} catch (Exception e) {
					logger.error(e);
					fire( new ErrorEvent<PortalObject>(event.getRequestTarget(), event.getModel(), e));
				}
			}
		});
	 	
		// Close
		//
	 	add(new WicketEventListener<PortalCloseEditAjaxEvent<PortalObject>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(PortalCloseEditAjaxEvent<PortalObject> event) {
				try {
					closeEditor(event.getModel(), event.getRequestTarget());
					event.getRequestTarget().add(PortalPageStructurePanel.this);
				} catch (Exception e) {
					logger.error(e);
					fire( new ErrorEvent<PortalObject>(event.getRequestTarget(), event.getModel(), e));
				}
			}
		});
		
		// Edit
		//
	 	add(new WicketEventListener<PortalEditAjaxEvent<PortalObject>>() {
				private static final long serialVersionUID = 1L;
				@Override
				public void onEvent(PortalEditAjaxEvent<PortalObject> event) {
					try {
						if (selected_object!=null && selected_object.equals(event.getModel().getObject().getId())) {
							logger.debug(this.getClass().getName());
							closeEditor(event.getModel(), event.getRequestTarget());
						}
						else {
							logger.debug(this.getClass().getName());
							openEditor(event.getModel(), event.getRequestTarget());
							
							
						}
						event.getRequestTarget().add(PortalPageStructurePanel.this);
					} catch (Exception e) {
						logger.error(e);
						fire( new ErrorEvent<PortalObject>(event.getRequestTarget(), event.getModel(), e));
					}
				}
			});
		 
		// Up
		//
		 add(new WicketEventListener<PortalMoveUpAjaxEvent<PortalObject>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(PortalMoveUpAjaxEvent<PortalObject> event) {
				try {
					moveUp(event.getModel());
					selected_object = event.getModel().getObject().getId(); 
					event.getRequestTarget().add(PortalPageStructurePanel.this);
				} catch (Exception e) {
					logger.error(e);
					fire( new ErrorEvent<PortalObject>(event.getRequestTarget(), event.getModel(), e));
				}
			}
		});


		 
		 
		 add(new WicketEventListener<PortalMoveDownAjaxEvent<PortalObject>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(PortalMoveDownAjaxEvent<PortalObject> event) {
				try {
					moveDown(event.getModel());
					selected_object = event.getModel().getObject().getId();
					event.getRequestTarget().add(PortalPageStructurePanel.this);
				} catch (Exception e) {
					logger.error(e);
					fire( new ErrorEvent<PortalObject>(event.getRequestTarget(), event.getModel(), e));
				}
			}
		});

		 
			// Archive
		 add(new WicketEventListener<PortalArchiveAjaxEvent<PortalObject>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(PortalArchiveAjaxEvent<PortalObject> event) {
				try {
					archiveRestore(event.getModel());
					event.getRequestTarget().add(PortalPageStructurePanel.this);
				} catch (Exception e) {
					logger.error(e);
					fire( new ErrorEvent<PortalObject>(event.getRequestTarget(), event.getModel(), e));
					// setResponsePage(new ErrorPage<PortalObject>(e));
				}

			}
		});


			// Restore
		 add(new WicketEventListener<PortalRestoreAjaxEvent<PortalObject>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(PortalRestoreAjaxEvent<PortalObject> event) {
				try {
					archiveRestore(event.getModel());
					event.getRequestTarget().add(PortalPageStructurePanel.this);
				} catch (Exception e) {
					logger.error(e);
					fire( new ErrorEvent<PortalObject>(event.getRequestTarget(), event.getModel(), e));
				}
			}
		});


			// Delete
		 add(new WicketEventListener<PortalDeleteAjaxEvent<PortalObject>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(PortalDeleteAjaxEvent<PortalObject> event) {
				try {
					delete(event.getModel());
					event.getRequestTarget().add(PortalPageStructurePanel.this);
				} catch (Exception e) {
					logger.error(e);
					fire( new ErrorEvent<PortalObject>(event.getRequestTarget(), event.getModel(), e));
				}
			}
		});
		 
		 
			// Add
			//
			 add(new WicketEventListener<PortalAddAjaxEvent<PortalObject>>() {
				private static final long serialVersionUID = 1L;
				@Override
				public void onEvent(PortalAddAjaxEvent<PortalObject> event) {
					try {
						PortalObject po = addChildern(event.getModel());
						if (po!=null) {
							openEditor(new ObjectModel<PortalObject>(po), event.getRequestTarget());
							event.getRequestTarget().add(PortalPageStructurePanel.this);
						}
						else
							setResponsePage(new ApplicationErrorPage<Void>(new Model<String>("addChildern(event.getModel()) is null -> " + event.getModel().getObject().getClassKey() +" " + event.getModel().getObject().getId().toString() )));
						
					} catch (Exception e) {
						logger.error(e);
						fire( new ErrorEvent<PortalObject>(event.getRequestTarget(), event.getModel(), e));
					}
				}
			});


				// Refresh
				//
			 	add(new WicketEventListener<PortalAjaxRefreshEvent<PortalObject>>() {
					private static final long serialVersionUID = 1L;
					@Override
					public void onEvent(PortalAjaxRefreshEvent<PortalObject> event) {
						try {
							reload();
							//getParameters().put("reload", "yes");
							
							event.getRequestTarget().add(PortalPageStructurePanel.this);
						} catch (Exception e) {
							logger.error(e);
							fire( new ErrorEvent<PortalObject>(event.getRequestTarget(), event.getModel(), e));
						}
					}
				});

			 	
			 	
				// Editor Disposition	 
				 add(new WicketEventListener<PortalAjaxEditorDispositionEvent<PortalObject>>() {
						private static final long serialVersionUID = 1L;
						@Override
						public void onEvent(PortalAjaxEditorDispositionEvent<PortalObject> event) {
							try {
								portal_editor_disposition = event.getDisposition(); 
								setIntPreference(PortalAjaxEvent.EDITOR_DISPOSITION, portal_editor_disposition);
								event.getRequestTarget().add(PortalPageStructurePanel.this);
								
							} catch (Exception e) {
								logger.error(e);
								logger.error(e);
								fire( new ErrorEvent<PortalObject>(event.getRequestTarget(), event.getModel(), e));
							}
						}
					});
			 	
			 	
		// Error	 
		 add(new WicketEventListener<ErrorEvent<PortalObject>>() {
				private static final long serialVersionUID = 1L;
				@Override
				public void onEvent(ErrorEvent<PortalObject> event) {
					try {
						Panel err= new PortalErrorPanel<PortalObject>("error-panel", event.getModel(), event.getThrowable());
						PortalPageStructurePanel.this.addOrReplace(err);
						event.getRequestTarget().add(PortalPageStructurePanel.this);
					} catch (Exception e) {
						logger.error(e);
						/** we can not fire a ErrorEvent here ! **/
						setResponsePage(new ApplicationErrorPage<PortalObject>(e));
					}
				}
			});
	}
	
	


	
	protected void archiveRestore(IModel<PortalObject> model) {
		
		if (model.getObject().getState()==ObjectState.ENABLED)
			model.getObject().setState(ObjectState.ARCHIVED);
		else
			model.getObject().setState(ObjectState.ENABLED);
		
		model.getObject().getSite().getService(SiteService.class).save();
		
	}

						
	protected void delete(IModel<PortalObject> model) {
		model.getObject().setState(ObjectState.DELETED);
		model.getObject().getSite().getService(SiteService.class).save();
	}

	
	public void setPreference(String key, String value) {
		((com.novamens.kbee.security.KbeeUser) getSessionUser()).getService(PreferencesService.class).setValue("portal-page-"+getModel().getObject().getId().toString(), key, value);
	}
	
	public void setIntPreference(String key, int value) {
		((com.novamens.kbee.security.KbeeUser) getSessionUser()).getService(PreferencesService.class).setIntValue("portal-page-"+getModel().getObject().getId().toString(), key, value);
	}
	
	
	public String getPreference(String key, String defaultValue) {
		return ((com.novamens.kbee.security.KbeeUser) getSessionUser()).getService(PreferencesService.class).getValue("portal-page-"+getModel().getObject().getId().toString(), key, defaultValue);
	}

	public int getIntPreference(String key, int defaultValue) {
		return ((com.novamens.kbee.security.KbeeUser) getSessionUser()).getService(PreferencesService.class).getIntValue("portal-page-"+getModel().getObject().getId().toString(), key, defaultValue);
	}

	
	/**
	 * 
	 * @param model
	 */
	protected PortalObject addChildern(IModel<PortalObject> model) {
		PortalObject po = model.getObject();
		
		if (po instanceof Block) {
			Block block = (Block) po;
			logger.debug(block.toString());
		}
		
		if (po instanceof Area) {
			Area area = (Area) po;
			logger.debug(area.toString());
			String name = Block.KEY +" " + String.valueOf(area.getBlocks().size());
			Block block = ServiceLocator.getService(SiteFactoryService.class).addNewBlock(area, new KbeeBlock(name), AreaSection.LEFT, null);
			return block;
		}
		
		if (po instanceof PageSection) {
			PageSection ps = (PageSection) po;
			logger.debug(ps.toString());
			String name = Area.KEY +" " + String.valueOf(ps.getAreas().size());
			Area area = ServiceLocator.getService(SiteFactoryService.class).addNewArea(ps, name, AreaType.AREA_1S);
			Area ret=getPortalDao().findAreaById(area.getId());
			
			logger.debug(ret.getParent());
			logger.debug(area.getParent());
			
			
			
			return ret;

		}
		if (po instanceof Page) {
			Page pa = (Page) po;
			logger.debug(pa.toString());
			String name = PageSection.KEY+" " + String.valueOf(pa.getPageSections().size());
			PageSection ps = ServiceLocator.getService(SiteFactoryService.class).addNewPageSection(pa, name, PageSectionType.ONE_SECTION, PageSectionDisposition.LEFT);
			return ps;
		}
		return null;
	}

	
	protected void moveUp(IModel<PortalObject> model) {
		
		PortalObject po = model.getObject();
		
		if (po instanceof Block) {
			Block block = getPortalDao().findBlockById(po.getId());
			Area parent = (Area) block.getParent();
			
			logger.debug(block.getAreaSection().getLabel() + " " + String.valueOf(block.getOrder()));
			
			parent.moveUp(block);
			Site site=parent.getSite();
			site.getService(SiteService.class).save();
			
			logger.debug(block.getAreaSection().getLabel() + " " + String.valueOf(block.getOrder()));
		}
		
		if (po instanceof Area) {
			
			Area area = getPortalDao().findAreaById(po.getId());
			PageSection parent = (PageSection) area.getParent();
			parent.moveUp(area);
			Site site=parent.getSite();
			site.getService(SiteService.class).save();
		}
		
		if (po instanceof PageSection) {
			PageSection ps = getPortalDao().findPageSectionById(po.getId());
			Page parent = (Page) ps.getParent();
			Site site=parent.getSite();
			site.getService(SiteService.class).save();
			//parent.getService(SiteService.class).save();
			//parent.moveUp(ps);
		}
	}
	
						
	protected void moveDown(IModel<PortalObject> model) {
		
		PortalObject po = model.getObject();
		
		if (po instanceof Block) {
			Block block = (Block) po;
			Area parent = (Area) block.getParent();
			parent.moveDown(block);
			Site site=parent.getSite();
			site.getService(SiteService.class).save();
		}
		
		if (po instanceof Area) {
			Area area = (Area) po;
			PageSection parent = (PageSection) area.getParent();
			parent.moveDown(area);
			Site site=parent.getSite();
			site.getService(SiteService.class).save();

		}
		
		if (po instanceof PageSection) {
			PageSection ps = (PageSection) po;
			Page parent = (Page) ps.getParent();
			Site site=parent.getSite();
			//site.getService(SiteService.class).save();
			//parent.moveUp(ps);
			//parent.getService(SiteService.class).save();
		}
	}
	
	// "edit-mode"
	
	protected void setShowController(int s) {
		this.showController=s;
		getParameters().put(PortalAjaxEvent.CONTROLLER_VISIBLE, String.valueOf(showController));
		setIntPreference(PortalAjaxEvent.CONTROLLER_VISIBLE, showController);
		
	}
	
	protected void setShowDeleted(int s) {
		this.showDeleted=s;
		getParameters().put(PortalAjaxEvent.DELETED_VISIBLE, String.valueOf(this.showDeleted));
		setIntPreference(PortalAjaxEvent.DELETED_VISIBLE, this.showDeleted);
	}

	
	protected void setShowArchived(int s) {
		this.showArchived=s;
		getParameters().put(PortalAjaxEvent.ARCHIVED_VISIBLE, String.valueOf(this.showArchived));
		setIntPreference(PortalAjaxEvent.ARCHIVED_VISIBLE, this.showArchived);
	}


	protected void setShowPayload(int s) {
		this.show_payload=s;
		getParameters().put(PortalAjaxEvent.PAYLOAD_VISIBLE, String.valueOf(this.show_payload));
		setIntPreference(PortalAjaxEvent.PAYLOAD_VISIBLE, this.show_payload);
	}
	
	
	protected void setShowHierarchy(int s) {
		this.showHierarchy=s;
		getParameters().put(PortalAjaxEvent.STRUCTURE_VIEW, String.valueOf(this.showHierarchy));
		setIntPreference(PortalAjaxEvent.STRUCTURE_VIEW, this.showHierarchy);
	}
	
	//protected void setViewMode(int viewMode) {
	//	this.viewMode = viewMode;
	//	getParameters().put(PortalAjaxEvent.STRUCTURE_VIEW, String.valueOf(this.viewMode));
	//	setIntPreference(PortalAjaxEvent.STRUCTURE_VIEW, this.viewMode);
	//	
	//}

	
	private void load() {

		error_panel = new InvisiblePanel("error-panel");
		addOrReplace(error_panel);
		
		add_panel = new InvisiblePanel("add-panel");
		addOrReplace(add_panel);

		
		Panel panel = null;
		
		addOrReplace(new PortalPageStructureToolbar("toolbar", getModel(), getParameters()));
		
		main_panel_contaniner = new WebMarkupContainer("main-panel-contaniner"); 
		main_panel_contaniner.setOutputMarkupId(true);
		addOrReplace(main_panel_contaniner);
		
		
    	Page page= getModel().getObject();
    	
    	try {
	    	if (page!=null) {
	    		PortalViewMode pvm =  PortalViewMode.EDIT;
	    		Map<String, String> map=getParameters();
	    		panel = page.getService(PortalObjectViewerRenderService.class).build("main-panel", 0, pvm,  map);
	    	}
	    	else
	    		panel = new PortalErrorPanel<Page>("main-panel", getModel(),  new Model<String>("page not found"));
	    	} catch (Exception e) {
	    		logger.error(e);
	    		panel = new PortalErrorPanel<Page>("main-panel", e);
	    	}
    	
    	main_panel_contaniner.add(panel);
    	editor_panel_contaniner = new WebMarkupContainer("editor-panel-contaniner"); 
    	editor_panel_contaniner.setOutputMarkupId(true);
		addOrReplace(editor_panel_contaniner);
		
		
		
		if( pin_editor == PortalAjaxEvent.PIN_EDITOR_YES) {
			main_panel_contaniner.add( new AttributeModifier("class", "col-md-8 col-lg-9 col-xs-12 main-panel " + (isEditorLeft() ? " fright": " fleft")));
			editor_panel_contaniner.add( new AttributeModifier("class", "col-md-4 col-lg-3 col-xs-12 editor-panel " + (isEditorLeft() ? " fleft": " fright")));
			editor_panel_contaniner.add(new InvisiblePanel("editor-panel"));
		}
		
		else {
			editor_panel_contaniner.add(new InvisiblePanel("editor-panel"));
			editor_panel_contaniner.setVisible(isEditorVisible());
			main_panel_contaniner.add( new AttributeModifier("class", "col-md-12 col-lg-12 col-xs-12 main-panel " + (isEditorLeft() ? " fright": " fleft")));
			editor_panel_contaniner.add( new AttributeModifier("class", "col-md-4 col-lg-3 col-xs-12 editor-panel " + (isEditorLeft() ? " fleft": " fright")));
		}
		
		/**
		if (portal_editor_disposition==PortalAjaxEvent.EDITOR_DISPOSITION_LEFT) {
			main_panel_contaniner.add( new AttributeModifier("style", "float:right;"));
			editor_panel_contaniner.add(  new AttributeModifier("style", "float:left;"));
		}
		else {
			main_panel_contaniner.add( new AttributeModifier("style", "float:left;"));
			editor_panel_contaniner.add(  new AttributeModifier("style", "float:right;"));
		}
		**/
	}

	
	/**
	 *  View -> plain / nested
	 *  View -> Release / Edit
	 *  View -> header expanded
	 *  Isolate 
	 *  all / deleted / archived / enabled
	 */
	@Override
	public void onInitialize() {
		super.onInitialize();
		setOutputMarkupId(true);
		load();
		
	}

	
	
	protected boolean isEditorVisible() {
		return is_editor_visible.booleanValue();
	}
	
	
	
	
	
	
	private void openEditor(IModel<PortalObject> model, AjaxRequestTarget target) {
		
		
		if (model.getObject() instanceof PageSection) {
			IModel<PageSection> em = new ObjectModel<PageSection>(  (PageSection) model.getObject() );
			editor_panel_contaniner.addOrReplace(new PortalPageSectionMainPanel("editor-panel", em));
		}
		
		else if (model.getObject() instanceof Area) {
			IModel<Area> em = new ObjectModel<Area>(  (Area) model.getObject() );
			editor_panel_contaniner.addOrReplace(new PortalAreaMainPanel("editor-panel", em));
		}

		else if (model.getObject() instanceof Block) {
			 editor_panel_contaniner.addOrReplace(new PortalBlockMainPanel("editor-panel", new ObjectModel<Block>(  (Block) model.getObject() )));
		}
		
		else if (model.getObject() instanceof Page) {
			editor_panel_contaniner.addOrReplace(new PortalPageMainPanel("editor-panel", new ObjectModel<Page>((Page) model.getObject() )));
		}

		else
			editor_panel_contaniner.addOrReplace(new PortalErrorPanel<PortalObject>("editor-panel",  model, new Model<String>(model.getObject().getTitle())));
		
		this.is_editor_visible = Boolean.valueOf(true);
		main_panel_contaniner.add( new AttributeModifier("class", "col-md-8 col-lg-9 col-xs-12 main-panel "     + (isEditorLeft() ? " fright": " fleft")));
		editor_panel_contaniner.add( new AttributeModifier("class", "col-md-4 col-lg-3 col-xs-12 editor-panel " + (isEditorLeft() ? " fleft": " fright")));
		editor_panel_contaniner.setVisible(isEditorVisible());
		selected_object = model.getObject().getId();
	
	}
	

					
	protected boolean isEditorLeft() {
		return this.portal_editor_disposition==PortalAjaxEvent.EDITOR_DISPOSITION_LEFT;
	}


	private void closeEditor(IModel<PortalObject> model, AjaxRequestTarget target) {
		
		if( pin_editor == PortalAjaxEvent.PIN_EDITOR_YES) {
			main_panel_contaniner.add( new AttributeModifier("class", "col-md-8 col-lg-9 col-xs-12 main-panel " + (isEditorLeft() ? " fright": " fleft")));
			editor_panel_contaniner.add( new AttributeModifier("class", "col-md-4 col-lg-3 col-xs-12 editor-panel " + (isEditorLeft() ? " fleft": " fright")));
			editor_panel_contaniner.addOrReplace(new InvisiblePanel("editor-panel"));	
		}
		else {
			main_panel_contaniner.add( new AttributeModifier("class", "col-md-12 col-lg-12 col-xs-12 main-panel"));
			is_editor_visible = Boolean.valueOf(false);
			editor_panel_contaniner.setVisible(isEditorVisible());
		}
		
		selected_object = null;
	}
	

	@SuppressWarnings("unchecked")
	private boolean walk(WebMarkupContainer c) {
		
		boolean done = false;
		
		if (c instanceof PortalIWebPanel) {
			
			if ((((PortalIWebPanel<PortalObject>) c).isSelected() && !(getModel().getObject()).getId().equals(selected_object))) {
				((PortalIWebPanel<PortalObject>) c).setSelected(false);
				((PortalIWebPanel<PortalObject>) c).onBeforeRender();
			}
			
			if ((((PortalIWebPanel<PortalObject>) c).getModel().getObject()).getId().equals(selected_object)) {
				((PortalIWebPanel<PortalObject>) c).setSelected(true);
				((PortalIWebPanel<PortalObject>) c).onBeforeRender();
				logger.debug("found -> " + ((PortalIWebPanel<PortalObject>) c).getClass().getName() );
				done = true;
				return done;
			}
			else {
				((PortalIWebPanel<PortalObject>) c).setSelected(false);
			}
		}
		
		
		Iterator<Component> it =c.iterator();
		
		 while (it.hasNext()) {
			
			 Component son = it.next();
			
			 if (son instanceof WebMarkupContainer) {
				 done=walk((WebMarkupContainer) son);
				 if (done)
					 break;
			} 
		}
		 return done;
	}
	
	
	
	private boolean setParametersWalk(WebMarkupContainer c) {
		
		boolean done = false;
		
		if (c instanceof IPortalWebPanel) {
			
			((IPortalWebPanel) c).setParameters(getParameters());
		}
		
		Iterator<Component> it =c.iterator();
		
		 while (it.hasNext()) {
			
			 Component son = it.next();
			
			 if (son instanceof WebMarkupContainer) {
				 done=setParametersWalk((WebMarkupContainer) son);
				 if (done)
					 break;
			} 
		}
		 return done;
	}
	

	private void setAllParameters() {
		Iterator<Component> it =this.iterator();
		while (it.hasNext()) {
			Component c = it.next();
			if (c instanceof WebMarkupContainer) {
				 boolean done=setParametersWalk((WebMarkupContainer) c);
				 if (done)
					 break;
			}
		}
	}
	
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	
}
	 
	



