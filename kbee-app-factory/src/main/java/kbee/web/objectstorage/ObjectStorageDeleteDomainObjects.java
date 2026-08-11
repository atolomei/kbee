package kbee.web.objectstorage;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.novamens.beans.BeansService;
import com.novamens.content.command.Command;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.command.CommandService;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KeyValue;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.NumberField;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.command.CommandStatusPanelV5;
import kbee.web.command.panel.CommandModel;
import kbee.web.form.EditButtonsV5;

/**
 * ObjectStorageDeleteDomainObjects
 */
public class ObjectStorageDeleteDomainObjects extends ObjectEditor<Domain> {

    /**
     * 
     * Delete -> Domain - Max Move -> Domain - Source OS - Dest. OS - Max Encrpyt ->
     * Domain - Max
     * 
     */
    private static final long serialVersionUID = 1L;

    static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ObjectStorageDeleteDomainObjects.class.getName());

    final boolean is_root = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
    final boolean is_domain_admin = ServiceLocator.getService(com.novamens.service.SecurityService.class)
            .isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
    final boolean is_service_admin = ServiceLocator.getService(com.novamens.service.SecurityService.class)
            .isMember(KbeeGlobalRole.SERVICE_ADMIN.getId());
    final boolean is_factory_admin = ServiceLocator.getService(com.novamens.service.SecurityService.class)
            .isMember(KbeeGlobalRole.DOMAIN_FACTORY_MANAGER.getId());

    private boolean is_executing;

    private IModel<OffsetDateTime> frommodel;
    private IModel<OffsetDateTime> tomodel;
    private IModel<KeyValue<String>> domainmodel;

    private Boolean is_kbee_domain;
    private IModel<Long> maxmodel = new Model<Long>(Long.valueOf(1000));

    private CommandStatusPanelV5 status_panel;

    /**
     * @param id
     */
    public ObjectStorageDeleteDomainObjects(String id) {
        super(id);
        setOutputMarkupId(true);
    }

    private void loadComponents() {
        frommodel = new Model<OffsetDateTime>(OffsetDateTime.now().minusDays(30));
        tomodel = new Model<OffsetDateTime>(OffsetDateTime.now());

        Form<Void> form = new Form<Void>("form", Disposition.VERTICAL);

        setDomainmodel(new Model<KeyValue<String>>(new KeyValue<String>("", "-1")));

//		getDomains().get(0))

        form.add(new ChoiceField<KeyValue<String>>("domainmodel", domainmodel,
                new PropertyModel<List<KeyValue<String>>>(this, "domains"), true) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isVisible() {
                return isKbeeDomain();
            }
        });

        form.add(new NumberField<Long>("maxmodel", maxmodel) {
            private static final long serialVersionUID = 1L;

            @Override
            public void onUpdate(AjaxRequestTarget target) {
                try {
                    maxmodel.setObject(getValue());
                } catch (Exception e) {
                    logger.error(e);
                }
            }
        });

        addOrReplace(form);

        form.add(new InvisiblePanel("command-status"));

        form.add(new EditButtonsV5<Domain>(this, false) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isVisible() {
                return (isKbeeDomain() && is_root);
            }

            @Override
            public boolean isEnabled() {
                if (is_executing)
                    return false;
                return (isKbeeDomain() && is_root);
            }

            @Override
            protected IModel<String> getSubmitLabel() {
                return new Model<String>("execute");
            }

            @Override
            protected String getCancelClass() {
                return "btn btn-default btn-sm";
            }

            @Override
            protected String getSubmitClass() {
                return "btn btn-primary btn-sm";
            }
        });

    }

    /**
     * 
     */
    @Override
    public void onInitialize() {
        super.onInitialize();

        if (!isKbeeDomain())
            throw new KbeeRuntimeException("authorization error");

        setEditionEnabled(false);
        is_executing = false;

        loadComponents();

        AjaxLink<Void> cf = new AjaxLink<Void>("clear-form") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                loadComponents();
                target.add(ObjectStorageDeleteDomainObjects.this);
            }
        };
        add(cf);
    }

    /**
     * 
     * from to domain limit
     * 
     */
    public void update(AjaxRequestTarget target) {

        Serializable command_id = null;

        Map<String, Object> map = new HashMap<String, Object>();

        if (maxmodel.getObject() != null) {
            if (maxmodel.getObject().longValue() > 0) {
                logger.debug(maxmodel.getObject().longValue());
                map.put("max", String.valueOf(Long.valueOf(maxmodel.getObject())));
            } else
                map.remove("max");
        }

        if (getDomainmodel() != null) {
            map.put("domain", getDomainmodel().getObject().value); // domain id
        }

        CommandService service = ServiceLocator.getService(CommandService.class);

        Command command = (Command) ServiceLocator.getService(BeansService.class).getBean("ObjectStorageDomainDeleteCommand");
        if (command != null) {
            command.setParameters(map);
            service.add(command);
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
            }
            command_id = command.getId();
        }

        if (command_id != null) {
            status_panel = new CommandStatusPanelV5("command-status",
                    new CommandModel(ServiceLocator.getService(CommandService.class).getCommand(command_id))) {
                private static final long serialVersionUID = 1L;

                @Override
                public void onAfterExecution(AjaxRequestTarget target) {
                    is_executing = false;
                    target.add(ObjectStorageDeleteDomainObjects.this);
                }
            };
            ((Form) get("form")).addOrReplace(status_panel);
        }
        target.add(ObjectStorageDeleteDomainObjects.this);
    }

    /**
     * 
     * @return
     */
    public List<KeyValue<String>> getDomains() {
        List<KeyValue<String>> queries = new ArrayList<KeyValue<String>>();

        // queries.add(new KV<String>("All", "all"));

        for (Domain domain : getContentDao().getAllDomains()) {
            if (domain.getState() == ObjectState.DELETED)
                queries.add(
                        new KeyValue<String>(domain.getName() + " - " + domain.getOrganization() + " ", domain.getId().toString()));
        }

        queries.sort(new Comparator<KeyValue<String>>() {
            @Override
            public int compare(KeyValue<String> a, KeyValue<String> b) {
                if (a.getKey().equals("All"))
                    return -1;
                if (b.getKey().equals("All"))
                    return 1;
                return a.getKey().toString().compareToIgnoreCase(b.getKey().toString());
            }
        });
        return queries;
    }

    public IModel<KeyValue<String>> getDomainmodel() {
        return domainmodel;
    }

    public void setDomainmodel(IModel<KeyValue<String>> domainmodel) {
        this.domainmodel = domainmodel;
    }

    public IModel<OffsetDateTime> getFrommodel() {
        return frommodel;
    }

    public void setFrommodel(IModel<OffsetDateTime> frommodel) {
        this.frommodel = frommodel;
    }

    public IModel<OffsetDateTime> getTomodel() {
        return tomodel;
    }

    public void setTomodel(IModel<OffsetDateTime> tomodel) {
        this.tomodel = tomodel;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (status_panel != null)
            status_panel.detach();
    }

    protected Domain getDomain() {
        return (Domain) ServiceLocator.getService(UserService.class).getDomain();
    }

    protected User getSessionUser() {
        return ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();
    }

    protected ContentDao getContentDao() {
        return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
    }

    protected boolean isKbeeDomain() {

        if (this.is_kbee_domain == null) {
            try {
                this.is_kbee_domain = Boolean.valueOf(getDomain().getName().toLowerCase().trim().equals("kbee"));
            } catch (Exception e) {
                logger.error(e);
                this.is_kbee_domain = Boolean.valueOf(false);
            }
        }
        return this.is_kbee_domain.booleanValue();

    }

}
