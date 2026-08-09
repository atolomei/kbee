package kbee.web.form;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.model.ListModel;

public class ZoneIdField extends ChoiceField<ZoneId> {

	@SuppressWarnings("unused")
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ZoneIdField.class.getName());

	private static final long serialVersionUID = 1L;
	
	private static Map<String, String> result;
	static private List<ZoneId> list;
	
	static {
		if (list==null) {
			list = new ArrayList<ZoneId>();
			
			for (String z: ZoneId.getAvailableZoneIds()) {
				if (! (z.startsWith("System") || z.startsWith("Etc")))
					list.add(ZoneId.of(z));
			}
			
			list.sort(new Comparator<ZoneId>() {
				@Override
				public int compare(ZoneId o1, ZoneId o2) {
					return o1.getId().compareToIgnoreCase(o2.getId());
				}
			});
		
			result = new HashMap<String, String>();
			List<String> zoneList = new ArrayList<>(ZoneId.getAvailableZoneIds());
		        
		     LocalDateTime dt = LocalDateTime.now();
		     for (String zoneId : zoneList) {
		        	ZoneId zone = ZoneId.of(zoneId);
		            ZonedDateTime zdt = dt.atZone(zone);
		            ZoneOffset zos = zdt.getOffset();
		            //replace Z to +00:00
		            String offset = zos.getId().replaceAll("Z", "+00:00");
		            result.put(zone.toString(), offset);
		        }
		}
    }
	
	/** 
	 *  key   : US/Central  
	 *  value : -05:00
	 */
	
	public Map<String, String> getAllZoneIds() {
		return result;
	}

	public List<ZoneId> getZones() {
		return list;
	}

	
	// ordered by US/Central - 00:05
	//
	public ZoneIdField(String id, IModel<ZoneId> model, boolean required) {
		super(id, model, null);
		IModel<List<ZoneId>> choices = new ListModel<ZoneId>(new Model<Panel>(this), "zones");
		super.setChoices(choices);
	}

	
	protected String getDisplayValue(ZoneId value) {
		Locale ba=getSessionUser().getLocale();
		if (ba==null)
			 ba=Locale.getDefault();
		return value.getId()+ " " + getAllZoneIds().get(value.getId());
	}
	
	public void onDetach() {
		super.onDetach();
	}
	
	
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();	
	}
	

}
