package com.novamens.kbee.command;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.novamens.content.command.AbstractCommandLifecycleCallback;
import com.novamens.scheduler.Dispatcher;

import org.reflections.Reflections;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.content.command.Command;
import com.novamens.content.command.CommandState;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.kbee.event.EvictCacheServiceEvent;
import com.novamens.metrics.SystemMetricsService;
import com.novamens.scheduler.SchedulerException;
import com.novamens.scheduler.SchedulerService;
import com.novamens.scheduler.ServiceRequest;
import com.novamens.service.ServiceLocator;

import org.springframework.util.Assert;

/**
  * 
  * addSchedulerCommand [ SchedulerCommand (allows to execute commands non serializable, async.)
  * <p>
  * AsyncCommand -> it uses the pool trhread to run them. no need for the Scheduler
  * </p>	
  * . SchedulerCommand
  * . AsyncCommand -> pool de threads
  * <p>
  *  . Command Maps are not passed by the scheduler, nor do they run in a thread of this service.
  *  . They run in a third party thread. *
  * </p>
  * <p> Commands are saved in the Map {@link Command} when sent by a client.
  * When the {@link Command} is executed, <strong> itself </strong> registers itself in the {@code Map},
  * which is updated as it runs. </p>
  *
  * <p> The client can remove a command from the {@code Map} when it stops using it.
  * normally this should happen after completion. </p>
  *
  * <p> Completed commands that are not removed -> are removed in the periodic <strong> {@link cleanup} </strong> process.
  * Registered commands that the {@link Scheduler} never executes -> are removed in the {@link cleanup} process. </p>
  *  
  */

public class KbeeCommandService implements CommandService, ApplicationListener<ApplicationEvent>, EventListener {

    static private final long TTL = 60 * 60 * 24 * 5; // 5 days  (in seconds)
    static private final int MAX_SIZE = 32000;


    private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeCommandService.class.getName());
	
	private List<Class<? extends Command>> command_classes;
	
	
    private static Set<String> runningSet = new HashSet<>();
    private Dispatcher dispatcher;
    private int max_size = MAX_SIZE;
    private SystemMetricsService metrics;
    private OffsetDateTime last_clean_up;
    
    /**
     * TODO HA
     * 
     * These structures must be ported to a distributed cache (Hazelcat, EHcache, ...) 
     * for the distributed version 
     * 
    **/
    private Map<Serializable, Command> commands = new ConcurrentHashMap<Serializable, Command>(16, 0.9f, 1);

    /** Instance of CommandService is accessed from multiple Threads */
    private ReadWriteLock com_lock = new ReentrantReadWriteLock();
    private List<Command> commands_terminated = Collections.synchronizedList(new ArrayList<Command>());

    
    
    /**
     * <p>Adds a {@link Command}, to do so it wraps it into a {@link ServiceRequest} and sends it to the {@link Scheduler}.
     * When the Request is taken by one of the Scheduler's Dispatcher Workers
     * the Request will retrieve the command from this Service to start executing it.
     * </p>
     * <p>IMPORTANT:<b>ServiceRequest</b> must be <b>Serializable</b>, while <b>Command</b> does not need to.</p>
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void add(Command command) {

    	register(command);
    	CommandWrapperServiceRequest request = new CommandWrapperServiceRequest(command);
        request.setPriority(command.getPriority());

        try {
            ServiceLocator.getService(SchedulerService.class).enqueue(request);
        } catch (SchedulerException e) {
            logger.error(e);
            command.setState(CommandState.ERROR);
        } finally {
            if (getCommands().size() > this.max_size)
                cleanUp();
        }
    }

    
    /**
     * <p>Runs an AsyncCommand (on its own Thread) 
     */

    @Override
    public void run(Command command) {
    	
        final String concurrentUniqueKey = command.getConcurrentUniqueKey();

        Assert.isInstanceOf(Runnable.class, command);
        Runnable runnable = (Runnable) command;
        boolean exists = false;

        if (concurrentUniqueKey != null) {
            synchronized (runningSet) {
                exists = !runningSet.add(concurrentUniqueKey);
            }
            if (!exists) {

                command.addCallback(new AbstractCommandLifecycleCallback() {
                    @Override
                    public void stop() {
                        unregister();
                    }

                    @Override
                    public void end() {
                        unregister();
                    }

                    private void unregister() {
                        runningSet.remove(concurrentUniqueKey);
                    }
                });
            }
        }
        if (!exists) {
            												
            getSystemMetricsService().getMeterCommandsStartExecution().mark();
        	dispatcher.execute(runnable);
            
        } else {
        	logger.error("Command with key '" + concurrentUniqueKey + "' is already being executed.");
            throw new ConcurrentModificationException("Command with key '" + concurrentUniqueKey + "' is already being executed.");
        }
    }


    
    
	@Override
    public void registerAndRun(Command command) {
    	register(command);
    	run(command);
    }
    
    /**
     * The {@link ServiceRequest}
     * is responsible to register the {@link Command} (taken from the Scheduler to start execution)
     *
     * @param command
     */
    public void register(Command command) {
        try {
            this.com_lock.writeLock().lock();
            getCommands().put(command.getId(), command);
            getSystemMetricsService().getMeterCommandsIn().mark();
        } finally {
            this.com_lock.writeLock().unlock();
        }
    }

    /**
     * Client has finished using the Command
     *
     * @param commandId
     */
    public void remove(Serializable id) {
        try {
            this.com_lock.writeLock().lock();
            getCommands().remove(id);
            getSystemMetricsService().getMeterCommandsTerminated().mark();
        } finally {
            this.com_lock.writeLock().unlock();
        }
    }

    /**
     * The Command has finished its execution
     *
     * @param commandId
     */
    public void executed(Command command) {

        try {
            this.com_lock.writeLock().lock();
            this.commands_terminated.add(command);
            getSystemMetricsService().getMeterCommandsTerminated().mark();
            
            if (getCommands().size() >= this.max_size) {
            	if (commands_terminated.size()>0)
            		getCommands().remove((Long) commands_terminated.get(0).getId());
            }
        } finally {
            this.com_lock.writeLock().unlock();
        }
    }


    /**
     * 
     *  @param commandId : command id
     * 
     *   returns progress 0.0% to 100.0%
     * 
     *   see {@link Command} 
     */
    @Override
    public double getProgress(Serializable commandId) {
        try {
            this.com_lock.readLock().lock();
            if (getCommands().containsKey(commandId))
                return getCommands().get(commandId).getProgress();
            return 0;
        } finally {
            this.com_lock.readLock().unlock();
        }
    }


    @Override
    public Command getCommand(Serializable id) {
        try {
            this.com_lock.readLock().lock();
            if (getCommands().containsKey(id))
                return getCommands().get(id);
            return null;
        } finally {
            this.com_lock.readLock().unlock();
        }
    }

    @Override
    public List<Command> getCommands(CommandState state) {
        List<Command> list = new ArrayList<Command>();
        try {
            this.com_lock.readLock().lock();
            for (Entry<Serializable, Command> entry : this.commands.entrySet()) {
                Command com = entry.getValue();
                if (com.getState() == state)
                    list.add(com);
            }
            return list;
        } finally {
            this.com_lock.readLock().unlock();
        }
    }

    @Override
    public List<Command> getCommandsAsList(Serializable domain_id) {
        List<Command> list = new ArrayList<Command>();
        try {
            this.com_lock.readLock().lock();
            for (Entry<Serializable, Command> entry : getCommands().entrySet()) {
                Command com = entry.getValue();
                if (com != null) {
                    if (domain_id == null) {
                        list.add(com);
                    } else {
                        if ((com.getDomainId() != null) && (com.getDomainId().equals(domain_id)))
                            list.add(com);
                    }
                }
            }
        } finally {
            this.com_lock.readLock().unlock();
        }
        return list;
    }

    @Override
    public synchronized void stopAll() {
        if (getCommands() == null)
            return;
        try {
            this.com_lock.readLock().lock();
            for (Entry<Serializable, Command> entry : getCommands().entrySet()) {
                Command cmd = entry.getValue();
                logger.debug("stop " + cmd.getId() + " " + cmd.getName());
                cmd.stop();
            }
        } finally {
            this.com_lock.readLock().unlock();
        }
    }

    /**
     * Clean Cache
     */
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        logger.debug("Evict Cache Received");
        this.max_size = MAX_SIZE;
        cleanUp(true);
    }

    @Override
    public boolean listen(Event event) {
        if (event instanceof EvictCacheServiceEvent)
            return true;
        return false;
    }

    @Override
    public void onEvent(Event event) {
        logger.debug("Evict Cache Received");
        this.max_size = MAX_SIZE;
        this.command_classes = null;
        cleanUp(true);
    }
    
    

    
    public OffsetDateTime getDateLastCleanUp() {
    	return last_clean_up;
    }


    @Override
    public Map<Serializable, Command> getCommands() {
        return this.commands;
    }

    @Override
    public List<Command> getCommandsAsList() {
        return getCommandsAsList(null);
    }

    public Dispatcher getDispatcher() {
        return dispatcher;
    }

    public void setDispatcher(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }
    
    @Override
    public int getTotalCommands() {
    	return commands.size();
    }
    
    
    @Override
    public int getTotalTerminatedCommands() {
    	return commands_terminated.size();
    }
    
    


	@Override
	public boolean contains(Serializable commandId) {
		return commands.containsKey(commandId);
	}
	
	
    public 	SystemMetricsService getSystemMetricsService() {

    	if (metrics!=null)
    		return metrics;
    	
    	synchronized (this) {
    			if (metrics==null)
    				metrics=ServiceLocator.getService(SystemMetricsService.class);
    		}
    	return metrics;
    }


    private  void cleanUp() {
		cleanUp(false);
    }

	/**
	 * Delete up to max_to_delete (MAX_SIZE / 2) of the finished Commands
	 */
	private synchronized void cleanUp(boolean bforce) {
	
	    if ((getCommands().size() < max_size) && !bforce) {
	    	return;
	    }
	
	    logger.debug("cleanUp process");
	
	    try {
	
	        this.com_lock.writeLock().lock();
	        this.commands_terminated.clear();
	
	        /**
	         Si Commands esta muy lleno, elimina todos que fueron creados
	         hace mucho tiempo, asumiendo que se perdieron en la ejecucion en el Scheduler
	         */
	        OffsetDateTime onow = OffsetDateTime.now();
	        this.last_clean_up = OffsetDateTime.now();
	
	        for (Entry<Serializable, Command> entry : getCommands().entrySet()) {
	            Command com = entry.getValue();
	            Duration duration = Duration.between(com.getDateCreated(), onow);
	
	            if (duration.getSeconds() > TTL)
	                com.setState(CommandState.UNKNOWN);
	
	            if (com.getState() == CommandState.COMPLETED ||
	                    com.getState() == CommandState.ERROR ||
	                    com.getState() == CommandState.CANCELED ||
	                    com.getState() == CommandState.UNKNOWN) {
	                this.commands_terminated.add(entry.getValue());
	            }
	        }
	
	        Iterator<Command> it = this.commands_terminated.iterator();
	        while (it.hasNext()) {
	            Command cmd = it.next();
	            getCommands().remove(cmd.getId());
	            logger.debug("remove " + cmd.getId() + " " + cmd.getName());
	        }
	        this.commands_terminated.clear();
	        this.max_size += (int) (MAX_SIZE * 1 / 4);
	    } finally {
	        this.com_lock.writeLock().unlock();
	    }
	}
	
	
	
	

	
	public List<Class<? extends Command>> getAllCommandClasses() {
		
		 if (command_classes!=null) 
			 return command_classes;
		 
		synchronized (this) {
		try {	
			command_classes = new ArrayList<Class<? extends Command>>();
			
			String name="";
			Reflections reflections = new Reflections(name);
			Set<Class<? extends Command>> classes = reflections.getSubTypesOf(Command.class);
			logger.debug(classes.toString());
			classes.forEach(item -> 
			{
				if (!Modifier.isAbstract( item.getModifiers()))
					command_classes.add(item);
			}
				);
	
			
			command_classes.sort(new Comparator<Class<? extends Command>>() {
				@Override
				public int compare(Class<? extends Command> o1, Class<? extends Command> o2) {
					return o1.getSimpleName().compareToIgnoreCase(o2.getSimpleName());
				}
				
			});
			} catch (Exception e) {
				logger.error(e);
			}
		}

		return command_classes;


	}
	
	
	
	

    
}


