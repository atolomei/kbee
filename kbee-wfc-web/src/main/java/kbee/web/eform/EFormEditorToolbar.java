package kbee.web.eform;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;
import com.novamens.content.form.EResourceField;
import com.novamens.content.model.Classificable;
import com.novamens.content.resource.KBFile;
import com.novamens.kbee.content.workflow.KbeeTaskForm;
import com.novamens.kbee.wicket.editor.ClassificableEditor;
import com.novamens.kbee.wicket.editor.Editor;
import com.novamens.kbee.wicket.markup.html.event.GeneralWicketAjaxEvent;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.service.ServiceLocator;
import com.novamens.signature.SignatureException;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.DonwloadMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.form.FormLayout;

import kbee.web.error.ErrorPanel;
import kbee.web.event.wicket.ErrorEvent;


/**
 * Task_Edition_unSigned -> Editor | Preview
 * Task_Edition_signed -> Editor | Preview | Remove Signature 
 *   
 * Task_Sign -> Signed  -> Editor | Remove Signature
 * Task_Sign -> Sign 
 *
 */
@SuppressWarnings("serial")
public class EFormEditorToolbar extends ModelPanel<EFormData> implements EFormPanel {
	
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(EFormEditorToolbar.class.getName());
	
	private AjaxLink<EFormData> viewEditor;
	private AjaxLink<EFormData> viewPreview;
	private AjaxLink<EFormData> removeSignature;
	private Link<EFormData> 	printPage;
	
	private DonwloadMenuItemPanelV5<EFormData> downloadPdf;
	private WebMarkupContainer bar;
	
	private WebMarkupContainer viewEditor_ONOFF;
	private WebMarkupContainer viewPreview_ONOFF;
	private WebMarkupContainer removeSignature_ONOFF;

	private final String class_ON = "fal fa-square-check";
	private final String class_OFF= "fa-light fa-square";
	
	private boolean b_viewEditor=true;
	private boolean b_viewPreview=true;
	private boolean b_signaturePanel=false;
	private boolean has_only_one_panel = false;

	
	protected final boolean root = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
	
	
	public void onDetach() {
		super.onDetach();
	}
	/**
	 * @param id
	 * @param model
	 */
	public EFormEditorToolbar(String id, IModel<EFormData> model, boolean isSignaturePanel) {
		super(id, model);
		b_signaturePanel=isSignaturePanel;
	}

	
	public void setSignaturePanel( boolean isSignaturePanel) {
		this.b_signaturePanel=isSignaturePanel;
		if (this.isInitialized()) {
			removeSignature_ONOFF.add( new AttributeModifier("class", b_signaturePanel ? class_ON : class_OFF));
		}
	}
	
	protected boolean hasEditor() {
		EForm eform= getModel().getObject().getForm();
		if (eform instanceof KbeeTaskForm) {
			FormLayout la=((KbeeTaskForm) eform).getFormLayout();
			
			if (la==null)
				return true;
			
			return la==FormLayout.EDITOR || la==FormLayout.EDITOR_WITH_VIEWER; 
		}
		return false;
	}
	
	
	protected boolean hasViewer() {
		EForm eform= getModel().getObject().getForm();
		if (eform instanceof KbeeTaskForm) {
			FormLayout la=((KbeeTaskForm) eform).getFormLayout();
			
			if (la==null)
				return true;
			
			return la==FormLayout.VIEWER || la==FormLayout.EDITOR_WITH_VIEWER;  
		}
		return false;
	}
	
	/**
	 *  hasEditor()
	 *  hasViewer()
	 * 
	 */
	@Override
	public void onInitialize() {
		super.onInitialize();
		try {
				
			bar = new WebMarkupContainer("bar");
			
			viewEditor_ONOFF = new WebMarkupContainer("onoff");
			viewPreview_ONOFF = new WebMarkupContainer("onoff");
			removeSignature_ONOFF = new WebMarkupContainer("onoff");
			
			viewEditor_ONOFF.add( new AttributeModifier("class",  b_viewEditor ? class_ON : class_OFF));
			viewPreview_ONOFF.add( new AttributeModifier("class", b_viewPreview ? class_ON : class_OFF));
			removeSignature_ONOFF.add( new AttributeModifier("class", b_signaturePanel ? class_ON : class_OFF));
			
			has_only_one_panel = !(hasEditor() && hasViewer()); 
			
			viewEditor = new AjaxLink<EFormData>("viewEditor", getModel()) {
				
				public boolean isVisible() {
					return !has_only_one_panel && hasEditor();
				}
				
				@Override
				public void onClick(AjaxRequestTarget target) {
					b_viewEditor=!b_viewEditor;
					viewEditor_ONOFF.add( new AttributeModifier("class",  b_viewEditor ? class_ON : class_OFF));
					GeneralWicketAjaxEvent event = new GeneralWicketAjaxEvent(target, "editor");
					fire (event);
				}
			}; 
			
			viewEditor.add(viewEditor_ONOFF); 
			bar.add(viewEditor);						
			
			viewPreview = new AjaxLink<EFormData>("viewPreview", getModel()) {
				
				@Override
				public boolean isVisible() {
					return !has_only_one_panel && hasViewer();
				}
				
				@Override				
				public void onClick(AjaxRequestTarget target) {
					b_viewPreview=!b_viewPreview;
					viewPreview_ONOFF.add( new AttributeModifier("class", b_viewPreview ? class_ON : class_OFF));
					GeneralWicketAjaxEvent event = new GeneralWicketAjaxEvent(target, "preview");
					fire (event);
				}
				
			};
			bar.add(viewPreview);
			viewPreview.add(viewPreview_ONOFF);
			
			removeSignature = new AjaxLink<EFormData>("removeSignature", getModel()) {
				@Override
				public boolean isVisible() {
					logger.debug(" is Signed > " + getModel().getObject().getClass().getName()+ " -> " + getModel().getObject().isSigned());
					
					if (! (getModel().getObject().getForm() instanceof KbeeTaskForm))
						return false;
					
					
					if (((KbeeTaskForm) getModel().getObject().getForm()).isSignatureRequired())
						return true;
					
					if (getModel().getObject().isSigned())
						return true;
					
					return false;
					
				}

				
				@Override
				public void onClick(AjaxRequestTarget target) {
					 b_signaturePanel=! b_signaturePanel;
					 removeSignature_ONOFF.add( new AttributeModifier("class", b_signaturePanel ? class_ON : class_OFF));
					GeneralWicketAjaxEvent event = new GeneralWicketAjaxEvent(target, "signaturePanel");
					fire (event);
				}
			};
			removeSignature.add(removeSignature_ONOFF);
			bar.add(removeSignature);

			
			downloadPdf = new  DonwloadMenuItemPanelV5<EFormData>("downloadPdf") {
				@Override 
				public String getLabel() {
					return "";
				}
				
				@Override
				public boolean isEnabled()  {
						return true; 
				}
				@Override
				public boolean isDeleteFileAfterDownload()  {
					return true;
				}
				
				public String getFileName() {
					try {
						if (getFormData().getForm().isFileContainer()) {
							File file = getIncludedFile(getFormData());
							return file!=null?file.getName():null;
						}
						else {
							return getPdf().getFileName();
						}	
					}
					catch (IOException e) {
						logger.error(e);
						return null;
					}
				}
				@Override
				protected File getFile() {
					try {
						if (getFormData().getForm().isFileContainer()) {
							return getIncludedFile(getFormData());
						}
						else {
							return getPdf().getFile();
						}	
					}
					catch (IOException|SignatureException e) {
						logger.error(e);
						return null;
					}
					catch (Exception e1) {
						logger.error(e1);
						return null;
					}
				}
				
				protected EPdfFile getPdf() {
					return new EPdfFile(getFormData());
				}
			};
			
			downloadPdf.setIconCssClass("far fa-download");
			bar.add(downloadPdf);
			
			
			
			printPage = new Link<EFormData>("printPage", getModel()) {
				public boolean isVisible() {
					return true;
				}
				@Override
				public void onClick() {
					setResponsePage( new EFormPrintPage(getModel()));
				}
			}; 
			
			printPage.add(new AttributeModifier("target", "_blank"));
			bar.add(printPage);
			
			add(bar);
			
		} 
		catch (Exception e) {
			logger.error(e);
			bar = new ErrorPanel("bar", e);
			addOrReplace(bar);
		}
		
	}

	@Override
	public void addListeners() {
		super.addListeners();
	}
	
	@SuppressWarnings("unchecked")
	public Classificable getObject() {
		Editor<?> editor = getEditor();
		if (editor instanceof ClassificableEditor<?>) {
			Classificable classificable = ((ClassificableEditor<Classificable>)editor).getModelObject();
			((ClassificableEditor<Classificable>)editor).update(classificable);
			return classificable;
		}
		return null;
	}
	
	public EFormData getFormData() {
		return getModelObject();
	}
	
	public Editor<?> getEditor() {
		MarkupContainer parent = getParent();
		Editor<?> editor = null;
		while (editor==null && parent!=null) {
			if (parent instanceof Editor) {
				editor = (Editor<?>)parent;
			}
			else
				parent = parent.getParent();
		}
		return editor;
	}
	
	
	protected boolean existsFile (EFormData data) throws IOException {
		
		for (EFormField<?> field : data.getForm().getFields()) {
			if (field instanceof EResourceField) {
				Object resourceobject = data.getData(field);
				if (resourceobject !=null && (resourceobject instanceof KBFile)) {
					boolean b =((KBFile)resourceobject).isExistInObjectStorage();
					return b; 
				}
			}
		}
		return false;
		
	}
	
	protected File getIncludedFile (EFormData data) throws IOException {
		try {
		for (EFormField<?> field : data.getForm().getFields()) {
			if (field instanceof EResourceField) {
				Object resourceobject = data.getData(field);
				if (resourceobject !=null && (resourceobject instanceof KBFile)) {
					File file = ((KBFile)resourceobject).getFile();
					return file;
				}
			}
		}
		return null;
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}
	
	protected boolean isConvertToPdfForm() {
		return hasViewer();
	}
	
	protected Panel getMenu() {
		ContextMenuPanel<EFormData> menu = new ContextMenuPanel<EFormData>(getModel());
		menu.setOutputMarkupId(true);
		List<MenuItemFactory<EFormData>> list = getMenuItems();
		list.forEach(item-> menu.addItem(item));
		return menu;
	}
	
	protected List<MenuItemFactory<EFormData>> getMenuItems() {

		List<MenuItemFactory<EFormData>> list = new ArrayList<MenuItemFactory<EFormData>>();
							
		list.add(new MenuItemFactory<EFormData>() {
			@Override
			public int getOrder() {
				return 1;
			}
			@Override
			public AbstractMenuItemPanelV5<EFormData> getItem(String id) {
				return new AjaxMenuItemPanelV5<EFormData>(id, getModel()) {
					public void onClick(AjaxRequestTarget target) {
						try {
						} 
						catch (Exception e) {
							logger.error(e);
							fire(new ErrorEvent<EFormData>(target, getModel(), e));
						}
					}
					@Override 
					public String getLabel() {
						return getLabelString("tools");
					}
					@Override
					public boolean isEnabled() {
						return true;
					}
					
				};
			}
		});

		return list;
	}
}