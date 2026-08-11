package kbee.web.model.eform;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.novamens.content.form.EForm;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.service.DomService;
import com.novamens.kbee.content.form.KbeeEForm;
import com.novamens.kbee.template.KbeeTemplateModelInfo;
import com.novamens.service.ApplicationServerService;
import com.novamens.service.ServiceLocator;
import com.novamens.text.TemplateModelInfo;
import com.novamens.text.TemplateModelInfo.ModelType;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.form.TextAreaField;

import kbee.util.FSUtils;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;
import kbee.web.template.ModelHelpModal;

public class EFormViewerEditor extends ObjectEditor<EForm> {
	private static final long serialVersionUID = 1L;
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(EFormViewerEditor.class.getName());

	private IModel<ContentTemplate> templatemodel;
	private String fileName;
	Form<?> form;
	
	
	@SuppressWarnings("serial")
	public EFormViewerEditor(String id, IModel<ContentTemplate> templatemodel, IModel<EForm> model) {
		super(id, model);
		
		setTemplate(templatemodel);
		setEditionEnabled(false);
		form = new Form<Void>("form", Disposition.VERTICAL);

// 		form.add(new FroalaField("viewer") {
//			@Override
//			public boolean isHelpInfo() {
//				return true;
//			}
//			@Override
//			public void onHelp(AjaxRequestTarget target) {
//				if (getHelpModel()!=null)
//				getHelpModal().open(target, getHelpModel());
//			}
// 		});
//
											
		AjaxLink<Void> apply = new AjaxLink<Void>("apply") {
			@SuppressWarnings("unchecked")
			@Override
			public void onClick(AjaxRequestTarget target) {
				String text=getFileText();
				
				logger.debug(text);
				
				((KbeeEForm) EFormViewerEditor.this.getModel().getObject()).setViewer(text);
				((TextAreaField<String>) form.get("viewer")).setValue(text);
				target.add(EFormViewerEditor.this);
			}
		};
		
		form.add(apply);
		
		
		form.add(new ChoiceField<File>("template", new PropertyModel<File>(this, "templateFile"),	
				new PropertyModel<List<File>>(this, "templateFiles"), false) {
		
			@Override
			protected String getDisplayValue(File value) {
				return value.getName();
			}
			
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				
				setTemplateFile( getValue() );
				//setUpdatedPart("Menu Icon Set: " + getValue());
			}
			
			@Override
			public boolean isEnabled() {
				return true;
			}
			
			@Override
			public boolean isVisible() {
				return true;
			}
		});
		

		
		
		form.add(new TextAreaField<String>("viewer", 40, 40) {
				@Override
				public boolean isHelpInfo() {
					return true;
				}
				@Override
				public void onHelp(AjaxRequestTarget target) {
					if (getHelpModel()!=null)
					getHelpModal().open(target, getHelpModel());
				}
		});
		
 		
		add(form);
		
		add(new EditButtonsV5<EForm>(this)  {
			@Override
			public boolean isEnabled() {
				return true;
			}
		});
		
		add(new ModelHelpModal("help-modal"));
	}	
	
	public void setTemplateFile( File file) {
		this.fileName=file.getAbsolutePath();
	}
	
	public File getTemplateFile() {
		if (fileName==null)
			return null;
		return new File(fileName); 
	}
	
	protected String getFileText() {
		
		if (getTemplateFile()==null)
			return "";
		
		String text = readHTMLFile(getTemplateFile());
		
		return text;
	}

	public void setTemplate(IModel<ContentTemplate> model) {
		this.templatemodel = model;
	}
	
	public ContentTemplate getContentTemplate() {
		return templatemodel.getObject();
	}
	
	@Override
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				KbeeEForm eform = (KbeeEForm)getModelObject();
				eform.getService(DomService.class).update(getUpdatedParts());
				reset();
			}
		}
		catch (Exception e) {
			fire(new ErrorEvent<>(target, e));
		}
	}
	
	protected TemplateModelInfo getHelpModel() {
		KbeeTemplateModelInfo model = new KbeeTemplateModelInfo();
		model.setName("Model");
		model.setType(ModelType.COMPOUND);
//		model.setDescription("Modelo del template para la generación de la notificación de un evento de inicio de tarea");
		List<TemplateModelInfo> elements = new ArrayList<TemplateModelInfo>();
		KbeeTemplateModelInfo e;
		
		e = new KbeeTemplateModelInfo();
		e.setName("content");
		e.setTemplate(getContentTemplate().getName());
		e.setType(KbeeTemplateModelInfo.ModelType.CONTENT);
		elements.add(e);
		
		e = new KbeeTemplateModelInfo();
		e.setName("signeddata");
		e.setType(KbeeTemplateModelInfo.ModelType.SIGNED);
		elements.add(e);

		
		model.setElements(elements);
		
		return model;
	}
	
	protected ModelHelpModal getHelpModal() {
		return (ModelHelpModal) get("help-modal");
	}
	

	
	public List<File> getTemplateFiles() {
		return getCandidateFiles();
	}
	
	
	private List<File> getCandidateFiles() {
		List<File> files = new ArrayList<File>();
		File base = new File(ServiceLocator.getService(ApplicationServerService.class).getFormTemplatesDir());
		if (!base.exists() || !base.isDirectory())
			return files;
		getSubDirFiles(base).forEach(item -> files.add(item));
		return files;
	}
	
	private List<File> getSubDirFiles(File dir) {
		
		  List<File> files = new ArrayList<File>();
		  
		  if (!dir.exists() || !dir.isDirectory())
			  return files;
		  
		  File arrfiles [] = dir.listFiles();
		  
		  for (File file: arrfiles) {
			  
			  if (file.isFile()) {
					if (FSUtils.isHTML(file.getName())) {
						logger.debug(file.getName());
						files.add(file);
					}
			  }
			  else {
				  if (file.isDirectory()) {
					  List<File> ls = getSubDirFiles(file);
					  ls.forEach(item -> files.add(item));
				  }
			  }
		  }
		  return files;
	   }
	
	private String readHTMLFile(File file) {
		try {
			return  Files.readString(Path.of(file.getAbsolutePath()));
		} catch (IOException e) {
			logger.error(e);
			return e.getClass().getName() + " | " + e.getMessage(); 
		}
		
		
	}

}
