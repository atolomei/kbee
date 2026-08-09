package com.novamens.kbee.wicket.markup.html.console.panel;

import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.IResourceStream;

import java.io.File;
import java.nio.file.Paths;

public class AJAXDownload extends AbstractDefaultAjaxBehavior {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String path;
    private boolean addAntiCache;

    public AJAXDownload() {
        this(true);
    }

    public AJAXDownload(boolean addAntiCache) {
        super();
        this.addAntiCache = addAntiCache;
    }

    public void initiate(AjaxRequestTarget target) {
        String url = getCallbackUrl().toString();
        if (addAntiCache) {
            url = url + (url.contains("?") ? "&" : "?");
            url = url + "antiCache=" + System.currentTimeMillis();
        }
        // the timeout is needed to let Wicket release the channel
        target.appendJavaScript("setTimeout(\"window.location.href='" + url + "'\", 100);");

    }

    protected void respond(AjaxRequestTarget target) {
        ResourceStreamRequestHandler handler = new ResourceStreamRequestHandler(getResourceStream(), getFileName());
        handler.setContentDisposition(ContentDisposition.ATTACHMENT);
        getComponent().getRequestCycle().scheduleRequestHandlerAfterCurrent(handler);
    }

    protected String getFileName() {
        return Paths.get(this.path).getFileName().toString();
    }

    protected IResourceStream getResourceStream() {
        File file = new File(path);
        return new FileResourceStream(file);
    }

    public void setFile(File file) {
        path = file.getAbsolutePath();
    }

}
