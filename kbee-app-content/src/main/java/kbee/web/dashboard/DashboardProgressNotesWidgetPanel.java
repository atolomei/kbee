package kbee.web.dashboard;


import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import com.novamens.content.base.Content;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.datetime.DateTimeService;
import com.novamens.indexer.query.ResultSet;
import com.novamens.kbee.content.workflow.KbeeActivityProgressNote;
import com.novamens.kbee.content.workflow.KbeeWorkflowActivity;
import com.novamens.kbee.content.workflow.WebTask;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.PortalViewRender;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.ActivityProgressNote;
import com.novamens.workflow.Task;

import kbee.web.error.ApplicationErrorPage;
import kbee.web.error.ErrorPanel;
import kbee.web.help.InlineHelpWebService;
import kbee.web.workflow.task.TaskPage;

@SuppressWarnings({ "serial", "deprecation" })
public class DashboardProgressNotesWidgetPanel extends DashboardWidgetBasePanel implements PortalViewRender {
				
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DashboardProgressNotesWidgetPanel.class.getName());
	
	static final int LIMIT = 100;

	static final int MAX = 260;
	
	//private  int size = 0;
	//private  long total = 0;
	
	private List<IModel<ActivityProgressNote>> lastnotes;
	private WebMarkupContainer help;
	private WebMarkupContainer main_container;
	private WebMarkupContainer list_container;
	
		
	
	public DashboardProgressNotesWidgetPanel(String id) {
		this(id, "progressnotes");
	}
	
	public DashboardProgressNotesWidgetPanel(String id, String preferences_key) {
		super(id, preferences_key);						
		super.setHelp(true);
		setTitle(getLabel("progressnotes"));
	}



	
	@Override
	public void onInitialize() {
		
		
		addNotesView();
		
		main_container.setVisible(!isCollapsed());
		main_container.add(new InvisiblePanel("help"));
		
		super.onInitialize();
	}
	
	public void onDetach() {
		super.onDetach();
		
		if ( lastnotes!=null)
			lastnotes.forEach(item -> item.detach());
	}
	
	
	@Override
	protected void refresh(AjaxRequestTarget target) {
		super.refresh(target);
		lastnotes = null;
		target.add(this);
	}

	protected boolean isCollapsable() {
		return true;
	}
	
	/**
	 */
	@Override
	protected void onClickCollapse(AjaxRequestTarget target) {
		main_container.setVisible(!main_container.isVisible());
		refresh(target);
	}

	protected void onHelp(AjaxRequestTarget target) {
		toogleHelp(target);
	}
	

	
	protected List<IModel<ActivityProgressNote>> getLastNotes() {
		
		
		if ( lastnotes!=null)
			return lastnotes;
		
		int index = 0;
		lastnotes = new ArrayList<IModel<ActivityProgressNote>>();
		
		ResultSet notes = getAllNotes();
		
		if (notes!=null) {
			while (notes.hasNext() && index++<LIMIT) {
				lastnotes.add(new ObjectModel<ActivityProgressNote>((ActivityProgressNote)notes.next().getObject()));
			}
			//total = notes.size();
		}
		
		//this.size = notes.size();
		
		return lastnotes;
	}
	
	protected ResultSet getAllNotes() {
		try {
			KbeeUser us = (KbeeUser) getSessionUser();
			return us.getService(UserDashboardService.class).getProgressNotes(LIMIT);
		} 
		catch (Exception e)  {	
			logger.error(e);
			return null;
		}
	}
	
	protected void addNotesView() {
		
		main_container = new WebMarkupContainer ("main-container");
		main_container.setOutputMarkupId(true);
		addOrReplace(main_container);
		
		list_container = new WebMarkupContainer ("list-container");
		list_container.setOutputMarkupId(true);
		main_container.addOrReplace(list_container);
		
		
		list_container.add(new ListView<IModel<ActivityProgressNote>>("note", ()->getLastNotes()) {
		
			public void populateItem(ListItem<IModel<ActivityProgressNote>> item) {
			
				try {
					
					ActivityProgressNote note = item.getModelObject().getObject();
					Content content = ((KbeeWorkflowActivity)note.getActivity()).getContent();
					IModel<Content> contentmodel = new ObjectModel<Content>(content);
					Link<?> notelink = new Link<Void>("note-link") {
						public void onClick() {
							DashboardProgressNotesWidgetPanel.this.onClick(contentmodel.getObject(), 0);
						}
					};
					
					notelink.setPopupSettings(new PopupSettings(  PopupSettings.LOCATION_BAR | PopupSettings.MENU_BAR | 
						PopupSettings.RESIZABLE | PopupSettings.SCROLLBARS | 
						PopupSettings.STATUS_BAR | PopupSettings.TOOL_BAR));
					
					notelink.add(new Label("content", content.getTitle()));
					Label textlabel = new Label("text",   getSnippet( note.getText() ));
					textlabel.setEscapeModelStrings(false);

					item.add(textlabel);
					item.add(notelink);
					item.getModelObject().detach();
					item.add(new Label("user", ((KbeeActivityProgressNote)note).getLastModifiedUser().getFirstLastName()));
					Label datelabel = new Label("date", ServiceLocator.getService(DateTimeService.class).timeElapsed(note.getTime()));
					datelabel.setEscapeModelStrings(false);
					item.add(datelabel);
					
				} catch (Exception e) {
					item.setVisible(false);
					logger.error(e);
				}
			}
		});
	}
	
	
	
	
	protected IModel<String> getSnippet(String text) {
		if (text==null || text.isEmpty())
			return new Model<String>();
		String s = null;
		if (text.length()>MAX)
			s = text.substring(0, MAX)+"...";
		else
			s=text;
		Safelist list = Safelist.basic();
		list.removeTags("p");
		String cleaned = Jsoup.clean(s, list);
		String t1 = cleaned;
		return  new Model<String>(t1);
	}
	
	@SuppressWarnings("unchecked")
	protected void onClick(Content content, int index) {

		if (content==null)
			return;
		
		WorkflowService workflowService = content.getService(WorkflowService.class);
		
		try {
			TaskPage<Content> page = null;
			if (workflowService.getTask()!=null && workflowService.getContext().getProcess().isRunning()) {
					Task task = workflowService.getTask();
					page = (TaskPage<Content>)((WebTask)task).getPage(workflowService.getContext());
					page.setInitialTab("notes");
					
					if (content.getWorkspace()>0) {
						if (getSessionUser().getId().toString().equals(content.getWorkspace().toString())) {
							page.setEditionEnabled(true);
							page.setReadOnly(false);
						}
						else {
							page.setEditionEnabled(false);
							page.setReadOnly(true);
						}
					}
					else {
						page.setEditionEnabled(false);
						page.setReadOnly(true);
					}
			}
			if (page==null)
				throw new IllegalArgumentException("page is null for content -> " + content.getDisplayName());
			
			setResponsePage(page);
		} 
		catch (Exception e) {
			logger.error(e);
			setResponsePage( new ApplicationErrorPage<>(e));
		}
	}
	
	protected IModel<String> getAllString() {
		return getLabel("mytasks");
	}
	
	@Override
	protected void onTitleClick() {
	}

	
	@Override
	protected WebMarkupContainer getHelpPanel() {
		WebMarkupContainer  pa = ServiceLocator.getService(InlineHelpWebService.class).getPanel("help", getLocale(), InlineHelpWebService.HOME_PROGRESS_NOTES);
		if (pa!=null) return pa;
		return new ErrorPanel("help", new Model<String>(InlineHelpWebService.HOME_PROGRESS_NOTES));
	}
	
	protected String getName() {
		return "home-progress-notes";
	}
	
	public void toogleHelp(AjaxRequestTarget target) {

		if (help==null) {
			help=getHelpPanel();
			help.setVisible(false);
			main_container.addOrReplace(help);
		}
		
		if (help!=null && !(help instanceof InvisiblePanel)) {
			help.setVisible(!help.isVisible());
			list_container.setVisible(!list_container.isVisible());
			target.add(this);
		}
	}
	
}