package com.novamens.content.web.command.batch.classify;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.ModelObject;
import com.novamens.content.service.ContentService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.command.AbstractCommand;
import com.novamens.scheduler.SchedulerService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;

/** --------------------------------------------------------------------------------------------------------------------------
 * 
 * <p>Clasifica contenidos en forma batch. Para eso recorre toda la base, por lo que puede tomar mucho tiempo.  
 * .
 * <b>CondiciÓn<b>
 * <ul>
 * <li>Condition.ContentClass				: ContentTemplate sobre los que se aplica (mandatorio)</li>
 * <li>Condition.Classifier				: Nombre del Classifier (si es null, se aplica a todas las instancias del ContentClass)</li>
 * <li>Condition.DatasetMember			: Valor del DataSetMember que se reemplaza (si mandatorio si Classifier != null)</li>
 * </ul>
 *  
 * <b>Valores a fijar en los contenidos que cumplen la condiciÓn</b>
 * <ul> 
 * <li>update.contentclass: ComunicaciÓn</li>
 * <li>update.domainid:3</li>
 * <li>update.classifier:Repositorio</li>
 * <li>update.datasetmember: PÚblico</li>

 * <li>Update.Classifier				: Nombre del Classifier a actualizar</li>
 * <li>Update.DatasetMember			: instancia de DataSetMember a agregar (solo soporta 1)</li>
 * </ul>
 * 
 * <b>Filtros</b>
 *</ul>
 * <li>Filter.limit					: cantidad a convertir (incluyendo los que den error)</li>
 * <li>Filter.Version				: head/ all (yes: solo los mÁs que son head, sino todas  las versiones)</li>
 * <li>Filter.Overwrite				: yes/no (no: si el contenido ya tiene el classifier no lo cambia)</li>
 * </ul>
 *
 *  	
</p> 
<p>Ejemplo:
<br/>
Clasificaro todos los "Documento" con Repositorio=Publico, 
los que ya tienen algo en Repositorio no se tocan:
<br/>
Condition.ContentClass: Documento
Condition.Classifier: null
Condition.DatasetMember: null
<br/>
Update.Classifier: Repositorio
Update.DataSetMember: Público
<br/>
Filter.limit: 0
Filter.Overwrite: no
Filter.version: all
</p>
 * 
 */
public class ClassifyBatchCommand extends AbstractCommand implements Runnable  {

	static Logger logger = LogManager.getLogger(ClassifyBatchCommand.class.getName());

	static final int BATCH_SIZE = 4;
 
	private Thread thread;
	private boolean running;
	
	private Serializable domainId = null;
	private Domain domain = null;
	
	private int counter 			= 0; // Convertidos
	private int already_classified  = 0; // cantidad que ya estaban clasificados en el clasificador 
	private int err_count 			= 0; // Intento convertir y dio error
	private int total_elements 		= 0; // Listado total 
	private int limit 				= 0; // limite max. a convertir (exito + intento con error)

	private String update_contenttempate_name;
	private String update_classifier_name;
	private String update_dataset_member_name;

	private String  condition_classifier_name=null;
	private String  condition_datasetmember_name=null;

	private String overwrite;
	private boolean b_overwrite;
	
	private volatile DataSetMember dataset_member;
	private volatile Classifier classifier;

	private Classification condition_classification;
	
	private ContentTemplate contenttemplate = null;
		
	/**
	update.contentclass: ComunicaciÓn
	update.domainid:3
	update.classifier:Repositorio
	update.datasetmember: PÚblico
	filter.limit:10
	 */

	public ClassifyBatchCommand(Map<String, Object> map) {

		setName("Classify Batch Command");
		setPriority(SchedulerService.LOW_PRIORITY);

		// Map<String, String> map = new HashMap<String, String>();
		//
		// Condition
	    // Content Class de Condition es la misma que Update
		//
		// this.condition_classifier_name		= map.get("condition.classfier");
		// this.condition_datasetmember_name 	= map.get("condition.datasetmember");
		
		// Update
		//
		this.update_contenttempate_name		= (String) map.get("update.contentclass");
		this.update_classifier_name			= (String) map.get("update.classifier");
		this.update_dataset_member_name		= (String) map.get("update.datasetmember");
	
		
		// Update (TBR)
		//
		//this.update_contenttempate_name=contenttemplate_name;
		//this.update_classifier_name=classifier_name;
		//this.update_dataset_member_name=datasetmember_name;
		
		// Filters
		//
		if (map.containsKey("filter.overwrite"))
			this.overwrite = (String) map.get("filter.overwrtite");
		
		if ( map.containsKey("filter.limit")) {
			try {
				this.limit = Integer.valueOf((String) map.get("filter.limit")).intValue(); 
				} catch (RuntimeException e) {
					this.limit=0;
				}
		}
			
		if (map.containsKey("update.domainid"))
			this.domainId = ((String) map.get("update.domainid")).trim();

		if (this.overwrite!=null)
			b_overwrite= this.overwrite.trim().toLowerCase().equals("yes");
		else
			b_overwrite=false;
	}
	
	/** --------------------------------------------------------------------
	 */

	@Override
	public void execute() {

		this.thread = new Thread(this);
    	this.thread.setDaemon(false);
    	this.thread.setName(getName());
    	this.thread.setPriority(2);
    	this.thread.start();
	}

	/** --------------------------------------------------------------------
	 */

	public void setDomainId(Serializable id) {
		domainId = id;
	}

	/** --------------------------------------------------------------------
	 */

	public Serializable getDomainId() {
		return domainId;
	}

	/** --------------------------------------------------------------------
	 */

	public void setDomain(Domain domain) {
		this.domain = domain;
		domainId = domain.getId();
	}

	
	public Domain getDomain() {
		
		if (domain == null) {
			if (domainId == null) {
				domain = ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain();
			}	
			else {
				try {
					long did = (Long.valueOf(domainId.toString())).longValue();
					domain = getContentDao().findDomainById(did);
				} catch (Throwable e) {
					logger.error(e.getStackTrace());
				}
			}
		}
		return domain;
	}

	/** --------------------------------------------------------------------
	 */
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	/** --------------------------------------------------------------------
	 */
	@Override
	public void run() {
		executeTask();
	}

	/** --------------------------------------------------------------------
	 */

	public boolean isRunning() {
	    	return this.running;
	}

	/** --------------------------------------------------------------------
	 */

	protected void setRunning(boolean value) {
    	this.running = value;
	}

	/** --------------------------------------------------------------------
	 */

	private void finalize(CommandState state) {
		setResultComments("Processed " +String.valueOf(counter)+".  Errors: " +String.valueOf(this.err_count));
		setDateTerminated(OffsetDateTime.now());
		setState(state);
	}

	/** --------------------------------------------------------------------
	 */

	/**
	 * Error. cuando no se puede empezar a procesar.
	 * @param error_str
	 */
	private void error(String error_str) {
		setResultComments(error_str);
		setDateTerminated(OffsetDateTime.now());
		setState(CommandState.ERROR);
	}
	

	/** --------------------------------------------------------------------
	 * 
	 * Se debe hacer 1 trx
	 * 
	 * @param list
	 */
	
	private void processList(List<Content> list) {
	
		if (isStopped() || getClassifier().getId()==null)
			return;
		

		
		String cla_id= getClassifier().getId().toString();
		
		try {
		
			for (Content content: list) {
				
				
				// Verifica que cumpla la condicion 
				//
				//
				//
				// Si la cumple
				
				boolean already = false;
	
				Classification clasi_to_remove1 = null;
				Classification clasi_to_remove2 = null;
				Classification clasi_to_remove3 = null;
				
				for(Classification clasi: content.getClassification()) {
					
					// Si el Classifier ya existe en el Content already=true
					// NOTA: Solo saca hasta 3 classification del Classifier
					//
					if  (clasi.getClassifier().getId()!=null && clasi.getClassifier().getId().toString().equals(cla_id)) {
					
							already =true;
					
							this.already_classified++;
							
							// Si el flag "overwrite" es true, saca lo que hay
							//
							if (b_overwrite) {  
								if (clasi_to_remove1==null) 
									clasi_to_remove1 = clasi;
								else if (clasi_to_remove2==null)
									clasi_to_remove2 = clasi;
								else
									clasi_to_remove3= clasi;
							}
							
							break;
					}
				}
	
				
				// si no tiene el classifier o el flag overwrite esta en true
				//
				if (!already || b_overwrite) {
	
					if (clasi_to_remove1 !=null)
						content.removeClassification(clasi_to_remove1);
				
					if (clasi_to_remove2 !=null)
						content.removeClassification(clasi_to_remove2);
	
					if (clasi_to_remove3 !=null)
						content.removeClassification(clasi_to_remove3);
					
					DataSetMember member = getDataSetMember();
			
					if (member!=null)
						content.addClassification(getClassifier(), getDataSetMember());
					
					try {
						
						content.getService(ContentService.class).update();
						
						logger.info("add:" + content.getTitle());
						this.counter++;
					
						
					} catch (ServiceNotFoundException e) {
						logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
						if (e.getStackTrace()!=null)
						logger.error(e.getStackTrace());
	
						this.err_count++;
						
					} catch (Exception e) {
	
						this.err_count++;
						logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
						if (e.getStackTrace()!=null)
						logger.error(e.getStackTrace());
					}
	
				}
			}
		} catch (RuntimeException e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			if (e.getStackTrace()!=null)
				logger.error(e.getStackTrace());
		}
		
	}
	
	/** --------------------------------------------------------------------
	 */

	private Classifier getClassifier() {
		
		if (classifier!=null)
			return classifier;
		
		try {
			if (update_classifier_name==null)
				return null;
			String c1 = update_classifier_name.trim().toLowerCase();
			for(Classifier clasi: getContentDao().getClassifiers(getDomain())) {
				if (clasi.getName().trim().toLowerCase().equals(c1)) {
					classifier=clasi;
					return classifier;
				}
			}
			return null;
		}  catch (RuntimeException e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			return null;
		}
	}


	
	/** --------------------------------------------------------------------
	 */

	private Classification getConditionClassification() {
		
		if (condition_classification!=null)
			return condition_classification;
		
		try {
			if (this.condition_classifier_name==null || this.condition_datasetmember_name==null)
				return null;
			
			Classifier cond_cl=null;
			DataSetMember cond_dm;
			
			String c1 = this.condition_classifier_name.trim().toLowerCase();
			for(Classifier clasi: getContentDao().getClassifiers(getDomain())) {
				if (clasi.getName().trim().toLowerCase().equals(c1)) {
					cond_cl=clasi;
					break;
				}
			}
			
			if (cond_cl==null)
				return null;
			
			
			cond_dm = (DataSetMember) getContentDao().findModelObjectByName(DataSetMember.class, cond_cl.getDataSet(), this.condition_datasetmember_name);
			
			if (cond_dm==null)
				return null;
			
			// condition_classification=new KbeeClassification(cond_cl, cond_dm);
			
			return null;
			
			
		}  catch (RuntimeException e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			logger.error(e.getStackTrace());

			return null;
		}
	}

	
	
	
	
	
	
	
	
	
	
	/** --------------------------------------------------------------------
	 */
	private ContentTemplate getContentTemplate() {
		
		if (this.contenttemplate!=null)
			return contenttemplate;
		
		try {
			
			if (update_contenttempate_name==null)
				return null;
			
			this.contenttemplate = getContentDao().findContentTemplateByName(update_contenttempate_name, getDomain().getId());
			return contenttemplate;
			
		}  catch (RuntimeException e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			logger.error(e.getStackTrace());

			return null;
		}
	}
	
	
	
	/** --------------------------------------------------------------------
	 */
	
	private DataSetMember getDataSetMember() {

		if (dataset_member!=null)
			return dataset_member;
		
		try {
			
			if (update_dataset_member_name==null || getClassifier()==null)
				return null;
			
			String c1 = update_dataset_member_name.trim().toLowerCase();
			
			ModelObject m = getContentDao().findModelObjectByName(DataSetMember.class, getClassifier().getDataSet(), c1);
			
			if (m==null)
				return null;
			
			dataset_member = (DataSetMember) m;
			
			return dataset_member;
			
		} catch (RuntimeException e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());

			logger.error(e.getStackTrace());
			return null;
		}
	}

	
	
	/** --------------------------------------------------------------------
	 */
	protected void executeTask() {
		
		this.setRunning(true);
		
		setState(CommandState.RUNNING);
		setDateStarted(OffsetDateTime.now());
		setProgress(0);
		this.counter=0;
		this.already_classified=0;
		
		try {

			setStatusInfo("Opening DB Session.");

			com.novamens.hibernate.session.Session.open();

			ServiceLocator.getService(SecurityService.class).authenticate("root@" + getDomain().getName().trim());

			Domain domain = getDomain();
			
				if (domain==null) {
		 			error("Domain is null");
		 			return;
				}
				
				if (update_classifier_name==null) {
		 			error("update classifier_name is null");
		 			return;
				}
				
				if (update_dataset_member_name==null) {
		 			error("update_dataset_member_name is null");
		 			return;
				}
		

				if (this.update_contenttempate_name==null) {
		 			error("update_contenttemplate_name is null");
		 			return;
				}

				
				List<Content> list = new ArrayList<Content>();
				List<? extends Content> all = getContentDao().getContent(getContentTemplate(), getDomain());
		
				setStatusInfo("Starting scanning " + String.valueOf(all.size()) +  " elements.");

				this.total_elements = 0;
				
				if (all.size()>0) {
					
						double effective_size = limit>0 ? (double) limit: (double) all.size(); 
			
						int n = 0;
						int batch_number = 0;
						
						for (Object con: all) {

							if (isStopped())
								break;

							if (this.limit>0 && (list.size()+this.counter+this.err_count)>=limit)
								break;

							list.add((Content)con);
							n++;
							total_elements++;

							if (n==BATCH_SIZE) {
							  setStatusInfo("Processing batch " + String.valueOf(batch_number));
							  processList(list);	
							  list.clear();
							  n=0;
							  batch_number++;
							}
							
							if (limit==0) 
								setProgress( (int) ( 100.0 * (double) total_elements / (double) effective_size));
							else
								setProgress( (int) ( 100.0 * ((double) this.counter + (double)this.err_count) / (double) limit));
						}

						  if (isStopped()) {
							finalize(CommandState.CANCELED);
							return;
						  }

						// Si queda un batch sin completar. lo vacía.
						//
						if (list.size()>0) {
							  setStatusInfo("Processing batch " + String.valueOf(batch_number));
							  processList(list);	
							  list.clear();
							  n=0;
							  batch_number++;
								if (limit==0) 
									setProgress( (int) (100.0 * (double) total_elements / (double) effective_size));
								else
									setProgress( (int) ( 100.0 * ((double) this.counter + (double)this.err_count) / (double) limit));
						}
				}
				
				setStatusInfo("Terminating processing.");
				setProgress(100);
				setResult("OK");
				
				setResultDetails(
						"Max. limit to Convert: " + String.valueOf(this.limit) + 
						". Converted: " + String.valueOf(this.counter) + 
						". Errors: " + String.valueOf(this.err_count) +
						". Already Classified: " + String.valueOf(this.already_classified) + 
						". Total Scanned: " + String.valueOf(total_elements));
				setStatusInfo("done.");
				finalize(CommandState.COMPLETED);
		
		} catch (Throwable e) {

			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			
			if (e.getStackTrace()!=null)
				logger.error(e.getStackTrace());

			setResult(e.getClass().getName());
			
			
			if (e.getStackTrace()!=null) {
				setResultDetails(e.getMessage()+ ". " + e.getStackTrace());	
			}
			else if (e.getMessage()!=null)
				setResultDetails(e.getMessage());
			
			finalize(CommandState.ERROR);
		
	} finally {
				
		
		com.novamens.hibernate.session.Session.close();
		setStatusInfo("DB Session closed.");
		
	}
  }
}
