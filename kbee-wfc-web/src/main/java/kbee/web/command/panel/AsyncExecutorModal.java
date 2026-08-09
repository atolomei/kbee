package kbee.web.command.panel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.kbee.content.command.AsyncCommand;
import com.novamens.wicket.markup.html.modal.Modal;

@SuppressWarnings("serial")
public class AsyncExecutorModal extends Modal {
	private static final long serialVersionUID = 1L;
	
	public class ExecuteButton extends Button {
		public ExecuteButton() {
			super("recyclebin.empty.execute", "btn btn-lg btn-danger");
		}
		@Override
		public boolean isVisible() {
 			return !isLaunched(); 
		}
		@Override
		public boolean closeOnClick() {
 			return false; 
		}
	}
	
	public class CancelButton extends Button {
		public CancelButton() {
			super("recyclebin.empty.cancel", "btn btn-lg btn-default");
		}
		public boolean isVisible() {
 			return isLaunched() && !isTerminated(); 
		}
		@Override
		public boolean closeOnClick() {
 			return false; 
		}
	}
	
	public class OkButton extends Button {
		public OkButton() {
			super("recyclebin.empty.ok", "btn btn-lg btn-primary");
		}
		public boolean isVisible() {
 			return isTerminated(); 
		}
	}
	
	public class CloseButton extends Button {
		public CloseButton() {
			super("recyclebin.empty.close", "btn btn-lg btn-default");
		}
		public boolean isVisible() {
 			return !isLaunched(); 
		}
	}
	
	public AsyncExecutorModal(String id) {
		super(id);
		
		setOutputMarkupId(true);
		
		setButtons(new CloseButton(), new CancelButton(), new ExecuteButton(), new OkButton());
	}
	
	public void open(AjaxRequestTarget target) {
		
		setBody(new AsyncExecutorPanel("body") {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				target.add(AsyncExecutorModal.this.get("modal-dialog"));
			}
			@Override
			protected AsyncCommand newCommand() {
				return AsyncExecutorModal.this.getCommand();	
			}
			@Override
			protected IModel<String> getConfirmationMessage() {
				return AsyncExecutorModal.this.getConfirmationMessage();
			}
			@Override
			protected IModel<String> getExecutionMessage() {
				return AsyncExecutorModal.this.getExecutionMessage();
			}
		});
		
		super.open(target, new Modal.Handler() {
			@Override
			public void onClick(AjaxRequestTarget target, Button button) {
				if (button.key().equals("recyclebin.empty.execute")) {
					((AsyncExecutorPanel)getBody()).execute(target);
				}
				if (button.key().equals("recyclebin.empty.cancel")) {
					((AsyncExecutorPanel)getBody()).cancel(target);
				}

			}
		});	
	}
	
	public boolean isLaunched() {
		return getBody() instanceof AsyncExecutorPanel && ((AsyncExecutorPanel)getBody()).isLaunched();
	}
	
	public boolean isTerminated() {
		return getBody() instanceof AsyncExecutorPanel && ((AsyncExecutorPanel)getBody()).isTerminated();
	}
	
	protected AsyncCommand getCommand() {
		return null;
	}
	
	protected IModel<String> getConfirmationMessage() {
		return new Model<String>("confirm");
	}
	
	protected IModel<String> getExecutionMessage() {
		return new Model<String>("executing...");
	}
}
