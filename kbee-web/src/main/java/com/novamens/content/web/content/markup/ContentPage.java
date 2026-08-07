package com.novamens.content.web.content.markup;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.ContentId;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.service.ServiceLocator;

import kbee.web.nav.NavigationPanel;
import kbee.web.page.AbstractApplicationPage;
import kbee.web.page.ApplicationMenuSection;

/**
 * 
 *  AbstractApplicationPage -> IntegratedPage -> ConsolePage
 *                    							 BasicIntegratedPage
 *  StandAlonePage ->   
 *  
 * @param <T>
 */
@SuppressWarnings("serial")
public class ContentPage<T extends Content> extends AbstractApplicationPage<T>  {
				
	private static final long serialVersionUID = 1L;

	private Boolean is_readonly;
	
	public class RefreshBehavior extends AbstractDefaultAjaxBehavior {
		@Override
		protected void respond(AjaxRequestTarget target) {
			ContentPage.this.refresh(target);
		}
		@Override
		public void renderHead(final Component component, final IHeaderResponse response) {
			super.renderHead(component, response);
			StringBuilder script = new StringBuilder();
			script.append("function refresh() {\n");
			script.append(getCallbackScript());
			script.append("}\n");
			response.render(JavaScriptHeaderItem.forScript(script.toString(), "refresh"));
		}
	}

	
	/**
	 *
	 * 
	 * 
	 * 
	 */
	public ContentPage() {
		add(new RefreshBehavior());
		setMenu(new InvisiblePanel("menu"));
		setLogVisit(true);
	}
	
	public ContentPage(IModel<T> model) {
		this(model, false);
	}
	
	
	public ContentPage(IModel<T> model, boolean readOnly) {
		super(model);
		setLogVisit(true);
		add(new RefreshBehavior());
		setReadOnly(readOnly);
		getPageParameters().set("oid", model.getObject().getOId().toString());
		if (!model.getObject().isHeadVersion()) {
			getPageParameters().set("ver", "v"+String.valueOf(model.getObject().getVersion()));
			getPageParameters().set("id", model.getObject().getId().toString());
		}
	}
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.CONTENT;
	}
	
	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);
	}
	
	public void setReadOnly(boolean b) {
		this.is_readonly = Boolean.valueOf(b);
	}
	
	public boolean isReadOnly() {
		
		if (this.is_readonly != null)
			return this.is_readonly.booleanValue();

//		El acceso al content estara determinado por las reglas de seguridad y no por el o los cabinets donde
//		este incluido.
		
//		Si la página es abierta desde una libreria de solo lectura recibira un parametro que lo indique y se
//		restringira el accesos por esa razon y no por las propiedades del content.

		
		this.is_readonly = Boolean.valueOf(false);
		
		return this.is_readonly;
	}

	protected void refresh(AjaxRequestTarget target) {
	}

	protected void onNavigate() {
		NavigationPanel<?> navigation = (NavigationPanel<?>)getPage().get("navigation");
		if (navigation!=null) 
			navigation.navigate();
	}

	@SuppressWarnings("unchecked")
	protected T getContent(PageParameters parameters) {
		T content = null;		
		Class<T> contentclass = (Class<T>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];		
		StringValue oid = parameters.get("oid");
		if (!oid.isNull() && !oid.isEmpty()) { 
			StringValue id = parameters.get("id");
			if (id.isNull() || id.isEmpty()) { 
				content = (T)getContentDao().findContentByOId(Long.valueOf(oid.toString()));
			}
			else {
				content = (T)getContentDao().findContentById(contentclass, id);
			}
		}	
		return content;
	}

	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	

	@Override
	protected String getName() {
		return "content";
	}
	
	
	/**
	 * Detalle
	 * Content ID
	 * Content OId
	 * 
	 * 
	 * 
	 */
	@Override
	protected String getPageType() {
		return "det";
	}
	
	@Override
	protected String getContentTitle() {
		return getModel().getObject().getTitle(); 
	}
	
	@Override
	protected String getContentId() {
		return new ContentId(getModel().getObject()).toString();
	}
	
	@Override
	protected Serializable getContentOId() {return getModel().getObject().getOId();}
	
	@Override				
	protected Serializable getCId() {return getModel().getObject().getId();}
	
	@Override
	protected Integer getContentVersion() {return Integer.valueOf(getModel().getObject().getVersion());}
	
	
	@Override
	protected String getStatsPageTitle() {
		
		return "det-"+getModel().getObject().getContentTemplate().getContentClassCode();
	}


}
