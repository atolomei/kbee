package com.novamens.kbee.content.webapi.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.novamens.content.base.Content;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EIdentifiableForm;
import com.novamens.content.service.TokenService;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.datetime.DateTimeService;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.content.workflow.KbeeTask;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import kbee.api.model.ApiProcedure;
import kbee.api.model.ApiProxy;
import kbee.api.model.ApiWorkflowContext;
import kbee.api.model.IFieldData;
import kbee.api.model.IFormData;
import kbee.api.model.IPendingTask;
import kbee.api.model.ITask;
import kbee.util.logging.Logger;

public class IPendingTaskAdapter implements Adapter<Content, IPendingTask> {
	
	private static Logger logger = Logger.getLogger(IPendingTaskAdapter.class.getName());

	public IPendingTaskAdapter() {
	}
	
	public IPendingTask adapt(Content content) {
		
		IPendingTask workitem = new IPendingTask();
		
		KbeeContext context = (KbeeContext)content.getService(WorkflowService.class).getContext();
		KbeeTask task = (KbeeTask)context.getTask();

		workitem.setForms(getForms(content, task));
		workitem.setId(String.valueOf(content.getId()));
		
		String timestring = ServiceLocator.getService(DateTimeService.class).timeElapsed(content.getLastModifiedOffsetDateTime());
		workitem.setTime(timestring);
		
		ApiProcedure iprocedure = (new IProcedureAdapter()).adapt(context.getProcess().getProcedure());
		Optional<ITask> itask = iprocedure.getTasks().stream().filter(t -> t.getId().equals(task.getId())).findFirst();
		
		workitem.setDisplayName(getTitle(content));
		workitem.setTask(itask.get());
		workitem.setDomain(content.getDomain().getName());
		workitem.setContext(getContext(context));
		
		return workitem;	
	}
	
	private ApiWorkflowContext getContext(KbeeContext context) {
		ApiWorkflowContext icontext = new ApiWorkflowContext();
		if (!context.getParameters().isEmpty()) {
			for (String key : context.getParameters().keySet()) {
				icontext.setParameter(key, context.getParameter(key));
			}
		}
		return icontext;
	}
	
	private List<IFormData> getForms(Content content, KbeeTask task) {
		List<IFormData> forms = new ArrayList<IFormData>();
		try {
			for (EForm form : task.getForms()) {
				IFormData idata = new IFormData();
				EFormData data = content.getFormData(form);
				if (form.getViewer()!=null)
				idata.setUrl(getUrl(content, form));
				ApiProxy iform = new ApiProxy();
				iform.setId(String.valueOf(((EIdentifiableForm)form).getId()));
				iform.setName(form.getDisplayName());
				idata.setForm(iform);
				idata.setSigned(data.isSigned());
				idata.setFile(new ApiProxy(String.valueOf(content.getId()), content.getTitle(), UriHelper.getUri(content), "content"));
				idata.setData(getData(data));
				forms.add(idata);
			}
		}
		catch (Exception e) {
			logger.error(e);
		}
		return forms;
	}
	
	private String getUrl(Content content, EForm form) {
		KbeeJson data = new KbeeJson();
		
		data.put("content", String.valueOf(content.getId()));
		data.put("form", String.valueOf(((EIdentifiableForm)form).getId()));
		data.put("user", getSessionUser().getName());
		
		return "/sharedform/" + ServiceLocator.getService(TokenService.class).getToken(data);
	}
	
	private List<IFieldData> getData(EFormData formdata) {
		List<IFieldData> data = (new IFormDataAdapter()).adapt(formdata);
		return data;
	}
	
	private String getTitle(Content content) {
		String title = content.getTitle();
		if (title==null) return "";
		title = title.replace("\r", "");
		title = title.replace("\n", "");
		return title;
	}
	
	private User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
}