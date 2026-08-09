package kbee.web.page;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.DefaultMarkupCacheKeyProvider;
import org.apache.wicket.markup.DefaultMarkupResourceStreamProvider;
import org.apache.wicket.util.resource.IResourceStream;

public class KbeeMarkupDefaultProvider extends  KbeeMarkupProvider {
	
	@Override
	public IResourceStream getMarkupResourceStream(final MarkupContainer container, Class<?> containerClass) {
		return (new DefaultMarkupResourceStreamProvider()).getMarkupResourceStream(container, containerClass);
	}
	
	@Override
	public String getCacheKey(final MarkupContainer container, Class<?> containerClass) {
		return (new DefaultMarkupCacheKeyProvider()).getCacheKey(container, containerClass);
	}
}
