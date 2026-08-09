package kbee.web.content.template;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.indexer.iql.IqlService;
import com.novamens.indexer.query.ResultSet;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.panel.ViewMode;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.iql.SolrIqlService;
import com.novamens.wicket.markup.html.form.DateField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.FileUtil;

import kbee.web.uploader.UploadBehavior;

@SuppressWarnings("serial")
public class BrowserPanel<T extends Content> extends ModelPanel<T>{
	private static final long serialVersionUID = 1L;
	
	//private String resourceType;
													
	protected static final ResourceReference BS_CSS = new CssResourceReference(Form.class, Form.BOOTSTRAP);
	
	private static final ResourceReference CSS_KBEE_BOOTSTRAP			 = new CssResourceReference(AbstractKbeeWebPage.class, "kbeebootstrap.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_0_360      = new CssResourceReference(AbstractKbeeWebPage.class, "kb-0-360.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_361_800    = new CssResourceReference(AbstractKbeeWebPage.class, "kb-361-800.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_801_1200   = new CssResourceReference(AbstractKbeeWebPage.class, "kb-801-1200.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_1201_1600  = new CssResourceReference(AbstractKbeeWebPage.class, "kb-1201-1600.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_1601  	 = new CssResourceReference(AbstractKbeeWebPage.class, "kb-1601.css");

	protected static final ResourceReference BS_JS = new JavaScriptResourceReference(DateField.class, com.novamens.wicket.markup.html.form.Form.BOOTSTRAP_JS);
	
//	public class RefreshBehavior extends AbstractDefaultAjaxBehavior {
//		@Override
//		protected void respond(AjaxRequestTarget target) {
//			target.add(BrowserPanel.this.get("resources-view"));
//		}
//		@Override
//		public void renderHead(final Component component, final IHeaderResponse response) {
//			super.renderHead(component, response);
//			StringBuilder script = new StringBuilder();
//			script.append("function refreshfiles"+BrowserPanel.this.getMarkupId()+"() {\n");
//			script.append(getCallbackScript());
//			script.append("}\n");
//			response.render(JavaScriptHeaderItem.forScript(script.toString(), "refreshfiles"+BrowserPanel.this.getMarkupId()));
//		}
//	}
	
	private IModel<Content> model;
	
	public BrowserPanel(String id) {
		super(id);
		
		//this.resourceType = resourceType;
		
		WebMarkupContainer view = new WebMarkupContainer("templates-view");
		
		view.setOutputMarkupId(true);
		
		view.add(new ListView<IModel<Content>>("template", new PropertyModel<List<IModel<Content>>>(this, "templates")) {
			public void populateItem(final ListItem<IModel<Content>> item) {
				item.add(new TemplateViewPanel("template-view", item.getModelObject()) {
					public void onClick(AjaxRequestTarget target) {
						target.add(view);
						BrowserPanel.this.model = getModel();
					}
					public ViewMode getViewMode() {
						return ViewMode.THUMBNAIL_LARGE;
					}
					//protected String getBodyStyle() {
					//	return BrowserPanel.this.model!=null && BrowserPanel.this.model.getObject().equals(getModel().getObject()) 
					//		? "background: #f4f6f7;"
					//		: "";		
					//}
				});
				
				
				item.add(new AttributeModifier("style",new Model<String>() {
					public String getObject() {
						return BrowserPanel.this.model!=null && BrowserPanel.this.model.getObject().equals(item.getModelObject().getObject()) 
								? "background: #f4f6f7;"
								: "";		
					}
				}));

				
			}
		});
		add(view);
	}

	
	public IModel<Content> getContentModel() {
		return model;
	}
	
	public List<IModel<Content>> getTemplates() {
		List<IModel<Content>> models = new ArrayList<>();
		
		IqlService iqlservice = getDomain().getService(IqlService.class);
		String stm = "template(true) and ishead(true)";
		stm += " and domain(" + String.valueOf(((KbeeUser)getSessionUser()).getDomain().getId()) + ")";
		ResultSet set = iqlservice.execute(stm);
		
		while (set.hasNext()) {
			models.add(new ObjectModel<Content>((Content)set.next().getObject()));
		}
		
		
		return models;
	}
	
	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);

		response.render(JavaScriptHeaderItem.forReference(getApplication().getJavaScriptLibrarySettings().getJQueryReference()));  
		response.render(CssHeaderItem.forReference(BS_CSS));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_0_360));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_361_800 ));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_801_1200));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_1201_1600));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_1601));
		response.render(JavaScriptHeaderItem.forReference(BS_JS));

	}
}
