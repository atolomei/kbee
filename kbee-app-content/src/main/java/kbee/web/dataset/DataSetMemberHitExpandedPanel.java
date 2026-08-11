package kbee.web.dataset;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.content.model.DataSetMember;
import com.novamens.kbee.wicket.markup.html.console.browser.HitExpandedPanel;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.tabs.AjaxTabbedPanel;

import kbee.web.console.AbstractConsole;

@SuppressWarnings("serial")
public class DataSetMemberHitExpandedPanel extends ObjectEditor<DataSetMember> implements HitExpandedPanel {
	private static final long serialVersionUID = 1L;

	private static boolean READ_ONLY = true;
	
	private AbstractConsole<?> console;
	
	public DataSetMemberHitExpandedPanel(String id, AbstractConsole<?> console, IModel<DataSetMember> model) {
			this(id, console, model, null);
	}
	
	public DataSetMemberHitExpandedPanel(String id, AbstractConsole<?> console, IModel<DataSetMember> model, List<String> snippets) {
		super(id, model);
		this.console=console;
	}
	
	public AbstractConsole<?> getConsole() {
		return console;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		List<ITab> tabs = new ArrayList<ITab>();
		
//		tabs.add(new AbstractTab(getLabel("editor.info")) {
//			@Override
//			public Panel getPanel(String panelId) {
//				return new ExpandedPanel<DataSetMember>(panelId, getConsole(), getModel());
//			}
//		});
		
		tabs.add(new AbstractTab(getLabel("editor.summary")) {
			@Override
			public Panel getPanel(String panelId) {
				return new MemberMetaInfoPanel<DataSetMember>(panelId, getModel());
			}
		});

		tabs.add(new AbstractTab(getLabel("editor.notes")) {
			@Override
			public Panel getPanel(String panelId) {
				return new MemberNotesEditorPanel(panelId, getModel(), false, READ_ONLY);
			}
		});
		
		AjaxTabbedPanel<ITab> tabbedpanel = new AjaxTabbedPanel<ITab>("tabs", tabs) {
			protected String getNavCss() {
				return "nav nav-tabs";
			}
		};
		
		add(tabbedpanel);
	}
}