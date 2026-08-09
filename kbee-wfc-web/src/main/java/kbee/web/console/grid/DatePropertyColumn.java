package kbee.web.console.grid;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import java.util.Locale;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.datetime.DateTimeService;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;


public class DatePropertyColumn<T, S> extends PropertyColumn<T,S> {

	private static final long serialVersionUID = -2166403247471605319L;
	
	private DateTimeFormatter default_converter;
	private DateTimeFormatter export_converter;
	
	private ZoneId  zone;
	private boolean is_elapsed_mode = true;  // for "ago"
	private Locale locale;


				
	protected Locale getLocale() {
		return locale;
	}
	
	
	protected ZoneId getZoneId() {
		return zone;
	}
	
	
	public DatePropertyColumn(IModel<String> displayModel, String propertyExpression,  ZoneId zone,  Locale locale) {
		this (displayModel, propertyExpression, zone,  locale, true);
	}
	
	
	
	public DatePropertyColumn(IModel<String> displayModel, String propertyExpression,  ZoneId zone,  Locale locale, boolean is_elapsed_mode) {
		super(displayModel, propertyExpression);
		this.is_elapsed_mode=is_elapsed_mode;
		this.locale=locale;
		this.zone=zone;
		
	}

	public void detach() {
		super.detach();
		this.default_converter=null;
	}
	

	public IModel<String> getCellAsString(IModel<T> result) {
		OffsetDateTime date =(OffsetDateTime) getDataModel(result).getObject();
		ZonedDateTime zd = ZonedDateTime.ofInstant(date.toInstant(), this.zone);
		return new Model<String>(getExportDatetimeConverter().format(zd));
	}

	
	@Override
	public void populateItem(final Item<ICellPopulator<T>> item, final String componentId, final IModel<T> rowModel) {

		if (this.is_elapsed_mode) {
			DateTimeService service = ServiceLocator.getService(DateTimeService.class);
			String tst = service.timeElapsed((OffsetDateTime) getDataModel(rowModel).getObject(), this.zone,  this.locale, DateTimeService.DATE_COLlOQUIAL_AGO, "ago");
			item.add((new Label(componentId, new Model<String>(tst))).setEscapeModelStrings(false));
		}
		else {
			OffsetDateTime date =(OffsetDateTime) getDataModel(rowModel).getObject();
			ZonedDateTime zd = ZonedDateTime.ofInstant( date.toInstant(), this.zone);
			item.add((new Label(componentId, new Model<String>(getDatetimeConverter().format(zd)))).setEscapeModelStrings(false));
		}
    	
		item.add(new AttributeModifier("class", "col-xs-2"));
    }
	
	
	protected DateTimeFormatter getDatetimeConverter() {
		if ( default_converter!=null)
			return  default_converter;
		default_converter = DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm:ss a", this.locale!=null?this.locale:getSessionUser().getLocale());
		return  default_converter;
		
	}
	
	protected DateTimeFormatter getExportDatetimeConverter() {
		if ( export_converter!=null)
			return  export_converter;
		export_converter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss Z", this.locale!=null?this.locale:getSessionUser().getLocale());
		return  export_converter;
	}
	
	protected KbeeUser getSessionUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	

}
