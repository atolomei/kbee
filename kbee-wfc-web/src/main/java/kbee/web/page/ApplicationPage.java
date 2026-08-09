package kbee.web.page;

import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupCacheKeyProvider;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.resource.IResourceStream;

import com.novamens.kbee.wicket.markup.html.page.PageMainTabs;

import kbee.util.logging.Logger;

/**
 * 
 * ApplicationPage
 *
 * @param <T>
 */
public abstract class ApplicationPage<T> extends AbstractApplicationPage<T> implements IMarkupResourceStreamProvider,  IMarkupCacheKeyProvider {
	private static final long serialVersionUID = 1L;

	static Logger logger = new Logger(LogManager.getLogger(ApplicationPage.class.getName()));
	
	public ApplicationPage() {
		this(null, null, null, null);
	}
	
	public ApplicationPage(IModel<T> model) {
		this(model, null, null, null);
	}
	
	public ApplicationPage(IModel<T> model, Component navigation) {
		this(model, navigation, null);
	}
	
	public ApplicationPage(IModel<T> model, Component navigation, WebMarkupContainer menu) {
		this(model, navigation, null, null);
	}

	public ApplicationPage(IModel<T> model, Component top_navigation, Component menu, Panel pageContentHeader) {
		super(model, top_navigation, menu, pageContentHeader);
	}
	
	@Override
	public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass) {
		return KbeeMarkupProvider.Get().getMarkupResourceStream(container, containerClass);
	}
		
	@Override
	public String getCacheKey(MarkupContainer container, Class<?> containerClass) {
		return KbeeMarkupProvider.Get().getCacheKey(container, containerClass);
	}
	
	@Override
	public void onInitialize() {
			super.onInitialize();

			logger.debug(getClass().getName());
	
	}
	
	@Override
	public void onBeforeRender() {
			super.onBeforeRender();
			if (getInitialTab()!=null)
				wcom(getPage().iterator());
	}
	
	
	protected boolean wcom(Iterator<Component> it) {
		
		boolean done=false;
		
		while (it.hasNext() && !done) {
			Component c=it.next();
			if (c instanceof PageMainTabs) {
				((PageMainTabs) c).setInitialTab(getInitialTab());
				done=true;
				return done;
			}
			if (c instanceof MarkupContainer)
				done = wcom(((MarkupContainer)c).iterator());
		}
		return done;
	}

	
	private Boolean is_kbee_domain = null;
	
	protected boolean isKbeeDomain() {
		if (this.is_kbee_domain == null) {
			try {
				this.is_kbee_domain = Boolean.valueOf(getDomain().getName().toLowerCase().trim().equals("kbee"));
			} 
			catch (Exception e) {
				this.is_kbee_domain = Boolean.valueOf(false);
			}
		}
		return this.is_kbee_domain.booleanValue();
	}

	
}