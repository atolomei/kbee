package com.novamens.content.web.admin.markup.datamanagement;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import com.novamens.content.command.CommandParameter;
import com.novamens.content.command.CommandParameterType;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeType;
import com.novamens.datetime.DateTimeService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.Field;
import com.novamens.wicket.markup.html.form.OffsetDateTimeField;
import com.novamens.wicket.markup.html.form.TextField;

import kbee.web.datamanagement.TagManagementTagAction;



public class ParameterPanel extends Panel {
	 private List<CommandParameter> commandParameterList=new ArrayList<CommandParameter>();
	 private Map<String, IModel>  parameterList=new HashMap<String, IModel>();
	
	 

	public List<CommandParameter> getCommandParameterList() {
		return commandParameterList;
	}

	public void setCommandParameterList(List<CommandParameter> commandParameterList) {
		this.commandParameterList = commandParameterList;
	}
	
	

	public Map<String, IModel> getParameterList() {
		return parameterList;
	}

	public void setParameterList(Map<String, IModel> parameterList) {
		this.parameterList = parameterList;
	}

	public ParameterPanel(String id) {
		super(id);
		// TODO Auto-generated constructor stub
	}
	
	

	@Override
	protected void onInitialize() {
		// TODO Auto-generated method stub
		super.onInitialize();
	
	}
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		
	
		if (get("paramlists") == null)
			
			addLists();

	}

	
	private void addLists() {
		
		ListView<CommandParameter> lview = new ListView<CommandParameter>("paramlists",new PropertyModel<List<CommandParameter>>(this, "commandParameterList")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<CommandParameter> item) {
				CommandParameter parameter= item.getModelObject();
				
				 if(parameter!=null && parameter.getType()!=null) {
			        	if(parameter.getType().equals(CommandParameterType.STRING)) {
			        		Model<String> model=new Model<String>();
			        		parameterList.put(item.getModel().getObject().getName(), model);
			        		item.add(new TextField<String>("paraminput",model){
								
								   @Override
								   public IModel<String> getLabel() {
								      return new Model<String>(item.getModel().getObject().getName());
								   
								}
						});
			        		
			        	}else if(parameter.getType().equals(CommandParameterType.DATE)) {
			        		 final IModel<OffsetDateTime> convertDateModel = new IModel<OffsetDateTime>() {
			                     /**
			     				 * 
			     				 */
			     				private static final long serialVersionUID = 1L;

			     				@Override
			                     public OffsetDateTime getObject() {
			     					
			                             return ServiceLocator.getService(DateTimeService.class).parseStrDate("2011-12-03T10:15:30+01:00");
			                           
			                     }

			                     @Override
			                     public void setObject(OffsetDateTime offsetDateTime) {
			                         if (offsetDateTime != null)
			                            ServiceLocator.getService(DateTimeService.class).getStr_ISO_OFFSET_DATE_TIME(offsetDateTime);
			                         
			                     }
			                 };
parameterList.put(item.getModel().getObject().getName(), convertDateModel);
			                 
			                 item.add(new OffsetDateTimeField("paraminput", ZoneId.of("UTC"), convertDateModel) {
			                	 @Override
								   public IModel<String> getLabel() {
								      return new Model<String>(item.getModel().getObject().getName());
								   
								}
			                 });
			                 //field = new OffsetDateTimeField("tagValue", ZoneId.of(getDomain().getTimeZone()), convertDateModel);
			        	}else if(parameter.getType().equals(CommandParameterType.BOOLEAN)) {
							
			        		Model<Boolean> model=new Model<Boolean>();
			        		parameterList.put(item.getModel().getObject().getName(), model);
			        		item.add(new BooleanField("paraminput",model) {
			        			 @Override
								   public IModel<String> getLabel() {
								      return new Model<String>(item.getModel().getObject().getName());
								   
								}
			        			 @Override
			                     public void onUpdate(AjaxRequestTarget target) {
			                         setValue(getValue());
			                     }
		});
			        		
			        	}else if(parameter.getType().equals(CommandParameterType.LONG)) {

			        		Model<Long> model=new Model<Long>();
			        		parameterList.put(item.getModel().getObject().getName(), model);
			        		item.add(new TextField<Long>("paraminput",model){
								
								   @Override
								   public IModel<String> getLabel() {
								      return new Model<String>(item.getModel().getObject().getName());
								   
								}
						});
			        		
			        		
			        	}
			        }
				
				
				

			}

		};

		lview.setOutputMarkupId(true);

		add(lview);
		
	}
	
	
}
