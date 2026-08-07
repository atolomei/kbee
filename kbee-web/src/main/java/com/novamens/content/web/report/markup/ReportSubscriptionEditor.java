package com.novamens.content.web.report.markup;

import com.novamens.beans.BeansService;
import com.novamens.content.entity.Person;
import com.novamens.content.reportsubscription.ReportSubscription;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserSelfService;
import com.novamens.content.user.UserService;
import com.novamens.kbee.content.reportsubscription.KbeeReportSubscription;
import com.novamens.kbee.content.reportsubscription.ReportExportSchedule;
import com.novamens.kbee.content.reportsubscription.SubscriptionReportExportRequest;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.BooleanSwitchField;
import com.novamens.wicket.markup.html.form.Form;

import kbee.web.form.EditButtonsV5;
import kbee.web.service.ReportsLibraryService;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import java.io.Serializable;
import java.util.*;


public class ReportSubscriptionEditor extends ObjectEditor<Person> {

	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ReportSubscriptionEditor.class.getName());
	
	private List<UserSubscription> userSubscriptions = new ArrayList<>();

	
/**
 * 
 */
    public ReportSubscriptionEditor(String id, IModel<Person> model) {
        super(id,model);
        setOutputMarkupId(true);

        // BeansService bs = ServiceLocator.getService(BeansService.class);

        List<ReportExportSchedule> reportExportSchedules;
        
        reportExportSchedules = getUserSessionReportSchedules();
        
        List<ReportSubscription>  userReportSubscriptions = ((KbeeUser) getUser()).getService(UserSelfService.class).getUserReportSubscriptions();

        for(ReportExportSchedule exportConfig: reportExportSchedules){
            boolean subscribed = false;
            Optional<ReportSubscription> subscription = userReportSubscriptions.stream().filter(
                    sub -> sub.getReportExportScheduleId().equals(exportConfig.getId())
                ).findFirst();
            if(subscription.isPresent()){
                subscribed=subscription.get().isEnabled();
            }
            
            
            String description = exportConfig.getDescription();
            List<SubscriptionReportExportRequest> cronSchedules = exportConfig.getCronSchedules();
            if(cronSchedules.size()>0){
                description=cronSchedules.get(0).getDescription();
                boolean anyScheduleEnabled = cronSchedules.stream().anyMatch(c -> c.isEnabled());
                if(anyScheduleEnabled) {
                	 this.userSubscriptions.add(new UserSubscription(exportConfig.getReport(), exportConfig.getId(), description, subscribed));

                }
            }
           
                   }


        
        Form<?> form = new Form<Void>("form", Form.Disposition.VERTICAL);
        add(form);

        form.add(new Label("email", getPerson().getEmail()));
        		
        form.add(new ListView<UserSubscription>("reportsubscription", userSubscriptions){
			private static final long serialVersionUID = 1L;
			@SuppressWarnings("serial")
			@Override
            protected void populateItem(ListItem<UserSubscription> item) {
                UserSubscription userSubscription = (UserSubscription) item.getDefaultModelObject();
                BooleanSwitchField component = new BooleanSwitchField("selector",
                        new PropertyModel<Boolean>(userSubscription, "subscribed"),
                        new PropertyModel<String>(userSubscription, "reportName"), null){
                    @Override
                    protected IModel<String> getHelpText() {
                        return ()->userSubscription.getDescription();
                    }
                    
                    @Override
                    public boolean isEnabled() {
                    	return getEditor().isEditionEnabled();
                    }
                };

                item.add(component);
            }
        });

        add(new EditButtonsV5<>(this));
        setEditionEnabled(false);

    }

    /**
     * 
     */
    @Override
    public void update(AjaxRequestTarget target) {
        try {
            List<String> updatedParts = getUpdatedParts();
            if (!updatedParts.isEmpty()) {
                KbeeUser user = (KbeeUser) getUser();
                List<ReportSubscription>  userReportSubscriptions = user.getService(UserSelfService.class).getUserReportSubscriptions();

                List<ReportSubscription>  updatedUserReportSubscriptions = new ArrayList<>();

                for (UserSubscription userSubscription : this.userSubscriptions) {
                	
                    if(updatedParts.contains(userSubscription.getReportName())) {
                        ReportSubscription reportSubscription = userReportSubscriptions.stream().filter(
                                repSub -> repSub.getReportExportScheduleId().equals(userSubscription.getReportExportScheduleID()
                                )).findFirst().orElse(null);
                        if (reportSubscription == null) {
                            reportSubscription = new KbeeReportSubscription();
                            reportSubscription.setUsr(user);
                            reportSubscription.setDefaultAudit();
                            reportSubscription.setDomain(user.getDomain());
                            reportSubscription.setReportExportScheduleId(userSubscription.reportExportScheduleID);
                        }

                        reportSubscription.setEnabled(userSubscription.getSubscribed());
                        updatedUserReportSubscriptions.add(reportSubscription);
                    }
                }

                user.getService(UserSelfService.class).saveUserReportSubscriptions(updatedUserReportSubscriptions);
                super.reset();
            }
        }
        catch (Exception e) {
            logger.error(e);
            throw new RuntimeException(e);
        }
    }

    public User getUser() {
        UserProfile profile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
        if (profile!=null)
            return profile.getUser();
        else
            return null;
    }



    public class UserSubscription implements Serializable {

		private static final long serialVersionUID = 1L;
		
		String reportName;
        String reportExportScheduleID;
        String description;
        Boolean subscribed;

        public UserSubscription(String reportName, String reportExportScheduleID, String description, Boolean subscribed) {
            this.reportName = reportName;
            this.reportExportScheduleID = reportExportScheduleID;
            this.description = description;
            this.subscribed = subscribed;
        }

        public String getReportName() {
            return reportName;
        }

        public void setReportName(String reportName) {
            this.reportName = reportName;
        }

        public Boolean getSubscribed() {
            return subscribed;
        }

        public void setSubscribed(Boolean subscribed) {
            this.subscribed = subscribed;
        }

        public String getReportExportScheduleID() {
            return reportExportScheduleID;
        }

        public void setReportExportScheduleID(String reportExportScheduleID) {
            this.reportExportScheduleID = reportExportScheduleID;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
    
    protected Person getPerson() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getPerson();
	}

    private List<ReportExportSchedule> getUserSessionReportSchedules() {
        return ServiceLocator.getService(ReportsLibraryService.class).getUserDomainReportExportSchedules();
    }
}
