package kbee.web.resource;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;

@Deprecated
public class ThumbnailResourceView<T extends Content> extends MetaInfoResourceView {
	private static final long serialVersionUID = 1L;

	public ThumbnailResourceView(String id, IModel<Resource> model, int index) {
		super(id, model, null, 0);
	}
	@Override
	public Component getImage() {
		return null;
	}
	
	@Override
	public Component getGlyphIcon() {
		return new Label("glyphicon", "").setVisible(false);
	}
}