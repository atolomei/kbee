package com.novamens.kbee.wicket.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupCacheKeyProvider;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.MarkupNotFoundException;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.response.StringResponse;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;

public class PanelCapture {
	
	Panel panel;
	
	private static class RenderPage extends WebPage implements	IMarkupResourceStreamProvider,	IMarkupCacheKeyProvider {
		private static final long serialVersionUID = 1L;

		private static final String DEFAULT_MARKUP = "<wicket:container wicket:id='%s'></wicket:container>";

		private final String markup;

		private RenderPage(Component component) 	{
			setStatelessHint(true);

			String componentMarkup;
			try 		{
				componentMarkup = component.getMarkup().toString(true);
			}
			catch (MarkupNotFoundException mnfx) {
				componentMarkup = String.format(DEFAULT_MARKUP, component.getId());
			}
			this.markup = componentMarkup;
			add(component);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,Class<?> containerClass) {
			return new StringResourceStream(markup);
		}

		@Override
		public String getCacheKey(MarkupContainer container, Class<?> containerClass) {
			return null;
		}

		@Override
		public boolean isBookmarkable() {
			return true;
		}
	}
	
	public PanelCapture(Panel data) {
		this.panel = data;
	}
	
	public String getString() {
		try {
			return new String(getStream().readAllBytes(), StandardCharsets.UTF_8);
		}
		catch (IOException e) {
			return null;
		}
	}
	
	public InputStream getStream() {
		StringResponse response = new StringResponse();
		Response oldResponse = RequestCycle.get().getResponse();
		RequestCycle.get().setResponse(response);
		try	{
			RenderPage page = new RenderPage(panel);
			page.internalInitialize();
			panel.beforeRender();
			panel.renderPart();
			InputStream stream = new ByteArrayInputStream(response.toString().getBytes());
			panel.detach();
			return stream;
		}
		catch (Exception e) {
			throw e;
		}
		finally	{
			RequestCycle.get().setResponse(oldResponse);
		}
	}
}