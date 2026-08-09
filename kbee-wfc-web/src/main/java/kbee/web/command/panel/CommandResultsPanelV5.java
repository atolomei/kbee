package kbee.web.command.panel;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.novamens.content.resource.KBFile;
import com.novamens.kbee.wicket.markup.html.console.panel.AJAXDownload;
import kbee.web.resource.WebFileReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.beans.BeansService;
import com.novamens.content.command.Command;
import com.novamens.content.dao.ContentDao;
import com.novamens.datetime.DateTimeService;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import org.apache.wicket.request.cycle.RequestCycle;

/**
 *
 */
public class CommandResultsPanelV5 extends CommandAbstractPanel {

    private static final long serialVersionUID = 1L;

    private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(CommandResultsPanelV5.class.getName());

    private List<Panel> panels;
    AJAXDownload resultFileDownload;
    AJAXDownload logFileDownload;

    public CommandResultsPanelV5(String id, IModel<Command> command_model) {
        super(id, command_model);
        setOutputMarkupId(true);
    }

    @Override
    public void onInitialize() {
        super.onInitialize();

        add(new Label("title", "Results"));

        add(new ListView<Panel>("result", getPanels()) {

            private static final long serialVersionUID = 1L;

            protected void populateItem(ListItem<Panel> item) {
                item.setOutputMarkupId(true);
                item.add(item.getModelObject());
                item.setVisible(item.getModelObject().isVisible());
            }
        });

        resultFileDownload = new AJAXDownload();
        add(resultFileDownload);
        add(new AjaxLink<String>("downloadResultFile", new Model<String>("Download Result")) {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                KBFile file = CommandResultsPanelV5.this.getModel().getObject().getResultFile();
                try {
                    resultFileDownload.setFile(file.getFile());
                    resultFileDownload.initiate(ajaxRequestTarget);
                } catch (IOException e) {
                	logger.error(e);
                }

            }

            @Override
            public boolean isVisible() {
                return CommandResultsPanelV5.this.getModel().getObject().getResultFile() != null;
            }
        });


        logFileDownload = new AJAXDownload();
        add(logFileDownload);
        add(new AjaxLink<String>("downloadLogFile", new Model<String>("Download Log File")) {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                File file = CommandResultsPanelV5.this.getModel().getObject().getLogFile();
                logFileDownload.setFile(file);
                logFileDownload.initiate(ajaxRequestTarget);

            }

            @Override
            public boolean isVisible() {
                File file = CommandResultsPanelV5.this.getModel().getObject().getLogFile();
                 return file != null && file.exists();
            }
        });
    }

    @SuppressWarnings("serial")
    public List<Panel> getPanels() {

        if (this.panels != null)
            return this.panels;

        this.panels = new ArrayList<Panel>();

        Model<String> dt = new Model<String>() {
            public String getObject() {
                if (CommandResultsPanelV5.this.getModel().getObject().isTerminated())
                    return ServiceLocator.getService(DateTimeService.class).timeElapsed(CommandResultsPanelV5.this.getModel().getObject().getDateTerminated());
                else
                    return "n/a";
            }
        };

        this.panels.add(new CommandAttributePanelV5("command_item", new Model<String>("Terminated"), dt));

        Model<String> dm = new Model<String>() {
            public String getObject() {
                DateTimeService service = ServiceLocator.getService(DateTimeService.class);
                return service.formatLapseSeconds(getModel().getObject().getDuration(), getSessionUser().getLocale(), "ago");
            }
        };

        this.panels.add(new CommandAttributePanelV5("command_item", new Model<String>("Duration"), dm));
        this.panels.add(new CommandAttributePanelV5("command_item", new Model<String>("Results"), () -> CommandResultsPanelV5.this.getModel().getObject().getResult()));
        this.panels.add(new CommandAttributePanelV5("command_item", new Model<String>("Results details"), () -> CommandResultsPanelV5.this.getModel().getObject().getResultDetails()));
        
        this.panels.add(new CommandAttributePanelV5("command_item", new Model<String>("Comments"), () -> CommandResultsPanelV5.this.getModel().getObject().getResultComment()));
		
        //this.panels.add(new CommandAttributePanelV5("command_item", new Model<String>("Details"), () -> CommandResultsPanelV5.this.getModel().getObject().getResultDetails()) {
         //   @Override
         //   public boolean isVisible() {
          //      return CommandResultsPanelV5.this.getModel().getObject().getResultDetails() != null && !CommandResultsPanelV5.this.getModel().getObject().getResultDetails().isEmpty();
          //  }
        //});
        return this.panels;
    }


    @SuppressWarnings("unchecked")
    @Override
    public void onDetach() {
        ListView<Panel> lv = (ListView<Panel>) get("result");
        if (lv != null)
            for (Panel panel : lv.getList())
                panel.detach();
        if (this.panels != null)
            for (Panel panel : this.panels)
                panel.detach();
        super.onDetach();
    }

    protected ContentDao getContentDao() {
        return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
    }

    protected User getSessionUser() {
        return ServiceLocator.getService(SecurityService.class).getSessionUser();
    }


    protected String getEstimatedTimeComplete() {
        Command cmd = getModel().getObject();
        Double value = Double.valueOf(cmd.estimatedSecsToEnd() * 1000.0);
        Long lv = value.longValue();
        if (lv < 0)
            return "N/A";

        DateTimeService service = ServiceLocator.getService(DateTimeService.class);
        return service.formatLapseSeconds(lv, getSessionUser().getLocale(), "ago");

    }


}

