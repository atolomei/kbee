package kbee.web.model.contentclass;


import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.springframework.dao.DataIntegrityViolationException;

import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.ExtractionRule;
import com.novamens.content.service.DOMObjectService;
import com.novamens.content.service.DomainService;
import com.novamens.kbee.content.model.KbeeCodeExecutor;
import com.novamens.kbee.content.model.KbeeContentTemplate;
import com.novamens.kbee.content.model.KbeeExtractionMacro;
import com.novamens.kbee.content.model.KbeeExtractionScript;
import com.novamens.kbee.wicket.markup.html.console.panel.ViewMode;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextAreaField;

import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.modal.InfoDialog;


import kbee.web.editor.DomainObjectEditor;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;

@SuppressWarnings("serial")
public class ContentClassDisplayEditor extends DomainObjectEditor<ContentTemplate> {
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ContentClassDisplayEditor.class.getName());


	final boolean role_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	
	private final static String MACRO_TYPE = "Macro";
	private final static String SCRIPT_TYPE = "Script";
	
	private String titleRuleType = MACRO_TYPE, titleMacro, titleScript;
	

	
	
	
	
	
	/**
	 * 
	 * 
	
	<#if content.resource??>

${content.resource.title}

<#else>

<#if content.documenttype??>${content.documenttype}<#else>${content.contenttemplate.name}</#if> 
<#if content.fechareferencia??> - ${content.fechareferencia ?string["dd/MM/yy"]} </#if> - ${content.oid}



</#if>


	 * 
	 * 
	 * @param id
	 * @param model
	 */
	
	
	
	
	
	
	public ContentClassDisplayEditor(String id, IModel<ContentTemplate> model) {
		super(id, model);
		
		
		setOutputMarkupId(true);
		setEditionEnabled(false);
		setRule(model.getObject().getTitleRule());
		
		add(new InfoDialog("help-modal"));

		
	}
	
	
	Panel test_macros_panel; 
	Form<?> form ;
		
	/**
	 * 
	 */
	public void onInitialize() {
		super.onInitialize();
		
		
		form = new Form<Void>("form", Disposition.VERTICAL);
		
		form.setOutputMarkupId(true);

		test_macros_panel = new InvisiblePanel("test-macro-panel");
		form.add(test_macros_panel);
		
		AjaxLink<Void> testl = new AjaxLink<Void>("test-macro") {

			@Override
			public void onClick(AjaxRequestTarget target) {
			}
		};
		 testl.setVisible(false);
		
		
		form.add(testl);
		
		form.add(new BooleanField("isTitleEditable"));
		
		ChoiceField<ViewMode> vm = new ChoiceField<ViewMode>("defaultViewMode", new PropertyModel<ViewMode>(this, "defaultViewMode"), new PropertyModel<List<ViewMode>>(this, "defaultViewModes")) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				getModelObject().setDefaultViewMode(getValue().getId());
				target.add(form);
			}
		}; 
		form.add(vm);
	 
		
		form.add(new ChoiceField<String>("titleRuleType", 
				new PropertyModel<String>(this, "titleRuleType"), 
				new PropertyModel<List<String>>(this, "ruleTypes")) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				setTitleRuleType(getValue());
				target.add(form);
			}
		});
		
		
		form.add((new TextAreaField<String>("titleMacro", new PropertyModel<String>(this, "titleMacro")) {
			@Override
			public boolean isVisible() {
				return MACRO_TYPE.equals(getTitleRuleType());
			}
			@Override
			public void onHelp(AjaxRequestTarget target) {
				getHelpModal().open(target, getTitleRuleHelp(), () -> { return new StringResourceModel("titleRule.onhelp" , ContentClassDisplayEditor.this, null).getObject(); });
			}
			@Override
			public boolean isHelpInfo() {
				return true;
			}
		}));
		
		form.add((new TextAreaField<String>("titleScript", new PropertyModel<String>(this, "titleScript"), 8, 0) {
			@Override
			public boolean isVisible() {
				return SCRIPT_TYPE.equals(getTitleRuleType());
			}
			@Override
			public void onHelp(AjaxRequestTarget target) {
				IModel<String> text = new Model<String>(KbeeCodeExecutor.GetHelpText(ContentClassDisplayEditor.this.getModelObject()));
				getHelpModal().open(target, () -> { return "How to write a Script"; }, text);
			}
			@Override
			public boolean isHelpInfo(){
				return true;
			}
		}));
				
		
		AjaxLink<Void> td= new AjaxLink<Void>("title-apply-default") {
			@SuppressWarnings("unchecked")
			@Override
			public void onClick(AjaxRequestTarget target) {
				String title_default= ContentClassDisplayEditor.this.getModelObject().getDomain().getService(DomainService.class).getDefaultTitleRule(ContentClassDisplayEditor.this.getModelObject());
				((TextAreaField<String>) form.get("titleMacro")).setValue(title_default);
				target.add(ContentClassDisplayEditor.this);
			}
			
			public boolean isVisible() {
				return !SCRIPT_TYPE.equals(getTitleRuleType());
			}
			
			@Override
			public boolean isEnabled() {
				return isEditionEnabled();
			}
			
			
		};
		form.add(td);
		

		AjaxLink<Void> sd= new AjaxLink<Void>("sub-apply-default") {
			@SuppressWarnings("unchecked")
			@Override
			public void onClick(AjaxRequestTarget target) {
				String sub_title_default= ContentClassDisplayEditor.this.getModelObject().getDomain().getService(DomainService.class).getDefaultSubTitleRule(ContentClassDisplayEditor.this.getModelObject());
				((TextAreaField<String>) form.get("consoleSubtitleRule")).setValue(sub_title_default);
				target.add(ContentClassDisplayEditor.this);
			}
			
			@Override
			public boolean isEnabled() {
				return isEditionEnabled();
			}
			
			
		};
		form.add(sd);

		
		form.add(new  TextAreaField<String>("consoleSubtitleRule", 8, 0) {
			@Override
			public void onHelp(AjaxRequestTarget target) {
				getHelpModal().open(target, getTitleRuleHelp(), () -> { return new StringResourceModel("titleRule.onhelp" , ContentClassDisplayEditor.this, null).getObject(); });
			}
			@Override
			public boolean isHelpInfo() {
				return true;
			}
		});
		
		

		form.add(new  TextAreaField<String>("portalsSubtitleRule",8,0) {
			@Override
			public void onHelp(AjaxRequestTarget target) {
				getHelpModal().open(target, getTitleRuleHelp(), () -> { return new StringResourceModel("titleRule.onhelp" , ContentClassDisplayEditor.this, null).getObject(); });
			}
			@Override
			public boolean isHelpInfo() {
				return true;
			}
		});

		
		AjaxLink<Void> spd= new AjaxLink<Void>("sub-portal-apply-default") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				String sub_title_default= ContentClassDisplayEditor.this.getModelObject().getDomain().getService(DomainService.class).getDefaultSubTitleRule(ContentClassDisplayEditor.this.getModelObject());
				((TextAreaField<String>) form.get("portalsSubtitleRule")).setValue(sub_title_default);
				target.add(ContentClassDisplayEditor.this);
			}
			
			@Override
			public boolean isEnabled() {
				return isEditionEnabled();
			}
			
			
		};
		form.add(spd);

		
		
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
				((KbeeContentTemplate)getModelObject()).setTitleRule(getRule());
				getModelObject().getService(DOMObjectService.class).update(getUpdatedParts());
				super.reset();
				ContentClassDisplayEditor.this.onUpdate(target);
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<>(target, e));
		}
	}
	
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
	
	public String getTitleRuleType() {
		return titleRuleType;
	}
	
	public void setTitleRuleType(String type) {
		this.titleRuleType = type;
	}
	
	public String getTitleMacro() {
		return titleMacro;
	}
	
	public void setTitleMacro(String type) {
		this.titleMacro = type;
	}
	
	public String getTitleScript() {
		return titleScript;
	}
	
	public void setTitleScript(String type) {
		this.titleScript = type;
	}
	
	public void setRule(ExtractionRule rule)  {
		if (rule instanceof KbeeExtractionScript) {
			setTitleRuleType(SCRIPT_TYPE);
			setTitleScript(((KbeeExtractionScript)rule).getScript());
		}
		if (rule instanceof KbeeExtractionMacro) {
			setTitleRuleType(MACRO_TYPE);
			setTitleMacro(((KbeeExtractionMacro)rule).getMacro());
		}
	}
	
	public ExtractionRule getRule()  {
		if (MACRO_TYPE.equals(getTitleRuleType())) {
			KbeeExtractionMacro rule = new KbeeExtractionMacro();
			rule.setMarco(getTitleMacro());
			return rule;
		}
		if (SCRIPT_TYPE.equals(getTitleRuleType())) {
			KbeeExtractionScript rule = new KbeeExtractionScript();
			rule.setScript(getTitleScript());
			return rule;
		}
		return null;
	}
	
	public List<String> getRuleTypes() {
		List<String> types = new ArrayList<String>();
		types.add(MACRO_TYPE);
		types.add(SCRIPT_TYPE);
		return types;
	}
	
	protected void onClose(AjaxRequestTarget target) {
	}

	protected void onCancel(AjaxRequestTarget target) {
		setEditionEnabled(false);
		target.add(this);							
	}
	
	public void onUpdate(AjaxRequestTarget target) {
	}
	
	private IModel<String> getTitleRuleHelp() {
		return new Model<String>("Rule");
	}
	
	private InfoDialog getHelpModal() {
		return (InfoDialog) get("help-modal");
	}
	
	public void setDefaultViewMode( ViewMode mode) {
		getModelObject().setDefaultViewMode(mode.getId());
	}
	
	public ViewMode getDefaultViewMode() {
		return ViewMode.of( getModelObject().getDefaultViewMode() );
	}
	
	public List<ViewMode> getDefaultViewModes() {
		List<ViewMode> list = new ArrayList<ViewMode>();
		list.add(ViewMode.ICON);
		list.add(ViewMode.THUMBNAIL);
		list.add(ViewMode.THUMBNAIL_LARGE);
		list.add(ViewMode.THUMBNAIL_JUMBO);
		return list;
	}
}
