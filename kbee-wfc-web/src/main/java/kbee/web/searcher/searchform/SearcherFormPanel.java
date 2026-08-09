package kbee.web.searcher.searchform;

import java.util.Map;

import org.apache.wicket.model.IModel;

public interface SearcherFormPanel<T> {

	public Map<String, Object> getParameters();
	
	public void setTitle(String s);
	public void setName(String s);
	public void setUsageInfo(String s);
	public void setDomainName(String s);
	
	public String getTitle();
	public String getName();
	public String getUsageInfo();
	public String getDomainName();
	
	public void setModel(IModel<T> model);
	public IModel<T> getModel();
	
	public IModel<String> getAdvancedSearchLinkLabel();
	public void  setAdvancedSearchLinkLabel(IModel<String> s);
	
	void setAdvancedSearchLinkVisible(boolean b);
	boolean isAdvancedSearchLinkVisible();
	
	
}
