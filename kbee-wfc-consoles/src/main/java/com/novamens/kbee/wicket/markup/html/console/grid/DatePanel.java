package com.novamens.kbee.wicket.markup.html.console.grid;

import java.time.OffsetDateTime;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;


import com.novamens.datetime.DateTimeService;


/**
 * @param <T>
 */
public class DatePanel<T extends com.novamens.security.Auditable> extends Panel {
	
	private static final long serialVersionUID = 1L;
	

	@SuppressWarnings("unused")
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DatePanel.class.getName());
	
	public DatePanel(String id, T object) {
		this(id, object, object.getLastModifiedOffsetDateTime(), DateTimeService.COLlOQUIAL_AGO_LABEL, true, "", "date-container");
	}

	public DatePanel(String id, T object, final OffsetDateTime xd, final String date_format, final boolean show_user) {
		this(id, object, xd, date_format, show_user, "", "date-container");
	}
	
	
	public IModel<String> getDateStringModel(final OffsetDateTime xd, final String date_format, final boolean show_user, String nullValue) {
		IModel<String> date = new DateFormatModel(xd, show_user, date_format, nullValue);
		return date;
	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();

		  
		 
		
		Label da=new Label("date", this.date_str);
		da.setEscapeModelStrings(false);
		
		WebMarkupContainer dc = new WebMarkupContainer("date-container");
		dc.add(da);
		add(dc);
		
		dc.add(new AttributeModifier("class", this.dateclass));
		
		WebMarkupContainer uc = new WebMarkupContainer("user-container");
		uc.setVisible(this.show_user);
		add(uc);
		
		if (this.show_user) {
			uc.add(new Label("username", this.username));
			uc.add((new Label("by", new StringResourceModel("by", DatePanel.this,null))));
		}
		else {
			uc.add((new Label("by", "")).setVisible(false));
			uc.add((new Label("username", "")).setVisible(false));
		}
	}
	
	private IModel<String> date_str;
	private boolean show_user;
	private String dateclass;
	private IModel<String> username;
	
	
	public DatePanel(String id, T object, final String dateStr, final String dateclass) {
		super(id);
		this.show_user=false;
		this.dateclass=dateclass;
		username = new Model<String>(object.getLastModifiedUser().getFirstLastName());
		this.date_str=new Model<String>(dateStr!=null?dateStr:"");
	}
	
	public DatePanel(String id, T object, final OffsetDateTime xd, final String date_format, final boolean show_user, String nullValue, String dateclass) {
		super(id);
		this.show_user=show_user;
		this.dateclass=dateclass;
		username = new Model<String>((object.getLastModifiedUser()!=null? object.getLastModifiedUser().getFirstLastName():""));
		this.date_str=getDateStringModel(xd, date_format, show_user, nullValue);
	}

	protected String getDateClass() {
		return "date-container";
	}


}
