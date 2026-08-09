package kbee.web.console.grid;

import java.time.OffsetDateTime;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.base.Content;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classificable;
import com.novamens.content.user.UserService;


import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.service.ServiceLocator;


/**
 * 
 * TODO: Revisar junto con esto: 
 * 
 * {@link AttributeEditor}
 * 
 * el Locale.getDefault() debe reemplazarse por el Locale del Domain, (o del usuario ?)
 *
 */
public class AttributeColumn extends GridColumn<SearchResult, String> {
	private static final long serialVersionUID = 1L;
	private IModel<Attribute> model;
							
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AttributeColumn.class.getName());
	
	private String console_name;
	
	
	public AttributeColumn(IModel<Attribute> model, String console_name) {
		super(String.valueOf(model.getObject().getId()), new Model<String>(model.getObject().getName()));
		this.console_name = console_name;
		setModel(model);
	}

	
	public AttributeColumn(IModel<Attribute> model, String console_name, boolean isPreferred) {
		super(String.valueOf(model.getObject().getId()), new Model<String>(model.getObject().getName()));
		this.console_name = console_name;
		setModel(model);
		setPreferred(isPreferred);
	}

	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	
	@Override
	protected IModel<String> getLabelModel(SearchResult object) {
		
		StringBuffer label = new StringBuffer();
		for (String value: ((Classificable)object.getObject()).getAttributeValues(getAttribute())) {
			
			
			if (getAttribute().isDate()) {
				
				logger.warn("Please use a AttributeDateColumn instead.");
				
				try {

					OffsetDateTime odate = ServiceLocator.getService(DateTimeService.class).parseStrDate(value);
					// ServiceLocator.getService(DateTimeService.class).getDateDisplayString(odate, getSessionUser().getLocale());
					//ZoneId zid= ZoneId.of(getDomain().getTimeZone());
					//ServiceLocator.getService(DateTimeService.class).getDomainZoneIDDateDisplayString(odate,  zid, getSessionUser().getLocale());
					ServiceLocator.getService(DateTimeService.class).getDomainInOriginalGMTDateDisplayString(odate, getSessionUser().getLocale());
					
				}
				catch (Exception e) {
					logger.error(value + " | " + e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
					value = e.getClass().getSimpleName();
				}
			}
			
			if (label.length()>0)
				label.append(", ");
			label.append(value);
		}
		return new Model<String>(label.toString());
	}
	
	@Override
	protected String getContextKey() {
		return this.console_name + super.getContextKey();	
	}
	
	@Override
	public boolean isPreferred() {
		return false;
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
	
	public Attribute getAttribute() {
		return getModel().getObject();
	}
	
	@Override
	public void detach() {
		super.detach();
		getModel().detach();
	}
	
	private KbeeUser getSessionUser() {
		return (KbeeUser)ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();
	}
	
}
