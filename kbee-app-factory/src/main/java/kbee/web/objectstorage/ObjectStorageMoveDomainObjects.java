package kbee.web.objectstorage;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.command.Command;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.KBFSStorageType;
import com.novamens.kbee.command.CommandService;
import com.novamens.kbee.kbfs.KbeeShardedMinioFileServer;
import com.novamens.kbee.kbfs.KbeeShardedOdilonFileServer;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.kbfs.FileServerMinio;
import com.novamens.kbfs.FileServerOdilon;
import com.novamens.kbfs.FileServerS3;
import com.novamens.kbfs.v1.FileServerV1;
import com.novamens.portal6.model.PortalObject;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.system.properties.SystemPropertiesService;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.util.KeyValue;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.NumberField;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.objectstorage.command.ObjectStorageDomainMoveCommand;
import kbee.util.PropertiesFactory;
import kbee.web.command.CommandStatusPanelV5;
import kbee.web.command.panel.CommandModel;
import kbee.web.form.EditButtonsV5;
import kbee.web.portal6.panel.PortalErrorPanel;

public class ObjectStorageMoveDomainObjects extends ObjectEditor<Domain> {

    private static final long serialVersionUID = 1L;

    static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ObjectStorageMoveDomainObjects.class.getName());

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

    private IModel<KeyValue<String>> sourcemodel;
    private IModel<KeyValue<String>> destinationmodel;

    private Boolean is_kbee_domain;
    private IModel<Long> hdmodel = new Model<Long>(Long.valueOf(100));

    private CommandStatusPanelV5 status_panel;

    private String bean_command = ObjectStorageDomainMoveCommand.class.getSimpleName();

    /**
     * @param id
     */
    public ObjectStorageMoveDomainObjects(String id) {
        super(id);
        setOutputMarkupId(true);
    }

    /**
     * 
     */
    @Override
    public void onInitialize() {
        super.onInitialize();

        if (!isKbeeDomain())
            throw new KbeeRuntimeException("Domain must be kbee");

        setEditionEnabled(false);
        is_executing = false;

        loadComponents();

        AjaxLink<Void> cf = new AjaxLink<Void>("clear-form") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                loadComponents();
                target.add(ObjectStorageMoveDomainObjects.this);
            }
        };
        add(cf);
    }

    /**
     * 
     * from to domain limit
     */
    public void update(AjaxRequestTarget target) {

        Serializable command_id = null;

        Map<String, Object> map = new HashMap<String, Object>();

        try {

            if (hdmodel != null && hdmodel.getObject() != null) {
                if (hdmodel.getObject().longValue() > 0) {
                    logger.debug(hdmodel.getObject().longValue());
                    map.put("hd", String.valueOf(Long.valueOf(hdmodel.getObject())));
                } else
                    map.remove("hd");
            }

            if (sourcemodel != null && sourcemodel.getObject() != null) {
                logger.debug(sourcemodel.getObject());
                map.put("source", sourcemodel.getObject().getValue());
                map.put("source_shard", sourcemodel.getObject().getLink());
            } else {
                map.remove("source");
                map.remove("source_shard");
            }

            if (destinationmodel != null && destinationmodel.getObject() != null) {
                logger.debug(destinationmodel.getObject());
                map.put("destination", destinationmodel.getObject().getValue());
                map.put("destination_shard", destinationmodel.getObject().getLink());
            } else {
                map.remove("destination");
                map.remove("destination_shard");
            }

            if (getDomainmodel() != null) {
                map.put("domain", getDomainmodel().getObject().value); // domain id
            }

            StringBuilder str = new StringBuilder();
            boolean err = false;

            if (!map.containsKey("hd")) {
                err = true;
                str.append((str.length() > 0 ? "|" : "") + "hd is null");
            }

            if (!map.containsKey("domain")) {
                err = true;
                str.append((str.length() > 0 ? "|" : "") + "domain is null");
            }

            if (!map.containsKey("source")) {
                err = true;
                str.append((str.length() > 0 ? "|" : "") + "source is null");
            }

            if (!map.containsKey("destination")) {
                err = true;
                str.append((str.length() > 0 ? "|" : "") + "destination is null");
            }

            if (!err && map.get("source").equals(map.get("destination"))) {

                if (map.get("source_shard") == null && map.get("destination_shard") == null) {
                    err = true;
                    str.append((str.length() > 0 ? "|" : "") + "source and destination are the same");
                } else if (map.get("source_shard") != null && map.get("destination_shard") != null) {
                    if (map.get("source_shard").equals(map.get("destination_shard"))) {
                        err = true;
                        str.append((str.length() > 0 ? "|" : "") + "source and destination are the same");
                    }
                }
            }

            logger.debug(map.toString());

            if (err) {
                logger.error(str.toString());
                PortalErrorPanel<PortalObject> err_p = new PortalErrorPanel<>("error", null, new Model<String>(str.toString()));
                ((Form<?>) get("form")).addOrReplace(err_p);
                target.add(ObjectStorageMoveDomainObjects.this);
                return;
            }

        } catch (Exception e) {
            logger.error(e);
            PortalErrorPanel<PortalObject> err_p = new PortalErrorPanel<>("error", null, e);
            ((Form<?>) get("form")).addOrReplace(err_p);
            target.add(ObjectStorageMoveDomainObjects.this);
            return;
        }

        CommandService service = ServiceLocator.getService(CommandService.class);

        Command command = (Command) ServiceLocator.getService(BeansService.class).getBean(bean_command);

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
                    target.add(ObjectStorageMoveDomainObjects.this);
                }
            };
            ((Form<?>) get("form")).addOrReplace(status_panel);
            ((Form<?>) get("form")).addOrReplace(new InvisiblePanel("error"));
        }
        target.add(ObjectStorageMoveDomainObjects.this);
    }

    /**
     * @return
     */
    public List<KeyValue<String>> getDomains() {
        List<KeyValue<String>> queries = new ArrayList<KeyValue<String>>();

        for (Domain domain : getContentDao().getDomains())
            queries.add(
                    new KeyValue<String>(domain.getOrganization() + " ( " + domain.getName() + " )", domain.getId().toString()));

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

    /**
     * @return
     */
    public List<KeyValue<String>> getStorages() {

        List<KeyValue<String>> sto = new ArrayList<KeyValue<String>>();

      
        boolean kbfs2_enabled = PropertiesFactory.getInstance("kbee").getProperties().getProperty("kbfs2.enabled", "yes")
                .toLowerCase().trim().equals("yes");

        if (kbfs2_enabled) {

            FileServerMinio fsv2 = ServiceLocator.getService(FileServerMinio.class);

            if (fsv2 instanceof KbeeShardedMinioFileServer) {
                for (Entry<Integer, FileServerMinio> entry : ((KbeeShardedMinioFileServer) fsv2).getShards().entrySet()) {
                    sto.add(new KeyValue<String>(
                            "Minio_" + String.valueOf(entry.getKey().intValue()) + " - " + entry.getValue().getEndPoint(),
                            FileServerMinio.KEY + "_" + String.valueOf(entry.getKey().intValue()),
                            String.valueOf(entry.getKey().intValue())));
                }
            } else {
                sto.add(new KeyValue<String>("Minio_1 - " + fsv2.getEndPoint(), FileServerMinio.KEY + "_1"));
            }
        }

        boolean odilon_enabled = PropertiesFactory.getInstance("kbee").getProperties().getProperty("odilon.enabled", "yes")
                .toLowerCase().trim().equals("yes");

        if (odilon_enabled) {

            FileServerOdilon fsv2 = ServiceLocator.getService(FileServerOdilon.class);

            if (fsv2 instanceof KbeeShardedOdilonFileServer) {
                for (Entry<Integer, FileServerOdilon> entry : ((KbeeShardedOdilonFileServer) fsv2).getShards().entrySet()) {
                    sto.add(new KeyValue<String>(
                            "Odilon_" + String.valueOf(entry.getKey().intValue()) + " - " + entry.getValue().getEndPoint(),
                            FileServerOdilon.KEY + "_" + String.valueOf(entry.getKey().intValue()),
                            String.valueOf(entry.getKey().intValue())));
                }
            } else {
                sto.add(new KeyValue<String>("Odilon_1 - " + fsv2.getEndPoint(), FileServerOdilon.KEY + "_1"));
            }
        }

        try {
            FileServerS3 s3 = ServiceLocator.getService(FileServerS3.class);
            if (s3 != null && s3.isEnabled()) {
                sto.add(new KeyValue<String>("Amazon S3 - " + s3.getEndPoint(), FileServerS3.KEY));
            }
        } catch (Exception e) {
            logger.error(e);
        }

        return sto;
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

    public KBFSStorageType getDefaultKBFSStorageType() {
        return KBFSStorageType.getByKey(getContentDao().findSystemParameterValueByKey("kbfs.storage.default",
                ServiceLocator.getService(SystemPropertiesService.class).getDefaultKBFSService()));
    }

    /**
     * ---------------------------------------------
     * 
     */
    private void loadComponents() {

        this.frommodel = new Model<OffsetDateTime>(OffsetDateTime.now().minusDays(30));
        this.tomodel = new Model<OffsetDateTime>(OffsetDateTime.now());

        Form<Void> form = new Form<Void>("form", Disposition.VERTICAL);
        form.addOrReplace(new InvisiblePanel("error"));

        setDomainmodel(new Model<KeyValue<String>>(new KeyValue<String>("", "-1")));

        form.add(new ChoiceField<KeyValue<String>>("domainmodel", domainmodel,
                new PropertyModel<List<KeyValue<String>>>(this, "domains"), true) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isVisible() {
                return isKbeeDomain();
            }
        });

        form.add(new NumberField<Long>("hdmodel", hdmodel) {
            private static final long serialVersionUID = 1L;

            @Override
            public void onUpdate(AjaxRequestTarget target) {
                try {
                    hdmodel.setObject(getValue());
                } catch (Exception e) {
                    logger.error(e);
                }
            }
        });

        this.sourcemodel = new Model<KeyValue<String>>(getStorages().get(0));

        form.add(new ChoiceField<KeyValue<String>>("source", this.sourcemodel,
                new PropertyModel<List<KeyValue<String>>>(this, "storages"), true) {
            private static final long serialVersionUID = 1L;

            @Override
            public void onUpdate(AjaxRequestTarget target) {
                try {
                    sourcemodel.setObject(getValue());

                } catch (Exception e) {
                    logger.error(e);
                }
            }

            @Override
            public boolean isVisible() {
                return isKbeeDomain();
            }
        });

        this.destinationmodel = new Model<KeyValue<String>>(getStorages().get(0));

        form.add(new ChoiceField<KeyValue<String>>("destination", this.destinationmodel,
                new PropertyModel<List<KeyValue<String>>>(this, "storages"), true) {
            private static final long serialVersionUID = 1L;

            @Override
            public void onUpdate(AjaxRequestTarget target) {
                try {
                    destinationmodel.setObject(getValue());
                } catch (Exception e) {
                    logger.error(e);
                }
            }

            @Override
            public boolean isVisible() {
                return isKbeeDomain();
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
                return new Model<String>(new StringResourceModel("execute", ObjectStorageMoveDomainObjects.this, null).getObject());
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
}
