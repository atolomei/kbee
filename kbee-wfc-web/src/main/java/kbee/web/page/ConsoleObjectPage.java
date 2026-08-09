package kbee.web.page;

import org.apache.wicket.Page;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.indexer.query.Cursor;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.wicket.markup.html.form.Form;

import kbee.web.error.ApplicationErrorPage;
import kbee.web.search.SearcherNavigatorPanel;

/**
 *  Detail page inside a Console
 * 
 * @param <T>
 */
@SuppressWarnings("serial")
public class ConsoleObjectPage<T> extends ApplicationPage<T> {
				
	private static final long serialVersionUID = 1L;
	
	protected static final ResourceReference BL = new CssResourceReference(Form.class, "build.css");
	protected static final ResourceReference BS = new CssResourceReference(Form.class, "bootstrap-select.css");
	protected static final ResourceReference BSJS = new JavaScriptResourceReference(Form.class, "bootstrap-select.js");

	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ConsoleObjectPage.class.getName());
	
	private String source;
	private IModel<Cursor> cursor_model;
	
	

	
	
	public ConsoleObjectPage() {
	}

	public ConsoleObjectPage(PageParameters parameters) {
	}
	
	public ConsoleObjectPage(IModel<T> model) {
		super(model);
	}
	
	public ConsoleObjectPage(IModel<T> model, IModel<Cursor> cursor_model) {
		super(model);
		this.cursor_model=cursor_model;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
	}
	
	public void setSource(String source) {
		this.source=source;
	}

	public String getSource() {
		return source;
	}
	public Panel getSearchNavigation()  {
			return getSearchNavigation("navigation"); 
	}
	
	public Panel getSearchNavigation(String id)  {
		
		if (getCursorModel()==null)
			return new InvisiblePanel(id);
																		
		SearcherNavigatorPanel<T> na=new SearcherNavigatorPanel<T>(id, getCursorModel()) {
			@Override
			public void onNavigate(T object) {
				try {
					setResponsePage( getNavigatePage(object, getCursor()));
				} 
				catch (Exception e) {
					logger.error(e);
					setResponsePage(new ApplicationErrorPage<T>(e));
				}
			}
		};
		return na;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (cursor_model!=null)
			cursor_model.detach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(BL));
		response.render(CssHeaderItem.forReference(BS));
		response.render(JavaScriptHeaderItem.forReference(BSJS));
	}
	
	protected void setCursorModel(IModel<Cursor> cm) {
		this.cursor_model=cm;
	}
	
	protected IModel<Cursor> getCursorModel() {
		return this.cursor_model;
	}

	protected Page getNavigatePage(T object, IModel<Cursor> mc) {
		return null;
	}
}
