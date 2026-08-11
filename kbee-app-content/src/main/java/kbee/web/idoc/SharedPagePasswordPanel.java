package kbee.web.idoc;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.novamens.content.base.Content;
import com.novamens.kbee.wicket.markup.html.ajax.WorkingAjaxLink;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.wicket.markup.html.form.PasswordField;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.panel.AlertPanel;
		
public abstract class SharedPagePasswordPanel<T extends Content> extends ModelPanel<T> {

	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SharedPage.class.getName());
	
	private Form<?> form;

		
	private String pwd=null;
	private String error= null;
	
	Label l_error;
	
	
	public void setError(String err) {
		this.error=err;
		l_error = new Label("error", error);
		l_error.setVisible(true);
		form.addOrReplace(l_error);
	}
	
	public String getError()
	{
		return error;
	}
	
	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	
	public SharedPagePasswordPanel(String id, IModel<T> model) {
		super(id, model);
	
	}
	
	public void onInitialize() {
		super.onInitialize();
		
		form = new com.novamens.wicket.markup.html.form.Form<Void>("form", Disposition.VERTICAL);
		add(form);
	
		l_error = new Label("error", "");
		l_error.setVisible(false);
		form.addOrReplace(l_error);
		
		form.add(new PasswordField("password", new PropertyModel<String>(this, "pwd")));
		
		AlertPanel<Void> pa=new AlertPanel<Void>("alert-text",AlertPanel.INFO,  null,  getLabel("pwdtitle"), getLabel("pwdtext"));
		
		pa.setIcon("fa-duotone fa-lock-keyhole");

		form.addOrReplace(pa);
		
		WorkingAjaxLink<Void> sb = new WorkingAjaxLink<Void>("apply") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				l_error.setVisible(false);
				setPwd(  ((PasswordField) form.get("password")).getValue() );
				logger.debug( "pwd -> " + getPwd() );
				SharedPagePasswordPanel.this.onClick(target, getPwd());
			}
		};
		
		form.add(sb);	
		
	}

	abstract protected void onClick(AjaxRequestTarget target, String pwd);
	

}
