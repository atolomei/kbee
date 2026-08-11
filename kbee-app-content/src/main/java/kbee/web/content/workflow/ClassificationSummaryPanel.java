package kbee.web.content.workflow;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;

import com.novamens.content.base.Content;
import com.novamens.content.service.ContentService;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.wicket.markup.html.editor.ObjectEditorPanel;

import kbee.web.event.wicket.EditorEvent;

@SuppressWarnings("serial")
public class ClassificationSummaryPanel<T extends Content> extends ObjectEditorPanel<T>{
	private static final long serialVersionUID = 1L;

	
	public ClassificationSummaryPanel() {
		this("summary");
	}
		
	public ClassificationSummaryPanel(String id) {
		super(id);
		
		setOutputMarkupId(true);
		
		add(new WicketEventListener<EditorEvent>() {
			public void onEvent(EditorEvent event) {
				event.getRequestTarget().add(ClassificationSummaryPanel.this);
			}
		});
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		if (get("label")==null) {
			addLabel();
		}
	}
	
	protected void addLabel() {
		add(new Label("label", new Model<String>() {
			public String getObject() {
				return getSummary(); 
			}
		}));
	}
	
	protected String getSummary() {
		return getModelObject().getService(ContentService.class).getConsoleSubtitleDefaultIfNull();
	}
}
