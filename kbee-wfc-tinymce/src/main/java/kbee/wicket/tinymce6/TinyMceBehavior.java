
package kbee.wicket.tinymce6;

import java.util.Collection;
import java.util.Collections;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.cycle.RequestCycle;

import org.apache.wicket.request.http.WebRequest;

import kbee.wicket.tinymce6.settings.TinyMCESettings;
import kbee.wicket.tinymce6.settings.TinyMCESettings.Mode;




/**
 * Renders a component (textarea) as WYSIWYG editor, using TinyMce.
 */
public class TinyMceBehavior extends Behavior
{
	private static final long serialVersionUID = 3L;

	private Component component;
	private TinyMCESettings settings;
	private boolean rendered = false;

	public TinyMceBehavior()
	{
		this(new TinyMCESettings());
	}

	public TinyMceBehavior(TinyMCESettings settings)
	{
		this.settings = settings;
	}

	@Override
	public void renderHead(Component c, IHeaderResponse response)
	{
		super.renderHead(c, response);
		if (component == null)
			throw new IllegalStateException("TinyMceBehavior is not bound to a component");

		//ResourceReferenceRequestHandler handler = new ResourceReferenceRequestHandler(
		//		TinyMCESettings.javaScriptReference(), null);
		
		// TinyMce javascript:
		if (mayRenderJavascriptDirect())
		{
			response.render(JavaScriptHeaderItem.forReference(TinyMCESettings.javaScriptReference()));
		}
		else
		{
			TinyMCESettings.lazyLoadTinyMCEResource(response);
		}

		String renderOnDomReady = getAddTinyMceSettingsScript(Mode.exact,
				Collections.singletonList(component));
		response.render(wrapTinyMceSettingsScript(renderOnDomReady, component));
	}
	
	/**
	 * Wrap the initialization script for TinyMCE into a HeaderItem. In this way we can control
	 * when and how the script should be executed.
	 * 
	 * @param settingScript
	 * 			the actual initialization script for TinyMCE
	 * @param component
	 * 			the target component that must be decorated with TinyMCE 
	 * @return
	 * 			the HeaderItem containing {@paramref settingScript}
	 * 
	 */
	protected HeaderItem wrapTinyMceSettingsScript(String settingScript, Component component){
		return OnDomReadyHeaderItem.forScript(settingScript);
	}

	private boolean mayRenderJavascriptDirect()
	{
		return RequestCycle.get().getRequest() instanceof WebRequest
				&& !((WebRequest)RequestCycle.get().getRequest()).isAjax();
	}


	protected String getAddTinyMceSettingsScript(Mode mode, Collection<Component> components)
	{
		StringBuffer script = new StringBuffer();
		// If this behavior is run a second time, it means we're redrawing this
		// component via an ajax call. The tinyMCE javascript does not handle
		// this scenario, so we must remove the old editor before initializing
		// it again.
		if (rendered)
		{
			for (Component c : components)
			{
				String tryToRemoveJS = "try{tinyMCE.remove(tinyMCE.get('%s'));}catch(e){}\n";
				script.append(String.format(tryToRemoveJS, c.getMarkupId()));
			}
		}

		script.append(settings.getLoadPluginJavaScript());
		script.append(" tinymce.init({" + settings.toJavaScript(mode, components) + " });\n");
		script.append(settings.getAdditionalPluginJavaScript());
		rendered = true;

		String code = script.toString();
		
		//code = " tinymce.init({ selector: 'textarea#" + component.getMarkupId() + "'});"; 
		
		return code;
	}

	@Override
	public void bind(Component component)
	{
		if (this.component != null)
			throw new IllegalStateException(
					"TinyMceBehavior can not bind to more than one component");
		super.bind(component);
		if (isMarkupIdRequired())
			component.setOutputMarkupId(true);
		this.component = component;
	}

	protected boolean isMarkupIdRequired()
	{
		return true;
	}

	protected Component getComponent()
	{
		return component;
	}
}
