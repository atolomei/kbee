package com.novamens.wicket.markup.html.date;

import java.util.Date;
import java.util.Locale;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.googlecode.wicket.jquery.core.Options;
import com.googlecode.wicket.jquery.core.utils.LocaleUtils;

public class DatePicker extends com.googlecode.wicket.kendo.ui.form.datetime.DatePicker {
	private static final long serialVersionUID = 1L;
	

	public DatePicker(String id, IModel<Date> model) {
		this(id, model, getDefaultPattern(), new Options());
	}
	
	public DatePicker(String id, IModel<Date> model, String pattern, Options options) {
		super(id, model, pattern, options.set("culture", Options.asString(LocaleUtils.getLangageCode(new Locale("en")))));
	}
	
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		String culturejs = "es".equals(WebSession.get().getLocale().getLanguage()) ? "kendo.culture.es.min.js" : "kendo.culture.en.min.js";
		if ("es".equals(WebSession.get().getLocale().getLanguage()))
			response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(DatePicker.class, culturejs)));
		response.render(CssHeaderItem.forReference(new JavaScriptResourceReference(DatePicker.class,"kendo.css")));
	}
	
	public static String getDefaultPattern() {
		String pattern = "es".equals(WebSession.get().getLocale().getLanguage()) ? "dd/MM/yyyy" : "MM/dd/yyyy";
		return pattern;
	}
}
