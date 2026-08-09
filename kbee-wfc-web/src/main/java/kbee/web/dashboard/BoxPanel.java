package kbee.web.dashboard;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import com.novamens.wicket.markup.html.panel.KBPanel;

public class BoxPanel extends KBPanel {

	
private static final long serialVersionUID = 1L;
	
	Label labelTitle;
	Label labelSubtitle;
	
	public BoxPanel(String id, Label labelTitle) {
		super(id);
		this.labelTitle=labelTitle;
		this.labelSubtitle=new Label("labelSubtitle");
		this.labelSubtitle.setVisible(false);
		if (!labelTitle.getId().equals("labelTitle"))
				throw new IllegalArgumentException("LabelTitle id must be 'labelTitle'");
	}

	public BoxPanel(String id, IModel<String> labelTitle) {
		super(id);
		this.labelTitle = new Label("labelTitle", labelTitle);
		this.labelSubtitle=new Label("labelSubtitle");
		this.labelSubtitle.setVisible(false);
	}
	
	public BoxPanel(String id, IModel<String> labelTitle, IModel<String> labelSubTitle) {
		super(id);
		this.labelTitle = new Label("labelTitle", labelTitle);
		this.labelSubtitle=new Label("labelSubtitle", labelSubTitle);
	}

	
	@Override
	public void onInitialize() {
		super.onInitialize();
		this.labelTitle.setEscapeModelStrings(false);
		add(this.labelTitle);
		this.labelSubtitle.setEscapeModelStrings(false);
		add(this.labelSubtitle);
	}

	public void setBoxStyle(String string) {
		this.add(new AttributeModifier("style", string));
	}
	
	public void setBoxCss(String string) {
		this.add(new AttributeModifier("css", string));
	}

	public void setTitleStyle(String string) {
		labelTitle.add(new AttributeModifier("style", string));
	}
	
	public void setTitleCss(String string) {
		labelTitle.add(new AttributeModifier("class", string));
	}

	
	public void setSubTitleStyle(String string) {
		labelSubtitle.add(new AttributeModifier("style", string));
	}
	
	public void setSubTitleCss(String string) {
		labelSubtitle.add(new AttributeModifier("class", string));
	}
	
	
}
