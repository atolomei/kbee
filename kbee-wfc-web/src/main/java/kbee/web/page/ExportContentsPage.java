package kbee.web.page;


import com.novamens.content.entity.Person;

import com.novamens.indexer.query.Query;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;

public class ExportContentsPage extends ApplicationPage<Person> {
	private static final long serialVersionUID = 1L;

	private Query query;

	public ExportContentsPage(Query query, final BCElement origin) {
		Person person = getPerson();
		this.query=query;
		
		setTopNavigation(getMainTopbar());     
		setMenu(getMainLaternalMenu());       
		
		setModel(new ObjectModel<Person>(person));
		addComponents(query); 
	}
	
	protected Query getQuery() {
		return this.query;
	}

	protected void onClose() {
	}

	private void addComponents(Query query) {

		add(new ExportQueryPanel("export-panel", query) {
			private static final long serialVersionUID = 1L;
			public void onClose() {
				ExportContentsPage.this.onClose();
			}
		});
    }
}
