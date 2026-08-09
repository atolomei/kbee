package com.novamens.kbee.wicket.markup.html.console.browser;


import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.wicket.markup.html.panel.KBPanel;

import kbee.web.console.BaseBrowser;
import kbee.web.console.Browser;
import kbee.web.console.Console;

/**
 * 
 * <p>Used on GridPanel Toolbar and 
 * Selected items Dropdown Menu
 * </p>
 * 
 */
public class ToolbarItem extends KBPanel {
	private static final long serialVersionUID = 1L;
	
	static final IModel<String> default_icon = new Model<String> ("far fa-file");
	
	static public int JUSTIFY_LEFT  = 0;
	static public int JUSTIFY_RIGHT = 1;
	
	public enum Align {
		TOP_RIGHT,
		TOP_LEFT,
		TOP_NONE,
		BOTTOM_LEFT,
		BOTTOM_RIGHT,
		BOTTOM_NONE
	};
	
	private BaseBrowser<?> browser;
	private Console<?> console;
	private Align align;
	private int justify = JUSTIFY_LEFT;
	private IModel<String> icon_css;
	private boolean isIcon;
	private long sid=Double.valueOf(Math.random()*1000000).longValue(); 

		
	public ToolbarItem(Browser<?> browser) {
		this(browser, Align.TOP_NONE);
	}
	
	public ToolbarItem(Browser<?> browser, Align align) {
		this(browser, align, false);
	}
	
	public ToolbarItem(Console<?> console, Align align) {
		super("item");
		this.browser = null;
		this.console = console;
		this.align = align;
		this.isIcon = false;
		icon_css = getDefaultIcon();
	}
	
	public ToolbarItem(Browser<?> browser, Align align, boolean isicon) {
		super("item");
		if (browser!=null) {
			if (!(browser instanceof BaseBrowser))
				throw new IllegalArgumentException("browser must be BaseBrowser");
			this.browser = (BaseBrowser<?>) browser;	
		}
		this.align = align;
		this.isIcon = isicon;
		icon_css = getDefaultIcon();
	}
	
	public long getItemId() {
		return this.sid;
	}
	
	public boolean isIcon() {
		return this.isIcon;
	}
	
	public BaseBrowser<?> getBrowser() {
		return browser!=null ? browser : (console!=null ? console.getBrowser() : null);
	}
	
	public Align getAlign() {
		return align;
	}
	
	public int getJustify() {
		return this.justify;
	}
	
	public void setJustify(int just) {
		this.justify=just;
	}
	
	protected IModel<String> getIconCss() {
		return icon_css; 
	}

	protected IModel<String> getDefaultIcon() {
		return default_icon;
	}
	
	protected String getAnchorTitle() {
		return null;
	}
}
