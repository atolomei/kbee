package com.novamens.kbee.wicket.markup.html.console.grid;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.datetime.DateTimeService;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

public class SimpleDatePanel<T> extends Panel {
			
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = LogManager.getLogger(SimpleDatePanel.class.getName());

	private String null_value;
	
	public SimpleDatePanel(String id, T object, final OffsetDateTime xd, final String date_format) {
		this (id, object, xd, date_format, "");
	}
	
	public SimpleDatePanel(String id, T object, final OffsetDateTime xd, final String date_format, String nullValue) {
		super(id);
		
		setNullValue(nullValue);
		
		IModel<String> date = new Model<String>() {
			private static final long serialVersionUID = 1L;
			public String getObject() {
				DateTimeService service = ServiceLocator.getService(DateTimeService.class);
				User user = getSessionUser();
				String zid = service.getMapZoneIds().get(user.getTimeZone());
				if (zid==null) {
						logger.warn("ZoneId: " + user.getTimeZone() + " is not in the database");
						zid=ZoneId.systemDefault().getId();
				}
				if (xd!=null) {
						ZonedDateTime zd = ZonedDateTime.ofInstant(xd.toInstant(), ZoneId.of(zid));
						String tst;
						if (date_format==null) 	
							tst = service.timeElapsed(zd, ZoneId.of(zid), user.getLocale(), DateTimeService.DATE_COLlOQUIAL_AGO, "ago");
						else if (date_format.equals(DateTimeService.COLlOQUIAL_AGO_LABEL))
							tst = service.timeElapsed(zd, ZoneId.of(zid), user.getLocale(), DateTimeService.DATE_COLlOQUIAL_AGO, "ago");
						else if (date_format.equals(DateTimeService.COLlOQUIAL_LABEL))
							tst = service.timeElapsed(zd, ZoneId.of(zid), user.getLocale(), DateTimeService.DATE_COLlOQUIAL, null);
						else if (date_format.equals(DateTimeService.MONTH_DAY_YEAR_LABEL))
							tst = service.format(xd, zid, user.getLocale(), DateTimeService.Month_Day_Year);
						else if (date_format.equals(DateTimeService.TIMESTAMP_LABEL))
							tst = service.format(xd, zid, user.getLocale(), DateTimeService.Month_Day_Year_hh_mm_ss_zzz);
						else
							tst = service.format(xd, zid, user.getLocale(), DateTimeService.Month_Day_Year_hh_mm);
						return tst;
				}
				return nullValue();
			}
		};
		
		add((new Label("date", date)).setEscapeModelStrings(false));
	}
	
	
	
	public void setNullValue(String null_value) {
		this.null_value=null_value;
	}
	
	protected String nullValue() {
		return null_value;
	}

	protected KbeeUser getSessionUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

}
