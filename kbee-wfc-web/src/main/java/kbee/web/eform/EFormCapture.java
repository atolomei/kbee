package kbee.web.eform;

import java.io.InputStream;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupCacheKeyProvider;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.MarkupNotFoundException;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.response.StringResponse;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;

import com.novamens.content.form.EFormData;

import software.amazon.awssdk.utils.StringInputStream;

public class EFormCapture {
	
	EFormData data;
	
	private static class RenderPage extends WebPage implements	IMarkupResourceStreamProvider,	IMarkupCacheKeyProvider {
		private static final long serialVersionUID = -6133727221785871946L;

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
	
	public EFormCapture(EFormData data) {
		this.data = data;
	}
	
	public EFormData getData() {
		return data;
	}
	
	public String getString() {
		return ((StringInputStream)getStream()).getString();
	}
	
	public InputStream getStream() {
		StringResponse response = new StringResponse();
		Response oldResponse = RequestCycle.get()!=null ? RequestCycle.get().getResponse() : null;
		RequestCycle.get().setResponse(response);
		try	{
			EFormViewer viewer = getViewer();
			RenderPage page = new RenderPage(viewer);
			page.internalInitialize();
			viewer.beforeRender();
			viewer.renderPart();
			InputStream stream = new StringInputStream(response.toString());
			viewer.detach();
			return stream;
		}
		catch (Exception e) {
			throw e;
		}
		finally	{
			if (oldResponse!=null)
			RequestCycle.get().setResponse(oldResponse);
		}
	}
	
	public EFormViewer getViewer() {
		EFormViewer viewer;
		if (getData().getForm().getViewer()!=null) {
			boolean isPdfViewer = false;
			viewer = new EFormTemplateViewer("eform", new EFormDataModel(getData()), isPdfViewer );
		}
		else {
			viewer = new EFormStatelessViewer("eform", new EFormDataModel(getData()));
		}
		return viewer;
	}
	
}