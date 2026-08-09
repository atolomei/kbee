package kbee.web.user;


import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.hibernate.engine.jdbc.internal.BasicFormatterImpl;

import com.novamens.content.entity.Person;
import com.novamens.content.properties.Property;

import com.novamens.kbee.content.user.UserPropertyService;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.ajax.WorkingIndicatorAjaxLinkV5;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;


public class UserQueryHistoryPanel extends Panel {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(UserQueryHistoryPanel.class.getName());
	 

	private static final long serialVersionUID = -7851293581479407066L;
	
	
	public UserQueryHistoryPanel(String id, String key, IModel<Person> model) {
		super(id, model);
		 setKey(key);
		setOutputMarkupId(true);
		
		
	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();

		add(new ListView<com.novamens.content.properties.Property>("properties", 
				new PropertyModel<List<com.novamens.content.properties.Property>>(this, "properties")) {
			
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<Property> item) {
				
				WorkingIndicatorAjaxLinkV5<Property> link = new WorkingIndicatorAjaxLinkV5<Property>("property-link", item.getModel()) {
						private static final long serialVersionUID = 1L;
						public void onClick(AjaxRequestTarget target) {
							try { 
								if (getModel().getObject().getValue()!=null) {
									UserQueryHistoryPanel.this.apply(target, getModel());
								}
							} catch (Exception e) {
								logger.error(e);
							}
						}
						
						
						protected String getWorkingLabel() {
							return "loading";
						}
					};
										
									
					WorkingIndicatorAjaxLinkV5<Property> edit = new WorkingIndicatorAjaxLinkV5<Property>("edit", item.getModel()) {
						private static final long serialVersionUID = 1L;
						public void onClick(AjaxRequestTarget target) {
							try { 
								logger.info(" apply " + getModel().getObject().getValue().toString());
								if (getModel().getObject().getValue()!=null) {
									UserQueryHistoryPanel.this.setQueryValue(target, getModel());
								}
							} catch (Exception e) {
								logger.error(e);
							}
						}
					};

					
					
					WorkingIndicatorAjaxLinkV5<Property> remove = new WorkingIndicatorAjaxLinkV5<Property>("remove", item.getModel()) {
						private static final long serialVersionUID = 1L;
						public void onClick(AjaxRequestTarget target) {
							try {	
							logger.info(" remove " + getModel().getObject().getValue().toString());
							getSessionUser().getService(UserPropertyService.class).removeProperty(getModel().getObject());
							target.add(UserQueryHistoryPanel.this);
							} catch (Exception e) {
								logger.error(e);
							}
						}
					};
					
					
					String qe=item.getModelObject().getValue().toString();
					
					if (qe!=null & qe.length()>320)
						qe=qe.substring(0, 320)+"...";
					
					Label la=new Label("property-label", qe);
					la.setEscapeModelStrings(false);
					
					link.add(la);
					item.add(link);
					item.add(remove);
					item.add(edit);
			};
			
		});
		
		AjaxLink<Void> link = new AjaxLink<Void>("remove-all") {
			private static final long serialVersionUID = 1L;
			public void onClick(AjaxRequestTarget target) {
				try {
					logger.info(" remove all");
					getSessionUser().getService(UserPropertyService.class).removePropertiesSet(getKey());
					target.add(UserQueryHistoryPanel.this);
				} catch (Exception e) {
					logger.error(e);
				}
			}

			@Override
			public boolean isVisible() {
				try {
					return !getSessionUser().getService(UserPropertyService.class).getPropertiesSet( getKey(), 30).isEmpty();
				} catch (Exception e) {
					return false;
				}
				
			}
		};

		add(link);
	}

	
	BasicFormatterImpl sqlf;
	
	private BasicFormatterImpl getSQLFormatter() {
		
		if (sqlf!=null)
			return sqlf ;
		
		sqlf= new BasicFormatterImpl();
		return sqlf; 
	}
	
	protected IModel<String> format(String sql) {
		
		String formattedSQL = getSQLFormatter().format(sql);
		
		/**
		 * 
		 * <div class="keyword">
		 * </div>
		 * 
		 */
		
		return new Model<String>(formattedSQL);
		
	}



	protected void setQueryValue(AjaxRequestTarget target, IModel<Property> model) {
	}

	
		
	protected void apply(AjaxRequestTarget target, IModel<Property> model) {
	}



	@Override
	public void onDetach() {
		super.onDetach();
		
		sqlf=null;
		
	}
		
	
	String key;
	
	public void setKey(String key) {
		this.key=key;
	}
	
	public String getKey() {
		return key;
	}
		
	public List<Property> getProperties() {
		List<Property> list = getSessionUser().getService(UserPropertyService.class).getPropertiesSet(getKey(), 30);
		if (list==null)
			return new ArrayList<Property>();
		return list;
	}
	
	
	
	protected KbeeUser getSessionUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
}
