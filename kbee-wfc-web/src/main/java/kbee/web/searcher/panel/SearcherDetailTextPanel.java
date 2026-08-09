package kbee.web.searcher.panel;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import com.novamens.content.base.Content;
import com.novamens.content.communication.OrganizationalText;
import com.novamens.content.model.ModelSection;
import com.novamens.portal6.model.Site;

public class SearcherDetailTextPanel<T extends Content> extends SearcherDetailPanel<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean isConsole = false;
	
	public SearcherDetailTextPanel(String id, IModel<T> model, IModel<Site> site_model, boolean  isConsole) {
		super(id, model, site_model);
		this. isConsole= isConsole;
	}
	
	public boolean isConsole() {
		return this.isConsole;
	}
	
	public  List<ModelSection>  getSections() {
		return new ArrayList<>();
		//return getModelObject().getContentTemplate().getPortalSections();	
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		String str;
		if (getModel().getObject() instanceof OrganizationalText) {
		
			str= ((OrganizationalText) getModelObject()).getText().asString();
			//String prefix="<span class=\"macro\">";
			//String suffix="</span>";
			//str=str.replace("${", prefix + "${").replace("}","}"+suffix);
			
		}
		else
			str= getModel().getObject().getContentTemplate().getDisplayName() + "-> has no text";
		
		
		String title=getModel().getObject().getContentTemplate().getText_label();
		
		add(new Label("title", title));
		
		add((new Label("text", str)).setEscapeModelStrings(false));
	}
	
	
	private String getTextEscaped(String s) {
		
		if (s==null)
			return null;
		
		String cleaned = Jsoup.clean(s, Safelist.basic());
		String t1 = cleaned;

		t1 = t1.replace("&", "&amp;");
		t1 = t1.replace("<", "&lt;");
		t1 = t1.replace(">", "&gt;");
		
		String prefix="<span class=\"macro\">";
		String suffix="</span>";
		String b=t1.replace("${", prefix + "${").replace("}","}"+suffix);
		return b; 

		
	}

}
