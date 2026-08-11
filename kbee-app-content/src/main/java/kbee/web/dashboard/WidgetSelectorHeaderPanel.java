package kbee.web.dashboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.util.ListModel;

import com.novamens.kbee.wicket.markup.html.event.GeneralWicketAjaxEvent;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.repository.DomRepository;
import com.novamens.repository.DomRepositoryService;
import com.novamens.security.Identifiable;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.panel.KBPanel;

@SuppressWarnings("serial")
public class WidgetSelectorHeaderPanel<T extends Identifiable> extends KBPanel {
	private static final long serialVersionUID = 1L;
			
	@SuppressWarnings("unused")
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(WidgetSelectorHeaderPanel.class.getName());

	private IModel<T> model;
	private List<IModel<T>> elements = null;
	//private IModel<String> title;
	private boolean isEdit  = false;
	private boolean isHelp  = false;

	private String key;
	
																	
	public WidgetSelectorHeaderPanel(String id, String key, IModel<String> title, IModel<T> model, List<IModel<T>> elements) {
		super(id);
		//this.title=title;
		setOutputMarkupId(true);
		this.model = model;
		this.elements = elements;
		this.key=key;
 	}
	
	
	public void setItem(IModel<T> lib) {
		this.model = lib;
	}
	
	public IModel<T> getItem() {
		return model;
		
	}
	
	public String getKey() {
		return this.key;
	}
	
	public List<IModel<T>> getItems() {
		if (elements==null)
			elements=new ArrayList<IModel<T>>();
		return elements;
	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		if (getItems()==null || getItems().size()==0) {
			add(new InvisiblePanel("item"));
			
		}
		else {
		
			ListModel<IModel<T>> li= new ListModel<IModel<T>>(getItems());
			
			DropDownChoice<IModel<T>> ch = new DropDownChoice<IModel<T>> ("item", 
					 new IModel<IModel<T>>() {
						@Override
						public IModel<T> getObject() {
							return getItem();
						}
				 	 public void setObject(IModel<T> m) {
				 		 setItem(m);
				 	 }
				 
			 }, li);
			 
			 ch.add(new AjaxFormComponentUpdatingBehavior("change") {
				protected void onUpdate(AjaxRequestTarget target) {
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("key", key);
					map.put("item", getItem().getObject().getId().toString());
					fireScanAll (new GeneralWicketAjaxEvent (target, WidgetSelectorHeaderPanel.class.getName(), map));
				}
			});
			 
			ch.setChoiceRenderer(new ChoiceRenderer<IModel<T>>() {
				public String getIdValue(IModel<T> value, int index) {
					return value.getObject().getId().toString();
				};
				public String getDisplayValue(IModel<T> value) {
					return WidgetSelectorHeaderPanel.this.getItemLabel(value);
				
					// return WidgetSelectorHeaderPanel.this.getLabel("name", value.getObject().getDisplayName()).getObject();
					//return  new StringResourceModel("name", WidgetSelectorHeaderPanel.this, null).setParameters(new Object[] {value.getObject().getDisplayName()}).getObject();
				};
			});
			
			add(ch);
			
		}
		
				
		
		 
		add(new Label("title", "").setVisible(false));
		
		
		AjaxLink<Void> re = new AjaxLink<Void>("refresh") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				WidgetSelectorHeaderPanel.this.refresh(target);
			}
			
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				IAjaxCallListener listener = new IAjaxCallListener() {
					@Override
					public CharSequence getSuccessHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getPrecondition(Component component) {
						return null;
					}
					@Override
					public CharSequence getFailureHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getCompleteHandler(Component component) {
						String s = null, s1=null;
						String id = component.getMarkupId();
						s1 = "document.getElementById('"+id+"').innerHTML = '"+"<i class=\"far fa-sync \"/>"+"';";
						 s ="setTimeout(function () {"+s1+"}, 350);";
						return s;
					}
					@Override
					public CharSequence getBeforeSendHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getBeforeHandler(Component component) {
						String s = null;
						String id = component.getMarkupId();
						s = "document.getElementById('"+id+"').innerHTML = '<i class=\"far fa-sync fa-spin spinning\"></i>'";
						return s;
					}
					@Override
					public CharSequence getAfterHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getDoneHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getInitHandler(Component component) {
						return null;
					}
				};
				attributes.getAjaxCallListeners().add(listener);
			}
		};
		
		add(re);
		
		
		
		
		
		
		AjaxLink<Void> help = new AjaxLink<Void>("help") {
			private static final long serialVersionUID = 1L;
			
			public boolean isVisible() {
				return isHelp();
			}
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				WidgetSelectorHeaderPanel.this.onHelp(target);
			}
			
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				IAjaxCallListener listener = new IAjaxCallListener() {
					@Override
					public CharSequence getSuccessHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getPrecondition(Component component) {
						return null;
					}
					@Override
					public CharSequence getFailureHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getCompleteHandler(Component component) {
						String s = null, s1=null;
						String id = component.getMarkupId();
						s1 = "document.getElementById('"+id+"').innerHTML = '"+"<i class=\"fal fa-info-circle \"/>"+"';";
						 s ="setTimeout(function () {"+s1+"}, 350);";
						return s;
					}
					@Override
					public CharSequence getBeforeSendHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getBeforeHandler(Component component) {
						String s = null;
						String id = component.getMarkupId();
						s = "document.getElementById('"+id+"').innerHTML = '<i class=\"far fa-sync fa-spin   spinning\"></i>'";
						return s;
					}
					@Override
					public CharSequence getAfterHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getDoneHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getInitHandler(Component component) {
						return null;
					}
				};
				attributes.getAjaxCallListeners().add(listener);
			}
		};
		
		

		
		
		
		AjaxLink<Void> edit = new AjaxLink<Void>("edit") {
			private static final long serialVersionUID = 1L;
			
			
			public boolean isVisible() {
				return isEdit();
			}
			@Override
			public void onClick(AjaxRequestTarget target) {
				WidgetSelectorHeaderPanel.this.onEdit(target);
			}
			
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				IAjaxCallListener listener = new IAjaxCallListener() {
					@Override
					public CharSequence getSuccessHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getPrecondition(Component component) {
						return null;
					}
					@Override
					public CharSequence getFailureHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getCompleteHandler(Component component) {
						String s = null, s1=null;
						String id = component.getMarkupId();
						s1 = "document.getElementById('"+id+"').innerHTML = '"+"<i class=\"far fa-sync \"/>"+"';";
						 s ="setTimeout(function () {"+s1+"}, 350);";
						return s;
					}
					@Override
					public CharSequence getBeforeSendHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getBeforeHandler(Component component) {
						String s = null;
						String id = component.getMarkupId();
						s = "document.getElementById('"+id+"').innerHTML = '<i class=\"far fa-edit spinning\"></i>'";
						return s;
					}
					@Override
					public CharSequence getAfterHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getDoneHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getInitHandler(Component component) {
						return null;
					}
				};
				attributes.getAjaxCallListeners().add(listener);
			}
		};
		
		

		
		
		
		
		
		
		
		
		add(help);
		add(edit);
		
		
		
		
		
	}


//	private IModel<String> getTitle() {
//		return title;
//	}

	protected String getItemLabel(IModel<T> value) {
		return WidgetSelectorHeaderPanel.this.getLabel("name", value.getObject().getDisplayName()).getObject();
	}


	protected <R> DomRepository<R> getRepository(Class<R> objectclass) {
		DomRepository<R> repository = ServiceLocator.getService(DomRepositoryService.class).getRepository(objectclass);
		return repository;
	}
	
	
	public void onDetach() {
		super.onDetach();

		if (model!=null)
			model.detach();
		
		if (elements!=null) 
			elements.forEach(item -> item.detach());
	}

	
	protected void refresh(AjaxRequestTarget target) {
	}
	
	
	protected void onEdit(AjaxRequestTarget target) {
		// TODO Auto-generated method stub
		
	}


	
	protected void onHelp(AjaxRequestTarget target) {
		// TODO Auto-generated method stub
		
	}
	
	
	public boolean isHelp() {
		return isHelp;
	}


	public void setHelp(boolean isHelp) {
		this.isHelp = isHelp;
	}


	public boolean isEdit() {
		return isEdit;
	}


	public void setEdit(boolean isEdit) {
		this.isEdit = isEdit;
	}

}
