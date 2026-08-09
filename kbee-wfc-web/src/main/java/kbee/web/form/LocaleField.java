package kbee.web.form;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.model.ListModel;


public class LocaleField extends ChoiceField<Locale> {
			
	@SuppressWarnings("unused")
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(LocaleField.class.getName());

	private static final long serialVersionUID = 1L;
	
	static  {
		
		
	}
	
	public LocaleField(String id, IModel<Locale> model, boolean required) {
		super(id, model, null);
		super.setChoices(new ListModel<Locale>(new Model<Panel>(this), "locales"));
	}

	private List<Locale> list = new ArrayList<Locale>();
	
	public List<Locale> getLocales() {
	
		if (list!=null)
			return list;
				
		list = new ArrayList<Locale>();
		
		for (Locale locale: Locale.getAvailableLocales()) {
			list.add(locale);
		}
		
		list.sort(new Comparator<Locale>() {
			@Override
			public int compare(Locale o1, Locale o2) {
				Locale ba=getSessionUser().getLocale();
				if (ba==null)
					 ba=Locale.getDefault();
				String a= o1.getDisplayLanguage(ba)+ " " + o1.getDisplayCountry(ba);
				String b= o2.getDisplayLanguage(ba)+ " " + o2.getDisplayCountry(ba);
				return a.compareToIgnoreCase(b);
			}
		});
		return list;
	}
	
	
	protected String getDisplayValue(Locale value) {
		Locale ba=getSessionUser().getLocale();
		if (ba==null)
			 ba=Locale.getDefault();
		return value.getDisplayLanguage(ba)+ " " + value.getDisplayCountry(ba);
	}
	
	public void onDetach() {
		super.onDetach();
		list=null;
	}
	
	
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();	
	}
}
