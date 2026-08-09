package kbee.web.workflow.task;

import java.io.Serializable;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.util.Assert;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.content.workflow.TaskPageFactory;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.Task;
import com.novamens.workflow.WorkflowContext;

import kbee.util.logging.Logger;

public class DefaultTaskPageFactory implements TaskPageFactory, BeanNameAware, Serializable {
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(DefaultTaskPageFactory.class.getName());
	
	private String beanName;
	
	public String getDisplayName() {
		return "Default";
	}
	
	@Override
	public void setBeanName(String name) {
		this.beanName = name;
	}
	
	@Override
	public String getName() {
		return beanName;
	}

	public TaskPage<?> getPage(Task task, WorkflowContext context) {
		String bean = getPageBean(task, context);
		TaskPage<?> page = (TaskPage<?>) ServiceLocator.getService(BeansService.class).getBean(bean, context, false);
		logger.debug("TaskPage -> " + page!=null?page.getClass().getName():"null");
		return page;
	}
	
	protected String getPageBean(Task task, WorkflowContext context) {
		String pagebean = getContentClass(((KbeeContext)context).getContent()) + "-taskpage-V2";
		
		//if (task instanceof WebTask) {
			//WebTask webtask = (WebTask)task;
			//if (eforms()) {
			//	pagebean += "-V2";
			//}
			//else
			//if (webtask.getVersion()!=null && !"".equals(webtask.getVersion())) {
			//	pagebean += "-" + webtask.getVersion();
			//}
		//}
		return pagebean;
	}
	
	protected String getContentClass(Content content) {
		Assert.isTrue(content!=null, "no content");
		String classname = content.getClass().getSimpleName().toLowerCase();
		int i = classname.indexOf("_");
		if (i>0) classname = classname.substring(0, i);
		return classname;
	}
	
	//private boolean eforms() {
	//	return ServiceLocator.getService(ApplicationServerService.class).isEforms();
	//}
}