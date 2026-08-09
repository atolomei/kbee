package kbee.web.report;


import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.novamens.dom.Domain;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.OffsetDateTimeField;
import com.novamens.wicket.model.ObjectModel;

public class DateRangeReportSidePanel extends ReportBaseParameterPanel {
																								
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DateRangeReportSidePanel.class.getName());
	
	private static final long serialVersionUID = 1L;
	
	private IModel<Domain> domain_model;

	final boolean role_support = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	final boolean role_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());

	private boolean showFromDatePicker = true;
	
	
	public DateRangeReportSidePanel(String id, String reportKey) {
		super(id, reportKey);
		OffsetDateTime now = OffsetDateTime.now();
		OffsetDateTime fst=now.minusDays(now.getDayOfMonth()-1);
		
		setOffsetDateTimeFrom(fst);
		setOffsetDateTimeTo(now);
		domain_model = new ObjectModel<Domain>(getDomain());
		setOutputMarkupId(true);
	}
	
	
	public DateRangeReportSidePanel(String id, String reportKey, OffsetDateTime from, OffsetDateTime to) {
			super(id, reportKey);
			OffsetDateTime now = OffsetDateTime.now();
			OffsetDateTime fst=now.minusDays(0);
			setOffsetDateTimeFrom(fst);
			setOffsetDateTimeTo(now);
			domain_model = new ObjectModel<Domain>(getDomain());
			setOutputMarkupId(true);
	}


	boolean domainSelector  = false;
	
	public void setDomainSelector(boolean  b) {
		this.domainSelector=b;
	}
	
	public boolean isDomainSelector() {
		return this.domainSelector;
	}
	public void onBeforeRender() {
		super.onBeforeRender();

		if (get("close")==null) {

			com.novamens.kbee.wicket.markup.html.ajax.WorkingIndicatorAjaxLinkV5<Void> close = new com.novamens.kbee.wicket.markup.html.ajax.WorkingIndicatorAjaxLinkV5<Void>("close") {
				private static final long serialVersionUID = 1L;
				@Override
				public void onClick(AjaxRequestTarget target) {
					onClose(target);
				}
			};
			add(close);

			Form<?> form = new Form<Void>("form");
																
			form.add(new ChoiceField<IModel<Domain>>("domain", new PropertyModel<IModel<Domain>>(this, "domainModel"), new PropertyModel<List<IModel<Domain>>>(this, "domains")) {
				private static final long serialVersionUID = 1L;

				@Override
				public boolean isVisible() {
					return isDomainSelector()  && isDomainKbee();
				}
				@Override
				public String getIdValue(IModel<Domain> value) {
					return String.valueOf(value.getObject().getId().toString());
				}

				@Override
				public String getDisplayValue(IModel<Domain> value) {
					return value.getObject().getOrganization(); //+ " ( "+ (value.getObject().getName())+")";
				}

				@Override
				public void onUpdate(AjaxRequestTarget target) {
					DateRangeReportSidePanel.this.setDomainModel(getValue());
					getParameters().put("domain", getValue().getObject().getId().toString());
					onChange(target, getParameters());
				}
			});
			
			
			
			form.add(new OffsetDateTimeField("from",  (getDomain().getTimeZone()!=null? ZoneId.of(getDomain().getTimeZone()) : ZoneId.systemDefault()), new PropertyModel<OffsetDateTime>(this, "from")) {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public boolean isVisible() {
					return isShowFromDatePicker();
				}

				public void onUpdate(AjaxRequestTarget target) {
					getParameters().put("from", getValue());
				}
			});

			form.add(new OffsetDateTimeField("to", ZoneId.of(getDomain().getTimeZone()), new PropertyModel<OffsetDateTime>(this, "to")) {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				public void onUpdate(AjaxRequestTarget target) {
					getParameters().put("to", getValue());
				}
			});
		
			form.add(new AjaxSubmitLink("submit") {
				private static final long serialVersionUID = 1L;
				@Override
				protected void onSubmit(AjaxRequestTarget target) {
					onChange(target, getParameters());
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
							return null;
						}
						@Override
						public CharSequence getBeforeSendHandler(Component component) {
							return null;
						}
						@Override
						public CharSequence getBeforeHandler(Component component) {
							return "document.getElementById('"+component.getMarkupId()+"').innerHTML = '<span class=\"" + com.novamens.wicket.markup.html.form.Form.SPINNING + " fa-fw\"></span> "+getLoading() +"'";
							
							
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
			});
			add(form);			
		}
		
		 
	}


	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
	}
	
	public IModel<Domain> getDomainModel() {
		return this.domain_model;
	}
	
	public void setDomainModel(IModel<Domain> d) {
		this.domain_model=d;
	}


	public boolean isShowFromDatePicker() {
		return showFromDatePicker;
	}

	public void setShowFromDatePicker(boolean showFromDatePicker) {
		this.showFromDatePicker = showFromDatePicker;
	}

	@Override
	public void setConsoleName(String consoleName) {
		super.setConsoleName(consoleName);
	}
	
	

	@Override
	public void onDetach() {
		super.onDetach();
		if (domain_model!=null)
			domain_model.detach();
 
		if (domains!=null) {
			for (IModel<Domain> m: domains)
				m.detach();
		}
	}

	protected void onChange(AjaxRequestTarget target, Map<String, Object> parameters) {
	}

			
	private List<IModel<Domain>> domains;
	
	public List<IModel<Domain>> getDomains() {

		if (domains != null)
			return domains;

		domains = new ArrayList<IModel<Domain>>();


		for (Domain domain : getContentDao().getDomains()) {
					domains.add(new ObjectModel<Domain>(domain));
			}
		
		Collections.sort(domains, new Comparator<IModel<Domain>>() {
			@Override
			public int compare(IModel<Domain> c1, IModel<Domain> c2) {
				try {
					if (c1.getObject().getOrganization() != null && c2.getObject().getOrganization() != null) {
						return c1.getObject().getOrganization().compareToIgnoreCase(c2.getObject().getOrganization());
					}
					return (c1.getObject().getOrganization() != null ? -1 : 0);
				} catch (Exception e) {
					logger.error(e);
					return 0;
				}
			}
		});
		return domains;
	}

	

}
