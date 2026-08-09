package kbee.web.resource;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.resource.ExternalResource;
import com.novamens.content.resource.KBFile;

public class IconResourceView extends MetaInfoResourceView {
	private static final long serialVersionUID = 1L;
	
	public IconResourceView(String id, IModel<Resource> model, IModel<Content> contentModel, int index) {
		super(id, model, contentModel);
	}
	
	@Override
	public Component getImage() {
		return (new ResourceIcon("image", getModelObject()).setVisible(false));
	}
	
	@Override
	public Component getGlyphIcon() {
		String icon;
		if (getModel()==null || getModel().getObject()==null)
			return new ResourceGlyphIcon("glyphicon");
		else if(getModelObject() instanceof KBFile)
			icon = ((KBFile) getModelObject()).getGlyphIcon();
		else if(getModelObject() instanceof ExternalResource)
			icon = ((ExternalResource)getModelObject()).getGlyphIcon();
		else
			return new ResourceGlyphIcon("glyphicon");
		return new ResourceGlyphIcon("glyphicon", icon);
	}
}