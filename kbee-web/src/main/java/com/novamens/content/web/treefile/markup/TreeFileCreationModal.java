package com.novamens.content.web.treefile.markup;

import java.io.File;
import java.io.Serializable;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;


import com.novamens.content.base.Content;
import com.novamens.content.command.Command;
import com.novamens.content.command.CommandState;
import com.novamens.content.model.ObjectId;
import com.novamens.content.user.UserService;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.kbee.command.CommandService;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.system.parameters.SystemParameterService;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.modal.Modal;

import kbee.util.NumberFormatter;
import kbee.web.command.TreeFileCreationCommand;
import kbee.web.fs.FileSystemSelector;

@SuppressWarnings("serial")
public class TreeFileCreationModal<T extends Content> extends Modal {
	private static final long serialVersionUID = 1L;
	
	protected static final ResourceReference AW = new CssResourceReference(Form.class, Form.FONTAWESOME );
	protected static final ResourceReference BL = new CssResourceReference(Form.class, "build.css");
	
	public static Button ButtonNext = new Button("button.next", "btn btn-sm btn-primary", ButtonType.BUTTON, false);
	public static Button ButtonPrevious = new Button("button.previous", "btn btn-sm btn-primary", ButtonType.BUTTON, false);
	public static Button ButtonConfirm = new Button("button.confirm", "btn btn-sm btn-primary", ButtonType.BUTTON, false);
	
	private IModel<File> foldermodel;
	private IModel<T> contentmodel;
	private List<IModel<File>> indexes;

	public class FolderSelectorStep extends Fragment {
		public FolderSelectorStep(String id) {
			super(id, "folder-selection-step-fragment", TreeFileCreationModal.this);
			add(new FileSystemSelector("selector", getUserHome(), false, true, false) {
				@Override
				public void onSelection(AjaxRequestTarget target, List<IModel<File>> selection) {
					if (!selection.isEmpty()) {
						IModel<File> model = selection.get(0);
						if (model.getObject().isDirectory()) {
							foldermodel = model;
							setStep(new IndexSelectorStep("step"));
							target.add(TreeFileCreationModal.this.get("modal-dialog"));
						}
					}
				}
			});
			replaceButtons(Modal.Cancel);
			setHandler(new Handler() {
				public void onClick(AjaxRequestTarget target, Button button) {
					if (button.equals(Modal.Cancel)) {
						target.appendJavaScript("$('#"+TreeFileCreationModal.this.getMarkupId()+"').modal('hide')");
					}
				}
			});
			addFooter(this);
		}
	};
	
	public class IndexSelectorStep extends Fragment {
		public IndexSelectorStep(String id) {
			super(id, "index-selection-step-fragment", TreeFileCreationModal.this);
			add(new FileSystemSelector("selector", foldermodel.getObject(), true, true, false));
			replaceButtons(ButtonPrevious, ButtonNext, Modal.Cancel);
			setHandler(new Handler() {
				public void onClick(AjaxRequestTarget target, Button button) {
					if (button.equals(ButtonPrevious)) {
						foldermodel = null;
						setStep(new FolderSelectorStep("step"));
						target.add(TreeFileCreationModal.this.get("modal-dialog"));
					}
					if (button.equals(ButtonNext)) {
						indexes = ((FileSystemSelector)IndexSelectorStep.this.get("selector")).getSelection();
						setStep(new ConfirmationStep("step"));
						target.add(TreeFileCreationModal.this.get("modal-dialog"));
					}
				}
			});
			addFooter(this);
		}
	}
	
	public class ConfirmationStep extends Fragment {
		public ConfirmationStep(String id) {
			super(id, "confirmation-step-fragment", TreeFileCreationModal.this);
			
			boolean error = false;
			Label message;
			if (foldermodel==null || indexes==null || indexes.isEmpty()) {
				message = new Label("message", getErrorMessage());
				error = true;
			}
			else {
				message = new Label("message", getMessage());
			}
			message.setEscapeModelStrings(false);
			add(message);
			
			if (error)
				replaceButtons(ButtonPrevious, Modal.Cancel);
			else
				replaceButtons(ButtonPrevious, ButtonConfirm, Modal.Cancel);
			setHandler(new Handler() {
				public void onClick(AjaxRequestTarget target, Button button) {
					if (button.equals(ButtonPrevious)) {
						setStep(new IndexSelectorStep("step"));
						target.add(TreeFileCreationModal.this.get("modal-dialog"));
					}
					if (button.equals(ButtonConfirm)) {
						setStep(new ExecutionStep("step"));
						target.add(TreeFileCreationModal.this.get("modal-dialog"));
					}
				}
			});
			addFooter(this);
		}
		private String getMessage() {
			String message = (new StringResourceModel("confirmation-message",TreeFileCreationModal.this)).getObject();
			message = message.replace("%1", foldermodel.getObject().getName());
			String indexesnames = "";
			for (IModel<File> index : indexes) {
				if (!"".equals(indexesnames)) indexesnames += ", ";
				indexesnames += getIndexPath(index.getObject());
			}
			message = message.replace("%2", indexesnames);
			return message;
		}
		private String getErrorMessage() {
			String message = (new StringResourceModel("error-message",TreeFileCreationModal.this)).getObject();
			return message;
		}
		private String getIndexPath(File file) {
			String treepath = foldermodel.getObject().getAbsolutePath();
			String filepath = file.getAbsolutePath();
			String indexpath = filepath.replace(treepath, "");
			indexpath = foldermodel.getObject().getName() + indexpath;
			return indexpath;
		}
	}
	
	public class ExecutionStep extends Fragment {
		private boolean started = false;
		private Serializable commandId;
		public ExecutionStep(String id) {
			super(id, "execution-step-fragment", TreeFileCreationModal.this);
			add(new Label("started", new Model<String>() {
				public String getObject() {
					return getCommand().getState().equals(CommandState.RUNNING) ? ServiceLocator.getService(DateTimeService.class).timeElapsed(getTimeStarted()) : "-";
				}
			}));
			((Label)get("started")).setEscapeModelStrings(false);
			add(new Label("status", () -> getStatus()));
			((Label)get("status")).setEscapeModelStrings(false);
			add(new Label("progress", new Model<String>() {
				public String getObject() {
					String value =  NumberFormatter.formatNumber(getProgress());
					return value.trim()+" %";
				}
			}));
			add(new Label("total", () -> String.valueOf(getTotal())));
			add(new Label("processed", () -> String.valueOf(getTotalProcessed())));
			add(new Label("estimated", new Model<String>() {
				public String getObject() {
					Double value =  Double.valueOf(getEstimatedTimeComplete() * 1000.0);
					Long lv = value.longValue();
					if (lv<0)return "-";
					DateTimeService service = ServiceLocator.getService(DateTimeService.class);
					return service.formatLapseSeconds(lv, getSessionUser().getLocale(), "ago");
				}
			}));
			((Label)get("estimated")).setEscapeModelStrings(false);
			add(new AbstractAjaxTimerBehavior(Duration.ofSeconds(1)) {
				@Override
				protected void onTimer(AjaxRequestTarget target) {
					try {
						if (getCommand().isTerminated()){
							replaceButtons(Modal.OK);
							this.stop(target);
//							onAfterExecution(target);
						}
						target.add(ExecutionStep.this);
					} 
					catch (Exception e) {
						//logger.error(e);
					}
				}
			});
			replaceButtons(Modal.Cancel);
			setHandler(new Handler() {
				public void onClick(AjaxRequestTarget target, Button button) {
				}
			});
			addFooter(this);
		}
		public double getProgress() {
			return getCommand()!=null ? getCommand().getProgress() : 0; 
		}
		public OffsetDateTime getTimeStarted() {
			return getCommand()!=null ? getCommand().getDateStarted() : null; 
		}
		public long getTotal() {
			return getCommand()!=null ? getCommand().getTotalItems() : 0; 
		}
		public long getTotalProcessed() {
			return getCommand()!=null ? getCommand().getTotalItemsProcessed() : 0; 
		}
		public double getEstimatedTimeComplete() {
			return getCommand()!=null ? getCommand().estimatedSecsToEnd() : 0; 
		}
		public String getStatus() {
			if (getCommand()==null) return "";
			return	"<span class=\""+getCommand().getState().getCss()+"\">" +
				getCommand().getState().getLabel() + "</span>";
		}	
		public Command getCommand() {
			return commandId!=null ? ServiceLocator.getService(CommandService.class).getCommand(commandId) : null;
		}
		@Override
		public void onBeforeRender() {
			super.onBeforeRender();
			if (!started) {
				Command command = new TreeFileCreationCommand();
				command.setParameter("content", new ObjectId(contentmodel.getObject()));
				command.setParameter("folder", foldermodel.getObject().getAbsolutePath());
				String indexes = "";
				for (IModel<File> index : TreeFileCreationModal.this.indexes) {
					if (!"".equals(indexes)) indexes += ", ";
					indexes += index.getObject().getAbsolutePath();
				}
				command.setParameter("indexes", indexes);
				ServiceLocator.getService(CommandService.class).add(command);
				commandId = command.getId();
				started = true;
			}
		}
	}

	public TreeFileCreationModal(String id, IModel<T> model) {
		super(id);
		this.contentmodel = model;
		setTitle("modal.treefilecreation.title");
		setStep(new FolderSelectorStep("step"));
	}
	
	@Override
	public void open(AjaxRequestTarget target) {
		target.appendJavaScript("$('#"+getMarkupId()+"').modal('show')");
		target.appendJavaScript("$('#"+getMarkupId()+"').on('hide.bs.modal', function (e) { refreshdialog"+TreeFileCreationModal.this.getMarkupId() + "();})");
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(AW));
		response.render(CssHeaderItem.forReference(BL));
	}
	
	protected void setStep(Component step) {
		if (get("modal-dialog")==null) {
			addComponents();
		}	
		WebMarkupContainer modal_dialog = (WebMarkupContainer)get("modal-dialog");
		modal_dialog.addOrReplace(step);
	}
	
	protected void addComponents() {
		WebMarkupContainer modal_dialog = new WebMarkupContainer("modal-dialog");
		modal_dialog.setOutputMarkupId(true);
		addOrReplace(modal_dialog);
		modal_dialog.add(new Label("title", getTitle()));
	}	
	
	protected void addFooter(WebMarkupContainer step) {
		WebMarkupContainer modal_footer = new WebMarkupContainer("modal-footer"); 
		step.add(modal_footer);
		modal_footer.add(new ListView<Button>("buttons", getButtons()) {
			public void populateItem(ListItem<Button> item) {
				final Button button = item.getModelObject();
				AbstractLink buttonlink;
				buttonlink = new AjaxLink<Void>("button") {
					@Override
					public void onClick(AjaxRequestTarget target) {
						TreeFileCreationModal.this.onClick(target, button);
						if (button.closeOnClick())
							target.appendJavaScript("$('#"+TreeFileCreationModal.this.getMarkupId()+"').modal('hide')");
					}
					@Override
					public boolean isVisible() {
						return button.isVisible();
					}
				};
				buttonlink.add(new AttributeModifier("class", button.getCssClass()));
				if (button.isCancel())
					buttonlink.add(new AttributeModifier("data-dismiss", "modal"));
				buttonlink.add(new Label("label", new StringResourceModel(button.key(), TreeFileCreationModal.this, null)));
				item.add(buttonlink);
			}
		});
		if (getFooterCss()!=null)
			modal_footer.add(new AttributeModifier("class", getFooterCss()));
		add(new RefreshBehavior());
	}
	
	protected File getUserHome() {
		File home = new File(getApplicationHome()+File.separator+getDomainHome()+File.separator+getUsernamePrefix());
		return home;
	}
	
	protected String getApplicationHome() {
		String drive_dir_name = ServiceLocator.getService(SystemParameterService.class).getParameter("integration.drive.home", "."+File.separator+"drive");
		return drive_dir_name;
	}
	
	protected String getDomainHome() {
		String home_dir_name = getDomain().getName().replace(" ", "").toLowerCase().trim();
		return home_dir_name;
	}
	
	protected String getUsernamePrefix() {
		return getSessionUser().getUserName().split("@")[0];
	}
	
	protected User getSessionUser() {
		User user = ServiceLocator.getService(SecurityService.class).getSessionUser();
		return user;
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
}
