package kbee.web.portal.dataprovider;

import org.apache.lucene.document.StringField;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.dom.Json;
import com.novamens.kbee.portal.model.KbeePortalObject;
import com.novamens.portal6.model.PortalDataProvider;
import com.novamens.portal6.model.PortalObject;
import com.novamens.portal6.service.PortalObjectService;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.editor.DomainObjectEditor;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;
import kbee.web.portal6.factory.PanelPortalModel;


@SuppressWarnings("serial")
public class PortalContentListDataProvider<T extends PortalObject> extends DomainObjectEditor<T> implements PanelPortalModel<T>, PortalDataProvider {
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PortalContentListDataProvider.class.getName());

	private IModel<String> statement;
	private IModel<String> sort;
	private IModel<String> bajada;
	private Boolean expander;
	
	public PortalContentListDataProvider(String id) {
		super(id);
	}
			
	
	public PortalContentListDataProvider(String id, IModel<T> model) {
		super(id, model);
	}

	public void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
	
	
		setEditionEnabled(false);
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		add(form);
	
		
		Label info=new Label("info", new StringResourceModel("data-provider-info", this, null));
		info.setEscapeModelStrings(false);
		form.add(info);
		
		
		Json js=getModel().getObject().getCustomValuesJson();
		
		if (js!=null && js.getString("statement")!=null) {
			setTextModel(new Model<String>(js.getString("statement").toString().replace("<br />", "\n").replace("<br/>", "\n")));
		}
		else
			setTextModel(new Model<String>(new String()));
		
		if (js!=null && js.getString("sort")!=null) {
			setSortModel(new Model<String>(js.getString("sort").toString().replace("<br />", "\n").replace("<br/>", "\n")));
		}
		else
			setSortModel(new Model<String>(new String()));
		
		if (js!=null && js.getString("abstract")!=null) {
			setAbstractModel(new Model<String>(js.getString("abstract").toString().replace("<br />", "\n").replace("<br/>", "\n")));
		}
		else
			setAbstractModel(new Model<String>(new String()));
		
		if (js!=null && js.getString("expander")!=null) {
			setExpander(js.getString("expander").toString().equals("yes") ? Boolean.valueOf(true): Boolean.valueOf(false));
		}
		else
			setExpander(Boolean.valueOf(false));
		
		form.add(new BooleanField("expander") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				setExpander(getValue());
				setUpdatedPart("expander");
				updateModel();
			}
		});
		
		form.add(new TextAreaField<String>("statement",  getTextModel(), 4, 40) {
			protected IModel<String> getHelpText() {
				return new Model<String>("<a href=\"#\">How to express a IQL Expression</a>");
			}
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				super.onUpdate(target);
				Json js = PortalContentListDataProvider.this.getModel().getObject().getCustomValuesJson();
				setTextModel(new Model<String>(getValue().toString()));
				js.put("statement", getValue().toString());
				((KbeePortalObject) PortalContentListDataProvider.this.getModel().getObject()).setCustomValuesJson(js);
				setUpdatedPart("statement");
				updateModel();
			}
		});
		
		form.add(new TextField<String>("sort",  getSortModel()) {
				protected IModel<String> getHelpText() {
					return new Model<String>("<a href=\"#\">Sort Criteria</a>");
				}
				@Override
				public void onUpdate(AjaxRequestTarget target) {
					super.onUpdate(target);
					Json js = PortalContentListDataProvider.this.getModel().getObject().getCustomValuesJson();
					setSortModel(new Model<String>(getValue().toString()));
					js.put("sort", getValue().toString());
					((KbeePortalObject) PortalContentListDataProvider.this.getModel().getObject()).setCustomValuesJson(js);
					setUpdatedPart("sort");
					updateModel();
				}
		});
		
		
		form.add(new TextAreaField<String>("abstract",  getAbstractModel(), 4, 40) {
			protected IModel<String> getHelpText() {
				return new Model<String>("<a href=\"#\">Freemaker</a>");
			}
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				super.onUpdate(target);
				Json js = PortalContentListDataProvider.this.getModel().getObject().getCustomValuesJson();
				setAbstractModel(new Model<String>(getValue().toString()));
				js.put("abstract", getValue().toString());
				((KbeePortalObject) PortalContentListDataProvider.this.getModel().getObject()).setCustomValuesJson(js);
				setUpdatedPart("abstract");
				updateModel();
			}
		});

		
		
		add(new EditButtonsV5<T>(this) {
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
		});
	}
	
	@Override
	public void setPortalModel(IModel<T> model) {
		setModel(model);
		
	}

	@Override
	public IModel<T> getPortalModel() {
		return getModel();
	}
	
	public void setText(String text) {
		Json js=getModel().getObject().getCustomValuesJson();
		js.put("statement", text);
	}
	
	public String getText() {
		Json js=getModel().getObject().getCustomValuesJson();
		return js.getString("statement").toString();
	}

	@Override
	public void edit(AjaxRequestTarget target) {
		super.edit(target);
		target.add(this);
	}
			
	@Override
	public void cancel(AjaxRequestTarget target) {
		super.cancel(target);
		target.add(this);
	}

	
	@Override
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				Json js =  getModel().getObject().getCustomValuesJson();
				js.put("statement", getTextModel().getObject().replace("\r\n", "<br/>"));
				js.put("sort", getSortModel().getObject().replace("\r\n", "<br/>"));
				js.put("abstract", getAbstractModel().getObject().replace("\r\n", "<br/>"));
				js.put("expander", isExpander().booleanValue() ? "true" : "false");
				((KbeePortalObject) getModel().getObject()).setCustomValuesJson(js);
				getModel().getObject().getService(PortalObjectService.class).save();
				super.reset();
				target.add(this);
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<T>(target, getModel(),  e));

		}
	}

	public Boolean isExpander() {
		return this.expander;
	}

	public void setExpander(Boolean b) {
		this.expander=b;
	}

	
	public IModel<String> getTextModel() {
		return this.statement;
	}

	public void setTextModel(IModel<String> mo) {
		this.statement=mo;
	}
	
	public IModel<String> getSortModel() {
		return this.sort;
	}

	public void setSortModel(IModel<String> mo) {
		this.sort=mo;
	}
	
	public IModel<String> getAbstractModel() {
		return this.bajada;
	}

	public void setAbstractModel(IModel<String> mo) {
		this.bajada=mo;
	}
}
