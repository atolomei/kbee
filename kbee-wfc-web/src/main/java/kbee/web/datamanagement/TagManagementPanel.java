package kbee.web.datamanagement;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.command.Command;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.user.UserService;
//import com.novamens.content.web.command.markup.CommandModel;
//import com.novamens.content.web.command.markup.CommandStatusPanelV5;
//import com.novamens.content.web.editor.markup.DomainObjectMainPanel;
//import com.novamens.content.web.nav.markup.ApplicationPageHeader;


import com.novamens.dom.Domain;
import com.novamens.kbee.command.CommandService;
import com.novamens.kbee.content.command.ReclassifyContentCommand;
import com.novamens.kbee.content.command.ReclassifyMemberCommand;
import com.novamens.kbee.wicket.markup.html.console.panel.VerticalLayout;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.modal.InfoDialog;

import kbee.web.command.CommandStatusPanelV5;
import kbee.web.command.panel.CommandModel;
import kbee.web.editor.DomainObjectMainPanel;
import kbee.web.nav.ApplicationPageHeader;
import kbee.web.nav.DataManagementPanelBC;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.*;

import java.util.ArrayList;
import java.util.List;

public class TagManagementPanel extends DomainObjectMainPanel<Domain> {

    /**
	 *
	 */
	private static final long serialVersionUID = 1L;

	static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(TagManagementPanel.class.getName());
    
    private List<IModel<Content>> list;
    private VerticalLayout<ITab> xtabs;
    
    private List<IModel<DataSetMember>> dm_list;
	private ContentTagManagementFormPanel contentTagManagementFormPanel;
    private MemberTagManagementFormPanel memberTagManagementFormPanel;
    private UserTagManagementFormPanel userTagManagementFormPanel;


    public TagManagementPanel(String id, IModel<Domain> model) {
        super(id, model);
        setOutputMarkupId(true);
    }

    
    public void setDataSetMemberSelection(List<IModel<DataSetMember>> list) {
    	this.dm_list=list;
    }
    
    public List<IModel<DataSetMember>> getDataSetMemberSelection() {
    	return this.dm_list;
    }

    
    @Override
    protected void onInitialize() {
        super.onInitialize();

        add(new InfoDialog("help-modal"));

        List<ITab> tabs=new ArrayList<>();
        
        tabs.add(new AbstractTab(new StringResourceModel("contents", this, null)) {
            private static final long serialVersionUID = 1L;
            @Override
            public Panel getPanel(String panelId) {
                contentTagManagementFormPanel = new ContentTagManagementFormPanel(panelId, getSelection()) {
        			private static final long serialVersionUID = 1L;
					@Override
                    protected void runCommand(AjaxRequestTarget target, Command reclassifyCommand) {
                        super.runCommand(target, reclassifyCommand);
                        TagManagementPanel.this.runCommand(target, reclassifyCommand);
                    }
                };
                return contentTagManagementFormPanel;
            }
        });

        tabs.add(new AbstractTab(new StringResourceModel("members", this, null)) {
            private static final long serialVersionUID = 1L;
            @Override
            public Panel getPanel(String panelId) {

                memberTagManagementFormPanel = new MemberTagManagementFormPanel(panelId, getDataSetMemberSelection()) {
					private static final long serialVersionUID = 1L;

					@Override
                    protected void runCommand(AjaxRequestTarget target, Command reclassifyCommand) {
                        super.runCommand(target, reclassifyCommand);
                        TagManagementPanel.this.runCommand(target, reclassifyCommand);
                    }
                };
                return memberTagManagementFormPanel;
            }
        });

        tabs.add(new AbstractTab(new StringResourceModel("users", this, null)) {
            private static final long serialVersionUID = 1L;
            @Override
            public Panel getPanel(String panelId) {
                userTagManagementFormPanel = new UserTagManagementFormPanel(panelId) {
					private static final long serialVersionUID = 1L;
					@Override
                    protected void runCommand(AjaxRequestTarget target, Command reclassifyCommand) {
                        super.runCommand(target, reclassifyCommand);
                        TagManagementPanel.this.runCommand(target, reclassifyCommand);
                    }
                };
                return userTagManagementFormPanel;
            }
        });

       xtabs = new VerticalLayout<ITab>("tabs", this.getClass().getName(), tabs, VerticalLayout.VERTICAL);
        
       xtabs.setTitle(new StringResourceModel("sections", this, null));
       xtabs.setContentBottomPanel(new InvisiblePanel("content-bottom-panel"));
       
       add(xtabs);
        
       for (Command cmd : getCommandService().getCommands().values()) {
            if(     (cmd.getDomain().getId().equals(getDomain().getId())) &&
            		(cmd instanceof ReclassifyContentCommand) &&
            		(cmd.getState()==CommandState.RUNNING)) {
                ReclassifyContentCommand reclassifyCommand = (ReclassifyContentCommand) cmd;
                if( (reclassifyCommand.getUsrId() != null) && (reclassifyCommand.getUsrId().equals(getSessionUser().getId()))) {
                	showRunningCommand(cmd);
                    allowSubmit(false, null);
                    break;
                }
            }
        }
    }

    public void setSelection(List<IModel<Content>> list) {
    	this.list=list;
    }
    
    public List<IModel<Content>> getSelection() {
    	return this.list;
    }
    
    
    public void runCommand(AjaxRequestTarget target, Command reclassifyCommand){
     
    	logger.debug("Sending " + reclassifyCommand.getId().toString() );
    	
    	if ( reclassifyCommand instanceof ReclassifyMemberCommand) {
    		logger.debug("IQL " + ((ReclassifyMemberCommand) reclassifyCommand).getIqlExpression() );
    		saveUserHistory("member-tagtool-iql-condition", ((ReclassifyMemberCommand) reclassifyCommand).getIqlExpression());
    	}
    	else if ( reclassifyCommand instanceof ReclassifyContentCommand) {
    		logger.debug("IQL " + ((ReclassifyContentCommand) reclassifyCommand).getIqlExpression() );
    		saveUserHistory("content-tagtool-iql-condition", ((ReclassifyContentCommand) reclassifyCommand).getIqlExpression());
    	}
    	
    	getCommandService().add(reclassifyCommand);
        showRunningCommand(reclassifyCommand);
        
        target.add(TagManagementPanel.this.xtabs);
        
        allowSubmit(false, target);
    }

    private void allowSubmit(boolean allow, AjaxRequestTarget target){
        if(contentTagManagementFormPanel!= null) {
            contentTagManagementFormPanel.allowSubmit(allow, target);
        }
        if(memberTagManagementFormPanel!=null) {
            memberTagManagementFormPanel.allowSubmit(allow, target);
        }
        if(userTagManagementFormPanel!=null) {
            userTagManagementFormPanel.allowSubmit(allow, target);
        }
    }


    protected IModel<String> getLabel(String string) {
		return new StringResourceModel(string, this, null);
	}


	protected User getSessionUser() {
        return ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();
    }

    private void showRunningCommand(Command cmd) {

    	CommandStatusPanelV5 panel = new CommandStatusPanelV5("content-bottom-panel", new CommandModel(ServiceLocator.getService(CommandService.class).getCommand(cmd.getId()))) {
			private static final long serialVersionUID = 1L;
			@Override
            public void onAfterExecution(AjaxRequestTarget target) {
					target.add(TagManagementPanel.this.getParent());
		            allowSubmit(true, target);
            }
        };
        
        this.xtabs.setContentBottomPanel(panel);
    }

    private CommandService getCommandService() {
        try {
            return (CommandService) ServiceLocator.getService(CommandService.class);
        } catch (Exception e) {
            logger.error(e);
            return null;
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
    	
        if (list!=null) 
    		list.forEach(item -> item.detach());
    	
    	if (dm_list!=null) 
    		dm_list.forEach(item -> item.detach());
    }

    protected InfoDialog getHelpModal() {
        return (InfoDialog) get("help-modal");
    }

    protected ContentDao getContentDao() {
        return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
    }


}

