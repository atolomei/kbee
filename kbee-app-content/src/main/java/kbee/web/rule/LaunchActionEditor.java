package kbee.web.rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.model.Classificable;
import com.novamens.content.rule.ActionRule;
import com.novamens.content.rule.LaunchAction;
import com.novamens.content.service.UrlService;
import com.novamens.content.workflow.AttributeRule;
import com.novamens.content.workflow.ClassificationRule;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.content.workflow.WorkflowDao;
import com.novamens.content.workflow.WorkflowRule;
import com.novamens.kbee.content.rule.KbeeLaunchAction;
import com.novamens.kbee.content.workflow.KbeeProcedure;
import com.novamens.kbee.content.workflow.MultipleRule;
import com.novamens.service.ServiceLocator;
import com.novamens.transaction.Transaction;
import com.novamens.transaction.TransactionService;
import com.novamens.wicket.markup.html.editor.ObjectEditorPanel;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.StaticField;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.DummyBlockPanel;
import com.novamens.workflow.Procedure;

import kbee.util.logging.Logger;
import kbee.web.model.procedure.AttributesRulesEditor;
import kbee.web.model.procedure.ProcedureEditor;
import kbee.web.model.procedure.ProcedurePage;
import kbee.web.model.procedure.ClassifiersRulesEditor;
import kbee.web.security.user.UserMainPanel;

@SuppressWarnings("serial")
public class LaunchActionEditor extends ObjectEditorPanel<ActionRule> {
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(LaunchActionEditor.class.getName());

	private LaunchAction action;
	private IModel<ProcessLauncher> launchermodel =  null;
	private String note;
	private String feedback;
	
	
	public LaunchActionEditor(LaunchAction action) {
		super("editor");
		this.action = action;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		// add(new StaticField<String>("type", getLabel("type")));
		
		setLauncher(((KbeeLaunchAction)action).getLauncher());
		setNote(((KbeeLaunchAction)action).getNote());
		
		add(new ChoiceField<ProcessLauncher>("launcher", new PropertyModel<ProcessLauncher>(this, "launcher"), () -> getLaunchers()) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				setLauncher(getValue());
				target.add(LaunchActionEditor.this);
			}
		});
		
		add( (new DummyBlockPanel("set-attributes", new Model<String>("Fijar attributos"))).setVisible(false));


		/**
		add(new RulesEditor<Procedure>("classifiersrules") {
			@Override
			public List<ClassificationRule> getRules() {
				return ProcedureEditor.this.getClassifiersRules();
			}
			@Override
			public void setRules(List<ClassificationRule> rules) {
				ProcedureEditor.this.setClassifiersRules(rules);
			}
		});
		
		add(new AttributesRulesEditor<Procedure>("attributesrules") {
			@Override
			public List<AttributeRule> getRules() {
				return ProcedureEditor.this.getAttributesRules();
			}
			@Override
			public void setRules(List<AttributeRule> rules) {
				ProcedureEditor.this.setAttributesRules(rules);
			}
		});
		**/
		
		add(new Link<Void>("launcher-link") {
			@Override
			public void onClick() {
				//setResponsePage(new ProcedurePage(launchermodel));
			}
			@Override
			public boolean isVisible() {
				return getLauncher()!=null;
			}
		});
		
		add(new TextAreaField<String>("note", new PropertyModel<String>(this, "note")));
		
		add(new IndicatingAjaxLink<Void>("test-link") {
			public void onClick(AjaxRequestTarget target) {
				test();
				target.add(LaunchActionEditor.this);
			}
		});
		
		Label feedbacklabel = new Label("feedback", ()->getFeedback()) {
			public boolean isVisible() {
				return getFeedback()!=null;
			}
		};
		feedbacklabel.setEscapeModelStrings(false);
		add(feedbacklabel);	
		
	}
	
	
	
	
	public ProcessLauncher getLauncher() {
		return launchermodel!=null ? launchermodel.getObject() : null;
	}
	
	public void setLauncher(ProcessLauncher launcher) {
		launchermodel = launcher!=null ? new ObjectModel<ProcessLauncher>(launcher) : null;
	}
	
	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public List<ProcessLauncher> getLaunchers() {
		List<ProcessLauncher> launchers = new ArrayList<ProcessLauncher>();
		for (ProcessLauncher launcher : getWorkflowDao().getLaunchers(getModelObject().getDomain())) {
			if (launcher.isApiEnabled()) {
				launchers.add(launcher);
			}
		}
		
		Collections.sort(launchers , new Comparator<ProcessLauncher>() {
			@Override
			public int compare(ProcessLauncher o1, ProcessLauncher o2) {
				try {
				if (o1.getDisplayName()==null)
					return 1;
				if (o2.getDisplayName()==null)
					return -1;
				return o1.getDisplayName().compareToIgnoreCase(o2.getDisplayName());
				} catch (Exception e) {
					logger.error(e);
					return 0;
				}
			}
			
		});
		return launchers;
	}
	
	public void updateModel() {
		try {
			KbeeLaunchAction kbeeaction = (KbeeLaunchAction)action;
			if (getLauncher()!=null) {
				if (kbeeaction.getLauncher()==null || !kbeeaction.getLauncher().getId().equals(getLauncher().getId())) {
					setUpdatedPart("action");
					kbeeaction.setLauncher(getLauncher());
				}
			}			
			if (getNote()!=null && !getNote().equals(kbeeaction.getNote())) {
				setUpdatedPart("action");
				kbeeaction.setNote(getNote());
			}
		} 
		catch (Exception e) {
			logger.error(e);
		}
	}
	
	public String getFeedback() {
		return feedback;
	}

	public void setFeedback(String feedback) {
		this.feedback = feedback;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		if (launchermodel!=null) {
			launchermodel.detach();
		}	
	}
	
	protected Classificable getOwner() {
		return null;
	}
	
	private void test() {
		if (getLauncher()==null) {
			setFeedback(getLabelString("no-launcher.message"));
			return;
		}
		Transaction transaction = null;
		try {
			KbeeLaunchAction kbeeaction = (KbeeLaunchAction)action;
			kbeeaction.setLauncher(getLauncher());
			kbeeaction.setNote(getNote());
			transaction = beginTransaction();
			Content content = (Content) kbeeaction.execute(getOwner());
			transaction.commit();
			String url = content.getService(UrlService.class).getUrl(false);
			String title = content.getTitle();
			setFeedback(getLabelString("test-ok.message", url, title));
		}
		catch (Exception e) {
			logger.error(e);
			setFeedback(e.getMessage());
			if (transaction!=null) {
				transaction.rollback();
			}
		}
	}
	
	private Transaction beginTransaction()  {
		return ServiceLocator.getService(TransactionService.class).beginTransaction(false);
	}
	
	private WorkflowDao getWorkflowDao() {
		return (WorkflowDao) ServiceLocator.getService(BeansService.class).getBean("WorkflowDao");
	}
	
	
}
