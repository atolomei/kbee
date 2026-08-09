package com.novamens.wicket.markup.html.modal;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

@SuppressWarnings("serial")
public class ExecutorDialog extends Dialog2 {
	private static final long serialVersionUID = 1L;
	
	private IModel<String> messagemodel = new Model<String>("KK");
	private IModel<String> workingmodel;
	private IModel<String> titlemodel;
	private Command command;
	
	public class ExecutorBehavior extends AbstractDefaultAjaxBehavior {
		@Override
		protected void respond(AjaxRequestTarget target) {
			ExecutorDialog.this.execute(target);
		} 
		@Override
		public void renderHead(final Component component, final IHeaderResponse response) {
			super.renderHead(component, response);
			StringBuilder script = new StringBuilder();
			script.append("function execute"+ExecutorDialog.this.getMarkupId()+"() {\n");
			script.append(getCallbackScript());
			script.append("}\n");
			response.render(JavaScriptHeaderItem.forScript(script.toString(), "execute"+ExecutorDialog.this.getMarkupId()));
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
				String bodyid = ExecutorDialog.this.get("modal-body").getMarkupId();
				s1 = "document.getElementById('"+bodyid+"').innerHTML = '"+getMessage().getObject()+"';";
				s ="setTimeout(function () {"+s1+"}, 1000);";
				return s;
			}
			@Override
			public CharSequence getBeforeSendHandler(Component component) {
				return null;
			}
			@Override
			public CharSequence getBeforeHandler(Component component) {
				String s = null;
				String bodyid = ExecutorDialog.this.get("modal-body").getMarkupId();
				s = "document.getElementById('"+bodyid+"').innerHTML = '<i class=\"far fa-sync fa-spin\" style=\"font-size:14px\"></i> "+getWorkingLabel().getObject()+"';";
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
	} 
	
	public interface Command extends Serializable {
		public void execute(AjaxRequestTarget target);
	};

	
	/** 
	 * @param id
	 */
	public ExecutorDialog(String id) {
		super(id, "confirmation.title", "confirmation.message", Dialog2.Close);
	}

	public void open(AjaxRequestTarget target, IModel<String> titlemodel, IModel<String> workingmodel, IModel<String> messagemodel, Command command) {
		if (titlemodel!=null)
		this.titlemodel = titlemodel;
		this.messagemodel = messagemodel;
		this.workingmodel = workingmodel;
		this.command = command;
		if (!getBehaviors(ExecutorBehavior.class).isEmpty()) {
			Behavior e = getBehaviors(ExecutorBehavior.class).get(0);
			remove(e);
		}
		target.add(this);
		target.appendJavaScript("$('#"+getMarkupId()+"').modal('show')");
		target.appendJavaScript("execute"+getMarkupId()+"();");
	}
	
	@Override
	public IModel<String> getTitle() {
		if (titlemodel!=null)
			return new Model<String>() {
				public String getObject() {
					return titlemodel!=null ? ExecutorDialog.this.titlemodel.getObject() : "";
				}
			};
		else
			return super.getTitle();
	}
	
	@Override
	public IModel<String> getMessage() {
		return new Model<String>() {
			public String getObject() {
				return messagemodel!=null ? ExecutorDialog.this.messagemodel.getObject() : "OO";
			}
		};
	}
	
	public IModel<String> getWorkingLabel() {
		return workingmodel == null ? new Model<String>("Working") : workingmodel;
	}
	
	protected void execute(AjaxRequestTarget target) {
		command.execute(target);
	}
}