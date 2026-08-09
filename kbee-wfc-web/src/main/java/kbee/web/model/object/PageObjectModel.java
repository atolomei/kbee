package kbee.web.model.object;

import org.apache.wicket.model.IModel;

import com.novamens.kbee.portal.model.KbeePage;
import com.novamens.portal6.model.Page;
import com.novamens.portal6.model.SimplePage;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.service.SiteModelService;
import com.novamens.wicket.model.ObjectModel;

public class PageObjectModel implements IModel<Page> {
	private static final long serialVersionUID = 1L;
	ObjectModel<Page> page_model;
	String page_key;
	IModel<Site> siteModel;
	public PageObjectModel(Page page, IModel<Site> siteModel) {
		this.siteModel = siteModel;
		if  (page instanceof KbeePage)
			page_model=new ObjectModel<Page>(page);
		else
			page_key=((SimplePage) page).getKey();
	}
	
	@Override
	public Page getObject() {
		if (page_model!=null)
			return page_model.getObject(); 
			return siteModel.getObject().getService(SiteModelService.class).getPage(page_key);
	}
	
	public void detach() {
		if (page_model!=null)
			page_model.detach();
	}

}
