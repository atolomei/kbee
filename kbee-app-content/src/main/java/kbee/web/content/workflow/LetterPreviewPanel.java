package kbee.web.content.workflow;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.base.Content;
import com.novamens.kbee.content.text.template.TemplateData;
import com.novamens.kbee.wicket.model.ModelPanel;

@SuppressWarnings("serial")
public abstract class LetterPreviewPanel<T extends Content> extends ModelPanel<T> {
	private static final long serialVersionUID = 1L;
	
	public LetterPreviewPanel(IModel<T> model, IModel<TemplateData> dataModel) {
		super("preview", model);
		setOutputMarkupId(true);
		IModel<String> textmodel = new Model<String>() {
			public String getObject() {
				return getTemplateText(); 
			}
		};
		Label textlabel = new Label("text", textmodel);
		textlabel.setEscapeModelStrings(false);
		add(textlabel);
	}
	
	protected abstract String getTemplateText();
}