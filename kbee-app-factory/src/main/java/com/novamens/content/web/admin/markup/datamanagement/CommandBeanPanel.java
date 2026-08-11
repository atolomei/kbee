package com.novamens.content.web.admin.markup.datamanagement;



import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.novamens.beans.BeansService;
import com.novamens.content.command.Command;
import com.novamens.content.command.CommandParameter;
import com.novamens.kbee.command.CommandService;
import com.novamens.kbee.content.questionanswer.QuestionAnswerPermission;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;

import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.modal.InfoDialog;
import com.novamens.wicket.markup.html.panel.KBPanel;

import kbee.web.command.CommandStatusPanelV5;
import kbee.web.command.panel.CommandModel;

@SuppressWarnings("serial")
public class CommandBeanPanel extends KBPanel {

    private static final long serialVersionUID = 1L;

    private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(CommandBeanPanel.class.getName());

    private IModel<Bean> model;
    private List<Bean> commandsbeans;
    private  List<CommandParameter> commandParams;
    
    private String param;
  

    private String parameters;
    private String description;
    private ParameterPanel parameterPanel;

    public String getDescription() {
		return description;
	}
    
	public void setDescription(String description) {
		this.description = description;
	}

	public String getParam() {
		return param;
	}	

	public void setParam(String param) {
		this.param = param;
	}
	



	//true when status panel controls if execution button is enabled or not
    private boolean statusPanelBinded;

    private class Bean implements Serializable {
        private String name;
        private String displayName;
        private String description;
        
        public Bean(String name, String displayName,String description) {
            this.name = name;
            this.displayName = displayName;
            this.description=description;
        }

        public String getDescription() {
            return this.description;
        }

        
        public String getName() {
            return name;
        }

        public String getDisplayName() {
            return displayName == null ? name : displayName;
        }
    }
    



    public CommandBeanPanel(String id) {
        super(id);
        setOutputMarkupId(true);
        addForm();
        addStatus();
    }

    public void onBeforeRender() {
    	super.onBeforeRender();
    	this.commandsbeans = null;
       	getCommands();
    }
    
    
    public Bean getCommand() {
        return this.model != null ? this.model.getObject() : null;
    }

    public void setCommand(Bean bean) {
        this.model = new Model<Bean>(bean);
    }

    public List<Bean> getCommands() {
    	
        if (this.commandsbeans == null) {
            BeansService bs = ServiceLocator.getService(BeansService.class);
            this.commandsbeans = new ArrayList<Bean>();
            Map<String, Command> beans = bs.getBeansOfType(Command.class);
            for (String bean : beans.keySet()) {
                try {
                    Command command = (Command) bs.getBean(bean);
                    this.commandsbeans.add(new Bean(bean, command.getName(), command.getDescription()));
                } catch (Exception e) {
                    logger.error(e);
                }
            }
            
            //this.commandsbeans.forEach(item -> logger.debug(item.getName()));
            
            Collections.sort(this.commandsbeans, new Comparator<Bean>() {
                @Override
                public int compare(Bean b1, Bean b2) {
                	try {
                		return b1.getDisplayName().compareToIgnoreCase(b2.getDisplayName());
                	} catch (Exception e) {
                		logger.error(e);
                		return 0;
                	}
                }
            });
        }
        return this.commandsbeans;
    }

    public String getParameters() {
        return this.parameters;
    }

    public void setParamaters(String parameters) {
        this.parameters = parameters;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.commandsbeans = null;
       
        if (this.model != null)
            this.model.detach();
    }

    /**
     *
     */
    private void addForm() {

        Form<?> form = new Form<Void>("form");
        
        
        parameterPanel=new ParameterPanel("parampanel") {
        	
			   public IModel<String> getLabel() {
			      return new Model<String>("param");
			   
			}
        };
        parameterPanel.setVisible(false);
       form.add(parameterPanel);
        
		  
        form.add(new ChoiceField<Bean>("command", new PropertyModel<Bean>(this, "command"), new PropertyModel<List<Bean>>(this, "commands")) {
        	
        	@Override
            public void onUpdate(AjaxRequestTarget target) {
                setCommand(getValue());
                CommandBeanPanel.this.statusPanelBinded = false;
                CommandBeanPanel.this.getStartButton().setEnabled(true);
                String d=((Bean) getValue()).getDescription();
                setDescription(d!=null?d:((Bean) getValue()).getDisplayName());
              
             commandParams=      CommandBeanPanel.this.callCommandParameters();
               if(commandParams!=null && !commandParams.isEmpty()) {
            	   parameterPanel.setVisible(true);            	   
            	  parameterPanel.setCommandParameterList(commandParams);
            	  
               }else {
            	   parameterPanel.setVisible(false); 
               }
               target.add(CommandBeanPanel.this);
            }
        	

            @Override
            protected String getDisplayValue(Bean value) {
                return value.getDisplayName();
            }

            @Override
            protected String getIdValue(Bean value) {
                return value.getName();
            }
        });
       
       

        form.add(new TextAreaField<String>("parameters", new PropertyModel<String>(this, "parameters"), 6, 20) {
            @Override
            public boolean isHelpInfo() {
                return getCommandHelp() != null;
            }

            @Override
            public void onHelp(AjaxRequestTarget target) {
                String helptext = getCommandHelp();
                if (helptext != null) {
                    getHelpModal().open(target, () -> {
                        return "Help";
                    }, () -> {
                        return helptext;
                    });
                }
            }
        });
        
        if (getCommand()!=null) {
        	setDescription(((Bean)getCommand()).getDescription());
			/*
			 * //parameterPanel.getParameterList() setParameterPanel(parameterPanel);
			 * 
			 * getParameterPanel().setParameterList(parameterPanel.getParameterList());
			 * CommandBeanPanel.this.setParameterPanel(parameterPanel);
			 */
        }
        
        Label des = new Label("description", new PropertyModel<String>(this, "description")) {
        	@Override
        	public boolean isVisible() {
        		return getDescription()!=null;
        	}
        };
        
        des.setEscapeModelStrings(false);
        form.add(des);
        
        add(form);

        form.add(new AjaxSubmitLink("start-button", form) {
            protected void onSubmit(AjaxRequestTarget target) {
                logger.debug("Sending " + getCommand().getName());
                startCommand();
                target.add(CommandBeanPanel.this);
            }
        });

        add(new InfoDialog("help-modal"));
    }

    protected List<CommandParameter> callCommandParameters() {
    	Command command = (Command) ServiceLocator.getService(BeansService.class).getBean(getCommand().getName());
    	
		return command.getParametersDefinition();
	}


	private void addStatus() {
        add(new Panel("status") {
            public boolean isVisible() {
                return false;
            }
        });
    }

    private void startCommand() {
        CommandService service = ServiceLocator.getService(CommandService.class);
        Command command = null;
        try {
            command = (Command) ServiceLocator.getService(BeansService.class).getBean(getCommand().getName());
            if (command != null) {
                statusPanelBinded = true;
              
                command.setParameters(getParametersMap());
                // Service will wrap up the command into a CommandRequest and send it to the Scheduler
                service.add(command);
                updateStatus((Long) command.getId());
                getStartButton().setEnabled(false);
            } else
                logger.error("commnad is " + getCommand().getName() + " is null ");
        } catch (Exception e) {
            logger.error(e);
        }
    }
    
  
    
    
    

    private Map<String, Object> getParametersMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, IModel> paramMap=parameterPanel.getParameterList();
        Collection<IModel> values=	paramMap.values();        
        if(values!=null && !values.isEmpty()) {
        	 for (String name : paramMap.keySet()) {
        	     	IModel model=   paramMap.get(name);
        	        if(model.getObject()!=null)
        	     	map.put(name, model.getObject());
        	        }
        }
       
        if (parameters != null && parameters.length() > 0) {
            String arr[] = parameters.split("\\|");
            for (String line : arr) {
                String kv[] = line.split("(?<!\\\\)(?:=)", 2);//not escaped '='
                if (kv.length < 2) {
                    kv = line.split(":", 2);
                }

                if (kv.length == 2) {
                    String key = kv[0].trim().toLowerCase().replace("\\=", "=");
                    String value = kv[1];
                    if(!map.containsKey(key)) {
                    map.put(key, value);
                    }
                }
            }
            if (map.isEmpty()) {
                String value = parameters.replace("\\=", "=");
                map.put("statement", value);
                map.put("query", value);
            }
        }

        return map;
    }

    private void updateStatus(Long cid) {

        addOrReplace(new CommandStatusPanelV5("status", new CommandModel(ServiceLocator.getService(CommandService.class).getCommand(cid))) {
            @Override
            public void onAfterExecution(AjaxRequestTarget target) {
                if (statusPanelBinded) {
                    getStartButton().setEnabled(true);
                    target.add(CommandBeanPanel.this);
                }
            }
        });
    }

    private Component getStartButton() {
        return CommandBeanPanel.this.get("form").get("start-button");
    }

    private String getCommandHelp() {

        if (getCommand() == null) return null;

        Command command = (Command) ServiceLocator.getService(BeansService.class).getBean(getCommand().getName());

        String help = command.getHelp();

        return help;
    }

    private InfoDialog getHelpModal() {
        return (InfoDialog) get("help-modal");
    }
    
	
	
}
