package kbee.wicket.tinymce.settings;

public class ImagePlugin extends Plugin {
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 */
	public ImagePlugin() {
		super("image");
	}
	
	protected void definePluginSettings(StringBuffer buffer) {
		buffer.append(", image_advtab: true");
	}
}
