package kbee.web.nav;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;


import com.novamens.kbee.wicket.editor.Editor;
import com.novamens.wicket.markup.html.repeater.util.Searcher;


/**
 * Bootstrap based. Navigation Bar
 */
@SuppressWarnings("serial")
public class TabNavigationBar<T> extends NavigationPanel<T>  {
	private static final long serialVersionUID = 1L;
	
	private IModel<String> model_title;
			
	public TabNavigationBar(String id) {
		super(id);
		setOutputMarkupId(true);
		add((new Panel("navigator"){}).setVisible(false));
	}
	
	public TabNavigationBar(String id, IModel<String> title) {
		super(id);
		setTitleModel(title);
		add((new Panel("navigator"){}).setVisible(false));
		setOutputMarkupId(true);
	}
	
	public TabNavigationBar(String id, Searcher searcher, long index) {
		super(id);
		setOutputMarkupId(true);
		add(newNavigator(searcher, index));
	}
	
	public void setTitleModel(IModel<String> tm) {
		this.model_title=tm;
		if (get("title")!=null)
			replace(new Label("title", this.model_title));
	}
	
	public IModel<String> getTitleModel() {
		return this.model_title;
	}
	
	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);
		StringBuilder script = new StringBuilder();
		script.append("function closewindow() {\n");
		script.append("	if (window.opener && window.opener.refresh) { window.opener.refresh(); };\n");
		script.append("	var agent = navigator.userAgent;\n");
		script.append("	if (agent.indexOf('Edge') > 0 || agent.indexOf('Trident') > 0) {\n");
		script.append("		window.open('', '_self', '');\n");
		script.append("	}\n");
		script.append("	window.close();\n");
		script.append("}\n");
		response.render(JavaScriptHeaderItem.forScript(script.toString(), "closewindow"));
	}
	
	public void navigate() {
		
	}
	
	public void onNavigate(T object) {
		
	};
	
	public void onStartWorkflow() {
		
	}
	
	public void setEditor(Editor<?> editor) {
		
	}
	
	public boolean isFromContentBase() {
		return false;
	}
	
	public void onReturn(AjaxRequestTarget target)  {
		target.appendJavaScript("closewindow();");
	}

	public void onBeforeRender() {
		super.onBeforeRender();

		addOrReplace(newCloseLink());
		
		Label label = new Label("title", (getTitleModel()!=null?getTitleModel():new Model<String>(""))) {
			public boolean isVisible() {
				return getTitleModel()!=null;
			}
		};
		
		addOrReplace (label);
		
	}

	protected Component newCloseLink()  {
		AbstractLink link = new AjaxLink<Void>("close-link") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				target.appendJavaScript("closewindow();");
 			}
		};	
		return link;
	}
	
	protected Component newNavigator(Searcher searcher, long index)  {
		return new NavigatorPanel<T>("navigator", searcher, (int)index) {
			public void onNavigate(T object) {
				TabNavigationBar.this.onNavigate(object);
			}
		};
	}
 }