package kbee.web.portal.dataprovider;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.ContentMgmtException;
import com.novamens.dom.Json;
import com.novamens.kbee.portal.model.KbeePortalObject;
import com.novamens.kbee.wicket.editor.Editor;
import com.novamens.kbee.wicket.markup.html.ajax.WorkingIndicatorAjaxLinkV5;
import com.novamens.kbee.wicket.markup.html.event.ClickH1Event;
import com.novamens.portal6.model.Block;
import com.novamens.portal6.model.PortalDataProvider;
import com.novamens.portal6.model.PortalObject;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.SiteService;
import com.novamens.portal6.service.PortalObjectService;
import com.novamens.wicket.markup.html.form.Form;

import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.form.TextAreaField;


import kbee.web.editor.DomainObjectEditor;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;
import kbee.web.form.TextEditorField;
import kbee.web.portal6.editor.PortalCloseDataProviderAjaxEvent;
import kbee.web.portal6.editor.PortalCloseEditAjaxEvent;
import kbee.web.portal6.event.PortalAjaxEvent;
import kbee.web.portal6.event.PortalEditAjaxEvent;
import kbee.web.portal6.factory.PanelPortalModel;

/***
 * 
 *  [ Query ]
 *  [ LIST ]
 *  
 * 
 * 
 */
public class PortalBlockTextDataProvider<T extends PortalObject> extends  DomainObjectEditor<T> implements PanelPortalModel<T>, PortalDataProvider {
			
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PortalBlockTextDataProvider.class.getName());

	
	private IModel<String> text;
	private IModel<T> model;
	
	public PortalBlockTextDataProvider(String id) {
		super(id);
	}

	
	public PortalBlockTextDataProvider(String id, IModel<T> model) {
		super(id, model);
	}
	 
	
	@Override
	public void onDetach() {
		super.onDetach();
		
		if (model!=null)
			model.detach();
	}
	
	
	public void setText(String text) {
		Json js=getModel().getObject().getCustomValuesJson();
		js.put("text", text);
	}
	
	public String getText() {
		Json js=getModel().getObject().getCustomValuesJson();
		return js.getString("text").toString();
	}
	
	WebMarkupContainer cn;
	
	
	
	public Form<?> getForm() {
		return (Form<?>) cn.get("form");
	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		
		setEditionEnabled(false);
		
		
		cn = new WebMarkupContainer("canvas-container");
		add(cn);
		cn.setOutputMarkupId(true);
		
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		cn.add(form);
		
		Json js=getModel().getObject().getCustomValuesJson();
		
		if (js!=null && js.getString("text")!=null) {
			setTextModel(new Model<String>(js.getString("text").toString().replace("<br />", "\n").replace("<br/>", "\n")));
		}
		else
			setTextModel(new Model<String>(new String()));
			
		
		form.add(new TextAreaField<String>("text",  getTextModel(), 10, 40) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void onUpdate(AjaxRequestTarget target) {
				super.onUpdate(target);
				
				Json js = PortalBlockTextDataProvider.this.getModel().getObject().getCustomValuesJson();
				
				logger.debug(getValue());
				
				setTextModel(new Model<String>(getValue().toString()));
				
				js.put("text", getValue().toString());
				
				
				

				
				
				((KbeePortalObject) PortalBlockTextDataProvider.this.getModel().getObject()).setCustomValuesJson(js);
				
				setUpdatedPart("text");
				updateModel();
			}
		});
		
		
	
		
		WorkingIndicatorAjaxLinkV5<Void> co = new WorkingIndicatorAjaxLinkV5<Void>("close") {
			private static final long serialVersionUID = 1L;
			public void onClick(AjaxRequestTarget target) {
				try {
					 close(target);
				} 
				catch (ContentMgmtException e) {
					logger.error(e);	
				}
				target.add( PortalBlockTextDataProvider.this);
			}

			@Override
			protected String getWorkingLabel() {
				return new StringResourceModel("working",  this, null).getObject();
			}
			
			@Override
			public boolean isVisible() {
				return !isEditionEnabled();
			}
		};
		
		cn.add(co);
		
		cn.add(new EditButtonsV5<T>(PortalBlockTextDataProvider.this, false) {
			
			private static final long serialVersionUID = 1L;
			
			@Override
			protected String getCancelClass() {
				return "btn btn-default btn-xs";
			}
			@Override
			protected String getSubmitClass() {
				return "btn btn-primary btn-xs";
			}
			protected String getEditClass() {
				return "btn btn-primary btn-xs";
			}
			
			@Override
			public boolean isVisible() {
				return !isReadOnly();
			}
			
			@Override
			public boolean isEnabled()  {
				return true;
			}
			
			public Editor<T> getEditor() {
				return  PortalBlockTextDataProvider.this;
			}
			
			@Override
			public void onEditClick(AjaxRequestTarget target) {
				PortalBlockTextDataProvider.this.edit(target);
				target.add(PortalBlockTextDataProvider.this.cn);
				onAfterButtonClick(target);
			}
			
			
		});
	}

	protected void close(AjaxRequestTarget target) {
		fireScanAll(new PortalCloseDataProviderAjaxEvent<T>(target, getModel()));
		// fireScanAll(new PortalCloseEditAjaxEvent<T>(target, getModel()));
		
	}


	public IModel<String> getTextModel() {
		return this.text;
	}

	public void setTextModel(IModel<String> mo) {
		this.text=mo;
	}


	public IModel<T> getModel() {
		return model;
	}

	public void setModel(IModel<T> model) {
		this.model = model;
	}

	@Override
	public void edit(AjaxRequestTarget target) {
		super.edit(target);
		target.add(this.cn);
	}
			
	@Override
	public void cancel(AjaxRequestTarget target) {
		super.cancel(target);
		target.add(this.cn);
	}

	
	@Override
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				
				
				
				Json js =  getModel().getObject().getCustomValuesJson();
				js.put("text", getTextModel().getObject().replace("\r\n", "<br/>"));
				logger.debug(js);
				
				
				((KbeePortalObject) getModel().getObject()).setCustomValuesJson(js);
				
				
				getModel().getObject().getService(PortalObjectService.class).save();
				
				
				// Block block= getPortalDao().findBlockById(getModel().getObject().getId());
				// block.getService(PortalObjectService.class);
				// Site site=block.getSite();
				// site.getService(SiteService.class).update(getUpdatedParts());
				
				super.reset();
				target.add(this.cn);
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<T>(target, getModel(),  e));

		}
	}


	
	@Override
	public void setPortalModel(IModel<T> model) {
		setModel(model);
		
	}


	@Override
	public IModel<T> getPortalModel() {
		return getModel();
	}
	
	
}
