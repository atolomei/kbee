package kbee.web.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.model.DataSet;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.ExtendedChoiceField;
import com.novamens.wicket.markup.html.panel.KBPanel;

import kbee.web.console.Console;
import kbee.web.model.contentclass.ContentTemplatesPage;

public class ModelConsoleSelector extends KBPanel {
	private static final long serialVersionUID = 1L;

	public ModelConsoleSelector(Console<?> console) {
		super("console");
		
		add(new ExtendedChoiceField<String>("console",  new Model<String>(console.getName()), new PropertyModel<List<String>>(this, "consoles")) {

			private static final long serialVersionUID = 1L;
			
			public void onUpdate(AjaxRequestTarget target) {
				switch (getValue()) {
				case "datasets":					setResponsePage(new DataSetsPage<DataSet>());				break;
				case "attributes":				setResponsePage(new AttributesPage());							break;
				case "classifiers":					setResponsePage(new ClassifiersPage());						break;
				case "contentclasses":					setResponsePage(new ContentTemplatesPage());			break;
				};
			}
			@Override
			public String getDisplayValue(String value) {
				return ModelConsoleSelector.this.getLabel(value).getObject();
			}
		});
	}
	
	public List<String> getConsoles() {
		List<String> consoles = new ArrayList<String>();
		consoles.add("datasets");
		
		if (!isFreeVersion())
			consoles.add("attributes");
		
		consoles.add("classifiers");
		consoles.add("contentclasses");
	  	return consoles;
	}
	
	
	protected boolean isFreeVersion() {
		return getDomain().getDomainType()==DomainType.EXPRESS;
	}
	
	/** --------------------------------------------------------------------------
	 */

	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}

	protected IModel<String> getLabel(String key) {
		return new StringResourceModel(key, this, null);
	}

}
