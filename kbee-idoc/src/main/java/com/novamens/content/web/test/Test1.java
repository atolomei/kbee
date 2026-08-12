package com.novamens.content.web.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import org.bouncycastle.openssl.PEMParser;

import com.googlecode.wicket.jquery.ui.markup.html.link.AjaxLink;
import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.entity.Person;
import com.novamens.content.user.UserService;
import com.novamens.content.workflow.WorkflowDao;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.content.workflow.KbeeWorkflowActivity;
import com.novamens.kbee.template.KbeeEMailTemplateModel;
import com.novamens.kbee.template.KbeeMethod;
import com.novamens.kbee.template.KbeeObjectWrapperTemplateModel;
import com.novamens.kbee.text.KbeeTextTemplate;
import com.novamens.service.ServiceLocator;
import com.novamens.signature.CertificateParser;
import com.novamens.signature.SignatureException;
import com.novamens.signature.SystemSignatureService;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.Activity;
import com.novamens.workflow.ActivityProgressNote;
import com.novamens.workflow.Task;
import com.novamens.workflow.WorkflowContext;

import freemarker.core.Environment;
import freemarker.core.TemplateNumberFormatFactory;
import freemarker.template.Configuration;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateNodeModel;

import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.form.TextAreaField;

import kbee.web.page.ApplicationPage;

@SuppressWarnings("serial")
public class Test1 extends ApplicationPage<Content> {
				
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(Test1.class.getName());
	
	class MyTemplateExceptionHandler implements TemplateExceptionHandler {
	    public void handleTemplateException(TemplateException te, Environment env, java.io.Writer out)
	            throws TemplateException {
	        try {
	            out.write("[ERROR: " + te.getMessage() + "]");
	        } 
	        catch (IOException e) {
	            throw new TemplateException("Failed to print error message. Cause: " + e, env);
	        }
	    }
	}
	
	private IModel<Content> model;
	private IModel<ActivityProgressNote> notemodel;
	private IModel<Activity> activitymodel;
	private String template, text;
	private Configuration cfg;

	
	public Test1(PageParameters parameters) {
		
		try {
		
		Certificate caCertificate =  getRootCACertificate();
		PrivateKey key =getDomain().getPrivateKey();
		Certificate certificate =  getDomain().getCertificate();
		KeyPair keys = ServiceLocator.getService(SystemSignatureService.class).createKeys();
		Certificate c2 = ServiceLocator.getService(SystemSignatureService.class).createCertificate(getDomain(), keys);
		byte pfx[] = CertificateParser.Get().writePfx(caCertificate, certificate, key, "alejo");
		
		
		
		PEMParser parser = new PEMParser(new StringReader(new String(pfx)));
		Object object = parser.readObject();
		// System.out.println(pfx);
        {
      
        	 KeyStore ks = KeyStore.getInstance("pkcs12");
        	 ks.load(new ByteArrayInputStream(pfx), "alejo".toCharArray());
        	 String alias = ks.aliases().nextElement();

        	 PrivateKey pKey = (PrivateKey)ks.getKey(alias, "alejo".toCharArray());
        	 X509Certificate cert = (X509Certificate)ks.getCertificate(alias);
        	 String pem = CertificateParser.Get().write(cert);
        	 // System.out.println(pem);
        }
		
		
		
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		
		setContent(getContent(parameters));
		//setNote(getNote(parameters));
		//setActivity(getActivity(parameters));
		//Content content = ((KbeeWorkflowActivity)getNote().getActivity()).getContent();
		//Content content = ((KbeeWorkflowActivity)getActivity()).getContent();
		//setContent(content);

		WebMarkupContainer panel = new WebMarkupContainer("panel") {
			public boolean isVisible() {
				return getContent()!=null;
			}
		};
		
		panel.add(new Label("title", new Model<String>() {
			public String getObject() {
				return getContent().getTitle();
			}
		}));
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		form.add(new TextAreaField<String>("template", new PropertyModel<String>(this, "template")) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				updateModel();
			}
			@Override
			public IModel<String> getLabel() {
				return new Model<String>("Template");
			}
		});
		
		panel.add(form);
		
		add(panel);
		
		WebMarkupContainer textpanel = new WebMarkupContainer("text-panel");
		textpanel.setOutputMarkupId(true);
		
		panel.add(new AjaxLink<Void>("expand-link") {
			public void onClick(AjaxRequestTarget target) {
				try {
					String templatetext = getTemplate();
					KbeeTextTemplate template = new KbeeTextTemplate(templatetext);
					String text = template.process(getTemplateModel0());
					setText(text);
				}
				catch (Exception e) {
					setText(e.getMessage());
					logger.error(e);
				}
				target.add(textpanel);
			}
		});
		
		
		Label text = new Label("text", () -> getText());
		text.setEscapeModelStrings(false);
		
		textpanel.add(text);
		
		panel.add(textpanel);
		
		WebMarkupContainer modelpanel = new WebMarkupContainer("model");
		
		
		ListView<String> modelview = new ListView<String>("model-view", getModelView()) {
			public void populateItem(ListItem<String> item) {
				String linetext = item.getModelObject();
				int deep = StringUtils.countMatches(linetext, "&nbsp;");
				linetext = linetext.replace("&nbsp;", "");
				Label line = new Label("line", linetext);
				String style = null;
				if (deep>0) {
					style = "padding-left:"+String.valueOf(deep*20)+"px;";
					line.add(new AttributeModifier("style", style));
				}
				line.setEscapeModelStrings(false);
				item.add(line);
			}
		};
		
		modelpanel.add(modelview);
		
		add(modelpanel);
		
		WebMarkupContainer error = new WebMarkupContainer("error") {
			public boolean isVisible() {
				return getContent()==null;
			}
		};
		
		add(error);
	}
	
	public void setContent(Content content) {
		model = content!=null ? new ObjectModel<Content>(content) : null;
	}
	
	public Content getContent() {
		return  model!=null ? model.getObject() : null;
	}
	
	public void setNote(ActivityProgressNote note) {
		notemodel = note!=null ? new ObjectModel<ActivityProgressNote>(note) : null;
	}
	
	public ActivityProgressNote getNote() {
		return  notemodel!=null ? notemodel.getObject() : null;
	}
	
	public void setActivity(Activity activity) {
		activitymodel = activity!=null ? new ObjectModel<Activity>(activity) : null;
	}
	
	public Activity getActivity() {
		return  activitymodel!=null ? activitymodel.getObject() : null;
	}
	
	public void setTemplate(String template) {
		this.template = template;
	}
	
	public String getTemplate() {
		return template;
	}
	
	public void setText(String template) {
		this.text = template;
	}
	
	public String getText() {
		return text;
	}
	
	public TemplateModel getTemplateModel0() {
		KbeeEMailTemplateModel model = new KbeeEMailTemplateModel();
		model.setModel("sender", getSessionUser());
		model.setContent(getContent());
		return model;
	}
	
	public TemplateModel getTemplateModel1() {
		//Person sender = ServiceLocator.getService(UserService.class).getSessionUserProfile().getPerson();
		KbeeEMailTemplateModel model = new KbeeEMailTemplateModel();
		//model.setSender(sender);
		ActivityProgressNote note = getNote();
		if (note==null) throw new KbeeRuntimeException("note not found");
		Content content = ((KbeeWorkflowActivity)note.getActivity()).getContent();
		model.setModel("note", note);
		model.setContent(content);
		KbeeWorkflowActivity activity = (KbeeWorkflowActivity)note.getActivity();
		Task task = content.getService(WorkflowService.class).getContext().getProcedure().getTask(activity.getTaskName());
		activity.setTask(task);
		model.setModel("activity", activity);
		return model;
	}
	
	public TemplateModel getTemplateModel2() {
		Person sender = ServiceLocator.getService(UserService.class).getSessionUserProfile().getPerson();
		KbeeEMailTemplateModel model = new KbeeEMailTemplateModel();
		model.setSender(sender);
		//model.setReceiver(sender);
		KbeeWorkflowActivity activity = (KbeeWorkflowActivity)getActivity();
		if (activity==null) throw new KbeeRuntimeException("activity not found");
		model.setModel("receiver", getSessionUser());
		model.setModel("activity", activity);
		model.setContent(getContent());
		Task task = getContent().getService(WorkflowService.class).getContext().getProcedure().getTask(activity.getTaskName());
		activity.setTask(task);
		KbeeContext context = (KbeeContext)getContent().getService(WorkflowService.class).getContext();
		KbeeWorkflowActivity previousactivity = (KbeeWorkflowActivity) context.getPreviousActivity();
		model.setModel("previousactivity", previousactivity);
		return model;
	}
	
	public TemplateModel getTemplateModel3() {
		KbeeEMailTemplateModel model = new KbeeEMailTemplateModel();
		Content content = getContent();
		KbeeContext context = (KbeeContext)content.getService(WorkflowService.class).getContext(); 
		model.setContent(content);
		Task task = context.getTask();
		model.setModel("task", task);
		KbeeWorkflowActivity previousactivity = (KbeeWorkflowActivity) context.getPreviousActivity();
		model.setModel("previousactivity", previousactivity);
		model.setModel("receiver", getSessionUser());
		return model;
	}
	
	public Configuration getConfiguration() {
		if (cfg==null) {
			cfg = new Configuration(Configuration.VERSION_2_3_29);
			cfg.setDefaultEncoding("UTF-8");
			cfg.setLogTemplateExceptions(false);
			cfg.setWrapUncheckedExceptions(true);
			cfg.setFallbackOnNullLoopVariable(false);
			cfg.setTemplateExceptionHandler(new MyTemplateExceptionHandler());
			Map<String, TemplateNumberFormatFactory> customNumberFormats = new HashMap<String, TemplateNumberFormatFactory>();
			customNumberFormats.put("size", SizeFormatFactory.INSTANCE);
			cfg.setCustomNumberFormats(customNumberFormats);
			cfg.setSharedVariable("link", new ResourceFormatFactory());
		}
		return cfg;
	}
	
	public List<String> getModelView() {
		List<String> model = new ArrayList<String>();
		model = printModel(getTemplateModel0(), model, 0);
		return model;
	}
	
	public List<String> printModel(TemplateModel model, List<String> lines, int deep) {
		try {
			if (model instanceof TemplateNodeModel) {
				TemplateNodeModel node = (TemplateNodeModel)model;
				String tab = "";
				for (int d=0; d<deep; d++) {
					tab += "&nbsp;";
				}
				lines.add(tab+node.getNodeName().toLowerCase());
				TemplateModel childs = node.getChildNodes();
				if (childs instanceof SimpleSequence) {
					SimpleSequence sequence = (SimpleSequence)childs;
					for (int s=0; s<sequence.size(); s++) {
						TemplateModel childmodel = sequence.get(s);
						printModel(childmodel, lines, deep+1);
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return lines;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		cfg = null;
		if (model!=null)
			model.detach();
	}
	
	private Content getContent(PageParameters parameters) {
		Content content = null;
		StringValue id = parameters.get("id");
		if (!id.isNull() && !id.isEmpty()) {
			content = (Content) getContentDao().findContentById(Long.valueOf(id.toString()));
		}	
		return content;
	}
	
	private Activity getActivity(PageParameters parameters) {
		Activity activity = null;
		StringValue id = parameters.get("id");
		if (!id.isNull() && !id.isEmpty()) {
			activity = getWorkflowDao().findActivityById(Long.valueOf(id.toString()));
		}	
		return activity;
	}
	
	private ActivityProgressNote getNote(PageParameters parameters) {
		ActivityProgressNote note = null;
		StringValue id = parameters.get("id");
		if (!id.isNull() && !id.isEmpty()) {
			note = getRepository(ActivityProgressNote.class).findById(Long.valueOf(id.toString()));
		}	
		return note;
	}
	
	private WorkflowDao getWorkflowDao() {
		return (WorkflowDao)ServiceLocator.getService(BeansService.class).getBean("WorkflowDao");
	}
	
	private Certificate getRootCACertificate() throws SignatureException  {
		return getKbeeDomain().getCertificate();
	}
	
	private Domain getKbeeDomain() {
		return getContentDao().findDomainByName("kbee");
	}
}