package kbee.web.page;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.util.resource.IResourceStream;

import com.novamens.beans.BeansService;
import com.novamens.service.ServiceLocator;

public abstract class KbeeMarkupProvider implements IMarkupResourceStreamProvider {
	
	public abstract IResourceStream getMarkupResourceStream(final MarkupContainer container, Class<?> containerClass);
	public abstract String getCacheKey(final MarkupContainer container, Class<?> containerClass);

	public static KbeeMarkupProvider Get() {
		return (KbeeMarkupProvider)ServiceLocator.getService(BeansService.class).getBean("KbeeMarkupProvider");
	}
}
