package com.novamens.kbee.wicket.markup.html.console.browser;


import java.io.File;
import java.io.IOException;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.IResourceStream;

import kbee.web.console.BaseBrowser;

@SuppressWarnings("serial")
public class DownloadButton extends ToolbarItem {
	
	private static final long serialVersionUID = 1L;
	
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DownloadButton.class.getName());

	/** ----
	 * 
	 *
	 */
	public static class AJAXDownload extends AbstractDefaultAjaxBehavior {
		private String path;
		private boolean addAntiCache;
		
		public AJAXDownload() {
			this(true);
		}
		public AJAXDownload(boolean addAntiCache) {
			super();
			this.addAntiCache = addAntiCache;
		}
		public void setFile(File file) {
			path = file.getAbsolutePath();
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
			return null;
		}
		protected IResourceStream getResourceStream() {
			File file = new File(path);
			return new FileResourceStream(file);
		};
	}

	/***
	 * 
	 * 
	 * 
	 * @param browser
	 * @param align
	 */
	public DownloadButton(BaseBrowser<?> browser, Align align) {
		super(browser, align);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		if (get("link")==null) {
			addLink();
		}
	}
	
	protected void addLink() {
		
		final AJAXDownload download = new AJAXDownload() {
			protected String getFileName() {
				return DownloadButton.this.getFileName();
			}
		};
		add(download);
		
		AjaxLink<?> link = new AjaxLink<Void>("link") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				try {
					File file = getFile();
					download.setFile(file);
				}
				catch (IOException e) {
					logger.error(e);
				}
				download.initiate(target);
			}

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				IAjaxCallListener listener = new IAjaxCallListener() {
					@Override
					public CharSequence getSuccessHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getPrecondition(Component component) {
						return null;
					}
					@Override
					public CharSequence getFailureHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getCompleteHandler(Component component) {
						String s = null, s1=null;
						String id = component.getMarkupId();
						s1 = "document.getElementById('"+id+"').innerHTML = '"+"<span class=\"far fa-cloud-download-alt\"></span>"+"';";
						s ="setTimeout(function () {"+s1+"}, 350);";
						return s;
					}
					@Override
					public CharSequence getBeforeSendHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getBeforeHandler(Component component) {
						String s = null;
						String id = component.getMarkupId();
						s = "document.getElementById('"+id+"').innerHTML = '<i class=\"far fa-sync fa-spin fa-fw spinning\" ></i>'";
						return s;
					}

					@Override
					public CharSequence getAfterHandler(Component component) {
						return null;
					}

					@Override
					public CharSequence getDoneHandler(Component component) {
						return null;
					}

					@Override
					public CharSequence getInitHandler(Component component) {
						return null;
					}
				};
				attributes.getAjaxCallListeners().add(listener);
			}
		};

		add(link);
		
		WebMarkupContainer icon = new WebMarkupContainer("icon");
		
		link.add(icon);
		
		add(link);
	}
	
	protected IModel<String> getLabel() {
		return new StringResourceModel("download-label", this, null);
	}
	
	protected void onClick(AjaxRequestTarget target) {
		
	}
	
	protected File getFile() throws IOException {
		return null;
	}
	
	protected String getFileName() {
		return null;
	}

}
