package kbee.web.workflow.task;

import java.io.Serializable;

import org.springframework.beans.factory.BeanNameAware;

import com.novamens.beans.BeansService;
import com.novamens.kbee.content.workflow.TaskPageFactory;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.Task;
import com.novamens.workflow.WorkflowContext;

import kbee.util.logging.Logger;

public class TaskPageFactoryBean implements TaskPageFactory, BeanNameAware, Serializable {
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(TaskPageFactoryBean.class.getName());
	
	private String factoryName, factoryDisplayName, taskPageBean;
	
	@Override
	public String getDisplayName() {
		return factoryDisplayName;
	}
	
	public void setDisplayName(String label) {
		this.factoryDisplayName = label;
	}
	
	@Override
	public String getName() {
		return factoryName;
	}
	
	@Override
	public void setBeanName(String name) {
		this.factoryName = name;
	}
	
	public String getBean() {
		return taskPageBean;
	}

	public TaskPage<?> getPage(Task task, WorkflowContext context) {
		TaskPage<?> page = (TaskPage<?>) ServiceLocator.getService(BeansService.class).getBean(getBean(), context, false);
		logger.debug("TaskPage -> " + page!=null?page.getClass().getName():"null");
		return page;
	}
}