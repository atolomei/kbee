package kbee.web.help;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import com.novamens.wicket.markup.html.panel.KBPanel;



/**
 * 
 * Title
 * Subtitle
 * HTMLText
 * Image - Gallery - Video
 * 
 * 
 *
 * @param <T>
 */
public class InlineHelpPanel extends KBPanel {

	
	private static final long serialVersionUID = 1L;
	
	IModel<String> text;
	IModel<String> title;
	
	private WebMarkupContainer title_container;
	
	
	public InlineHelpPanel(String id) {
		super(id);
	}
	public InlineHelpPanel(String id,IModel<String> text) {
		super(id);
		this.text=text;
	}
	public InlineHelpPanel(String id, IModel<String> title, IModel<String> text) {
		super(id);
		this.title=title;
		this.text=text;
	}

	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		title_container = new WebMarkupContainer("title-container");
		add(title_container);
		
		
		Label t = new Label("title", getTitle());
		t.setEscapeModelStrings(false);
		t.setVisible(getTitle()!=null);
		title_container.add(t);
		title_container.setVisible(getTitle()!=null);
		
		
		
		Label tx= new Label("text", getText());
		tx.setEscapeModelStrings(false);
		tx.setVisible(getText()!=null);
		add(tx);
		
		
	}
	
	
	public IModel<String> getTitle() {
		return title;
	}
	
	public IModel<String> getText() {
		return text;
	}
}
