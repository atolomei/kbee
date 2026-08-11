package com.novamens.content.web.admin.markup.datamanagement;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.codesnippets4all.json.parsers.JSONParser;
import com.codesnippets4all.json.parsers.JsonParserFactory;
import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.command.CleanIndexCommand;
import com.novamens.kbee.content.command.ReindexCommand;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.form.NumberField;

public class ReindexPanel extends Panel {
			
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ReindexPanel.class.getName());

	
	private AdminForm form;
	

	private Integer threads  = Integer.valueOf(18);
	
	private IModel<Integer> m_threads  =  new Model<Integer>(threads);
	
	public IModel<Integer> getThreads() {
		return m_threads;
	}
	
	
	public void setThreads(IModel<Integer> m) {
		m_threads=m;
	}
	
	
	
	public ReindexPanel(String id) {
		super(id);
		
		setOutputMarkupId(true);
		form = new AdminForm("form");
		add(form);
		
		Label history = new Label("history", new Model<String>() {
			private static final long serialVersionUID = 1L;
			@SuppressWarnings("rawtypes")
			@Override
			public String getObject() {
				try {
					String str = getUser().getService(PreferencesService.class).getValue(ReindexPanel.this.getClass().getSimpleName(), "history");
					JsonParserFactory factory=JsonParserFactory.getInstance();			
					JSONParser parser=factory.newJsonParser();
					if (str!=null) {
						Map jsonData=parser.parseJson(str);
						List rootJson= (List) jsonData.get("root");
						Map al= (Map) rootJson.get(0);
						@SuppressWarnings("unchecked")
						List<String> lis = (List<String>) al.get("commands");
						if (lis.isEmpty())
							return "no commands yet";
						StringBuilder xs =new StringBuilder();
						for (String s: lis)
							xs.append(s+"<br/>");
						return xs.toString();
					} else
						return "";
				} catch (Exception e) {
					
					getUser().getService(PreferencesService.class).setValue(ReindexPanel.this.getClass().getSimpleName(), "history", new KbeeJson().toString());
					
					return "error retrieving history";
				}
			}
		});
		
		add(history.setEscapeModelStrings(false));
		}
	
	
	public class AdminForm extends Form<Void> {
		 
		private static final long serialVersionUID = -53671384019988127L;
		private String statement;
		
		public AdminForm(String id) {
			super(id);
			
			
			TextArea<String> statement = new TextArea<String>("statement");
			statement.setModel(new PropertyModel<String>(this,"statement"));
			add(statement);
			
			add(new AjaxButton("reindex-button", this) {

				private static final long serialVersionUID = -8358957179617226851L;

				protected void onSubmit(AjaxRequestTarget target) {
					try {
						if (getStatement()!=null) {
							
							
							
							
							reindex(getStatement());
							
							form.info("OK");
							String str = getUser().getService(PreferencesService.class).getValue(this.getClass().getSimpleName(), "history");

							KbeeJson values;
							if (str==null) {
								values = new KbeeJson();
								List<String> list = new ArrayList<String>();
								list.add(getStatement());
								values.put("commands", list);
							}
							else {
								values = new KbeeJson();
								JsonParserFactory factory=JsonParserFactory.getInstance();
								JSONParser parser=factory.newJsonParser();
								Map jsonData=parser.parseJson(str);
								List rootJson= (List) jsonData.get("root");
								Map al= (Map) rootJson.get(0);
								@SuppressWarnings("unchecked")
								List<String> lis = (List<String>) al.get("commands");
								List<String> newlist = new ArrayList<String>();
								newlist.add(getStatement());
								if (!lis.isEmpty()) {
									int max = (lis.size()>14?14:lis.size());
									for (int n=0; n<max; n++) {
										if (!lis.get(n).trim().equals(getStatement().trim()))
												newlist.add(lis.get(n));
									}
								}
								values.put("commands", newlist);
							}										
							getUser().getService(PreferencesService.class).setValue(ReindexPanel.this.getClass().getSimpleName(), "history", values.toString());
							target.add(ReindexPanel.this);
						}
					}
					catch (Exception e) {
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						PrintStream ps = new PrintStream(baos);
						e.printStackTrace(ps);
						logger.error(e);
						String message =  baos.toString();
						form.error(message);
					}
					target.add(AdminForm.this);
				}
			});
			
			add(new AjaxButton("cleanindex-button", this) {
				/**
				 * 
				 */
				private static final long serialVersionUID = -5138122862239508560L;

				protected void onSubmit(AjaxRequestTarget target) {
					try {
						if (getStatement()!=null) {
							CleanIndexCommand command = new CleanIndexCommand(getStatement(), getDomain().getId().toString());
							
							command.execute();
							//form.info("La consulta de limpieza de índices es de "+command.getCantRes()+" resultados.");
							//form.info("Fueron eliminados "+command.getDeleted()+" índices.");
						}
					}
					catch (Exception e) {
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						PrintStream ps = new PrintStream(baos);
						e.printStackTrace(ps);
						logger.error(e);
						String message = baos.toString();
						form.error(message);
					}
					target.add(AdminForm.this);
				}
			});
			
			add(new FeedbackPanel("feedback"));
		}
		
		public String getStatement() {
			return this.statement;
		}
		
		public void setStatement(String statement) {
			this.statement = statement;
		}
	}

	/** --------------------------------------------------------------------------
	 *
	 * "all"
	 * "security": Users, Groups, Rules
	 * "model": classifiers, datasets, ...
	 * 
	 * @param statement 
	 */
	private void reindex(String statement) {
		
		if (statement.trim().toLowerCase().equals("all")) {
			
			List<String> statements = new ArrayList<String>();
			
			statements.add("from KbeeDataSetMember");
			statements.add("from KbeeClassifier");
			statements.add("from KbeeIDoc");
			statements.add("from KbeeOrganizationalText");
			statements.add("from KbeeUser");
			statements.add("from KbeeGroup");
			statements.add("from ObjectEvent");
			statements.add("from SendEmailEvent");
			
			
			List<Domain> domains = getContentDao().getDomains();
				for (Domain domain: domains) {
					for (String str: statements) {
						 ReindexCommand indexer = new ReindexCommand(str, domain);
						 indexer.setDomainId(Long.valueOf(String.valueOf(domain.getId())));
						 try {
							 indexer.execute();
						 } 
						 catch(Exception e) {
							 logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());	
						 }
					}
				}
		}
		else {
			
			ReindexCommand command = new ReindexCommand(statement);
			
			
			command.setIncludeAttachments(true);
			command.execute();
		}
	}

	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	protected KbeeUser getUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	protected Domain getDomain() {
        return (Domain) ServiceLocator.getService(UserService.class).getDomain();
    }
	
}

