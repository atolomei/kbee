package kbee.web.searcher.editor;

import org.apache.wicket.model.IModel;

import com.novamens.portal6.model.Block;
import com.novamens.portal6.model.Site;

import kbee.web.editor.DomainObjectEditor;

/**
 * List de panel
 * cada panel es un HomeBlockGroupPanel
 * 
 * Json 
 * 
 * list list=get("home") 
 * list item = list.get("a")
 * item = [_group_id, iql, sort_mode_]
 *
 */
public class SearcherHomeBlockGroupEditor extends DomainObjectEditor<Block> {

	private static final long serialVersionUID = 1L;

	IModel<Site> siteModel;
	private String values;

	private String groupId;
	private String iql;
	private String sort;
	private String max;
	private String order;
														
	public SearcherHomeBlockGroupEditor(String id, String values, IModel<Block> model, IModel<Site> site_model) {
		super(id, model);
		setSiteModel(site_model);
	}
	
	public String getValues() {
		return this.values;
	}
	
	public void setValues(String values) {
		this.values=values;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		String arr[] = getValues().split(";");
		groupId	=	arr[0]!=null?arr[0]:"";
		iql		=	arr[1]!=null?arr[1]:"";
		sort	=	arr[2]!=null?arr[2]:"";
		max		=	arr[3]!=null?arr[3]:"";
		order	=	arr[4]!=null?arr[4]:"";
		
	}

	
	public IModel<Site> getSiteModel() {
		return siteModel;
	}

	public void setSiteModel(IModel<Site> siteModel) {
		this.siteModel = siteModel;
	}
	

	public String getIql() {
		return iql;
	}

	public void setIql(String iql) {
		this.iql = iql;
	}

	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	public String getMax() {
		return max;
	}

	public void setMax(String max) {
		this.max = max;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}
	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	
	
}
