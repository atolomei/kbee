package com.novamens.kbee.wicket.markup.html.console.panel;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.IModel;

import com.novamens.content.model.Classificable;
import com.novamens.content.model.LabelMember;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
				

/**
 * 
 * HTML is redefined for this class because it has 2 icons
 *
 * @param <T>
 */
public class ObjectLabelMenuItem<T> extends AjaxMenuItemPanelV5<T> {

	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ObjectLabelMenuItem.class.getName());
	
	private IModel<T> object_model;
	private IModel<LabelMember> model;
	private long time = 0;
	private AjaxLink<?> link;
	
	public ObjectLabelMenuItem(String id, IModel<LabelMember> model, IModel<T> object_model) {
		super(id);
		setOutputMarkupId(true);
		this.model = model;
		this.object_model = object_model;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		this.model.detach();
		this.object_model.detach();
	}

	public IModel<T> getObjectModel() {
		return this.object_model;
	}
	
	public IModel<LabelMember> getLabelMemberModel() {
		return this.model;
	}
	
	
	@Override
	public String getCssClass() {
		return isEnabledLabelMember() ? "label-selected": "label-no-selected";
	}
	
	@Override
	public String getLabel() {
		return model.getObject().getDisplayName();
	}

	@Override
	public void onClick(AjaxRequestTarget target) throws Exception {
		long now = System.currentTimeMillis();

		if (now-time<1000) 
			return;
		
		time = now;
		
		target.add(link);
		
		checkLabelMember();
		
		onUpdate(target);
	}

	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer tag = new WebMarkupContainer("item-tag");
		tag.setVisible(true);
		tag.add(new AttributeModifier ("class", getLabelMemberModel().getObject().getLabelColor().getKey() + " far fa-tag"));
		getLink().addOrReplace(tag);
		
		
	}

	public String getIconCssClass() {
		return isEnabledLabelMember() ? (CHECK + " toright fa-fw") : ""; 	
	}
	
	
	@Override
	protected AbstractLink getNewLink(String id) {
		link = new AjaxLink<Void>(id) {
			private static final long serialVersionUID = 1L;
				public void onClick(AjaxRequestTarget target) {
				try {
					ObjectLabelMenuItem.this.onClick(target);
				}
				catch (Exception e) {
					logger.error(e);
					throw new RuntimeException(e);
				}
			}
		};
		return link;
	}
	
	protected boolean isEnabledLabelMember() {
		return false;
	}

	protected void checkLabelMember() {
	}

	protected void onUpdate(AjaxRequestTarget target) {
	}
}
