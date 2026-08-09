package com.novamens.kbee.wicket.markup.html.console.grid;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;

import com.novamens.datetime.DateTimeService;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

@SuppressWarnings("serial")
public abstract class SimpleDateColumn<T> extends GridColumn<SearchResult, String> {
	private static final long serialVersionUID = 1L;
											
	
	private static org.apache.logging.log4j.Logger logger = LogManager.getLogger(DateColumn.class.getName());
	
	
	public  SimpleDateColumn(String id, IModel<String> displayModel, String sortProperty) {
		super(id, displayModel, sortProperty);
	}
	
	public SimpleDateColumn(String id, IModel<String> displayModel, String sortProperty, String date_format) {
		super(id, displayModel, sortProperty);
		setDateFormat(date_format);
	}
	
	@SuppressWarnings("unchecked")
	public void populateItemExpanded(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
		try {
			T object = (T) resultmodel.getObject().getObject();		
			cellItem.add(new Label(componentId, getStrDateAsString(object, getOffsetDateTime(object), getDateFormat(), getNullValue())  ) );
		} 
		catch (NullPointerException e) {
			if (logger.isDebugEnabled()) {
				logger.error(getId() + "column error", e);
			}
			else {
				logger.error(getId() + "column error");
			}
			cellItem.add(new Panel(componentId) {
				@Override
				public boolean isVisible() {
					return false;
				}
			});
		}
	}
	
	
	public String getStrDateAsString(T object, final OffsetDateTime xd, final String date_format, String nullValue) {
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
		return "";
	}
	

	
	
	
	@SuppressWarnings("unchecked")
	public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
		try {
			T object = (T) resultmodel.getObject().getObject();		
			cellItem.add(new SimpleDatePanel<T>(componentId, object, getOffsetDateTime(object), getDateFormat(), getNullValue()));
		} 
		catch (NullPointerException e) {
			if (logger.isDebugEnabled()) {
				logger.error(getId() + "column error", e);
			}
			else {
				logger.error(getId() + "column error");
			}
			cellItem.add(new Panel(componentId) {
				@Override
				public boolean isVisible() {
					return false;
				}
			});
		}
	}
	
	protected abstract OffsetDateTime getOffsetDateTime(T object);

	protected String getNullValue() {
		return "";
	}
	
	
	@Override
	public IModel<String> getCellAsString(SearchResult result) {

		//String format used in getLabelModel is fixed, populateItem use custom format implemented in SimpleDatePanel
		
		final int strFormat = DateTimeService.Full_GMT;
		OffsetDateTime offsetDateTime = getOffsetDateTime((T) result.getObject());

		if(offsetDateTime == null)
			return ()-> "";

		DateTimeService service = ServiceLocator.getService(DateTimeService.class);


		User user = getSessionUser();
		String zid = service.getMapZoneIds().get(user.getTimeZone());
		if (zid == null) {
			logger.warn("ZoneId: " + user.getTimeZone() + " is not in the database");
			zid = ZoneId.systemDefault().getId();
		}

		String strDate = service.format(offsetDateTime, zid, user.getLocale(), strFormat);
		return ()->strDate;
	}
	
	protected KbeeUser getSessionUser() {
		return (KbeeUser) ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

}
