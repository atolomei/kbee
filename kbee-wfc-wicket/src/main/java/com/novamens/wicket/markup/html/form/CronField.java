package com.novamens.wicket.markup.html.form;

import com.novamens.scheduler.CronExpressionJ8;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import java.util.MissingResourceException;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

@SuppressWarnings("serial")
public class CronField extends TextField<CronExpressionJ8> {
	private static final long serialVersionUID = 1L;
			
	//private static Logger logger = Logger.getLogger(CronField.class.getName());
	

	protected static final ResourceReference CSS = new CssResourceReference(CronField.class, "cron/jqCron.css");
	protected static final ResourceReference JS = new CssResourceReference(CronField.class, "cron/jqCron.js");
	protected static final ResourceReference JSLang = new CssResourceReference(CronField.class, "cron/jqCron.en.js");

	
	
	protected IModel<String> getText() {
 		IModel<String> model = new StringResourceModel(getProperty()+".text", CronField.this, null);
		try {
			if (model!=null && model.getObject()!=null)
				return model;
			return null;
		}
		catch (MissingResourceException e) {
			return null;
		}
	}

	
	//"margin-top: -44px;  margin-left: 15px;"
	public String getStyleStr() {
		return "";
	}
	/**
	 *  
	 */
	public class DateControlFragment extends Fragment {

		public DateControlFragment(String id) {
			super(id, "control-fragment", CronField.this);

			WebMarkupContainer cc= new WebMarkupContainer("control-container");
			add(cc);
			
			cc.add(new AttributeModifier("class", isBorder() ? "control-container control-container-border" : "control-container control-container-plain"));
			cc.add(new AttributeModifier("style", getStyleStr() ));
			
			
			
			
			final org.apache.wicket.markup.html.form.TextField<?> input  = newTextField();
			input.setOutputMarkupId(true);

			cc.add(input);
			cc.add(getFeedback());

			IModel<String> help = getHelpText();

			if (help!=null && help.getObject()!=null)
				cc.add((new Label ("help", help)).setEscapeModelStrings(false));
			else
				cc.add((new Label ("help", "")).setVisible(false));
			
			IModel<String> help2 = getText();
			if (help2!=null && help2.getObject()!=null)
				add( (new Label ("help2", help2)).setEscapeModelStrings(false));
			else
				add((new Label ("help2", "")).setVisible(false));
		}
	}

	/**
	 *  
	 */

	public CronField(String id) {
		this(id, null, false, Width.W12);
	}

	public CronField(String id, Width width) {
		this(id, null, false, width);
	}

	public CronField(String id, boolean required) {
		this(id, null, required, Width.W12);
	}

	public CronField(String id, IModel<CronExpressionJ8> model) {
		this(id, model, false, Width.W12);
	}

	public CronField(String id, IModel<CronExpressionJ8> model, boolean required) {
		this(id, model, required, Width.W12);
	}

	public CronField(String id, IModel<CronExpressionJ8> model, boolean required, Width width) {
		super(id, model, required, width, null);
	}
	
	@Override
	public Component getInput() {
		if (getDisposition()==null || getDisposition()==Disposition.HORIZONTAL) {
			return get("horizontal-layout:control:control-container:input");
		}
		else {
			return get("control:control-container:input");
		}
	}

	
	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(CSS));
		response.render(JavaScriptHeaderItem.forReference(JS));
		response.render(JavaScriptHeaderItem.forReference(JSLang));

		response.render(OnDomReadyHeaderItem.forScript(
				"	$('#"+ getInput().getMarkupId()+"').jqCron({" +
								"lang: 'en'," +
								"enabled_minute: false," +
								"enabled_hour: false," +
								"enabled_day: true," +
								"enabled_week: true," +
								"enabled_month: true," +
								"enabled_year: true," +
								"multiple_dom: false," +
								"multiple_month: false," +
								"multiple_mins: false," +
								"multiple_dow: false," +
								"multiple_time_hours: false," +
								"multiple_time_minutes: false," +
								"numeric_zero_pad: false," +
								"default_period: 'day'," +
								"default_value: '0 3 1 * *'," +
								"no_reset_button: true," +
								"disabled: " + (this.getEditor().isEditionEnabled() ? "false":"true") +
					"});" ));
	}

	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
	}
	

	/**
	 * Date in the machine TZ
	 * @return
	 */


	@Override
	protected Fragment newControlFragment() {
		return new DateControlFragment("control");
	}
/*
	@Override
	public CronExpressionJ8 getValue() {
		return super.getModel();
		/*final String value = super.getModel();
		String fromQuartzToUnixString=null;
		if(value != null) {
			CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);

			final Cron parse = new CronParser(cronDefinition).parse(value);
			fromQuartzToUnixString = CronMapper.fromUnixToQuartz().map(parse).asString();
		}
		return fromQuartzToUnixString;
	}

	@Override
	public void setValue(CronExpressionJ8 value) {
		CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
		final Cron parse = new CronParser(cronDefinition).parse(value);
		String fromQuartzToUnixString = CronMapper.fromQuartzToUnix().map(parse).asString();
		super.setValue(fromQuartzToUnixString);

	}*/
	
	private boolean isborder;

	public void setBorder(boolean b) {
		this.isborder=b;
	}

	
	@Override
	public void setFieldValue(String value) {
		super.setFieldValue(value);
	}
	
	public boolean isBorder() {
		return this.isborder;
	}

	
}
 