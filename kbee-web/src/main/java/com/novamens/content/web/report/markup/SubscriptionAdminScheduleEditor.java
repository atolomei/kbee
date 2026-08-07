package com.novamens.content.web.report.markup;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import com.novamens.beans.BeansService;
import com.novamens.content.service.DomainService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.event.EventService;
import com.novamens.kbee.content.reportsubscription.ReportExportSchedule;
import com.novamens.kbee.content.reportsubscription.SubscriptionReportExportRequest;
import com.novamens.kbee.event.EvictCacheServiceEvent;
import com.novamens.kbee.scheduler.CronJobDao;
import com.novamens.kbee.wicket.editor.Editor;
import com.novamens.scheduler.CronExpressionJ8;
import com.novamens.scheduler.CronSchedulerService;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.*;

import kbee.web.form.EditButtonsV5;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import java.util.List;
import java.util.stream.Collectors;

import static com.cronutils.model.CronType.QUARTZ;

public class SubscriptionAdminScheduleEditor extends ObjectEditor<ReportExportSchedule> {

    private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SubscriptionAdminScheduleEditor.class.getName());

    private CronExpressionJ8 cronExpression;
    private Boolean cronEnabled=false;
    private String description;

    public SubscriptionAdminScheduleEditor(String id, IModel<ReportExportSchedule> model) {
        super(id, model);

        List<SubscriptionReportExportRequest> cronSchedules = getModelObject().getCronSchedules();
        this.setDescription(model.getObject().getDescription());
        if(cronSchedules.size() > 0){
            final SubscriptionReportExportRequest subscriptionReportExportRequest = cronSchedules.get(0);
            this.setCronExpression(subscriptionReportExportRequest.getCronExpression());
            this.setCronEnabled(subscriptionReportExportRequest.isEnabled());
            this.setDescription(subscriptionReportExportRequest.getDescription());
        }
        Form<?> form = new Form<Void>("form", Form.Disposition.VERTICAL);
        add(new Label("scheduleLabel", getModel().getObject().getReport()));

        form.add(new TextAreaField<String>("scheduleDescription", new PropertyModel<>(this, "description"), 5, 40));
        
        CronField c=new CronField("schedule", new PropertyModel<>(this, "cronExpression"));
        c.setBorder(true);
        form.add(c);
        
        BooleanSwitchField b=new BooleanSwitchField("enable", new PropertyModel<>(this, "cronEnabled"));
        b.setBorder(true);
        form.add(b);
        

        add(form);
        
        add(new EditButtonsV5<ReportExportSchedule>(this) {
			private static final long serialVersionUID = 1L;
			protected String getEditClass() {
        		return "btn-link";
        	}
        });
        setEditionEnabled(false);
    }

    @Override
    public void update(AjaxRequestTarget target) {
        try {
            List<String> updatedParts = getUpdatedParts();
            if (!updatedParts.isEmpty()) {
                final SubscriptionReportExportRequest request;
                List<SubscriptionReportExportRequest> cronSchedules = getModelObject().getCronSchedules();
                if(cronSchedules.size() > 0){
                    request = cronSchedules.get(0);
                }else{
                    request = new SubscriptionReportExportRequest();
                    request.setReportExportScheduleId(this.getModel().getObject().getId());
                }
                request.setCronExpression(this.cronExpression);
                request.setName(this.getModel().getObject().getId());
                request.setDescription(this.getDescription());
                request.setEnabled(this.getCronEnabled());

                Domain domain = ServiceLocator.getService(UserService.class).getDomain();
                DomainService objectService = domain.getService(DomainService.class);
                objectService.saveSubscriptionSchedule(request);


                super.reset();
            }
        }
        catch (Exception e) {
            logger.error(e);
            throw new RuntimeException(e);
        }
    }





    public CronExpressionJ8 getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(CronExpressionJ8 cronExpression) {
        this.cronExpression = cronExpression;
    }

    public Boolean getCronEnabled() {
        return cronEnabled;
    }

    public void setCronEnabled(Boolean cronEnabled) {
        this.cronEnabled = cronEnabled;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

