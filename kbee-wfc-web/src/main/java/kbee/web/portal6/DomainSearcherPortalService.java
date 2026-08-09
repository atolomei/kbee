package kbee.web.portal6;

import java.util.List;
import java.util.Locale;

import com.novamens.content.searcher.SearcherHomeBlock;
import com.novamens.portal6.model.Site;
import com.novamens.service.BusinessObjectService;
import com.novamens.util.KeyValue;

public interface DomainSearcherPortalService extends BusinessObjectService {
	
	public class HomeBlock {
		public boolean isEnabled = true;
		public String key;
		public String title_en  = null;
		public String title_spa = null;
		
		public String iql = null;
		public Long total = new Long(15);
		
		public String getTitle(Locale locale) {
			if (locale.getLanguage().equals("spa") || locale.getLanguage().equals("es"))
				return title_spa;
			return title_en;
		}
	};
	

	
	public String getDefaultSearcherPortalIql(Site site);
	
	
	
	public List<KeyValue<String>> getFooterMenuOptions();
	public List<KeyValue<String>> getFooterMenuOptions(Locale locale);
	
	public HomeBlock getHomeBlock(String key);
	
 	public boolean isPublic();
	
 	public String getAboutTitle();
	public String getAboutAbstract();
	public String getAboutText();
	
	public String getAboutAbstract(Locale locale);
	public String getAboutTitle(Locale locale);
	public String getAboutText(Locale locale);
	
	
	// public String getSearcherQuery();
	public String getSearchPortalName(Locale locale);

	// public String getContactEmail();
	//public String getContactAbstract();
	//public String getContactTitle();
	//public String getContactText();
	
	//public String getContactAbstract(Locale locale);
	//public String getContactTitle(Locale locale);
	//public String getContactText(Locale locale);
	

	public List<SearcherHomeBlock> getSearcherHomeBlock();
	public void save(SearcherHomeBlock block);
	
}
