package kbee.web.idoc;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.string.StringValue;

import com.novamens.content.base.Content;
import com.novamens.content.document.IDoc;
import com.novamens.content.entity.Person;
import com.novamens.content.form.EFormAccessLevel;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormData;
import com.novamens.content.service.TokenService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.web.console.markup.ErrorPanel;
import com.novamens.content.web.idoc.markup.ContentPageV6;
import com.novamens.dom.Json;
import com.novamens.kbee.content.workflow.KbeeTaskForm;
import com.novamens.kbee.wicket.markup.html.console.panel.VerticalLayout;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.tabs.AbstractTabKB;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.DummyBlockPanel;

import kbee.web.content.eform.ContentFormViewer;
import kbee.web.nav.ErrorNavigationBar;
import kbee.web.searcher.page.SearcherResultsPage;
import kbee.web.searcher.panel.SearcherDetailHeaderPanel;



/**
 * 
 * con cuenta kbee - con Persona kbee (todas las cuentas kbee tiene Persona) -> -> banner "cree su dominio kbee"
 * sin cuenta kbee - con Persona kbee -> sólo puede ver la shared page -> banner cree su cuenta kbee para acceder a los contenidos del dominio -> banner "cree su dominio kbee"
 * sin cuenta kbee - sin Persona kbee -> sólo puede ver la shared page -> banner "cree su dominio kbee"
 * 
 * 
 * -XX:HotswapAgent=core
-Xbootclasspath/a:C:\Users\atolo\eclipse-workspace\idoc-config\src\main\resources
-Dlog4j.configurationFile=C:\Users\atolo\eclipse-workspace\idoc-config\src\main\resources\log4j2-dev.xml 
-Dcom.novamens.kbee.jettyProfilesDir=C:\Users\atolo\eclipse-workspace\idoc-config\src\main\resources\jprofiles\ 
-Dcom.novamens.kbee.jettyProfiles=jetty-threadpool.xml;jetty.xml;jetty-http.xml
-Dhttps.protocols=TLSv1,TLSv1.1,TLSv1.2
-Djetty.port=8080
-Xmx9000M  
-DupdateSolrSchemas
-DupdateDBSchema


SharedPage -> solamente eForms "General"  
No se publican los eForms "Internal Use"

 *
 */
@SuppressWarnings("serial")
public class SharedPage extends ContentPageV6<Content> {
	
	private static final long serialVersionUID = 1L;
				
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SharedPage.class.getName());
	
	private IModel<Person> personmodel;

	private static final ResourceReference KBEE_SEARCHER_CSS = new CssResourceReference(SearcherResultsPage.class, "searcher.css");
	
//	private static int PASSWORD_NOT_REQUIRED = 0;
//	private static int PASSWORD_REQUIRED = 1;
//	private static int PASSWORD_VALIDATED = 2;
	
	//private int status = PASSWORD_NOT_REQUIRED;
	
	private WebMarkupContainer tp;
	private WebMarkupContainer cp;
	
	private String password=null;


	long sleeper = 500;
	
	/**
	 * 
	 */
	
	public SharedPage() {
		setLogVisit(false);
	}
	

	public SharedPage(IModel<Content> model) {
		 setModel(model);
		 setLogVisit(false);
	}
	
	

	public SharedPage(PageParameters parameters) {
		
		setLogVisit(false);
		
		Content idoc = getContent(parameters);
		
		if (idoc!=null) 
			 setModel(new ObjectModel<Content>(idoc));
		
		Person person = getPerson(parameters);
		
		if (person!=null) 
			setPerson(person);
		
		
		String pass = getPassword(parameters);
		
		if (pass!=null) 
			setPassword(pass);
		

		
		
		
		
		 		
	}
	

	SharedPagePasswordPanel<Content>  pwdpanel;
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		setLogVisit(false);
		sleeper = 500;
		
		/** 
		 * if there is no content -> error 
		 * */

		this.cp = new WebMarkupContainer("canvas-panel");
		addOrReplace(cp);
		cp.setOutputMarkupId(true);

		
		if (getModel()==null ) {
			logger.debug("model is null");
			setTopNavigation(new ErrorNavigationBar<Content>("navigation"));
			setMenu(new InvisiblePanel("menu"));
			addOrReplace(new InvisiblePanel("header"));
			cp.addOrReplace(new InvisiblePanel("password-panel"));
			
			WebMarkupContainer tp = new WebMarkupContainer("tabs-panel");
			cp.addOrReplace(tp);
			tp.add( new ErrorPanel("tabs", null, getLabel("not-found")));
			return;
		}


		
		
		addTabsPanel();
		
		if (getPassword()!=null) {
			
			pwdpanel  = new SharedPagePasswordPanel<Content>("password-panel", getModel()) {
				@Override
				protected void onClick(AjaxRequestTarget target, String pwd) {
					
					if ( pwd!=null && getPassword().equals(pwd) ) {
						//status = PASSWORD_VALIDATED;
						tp.setVisible(true);
						pwdpanel.setVisible(false);
						target.add(cp);
					}
					else {
						
						//status = PASSWORD_REQUIRED;
						
						try {
							Thread.sleep(sleeper);
							sleeper += 500;
							if (sleeper>5000)
								sleeper=5000;
						} catch (InterruptedException e) {
							
						}
						pwdpanel.setError("password invalid");
						target.add(cp);
					}
				}
				
			};
			
			cp.addOrReplace(pwdpanel);
			
			//status = PASSWORD_REQUIRED;
			tp.setVisible(false);
			pwdpanel.setVisible(true);
		}
		
		else {
			//status = PASSWORD_NOT_REQUIRED;
			cp.add(new InvisiblePanel("password-panel"));
			tp.setVisible(true);
		}
	}

	
	
	
	private void addTabsPanel() {

		this.tp = new WebMarkupContainer("tabs-panel");
		cp.addOrReplace(tp);
		
		logger.debug( getModel().getObject()!=null? getModel().getObject().getIdInfo():"null");
		
		setPageTitle(new Model<String>(getModel().getObject().getTitle()));
		
		if (getSessionUser()!=null)
			setTopNavigation(new SharedContentTopBar("navigation", getModel()));
		else 
			setTopNavigation(new ErrorNavigationBar<IDoc>("navigation"));
			
		
		setMenu(new InvisiblePanel("menu"));
			
		List<ITab> tabs = new ArrayList<ITab>();
			
		for (EForm eform : getForms()) {
			try {
				if (!isEmpty(eform)) {
					tabs.add(new AbstractTabKB(new Model<String>(eform.getDisplayName()), eform.getName()) {
						@Override
						public Panel getPanel(String panelId) {
							try {
								return new ContentFormViewer<Content>(panelId, getModel(), eform, true);
							} catch (Exception e) {
								logger.error(e);
								return new kbee.web.error.ErrorPanel(panelId,e);
							}
						}
					});
				}
			}
			catch (Throwable e) {
				logger.error(e);
				IModel<String> title = eform!=null? new Model<String>(eform.getDisplayName()): new Model<String>("null");
				 tabs.add(new AbstractTabKB(title, title.getObject()) {
					@Override
					public Panel getPanel(String panelId) {
						String message = e.getCause()!=null ? e.getCause().getMessage() : e.getMessage();
						return new ErrorPanel(panelId, new Model<String>("Form Error"), new Model<String>(message));
					}
				});
			}
		}
			
			VerticalLayout<ITab> panel = new VerticalLayout<ITab>("tabs", "content", tabs);
			panel.setTitle(new StringResourceModel("sections", this, null));
			panel.setSections(VerticalLayout.COLS_9X3);
			
			
			try {
				SearcherDetailHeaderPanel<Content> pa=new SearcherDetailHeaderPanel<Content>("content-top-panel", getModel(), getSiteModel(), null, false);
				pa.setStandAlonePage(true);
				panel.setContentTopPanel(pa);
			} 
			catch (Exception e) {
				logger.error(e);
				panel.setContentTopPanel(new kbee.web.error.ErrorPanel("content-top-panel", e));
			}
		

			panel.setContentPanelCss("main-area detail text");
			panel.setMenuItemFactory(getMenuItems());
			
			tp.add(panel);
		
	}
	

	@Override
	protected Panel getFooter() {
		return  new DummyBlockPanel("console-footer");
	}
	
	protected boolean isFooterRequired() {
		return true;
	}
	
	
	
	
	public void setPassword(String password) {
		this.password=password; 
	}
				
	public String getPassword() {
		return this.password; 
	}
	
	
	public void setPerson(Person person) {
		personmodel = person!=null ? new ObjectModel<Person>(person) : null; 
	}
	
	public IModel<Person> getPersonModel() {
		return personmodel;
	}
	
	protected boolean isEmpty(EForm eform) {
		EFormData data = getModelObject().getFormData(eform);
		return data.isEmpty();
	}
	
	@Override
	protected boolean hasLateralMenu() {
		return false;
	}
	
	protected boolean isConsole() {
		return true;
	}
	
	protected List<EForm> getForms() {
		List<EForm> forms = new ArrayList<EForm>();
		for (EForm form : getModelObject().getContentTemplate().getForms()) {
			if (form.getFormAccessLevel().equals(EFormAccessLevel.GENERAL)) {
				forms.add(new KbeeTaskForm(form));
			}
		}
		return forms;
	}
	
	protected Content getContent(PageParameters parameters) {
		Content content = null;		
		StringValue token = parameters.get("token");
		if (!token.isNull() && !token.isEmpty()) { 
			content = (Content) getContentDao().findContentByToken(token.toString());
		}	
		return content;
	}
	
	protected Person getPerson(PageParameters parameters) {
		Person person = null;		
		StringValue token = parameters.get("token");
		if (!token.isNull() && !token.isEmpty()) {
			Json data = ServiceLocator.getService(TokenService.class).decode(token.toString());
			if (data!=null && data.get("person")!=null) {
				person = (Person)getContentDao().findMemberById(Long.valueOf((String)data.get("person")));
			}
		}
		return person;
	}
	
	
	protected String getPassword(PageParameters parameters) {
		String password = null;		
		StringValue token = parameters.get("token");
		if (!token.isNull() && !token.isEmpty()) {
			Json data = ServiceLocator.getService(TokenService.class).decode(token.toString());
			if (data!=null && data.get("password")!=null) {
				password = (String)data.get("password");
			}
		}
		return password;
	}
	
	
	public User getUser() {
		if (personmodel==null) return null;
		UserProfile profile = personmodel.getObject().getProfile(UserProfile.class);
		if (profile==null) return null;
		User user = profile.getUser();
		return user;
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(KBEE_SEARCHER_CSS));
	}
	
	private List<MenuItemFactory<Panel>> getMenuItems() {
		List<MenuItemFactory<Panel>> list = new ArrayList<MenuItemFactory<Panel>>();
		return list;
	}
	
//	private String getRegistrationUrl(Person person) {
//		KbeeJson data = new KbeeJson();
//		if (person==null) return null;
//		data.put("id", String.valueOf(person.getId()));
//		data.put("date", person.getCreationOffsetDateTime().toString());
//		data.put("domain", String.valueOf(person.getDomain().getId()));
//		String url = person.getService(UrlService.class).getServerUrl() + "/registrationinit/" + ServiceLocator.getService(TokenService.class).getToken(data);
//		return url;
//	}
}



/**
try {
	
	BannerPanel banner = new BannerPanel("content-bottom-panel", getPersonModel());
	banner.setTitle(new StringResourceModel("banner-title", this, null));
	banner.setText (new StringResourceModel("banner", this, null));
	banner.setBck(true);
	banner.setTextHTMLStyle("float: left;    width: 100%;    text-align: center;    font-size: 1.75em;    padding: 0 10%;        \r\n"
			+ "    font-weight: 800;    letter-spacing: -0.005em;    text-transform: none;    font-family: Montserrat, Lato,sans-serif;    -webkit-text-fill-color: transparent;\r\n"
			+ "    background-image: linear-gradient(160deg,#000000, #373e30,#94ab2f,#c4d86b);    text-align: center;    bottom: 44%;    -webkit-background-clip: text;    background-clip: text;      ");
	
	banner.setLink (ServiceLocator.getService(BrandingService.class).getApplicationURL());
	panel.setContentBottomPanel(banner);
} 
catch (Exception e) {
	logger.error(e);
	panel.setContentBottomPanel(new kbee.web.error.ErrorPanel("content-bottom-panel"));
}
**/

/**
if ( getPersonModel()!=null && getUser()==null) {
	try {
		BannerPanel banner = new BannerPanel("header-bottom-panel", getPersonModel());
		banner.setHTMLStyle("margin:3em 0; float:left; width:100%; background: #f4f5f6;");
		banner.setBck(false);
		
		banner.setTitle (new Model<String>("registrate"));
		banner.setText (new StringResourceModel("banner-account", this, null));
		
		banner.setLink (getRegistrationUrl(getPersonModel().getObject()));
		panel.setHeaderBottomPanel(banner);
	} 
	catch (Exception e) {
		logger.error(e);
		panel.setHeaderBottomPanel( new kbee.web.error.ErrorPanel("header-bottom-panel"));
	}
}
**/

/**
private class PasswordFragment extends Fragment {

		private static final long serialVersionUID = 1L;
			
		
		private Form<?> form;
	
			
		private String pwd=null;
		
		public String getPwd() {
			return pwd;
		}
	
		public void setPwd(String pwd) {
			this.pwd = pwd;
		}
	
		public PasswordFragment(String id) {
			super(id, "password-fragment", SharedPage.this);
		}
		
		public void onInitialize() {
			super.onInitialize();
				
			form = new com.novamens.wicket.markup.html.form.Form<Void>("form", Disposition.VERTICAL);
			add(form);
		
			form.add(new PasswordField("password", new PropertyModel<String>(this, "pwd")));
			
			
			AlertPanel<Void> pa=new AlertPanel<Void>("alert-text",AlertPanel.INFO,  null, 
					getLabel("alert-title"), getLabel("alert-text"));
			
			pa.setIcon("fa-duotone fa-envelope");
			addOrReplace(pa);
			
				
			AjaxLink<Void> sb = new AjaxLink<Void>("apply") {
				@Override
				public void onClick(AjaxRequestTarget target) {
					
					logger.debug( "pwd -> " + getPwd() );
					logger.debug( "password -> " + getPassword());
					
					if ( getPwd()!=null && getPassword()!=null && getPassword().equals(getPwd())) {
						status=PASSWORD_VALIDATED;
					}
					
					
					target.add(tp);
				}
			};
			
			form.add(sb);
	
				
		}
		
	
}
*/
