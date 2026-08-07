package com.novamens.content.web.security.markup;



import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
 
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Transient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.wicket.model.IModel;


import com.novamens.beans.BeansService;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;

import com.novamens.content.model.DataSet;

import com.novamens.content.model.UserSet;
import com.novamens.content.model.UserSubset;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.command.AbstractCommand;
import com.novamens.scheduler.SchedulerService;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

public class UsersBatchCreationCommand extends AbstractCommand {

	static private Logger logger = LogManager.getLogger(UsersBatchCreationCommand.class.getName());

	private Domain domain;
	private Serializable user_id;
	private String elements;

	private int total_elements = -1;


	/** ----------------------------------------------------------------------------------------			
	 */
	public UsersBatchCreationCommand() {
	}
	
	/** ----------------------------------------------------------------------------------------			
	 */
	public UsersBatchCreationCommand(Serializable domain_id, Serializable user_id,  String elements) {
		setName("User Member Batch Creation");
		setPriority(SchedulerService.HIGH_PRIORITY);
		setElementsStr(elements);
		setDomainId(domain_id);
		setUserId(user_id);
	}
	
	/** ----------------------------------------------------------------------------------------			
	 */
	@Override
	public String getDescription() {
		return "Comando para la Creación de Usuarios en forma batch. Se llama desde la Consola de Usuarios.";
	}

	/** ----------------------------------------------------------------------------------------			
	 */
	private void setElementsStr(String elements2) {
			elements=elements2;
	}
	
	/** ----------------------------------------------------------------------------------------			
	 */
	private String getElementsStr() {
		return elements;
	}

	
	@Transient
	Map<String, String> map_elements;
	
	@Transient
	List<String> members;

	@Transient
	List<String> duplicates;
	
	@Transient
	List<String> existing;
	
	@Transient
	List<String> errors;


	private int getTotalElements() {
		if (total_elements<0)
			getMap();
		return total_elements;
	}
	
	private List<String> getErrors() {
		if (errors!=null)
			return errors;
		getMap();
		return errors;
	}

	
	private List<String> getMembers() {
		if (members!=null)
			return members;
		getMap();
		return members;
	}
	
	private List<String> getExisting() {
		if (existing!=null)
			return existing;
		getMap();
		return existing;
	}

	private List<String> getDuplicates() {
		if (duplicates!=null)
			return duplicates;
		getMap();
		return duplicates;
	}

	
	private Map<String, String> getMap() {
		
		map_elements = new HashMap<String, String>();
		duplicates  = new ArrayList<String>();
		existing 	= new ArrayList<String>();
		members 	= new ArrayList<String>();
		errors	 	= new ArrayList<String>();
		
		String arr[] = getElementsStr().split("\\r|\\n");
		for(String st: arr) {
			if (!(st==null || st.trim().length()==0)) {
				if(!map_elements.containsKey(st.toLowerCase().trim())) {
					map_elements.put(st.toLowerCase().trim(), st);
				} else {
					duplicates.add(st);
				}
			}
		}
		total_elements = arr.length;
				
		return map_elements;
	}
	
	/** ------------------------------------------------------------------------------------------
	 * 
	 */
	@Override
	public void execute() {
		
		int counter  = 0;
		int errCount = 0;
		int total    = 0;

		try {

				ServiceLocator.getService(SecurityService.class).authenticate("root@" + getDomain().getName());
			
				setDateStarted(OffsetDateTime.now());

				if (getUserId()==null || getElementsStr()==null) {
					stop();
					setResult("ERROR");
					setResultComments("User and/or Elements is null");
					setState(CommandState.ERROR);
					return;
				}

				UserSet userset = getUserSet();
				IModel<UserSet> model = new ObjectModel<UserSet>(userset);
				setDataSetModel(model);
				
				Map<String, String> map = getMap(); 
				
				int size = map.size();
						
				try {
					
					for(Entry<String, String> entry: map.entrySet()) {

							if (!(isStopped())) {

								String element = entry.getValue().trim();

								if (errCount>100)
									 break;

								try {
										
									
									String[]  item = parseItem(element);
									
									if (item!=null) {
										
										if (!exists(item)) {
											
											//
											//
											// User user = ServiceLocator.getService(SecurityService.class).findUserById(getUserId());
											//
											//
											// try {
												//
												//
												// Object user = ServiceLocator.getService(com.novamens.content.service.SecurityService.class).createUser(getDataSet(), item[0], item[1], item[2]);
												//
												//
												// getContentDao().save(member);
												// logger.info(new ModelEvent(member, "update"));
											//	
											//}
											//catch (ContentCreationException e) {
											//	throw new KbeeRuntimeException(e);
											// }
											//
											//
											
											getMembers().add(element);	
											logger.info(element);
											counter++;
											
										}
										else {
											getExisting().add(element);
										}											
									}
									else {
										getErrors().add(element);
										errCount++;
									}
									
									
									
											
											
									//	} else {
											
											//User user = ServiceLocator.getService(SecurityService.class).findUserById(getUserId());
											
											
											//KbeeValueMember member = (KbeeValueMember) getDataSet().createMember();
											//member.setLastModifiedUser(user);
											//member.setLastModifiedDate(new Date());
											//member.setDataSet(getDataSet());
											//member.setValue(element);
											
										
										
								} catch (Exception e) {

									getErrors().add(element);
									errCount++;
									logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
									
								}
								finally {
									total++;
									setProgress(size>0?(int) 100*total/size:100);	
								}
							}
							else
								break;
						}
						
						if (isStopped()) {
							setResult("Stopped");
						}
						else if (errCount>20)
								setResult("Error");
						else {
							setProgress(100);
							setResult("OK");
						 }
						 
						setState(CommandState.COMPLETED);
						
		
				}
				catch (RuntimeException e) {
					logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
				}
			}
			finally {
				
				if (getState()==CommandState.RUNNING)
					setState(CommandState.ERROR);
				
				setDateTerminated(OffsetDateTime.now());
				
				StringBuilder str_comments = new StringBuilder();
				str_comments.append("Total in list: " + String.valueOf(getTotalElements()) +"<br/>");
				str_comments.append("Total processed: " + String.valueOf(total) +"<br/>");
				str_comments.append("Total Created: " + String.valueOf(counter) +"<br/>");

				
				str_comments.append("Duplicates in list: " + String.valueOf(getDuplicates().size()) +"<br/>");
				if (getDuplicates().size()>0) {
					for (String s: getDuplicates()) {
						str_comments.append(s+"<br/>");	
					}
				}


				str_comments.append("Existing: " + String.valueOf(getExisting().size()) +"<br/>");
				if (getExisting().size()>0) {
					for (String s: getExisting()) {
						str_comments.append(s+"<br/>");	
					}
				}

				str_comments.append("Errors: " + String.valueOf(errCount) +"<br/>"); 
				if (getErrors().size()>0) {
					for (String s: getErrors()) {
						str_comments.append(s+"<br/>");	
					}
				}

				setResultDetails(str_comments.toString());
				setResultComments("Created: " + String.valueOf(counter));

				logger.info(String.valueOf(this.getDuration()) + " milisecs.");
			}
    }
	

	
	private boolean exists(String[] item) {
	
		String name = item[0];
		String lastname  = item[1];
		String email = item[2];
		
		double estimate = getContentDao().findPersonEstimate(lastname, name, email, getDomain()); 
		
		if (estimate>0.9)
			return true;
		
		return false;
	}


	public void setDomain(Domain domain) {
		this.domain = domain;
		setDomainId(domain.getId());
	}
	
	public Domain getDomain() {
		if (domain == null) {
			if ( getDomainId() == null) {
				if (user_id==null)
					domain = ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain();
				else
					domain=getContentDao().findUserProfileByUser(getUser()).getDomain();
			}	
			else
				domain = getContentDao().findDomainById(getDomainId());
		}
		return domain;
	}

	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	
	
	public User getUser() {
		User user = null;
		if (getUserId()!=null) {
			
		}
		return user;
	}
	
	public Serializable getUserId() {
		return this.user_id;
	}
	
	public void setUserId(Serializable id) {
		this.user_id=id;
	}
	
	private String [] parseItem(String str) {
		if (str==null)
			return null;
		String [] arr = str.split(",");
		
		if (arr.length<3)
			return null;
		
		return arr;
	}
	
	
	private IModel<UserSet> datasetmodel;

	
	public void setDataSetModel(IModel<UserSet> model) {
		this.datasetmodel = model;
	}
	
	public IModel<UserSet> getDataSetModel() {
		return datasetmodel;
	}
	
	private UserSet getUserSet() {
		UserSet userset= null;
		for (DataSet dataset : getDataSets()) {
			if (dataset instanceof UserSet && !(dataset instanceof UserSubset)) {
				userset = (UserSet)dataset;
				break;
			}
		}
		return userset;
	}


	@SuppressWarnings("unused")
	private UserSet getDataSet() {
		return this.datasetmodel.getObject();
	}

	
	private List<DataSet> getDataSets() {
		return getContentDao().getDataSets(ServiceLocator.getService(UserService.class).getDomain());
	}


}
