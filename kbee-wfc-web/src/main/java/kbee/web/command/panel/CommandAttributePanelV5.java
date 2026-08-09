package kbee.web.command.panel;

import java.time.OffsetDateTime;
import java.time.ZoneId;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.Panel;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.datetime.DateTimeService;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;


public class CommandAttributePanelV5 extends Panel  {
		
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(CommandAttributePanelV5.class.getName());
	
	private IModel<String> key;
	
	private IModel<String> value;
	private IModel<String> value_css;
	
	private IModel<String> value_suffix;
	private IModel<String> value_suffix_css;
	
	private IModel<String> container_css;
	private IModel<String> value_container_css;	

	
	private String link;
	
	boolean has_link = false;
	
	public void setLink(String link) {
		this.link=link;
		has_link=true;
	}

	public String getLink() {
		return this.link;
	}
	
	/**
	 * @param id
	 * @param key
	 * @param date
	 */
	public CommandAttributePanelV5(String id, IModel<String> key, OffsetDateTime date, IModel<String> keyCss, IModel<String> valueCss) {
		super(id);
		setOutputMarkupId(true);
		setKey(key);

		DateTimeService d_service = ServiceLocator.getService(DateTimeService.class);
		String zid = d_service.getMapZoneIds().get(getSessionUser().getTimeZone());
		if (zid==null) 
				zid=ZoneId.systemDefault().getId();

		Model<String> value;
		if(date != null)
			value = new Model<>(d_service.format(date, zid, getSessionUser().getLocale(), DateTimeService.DATE_COLlOQUIAL_AGO));
		else
			value =new Model<>("");

		setValue(value);
		setContainer_css(keyCss);
		setValue_container_css(valueCss);
	}
	
	
	public CommandAttributePanelV5(String id, IModel<String> key, IModel<String> value) {
		super(id);
		setOutputMarkupId(true);
		setKey(key);
		setValue(value);
	}
	
	
	public CommandAttributePanelV5(String id, IModel<String> key, IModel<String> value, IModel<String> keyCss, IModel<String> valueCss) {
		super(id);
		setOutputMarkupId(true);
		setKey(key);
		setValue(value);
		setContainer_css(keyCss);
		setValue_container_css(valueCss);
	}
	
	public CommandAttributePanelV5(String id, IModel<String> key, String value, IModel<String> keyCss, IModel<String> valueCss) {
		super(id);
		setOutputMarkupId(true);
		setKey(key);
		setValue(new Model<String>(value));
		setContainer_css(keyCss);
		setValue_container_css(valueCss);
	}
	
	@Override
	public void onDetach() {
 		super.onDetach();
 		
 		
 		if (key!=null)
 			key.detach();
 		
 		if (value!=null);
 			value.detach();
 		
 		if(value_css!=null)
 			value_css.detach();
 			
 		
 		if (value_suffix!=null)
 			value_suffix.detach();
 		
 		if (value_suffix_css!=null)
 			value_suffix_css.detach();
 		
 		
 		if (container_css!=null)
 			container_css.detach();
 		
 		if (value_container_css!=null)
 			value_container_css.detach();
 		
 	}	


	@SuppressWarnings("serial")
	public void onInitialize() {
		super.onInitialize();
		
		

		WebMarkupContainer kcontainer = new WebMarkupContainer("key-container");
		
		kcontainer.add(new AttributeModifier("class", new Model<String>() {
			public String getObject() {
				if (getContainer_css()!=null&&getContainer_css().getObject()!=null)
					return getContainer_css().getObject();
				
				return "col-lg-3 col-md-5 col-xs-5";
			}
		}));
		add(kcontainer);
		kcontainer.add(new Label("key", getKey()));

		WebMarkupContainer value_container = new WebMarkupContainer("value_container");
		value_container.add(new AttributeModifier("class", new Model<String>() {
			public String getObject() {
				if (getValue_container_css()!=null&&getValue_container_css().getObject()!=null)
					return getValue_container_css().getObject();
				return "col-lg-9 col-md-7 col-xs-7";
			}
		}));
		add(value_container);

		
		
		if (has_link) {
			
			value_container.add((new Label("value", "")).setVisible(false));

			
			
			Label value=new Label("value", getValue());
			value.setEscapeModelStrings(false);
			value.add(new AttributeModifier("class", new Model<String>() {
				public String getObject() {
					if (getValue_css()!=null&&getValue_css().getObject()!=null)
						return getValue_css().getObject();
					return "value";
				}
			}));

			Link<Void> l = new Link<Void>("vlink") {
				@Override
				public void onClick() {
					setResponsePage( new RedirectPage(getLink()));
				}
			};
			
			l.add(value);
			value_container.add(l);
			
			
		}
		else {
		
			Label value=new Label("value", getValue());
			value.add(new AttributeModifier("class", new Model<String>() {
				public String getObject() {
					if (getValue_css()!=null&&getValue_css().getObject()!=null)
						return getValue_css().getObject();
					return "value";
				}
			}));
			value.setEscapeModelStrings(false);
			value_container.add(value);
			
			Link<Void> l = new Link<Void>("vlink") {
				@Override
				public void onClick() {
				}
				
			};
			l.add((new Label("value", "")).setVisible(false));
			l.setVisible(false);
			value_container.add(l);
		}
			
		
		

		
		
		
		
		
		
		
		Label value_s=new Label("value_suffix", getValue_suffix()) {
			public boolean isVisible() {
				return  getValue_suffix()!=null &&  getValue_suffix().getObject()!=null;
			}
		};
		value_s.add(new AttributeModifier("class", new Model<String>() {
			public String getObject() {
				if (getValue_suffix_css()!=null&&getValue_suffix_css().getObject()!=null)
					return getValue_suffix_css().getObject();
				return "ago";
			}
		}));
		value_container.add(value_s);
	}
	


	public IModel<String> getKey() {
		return key;
	}

	public void setKey(IModel<String> key) {
		this.key = key;
	}

	public IModel<String> getValue() {
		return value;
	}

	public void setValue(IModel<String> value) {
		this.value = value;
	}

	public IModel<String> getValue_suffix() {
		return value_suffix;
	}

	public void setValue_suffix(IModel<String> value_suffix) {
		this.value_suffix = value_suffix;
	}

	public IModel<String> getValue_css() {
		return value_css;
	}

	public void setValue_css(IModel<String> value_css) {
		this.value_css = value_css;
	}
	
	
	public IModel<String> getValue_container_css() {
		return value_container_css;
	}

	public void setValue_container_css(IModel<String> value_css) {
		this.value_container_css = value_css;
	}
	
	public IModel<String> getContainer_css() {
		return container_css;
	}
	
	public void setContainer_css(IModel<String> value_css) {
		this.container_css = value_css;
	}

	public IModel<String> getValue_suffix_css() {
		return value_suffix_css;
	}

	public void setValue_suffix_css(IModel<String> value_suffix_css) {
		this.value_suffix_css = value_suffix_css;
	}
	
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}


}
