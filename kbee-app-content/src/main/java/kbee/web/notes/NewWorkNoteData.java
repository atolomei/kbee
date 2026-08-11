package kbee.web.notes;

import java.io.Serializable;

import org.apache.wicket.model.IDetachable;


public class NewWorkNoteData implements IDetachable, Serializable {
			
	private static final long serialVersionUID = 1L;
	
	private String title;
	private String text;
	
	public NewWorkNoteData() {
	}
	
	public NewWorkNoteData(String title) {
			this.title=title;
	}
	
	public void setTitle(String name) {
		this.title = name;
	}
	
	public String getTitle() {
		return this.title;
	}
	
	public void setText(String name) {
		this.text = name;
	}
	
	public String getText() {
		return this.text;
	}
	
	@Override
	public void detach() {
	}

}
