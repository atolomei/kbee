package kbee.web.dashboard;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.entity.Person;
import com.novamens.kbee.wicket.markup.html.event.GeneralWicketAjaxEvent;
import com.novamens.kbee.wicket.markup.html.event.GeneralWicketEvent;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.DummyBlockPanel;

import kbee.web.error.ErrorPanel;
import kbee.web.help.InlineHelpWebService;

public class DashboardWidgetSimpleWrapperPanel<T> extends DashboardWidgetBasePanel {

	private static final long serialVersionUID = 1L;
	
	private WebMarkupContainer payload;
	private WebMarkupContainer bottom;

	private  WebMarkupContainer help;
	private  WebMarkupContainer main_container;
	private  String help_key;
	
	private  IModel<Person> model;
	
	
	public DashboardWidgetSimpleWrapperPanel(String id, IModel<T> model, String preferences_key) {
		super(id, preferences_key);
		setModel(new ObjectModel<Person>(getPerson()));
	}
	
	public DashboardWidgetSimpleWrapperPanel(String id, IModel<T> model, WebMarkupContainer panel, String preferences_key) {
		super(id, preferences_key);
		payload=panel;
		setModel(new ObjectModel<Person>(getPerson()));
	}
	

	public DashboardWidgetSimpleWrapperPanel(String id, IModel<T> model, WebMarkupContainer panel, IModel<String> title, String preferences_key) {
		super(id,  preferences_key);
		payload=panel;
		setTitle(title);
		setModel(new ObjectModel<Person>(getPerson()));
	}
	

	
	public void setHelpKey(String b) {
		setHelp(true);
		this.help_key=b;
	}
	
	public String getHelpKey() {
		return  help_key;
	}

	
	public void toogleHelp(AjaxRequestTarget target) {

		if (help!=null && !(help instanceof InvisiblePanel)) {
			
			help.setVisible(!help.isVisible());
			main_container.get( "payload").setVisible(!main_container.get( "payload").isVisible());
			target.add(this.main_container);
		}
	}

	
	public void setHelpPanel(WebMarkupContainer p_help) {
		if (!p_help.getId().contentEquals("help"))
			throw new IllegalArgumentException(" id must be help");
		help =p_help;
		if (this.isInitialized())
			main_container.addOrReplace(help);
	}

	
	@Override
	public void onInitialize() {
		super.onInitialize();

		setHelp(true);
		
		main_container = new WebMarkupContainer ("container");
		main_container.setOutputMarkupId(true);
		add(main_container);
		
		if (help==null)
			help=new InvisiblePanel("help");
		main_container.add(help);
		
		
		if(payload==null)
			payload=new InvisiblePanel("payload");
		
		if (!payload.getId().contentEquals("payload"))
			throw new IllegalArgumentException("payload Panel must have id payload. Id is  " + payload.getId() );

		
		main_container.addOrReplace(payload);
		
		//if (bottom==null)
		//	 bottom=new InvisiblePanel("simple-bottom");
		// addOrReplace(bottom);
	}
	
	
					
	//public void setSimpleBottomPanel(WebMarkupContainer p) {
		//if (this.bottom!=null) {
		//	this.bottom=p;
		//	addOrReplace(this.bottom);
	//	}
	//	else
	//		this.bottom=p;
	//}
	
	
	public void setSimplePayloadPanel(WebMarkupContainer p) {
		if (this.payload!=null) {
			this.payload=p;
			addOrReplace(this.payload);
		}
		else
			this.payload=p;
	}
	
	
	public IModel<Person> getModel() {
		return model;
	}

	public void setModel(IModel<Person> model) {
		this.model = model;
	}
	

	@Override
	protected void onClickCollapse(AjaxRequestTarget target) {
		main_container.setVisible(!main_container.isVisible());
		refresh(target);
	}

	@Override
	protected void onTitleClick() {
		fire (new GeneralWicketEvent( this.getClass().getSimpleName()+ "/" + super.getPreferencesKey()));
	}
	
	@Override
	protected WebMarkupContainer getHelpPanel() {
		
		if (getHelpKey()==null)
			return new InvisiblePanel("help");
		
		InlineHelpWebService se = ServiceLocator.getService(InlineHelpWebService.class);
		WebMarkupContainer pa = se.getPanel("help", getLocale(), getHelpKey() );
		if (pa!=null) return pa;
		return new ErrorPanel("help", new Model<String>( getHelpKey()  ));
	}
	
	

	@Override
	protected void onHelp(AjaxRequestTarget target) {
		
		if (help==null || help instanceof InvisiblePanel) {
			help= getHelpPanel();
			help.setVisible(false);
			main_container.addOrReplace(help);
		}
		toogleHelp(target);
	}
	
	
	

}
