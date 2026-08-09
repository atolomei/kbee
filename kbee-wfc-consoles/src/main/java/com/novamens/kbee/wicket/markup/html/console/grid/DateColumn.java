package com.novamens.kbee.wicket.markup.html.console.grid;

import java.time.OffsetDateTime;
import java.time.ZoneId;

import com.novamens.datetime.DateTimeService;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;

import com.novamens.indexer.query.SearchResult;

public abstract class DateColumn<T extends com.novamens.security.Auditable> extends GridColumn<SearchResult, String> {

	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DateColumn.class.getName());

	protected OffsetDateTime date;
	private boolean user_visible = true;
	private String contextKey=null;


	public DateColumn(String id, IModel<String> displayModel, String sortProperty) {
		super(id, displayModel, sortProperty);
	}

	public DateColumn(String id, IModel<String> displayModel, String sortProperty, String date_format) {
		super(id, displayModel, sortProperty);
		setDateFormat(date_format);
	}

	@SuppressWarnings("unchecked")
	public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
		try {
			T object = (T) resultmodel.getObject().getObject();

			String date_f=getDateFormat();
			
			cellItem.add(new DatePanel<T>(componentId, object, getOffsetDateTime(object), date_f, isUserVisible(), getNullValue(), getDateClass(resultmodel)));
		}
		catch (Exception e) {
			logger.error(e);
			cellItem.add(new Panel(componentId) {
				private static final long serialVersionUID = 1L;

				@Override
				public boolean isVisible() {
					return false;
				}
			});
		}
	}


	
	public void populateItemExpanded(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
		try {
			T object = (T) resultmodel.getObject().getObject();
			String date_f=getDateFormat();
			cellItem.add( (new Label(componentId, getDateStringModel(getOffsetDateTime(object), date_f, false, getNullValue()))).setEscapeModelStrings(false));
		}
		catch (Exception e) {
			logger.error(e);
			cellItem.add(new Label(componentId, e.getClass().getName()));
			
		}
	}

	
	public IModel<String> getDateStringModel(final OffsetDateTime xd, final String date_format, final boolean show_user, String nullValue) {
		IModel<String> date = new DateFormatModel(xd, show_user, date_format, nullValue);
		return date;
	}
	
	
	
	
	
	
	
	@Override
	public IModel<String> getCellAsString(SearchResult result) {
		//String format used in getLabelModel is fixed, populateItem use custom format implemented in SimpleDatePanel
		final int strFormat = DateTimeService.Full_GMT; // .Month_Day_Year_hh_mm_ss_zzz;
		@SuppressWarnings("unchecked")
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

	@SuppressWarnings("unchecked")
	@Override
	protected IModel<String> getLabelModel(SearchResult object) {
		return new DateFormatModel(getOffsetDateTime((T) object.getObject()), isUserVisible(), getDateFormat(), getNullValue());
	}

	protected abstract OffsetDateTime getOffsetDateTime(T object);

	protected String getDateClass(IModel<SearchResult> resultmodel) {
		return "date-container";
	}

	protected KbeeUser getSessionUser() {
		return (KbeeUser) ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	@Override
	public String getDateFormat() {
		if (super.getDateFormat() != null)
			return super.getDateFormat();
		return super.getGridDateFormat();
	}

	public boolean isUserVisible() {
		return user_visible;
	}

	protected String getNullValue() {
		return "";
	}

	public void setUserVisible(boolean b) {
		user_visible = b;
	}

	@Override
	protected String getContextKey() {
		if(this.contextKey == null)
			return super.getContextKey();
		return contextKey;
	}

	protected void setContextKey(String contextKey) {
		this.contextKey = contextKey;
	}
}
