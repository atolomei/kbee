package kbee.web.dataset;



import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;

import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.ExtractionRule;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.wicket.markup.html.editor.ObjectEditorPanel;

import kbee.web.eform.EAjaxFormEvent;

@SuppressWarnings("serial")

public class MemberDisplayNamePanel<T extends DataSetMember> extends ObjectEditorPanel<T>  {
				
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(MemberDisplayNamePanel.class.getName());
	private String title;
 
	
	public MemberDisplayNamePanel(String id) {
		super(id);
		setOutputMarkupId(true);
	}
	
	public MemberDisplayNamePanel() {
		this("title-panel");
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}
	
	@Override
	public void updateModel() {
		if (title!=null && !title.equals(getModelObject().getStrValue()) && !"".equals(title.trim())) {
			getModelObject().setStrValue(title);
		}
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		addListeners();
	}

	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		if (get("xcontent-title")==null) {
			setTitle(getModelObject().getStrValue());
			addComponents();
		}
	}

	protected void addComponents() {
		WebMarkupContainer title = new WebMarkupContainer("xcontent-title") {
			@Override
			public boolean isVisible() {
				return true;
			}
		};
		
		title.add(new Label("title-text", new Model<String>() { 
			public String getObject() { 
				return getTitle(); 
			};
		}));
		
		add(title);
	}
	
	protected void addListeners() {
		
		add(new WicketEventListener<EAjaxFormEvent>() {
			public void onEvent(EAjaxFormEvent event) {
				T content = getEditor().getModelObject();
				(getEditor()).update(content);
				String title = getTitleByRule(content);
				if (title!=null) setTitle(title);
				if (event.getRequestTarget()!=null) {
					event.getRequestTarget().add(MemberDisplayNamePanel.this);
				}
			}
		});	
	}

	protected String getTitleByRule(T content) {
		try {
			ExtractionRule rule = getModelObject().getDataSet().getDisplayNameRule();
			String title = rule!=null ? (String)rule.extract(content) : null;
			return title;
		}
		catch (Exception e) {
			logger.error(e);
			return null;
		}
	}
}