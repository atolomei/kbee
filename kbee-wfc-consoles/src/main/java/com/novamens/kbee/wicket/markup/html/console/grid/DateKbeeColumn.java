package com.novamens.kbee.wicket.markup.html.console.grid;

import com.novamens.datetime.DateTimeService;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.danekja.java.util.function.serializable.SerializableFunction;
import org.danekja.java.util.function.serializable.SerializableSupplier;

import java.time.OffsetDateTime;
import java.time.ZoneId;


public class DateKbeeColumn<T> extends KbeeGridColumn<T> {

	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DateKbeeColumn.class.getName());
	
	
	private SerializableFunction<T, OffsetDateTime> dateValueResolver;

	private String dateFormat;

	//We need to use lambda so format is automatically updated
	private SerializableSupplier<String> dateFormatResolver;

	//String format used in getLabelModel is fixed, populateItem use custom format implemented in SimpleDatePanel
	static private final int strFormat = DateTimeService.Month_Day_Year_hh_mm_ss_zzz;



	//dateFormatResolver has to be a lambda 
	// because calling "getBrowser().getPanel(GridPanel.class).getDateFormat()" from getColumns() Console method, gives StackOverflow
	
	public DateKbeeColumn(String id, IModel<String> displayModel, SerializableFunction<T, OffsetDateTime> dateValueResolver, SerializableSupplier<String> dateFormatResolver) {
		super(id, displayModel);
		this.dateValueResolver = dateValueResolver;
		this.dateFormatResolver = dateFormatResolver;
		this.dateFormat = null;

	}

	public DateKbeeColumn(String id, IModel<String> displayModel, String sortProperty, SerializableFunction<T, OffsetDateTime> dateValueResolver, SerializableSupplier<String> dateFormatResolver) {
		super(id, displayModel, sortProperty);
		this.dateValueResolver = dateValueResolver;
		this.dateFormatResolver = dateFormatResolver;
		this.dateFormat = null;
	}

	@SuppressWarnings("unchecked")
	public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
		try {
			T object = (T) resultmodel.getObject().getObject();
			cellItem.add(new SimpleDatePanel<T>(componentId, object, getValue(object), getDateFormat(), getNullValue()));
		}
		catch (NullPointerException e) {
			logger.error(e, getId() + "column error");
			cellItem.add(new InvisiblePanel(componentId));
		}
	}

	protected OffsetDateTime getValue(T object){
		return dateValueResolver.apply(object);
	}

	@Override
	protected String getValueAsString(T object) {

		DateTimeService service = ServiceLocator.getService(DateTimeService.class);
		OffsetDateTime offsetDateTime = getValue(object);

		if(offsetDateTime == null)
			return "";

		User user = getSessionUser();
		String zid = service.getMapZoneIds().get(user.getTimeZone());
		if (zid==null) {
			logger.warn("ZoneId: " + user.getTimeZone() + " is not in the database");
			zid= ZoneId.systemDefault().getId();
		}

		String strDate = service.format(offsetDateTime, zid, user.getLocale(), strFormat);

		return strDate;
	}

	
	protected String getExpandedValueAsString(T object) {
		return getValueAsString(object);
	}

	
	protected KbeeUser getSessionUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	@Override
	public String getDateFormat() {
		if(this.dateFormat == null) {
			String resolvedFormat = dateFormatResolver.get();
			if(resolvedFormat != null)
				return resolvedFormat;
			return super.getDateFormat();
		}
		return dateFormat;
	}

	@Override
	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}


	protected String getNullValue() {
		return "";
	}


}
