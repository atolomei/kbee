package com.novamens.kbee.content.command;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.SessionFactory;

import com.novamens.beans.BeansService;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.service.SecurityContentMgmtService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.scheduler.SchedulerService;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

public class ResetSupportUsersValuesCommand extends AbstractCommand implements Runnable {

	private Serializable domainId = null;

	private transient Domain domain = null;

	@SuppressWarnings("unused")
	private SessionFactory sf;

	private Thread thread;
	@SuppressWarnings("unused")
	private boolean running;

	private int errors = 0;
	private int pwd_changed = 0;
	private int processed = 0;
	private int total = 0;

	private String password;

	public ResetSupportUsersValuesCommand(String pwd) {
		setName("Reset Support Users Values " + String.valueOf(getId()));
		this.password = pwd;
		setPriority(SchedulerService.HIGH_PRIORITY);
	}

	@Override
	public void execute() {
		this.thread = new Thread(this);
		this.thread.setDaemon(false);
		this.thread.setName(getName());
		this.thread.setPriority(Thread.NORM_PRIORITY);
		this.thread.start();
	}

	@Override
	public void run() {
		setState(CommandState.RUNNING);
		executeTask();
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
		domainId = domain.getId();
	}

	public Domain getDomain() {
		if (this.domain == null) {
			if (this.domainId == null)
				this.domain = ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain();
			else
				this.domain = getContentDao().findDomainById(domainId);
		}
		return this.domain;
	}

	private void executeTask() {
		debug("Starting Command execution " + getName());

		setDateStarted(OffsetDateTime.now());
		setProgress(0);

		this.errors = 0;
		this.pwd_changed = 0;
		this.processed = 0;
		this.total = 0;

		if (this.password == null) {
			setResult("Password can not be null. Please add parameter Password=password");
			setState(CommandState.ERROR);
			error("Password is null.");
			this.setDateTerminated(OffsetDateTime.now());
			return;
		}

		try {

			this.sf = com.novamens.hibernate.session.Session.open();

			List<User> list = getContentDao().findSupportAllUsers();

			total = list.size();

			ServiceLocator.getService(SecurityService.class).authenticate("root@kbee");

			info("Starting to process " + String.valueOf(total));

			List<String> part = new ArrayList<String>();
			part.add("Password Change (root@kbee Command)");

			for (User user : list) {

				if (this.isStopped()) {
					break;
				}

				try {
					((com.novamens.kbee.security.KbeeUser) user).setPassword(this.password);
					ServiceLocator.getService(SecurityContentMgmtService.class).update(user, part);
					this.pwd_changed++;

				} catch (Exception e) {
					errors++;
					error(e.getClass().getName() + " | " + Thread.currentThread().getStackTrace()[1].getMethodName());

				} finally {
					processed++;
					setProgress((int) (100 * processed / total));
					info("progress: " + getProgress());
				}
			}

			setDateTerminated(OffsetDateTime.now());

			StringBuilder str = new StringBuilder();
			str.append("Total " + String.valueOf(total));
			str.append(". Processed " + String.valueOf(processed));
			str.append(". Pwd Changed " + String.valueOf(pwd_changed));
			str.append(". Errors " + String.valueOf(errors));

			setResultComments(str.toString());

			if (!isStopped()) {
				setProgress(100);
				setResult("OK");
				setState(CommandState.COMPLETED);
			} else {
				setResult("Cancelled by User.");
				setState(CommandState.CANCELED);
			}

			debug("Ending Command execution " + getName());
		} finally {
			com.novamens.hibernate.session.Session.close();
			setStatusInfo("DB Session closed.");
		}
	}

	protected void setRunning(boolean value) {
		this.running = value;
	}

	private ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
