package com.novamens.kbee.content.command;

import java.io.File;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import com.novamens.content.command.Command;
import com.novamens.content.command.CommandLifecycleCallback;
import com.novamens.content.command.CommandParameter;
import com.novamens.content.command.CommandState;
import com.novamens.content.resource.KBFile;
import com.novamens.dom.Domain;
import com.novamens.kbee.command.CommandService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;

public class CommandProxy implements Command, Serializable {
	
	
	private static final long serialVersionUID = 1L;
	
	private Serializable id;
	private Command object;
	private boolean detached = false;
	
	public CommandProxy(Command command) {
		this.object=command;
		this.id=command.getId();
	}

	private CommandService getCommandService() {
		return ServiceLocator.getService(CommandService.class);
	}
	
	
	
	public Command getObject() {
		if (detached) {
			object= getCommandService().getCommand((Long) this.id);
			detached=false;
		}
		return this.object;
	}
	
	
	@Override
	public String getDisplayName() {
		return getObject().getDisplayName();
	}

	@Override
	public Domain getDomain() {
		return getObject().getDomain();
	}

	@Override
	public void setDomain(Domain domain) {
			getObject().setDomain(domain);
		
	}

	@Override
	public Serializable getId() {
		return id;
	}

	@Override
	public String getName() {
		return  getObject().getName();
	}

	@Override
	public String getTitle() {
		return getObject().getTitle();
	}

	@Override
	public void setDescription(String des) {
		
	}

	@Override
	public String getDescription() {
		return getObject().getDescription();
	}

	@Override
	public String getHelp() {
		return getObject().getHelp();
	}

	@Override
	public void execute() {
		getObject().execute();
	}

	@Override
	public int getScope() {
		return getObject().getScope();
	}

	@Override
	public Object getParameter(String name) {
		return getObject().getParameter(name);

	}

	@Override
	public void setParameter(String name, Object value) {
		getObject().setParameter(name, value);
	}

	@Override
	public CommandState getState() {
		return getObject().getState();
	}

	@Override
	public void setState(CommandState state) {
		throw new KbeeRuntimeException ("can not setState a Proxy");
	}

	@Override
	public double getProgress() {
		return getObject().getProgress();
	}

	@Override
	public long getDuration() {
		return getObject().getDuration();
	}

	@Override
	public double estimatedSecsToEnd() {
		return getObject().estimatedSecsToEnd();
	}

	@Override
	public String getStatusInfo() {
		return getObject().getStatusInfo();
	}

	@Override
	public boolean isTerminated() {
		return getObject().isTerminated();
	}

	@Override
	public String getResult() {
		return getObject().getResult();
	}

	@Override
	public String getResultComment() {
		return getObject().getResultComment();
	}

	@Override
	public OffsetDateTime getDateCreated() {
		return getObject().getDateCreated();
	}

	@Override
	public OffsetDateTime getDateStarted() {
		return getObject().getDateStarted();
	}

	@Override
	public OffsetDateTime getDateTerminated() {
		return getObject().getDateTerminated();

	}

	@Override
	public String getResultDetails() {
		return getObject().getResultDetails();
	}

	@Override
	public void setResultDetails(String rsd) {
		getObject().setResultDetails(rsd);;

	}

	@Override
	public KBFile getResultFile() {
		return getObject().getResultFile();
	}

	@Override
	public File getLogFile() {
		return getObject().getLogFile();
	}

	@Override
	public void stop() {
		getObject().stop();
		
	}

	@Override
	public boolean isStopped() {
		return getObject().isStopped();
	}

	@Override
	public void pause() {
		throw new KbeeRuntimeException ("can not  a Proxy");
		
	}

	@Override
	public boolean isPaused() {
		return getObject().isPaused();
				
	}

	@Override
	public void resume() {
		getObject().resume();
		
	}

	@Override
	public int getPriority() {
		return getObject().getPriority();
	}

	@Override
	public void setPriority(int p) {
		 getObject().setPriority(p);
		
	}

	@Override
	public Serializable getDomainId() {
		return getObject().getDomainId();

	}

	@Override
	public void setDomainId(Serializable did) {
		getObject().setDomainId(did);
		
	}

	@Override
	public List<String> getMetadataAsList() {
		return getObject().getMetadataAsList();
	}

	@Override
	public Serializable getUserId() {
		return getObject().getUserId();

	}

	@Override
	public void setUserId(Serializable did) {
		getObject().setUserId(did);
		
	}

	@Override
	public void setParameters(Map<String, Object> map) {
		getObject().setParameters(map);
	}

	@Override
	public Map<String, Object> getParameters() {
		return getObject().getParameters();
	}

	@Override
	public void end() {
		getObject().end();
	}

	@Override
	public long getTotalItemsProcessed() {
		return getObject().getTotalItemsProcessed();
	}

	@Override
	public long getTotalItems() {
		return getObject().getTotalItems();

	}

	@Override
	public String getKey() {
		return getObject().getKey();

	}

	@Override
	public int getThreads() {
		return getObject().getThreads();
	}

	@Override
	public boolean isExactlyOneSemantics() {
		return getObject().isExactlyOneSemantics();

	}

	@Override
	public void setExactlyOneSemantics(boolean exactlyOneSemantics) {
		getObject().setExactlyOneSemantics(exactlyOneSemantics);
		
	}

	@Override
	public void setRequiresExplicitTrx(boolean b) {
		getObject().setRequiresExplicitTrx(b);
		
	}

	@Override
	public boolean isRequiresExplicitTrx() {
		return getObject().isRequiresExplicitTrx();

	}

	@Override
	public void addCallback(CommandLifecycleCallback commandLifecycleCallback) {
		getObject().addCallback(commandLifecycleCallback);
	}

	@Override
	public String getConcurrentUniqueKey() {
		return getObject().getConcurrentUniqueKey();

	}

	@Override
	public String getStringParameter(String name, String defaultValue) {
		return  getObject().getStringParameter(name, defaultValue);
	}

	@Override
	public List<CommandParameter> getParametersDefinition() {
		// TODO Auto-generated method stub
		return getObject().getParametersDefinition();
	}	
}
