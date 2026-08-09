package kbee.web.portal6.sitemanager;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.novamens.content.user.UserService;

import com.novamens.dom.Domain;
import com.novamens.dom.Json;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.kbee.portal.model.KbeeSite;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.SiteService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.editor.DomainObjectEditor;
import kbee.web.error.ErrorExceptionPanel;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;
import kbee.web.portal6.DomainSearcherPortalService;

public class SimpleSiteContentsEditor extends DomainObjectEditor<Site> {
			
			
	private static final long serialVersionUID = 1L;

	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SimpleSiteContentsEditor.class.getName());
	
	private String iql;

	boolean isDisplayValidVersion;

	
	@Override
	public void edit(AjaxRequestTarget target) {
		super.edit(target);
		target.add(this.getParent());
	}
			
	@Override
	public void cancel(AjaxRequestTarget target) {
		super.cancel(target);
		target.add(this.getParent());
	}
	
	public SimpleSiteContentsEditor(String id, IModel<Site> model) {
		super(id, model);
		setOutputMarkupId(true);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		try {
			setEditionEnabled(false);
			
			add (new SiteBCPanel("bc.site-contents", getModel()));
			
			Json json;
			String site_iql;
			
			try {
				json = getModel().getObject().getCustomValuesJson();
				site_iql = (String) json.get("iql");
			}
			catch (Exception e) {
				logger.error(e);
				json = new KbeeJson();
				site_iql = null;
			}
			
			setIql(site_iql !=null ? site_iql : getDomain().getService(DomainSearcherPortalService.class).getDefaultSearcherPortalIql(getModel().getObject()));
			setDisplayValidVersion(getModel().getObject().isDisplayValidVersion());
			
			Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
			form.setOutputMarkupId(true);
			
			form.add(new TextAreaField<String>("iql", new Model<String>() {
				private static final long serialVersionUID = 1L;
				@Override
				public String getObject() {
					return getIql();
				}
				
				@Override
				public void setObject(String o) {
					setIql(o);
				}
			}, 8, 80));
	
			
			form.add(new BooleanField("displayValidVersion", new PropertyModel<Boolean>(this, "displayValidVersion")) {
				
				private static final long serialVersionUID = 1L;
				
				protected String getFalseStr() {
					return "Head";
					// return new StringResourceModel("no", this, null).getString();
				}
				protected String getTrueStr() {
					return "Valid";
					// return new StringResourceModel("yes", this, null).getString();
				}
				
			});
			add(form);
	
			add(new EditButtonsV5<Site>(this) {
				private static final long serialVersionUID = 1L;
	
				@Override
				public boolean isVisible() {
					return !isReadOnly();
				}
				
				@Override
				protected String getCancelClass() {
					return "btn btn-default btn-sm";
				}
				@Override
				protected String getSubmitClass() {
					return "btn btn-primary btn-sm";
				}
				protected String getEditClass() {
					return "btn btn-link";
				}
				
				public void onEditClick(AjaxRequestTarget target) {
						super.onEditClick(target);
						target.add(SimpleSiteContentsEditor.this.getParent());
				}
								
				public void onCancelClick(AjaxRequestTarget target) {
					super.onCancelClick(target);
					target.add(SimpleSiteContentsEditor.this.getParent());
				}
	
				public void onSubmitClick(AjaxRequestTarget target) {
					super.onSubmitClick(target);
					target.add(SimpleSiteContentsEditor.this.getParent());
				}
			});
		
		} catch (NullPointerException e) {
			logger.error(e);
			
			add((new Form("form")).setVisible(false));
			//add(new ErrorExceptionPanel("form", e));
			
			add(new InvisiblePanel("breadcrumb"));
			add(new InvisiblePanel("buttons"));
			
		} catch (Exception e) {
			logger.error(e);
			throw(e);
		}
	}


	public String getIql() {
		return iql;
	}


	public void setIql(String iql) {
		this.iql = iql;
	}

	public boolean isDisplayValidVersion() {
		return this.isDisplayValidVersion;
	}
	
	public void setDisplayValidVersion(boolean b) {
		this.isDisplayValidVersion=b;
	}

	
	public void onEdit(IModel<Site> model) {
	}

	public void onUpdate(AjaxRequestTarget target) {
	}

	public void onCancel(AjaxRequestTarget target) {
	}

	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				Json json = getModel().getObject().getCustomValuesJson();
				json.put("iql", getIql()==null?"null":getIql());
				KbeeSite ksite = (KbeeSite) getModel().getObject();
				ksite.setCustomValuesJson(json);
				ksite.setDisplayValidVersion(this.isDisplayValidVersion());
				ksite.getService(SiteService.class).update(getUpdatedParts());
			}
		} catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent(target, e));
			
		}
	}

	protected Domain getDomain() {
		try {
			return ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain();
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}

}
