package com.novamens.content.web.test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

import com.googlecode.wicket.jquery.ui.markup.html.link.AjaxLink;
import com.novamens.content.base.Content;
import com.novamens.content.entity.Person;
import com.novamens.content.user.UserService;
import com.novamens.kbee.template.KbeeContentTemplateModel;
import com.novamens.kbee.template.KbeeEMailTemplateModel;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.model.ObjectModel;

import freemarker.core.Environment;
import freemarker.core.InvalidFormatParametersException;
import freemarker.core.TemplateFormatUtil;
import freemarker.core.TemplateNumberFormat;
import freemarker.core.TemplateNumberFormatFactory;
import freemarker.core.UnformattableValueException;
import freemarker.template.Configuration;
import freemarker.template.SimpleSequence;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNodeModel;
import freemarker.template.TemplateNumberModel;

import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.form.TextAreaField;

import kbee.web.page.ApplicationPage;


	
public class SizeFormatFactory extends TemplateNumberFormatFactory {

	public static final SizeFormatFactory INSTANCE  = new SizeFormatFactory();

	private SizeFormatFactory() {
		// Defined to decrease visibility
	}
	
	@Override
	public TemplateNumberFormat get(String params, Locale locale, Environment env) throws InvalidFormatParametersException {
		TemplateFormatUtil.checkHasNoParameters(params);
		return SizeFormat.INSTANCE;
	}

	private static class SizeFormat extends TemplateNumberFormat {

		private static final SizeFormat INSTANCE = new SizeFormat();

		private SizeFormat() { }

		@Override
		public String formatToPlainText(TemplateNumberModel numberModel) throws UnformattableValueException, TemplateModelException {
			String format = "-";
			Number number = numberModel.getAsNumber();
			if (number instanceof Long) {
				String unit = "";
				Float size = (float)0;
				Long bz = (Long)number;
				Float kz = bz.floatValue()/1024;
				if (kz>1024) {
					Float mz = kz/1024;
					if (mz>1024) {
						
					}
					else {
						size  = mz;
						unit = "MB";
					}
				}
				if (size>0) {
					int si = (int)(size*10);
					size = ((float)si)/10;
					format = String.valueOf(size) + " " + unit;
				}
			}
//	            Number n = TemplateFormatUtil.getNonNullNumber(numberModel);
//	            try {
//	                return Integer.toHexString(NumberUtil.toIntExact(n));
//	            } catch (ArithmeticException e) {
//	                throw new UnformattableValueException(n + " doesn't fit into an int");
//	            }
			
			return format;
		}

		@Override
		public boolean isLocaleBound() {
			return false;
		}

		@Override
		public String getDescription() {
			return "hexadecimal int";
		}

	}

}
