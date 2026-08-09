package kbee.web.nav;



import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

public class DataManagementPanelBC extends MenuBreadCrumbPanel<Void> {
				
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String key;
	
	
	public DataManagementPanelBC( String key ) {
		this.key=key;
	}
	
	public void onInitialize() {
		super.onInitialize();
	
		DropDownMenuBC<Void> dd = new DropDownMenuBC<Void>();
		dd.addElement(new DataManagementBC(), true);
		
		dd.addElement(new TagManagementToolBC());
		dd.addElement(new TimeDependentRuleBC());
		dd.addElement(new ReindexBC());
		// dd.addElement(new RetentionPolicyBC());
		addElement(dd);
		addElement(new BCElement(key));
		
	}

}
