package com.novamens.wicket.markup.html.form;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.springframework.util.Assert;

import com.novamens.wicket.markup.html.form.Form.Disposition;

@SuppressWarnings("serial")
public class MultipleChoiceField<T extends List<P>, P> extends Field<T> {
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(MultipleChoiceField.class.getName());

	
	private static final long serialVersionUID = 1L;
	
	IModel<List<P>> choices;
	
	public class ChoiceModel implements IModel<Boolean> {
		private String id;
		ChoiceModel(String id) {
			Assert.isTrue(id!=null, "null");
			this.id = id;
		}
		public Boolean getObject() {
			for (P value : MultipleChoiceField.this.getValue()) {
				if (getId().equals(getIdValue(value)))
					return true;
			}
			return false;
		}
		public void setObject(Boolean value) {
			if (!value) {
				for (P selectedchoice : MultipleChoiceField.this.getValue()) {
					if (getId().equals(getIdValue(selectedchoice))) {
						MultipleChoiceField.this.getValue().remove(selectedchoice);
						break;
					}
				}
			}
			else {
				P modelchoice = null;
				for (P choice : getChoices().getObject()) {
					if (getId().equals(getIdValue(choice))) {
						modelchoice = choice;
						break;
					}
				}
				if (modelchoice!=null) {
					MultipleChoiceField.this.getValue().add(modelchoice);
				}
			}
		}
		public String getId() {
			return id;
		}
		public void detach()  {
			
		}
	}
	
	public class ControlFragment extends Fragment {
		
		WebMarkupContainer sub_container;
		Label subtitle;
		
		public ControlFragment(String id, IModel<List<P>> choices) {
			super(id, "control-fragment", MultipleChoiceField.this);
			
			

			 sub_container = new  WebMarkupContainer("subtitle-container");
			 add(sub_container);
			 sub_container.setVisible(  getSubtitle()!=null );
			 subtitle = new Label("subtitle", getSubtitle());
			 subtitle.setEscapeModelStrings(false);
			 sub_container.add(subtitle);
			 
			 
			add(new ListView<String>("option", getIds(choices.getObject())) {
				public void populateItem(ListItem<String> item) {
					String id = item.getModelObject();
					item.add(new AjaxCheckBox("input", new ChoiceModel(id)) {
						@Override
						public boolean isEnabled() {
							return getEditor()!=null ? getEditor().isEditionEnabled() : true;
						}
						@Override
						protected void onUpdate(AjaxRequestTarget target) {
							//MultipleChoiceField.this.onUpdate(target);
						}
					});
					P choice = ControlFragment.this.getChoice(id);
					item.add( (new Label("label", MultipleChoiceField.this.getLabel(choice))).setEscapeModelStrings(false));
				}
			});
			
			if (getTabIndex()>0)
				this.add(new AttributeModifier("tabindex", getTabIndex()));

			
			IModel<String> help = getHelpText();
			
			if (help!=null && help.getObject()!=null)
				add( (new Label ("help", help)).setEscapeModelStrings(false));
			else
				add((new Label ("help", "")).setVisible(false));

			
			add(getFeedback());
		}
		public T getValue() {
			return MultipleChoiceField.this.getValue();
		}
		public void setValue(T value) {
			MultipleChoiceField.this.setValue(value);
		}
		public P getChoice(String id) {
			for (P value : MultipleChoiceField.this.getChoices().getObject()) {
				if (id.equals(getIdValue(value)))
					return value;
			}
			return null;
		}
		public List<String> getIds(List<P> choices) {
			List<String> ids = new ArrayList<String>();
			for (P choice : choices) {
				ids.add(getIdValue(choice));
			}
			return ids;
		}
		
	}
	
	
	/**
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param id
	 */
	public MultipleChoiceField(String id) {
		this(id, null);
	}

	public MultipleChoiceField(String id, IModel<List<P>> choices) {
		this(id, null, choices, false);
	}

	public MultipleChoiceField(String id, IModel<List<P>> choices, boolean required) {
		this(id, null, choices, required);
	}

	public MultipleChoiceField(String id, IModel<T> model, IModel<List<P>> choices) {
		this(id, model, choices, false);
	}

	public MultipleChoiceField(String id, IModel<T> model, IModel<List<P>> choices, boolean required) {
		super(id, model);
		setOutputMarkupId(true);
		setRequired(required);
		setChoices(choices);
		if (model!=null)
			setValue(model.getObject());
	
	}
	
	public IModel<List<P>> getChoices() {
		return choices;
	}
	
	public void setChoices(IModel<List<P>> choices) {
		this.choices = choices;
	}
	
	public IModel<String> getLabel() {
		try {
			return new StringResourceModel("property."+getProperty(), this, null);
		} 
		catch (java.util.MissingResourceException e) {
			return new Model<String>(getProperty());
		} 
		catch (Exception e2) {
			return new Model<String>(getProperty());
		}
	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		if (getChoices()==null)
			throw new NullPointerException("choices is null");
		
		Label label = new Label("label", getLabel());
		

		label.add(new AttributeModifier("class", new Model<String>() {
			public String getObject() {		
				return getDisposition()==null||getDisposition()==Disposition.HORIZONTAL ? "col-lg-1 control-label" : "control-label";
			}
		}));
		
		WebMarkupContainer lc = new WebMarkupContainer("label-container") {
			public boolean isVisible() {
				return  getLabel()!=null && 
						getLabel().getObject()!=null 
						&& getLabel().getObject().length()>0; 
			}
		};
		
		add(lc);
		lc.add(label);
		
		
		lc.add(new WebMarkupContainer("readonly") {
			public boolean isVisible() {
				return MultipleChoiceField.this.isReadOnly();
			}
		});
		
		AjaxLink<Void> helpLink = new AjaxLink<Void>("help-info") {
			public boolean isVisible() {
				return isHelpInfo();
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onHelp(target);
			}
		};

		lc.add(helpLink);
		
		lc.add(new WebMarkupContainer("mandatory") {
			public boolean isVisible() {
				return MultipleChoiceField.this.isRequired();
			}
		});
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		if (get("horizontal-layout")==null) {
			WebMarkupContainer layout = new WebMarkupContainer("horizontal-layout");
			layout.add(new ControlFragment("control", choices));
			add(layout);
			add(new ControlFragment("control", choices));
			if (getDisposition()==null || getDisposition()==Disposition.HORIZONTAL) {
				get("control").setVisible(false);
			}
			else {
				layout.setVisible(false);
			}
		}
		
	}
	
	protected void onHelp(AjaxRequestTarget target) {
	}
	
	protected String getIdValue(P value) {
		return value.toString();
	}
	
	protected String getLabel(P value) {
		return value.toString();
	}
	
	public Component getInput() {
		if (getDisposition()==null || getDisposition()==Disposition.HORIZONTAL) {
			return get("horizontal-layout:control:input");
		}
		else {
			return get("control:input");
		}
	}
}
 