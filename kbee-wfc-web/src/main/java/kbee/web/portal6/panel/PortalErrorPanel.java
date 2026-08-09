package kbee.web.portal6.panel;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.portal6.model.PortalObject;

import kbee.web.portal6.editor.CloseErrorPanelEvent;


/**
 * @param <T>
 */
public class PortalErrorPanel<T extends PortalObject> extends PortalPanel<T> {
			
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PortalErrorPanel.class.getName());

	private IModel<String> title;
	private IModel<String> text;
	private IModel<String> sttrace;
	private String css = null;
	private AjaxLink<Void> stl;
	private boolean istkvisible = false;
	private boolean is_close = false;

	public boolean isClose() {
		return is_close;
	}


	public void setClose(boolean is_close) {
		this.is_close = is_close;
	}


	/**
	 * @param id
	 * @param model
	 */
	public PortalErrorPanel(String id, IModel<T> model) {
		super(id, model);
		this.title = getDefaultTitle();
		this.text  = getDefaultText();
		this.sttrace =null;
	}
	

	/**
	 */
	public PortalErrorPanel(String id, IModel<T> model, IModel<String> title) {
		super(id, model);
		this.title=title;
		this.sttrace = null;
	}
	
	/**
	 */
	public PortalErrorPanel(String id, IModel<T> model, IModel<String> title, IModel<String> text) {
		super(id, model);
		this.title=title;
		this.text=text;
		this.sttrace =null;
	}
	

	/**
	 */
	public PortalErrorPanel(String id, IModel<T> model, Throwable e) {
		super(id);
		
		this.title=new Model<String>(e.getClass().getName());
		
		StringBuilder str = new StringBuilder();
		str.append(e.getMessage());

		if (e.getCause()!=null) {
			str.append("<br/><br/>" +e.getCause());
		}
		
		this.text=new Model<String>(str.toString());
		
		if (logger.isDebugEnabled() && e!=null) {
			this.sttrace = new Model<String>(e.getStackTrace().toString());
		}
	}
	
	
		
	public PortalErrorPanel(String id, Throwable e) {
		super(id);
		this.title=new Model<String>(e.getClass().getName());
		this.text=new Model<String>(e.getMessage());

		if (logger.isDebugEnabled() && e!=null) 
			sttrace = new Model<String>(e.getStackTrace().toString());

	}
	
	public String getCss() {
		if (this.css==null)
			 return getDefaultCss();
		return this.css;
	}

	
	
	/**
	 * 
	 */
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		WebMarkupContainer c=new WebMarkupContainer("container");
		add(c);
		
		AjaxLink<Void> aj =new AjaxLink<Void>("close") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
					fire (new CloseErrorPanelEvent<>(target));
			};
		};
		
		aj.setVisible(is_close);
		
		c.add(aj);
		
		c.add(new AttributeModifier("class", getCss()));
		
		Label ti = new Label("title", (title!=null? title.getObject():""));
		c.add(ti);
		ti.setVisible(title!=null);
		ti.setEscapeModelStrings(false);
		
		Label te  = new Label("text", (text!=null?text.getObject():""));
		te.setEscapeModelStrings(false);
		te.setVisible(text!=null);
		c.add(te);
		
		Label mi  = new Label("modelinfo", (getModel()!=null ? getModel().getObject().toString():""));
		mi.setEscapeModelStrings(false);
		mi.setVisible(getModel()!=null);
		c.add(mi);
		
		this.stl = new AjaxLink<Void>("stacktrace-link") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				istkvisible=!istkvisible;
				Label st = new Label("stacktrace", sttrace);
				st.setEscapeModelStrings(false);
				st.setVisible(istkvisible);
				stl.addOrReplace(st);
				target.add(PortalErrorPanel.this);
			}
		};
		
		this.stl.setVisible(sttrace!=null); 
				
		Label st = new Label("stacktrace", sttrace);
		st.setEscapeModelStrings(false);
		st.setVisible(false);
		this.stl.add(st);
		c.add(stl);
	}
	

	protected String getDefaultCss() {
		if (getModel()==null)
			return "portal-error";
		return getModel().getObject().getClassKey()+"-error";
	}

	private IModel<String> getDefaultTitle() {
		if (getModel()!=null) {
			return new Model<String>(getModel().getObject().getDisplayName());
		}
		return null;
	}

	private IModel<String> getDefaultText() {
		return null;
	}

}
