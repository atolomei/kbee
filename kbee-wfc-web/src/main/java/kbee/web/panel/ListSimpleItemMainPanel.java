package kbee.web.panel;

import java.io.IOException;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.Identifiable;
import com.novamens.wicket.markup.html.panel.KBPanel;

import kbee.util.logging.Logger;

@SuppressWarnings("serial")
public class ListSimpleItemMainPanel<T> extends KBPanel {
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = Logger.getLogger(ListSimpleItemMainPanel.class.getName());

	private WebMarkupContainer  container;
	private IModel<T> model;
	private int index=0;
	private boolean isExpand = false;

	public ListSimpleItemMainPanel(String id) {
		super(id);
	}
	
	public ListSimpleItemMainPanel(String id, IModel<T> model, int index, boolean is_expanded) {
		super(id, model);
		this.model=model;
		this.index=index;
		this.isExpand=is_expanded;
		setOutputMarkupId(true);
	}

	public IModel<T> getModel() {
		return model;
	}
	
	protected void setModel(IModel<T> model) {
		this.model = model;
	}
	
	public boolean isExpanded() {
		return isExpand;
	}

	public void setExpanded(boolean isExpand) {
		this.isExpand = isExpand;
	}
	
	public PopupSettings getPopupSettings() {
		return null;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		try {
		
			logger.debug("Initializing ListSimpleItemMainPanel with model -> " + 
				getModel().getObject().toString());
			
			Link<T> link = getLink();
			
			container = new WebMarkupContainer("container");
			add(container);
			
			WebMarkupContainer label_container = new WebMarkupContainer("label-container");
			label_container.add( new AttributeModifier("class", getLabelContainerCss()));
			
			link.add(label_container);
			
			//
			// Label
			//
			IModel<String> ml=getItemLabel(getModel());
			
			Label il =new Label("item-label", ml.getObject());
			il.setEscapeModelStrings(false);
			label_container.add(il);
			link.add( new AttributeModifier("class",  ( isExpanded() ? "selected" : "")));
			container.add(link);
	
			 
			// meta
			//
	
			IModel<String> ms=getItemLabelMeta(getModel());
			
			
			Label lms=new Label("item-label-meta", ms);
				
			
			boolean isMeta = (ms!=null && ms.getObject().length()>0);
			
			lms.setVisible(isMeta);
			lms.setEscapeModelStrings(false);
			container.add(lms);
			
	
			// tags 
			//
			WebMarkupContainer c=getItemTags(getModel());
			
			//boolean isTags  = (c!=null && c.isVisible());
			container.add(c!=null?c: new InvisiblePanel("labels")); 
	
			// moreinfo
			//
	
			WebMarkupContainer m=getMoreInfoPanel(getModel());
			boolean isMoreInfo  = (m!=null && m.isVisible());
			container.add( m!=null?m: new InvisiblePanel("more-info-container"));
			
			//if (isMeta || isMoreInfo) 		
			//	container.add( new AttributeModifier("class", "has-meta"));
		}
		catch (Exception e) {
			container = (WebMarkupContainer)get("container");
			if (container==null) {
				container = new WebMarkupContainer("container");
				add(container);
			}
			container.add(new InvisiblePanel("item-link")); 
			container.add(new Label("item-label-meta", e.getMessage())); 
			container.add(new InvisiblePanel("labels")); 
			container.add(new InvisiblePanel("more-info-container")); 
		}
	}
	
	protected int getIndex() {
		return index;
	}

	protected void onClick() {
		fireScanAll(new ClickItemEvent<T>( getModel(),  getIndex()));
	}
	
	protected void onClick(AjaxRequestTarget target) {
		fireScanAll(new ClickItemEvent<T>( target, getModel(),  getIndex()));
	}

	protected IModel<String> getLabelContainerCss() {
		return new Model<String>("label-container c100");
	}
	
	protected IModel<String> getItemLabel(IModel<T> modelObject) {
		if (modelObject.getObject() instanceof Identifiable) {
			String str=((Identifiable) modelObject.getObject()).getDisplayName();
			return new Model<String>(str!=null?str:"[]");
		}
		return new Model<String>(modelObject.getObject().toString());
	}
	
	protected IModel<String> getItemLabelMeta(IModel<T> modelObject) {
		return null;
	}
	
	protected WebMarkupContainer getItemTags(IModel<T> modelObject) {
		return null;
	}
	
	protected WebMarkupContainer getMoreInfoPanel(IModel<T> modelObject) {
		return null;
	}
 
	protected Link<T> getLink() {
		Link<T> link = new Link<T> ("item-link", getModel()) {
			@Override
			public void onClick() {
				ListSimpleItemMainPanel.this.onClick();
			}
		};
		if (getPopupSettings()!=null)
			link.setPopupSettings(getPopupSettings());
		return link;
	}
}
