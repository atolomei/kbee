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
import com.novamens.kbee.content.document.KbeeIDoc;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.content.workflow.KbeeTask;
import com.novamens.kbee.content.workflow.KbeeTaskForm;
import com.novamens.kbee.content.workflow.KbeeWorkflowActivity;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.Activity;

import kbee.api.model.ApiActivity;
import kbee.api.model.ApiFile;
import kbee.api.model.ApiProcedure;
import kbee.api.model.ApiProcess;
import kbee.api.model.ApiProxy;
import kbee.api.model.ApiViewMode;
import kbee.api.model.ApiWorkflowContext;
import kbee.api.model.IFieldData;
import kbee.api.model.IFormData;
import kbee.api.model.INote;
import kbee.api.model.ISignedData;
import kbee.api.model.ITask;
import kbee.util.logging.Logger;

public class ApiActivityAdapter implements Adapter<Activity, ApiActivity> {
	
	private static Logger logger = Logger.getLogger(ApiActivityAdapter.class.getName());
	
	private ApiViewMode viewMode;

	public ApiActivityAdapter() {
		this.viewMode = ApiViewMode.All;
	}
	
	public ApiActivityAdapter(ApiViewMode viewMode) {
		this.viewMode = viewMode;
	}
	
	public ApiActivity adapt(Activity activity) {
		ApiActivity iactivity = new ApiActivity();
		
		iactivity.setId(String.valueOf(activity.getId()));
		
		if (ApiViewMode.All.equals(viewMode)) {
			iactivity.setForms(getForms(activity));
		}
		
		String timestring = ServiceLocator.getService(DateTimeService.class).timeElapsed(activity.getStartTime());
		iactivity.setTime(timestring);
		iactivity.setStartTime(activity.getStartTime());
		
		ApiProcess iprocess = (new IProcessAdapter()).adapt(activity.getProcess());
		iactivity.setProcess(iprocess);
		
		String taskname = ((KbeeWorkflowActivity)activity).getTaskName();
		ApiProcedure iprocedure = (new IProcedureAdapter(viewMode))
			.adapt(activity.getProcess().getProcedure());
		Optional<ITask> itask = iprocedure
			.getTasks()
			.stream()
			.filter(t -> t.getId().equals(taskname)).findFirst();
		iactivity.setTask(itask.get());
		
		Content content = ((KbeeWorkflowActivity)activity).getContent();
		
		iactivity.setDisplayName(getTitle(content));
		
		iactivity.setDomain(content.getDomain().getName());
		iactivity.setStatus(activity.getStatus().name());
		iactivity.setContext(getContext(activity));
		iactivity.setUser(new ApiUserProxy(activity.getUser()));
		
		if (activity.isRunning()) {
			KbeeContext context = (KbeeContext)content.getService(WorkflowService.class).getContext();
			Activity previous = context.getPreviousActivity();
			if (previous!=null && previous.getNote()!=null) {
				INote note = new INote();
				note.setText(previous.getNote());
				note.setTime(previous.getEndTime());
				note.setAuthor(new ApiUserProxy(previous.getUser()));
				iactivity.setNote(note);
			}
		}
		
		return iactivity;	
	}
	
	private ApiWorkflowContext getContext(Activity activity) {
		ApiWorkflowContext icontext = new ApiWorkflowContext();
		KbeeContext context = (KbeeContext)(((KbeeWorkflowActivity)activity).getContent())
			.getService(WorkflowService.class)
			.getContext();
		ApiFile file = (new IDocAdapter("1", viewMode, false, false, false))
			.adapt((KbeeIDoc)context.getContent());
		icontext.setFile(file);	
		if (!context.getParameters().isEmpty()) {
			for (String key : context.getParameters().keySet()) {
				icontext.setParameter(key, context.getParameter(key));
			}
		}
		return icontext;
	}
	
	private List<IFormData> getForms(Activity activity) {
		List<IFormData> forms = new ArrayList<IFormData>();
		try {
			KbeeTask task = (KbeeTask)activity.getTask();
			Content content = ((KbeeWorkflowActivity)activity).getContent();
			for (EForm form : task.getForms()) {
				IFormData idata = new IFormData();
				EFormData data = content.getFormData(form);
				if (form.isVisible(data)) {
					if (form.getViewer()!=null)
					idata.setUrl(getUrl(content, form));
					ApiProxy iform = new ApiProxy();
					iform.setId(String.valueOf(((EIdentifiableForm)form).getId()));
					iform.setName(form.getDisplayName());
					idata.setForm(iform);
					idata.setFile(new ApiProxy(String.valueOf(content.getId()), content.getTitle(), UriHelper.getUri(content), "content"));
					idata.setSigned(data.isSigned());
					if (data.isSigned() && !data.getSignatures().isEmpty()) {
						ISignedData isigneddata = new ISignedData();
						isigneddata.setUser(new ApiUserProxy(data.getSignatures().get(0).getSignature().getUser()));
						isigneddata.setTime(data.getSignatures().get(0).getDate());
						idata.setSignedData(isigneddata);
					}
					idata.setLayout(((KbeeTaskForm)form).getFormLayout().name());;
					idata.setFileContainer(form.isFileContainer());
					idata.setData(getData(data));
					forms.add(idata);
				}
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