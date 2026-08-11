package kbee.web.objectstorage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;

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
import com.novamens.util.KbeeRuntimeException;
import com.novamens.util.KeyValue;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.NumberField;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.objectstorage.command.ObjectStoragePurgeCommand;
import kbee.web.command.CommandStatusPanelV5;
import kbee.web.command.panel.CommandModel;
import kbee.web.form.EditButtonsV5;

public class ObjectStoragePurgeDomainObjects extends ObjectEditor<Domain> {

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

    private Boolean is_kbee_domain;
    private IModel<Long> maxmodel = new Model<Long>(Long.valueOf(1000));
    private IModel<Boolean> onlyList = new Model<Boolean>(Boolean.valueOf(false));

    private CommandStatusPanelV5 status_panel;

    public void setOnlyList(IModel<Boolean> m) {
        this.onlyList = m;
    }

    public IModel<Boolean> getOnlyList() {
        return this.onlyList;
    }

    /**
     * @param id
     */
    public ObjectStoragePurgeDomainObjects(String id) {
        super(id);
        setOutputMarkupId(true);
    }

    private void loadComponents() {

        Form<Void> form = new Form<Void>("form", Disposition.VERTICAL);

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

        form.add(new BooleanField("onlylist", onlyList) {
            private static final long serialVersionUID = 1L;

            protected String getFalseStr() {
                return "Delete";
            }

            protected String getTrueStr() {
                return "List";
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
                target.add(ObjectStoragePurgeDomainObjects.this);
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

        map.put("server", getServerUrl());
        map.put("client", getClientIP());
        map.put("removefiles", getOnlyList().getObject().toString().equals("true") ? "false" : "true");

        if (maxmodel.getObject() != null) {
            if (maxmodel.getObject().longValue() > 0) {
                logger.debug(maxmodel.getObject().longValue());
                map.put("max", String.valueOf(Long.valueOf(maxmodel.getObject())));
            } else
                map.remove("max");
        }

        CommandService service = ServiceLocator.getService(CommandService.class);

        Command command = (Command) ServiceLocator.getService(BeansService.class)
                .getBean(ObjectStoragePurgeCommand.class.getSimpleName());

        if (command != null) {
            command.setParameters(map);
            service.add(command);
            try {
                Thread.sleep(1200);
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
                    target.add(ObjectStoragePurgeDomainObjects.this);
                }
            };
            ((Form<?>) get("form")).addOrReplace(status_panel);
        }
        target.add(ObjectStoragePurgeDomainObjects.this);
    }

    /**
     * 
     * @return
     */
    public List<KeyValue<String>> getDomains() {
        List<KeyValue<String>> queries = new ArrayList<KeyValue<String>>();

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

    protected String getServerUrl() {
        String protocol = ((WebRequest) RequestCycle.get().getRequest()).getUrl().getProtocol();
        String host = ((WebRequest) RequestCycle.get().getRequest()).getUrl().getHost();
        Integer iport = ((WebRequest) RequestCycle.get().getRequest()).getUrl().getPort();
        String port = (iport.equals(80) || iport.equals(443) ? "" : (":" + iport.toString()));
        return protocol + "://" + host + port;
    }

    protected String getClientIP() {
        try {
            WebRequest req = (WebRequest) RequestCycle.get().getRequest();
            HttpServletRequest httpReq = (HttpServletRequest) req.getContainerRequest();
            String clientAddress = httpReq.getRemoteHost();
            return clientAddress;
        } catch (Exception e) {
            logger.error(e);
            return "";
        }
    }
}
