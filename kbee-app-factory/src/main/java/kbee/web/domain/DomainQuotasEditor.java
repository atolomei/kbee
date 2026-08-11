package kbee.web.domain;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.novamens.content.entity.Person;
import com.novamens.content.service.DomainService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;

import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;

import com.novamens.wicket.markup.html.form.NumberField;

public class DomainQuotasEditor extends ObjectEditor<Domain> {
	private static final long serialVersionUID = 1L;

	static Logger logger = LogManager.getLogger(DomainQuotasEditor.class.getName());

	private com.novamens.service.SecurityService service = null;
	
	public enum Quota {
		Q01GB  (1,   "1 GB"),
		Q05GB  (5,   "5 GB"),
		Q010GB (10, "10 GB"),
		Q020GB (20, "20 GB"),
		Q050GB (50, "50 GB"),
		Q100GB (100, "100 GB"),
		Q200GB (200, "200 GB"),
		Q500GB (500, "500 GB"),
		Q001TB (1000, "1 TB"),
		QUNLIMITED (-1, "Unlimited");
		
		private String label;
		private int quota;
		
		private  Quota(int quota, String label) {
			this.label = label;
			this.quota = quota;
		}
		
		public String toString() {
			return ("quota: " + getQuota() + "  label: "+ getLabel());
		}
		
		public String getLabel() {
			return label;
		}
		
		public int getQuota() {
			return quota;
		}
	}
	
	@SuppressWarnings("unused")
	public DomainQuotasEditor(String id, IModel<Domain> model) {
		super(id, model);

		final boolean IS_DOMAIN_KBEE = isDomainKbee();
		
		setOutputMarkupId(true);
		setEditionEnabled(false);
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		add(form);

		IModel<Quota> quotamodel = new IModel<Quota>() { 
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			public Quota getObject() {

				int quota = getModelObject().getQuota();
				
				if (quota<1)
					return Quota.QUNLIMITED;
				
				for (Quota q : getQuotas()) {
					if (quota <= q.getQuota())
						return q;
				}
				return Quota.QUNLIMITED;
			}
			
			public void setObject(Quota quota) {
				getModelObject().setQuota(quota.getQuota());
			}
			public void detach() {
			} 
		};
		
		form.add(new NumberField<Integer>("maxUsers"));
		form.add(new ChoiceField<Quota>("quota", quotamodel, new PropertyModel<List<Quota>>(this, "quotas")));
		
		form.add(new BooleanField("template"));
		
		add(new EditButtonsV5<Domain>(this));
		
		
		
	}
	
	@Override
	public void onDetach() {
		service=null;
		super.onDetach();
	}
	
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				
				logger.info(getModelObject().getQuota());
				logger.info(getModelObject().getMaxUsers());
				
				getModelObject().getService(DomainService.class).update(getUpdatedParts());
				// logger.info(new DomainUpdateEvent(getModelObject(), getUpdatedParts()));
				reset();
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<>(target, e));
		}
	}

	
	public List<Quota> getQuotas() {
		List<Quota> quotas = new ArrayList<Quota>();
		
		quotas.add(Quota.Q01GB);
		quotas.add(Quota.Q05GB);
		quotas.add(Quota.Q010GB);
		quotas.add(Quota.Q020GB);
		quotas.add(Quota.Q050GB);
		quotas.add(Quota.Q100GB);
		quotas.add(Quota.Q200GB);
		quotas.add(Quota.Q500GB);
		quotas.add(Quota.QUNLIMITED);
		return quotas;
	}

	
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	@SuppressWarnings("unused")
	private com.novamens.service.SecurityService getSecurityService() {
		if (service!=null)
			return service;
		
		service = ServiceLocator.getService(com.novamens.service.SecurityService.class);
		return service;
	}
	
	protected KbeeUser getUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	
	private boolean isDomainKbee() {
		try {
			return getPerson().getDomain().getName().toLowerCase().trim().equals("kbee");
		} 
		catch (Exception e) {
			return false;
		}
	}
	
	
}
