package kbee.web.form;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novamens.indexer.query.QuerySortOrder;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.TextRequestHandler;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.validation.IValidator;
import org.hibernate.SessionFactory;

import com.novamens.beans.BeansService;
import com.novamens.indexer.query.Suggestion;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.behaviour.KeyboardBehavior;
import com.novamens.kbee.wicket.util.DisplayNameExtractor;
import com.novamens.security.Identifiable;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.form.FocusOnLoadBehavior;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.model.ObjectModel;


@SuppressWarnings("serial")
public abstract class AutoCompleteFieldV5<T> extends TextField<T> {
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AutoCompleteFieldV5.class.getName());
	private static final int MAX_HISTORY = 12;
	private static final ResourceReference JS = new JavaScriptResourceReference(Form.class, "typeahead.bundle.js");

	private Suggestion suggestion;
	private List<Suggestion> suggestions;

	private String stringvalue;
	private String infovalue;
	private List<IModel<T>> history;
	
	private boolean renderhead = false;
	private boolean helptextvisible = false;
	private int max_history = MAX_HISTORY;
	
	SelectBehavior selectionBehavior;

	
	/**
	 *
	 */
	class JsonBehavior extends AbstractDefaultAjaxBehavior {
        @Override
        protected void respond(AjaxRequestTarget target) {
            RequestCycle rc = RequestCycle.get();
            Request request = rc.getRequest();
            String q = request.getRequestParameters().getParameterValue("q").toString("");
            if(isEnabledAdvancedOptions()) {
                String maxResultsParam = request.getRequestParameters().getParameterValue("maxResults").toString("");
                String sortOrderParam = request.getRequestParameters().getParameterValue("sortOrder").toString("");
                QuerySortOrder querySortOrder = QuerySortOrder.fromId(sortOrderParam, QuerySortOrder.RELEVANCE);

                int maxResults = 100;
                try {
                    maxResults = Integer.parseInt(maxResultsParam);
                } 
                catch (Exception e) {
                }
                suggestions = getSuggestions(q, maxResults, querySortOrder);;

            }else{
                suggestions = getSuggestions(q);;
            }
            rc.replaceAllRequestHandlers(new TextRequestHandler("text/html", "UTF-8", getJson(suggestions)));
        }
        @Override
        protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
            super.updateAjaxAttributes(attributes);
        }
    }
	
	public class SelectBehavior extends AbstractDefaultAjaxBehavior {
		@Override
		protected void respond(AjaxRequestTarget target) {
			
			try {
				Request request = RequestCycle.get().getRequest();
				String select = request.getRequestParameters().getParameterValue("select").toString("");
				KbeeJson json = new KbeeJson(select);
				setStringValue((String)json.get("value"));
				setInfoValue((String)json.get("info"));
				onInput(target);
			} catch (Exception e) {
				logger.error(e);
			}
			
		}
		@Override
		public void renderHead(final Component component, final IHeaderResponse response) {
			super.renderHead(component, response);
			StringBuilder script = new StringBuilder();
			script.append("function aselection() {\n");
			script.append(getCallbackScript());
			script.append("}\n");
			response.render(JavaScriptHeaderItem.forScript(script.toString(), "aselect"));
		}
		@Override
		protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
			super.updateAjaxAttributes(attributes);
			attributes.getDynamicExtraParameters().add("return {select: top.select};");
		}
	}

	
	/**
	 *
	 */
	public class ControlFragment extends Fragment {
		boolean islabelvisible = false;
		public ControlFragment(String id) {
			super(id, "control-fragment", AutoCompleteFieldV5.this);
		}
		@Override
		public void onInitialize() {
			super.onInitialize();
            WebMarkupContainer th = new WebMarkupContainer("th");
            th.setOutputMarkupId(true);
            org.apache.wicket.markup.html.form.TextField<?> input = newTextField();

            input.setOutputMarkupId(true);

            input.add(new AjaxFormComponentUpdatingBehavior("change") {
                protected void onUpdate(AjaxRequestTarget target) {
                   AutoCompleteFieldV5.this.onInput(target);
                  if (AutoCompleteFieldV5.this.hasFeedback()) {
                      AutoCompleteFieldV5.this.validate();
                      target.add(AutoCompleteFieldV5.this);
                  }
                }

                protected void onError(AjaxRequestTarget target, RuntimeException e) {
                    target.add(AutoCompleteFieldV5.this);
                }
            });

            input.add(new KeyboardBehavior() {
                protected void onKey(AjaxRequestTarget target, String jsKeycode) {
                    AutoCompleteFieldV5.this.onKey(target, jsKeycode);
                }
            });

            th.add(input);

            th.add(getSearcher(th.getMarkupId()));

            add(th);
            
            addHelpLink();

            add(getFeedback());
            add(new HistoryFragment("history"));
            add(new JsonBehavior());

            add(getInfo());
		}
		public T getValue() {
			return AutoCompleteFieldV5.this.getValue();
		}
		public void setValue(T value) {
			AutoCompleteFieldV5.this.setValue(value);
		}
		protected void addHelpLink() {
			IModel<String> help = getHelpText();
			AjaxLink<T> hl = new AjaxLink<T>("help-link-choice") {
				
				public void onDetach() {
					super.onDetach();
					if (getModel()!=null) {
						if (getModel().getObject()!=null) {
							if (getModel().getObject() instanceof IDetachable) {
								((IDetachable) getModel().getObject()).detach();
							}
						}
					}
					
				}
				@Override
				public void onClick(AjaxRequestTarget target) {
					setHelpTextVisible(!isHelpTextVisible());
					target.add(AutoCompleteFieldV5.this);
				}
				public boolean isVisible() {
					return isHelpVisible(); 
				}
			};
			Label la = new Label("helpstr", new StringResourceModel("help", AutoCompleteFieldV5.this, null));
			la.setEscapeModelStrings(false);
			hl.add(la);
			add(hl);
			hl.setVisible(false);
			if (help!=null && help.getObject()!=null) {
				Label label=new Label ("help", help) {
					public boolean isVisible() {
						return isHelpTextVisible();
					}
				};
				label.setEscapeModelStrings(false);
				add(label);
			}
			else
				add((new Label ("help", "")).setVisible(true));
		}
	}

	
	
	/**
	 *
	 */
    public class HistoryFragment extends Fragment {

        public HistoryFragment(String id) {
            super(id, "history-fragment", AutoCompleteFieldV5.this);
            setOutputMarkupId(true);
        }

        @Override
        public void onInitialize() {
            super.onInitialize();

            add(new ListView<IModel<T>>("history", new PropertyModel<List<IModel<T>>>(AutoCompleteFieldV5.this, "history")) {

                public void populateItem(final ListItem<IModel<T>> item) {
                    AjaxLink<T> link = new AjaxLink<T>("link", item.getModelObject()) {
                        public void onClick(AjaxRequestTarget target) {
                            T value = item.getModelObject().getObject();
                            String valueDisplayName = DisplayNameExtractor.get(item.getModelObject().getObject());

                            if(getFirstHit(getSuggestions(valueDisplayName), valueDisplayName) == null) {
                                value = null;
                                valueDisplayName="";
                            }

                            setValue(value);
                            setSuggestion(null);
                            setStringValue(valueDisplayName);
                            ((org.apache.wicket.markup.html.form.TextField<?>) AutoCompleteFieldV5.this.getInput()).clearInput();
                            ((org.apache.wicket.markup.html.form.TextField<?>) AutoCompleteFieldV5.this.getInput()).validate();
                            target.add(AutoCompleteFieldV5.this);
                            onUpdate(target);
                        }
                    };
                    try {
                        Object value = item.getModelObject().getObject();
                        link.add(new Label("name", DisplayNameExtractor.get(value)));
                        link.setVisible(value != null);
                        item.add((new Label("separator", "-")).setVisible(value != null && item.getIndex() > 0));
                        item.add(link);
                    } catch (Exception e) {
                        logger.error(e);
                    }
                }
            });
        }

        @Override
        public boolean isVisible() {
            return isInputEnabled();
        }
    }

    public AutoCompleteFieldV5(String id) {
        this(id, null, false, Width.W12);
    }

    public AutoCompleteFieldV5(String id, IValidator<T> validator) {
        this(id, null, false, Width.W12);
        add(validator);
    }

    public AutoCompleteFieldV5(String id, Width width) {
        this(id, null, false, width);
    }

    public AutoCompleteFieldV5(String id, boolean required) {
        this(id, null, required, Width.W12);
    }

    public AutoCompleteFieldV5(String id, IModel<T> model) {
        this(id, model, false, Width.W12);
    }

    public AutoCompleteFieldV5(String id, IModel<T> model, boolean required) {
        this(id, model, required, Width.W12);
    }

    public AutoCompleteFieldV5(String id, IModel<T> model, boolean required, Width width) {
        super(id, model, required, width, null);
        setOutputMarkupId(true);
    }

    @Override
    public void onDetach() {

        if (history != null)
        	history.forEach(item->item.detach());
        
       	suggestions = null;
       	suggestion = null;

        super.onDetach();
    }

    
    
    public Suggestion getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(Suggestion suggestion) {
        this.suggestion = suggestion;
    }

    public String getStringValue() {
        return stringvalue;
    }

    public void setStringValue(String value) {
        this.stringvalue = value;
    }
    

    public String getInfoValue() {
		return infovalue;
	}

	public void setInfoValue(String info) {
		this.infovalue = info;
	}

	public Component getInput() {
        if (getDisposition() == null || getDisposition() == Disposition.HORIZONTAL) {
            return get("horizontal-layout:control:th:input");
        } else {
            return get("control:th:input");
        }
    }

    public List<IModel<T>> getHistory() {

        if (history != null)
            return history;

        history = new ArrayList<IModel<T>>();

        try {
            if (getHistoryKey() == null) {
                logger.debug("getHistoryKey()==null");
                return history;
            }
        } catch (Exception e) {
            logger.error(e);
            return history;
        }

        String strvalue = getSessionUser() != null ?
                getSessionUser().getService(PreferencesService.class).getValue("autocomplete", getHistoryKey()) :
                null;

        if (strvalue == null)
            return history;

        StringTokenizer tokenizer = new StringTokenizer(strvalue, ";");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            IModel<T> valuemodel = null;
            try {
                valuemodel = deserialize(token);
                if (valuemodel != null) {
                    valuemodel.detach();
                }
            } catch (Exception e) {
                logger.error(e);
                valuemodel = null;
            }
            if (valuemodel != null && isValid(valuemodel)) {
                history.add(valuemodel);
            }
        }

        return history;
    }

    @SuppressWarnings("unchecked")
    public void onInput(AjaxRequestTarget target) {
        if (getStringValue() != null) {
            suggestions = getSuggestions("\""+getStringValue()+"\"");
        } else {
            setValue(null);
            onUpdate(target);
        }

        if (suggestions != null) {
            final Suggestion firstHit = getFirstHit(suggestions,getStringValue());
            if(firstHit!=null){
                    setSuggestion(firstHit);
                    if (firstHit.getObject() instanceof IModel) {
                        setValue(((IModel<T>) firstHit.getObject()).getObject());
                    } else
                        setValue((T) firstHit.getObject());
                    addHistory(getValue());
                    target.add(get("control:history"));
                    onUpdate(target);
            }else{
                setStringValue(null);
                setValue(null);
                onUpdate(target);
            }
        }
    }

    public Suggestion getFirstHit(List<Suggestion> suggestions, String value){
        for (Suggestion suggestion : suggestions) {
            if (escape(getValue(suggestion)).equals(value)) {
            	if (isSelectionBehavior() && getInfoValue()!=null) {
            		String infosuggestionvalue = getInfo(suggestion);
            		if (getInfoValue().equals(infosuggestionvalue)) {
                        return suggestion;
            		}
            	}
            	else {
            		return suggestion;
            	}
            }
        }
        return null;
    }

    public List<Suggestion> getSuggestions(String pattern, int maxResults, QuerySortOrder querySortOrder) {
        return getSuggestions(pattern);
    }

    public List<Suggestion> getSuggestions(String pattern) {
        return null;
    }
    
    public abstract String getHistoryKey();

    public void setMaxHistory(int m) {
        max_history = m;
    }

    public int getMaxHistory() {
        return max_history;
    }
    
    public void clearCache(AjaxRequestTarget target) {
        MarkupContainer control = (MarkupContainer) get("control:th");
        if (getDisposition() == null || getDisposition() == Disposition.HORIZONTAL) {
            control = (MarkupContainer) get("horizontal-layout:control:th");
        }
        if (control == null) return;
        String markupid = control.getMarkupId();
        String s = "top.ds" + markupid + ".clearPrefetchCache();";
        s += "top.ds" + markupid + ".clearRemoteCache();";
        target.appendJavaScript(s);
    }

    public void clearCache(Response response) {
        response.write("<script type=\"text/javascript\">");
        MarkupContainer control = (MarkupContainer) get("control:th");
        String markupid = control.getMarkupId();
        String s = "top.ds" + markupid + ".clearPrefetchCache();";
        s += "top.ds" + markupid + ".clearRemoteCache();";
        response.write(s);
        response.write("</script>");
    }

    public void clearInput(AjaxRequestTarget target) {
        super.clearInput();
        setValue(null);
        MarkupContainer control = (MarkupContainer) get("control:th");
        if (getDisposition() == null || getDisposition() == Disposition.HORIZONTAL) {
            control = (MarkupContainer) get("horizontal-layout:control:th");
        }
        if (control == null) return;
        String markupid = control.getMarkupId();
        String s = "$('#" + markupid + " .typeahead').typeahead('val', '')";
        target.appendJavaScript(s);
    }
    
    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);
        renderhead = true;
        response.render(JavaScriptHeaderItem.forReference(JS));
        if (isInputEnabled() && isEnabledAdvancedOptions())
            response.render(JavaScriptHeaderItem.forScript(getAdvancedOptionsJS(), "typeaheadhelper"));
    }

    @Override
    public void onBeforeRender() {
        boolean render = false;
        if (get("horizontal-layout") == null) {
            render = true;
            if (isSelectionBehavior()) {
	            selectionBehavior = new SelectBehavior();
	    		getPage().add(selectionBehavior);
            }
        }
        super.onBeforeRender();
        if (render && getValue() != null && getStringValue() == null) {
            setStringValue(DisplayNameExtractor.get(getValue()));
        }
        if (autofocus()) {
            getInput().add(new FocusOnLoadBehavior());
        }
    }
 
    public boolean isHelpTextVisible() {
		return helptextvisible;
	}

	public void setHelpTextVisible(boolean value) {
		this.helptextvisible = value;
	}

	@Override
    public void onAfterRender() {
        super.onAfterRender();
        if (get("horizontal-layout") != null && renderhead && isInputEnabled()) {
            getResponse().write("<script type=\"text/javascript\">");
            getResponse().write(getScript());
            getResponse().write("</script>");
        }
    }
    

    @Override
    protected Object getInputValue() {
        return getValue();
    }


    protected String getJson(List<Suggestion> suggestions) {
        final Iterator<Suggestion> suggestionIterator = suggestions.iterator();
        JsonFactory jfactory = new JsonFactory();
        //[{value:"value"}]
        try {
            StringWriter jsonObjectWriter = new StringWriter();
            try (JsonGenerator jsonGenerator = jfactory.createGenerator(jsonObjectWriter)) {
                jsonGenerator.writeStartArray();//[
                while(suggestionIterator.hasNext()) {
                    Suggestion suggestion = suggestionIterator.next();
                    jsonGenerator.writeStartObject();//{
                    jsonGenerator.writeStringField("value", getValue(suggestion));
                    if (getTemplate()!=null) {
                    	String info = getInfo(suggestion);
//                    	if (info!=null) {
                    		jsonGenerator.writeStringField("info", info==null?"":info);
 //                   	}
                    }
                    jsonGenerator.writeEndObject();//}
                }
                jsonGenerator.writeEndArray();//]
            }
            return jsonObjectWriter.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    protected String getValue(Suggestion suggestion) {
    	return suggestion.getText();
    }
    
    protected String getInfo(Suggestion suggestion) {
    	return null;
    }
    
    protected String getTemplate() {
    	return null;
    }
    
    protected String escape(String text) {
        return text != null ? text.replace("\"", "'") : "";
    }

    protected Component getSearcher(final String markupid) {
        WebMarkupContainer searcher = new WebMarkupContainer("searcher") {
            @Override
            public boolean isVisible() {
                return isEnabledInHierarchy() && isInputEnabled();
            }
        };
        searcher.add(new AttributeModifier("onclick", new Model<String>() {
            public String getObject() {
                return "$('#" + markupid + " .typeahead').typeahead('open2');" +
                        "setTimeout(function(){ $('#" + markupid + " .typeahead.tt-input').focus() }, 10);";//open2 wont work without setTimeout :-(
            }
        }));
        return searcher;
    }

    @SuppressWarnings("unchecked")
    protected String getScript() {
        StringBuffer script = new StringBuffer();
        Component control = null;

        if (getDisposition() == null || getDisposition() == Disposition.HORIZONTAL) {
            control = (MarkupContainer) get("horizontal-layout:control:th");
        } else {
            control = (MarkupContainer) get("control:th");
        }

        JsonBehavior jsonbehavior = control.getParent().getBehaviors(JsonBehavior.class).get(0);

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
            script.append("     header: function(context) { return typeaheadGetHeader(context, '" + control.getMarkupId() + "'); }, ");
            if (getTemplate()!=null)
            script.append("     suggestion: "+getTemplate()+", ");
            script.append("     footer: function(context) { return typeaheadGetFooter(context, '" + control.getMarkupId() + "'); }");
            script.append(" },");
        }
        script.append("	source: typeahedSource" + control.getMarkupId());
        script.append("});");

        script.append("$('#" + control.getMarkupId() + " .typeahead').bind('typeahead:select', function(ev, suggestion) {");
        script.append(" top.select = JSON.stringify(suggestion);");
        script.append(" window['" + control.getMarkupId() + " _preventOpenOnce']=true;");
        if (isSelectionBehavior()) {
        	script.append(selectionBehavior.getCallbackScript()+";");
        }
        else {
        	script.append("$('#" + control.getMarkupId() + " .typeahead.tt-input').change();");
        };

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

    public boolean isEnabledAdvancedOptions(){
        return false;
    }

     
	public List<SortOption> getSortingOptions() {
        ArrayList<SortOption> sortOptionList = new ArrayList<>();
        sortOptionList.add(new SortOption("relevance_desc", "far fa-sort-amount-down", new StringResourceModel("sort.relevance_desc", this).getString()));
        sortOptionList.add(new SortOption("alpha_asc", "far fa-sort-alpha-down", new StringResourceModel("sort.alpha_asc", this).getString()));
        sortOptionList.add(new SortOption("alpha_desc", "far fa-sort-alpha-up", new StringResourceModel("sort.alpha_desc", this).getString()));
        return sortOptionList;
    }

	static private ObjectMapper mapper = new ObjectMapper();
	
    private String getSortOptionsJson() {
    
        try {
            return mapper.writeValueAsString(getSortingOptions());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    protected boolean isValid(IModel<T> model) {
        return true;
    }

    protected Fragment newControlFragment() {
        return new ControlFragment("control");
    }

    protected void addHistory(T value) {
        boolean found = false;
        if (!(value instanceof Identifiable))
            return;

        for (IModel<T> model : getHistory()) {
            if (model.getObject()!=null && model.getObject().equals(value)) {
                found = true;
                break;
            }
        }

        if (!found) {
            getHistory().add(0, getModel(value));
            if (getHistory().size() > getMaxHistory()) {
                getHistory().remove(getHistory().size() - 1);
            }
            updateHistory();
        }
    }

    protected org.apache.wicket.markup.html.form.TextField<?> newTextField() {

        org.apache.wicket.markup.html.form.TextField<?> input = new org.apache.wicket.markup.html.form.TextField<String>("input", new PropertyModel<String>(this, "stringValue")) {
            @Override
            public void validate() {
                AutoCompleteFieldV5.this.validate();
                super.validate();
            }
            @Override
            public boolean isEnabled() {
                return isInputEnabled();
            }
            protected void onComponentTag(final ComponentTag tag) {
                super.onComponentTag(tag);
            }
        };

        return input;
    }

    protected void updateHistory() {
        if (getHistoryKey() == null)
            return;
        StringBuffer buffer = new StringBuffer();
        boolean first = true;
        for (IModel<T> model : getHistory()) {
            if (!first)
                buffer.append(";");
            if (model.getObject()!=null) {
            	buffer.append(serialize(model));
            }
            first = false;
        }
        if (getSessionUser() != null)
            getSessionUser().getService(PreferencesService.class).setValue("autocomplete", getHistoryKey(), buffer.toString());
    }

    protected String serialize(IModel<T> model) {
        String classname = model.getObject().getClass().getName();
        int i = classname.indexOf("_");
        if (i > 0) classname = classname.substring(0, i);
        i = classname.indexOf("$");
        if (i > 0) classname = classname.substring(0, i);
        return classname + "-" + ((Identifiable) model.getObject()).getId();
    }

    protected IModel<T> deserialize(String token) {
        int i = token.indexOf("-");
        if (i <= 0) return null;
        String classname = token.substring(0, i);
        String id = token.substring(i + 1);
        IModel<T> model = getModel(classname, id);
        return model;
    }

    protected IModel<T> getModel(String classname, String id) {
        ObjectModel<T> model = null;
        try {
            Class<?> clazz = Class.forName(classname);
            SessionFactory sf = (SessionFactory) ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
            @SuppressWarnings("unchecked")
            Object object = (T) sf.getCurrentSession().get(clazz, Long.valueOf(id));
            if (object != null) {
                model = new ObjectModel<T>(clazz, Long.valueOf(id));
                model.getObject();
            }
        } catch (Exception e) {
            model = null;
        }
        return model;
    }
    
    protected boolean isSelectionBehavior() {
    	return false;
    }

    protected KbeeUser getSessionUser() {
        return (KbeeUser) ServiceLocator.getService(SecurityService.class).getSessionUser();
    }


    @SuppressWarnings("unused")
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