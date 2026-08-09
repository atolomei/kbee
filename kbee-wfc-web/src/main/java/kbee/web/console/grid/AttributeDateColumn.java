package kbee.web.console.grid;



import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import com.novamens.content.model.*;
import com.novamens.content.user.UserService;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.wicket.markup.html.console.grid.DateColumn;
import com.novamens.kbee.wicket.markup.html.console.grid.DatePanel;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;


/**
 * 
 * AttributeDateColumn are displayed as 
 * mm dd yyyy
 * or 
 * dd mm yyy
 * 
 * in both cases the in their original GMT. 
 * Normally this is the Domain GMT (AttributeEditor)
 *
 */
public class AttributeDateColumn extends DateColumn<Classificable> {
			
	private static final long serialVersionUID = 1L;
	
	 
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AttributeDateColumn.class.getName());

	private IModel<Attribute> model;
	private String console_name;
	
	
	/***
	 * 
	 * 
	 */
	
	static final Locale LOCALE_ES = new Locale("es");
	static final  DateTimeFormatter df_eng = DateTimeFormatter.ofPattern ( "MMM d yyyy", 	Locale.ENGLISH); // 17 month day year gmt
	static final  DateTimeFormatter df_es  = DateTimeFormatter.ofPattern ( "d MMM yyyy",    LOCALE_ES ); // 17 month day year gmt
	
	
	public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
		try {
		
			Classificable object = (Classificable) resultmodel.getObject().getObject();
			// String date_f=getDateFormat();
			String dateStr;
			try {
				OffsetDateTime odt=getOffsetDateTime(object);
				if (odt!=null) {
					if (getSessionUser().getLocale().getLanguage().equals("es"))
						dateStr = df_es.format(odt);
					else {
						dateStr = df_eng.format(odt);
					}
				}
				else
					dateStr="";
				// dateStr = ServiceLocator.getService(DateTimeService.class).getDomainInOriginalGMTDateDisplayString( getOffsetDateTime(object), getSessionUser().getLocale());
				
			} catch (Exception e) {
				logger.error(e);
				dateStr = e.getClass().getSimpleName();
			}
			cellItem.add(new DatePanel<Classificable>(componentId, object, dateStr, getDateClass(resultmodel)));
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
	
	
	@Override
	public IModel<String> getCellAsString(SearchResult result) {
		
		//String format used in getLabelModel is fixed, populateItem use custom format implemented in SimpleDatePanel
		final int strFormat = DateTimeService.Full_GMT; // .Month_Day_Year_hh_mm_ss_zzz;
		
		OffsetDateTime offsetDateTime = getOffsetDateTime((Classificable) result.getObject());

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


	@Override
	protected IModel<String> getLabelModel(SearchResult object) {
		try {
			 
		String value;
			
		OffsetDateTime odt=getOffsetDateTime( (Classificable) object.getObject());
		if (odt!=null) {
			if (getSessionUser().getLocale().getLanguage().equals("es"))
				value = df_es.format(odt);
			else {
				value = df_eng.format(odt);
			}
		}
		else
			value ="";
			
			
		 //ServiceLocator.getService(DateTimeService.class).getDomainInOriginalGMTDateDisplayString( getOffsetDateTime( (Classificable) object.getObject()), getSessionUser().getLocale());
		
		
		return new Model<String>(value);
		} catch ( Exception e) {
			logger.error(e);
			return new Model<String>(e.getClass().getSimpleName());
		}
	}

	/**
	 * @param model
	 * @param console_name
	 * @param isPreferred
	 */
	
	public AttributeDateColumn(IModel<Attribute> model, String console_name, boolean isPreferred) {
		super(	String.valueOf(model.getObject().getId()), 
				new Model<String>(model.getObject().getName()),
				model.getObject().getUniqueName()+"member");
		setModel(model);
		setPreferred(isPreferred);
		this.console_name = console_name;
	}

	public AttributeDateColumn(IModel<Attribute> model, String console_name) {
		super(	String.valueOf(model.getObject().getId()), 
				new Model<String>(model.getObject().getName()),
				model.getObject().getUniqueName()+"member");
		setModel(model);
		this.console_name = console_name;
	}

	public Attribute getAttribute() {
		return getModel().getObject();
	}
	
	@Override
	public String getSortProperty() {
		return getAttribute().isOrdered() ?  getAttribute().getUniqueName()+"name_sort" : null;
	}
	
	public IModel<Attribute> getModel() {
		return model;
	}

	public void setModel(IModel<Attribute> model) {
		this.model = model;
	}
	
	public void detach() {
		super.detach();
		this.model.detach();
	}
	
	
	//static final private int DATE_LEN = "yyyy-mm-dd".length();
	//static final DateTimeFormatter local_tstamp = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	//static final int LOCAL_TSTAMP_LENGTH = ("yyyy-MM-dd HH:mm:ss").length();
	//private static final boolean parse_date_locally = PropertiesFactory.getInstance("kbee").getProperties().getProperty("parse_date_locally", "false").equals("true");
	
	
	@Override
	protected OffsetDateTime getOffsetDateTime(Classificable object) {

		List<String> list = object.getAttributeValues(getModel().getObject());

		if (list!=null && !list.isEmpty()) { 
			
			String val=list.get(0);
			
			/**----------------------------------------
			if (!parse_date_locally) {
				if ( val.length() ==  LOCAL_TSTAMP_LENGTH) {
					try {
					    LocalDateTime datetime = LocalDateTime.parse(val, local_tstamp);
					    ZoneId domain_zid=ZoneId.of(getDomain().getTimeZone());
					    ZonedDateTime zdt = datetime.atZone(domain_zid);
					    
					    logger.debug(zdt.withZoneSameInstant( domain_zid).toOffsetDateTime().toString());
					    
					    return zdt.withZoneSameInstant( domain_zid).toOffsetDateTime();
	
					    
					} catch (Exception e) {
						logger.error( e, " | Tried timestamp without GMT. " + val);
					}
				}
				
				try {
					String xstr=val.substring(0, DATE_LEN);
					LocalDate local  = LocalDate.parse(xstr, DateTimeFormatter.ISO_DATE);
					ZoneId domain_zid=ZoneId.of(getDomain().getTimeZone());
					LocalDateTime ldt = local.atStartOfDay();
					ZonedDateTime zdt = ldt.atZone(domain_zid);
				    
					logger.debug(zdt.withZoneSameInstant( domain_zid).toOffsetDateTime().toString());
				    
				    return zdt.withZoneSameInstant( domain_zid).toOffsetDateTime();
				} catch (Exception e) {
					logger.error(e, " | can not parse the date. returns null " + val);
				}
				
			}-*/
			// logger.debug(ServiceLocator.getService(DateTimeService.class).parseStrDate(val));
			
			return ServiceLocator.getService(DateTimeService.class).parseStrDate(val);
		}
		return null;
	}

	/**@Override
	public String getDateFormat() {
		if (super.getDateFormat().equals(DateTimeService.TIMESTAMP_LABEL))
			return  DateTimeService.MONTH_DAY_YEAR_GMT_LABEL;
		else
			return  DateTimeService.MONTH_DAY_YEAR_LABEL;
	}**/
	
	@Override
	protected String getContextKey() {
		return this.console_name + super.getContextKey();
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
}
