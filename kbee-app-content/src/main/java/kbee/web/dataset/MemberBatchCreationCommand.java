package kbee.web.dataset;

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

import com.novamens.beans.BeansService;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.service.DataSetService;
import com.novamens.content.service.ObjectFactoryService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.command.AbstractCommand;
import com.novamens.kbee.content.model.KbeeClassification;
import com.novamens.kbee.content.model.KbeeMemberClassification;
import com.novamens.kbee.content.model.KbeeValueMember;


import com.novamens.scheduler.SchedulerService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

/** 
 * 			
 * Comando para la Creación de DataSet Elements en forma batch.
 * Se llama desde la Consola de DataSet
 * 
 */
public class MemberBatchCreationCommand extends AbstractCommand   {
			
	static private Logger logger = LogManager.getLogger(MemberBatchCreationCommand.class.getName());
	
//	private static final long serialVersionUID = 1L;

	private Domain domain;
	private Serializable dataset_id;
	private Serializable user_id;
	private String elements;

	private int total_elements = -1;

	private DataSet dataset = null;

	
	private Serializable datasetmember_builtin_id;
	private DataSetMember datasetmember_builtin = null;
	
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


	public MemberBatchCreationCommand() {
	}
	
	public MemberBatchCreationCommand(Serializable dataset_id, Serializable domain_id, Serializable user_id,  String elements) {
		this(dataset_id, domain_id, user_id,elements,null);
	}
	
	
	public MemberBatchCreationCommand(Serializable dataset_id, Serializable domain_id, Serializable user_id,  String elements, Serializable dm_bin) {
		setName("DataSet Member Batch Creation");
		setPriority(SchedulerService.HIGH_PRIORITY);
		setDataSetId(dataset_id);
		setElementsStr(elements);
		setDomainId(domain_id);
		setUserId(user_id);
		setDataSetMemberBuiltInId(dm_bin);
	}


	@Override
	public String getDescription() {
		return "Command for the Creation of DataSet Elements in batch form. It is called from the DataSet Console.";
	}


	private void setElementsStr(String elements2) {
			elements=elements2;
	}


	private void setDataSetId(Serializable dataset_id2) {
			dataset_id=dataset_id2;		
	}

	
	private void setDataSetMemberBuiltInId(Serializable  dm_bin) {
		datasetmember_builtin_id=dm_bin;
	}

	private String getElementsStr() {
		return elements;
	}

	private Serializable getDataSetId() {
		return dataset_id;		
	}

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
		
		String arr[] = getElementsStr().trim().split("\\r|\\n");
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
	
	
	
	private void addDataSetMember(String  element) {
		try {
			if (getContentDao().findMemberByValue(getDataSet(), element)!=null) {
					getExisting().add(element);
				} else {
					Object member = ServiceLocator.getService(ObjectFactoryService.class).createMemberNoTx(getDataSet());
					if (member instanceof KbeeValueMember){
						((KbeeValueMember)member).setValue(element);
						getContentDao().save((KbeeValueMember)member);
					}
					getMembers().add(element);	
					logger.info(element);
					counter++;
				}
				
		} catch ( Exception e) {

			getErrors().add(element);
			errCount++;
			logger.error(e);
		}
		finally {
			total++;
			setProgress(xsize>0?(int) 100*total/xsize:100);	
		}
	}
	

	List<DataSetMember> agg_values = null;
	private List<DataSetMember> getAggregatedValues() {
		
		if (agg_values!=null) 
			return agg_values;
		
		agg_values = getDataSet().getService(DataSetService.class).getAggregatedValues(getDataSetMemberBuiltIn());
		return agg_values;  
	}

	/**
	 * @param element
	 */
	private void addDataSetMemberBuiltIn(String  element) {
		try {
			
			boolean exists = false;
			
			List<DataSetMember> list =  getAggregatedValues(); // getDataSet().getService(DataSetService.class).getAggregatedValues( getDataSetMemberBuiltIn().getDataSet(), getDataSetMemberBuiltIn());
			
			// all the _Units of a Property 
			if (list!=null) {
				for (DataSetMember dm: list) {
					if (dm.getName()!=null && dm.getName().trim().toLowerCase().equals(element.toLowerCase().trim())) {
						exists=true;
						break;
					}
				}
			}
				
			if (!exists) {
				
				DataSetMember member = (DataSetMember) ServiceLocator.getService(ObjectFactoryService.class).createMemberNoTx(getDataSet());
				
				Classifier cl= getDataSet().getService(DataSetService.class).getClassifier(getDataSetMemberBuiltIn().getDataSet());
				
				member.addClassification( new KbeeMemberClassification(cl, getDataSetMemberBuiltIn(), member));
				
				if (member instanceof KbeeValueMember){
						((KbeeValueMember)member).setValue(element);
						logger.debug(member.getStrValue() + " | " + member.getId().toString());
						getContentDao().save((KbeeValueMember)member);
					}
					getMembers().add(element);	
					logger.debug(element);
					counter++;
			}
			else {
				logger.debug("exists ->" + element);
				getExisting().add(element);
			}
			
		} catch (Exception e) {

			getErrors().add(element);
			errCount++;
			logger.error(e);
		}
		finally {
			total++;
			setProgress(xsize>0?(int) 100*total/xsize:100);	
		}
	}

	
	
	
	int counter  = 0;
	int errCount = 0;
	int total    = 0;
	int xsize     = 0;
	
	/**
	 * 
	 * 
	 */
	@Override
	public void execute() {
		
		counter  = 0;
		errCount = 0;
		total    = 0;
		xsize 	 = 0;

		agg_values = null;
		
		try {

				ServiceLocator.getService(SecurityService.class).authenticate("root@" + getDomain().getName());
			
				setDateStarted(OffsetDateTime.now());

				if ( getDataSetId()==null || getUserId()==null || getElementsStr()==null || getDataSet()==null ) {
					stop();
					setResult("ERROR");
					setResultComments("DataSet, User and/or Elements is null");
					setState(CommandState.ERROR);
					return;
				}

				
				if (getDataSet().getState()==ObjectState.DELETED || !(getDataSet().getDataSetType()==DataSetType.STRING || getDataSet().getDataSetType()==DataSetType.ENTITY)) {
					setProgress(100);
					setResult("ERROR");
					setResultComments("DataSet is Deleted or noy ValueType=String");
					setState(CommandState.ERROR);
					return;
				}
		
				Map<String, String> map = getMap(); 
				
				this.xsize = map.size();
						
				try {
					
					
						
					
						for(Entry<String, String> entry: map.entrySet()) {

							if (!(isStopped())) {

								String element = entry.getValue().trim();

								if (errCount>100)
									 break;
								
								try {	
									if (this.getDataSet().isAggregation())
										addDataSetMemberBuiltIn(element);	
									else
										addDataSetMember(element);
								}  catch (Exception e) {
									logger.error(e);
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
					logger.error(e);
				}
			}
			finally {
				
				if (getState()==CommandState.RUNNING)
					setState(CommandState.ERROR);
				
				setDateTerminated(OffsetDateTime.now());
				
				StringBuilder str_comments = new StringBuilder();
				str_comments.append("Total in list      : " + String.valueOf(getTotalElements()) +"<br/>");
				str_comments.append("Total processed    : " + String.valueOf(total) +"<br/>");
				str_comments.append("Total Created      : " + String.valueOf(counter) +"<br/>");

				
				str_comments.append("Duplicates in list : " + String.valueOf(getDuplicates().size()) +"<br/>");
				if (getDuplicates().size()>0) {
					for (String s: getDuplicates()) {
						str_comments.append(s+"<br/>");	
					}
				}


				str_comments.append("Existing           : " + String.valueOf(getExisting().size()) +"<br/>");
				if (getExisting().size()>0) {
					for (String s: getExisting()) {
						str_comments.append(s+"<br/>");	
					}
				}

				str_comments.append("Errors             : " + String.valueOf(errCount) +"<br/>"); 
				if (getErrors().size()>0) {
					for (String s: getErrors()) {
						str_comments.append(s+"<br/>");	
					}
				}

				setResultDetails(str_comments.toString());
				setResultComments("Created              : " + String.valueOf(counter));

				logger.info(String.valueOf(this.getDuration()) + " milisecs.");
			}
	}
	
	private DataSet getDataSet() {
		
		if (this.dataset!=null)
			return this.dataset;
		
		for( DataSet ds: getContentDao().getDataSets(getDomain())) {
			if (ds.getId().toString().equals(getDataSetId().toString())) {
				this.dataset =  ds;
				break;
			}
		}
		return this.dataset;
	}


	private DataSetMember getDataSetMemberBuiltIn() {
		
		if (this.datasetmember_builtin !=null)
			return this.datasetmember_builtin;
		
		if (datasetmember_builtin_id==null)
			return null;
		
		this.datasetmember_builtin = (DataSetMember) getContentDao().findModelObjectById(DataSetMember.class, datasetmember_builtin_id);

		return this.datasetmember_builtin;
		
	}



	public void setDomain(Domain domain) {
		this.domain = domain;
		setDomainId(domain.getId());
	}
	
	public Domain getDomain() {
		if (this.domain == null) {
			if ( getDomainId() == null) {
				if (this.user_id==null)
					this.domain = ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain();
			}	
			else
				this.domain = getContentDao().findDomainById(getDomainId());
		}
		return this.domain;
	}

	public Serializable getUserId() {
		return this.user_id;
	}
	
	public void setUserId(Serializable id) {
		this.user_id=id;
	}

	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
