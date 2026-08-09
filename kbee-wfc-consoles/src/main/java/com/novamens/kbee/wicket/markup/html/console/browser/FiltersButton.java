package com.novamens.kbee.wicket.markup.html.console.browser;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.kbee.wicket.markup.html.console.panel.FiltersPanel;
import com.novamens.kbee.wicket.markup.html.console.panel.ParametersPanel;
import com.novamens.kbee.wicket.util.InvisiblePanel;

import kbee.web.console.BaseBrowser;

@SuppressWarnings("serial")
public class FiltersButton extends ToolbarItem {
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private static Logger logger = LogManager.getLogger(FiltersButton.class.getName());

	ParametersPanel ppanel;
	
	public FiltersButton(BaseBrowser<?> browser, Align align) {
		super(browser, align);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		super.setOutputMarkupId(true);
		addLink();
	}
	
	protected void addLink() {
		
		AjaxLink<Void> lnk = new AjaxLink<Void>("link") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				getBrowser().togglePanel(FiltersPanel.class);
				if ( (ppanel!=null) && (ppanel instanceof ParametersPanel)) {
					ppanel.setVisible(false);
					ppanel=null;
				}
				target.add(getBrowser());
				fireScanAll(new SidePanelEvent(target));
			}
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				IAjaxCallListener listener = new IAjaxCallListener() {
					@Override
					public CharSequence getSuccessHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getPrecondition(Component component) {
						return null;
					}
					@Override
					public CharSequence getFailureHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getCompleteHandler(Component component) {
						String s = null, s1=null;
						String id = component.getMarkupId();
						s1 = "document.getElementById('"+id+"').innerHTML = '"+"<i class=\"far fa-filter\"></i>"+"';";
						s ="setTimeout(function () {"+s1+"}, 350);";
						return s;
					}
					@Override
					public CharSequence getBeforeSendHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getBeforeHandler(Component component) {
						String s = null;
						String id = component.getMarkupId();
						s = "document.getElementById('"+id+"').innerHTML = '<i class=\""+com.novamens.wicket.markup.html.form.Form.SPINNING + " fa-fw \" ></i>";
						s +="';";
						return s;
					}
					@Override
					public CharSequence getAfterHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getDoneHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getInitHandler(Component component) {
						return null;
					}
				};
				attributes.getAjaxCallListeners().add(listener);
			}
		};

		
		add(lnk);
		
		
		AjaxLink<Void> pp=new AjaxLink<Void>("filters-link") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				if (ppanel==null) {
					ppanel = new ParametersPanel(getBrowser().getQuery().getParameters(), getBrowser().getConsoleKey()) {
						private static final long serialVersionUID = 1L;
						@Override
						public void onBeforeRender() {
							super.onBeforeRender();
							setParameters(FiltersButton.this.getBrowser().getQuery().getParameters());
						}
						
						@Override
						public void onUpdate(AjaxRequestTarget target) {
							FiltersButton.this.getBrowser().getQuery().setParameters(getParameters());
							FiltersButton.this.getBrowser().refresh(target);
						}
						
						@Override
						public boolean isSaveQuerySupported() {
							return false;
						}
					};
					FiltersButton.this.addOrReplace(ppanel);
				}
				else {
					ppanel.setVisible(!FiltersButton.this.get("parameters").isVisible());	
				}
				target.add(FiltersButton.this);
			}
		};
			
		add(pp);
		add(new InvisiblePanel("parameters"));
		
		Label fa = new Label("label", getLabel()) {
			@Override
			public boolean isVisible() {
				if (getBrowser().getPanel(FiltersPanel.class)==null || getBrowser().getPanel(FiltersPanel.class).isVisible())
					return false;
				Map<String, Object> map = ((FiltersPanel) getBrowser().getPanel(FiltersPanel.class)).getParameters();
				if (map!=null && (map.get("text")!=null || map.get("iql")!=null))
						return true;
				return ((FiltersPanel) getBrowser().getPanel(FiltersPanel.class)).isFiltersApplied();
			}
		};
		
		if (getLabelCss()!=null)
			fa.add(new AttributeModifier("class", getLabelCss()));
		
		pp.add(fa);
		
	}
	
	
	protected String getLabelCss() {
		return "highlight";

	}
	
	protected boolean isVisibleLabel() {
		if (getBrowser().getPanel(FiltersPanel.class).isVisible())
			return false;
		Map<String, Object> map = ((FiltersPanel) getBrowser().getPanel(FiltersPanel.class)).getParameters();
		if (map!=null && (map.get("text")!=null || map.get("iql")!=null))
				return true;
		return ((FiltersPanel) getBrowser().getPanel(FiltersPanel.class)).isFiltersApplied();
	}
	
	protected IModel<String> getLabel() {
		return new Model<String>() {
		private static final long serialVersionUID = 1L;
		@Override
		public String getObject() {
			return new StringResourceModel("filters-applied", FiltersButton.this, null).getObject();
		}};

	}
	
}
