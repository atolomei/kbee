package kbee.web.library;


import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.library.Library;
import com.novamens.content.user.UserService;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.panel.VerticalLayout;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.kbee.wicket.markup.html.page.PageMainTabs;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.editor.ObjectEditor;

import kbee.web.model.object.AuditTrailObjectPanel;
import kbee.web.object.ObjectStateEditor;

@SuppressWarnings("serial")
public class LibraryMainPanel extends ObjectEditor<Library> implements PageMainTabs {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(LibraryMainPanel.class.getName());

	private static final long serialVersionUID = 1L;
	
	public LibraryMainPanel(IModel<Library> model, boolean isNew) {
		this("editor", model, isNew);
	}
	
	public LibraryMainPanel(String id, IModel<Library> model, boolean isNew) {
		super(id, model);
		
		setModel(model);
		setIsNew(isNew);
		
		List<ITab> tabs = new ArrayList<ITab>();
		
		tabs.add(new AbstractTab(new StringResourceModel("editor.cabinet", this, null)) {
			@Override
			public Panel getPanel(String panelId) {
				return new LibraryEditor(panelId, getModel(), isNew()) {
					@Override
					public void setEditionEnabled(boolean value) {
						LibraryMainPanel.this.setEditionEnabled(value);
					}
					@Override
					public boolean isEditionEnabled() {
						return LibraryMainPanel.this.isEditionEnabled();
					}
					@Override
					public void onClose(AjaxRequestTarget target) {
						LibraryMainPanel.this.onClose(target);
					}
					
					@Override
					public void onCancel(AjaxRequestTarget target) {
						if (isNew()) {
							LibraryMainPanel.this.onClose(target);
						}
						else { 
							setEditionEnabled(false);
							target.add(LibraryMainPanel.this);
						}
					}
				};
			}
		});
		
		tabs.add(new AbstractTab(new StringResourceModel("editor.state", this, null)) {
			@Override
			public Panel getPanel(String panelId) {
				return new ObjectStateEditor<Library>(panelId, getModel());
			}
		});

		
		/**tabs.add(new AbstractTab(new StringResourceModel("editor.roles", this, null)) {
			@Override
			public Panel getPanel(String panelId) {
				return new ObjectStateEditor<Library>(panelId, getModel());
			}
		});
		**/
		
		
		tabs.add(new AbstractTab(new StringResourceModel("editor.audit", this, null)) {
			@Override
			public Panel getPanel(String panelId) {
				return new AuditTrailObjectPanel<Library>(panelId, getModel());
			}
		});
		
		
		
		
		VerticalLayout<ITab> editor = new VerticalLayout<ITab>("tabs",this.getClass().getName(),tabs)  {
			@Override
			protected void onAjaxUpdate(AjaxRequestTarget target) {
				String str = this.getTabs().get(this.getSelectedTab()).getTitle().getObject();
				getUser().getService(PreferencesService.class).setValue(LibraryMainPanel.class.getSimpleName(), "selected_tab", str);
				((AbstractKbeeWebPage) getPage()).setPageInternalSectionHelpKey(str);
			}
		};
		
		
		add(editor);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		@SuppressWarnings("unchecked")
		VerticalLayout<ITab> tabs = (VerticalLayout<ITab>) get("tabs");
		tabs.setTitle(new StringResourceModel("sections", this, null));
		
		int sel = tabs.getSelectedTab();
		if (sel==-1) sel=0;
		String str = (tabs.getTabs().get(sel)).getTitle().getObject();
		((AbstractKbeeWebPage) getPage()).setPageInternalSectionHelpKey(str);
	}

	protected void onClose(AjaxRequestTarget target) {
	}
	 
	private KbeeUser getUser() {
		return (KbeeUser) ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();
	}
	
	private String initial_tab;
	@Override
	public void setInitialTab(String a) {
			initial_tab=a;
			try {
				Integer in=Integer.valueOf(a)-1;
				if (in>=0 && in < ((VerticalLayout<ITab>) get("tabs")).getTabs().size())
				((VerticalLayout<ITab>) get("tabs")).setSelectedTab(in.intValue());	
			} catch (Exception e) {
				logger.error(e);
			}
	}

	@Override
	public String getInitialTab() {
		return initial_tab;
	}
}
