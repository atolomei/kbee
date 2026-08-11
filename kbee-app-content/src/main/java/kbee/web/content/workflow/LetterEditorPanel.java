package kbee.web.content.workflow;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.base.Content;
import com.novamens.content.text.template.ContentTextTemplate;
import com.novamens.content.text.template.Variable;
import com.novamens.content.text.template.VariableResolver;
import com.novamens.datetime.DateTimeService;
import com.novamens.indexer.query.Suggestion;
import com.novamens.kbee.content.text.template.TemplateData;
import com.novamens.kbee.content.util.ContentVariableResolver;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
 
import com.novamens.wicket.markup.html.form.DateField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.util.logging.Logger;
import kbee.web.form.AutoCompleteFieldV5;
import kbee.web.form.TextEditorField;

import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.WebSuggestion;

@SuppressWarnings("serial")
public class LetterEditorPanel<T extends Content> extends ObjectEditor<TemplateData> {
	private static final long serialVersionUID = 1L;
																					
	static Logger logger =  Logger.getLogger(LetterEditorPanel.class.getName());

	private IModel<T> contentModel;
	private IModel<ContentTextTemplate> templateModel;
	
	public class FieldModel implements IModel<String> {
		private String name;
		public FieldModel(String name) {
			this.name = name;
		}
		public String getObject() {
			return getModelObject().get(name);
		}
		public void setObject(String value) {
			getModelObject().put(name, value);
		}
		public void detach() {
		}
	}
	
	public class DateFieldModel implements IModel<Date> {
		private String name;
		
		public DateFieldModel(String name) {
			this.name = name;
		}
		public Date getObject() {
			String datestring = getModelObject().get(name);
			try {
				if (datestring!=null) {
					DateTimeService service = ServiceLocator.getService(DateTimeService.class);
					OffsetDateTime odate = service.parseStrDate(datestring);
					Date lodate = new Date(odate.toInstant().toEpochMilli());
					return lodate;
				}
			}
			catch (Exception e) {
				logger.error(e);
			}
			return null;
		}
		public void setObject(Date value) {
			DateTimeService service = ServiceLocator.getService(DateTimeService.class);
			String datestring = service.getStr_ISO_OFFSET_DATE_TIME(value);
			getModelObject().put(name, datestring);
		}
		public void detach() {
		}
	}
	
	public class OptionsPanel extends Fragment {
		TextEditorField editor;
		Variable variable;
		public OptionsPanel(Variable variable, TextEditorField editor) {
			super("options", "options-fragment", LetterEditorPanel.this);
			this.variable = variable; 
			add(new AutoCompleteFieldV5<String>("options", new Model<String>("")) {
				@Override
				public void onUpdate(AjaxRequestTarget target) {
					editor.addText(target, getValue());
					setSuggestion(null);
					setStringValue(null); 
					setSuggestion(null);
				};
				@Override
				public List<Suggestion> getSuggestions(String pattern) {
					return getOptions(pattern); 
				}
				public IModel<String> getLabel() {
					return new Model<String>(variable.getName());
				}
				@Override
				public String getHistoryKey() {
					return null;
				}
			});
		}
		public List<Suggestion> getOptions(String pattern) {
			List<Suggestion> options = new ArrayList<Suggestion>();
			for(String option : getOptions()) {
				options.add(new WebSuggestion(option, option, 0, false));
			}
			return options;
		}
		public List<String> getOptions() {
			return getVariable().getOptions();
		}
		public void setVariable(Variable variable) {
			this.variable = variable;
		}
		public Variable getVariable() {
			return variable;
		}
	}
	
	public LetterEditorPanel(IModel<T> contentModel, IModel<TemplateData> dataModel, IModel<ContentTextTemplate> templateModel) {
		super("editor", dataModel);
		
		setContent(contentModel);
		
		setTemplate(templateModel);
	}
	
	@Override
	public boolean isVisible() {
		return getTemplate()!=null;
	}
	
	public void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		form.add(new ListView<Variable>("variable", () -> getVariables()) {
			
			public void populateItem(final ListItem<Variable> item) {
				Variable variable = item.getModelObject();
				if ("attribute".equals(variable.getType())) {
					item.add((new WebMarkupContainer("options")).setVisible(false));
					IModel<String> model = new FieldModel(variable.getName());
					model.setObject(resolve(variable));
					item.add(new TextField<String>("field", model) {
						@Override
						public IModel<String> getLabel() {
							return new Model<String>(item.getModelObject().getName());
						}
						@Override
						public boolean isEnabled() {
							return false;
						}
					});
				}
				else
				if ("field".equals(variable.getType()) && "template".equals(variable.getValueType())) {
					IModel<String> model = new FieldModel(variable.getName());
					if (model.getObject()==null && variable.getDefaultValue()!=null)
						model.setObject(variable.getDefaultValue());
					TextEditorField editor = new TextEditorField("field", model) {
						@Override
						public IModel<String> getLabel() {
							return new Model<String>("");
						}
						@Override
						public void onUpdate(AjaxRequestTarget target) {
							super.onUpdate(target);
							updateModel();
							LetterEditorPanel.this.onUpdate(target);
						}
					};
					if (variable.getOptions()!=null && !variable.getOptions().isEmpty()) {  
						item.add(new OptionsPanel(variable , editor));
					}
					else
						item.add((new WebMarkupContainer("options")).setVisible(false));
					item.add(editor);
				}
				else if ("field".equals(variable.getType()) && "date".equals(variable.getValueType())) {
					item.add((new WebMarkupContainer("options")).setVisible(false));
					item.add(new DateField("field", new DateFieldModel(variable.getName())) {
						@Override
						public IModel<String> getLabel() {
							return new Model<String>(item.getModelObject().getName());
						}
						@Override
						public void onUpdate(AjaxRequestTarget target) {
							super.onUpdate(target);
							updateModel();
							LetterEditorPanel.this.onUpdate(target);
						}
					});
				}
				else 
				if ("field".equals(variable.getType()) && "string".equals(variable.getValueType())) {
					item.add((new WebMarkupContainer("options")).setVisible(false));
					item.add(new TextField<String>("field", new FieldModel(variable.getName())) {
						@Override
						public IModel<String> getLabel() {
							return new Model<String>(item.getModelObject().getName());
						}
						@Override
						public void onUpdate(AjaxRequestTarget target) {
							super.onUpdate(target);
							updateModel();
							LetterEditorPanel.this.onUpdate(target);
						}
					});
				}
				else {
					item.setVisible(false);
				}
			}
		});

		add(form);
		
		add(new SubmitButton("submit", getForm()) {
			@Override 
			protected void onSubmit(AjaxRequestTarget target) {
				super.onSubmit(target);
				onUpdate(target);
			}
			@Override
			protected IModel<String> getWorkingLabel() {
				return LetterEditorPanel.this.getLabel("button.submiting");
			}
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				AjaxCallListener myAjaxCallListener = new AjaxCallListener() {
					@Override 
					public CharSequence getBeforeHandler(Component component) { 
						String s;
						s = "document.getElementById('"+component.getMarkupId()+"').innerHTML = '<i class=\"far fa-sync fa-spin fa-fw spinning\"></i> "+getWorkingLabel().getObject()+"'; ";
						return s+" if (typeof(tinyMCE) != \"undefined\") tinyMCE.triggerSave(true,true)";
					}
					@Override
					public CharSequence getCompleteHandler(Component component) {
						String s = null, s1=null;
						String id = component.getMarkupId();
						s1 = "document.getElementById('"+id+"').innerHTML = '"+"<span>"+ getLabel().getObject() + "</span>";
						s1 +="';";
						s ="setTimeout(function () {"+s1+"}, 350);";
						return s;
					}
				};
				attributes.getAjaxCallListeners().add(myAjaxCallListener);
			}
		});
		
//		add(new AjaxLink<Void>("close") {
//			public void onClick(AjaxRequestTarget target) {
//				target.appendJavaScript("closewindow();");
//			}
//		});
//		
//		add(new AjaxLink<Void>("reset") {
//			public void onClick(AjaxRequestTarget target) {
//				LetterEditorPanel.this.getModelObject().getValues().clear();
//				target.add(LetterEditorPanel.this);
//			}
//		});
	}
	
	public String resolve(Variable variable) {
		return getResolver().getValue(variable);
	}

	public IModel<T> getContentModel() {
		return contentModel;
	}

	public void setContent(IModel<T> model) {
		contentModel = model;
	}
	
	public ContentTextTemplate getTemplate() {
		return templateModel.getObject();
	}
	
	public void setTemplate(IModel<ContentTextTemplate> model) {
		this.templateModel = model;
	}
	
	public List<Variable> getVariables() {
		return getTemplate()!=null ? getTemplate().getVariables() : new ArrayList<Variable>();
	}

	protected void onUpdate(AjaxRequestTarget target) {
	}

	protected VariableResolver getResolver() {
		return new ContentVariableResolver<T>(getContentModel(), getModel());
	}
}
