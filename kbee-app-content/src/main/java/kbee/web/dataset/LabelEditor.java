package kbee.web.dataset;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.novamens.content.user.UserLabel;
import com.novamens.content.user.UserLabelsService;
import com.novamens.dom.Domain;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.Buttons;

@Deprecated
@SuppressWarnings("serial")
public class LabelEditor extends ObjectEditor<UserLabel> {
	private static final long serialVersionUID = 1L;

	static Logger logger = LogManager.getLogger(LabelEditor.class.getName());
	
	private String title_panel;
	private boolean is_new_label = false;

	private List<String> css_list;
	
	 

	public LabelEditor(IModel<UserLabel> model) {
		this("editor", model);
	}

	 

	public LabelEditor(String id, IModel<UserLabel> model) {
		super(id, model);
		setOutputMarkupId(true);
	}

	 

	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
 
		if (get("title-panel")==null) {
			Label title_panel = new Label("title-panel", new Model<String>() {
				private static final long serialVersionUID = 1L;
				public String getObject() {
					return getTitlePanel();
				}
			}) {
				private static final long serialVersionUID = 1L;
				public boolean isVisible() {
					return getTitlePanel() !=null;
				}
			};
			add(title_panel);
		}
		
		
		if (get("form")==null) {
			
			Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
			add(form);
			
			if (!isNewLabel())
				form.add(new AttributeModifier("style", "margin-left: 20px;"));
			
			form.add(new TextField<String>("label"));
			form.add(new ChoiceField<String>("css", new PropertyModel<List<String>>(this, "cssOptions")));
			form.add(new Buttons<UserLabel>(this) {
				@Override
				protected String getCss() {
					return isNewLabel() ? "  btn-default btn btn-sm" : "  btn-default btn btn-sm";
				}
				@Override
				protected String getSubmitCss() {
					return isNewLabel() ? "  btn-primary btn btn-sm" : "  btn-primary btn btn-sm";
				}
			});	
		}
	}

	/** ------------------------------------------------------------------------------
	 */ 

	public String getTitlePanel() {
		return title_panel;
	}

	/** ------------------------------------------------------------------------------
	 */ 

	public void  setTitlePanel(String tp) {
		title_panel = tp;
	}

	/** ------------------------------------------------------------------------------
	 */ 

	@Override
	public void cancel(AjaxRequestTarget target) {
		super.cancel(target);
		 onCancel(target); 
	}
	
	/** ------------------------------------------------------------------------------
	 */ 

	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				UserLabelsService labelsService = getRoot().getService(UserLabelsService.class);
				labelsService.update(getModelObject());
				reset();
				onUpdate(target);
			}
			else {
				reset();
				onUpdate(target);
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<>(target, e));

		}
	}
	
	 

	public void onUpdate(AjaxRequestTarget target) {
	}

	 

	public void onCancel(AjaxRequestTarget target) {
	}
	
	 

	public void setIsNewLabel(boolean b) {
		this.is_new_label=b;
	}

	 

	public List<String> getCssOptions() {
		if (css_list==null) {
			css_list = new ArrayList<String>();
			for (String str: UserLabel.CSS) 
				css_list.add(str);
		}
		return this.css_list;
	}

	 

	protected boolean isNewLabel() {
		return this.is_new_label;
	}
	
	 
	private KbeeUser getRoot() {
		Domain domain = ((KbeeUser) getSessionUser()).getDomain();
		KbeeUser root = (KbeeUser)ServiceLocator.getService(SecurityService.class).findUserByUsername("root@"+domain.getName());
		return root;
	}

	
	private User getSessionUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

}
