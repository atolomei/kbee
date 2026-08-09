package kbee.web.searcher.panel;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.model.DataSetMember;
import com.novamens.content.text.Text;
import com.novamens.wicket.markup.html.panel.KBPanel;

@SuppressWarnings("serial")
public class SearcherMemberNotesPanel<T extends DataSetMember> extends KBPanel {
	private static final long serialVersionUID = 1L;
	
	private IModel<T> model;

	public SearcherMemberNotesPanel(String id, IModel<T> model) {
		super(id, model);
		
		this.model = model;
		
		Label notes = new Label("notes", new Model<String>() {
			public String getObject() {
				Text notes = getNotes();
				return notes!=null
					? notes.asString()
					: "";		
			}
		});
		
		notes.setEscapeModelStrings(false);
		
		add(notes);
	}
	
	public Text getNotes() {
		return model.getObject().getNotes();
	}
}
