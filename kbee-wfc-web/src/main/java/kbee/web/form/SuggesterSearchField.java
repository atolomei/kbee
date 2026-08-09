package kbee.web.form;


import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.IFormModelUpdateListener;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.TextRequestHandler;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.indexer.query.Suggestion;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.wicket.markup.html.form.Form;

@SuppressWarnings("serial")
public class SuggesterSearchField<T> extends Panel implements IFormModelUpdateListener {
				
	private static final long serialVersionUID = 1L;
	

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SuggesterSearchField.class.getName());

	

	protected static final ResourceReference JS = new JavaScriptResourceReference(Form.class, "typeahead.bundle.js");
	protected static final ResourceReference BH = new JavaScriptResourceReference(Form.class, "bloodhound.js");
	protected static final ResourceReference HB = new JavaScriptResourceReference(Form.class, "handlebars.js");
	
	private IModel<T> model;
	private String value;
	private String valuetype;
	private List<Suggestion> suggestions;

	private IModel<String> placeholder= null;
	
	public class SelectBehavior extends AbstractDefaultAjaxBehavior {
		@Override
		protected void respond(AjaxRequestTarget target) {
			
			try {
				Request request = RequestCycle.get().getRequest();
				String select = request.getRequestParameters().getParameterValue("select").toString("");
				KbeeJson json = new KbeeJson(select);
				setStringValue((String)json.get("value"));
//				setStringValue((String)json.get("display"));
				setValueType((String)json.get("type"));
				SuggesterSearchField.this.onInput(target);
			} catch (Exception e) {
				logger.error(e);
			}
			
		}
		@Override
		public void renderHead(final Component component, final IHeaderResponse response) {
			super.renderHead(component, response);
			StringBuilder script = new StringBuilder();
			script.append("function selection() {\n");
			script.append(getCallbackScript());
			script.append("}\n");
			response.render(JavaScriptHeaderItem.forScript(script.toString(), "select"));
		}
		@Override
		protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
			super.updateAjaxAttributes(attributes);
			attributes.getDynamicExtraParameters().add("return {select: top.select};");
		}
	}

	class JsonBehavior extends AbstractDefaultAjaxBehavior {
		@Override
		protected void respond(AjaxRequestTarget target) {
			RequestCycle rc = RequestCycle.get();
			Request request = rc.getRequest();
			String pattern = request.getRequestParameters().getParameterValue("q").toString("");
			rc.replaceAllRequestHandlers(new TextRequestHandler("text/html", "UTF-8", getJson(pattern)));
		}
	}  
	
	public SuggesterSearchField(String id, IModel<T> model) {
		super(id);
		setModel(model);
		add(new JsonBehavior());
		add(new SelectBehavior());
	}
	
	public List<Suggestion> getSuggestions(String pattern) {
		return null;
	}
	
	public void updateModel() {
		
	};
	
	public Component getInput() {
		return get("field:input");
	}
	
	public void setStringValue(String value) {
		this.value = value;
	}
	
	public String getStringValue() {
		return value;
	}
	
	public void setValueType(String value) {
		if (value!=null) {
			value = value.replace("(","");
			value = value.replace(")","");
			this.valuetype = value;
		}
	}
	
	public String getValueType() {
		return valuetype;
	}
	
	public void onSearch(AjaxRequestTarget target, Suggestion suggestion) {
		
	}
	
	public void onBeforeRender() {
		super.onBeforeRender();
		if (get("field")==null) {
			addInput();
		}
	}
	
	@Override
	public void onDetach() {
		suggestions = null;
		if (this.placeholder!=null)
			this.placeholder.detach();
		super.onDetach();
	}
	
	protected String getJson(String pattern) {
		StringBuffer json = new StringBuffer();
		json.append("[");
		int i =0;
		suggestions = getSuggestions(pattern);
		for (Suggestion suggestion : suggestions) {
			if (i>0) json.append(",");
			json.append("{");
			String type = "";
			String value = suggestion.getText();
			if (value.contains(" - ")) {
				int p = value.indexOf(" - ");
				type = value.substring(p);
				type = type.replace(" - ", "");
				type = "("+type+")";
				value = value.substring(0,p);
				json.append(" \"type\": \""+type+"\",");
			}
			value = value!=null ? value.replace("\"", "'") : "";
			json.append(" \"value\": \""+value+"\",");
			json.append(" \"display\": \""+value+" - "+type+"\"");
			json.append("}");
			i++;
		}
		json.append("]");
		return json.toString();
	}
	
	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptReferenceHeaderItem.forReference(getApplication().getJavaScriptLibrarySettings().getJQueryReference()));
		response.render(JavaScriptHeaderItem.forReference(JS));
		response.render(JavaScriptHeaderItem.forReference(BH));
		response.render(JavaScriptHeaderItem.forReference(HB));
		response.render(OnDomReadyHeaderItem.forScript( getScript()));
	}
	
	public void setPlaceHolder(IModel<String> model) {
		this.placeholder=model;
	}
	
	@SuppressWarnings("unchecked")
	protected String getScript() {
		StringBuffer script = new StringBuffer();
		
		MarkupContainer field = (MarkupContainer)get("field");
				
		JsonBehavior jsonbehavior = (JsonBehavior)getBehaviors(JsonBehavior.class).get(0);

		script.append("var source" + field.getMarkupId() + " = new Bloodhound({");
		script.append("datumTokenizer: Bloodhound.tokenizers.obj.whitespace('value'),");
		script.append("queryTokenizer: Bloodhound.tokenizers.whitespace,");
		script.append("remote: {");
		script.append("	url: '"+jsonbehavior.getCallbackUrl()+"&wicket-ajax=true&wicket-ajax-baseurl=/&q=%QUERY',");
		script.append("	limit: 15,");
		script.append("	cache: false,");
		script.append("	wildcard: '%QUERY'");
		script.append("}");
		script.append("});");
		script.append("template = '<div>{{value}}' + ' <span class=\"ago\">{{type}}</span></div>';");
		script.append("top.ds"+field.getMarkupId()+"=source"+field.getMarkupId()+";");
		script.append("$('#"+field.getMarkupId()+" .typeahead').typeahead({");
		script.append("	highlight: true ");
		script.append("			}, {"); 
		script.append("	name: 'source"+field.getMarkupId()+"',");
		script.append("	display: 'display',");
		script.append("	limit: 10,");
		script.append("	cache: false,");
		script.append("	source: source"+field.getMarkupId()+",");
		script.append("	templates: {");
		script.append("suggestion: Handlebars.compile(template)");
		script.append("	}");
		script.append("	});");
		script.append("$('#"+field.getMarkupId()+" .typeahead').bind('typeahead:select', function(ev, suggestion) {");
		script.append("top.select = JSON.stringify(suggestion); selection();");
		script.append("$('#"+field.getMarkupId()+" .typeahead').typeahead('val', '');");
		
		script.append("});");
		return script.toString();
	}
	
	protected void setModel(IModel<T> model) {
		this.model = model;
	}
	
	protected IModel<T> getModel() {
		return model;
	}
	
	protected void addInput() {
		WebMarkupContainer field = new WebMarkupContainer("field");
		field.setOutputMarkupId(true);
		final org.apache.wicket.markup.html.form.TextField<T> input = new org.apache.wicket.markup.html.form.TextField<T>("input", getModel());
		input.setOutputMarkupId(true);
		if (getPlaceHolder()!=null && getPlaceHolder().getObject()!=null)
		input.add(new AttributeModifier("placeholder",  getPlaceHolder()));
		field.add(input);
		add(field);
	}
	
	protected IModel<String> getPlaceHolder() {
		return  placeholder;
	}
	

	protected void onInput(AjaxRequestTarget target) {
		if (getStringValue()!=null) {
			String value = getStringValue();
			value = value.replace(" -- ", " - ");
			value = value.replace(" - File", "");
			suggestions = getSuggestions(value);
		}
		else {
		}
		if (suggestions!=null) {
			String stringvalue = getStringValue();
			String valuetype = getValueType();
			String displayvalue = stringvalue;
			displayvalue = displayvalue.replace("'", "\"");
			if (valuetype!=null && !"".equals(valuetype))
				displayvalue += " - " + valuetype; 
			for (Suggestion suggestion : suggestions) {
				String value = suggestion.getText();
				if (value.equals(displayvalue)) {
					onSearch(target, suggestion);
					break;
				}
			}
		}
	}
}
 