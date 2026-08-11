package com.novamens.kbee.content.service.datamanagement;


import java.time.Duration;
import java.time.Instant;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceContainer;

import com.novamens.content.resource.KBFile;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.ResultSet;

/** ------------------------------------------------
 */
public class QuerySizeEstimator {

	static final double GB = 1024 * 1000 * 1000;
			
	static Logger logger = LogManager.getLogger(QuerySizeEstimator.class.getName());

	Instant start;
	Instant end;
	
	private boolean is_calcutated = false;
	
	Query query;
	long estimate 			= 0;  // estimated space in bytes
	
	
	int sample_size			= 0;
		
	int counter 			= 0;  // processed partial
	long  total_resources 	= 0; // total resources (including external)
	long  total_space 		= 0;  // total space used in bytes
	
	
	
	long  total_contents = 0; // total contents
	long  total_files 	 = 0; // total  files
	long  total_errors 	 = 0; // total  files with errors (typically non existent)

	
	
	double average_content_size_bytes; 
	double average_pdf_size_bytes;
	double estimate_total_size_gb;

	double total_pdfs_bytes;
	
	
	public QuerySizeEstimator(Query query) {
		this.query=query;
	}
	
	public long getTotalContents() {
		if (!is_calcutated)
			calculate();
		return total_contents;
	}
	
	public long getTotalResources() {
		if (!is_calcutated)
			calculate();
		return total_resources;
	}

	public long getTotalFiles() {
		if (!is_calcutated)
			calculate();
		return total_files;
	}

	public long getTotalErrors() {
		if (!is_calcutated)
			calculate();
		return total_errors;
	}

	public Duration getDuration() {
		if (!is_calcutated)
			calculate();
	  	return Duration.between(start,  end);
	}
	
	public long getCounter() {
		return counter;
	}
	

	public double getTotalSpace() {
		if (!is_calcutated)
			calculate();
		
		return this.estimate_total_size_gb;
		
	}

	
	public Query getQuery() {
		return this.query;
	}
	
	
	private void calculate() {

		int size = 0;

	try {

				boolean done = false;
				
				start = Instant.now();
						
				ResultSet results = getQuery().execute();
				
				size = results.size();
				
				
				if (size==0) {
					return;
				}
				
				counter         = 0;
				sample_size	    = 0;
				total_resources = 0;
				total_space     = 0;
				total_files 	= 0;  // total  files
				total_errors 	= 0;
				
				average_content_size_bytes=0; 
				average_pdf_size_bytes=0;
				estimate_total_size_gb=0.0;


				while (results.hasNext() && !done) {
				
					Content content = (Content) results.next().getObject();

					total_contents++;
					sample_size++;
						
					// logger.info(content.getTitle());
					
					if (content instanceof ResourceContainer) {
						for ( Resource res: ((ResourceContainer) content).getResources()) {
							if (res instanceof KBFile) {
								//try {
									((KBFile) res).getSize();
									total_space += ((KBFile) res).getSize();
										
									
								//} catch (IOException e) {
								//	logger.error(e.getClass().getSimpleName());
								//	total_errors++;
								//}
							}
							total_resources++;
						}
					}
					
					counter++;
	
					if (counter>=600)
						done=true;
					
					if (size>1200) 
						results.next();
				
					if (size>1800) 
						results.next();
					
				}
				
		}
	
		finally {

			this.is_calcutated = true;
			this.end = Instant.now();

			if (total_contents==size) {
				this.estimate_total_size_gb =  (double) total_space  / GB;
				return;
			}
				
			if (total_contents==0)
				return;

			this.estimate_total_size_gb =  (double) size * ((double) total_space / (double) total_contents) / GB;
		
		}
	}	
	
}
