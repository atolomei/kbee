package com.novamens.content.web.report.markup;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.content.web.suggestion.service.UserSuggestionService;
import com.novamens.indexer.query.Suggestion;
import com.novamens.kbee.content.reportsubscription.ReportExportSchedule;
import com.novamens.kbee.content.reportsubscription.SubscriptionReportExportCommand;
import com.novamens.kbee.wicket.markup.html.ajax.WorkingIndicatorAjaxSubmitLink;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.*;
import com.novamens.wicket.markup.html.modal.Dialog;
import com.novamens.wicket.markup.html.modal.InfoDialog;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.form.AutoCompleteFieldV5;
import kbee.web.service.ReportsLibraryService;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubscriptionAdminPanel extends Panel {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	Form<Void> testEmailForm;
	
	private IModel<User> impersonatedTestUser;
	
	
    public SubscriptionAdminPanel(String id) {
        super(id);

        setImpersonatedTestUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
        		
        final List<ReportExportSchedule> userSessionReportSchedules = getUserSessionReportSchedules();

        

        final ListView<ReportExportSchedule> exportsList = new ListView<ReportExportSchedule>("subscriptionExport", userSessionReportSchedules) {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
            protected void populateItem(ListItem<ReportExportSchedule> listItem) {
                final SubscriptionAdminScheduleEditor scheduleList = new SubscriptionAdminScheduleEditor("scheduleEditor", () -> listItem.getModelObject());
                listItem.add(scheduleList);
                testEmailForm = new Form<Void>("testSubscriptionForm");

                WorkingIndicatorAjaxSubmitLink submitButton = new WorkingIndicatorAjaxSubmitLink("testSubscriptionBtn", "Send report to my email",  testEmailForm) {
					private static final long serialVersionUID = 1L;
                    
					@Override
					public String getAjaxIndicatorMarkupId() {
						return getId();
					}
					
					@Override
                    protected void onSubmit(AjaxRequestTarget target) {
                        super.onSubmit(target);
                        
                        if (impersonatedTestUser != null) {
                            UserProfile profile = ((ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao")).findUserProfileByUser(impersonatedTestUser.getObject());
                            SubscriptionReportExportCommand subscriptionReportExportCommand = new SubscriptionReportExportCommand(listItem.getModel().getObject().getId());
                            final String sessionUserEmail = getSessionUserProfile().getPerson().getEmail();
                            subscriptionReportExportCommand.testEmail((Long) profile.getUser().getId(), sessionUserEmail);
                            InfoDialog infoDialog = (InfoDialog) getInformationModal();
                            infoDialog.open(target,new StringResourceModel("successTestModalTitle", this, null), new StringResourceModel("successTestModalBody", this, null));
                            target.add(testEmailForm);
                        }
                    }
                };
                testEmailForm.add(submitButton);

                AutoCompleteFieldV5<User> impersonatedTestUserSuggester = new AutoCompleteFieldV5<User>("userSelector", new PropertyModel<User>(SubscriptionAdminPanel.this, "impersonatedTestUser"), true) {
					private static final long serialVersionUID = 1L;
					@Override
                    public List<Suggestion> getSuggestions(String pattern) {
                        Map<String, Object> parameters = new HashMap<String, Object>();
                        return ServiceLocator.getService(UserSuggestionService.class).getSuggestions(pattern, parameters);
                    }

                    @Override
                    public void onUpdate(AjaxRequestTarget target) {
					    SubscriptionAdminPanel.this.setImpersonatedTestUser(this.getValue());
                        super.onUpdate(target);
                    }

                    @Override
					public String getHistoryKey() {
						return  SubscriptionAdminPanel.this.getClass().getSimpleName();
					}
                };
                
                testEmailForm.add(impersonatedTestUserSuggester);
                //testEmailForm.add(new Label("currentUserEmail", () -> getSessionUserProfile().getPerson().getEmail()));
                listItem.add(testEmailForm);
            }
        };

        add(new InfoDialog("information-modal"));
        add(exportsList);
    }

    
	public User getImpersonatedTestUser() {
		 return this.impersonatedTestUser != null ? this.impersonatedTestUser.getObject() : null;
		}
		
		public void setImpersonatedTestUser(User user) {
			 this.impersonatedTestUser= (user != null ? new ObjectModel<>(user) : null);
		}
			
		public void onDetach() {
			super.onDetach();
			if(impersonatedTestUser != null)
	            impersonatedTestUser.detach();
		}

		

    public UserProfile getSessionUserProfile() {
        try {
            return ServiceLocator.getService(UserService.class).getSessionUserProfile();
        } catch (Exception e) {
            return null;
        }
    }

    public SubscriptionAdminPanel(String id, IModel<?> model) {
        super(id, model);
    }

    protected Dialog getInformationModal() {
        return (Dialog) get("information-modal");
    }

    
    private List<ReportExportSchedule> getUserSessionReportSchedules() {
        return ServiceLocator.getService(ReportsLibraryService.class).getUserDomainReportExportSchedules();
    }

}
