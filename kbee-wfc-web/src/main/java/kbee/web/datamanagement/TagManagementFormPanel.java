package kbee.web.datamanagement;

import com.novamens.beans.BeansService;
import com.novamens.content.command.Command;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.properties.Property;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.indexer.iql.IqlService;
import com.novamens.indexer.query.ResultSet;
import com.novamens.kbee.wicket.markup.html.ajax.WorkingIndicatorAjaxSubmitLink;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.*;
import com.novamens.wicket.markup.html.modal.InfoDialog;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.iql.KbeeIqlHelpService;
import kbee.web.user.UserQueryHistoryPanel;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import java.util.*;

public abstract class TagManagementFormPanel extends Panel {

    private static final long serialVersionUID = 1L;

    static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(TagManagementPanel.class.getName());

    private String condition;
    protected Form<?> form;

    protected WorkingIndicatorAjaxSubmitLink submitBtn;
    private TagManagementAction tagManagementAction;
    private WebMarkupContainer tagToolActionContainer;
    private ChoiceField<TagManagementAction> contentTemplateChoiceField;
    private List<TagManagementAction> actions = new ArrayList<>();

    final boolean root		     = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
    final boolean role_admin     = root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());

    public String getHistoryKey() {
        return "tagform";
    }

    public TagManagementFormPanel(String id) {
        super(id);
    }


    protected Person getPerson() {
        return ServiceLocator.getService(UserService.class).getSessionUserProfile().getPerson();
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();


        this.form = new Form<Void>("form", Form.Disposition.VERTICAL);

        add(new InfoDialog("help-modal"));
        
        WebMarkupContainer tagContainer = new WebMarkupContainer("tag-container");
        tagContainer.setOutputMarkupId(true);


        final TextAreaField<String> condition = new TextAreaField<String>("condition", new PropertyModel<String>(this, "condition"), true, Field.Width.W12, new IqlValidator(), 3, 3) {
            private static final long serialVersionUID = 1L;

            @Override
            public void onUpdate(AjaxRequestTarget target) {
                super.onUpdate(target);
                setCondition(getValue());
            }

            @Override
            public boolean isHelpInfo() {
                return true;
            }

            @Override
            public boolean isRequired() {
                return TagManagementFormPanel.this.isIQLVisible();
            }

            public boolean isVisible() {
                return TagManagementFormPanel.this.isIQLVisible();
            }

            @Override
            public void onHelp(AjaxRequestTarget target) {
                getHelpModal().open(target, TagManagementFormPanel.this.getLabel("howto-criteria"), getPredicatesHelp());
            }
        };
        form.add(condition);


        form.add(new UserQueryHistoryPanel("user-history", getHistoryKey()+"-tagtool-iql-condition", new ObjectModel<Person>(getPerson())) {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			@Override
            public boolean isVisible() {
                return TagManagementFormPanel.this.isIQLVisible();
            }
            protected void apply(AjaxRequestTarget target, IModel<Property> model) {
                setCondition(model.getObject().getValue().toString());
                condition.setValue(model.getObject().getValue().toString());
                form.addOrReplace(condition);
                target.add(form);
            }
        });

        //form.add(new WebMarkupContainer("selection"));
        
        final WebMarkupContainer affectedItemsLink = new WebMarkupContainer("affectedItemsLink");
        affectedItemsLink.setVisible(false);
        affectedItemsLink.add(new WebMarkupContainer("affectedItemsLinkTitle"));
        form.add(affectedItemsLink);


        contentTemplateChoiceField = new ChoiceField<TagManagementAction>("actionSelector", new PropertyModel<TagManagementAction>(this, "tagManagementAction"), ()->actions) {
            private static final long serialVersionUID = 1L;

            public void onUpdate(AjaxRequestTarget target) {
                setCurrentTagActionPanel(getValue(), target);
            }

            @Override
            protected String getDisplayValue(TagManagementAction value) {
                return value.getActionName();
            }

            private void setCurrentTagActionPanel(TagManagementAction tagManagementAction, AjaxRequestTarget target) {
                TagManagementFormPanel.this.tagManagementAction = tagManagementAction;
                if(tagManagementAction != null)
                    tagToolActionContainer.addOrReplace(tagManagementAction);
                else
                    tagToolActionContainer.addOrReplace(new WebMarkupContainer("tagAction"));

                target.add(tagToolActionContainer);
            }
        };
        contentTemplateChoiceField.setRequired(true);
        form.add(contentTemplateChoiceField);

        tagToolActionContainer = new WebMarkupContainer("tagToolActionContainer");
        tagToolActionContainer.setOutputMarkupId(true);
        tagToolActionContainer.add(new WebMarkupContainer("tagAction"));
        form.add(tagToolActionContainer);

        form.add(tagToolActionContainer);
        submitBtn = new   WorkingIndicatorAjaxSubmitLink("submit-button", getLabel("execute").getObject(), form) {

            private static final long serialVersionUID = -8358957179617226851L;

            protected void onSubmit(AjaxRequestTarget target) {

                if (TagManagementFormPanel.this.isIQLVisible() && getCondition() == null) {
                    super.error("Condition is null");
                    return;
                }
                runCommand(target, getCommand());
            }

            @Override
            public String getAjaxIndicatorMarkupId() {
                return super.getId();
            }

            public boolean isEnabled() {
                return root || role_admin;
            }
        };

        submitBtn.setOutputMarkupId(true);
        form.add(submitBtn);
        form.setOutputMarkupId(true);

        add(form);

        actions= getPossibleTagManagementActions();
    }

    public TagManagementAction getTagManagementAction() {
        return tagManagementAction;
    }

    protected void refreshActions(AjaxRequestTarget target){
        target.add(contentTemplateChoiceField);
        this.actions=getPossibleTagManagementActions();
        if(!actions.isEmpty())
            contentTemplateChoiceField.setValue(actions.get(0));
        else
            contentTemplateChoiceField.setValue(null);

        contentTemplateChoiceField.onUpdate(target);

        target.add(tagToolActionContainer);
    }

    protected abstract List<TagManagementAction> getPossibleTagManagementActions();


    protected boolean isIQLVisible() {
        return true;
    }


    protected abstract Command getCommand();

    protected void updateTags(AjaxRequestTarget target) {
        final Component tagSelector = TagManagementFormPanel.this.get("form:tag-container:tag-selector");
        if (tagSelector.isVisible())
            target.add(tagSelector);
    }


    protected void runCommand(AjaxRequestTarget target, Command reclassifyCommand) {
    }

    protected Domain getDomain() {
        return (Domain) ServiceLocator.getService(UserService.class).getDomain();
    }

    protected IModel<String> getLabel(String string) {
        return new StringResourceModel(string, this, null);
    }


    protected User getSessionUser() {
        return ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();
    }


    class IqlValidator implements IValidator<String> {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        @Override
        public void validate(final IValidatable<String> validatable) {
            String statement = validatable.getValue();
            try {
                if (statement == null || "".equals(statement))
                    return;
                IqlService iqlservice = getDomain().getService(IqlService.class);
                ResultSet set = iqlservice.execute(statement);
                set.hasNext();
            } catch (RuntimeException e) {
                logger.error(e);
                validatable.error(new ValidationError(this));
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();


    }

    public void allowSubmit(boolean allow, AjaxRequestTarget target) {
        submitBtn.setVisible(allow);
        if (target != null)
            target.add(submitBtn);
    }

    protected InfoDialog getHelpModal() {
        return (InfoDialog) get("help-modal");
    }

    protected IModel<String> getPredicatesHelp() {
        return new Model<String>(getDomain().getService(KbeeIqlHelpService.class).getPredicatesHelp());
    }

    protected ContentDao getContentDao() {
        return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
    }


    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }





}
