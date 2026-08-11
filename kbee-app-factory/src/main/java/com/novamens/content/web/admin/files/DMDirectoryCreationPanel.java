package com.novamens.content.web.admin.files;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.web.admin.markup.datamanagement.AbstractDataManagementPanel;
import com.novamens.util.KbeeFileUtils;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.util.BCElement;

public class DMDirectoryCreationPanel extends AbstractDataManagementPanel {

	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DMDirectoryCreationPanel.class.getName());

	String name;
	private String folder_to_upload_path;
	
	public DMDirectoryCreationPanel(String id,  PageParameters parameters) {
			super(id);
			if (parameters!=null) {
				this.folder_to_upload_path=parameters.get("directory").toOptionalString();
				if (this.folder_to_upload_path==null)
					logger.error("parameters.get(\"directory\") is null");
			}
			else
				logger.error("parameters is null");
	}

	
	public DMDirectoryCreationPanel(String id,  String folder_to_upload_path) {
		super(id);
		this.folder_to_upload_path=folder_to_upload_path;
	}
	
	
	
	public String getName() {
		return this.name;
	}
				
	public void setName(String name) {
		this.name = name;
	}
	

	private String getLabel() {
		return "Create";
	}

	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		TextField<String> name = new TextField<String>("name", new PropertyModel<String>(this, "name"), true);
		form.add(name);
		
		AjaxSubmitLink searchbutton = new AjaxSubmitLink("submit") {
			
			private static final long serialVersionUID = 1L;
			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				  onChange(target);
				  target.add(DMDirectoryCreationPanel.this);
				  setName(null);
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
						return null;
					}
					@Override
					public CharSequence getBeforeSendHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getBeforeHandler(Component component) {
						String s = "document.getElementById('"+component.getMarkupId()+"').innerHTML = '<span class=\"" + Form.SPINNING + " fa-fw\"></span> "+getLabel() +"'";
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
		
		form.add(searchbutton);
		
		form.setDefaultButton(searchbutton);
		
		add(form);
	}
	
	
	protected void onChange(AjaxRequestTarget target) {
		
		
		String path= folder_to_upload_path + File.separator + getName();

		File dir = new File(path);
		if ( !( dir.exists() && dir.isDirectory() )) {
			try {
				
				KbeeFileUtils.forceMkdir(dir);
				logger.debug("Created -> " + dir.getAbsolutePath());
				target.add(this);
				
				  
			  } catch (Exception e) {
					logger.error(e);
					throw new KbeeRuntimeException("Can not create Work Directory " + path);
			  }
		}
	}

	/**
	 * 
	 * 
	 */
	protected List<BCElement> getPageBreadCrumb() {
		List<BCElement> li = new ArrayList<BCElement>();
		li.add(new DMFileExplorerBC());
		li.add(new BCElement(new Model<String>("Create Directory")));
		return li;
	}


}
