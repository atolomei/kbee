package com.novamens.content.web.workflow.markup;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.datetime.DateTimeService;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.PrintableBehavior;
import com.novamens.workflow.Activity;
import com.novamens.workflow.WorkflowContext;

import kbee.web.workflow.ResolutionPage;

/**
 * 
 * Letter Templates 
 * 
 * 
 */
@SuppressWarnings("serial")
public class ResolutionPanel extends ModelPanel<WorkflowContext> {
				
	private static final long serialVersionUID = 1L;
	
	@SuppressWarnings("unused")
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ResolutionPanel.class.getName());
	
	public ResolutionPanel(String id, IModel<WorkflowContext> model) {
		super(id, model);
		
		setOutputMarkupId(true);
		
		
		
	}
	
	
	public void onInitialize() {
		super.onInitialize();
		
		Label textlabel = new Label("text", getResolution());
		
		textlabel.setEscapeModelStrings(false);
		
		add(textlabel);
		
		Label metainfolabel = new Label("metainfo", new Model<String>() {
			public String getObject() {
				return ResolutionPanel.this.getMetainfo();
			}
		});
		
		metainfolabel.setEscapeModelStrings(false);
		add(metainfolabel);
		
		Link<Void> ln = new Link<Void>("print-page") {

			@Override
			public void onClick() {
				setResponsePage(new ResolutionPage(getResolution()));
				
			}
			
		};
		
		add((new PrintableBehavior()).new PrintButton("print-button", "response"));
		add(new PrintableBehavior());
		
		add(ln);

		
	}
	/**
	 * 
	 */

	public Activity getPreviuosActivity() {
		return ((KbeeContext)getModelObject()).getPreviousTerminatedActivity();
	}
	
	/**
	 * 
	 */
	public String getResolution() {
		Activity activity = ((KbeeContext)getModelObject()).getPreviousTaskResolution();
		return activity!=null ? activity.getResolution() : null;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getMetainfo() {
		
		Activity previous = getPreviuosActivity();
		if (previous==null) 
			return null;
		
		String user = previous.getUser().getFirstLastName();
		String time = ServiceLocator.getService(DateTimeService.class).timeElapsed(previous.getEndTime());
		
		String label = (getLabel("metainfo", user, time)).getObject();
		return label;
 	}
	
	
	protected IModel<String> getLabel(String key, String... parameter) {
		StringResourceModel model = new StringResourceModel(key, this);
		model.setParameters((Object[])parameter);
		return model;
	}
}
