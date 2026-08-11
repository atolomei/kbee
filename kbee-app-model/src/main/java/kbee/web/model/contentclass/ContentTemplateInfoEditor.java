package kbee.web.model.contentclass;

import java.util.ArrayList;
import java.util.List;
 
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.springframework.dao.DataIntegrityViolationException;

import com.novamens.content.base.ContentClass;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.service.DOMObjectService;
import com.novamens.dom.DomainType;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.modal.InfoDialog;

import kbee.web.editor.DomainObjectEditor;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;


@SuppressWarnings("serial")
public class ContentTemplateInfoEditor extends DomainObjectEditor<ContentTemplate> {

	private static final long serialVersionUID = 1L;

	static kbee.util.logging.Logger logger =  kbee.util.logging.Logger.getLogger(ContentTemplateInfoEditor.class.getName());

	private IModel<String> res;
	private IModel<String> tit;

	boolean is_alias_null; 
	
	/**
	 * 
	 * @param id
	 * @param model
	 * 
	 */
	public ContentTemplateInfoEditor(String id, IModel<ContentTemplate> model, boolean isNew) {
		super(id, model);
		
		final boolean role_admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
		
		
		//final boolean free_version = isFreeVersion();
		
		setOutputMarkupId(true);
		setEditionEnabled(false);
		
		setDefaults();

		setEditionEnabled(isNew);
		
		add(new InfoDialog("help-modal"));
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);

		WebMarkupContainer sapi = new WebMarkupContainer("isapi");
		sapi.setVisible(model.getObject().isAPIContentClass());
		form.add(sapi);

		
		WebMarkupContainer ssys = new WebMarkupContainer("issystem");
		ssys.setVisible(model.getObject().isOnlyRootEdit());
		form.add(ssys);
		
		this.is_alias_null = isNew || (model.getObject().getContentClassCode()==null || model.getObject().getContentClassCode().length()==0);

		
		// Structure -------------------------------------------------
		//
		//
		
		form.add(new TextAreaField<String>("description"));
		
		form.add(new TextField<String>("name", true) {
			@Override
			@SuppressWarnings("unchecked")
			public void onUpdate(AjaxRequestTarget target) {
				if (is_alias_null && super.getValue()!=null) {
					String st =parseAlias(super.getValue().trim()).toLowerCase();
					if (st.length()>24) 
						st=st.substring(0, 24)+st.substring(st.length()-3, st.length());
					((ContentTemplate) ContentTemplateInfoEditor.this.getModelObject()).setContentClassCode(st);
					((TextField<String>) ContentTemplateInfoEditor.this.get("form:contentClassCode")).setValue(ContentTemplateInfoEditor.this.getModelObject().getContentClassCode());
					target.add(ContentTemplateInfoEditor.this);
				}
			}
		});

		/**
		 * contentclasscode code was replaced by the alias
		 */
		
		form.add(new TextField<String>("contentClassCode", true) {
			@Override
			public boolean isEnabled() {
				return isRoot();
			}
		});
		
		
		
		ChoiceField<ContentClass> chc=new ChoiceField<ContentClass>("contentClass", new PropertyModel<List<ContentClass>>(this, "classes"));
		chc.setVisible(isRoot());
		form.add(chc);
		
		
		form.add((new TextField<String>("text_label", true)).setVisible(isTextSectionAvailable()));
		
		// form.add(new BooleanField("abstract"));
		// form.add(new BooleanField("PrivateNotes"));
		// form.add(new BooleanField("externalReference"));
		//form.add(new BooleanField("linkResources"));
		//form.add(new BooleanField("treeFileResource"));
		
		form.add(new BooleanField("customAttributes") {
			public boolean isVisible() {
				return false;
			}
		});
		
		form.add(new TextField<String>("customattributes_label", true) {
			public boolean isVisible() {
				return false;
			}
		});

		// ChoiceField<String> res_b=new ChoiceField<String>("res", getRes(), new PropertyModel<List<String>>(this, "list")) {
		//	 public void onUpdate(AjaxRequestTarget target) {
		//	 }
		// };
		//
		//
		//form.add(res_b);
		//form.add(new  TextField<String>("tit", this.tit, true));
		//
		// ------------
		// form.add(new TextField<String>("private_notes_label", true));
		// ------------
		//
		// form.add(new TextField<String>("abstract_label", true));
		

		 

		// Relations -------------------------------------------------------
		//

		form.add(new BooleanField("includesRelationshipsByCriteria") {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				//ContentClassInfoEditor.this.onUpdate(target);
			}
			@Override
			public boolean isHelpInfo() {
				return true;
			}
			@Override
			public void onHelp(AjaxRequestTarget target) {
				getHelpModal().open(target, () -> { return "Title Rule"; }, 
						getTitleRuleHelp());
			}
			
		});
		
		form.add(new BooleanField("acceptsRelationshipsByCriteria") {

			@Override
			public boolean isHelpInfo() {
				return true;
			}
			@Override
			public void onHelp(AjaxRequestTarget target) {
				getHelpModal().open(target, () -> { return "Title Rule"; }, 
						getTitleRuleHelp());
			}

		});
		
		
		form.add(new BooleanField("instanceTimeBasedNotification")  {
			@Override
			public boolean isHelpInfo() {
				return false;
			}
			@Override
			public void onHelp(AjaxRequestTarget target) {
				getHelpModal().open(target, () -> { return "Title Rule"; }, 
						getTitleRuleHelp());
			}

		});
		
		
		// Semantic --------------------------------------------------------
		//
		 
		add(form);
		
		add(new EditButtonsV5<ContentTemplate>(this) {
			@Override
			public boolean isEnabled() {
				if (isRoot())
					return true;
				
				if (getModel().getObject().isOnlyRootEdit())
					return false;

				
				return (role_admin && !isExpressVersion());
			}
		});
	}

	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
 				
				if (getModelObject().getContentClassCode()==null || getModelObject().getContentClassCode().length()<1) {
					if (getModelObject().getName()!=null) {
						String s = getModelObject().getName().toLowerCase().replaceAll("[ |\\t|\\s|(|)]", "");
						if (s.length()>=8)
							getModelObject().setContentClassCode(s.substring(0, 8));
						else
							getModelObject().setContentClassCode(s);
					}
				}
 				
 				if (getModelObject().getContentClassCode().length()>24)
 					getModelObject().setContentClassCode(getModelObject().getContentClassCode().toLowerCase().substring(0, 23));
 				
 				logger.debug("external reference " + (getModelObject().isExternalReference()?"true":"false"));
 				
				boolean b1=res.getObject().equals("List") || res.getObject().equals("Both"); 
				boolean b2=res.getObject().equals("Tree") || res.getObject().equals("Both");
				
				if (b1&&b2) {
					getModel().getObject().setResources(true);
					getModel().getObject().setTreeFile(true);
					getModel().getObject().setResourcesLabel(tit.getObject());
				}
				else if (b1&& !b2) {
					getModel().getObject().setResources(true);
					getModel().getObject().setTreeFile(false);
					getModel().getObject().setResourcesLabel(tit.getObject());
				}
				else if (!b1&& b2) {
					getModel().getObject().setResources(false);
					getModel().getObject().setTreeFile(true);
					getModel().getObject().setTreeFileLabel(tit.getObject());
				}
				else  {
					getModel().getObject().setResources(false);
					getModel().getObject().setTreeFile(false);
				}
	
				if (getModelObject().getName()!=null)
					getModelObject().setName(getModelObject().getName().trim().replace("  ", " "));
				
				getModelObject().getService(DOMObjectService.class).update(getUpdatedParts());
				super.reset();
				
				ContentTemplateInfoEditor.this.onUpdate(target);
				
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<>(target, e));
		}
	}
	
	/**
	 * 
	 */
	@Override
	public void cancel(AjaxRequestTarget target) {
		if (isNew()) {
			try {
				getModelObject().getService(DOMObjectService.class).delete();
			}
			catch (DataIntegrityViolationException e) {
				logger.error(e);
			}
			catch (Exception e) {
				logger.error(e);
			}
			onClose(target);
		}
		else
			onCancel(target);
	}
	
	
	protected void onClose(AjaxRequestTarget target) {
	}

	protected void onCancel(AjaxRequestTarget target) {
		setEditionEnabled(false);
		target.add(this);							
	}
	
	public void onUpdate(AjaxRequestTarget target) {
	}
	
	public List<ContentClass> getClasses() {
		List<ContentClass> list= new ArrayList<ContentClass>();
		for (ContentClass content_class: getContentDao().getClasses()) {
			if (content_class.isSelectable())
					list.add(content_class); 
		}
		return list;
	}
	

	public List<String> getList() {
		List<String> list = new ArrayList<String>();
		list.add("None");
		list.add("List");
		list.add("Tree");
		return list;
	}


	private boolean isTextSectionAvailable() {
		if (isExpressVersion())
			return false;
		return (getModel().getObject().getContentClass().getName()!=null && getModel().getObject().getContentClass().getName().toLowerCase().equals("organizationaltext"));
	}
	
	public void setRes(IModel<String> r) {
		res=r;
	}
	
	public IModel<String> getRes() {
		return res;
	}
	
	public void setTit(IModel<String> r) {
		tit=r;
	}
	
	public IModel<String> getRet() {
		return tit;
	}

	private void setDefaults() {
		
		if (getModel().getObject().getPrivate_notes_label()==null)							
				getModel().getObject().setPrivate_notes_label(new StringResourceModel("private-notes", this, null).getString());
		
		if (getModel().getObject().getText_label()==null)
			getModel().getObject().setText_label(new StringResourceModel("text", this, null).getString());
		
		if (getModel().getObject().getTreeFileLabel()==null)
			getModel().getObject().setText_label(new StringResourceModel("treefile", this, null).getString());
		
		if (getModel().getObject().getResourcesLabel()==null)
			getModel().getObject().setText_label(new StringResourceModel("resources", this, null).getString());

		if (getModel().getObject().getAbstract_label()==null)
			getModel().getObject().setAbstract_label(new StringResourceModel("notes", this, null).getString());
		
		if (getModel().getObject().getCustomattributes_label()==null)
			getModel().getObject().setCustomattributes_label(new StringResourceModel("custom-attribures", this, null).getString());
		
		
		boolean b1=getModel().getObject().isResources();
		boolean b2=getModel().getObject().isTreeFile();
		
		if (b1&&b2) {
			res=new Model<String>("List"); // both
			tit=new Model<String>(getModel().getObject().getResourcesLabel());
		}
		else if (b1&& !b2) {
			res=new Model<String>("List");
			tit=new Model<String>(getModel().getObject().getResourcesLabel());

		}
		else if (!b1&& b2) {
			res=new Model<String>("Tree");
			tit=new Model<String>(getModel().getObject().getTreeFileLabel());

		}
		else  {
			res=new Model<String>("None");
			tit=new Model<String>();
		}
	}
	
	protected InfoDialog getHelpModal() {
		return (InfoDialog) get("help-modal");
	}

	
	
	private IModel<String> getTitleRuleHelp() {
		return new Model<String>("getTitleRuleHelp()");
		/**
		StringBuilder str = new StringBuilder();
		str.append("<div class=\"panel col-lg-12\"><p class=\"text col-lg-12\">Classifier and Attributes</p>");
		str.append("<p class=\"text col-lg-12\"> Example: $attribute:Household Last Name:capital$, $attribute:Household First Name:capital$ $classifier:File Type.Code$ $classifier:Effective date:MM/dd/yy$</p>");
		str.append("<p class=\"text col-lg-12\">By default the the title is taken from the 1st Resource uploaded.</p>");
		str.append("</div>");
		return new Model<String>(str.toString());
		
		
		
 
 
		
		
		
		**/
	}

}
