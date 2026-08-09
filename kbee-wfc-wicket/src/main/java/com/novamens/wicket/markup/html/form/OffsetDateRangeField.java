package com.novamens.wicket.markup.html.form;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.MissingResourceException;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.IFormModelUpdateListener;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.kbee.wicket.editor.Editor;
import com.novamens.wicket.markup.html.form.Form.Disposition;


@SuppressWarnings("serial")
public class OffsetDateRangeField extends Panel implements IFormModelUpdateListener  {

	private static final long serialVersionUID = 1L;

	IModel<OffsetDateTime> from;
	IModel<OffsetDateTime> to;
	
	IModel<String> label; 
	IModel<String> from_label;
	IModel<String> to_label;
	private String property;

	private boolean feedback = false;
	private Disposition disposition;
	//private Width width = Field.Width.W10;

	private Editor<?> editor;
	
	private boolean required;
	
	ZoneId zid;
	public void setZoneId( ZoneId z) {
		this.zid=z;
	}
	
	public ZoneId getZoneId() {
		return this.zid;
	}
	
	public OffsetDateRangeField(String id, ZoneId zid, IModel<OffsetDateTime> from, IModel<OffsetDateTime> to) {
		super(id);
		this.from=from;
		this.to=to;
		setZoneId(zid);
		
		setProperty(id);
		
		from_label = new StringResourceModel("from", this, null);
		to_label = new StringResourceModel("from", this, null);
		label = new StringResourceModel("label", this, null);
		
	}
	
	
	
	public IModel<String> getLabel() {
		return label;
	}
	
	public IModel<String> getToLabel() {
		return to_label;
	}
	
	public IModel<String> getFromLabel() {
		return from_label;
	}
	
	
	public void setProperty(String name) {
		this.property = name;
	}
	
	public String getProperty() {
		return this.property;
	}
	
	public void setRequired(boolean value) {
		this.required = value;
	}
	
	public boolean isRequired() {
		return required;
	}
	
	
	public boolean helpInfo() {
		return false;
	}
	
	public String helpIcon() {
		return "far fa-info-circle";
	}
	public void onInitialize() {
		super.onInitialize();
		
		OffsetDateTimeField from_f= new OffsetDateTimeField("from", getZoneId(), from) {
			@Override
			protected IModel<String> getHelpText() {
				return getFromHelpText(); 
			}
		};
		
		OffsetDateTimeField to_f= new OffsetDateTimeField("to",  getZoneId(),  to) {
			@Override
			protected IModel<String> getHelpText() {
				return getToHelpText(); 
			}
		};
		

		from_f.setLabel(getFromLabel());
		to_f.setLabel(getToLabel());
		
		add(from_f);
		add(to_f);
		
		
		final WebMarkupContainer daterange = new WebMarkupContainer("date-range");
		
		IModel<String> help = getHelpText();
		
		if (help!=null && help.getObject()!=null)
			daterange.add((new Label ("help", help)).setEscapeModelStrings(false));
		else
			daterange.add((new Label ("help", "")).setVisible(false));
		
		
		
	}
 



	public IModel<OffsetDateTime> getFrom() {
		return this.from;
	}
	
	public IModel<OffsetDateTime> getTo() {
		return this.to;
	}
	
	protected IModel<String> getHelpText() {
 		IModel<String> model = new StringResourceModel(getProperty()+".help", OffsetDateRangeField.this, null);
		try {
			model.getObject();
			return model;
		}
		catch (MissingResourceException e) {
			return null;
		}
	}

	protected IModel<String> getFromHelpText() {
 		IModel<String> model = new StringResourceModel(getProperty()+".from.help", OffsetDateRangeField.this, null);
		try {
			model.getObject();
			return model;
		}
		catch (MissingResourceException e) {
			return null;
		}
	}

	
	protected IModel<String> getToHelpText() {
 		IModel<String> model = new StringResourceModel(getProperty()+".to.help", OffsetDateRangeField.this, null);
		try {
			model.getObject();
			return model;
		}
		catch (MissingResourceException e) {
			return null;
		}
	}
	
	
	@Override
	public void updateModel() {
		// TODO Auto-generated method stub
	}
	
	
	
	
	protected Component getFeedback() {
		WebMarkupContainer feedback = new WebMarkupContainer("feedback") {
			@Override
			public boolean isVisible() {
				return OffsetDateRangeField.this.hasFeedback();
			}
		};
		
		feedback.add(new Label("error", new PropertyModel<String>(this, "message")) {
			@Override
			public boolean isVisible() {
				return OffsetDateRangeField.this.hasErrorMessage();
			}
		});
		
		WebMarkupContainer icon = new WebMarkupContainer("icon") {
			@Override
			public boolean isVisible() {
				return OffsetDateRangeField.this.hasFeedback();
			}
		};
		
		icon.add(new AttributeModifier("class", new Model<String>() {
			public String getObject() {
				if (OffsetDateRangeField.this.hasFeedback()) {
					if (OffsetDateRangeField.this.hasErrorMessage()) {
						return "fal fa-times form-control-feedback";
					}
				}
				return "glyphicon glyphicon-ok form-control-feedback";
			}
		}));
		
		feedback.add(icon);
		
		return feedback;
	}
	
	
	public boolean hasFeedback() {
		return feedback;
	}
	
	
	
	public void setDisposition(Disposition disposition) {
		this.disposition = disposition;
	}	
	
	
	public Disposition getDisposition() {
		if (this.disposition==null) {
			if (getEditor()!=null) {
				if (getEditor().getForm()!=null) {
					if (getEditor().getForm() instanceof Form)
						this.disposition = ((Form<?>)getEditor().getForm()).getDisposition();
				}
			}
		}
		
		if (this.disposition==null)
			return Disposition.VERTICAL;
		
		return this.disposition;
	}
	
	
	
	protected Editor<?> getEditor() {
		if (editor==null) {
			MarkupContainer parent = getParent();
			while (editor==null && parent!=null) {
				if (parent instanceof Editor) {
					editor = (Editor<?>)parent;
				}
				else
					parent = parent.getParent();
			}
		}
		return editor;
	}

	
}


