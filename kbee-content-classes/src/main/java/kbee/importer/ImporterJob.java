package kbee.importer;


import com.novamens.beans.BeansService;
import com.novamens.dom.Domain;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.user.UserService;
import com.novamens.kbee.command.CommandService;
import com.novamens.scheduler.AbstractCronJobRequest;
import com.novamens.scheduler.SchedulerService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
	
@Deprecated
public class ImporterJob extends AbstractCronJobRequest {
			
	private static final long serialVersionUID = 8493968328948344749L;
	
	 public ImporterJob() {
		setName("Importer Job");
	 }
	
	public void execute() {
		
		ContentDao contentdao = getContentDao();
		
		ServiceLocator.getService(SecurityService.class).authenticate(contentdao.findSystemParameterByKey("local_user_importer").getValue());
		
		CommandService service = ServiceLocator.getService(CommandService.class);
		
		ImporterCommand command = new ImporterCommand();
		
		command.setParameter("groups", "false");
		command.setParameter("templates", "false");
		command.setParameter("datasets", "false");
		command.setParameter("classifiers", "false");
		command.setParameter("values", "false");
		command.setParameter("rules", "false");
		command.setParameter("users", "false");
		command.setParameter("files", "true");
		command.setParameter("criteria", contentdao.findSystemParameterByKey("criteria_importer").getValue());
		command.setParameter("server", contentdao.findSystemParameterByKey("remote_server_importer").getValue());
		command.setParameter("user", contentdao.findSystemParameterByKey("remote_user_importer").getValue());
		command.setParameter("password", contentdao.findSystemParameterByKey("remote_password_importer").getValue());
		command.setParameter("domain", getDomain().getId());
		
		command.setPriority(SchedulerService.HIGH_PRIORITY);
		
		service.add(command);
	}
	

	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	private Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
}
