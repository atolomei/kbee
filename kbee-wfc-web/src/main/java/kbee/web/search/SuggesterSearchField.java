package kbee.web.search;


import java.util.ArrayList;
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
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.TextRequestHandler;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
	private String info;
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
				//setStringValue((String)json.get("display"));
				setValueType((String)json.get("type"));
				setInfoValue((String)json.get("info"));
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

			String s = getJson(pattern);
			rc.replaceAllRequestHandlers(new TextRequestHandler("application/json", "UTF-8",s));
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
	
	public void setInfoValue(String value) {
		this.info = value;
	}
	
	public String getInfoValue() {
		return info;
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
			value = value.replace("\r\n", "");
			if (value.contains(" - ")) {
				int p = value.indexOf(" - ");
				type = value.substring(p);
				type = type.replace(" - ", "");
				type = "("+type+")";
				value = value.substring(0,p);
				json.append(" \"type\": \""+type+"\",");
			}
			else {
				json.append(" \"type\": \""+type+"\",");
			}
			value = value!=null ? value.replace("\"", "'") : "";
			//value="v";
			json.append(" \"value\": \""+value+"\",");
			json.append("\"display\": \""+value+" - "+type+"\"");
			
			if (includeInfo()) {
				String info = getInfo(suggestion);
				info = info.replace("\r", "");
				info = info.replace("\n", "");
				info = info.replace("\"", "\\\"");
				json.append(", \"info\": \""+info+"\"");
			}
			
			json.append("}");
			i++;
		}
		json.append("]");
		String s = json.toString();
		return s;
		
	}
	
	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptReferenceHeaderItem.forReference(getApplication().getJavaScriptLibrarySettings().getJQueryReference()));
		response.render(JavaScriptHeaderItem.forReference(JS));
		response.render(JavaScriptHeaderItem.forReference(BH));
		response.render(JavaScriptHeaderItem.forReference(HB));
		response.render(OnDomReadyHeaderItem.forScript( getScript2()));
        response.render(JavaScriptHeaderItem.forScript(getAdvancedOptionsJS(), "typeaheadhelper"));

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
		script.append("	limit: 999999,");
		script.append("	cache: false,");
		script.append("	wildcard: '%QUERY'");
		script.append("}");
		script.append("});");
		
		String template = null;
		if (includeInfo()) {
			//String template = "template = '<div class=\"list-group-item\" style=\"border:none;\"><span class=\"list-group-item-heading\">{{value}}" +
			//	"</span> - <span class=\"list-group-item-text\">{{info}}</span></div>'; ";

			template = "function(data) {  "+
				"var value = '<div class=\"list-group-item\" style=\"border:none;\"><span class=\"list-group-item-heading\">' + data.display; " +
				"if (data.info) { value = value + '</span> - '+ data.type +'<span class=\"list-group-item-text\" ><div style=\"color:#666666;\">' + data.info + '</div></span></div>'; } else { value = value + '</span></div>' };" +
				"return value;}";
			//script.append(template);
		}
		else {
			script.append("template = '<div>{{value}}' + ' <span class=\"ago\">{{type}}</span></div>';");
		}	
		
		script.append("top.ds"+field.getMarkupId()+"=source"+field.getMarkupId()+";");
		script.append("$('#"+field.getMarkupId()+" .typeahead').typeahead({");
		script.append("	highlight: true ");
		script.append("			}, {"); 
		script.append("	name: 'source"+field.getMarkupId()+"',");
		script.append("	display: 'display',");
		script.append("	limit: 10,");
		script.append("	cache: false,");
		script.append("	source: source"+field.getMarkupId()+",");
		
		if (!includeInfo()) {
			script.append("	templates: {");
			script.append("suggestion: Handlebars.compile(template)");
			script.append("	}");
		}
		else {
	        script.append(" templates: {");
	        //script.append("     header: function(context) { return typeaheadGetHeader(context, '" + field.getMarkupId() + "'); }, ");
	        script.append("     suggestion: "+template+" ");
	        //script.append("     footer: function(context) { return typeaheadGetFooter(context, '" + field.getMarkupId() + "'); }");
	        script.append(" }");
		}
		
		
		script.append("	});");
		script.append("$('#"+field.getMarkupId()+" .typeahead').bind('typeahead:select', function(ev, suggestion) {");
		script.append("top.select = JSON.stringify(suggestion); selection();");
		script.append("$('#"+field.getMarkupId()+" .typeahead').typeahead('val', '');");
		
		script.append("});");
		return script.toString();
	}
	

    @SuppressWarnings("unchecked")
    protected String getScript2() {
         StringBuffer script = new StringBuffer();

		MarkupContainer control = (MarkupContainer)get("field");

		JsonBehavior jsonbehavior = (JsonBehavior)getBehaviors(JsonBehavior.class).get(0);


        script.append("var typeahedSource" + control.getMarkupId() + " = new Bloodhound({");
        script.append(" datumTokenizer: Bloodhound.tokenizers.obj.whitespace('value'),");
        script.append(" queryTokenizer: Bloodhound.tokenizers.whitespace,");
        script.append(" remote: {");
        script.append(" 	url: '" + jsonbehavior.getCallbackUrl() + "&wicket-ajax=true&wicket-ajax-baseurl=/',");
        script.append(" 	limit: 999999,");
        script.append(" 	prepare: function(query, settings) {");
        if(isEnabledAdvancedOptions()) {
            script.append(" 	    var typeaheadStatus=typeaheadGetStatus('" + control.getMarkupId() + "');");
            script.append(" 	    var currentSortingOption=typeaheadGetCurrentSortOption('" + control.getMarkupId() + "');");
            script.append("         settings.url += '&q=' + query + '&sortOrder='+currentSortingOption.id+'&maxResults='+typeaheadStatus.maxResults;");
        }else{
            script.append("         settings.url += '&q=' + query;");
        }
        script.append("         return settings;");
        script.append("     },");
        script.append(" 	cache: false,");
        script.append(" }");
        script.append("});");

        script.append("$('#" + control.getMarkupId() + " .typeahead').typeahead({");
        script.append("		highlight: true ");
        script.append("	}, {");
        script.append("	name: 'typeahead-" + control.getMarkupId() + "',");
        script.append("	display: 'value',");
        script.append("	limit: 999999,");//see https://github.com/twitter/typeahead.js/issues/1415

        if(isEnabledAdvancedOptions()) {
            script.append(" templates: {");
            //script.append("     header: function(context) { return typeaheadGetHeader(context, '" + control.getMarkupId() + "'); }, ");
            if (getTemplate()!=null)
            script.append("     suggestion: "+getTemplate()+", ");
           // script.append("     footer: function(context) { return typeaheadGetFooter(context, '" + control.getMarkupId() + "'); }");
            script.append(" },");
        }
        script.append("	source: typeahedSource" + control.getMarkupId());
        script.append("});");

        script.append("$('#" + control.getMarkupId() + " .typeahead').bind('typeahead:select', function(ev, suggestion) {");
        script.append(" top.select = JSON.stringify(suggestion);");
        script.append(" window['" + control.getMarkupId() + " _preventOpenOnce']=true;");
        script.append(" selection();");
       // script.append(" $('#" + control.getMarkupId() + " .typeahead.tt-input').change();");

        script.append("});");

        script.append("$('#" + control.getMarkupId() + " .typeahead').bind('typeahead:open', function(e) {");
        script.append("    if (window['" + control.getMarkupId() + " _preventOpenOnce']) {");
        script.append("        window['" + control.getMarkupId() + " _preventOpenOnce']=false;");
        script.append("        $('#" + control.getMarkupId() + " .typeahead').typeahead('close');");
        script.append("    };");
        script.append("});");


        if(isEnabledAdvancedOptions()) {
           script.append("$('#" + control.getMarkupId() + " .typeahead.tt-input').on('change keydown paste', function () {");
           script.append(" typeaheadInitStatus('" + control.getMarkupId() + "');");
           script.append("});");

            script.append("$('#" + control.getMarkupId() + " .typeahead').bind('typeahead:close ', function() {");
            script.append(" var curval=$('#" + control.getMarkupId() + " .typeahead.tt-input').val();");
            script.append(" if(curval!=''){");
            script.append("     var typeahead=$('#" + control.getMarkupId() + " .typeahead');");
            //script.append("     typeahead.typeahead('val', '');");
            script.append(" }");
            script.append("});");


            script.append("typeaheadInitStatus('" + control.getMarkupId() + "');");
            script.append("typeaheadSetSortOptions('" + control.getMarkupId() + "'," + getSortOptionsJson() + ");");
        }
        return script.toString();
    }
    

    public String getAdvancedOptionsJS() {
        return "" +
                "function typeaheadGetStatus(controlId){" +
                "	return window[controlId+'_status'];" +
                "}" +
                "function typeaheadInitStatus(controlId){" +
                "	window[controlId+'_status'] = { " +
                "		sortOptionIdx: 0," +
                "		maxResults: 100," +
                "	};" +
                "}" +
                "function typeaheadSetSortOptions(controlId, sortOptions){" +
                "	window[controlId+'_SortOptions'] = sortOptions;" +
                "}" +
                "function typeaheadGetSortOptions(controlId){" +
                "	return window[controlId+'_SortOptions'];" +
                "}" +
                "function typeaheadGetCurrentSortOption(controlId){" +
                "	var typeaheadStatus=typeaheadGetStatus(controlId);" +
                "	return typeaheadGetSortOptions(controlId)[typeaheadStatus.sortOptionIdx];" +
                "}" +
                "function typeaheadSwapCurrentSortOption(controlId){" +
                "	var typeaheadStatus=typeaheadGetStatus(controlId);" +
                "	var sortOptions=typeaheadGetSortOptions(controlId);" +
                "	typeaheadStatus.sortOptionIdx= (typeaheadStatus.sortOptionIdx+1)%sortOptions.length;" +
                "	typeaheadRefresh(controlId);" +
                "}" +
                "function typeaheadRefresh(controlId){ " +
                "	var engine=eval('typeahedSource'+controlId);" +
                "	var typeahead=$('#'+controlId+' .typeahead');" +
                "	var curval=$('#'+controlId+' .typeahead.tt-input').val();" +
                "	if(curval != '')" +
                "		typeahead.typeahead('val', '');" +
                "	else" +
                "		typeahead.typeahead('val', '-');" +
                "	engine.clear();" +
                "	engine.clearPrefetchCache();" +
                "	engine.clearRemoteCache();" +
                "	typeahead.typeahead('val', curval);" +
                "	if(curval == ''){" + //when no input must explicitly close/open or it wont open
                "		typeahead.typeahead('close');" +
                "		typeahead.typeahead('open2');" +
                "	}" +
                "}" +
                "function typeaheadShowMore(controlId){ " +
                "	console.log('typeaheadToogleSort ' + controlId);" +
                "	var typeaheadStatus=typeaheadGetStatus(controlId);" +
                "	typeaheadStatus.maxResults+=100;" +
                "	typeaheadRefresh(controlId);" +
                "}" +
                "function typeaheadGetFooter(context, controlId){ " +
                "	var typeaheadStatus=typeaheadGetStatus(controlId);" +
                "   var resultCount=context.suggestions.length;" +
                "   if( resultCount > 10){" +
                "     if( typeaheadStatus.maxResults == resultCount){" +
                "       var showMore= '" + new StringResourceModel("typeahead.showMore", this).getString() + "';" +
                "       var showingResults= '" + new StringResourceModel("typeahead.showingResults", this).getString() + "';" +
                "       return showingResults.replace('{0}', resultCount) + ' ( <a id=\"' + controlId + '_showAll\" onClick=\"typeaheadShowMore(\\'' + controlId + '\\');\">' + showMore + '</a> )';" +
                "     }else{" +
                "       var showingAllResults= '" + new StringResourceModel("typeahead.showingAllResults", this).getString() + "';" +
                "       return showingAllResults.replace('{0}', resultCount); " +
                "      }" +
                "    }else{" +
                "      return '<div />';" +
                "    }" +
                "}" +
                "function typeaheadGetHeader(context, controlId){ " +
                "   return '<i id=\"' + controlId + '_ordering\" class=\"'+ typeaheadGetCurrentSortOption(controlId).cssClass + '\" title=\"'+ typeaheadGetCurrentSortOption(controlId).toolTip + '\" style=\"float:right; position:relative; z-index:10000;\" aria-hidden=\"true\" onClick=\"typeaheadSwapCurrentSortOption(\\'' + controlId + '\\')\"></i>'" +
                "}" +
                "";
    }
    
    private String getTemplate() {
		String template = "function(data) {  "+
				"var value = '<div class=\"list-group-item\" style=\"border:none;\"><span class=\"list-group-item-heading\">' + data.value; " +
				"if (data.info) "+
				"{ value = value + '</span> - '+ data.type +'<span class=\"list-group-item-text\" ><div style=\"color:#666666;\">' + data.info + '</div></span></div>'; } "+
				"else "+
				"{ value = value + '</span> - <span style=\"color:#666666;\">'+ data.type+'</span></div>' };" +
//				"{ value = value + '</span></div>' };" +
				"return value;}";
		return template;
    	
    }
    
	static private ObjectMapper mapper = new ObjectMapper();

    
    private String getSortOptionsJson() {
        
        try {
            return mapper.writeValueAsString(getSortingOptions());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    
	public List<SortOption> getSortingOptions() {
        ArrayList<SortOption> sortOptionList = new ArrayList<>();
        sortOptionList.add(new SortOption("relevance_desc", "far fa-sort-amount-down", new StringResourceModel("sort.relevance_desc", this).getString()));
        sortOptionList.add(new SortOption("alpha_asc", "far fa-sort-alpha-down", new StringResourceModel("sort.alpha_asc", this).getString()));
        sortOptionList.add(new SortOption("alpha_desc", "far fa-sort-alpha-up", new StringResourceModel("sort.alpha_desc", this).getString()));
        return sortOptionList;
    }
    
  boolean isEnabledAdvancedOptions() {
	  return true;
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
	
	protected boolean includeInfo() {
		return false;
	}
	
	protected String getInfo(Suggestion suggestion) {
		return null;
	}

	protected void onInput(AjaxRequestTarget target) {
		if (getStringValue()!=null) {
			String value = getStringValue();
			value = value.replace(" -- ", " - ");
			value = value.replace(" - File", "");
			suggestions = getSuggestions("\""+value+"\"");
		}
		else {
		}
		if (suggestions!=null) {
			String stringvalue = getStringValue();
			String valuetype = getValueType();
			String displayvalue = stringvalue;
			if (valuetype!=null && !"".equals(valuetype))
				displayvalue += " - " + valuetype;
			List<Suggestion> matchs = new ArrayList<>();
			for (Suggestion suggestion : suggestions) {
				String value = suggestion.getText();
				if (value.equals(displayvalue)) {
					matchs.add(suggestion);
				}
			}
			if (matchs.size()>1 && getInfoValue()!=null) {
				matchs = new ArrayList<>();
				for (Suggestion suggestion : suggestions) {
					String value = suggestion.getText();
					String info = getInfo(suggestion);
					if (value.equals(displayvalue) && info!=null && getInfoValue().equals(info)) {
						matchs.add(suggestion);
					}
				}
			}
			if (!matchs.isEmpty()) {
				onSearch(target, matchs.get(0));
			}
		}
	}
	
    private static class SortOption {
        String id;
        String cssClass;
        String toolTip;

        public SortOption(String id, String cssClass, String toolTip) {
            this.id = id;
            this.cssClass = cssClass;
            this.toolTip = toolTip;

        }

        public String getCssClass() {
            return cssClass;
        }

        public void setCssClass(String cssClass) {
            this.cssClass = cssClass;
        }

        public String getToolTip() {
            return toolTip;
        }

        public void setToolTip(String toolTip) {
            this.toolTip = toolTip;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
 