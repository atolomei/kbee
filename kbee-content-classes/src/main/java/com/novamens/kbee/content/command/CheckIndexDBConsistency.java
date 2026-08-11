package com.novamens.kbee.content.command;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.novamens.beans.BeansService;
import com.novamens.content.command.CommandState;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.service.Index;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.query.SolrDateRangeFilter;
import com.novamens.solr.indexer.query.SolrParametersQuery;

public class CheckIndexDBConsistency extends AsyncCommand {
																													
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger("checkdb");		
	
	private NumberFormat integer_nf;
	private SessionFactory sf;

	private long db_total=0 ;
	private long solr_total=0;

	
	/**
	 * 
	 */
	private class DataItem {
		
		public String db_query;
		public Map<String, Object> solr_query = new HashMap<String, Object>();
		public String solr_query_str;
		public boolean is_valid;
	
		public OffsetDateTime from;
		public OffsetDateTime to;
		
		public long db_count;
		public long solr_count;
		
		
		public boolean isOK() {
			if (!is_valid)
					return true;
			return db_count==solr_count;
		}
		
		public String toString() {
			StringBuilder str=new StringBuilder();
			str.append("from:"+ from.toString() +"\n");
			str.append("to: " + to.toString()+"\n");
			str.append("db_query: "+db_query.toString()+"\n");
			str.append("solr_query:"+solr_query_str+" \n");
			str.append("is_valid: "+ (is_valid?"yes":"no")+"\n");
			str.append("db_count: "+ integer_nf.format(db_count)+"\n");
			str.append("solr_count: "+integer_nf.format(solr_count)+"\n");
			return str.toString();
		}
		
	}
	
	 public CheckIndexDBConsistency() {
		 setName(this.getClass().getSimpleName());
		 setDescription("Checks consitency between Hibernate and SolR by comparing iDoc modified in every month since 2010");
	 }
	 
	 boolean success = true;
	 
	@Override
	protected void executeAsync() {

		try  {
			
			com.novamens.hibernate.session.Session.open();
			ServiceLocator.getService(SecurityService.class).authenticate("root@kbee");
			
			initCID();
			runQ();
			
			for (DataItem i: list) {
				this.success=i.isOK();
				if (!this.success)
					break;
			}
			
			setProgress(100.0);
			
			this.db_total=0;
			this.solr_total=0;
			
			list.forEach( item -> {  
				if (item.is_valid) {
					this.db_total+=item.db_count;
					this.solr_total+=item.solr_count;
				};
			});

			/** print results on checkfb.log */
			list.forEach( item -> {logger.info(item.from.toString()+" - " + item.to.toString()+" - "+"Hib|Solr: " + String.format("%10s", this.integer_nf.format(item.db_count)) +" | " + String.format("%10s",this.integer_nf.format(item.solr_count)));});
			logger.info("Hib|SolR total: " +  this.integer_nf.format(db_total) + " | " +  this.integer_nf.format(solr_total));
			
			setState(CommandState.COMPLETED);
			
			StringBuilder str = new StringBuilder();
			
			if (this.success) {
				str.append("All checks OK");
				str.append("db_total: " +  this.integer_nf.format(db_total) + " - solr_total: " +  this.integer_nf.format(solr_total));
			}
			else
				list.forEach( item -> { if (!item.isOK()) { 
					str.append(item.from.toString()+" - " + item.to.toString()+" - Hibernate: " + integer_nf.format(item.db_count) +" - "+"SolR: " + integer_nf.format(item.solr_count)+"\n");
					}
			});

			setResultComments(str.toString());
			
			
		} catch (Exception e) {
			logger.error(e);
			setState(CommandState.ERROR);
			setResultComments(e.getClass().getSimpleName()+"| " + e.getMessage());
			
		} finally {

			logger.debug(success?"Success": "Failure");
			setResult(success?"Success": "Failure");
			
			setDateTerminated(OffsetDateTime.now());
			logger.debug("Duration: " + String.valueOf(getDuration()/1000)+" ms");
			
			com.novamens.hibernate.session.Session.close();
			setStatusInfo("DB Session closed.");
 		}
	}
	
	
	private void initCID() {
		
		BeansService beans = ServiceLocator.getService(BeansService.class);
		 sf = (SessionFactory)beans.getBean("sessionFactory");
		
		this.integer_nf = NumberFormat.getInstance(Locale.getDefault());
		this.integer_nf.setMinimumFractionDigits(0);
		this.integer_nf.setMaximumFractionDigits(0);
		this.integer_nf.setRoundingMode(RoundingMode.HALF_UP);

	}
	
	List<DataItem> list = new ArrayList<DataItem>();
	
	
	/**
	 * 
	 */
	private void runQ() {

		ZoneOffset utc_offset = ZoneOffset.of("Z");
								
		OffsetDateTime start_d 	= OffsetDateTime.of(2010, 1, 1, 00, 0, 0, 0, utc_offset);
		OffsetDateTime aux   	= OffsetDateTime.of(2010, 1, 1, 00, 0, 0, 0, utc_offset);
		OffsetDateTime end_d 	= OffsetDateTime.now().minusHours(1).truncatedTo(ChronoUnit.MILLIS);
		
		list.clear();
		
		Duration dr=Duration.between(start_d, end_d);

		int n = 0;
		
		while (start_d.isBefore(end_d) && n++<1000) {
																										
			setProgress( 100.0 * Double.valueOf(Duration.between(start_d, aux).toHours()).doubleValue() / Double.valueOf((dr.toHours()+1.0)).doubleValue());
			
			start_d=start_d.truncatedTo(ChronoUnit.MILLIS);
			aux = start_d.plusMonths(1).truncatedTo(ChronoUnit.MILLIS);
			
			if (aux.isAfter(end_d))
				aux=end_d;
			
			DataItem i = new DataItem();

			i.is_valid = false;
			i.from = OffsetDateTime.from(start_d);  
			i.to = OffsetDateTime.from(aux);
			i.db_count = 0;
			i.solr_count = 0;
			
			try {
				
				executeQuery(i);
				
			} catch (Exception e) {
				logger.error(e);
				
			}
			finally {
				list.add(i);
			}
			start_d=aux;
		}
	}

	
	/**
	 * @param i
	 */
	private void executeQuery(DataItem i) {
	
		try {
			
			/** Hibernate  --------- */
			
			Query<?> query_h = sf.getCurrentSession().createQuery("select count(*) FROM KbeeIDoc c WHERE c.lastModifiedDate BETWEEN :stDate AND :edDate AND domain.id>0")
			.setParameter("stDate", i.from)
			.setParameter("edDate", i.to);
			i.db_count = ((Long) query_h.uniqueResult()).longValue();
			i.db_query=query_h.getQueryString();

			
			/** SolR  --------- */
			
			SolrParametersQuery query_s = new SolrParametersQuery(getQueryIndex());
			
			
			i.solr_query.put("domain", "[1 TO *]");
			i.solr_query.put("type",   "idoc");			
			i.solr_query.put("modified", new SolrDateRangeFilter("modified", i.from, i.to));
			
			for (Entry<String, Object> entry: i.solr_query.entrySet())
				query_s.getParameters().put(entry.getKey(), entry.getValue());
			
			i.solr_query_str=query_s.getStatement();
			
			ResultSet resultset = query_s.execute();
			i.solr_count = Integer.valueOf(resultset.size()).longValue();
			
			i.is_valid=true;
			
		} catch (Exception e) {
				i.is_valid=false;
				logger.error(e);
				throw(e);
			}
	}
	
	
	public Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	
	private Index getQueryIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}

	
	//i.db_query   = "select count(*) from KbeeIDoc where lastmodifieddate >="+
	//"'"+String.valueOf(i.from.getYear())+"-"+String.format("%02d", i.from.getMonth().getValue())+"-01 00:00:00.000-00' and lastmodifieddate <"+
	//"'"+String.valueOf(i.to.getYear())  +"-"+String.format("%02d",   i.to.getMonth().getValue())+"-01 00:00:00.000-00'";
	/*
	Query<?> query = sessionfactory.getCurrentSession()
	i.db_query   = "select count(*) from KbeeIDoc where lastmodifieddate >="+
			DateTimeFormatter.ofPattern ( "YYYY-MM-dd HH:mm:ss").format(i.from) + " and lastmodifieddate < " +
			DateTimeFormatter.ofPattern ( "YYYY-MM-dd HH:mm:ss").format(i.to);
	logger.debug(DateTimeFormatter.ofPattern ( "YYYY-MM-dd HH:mm:ss").format(i.from));
	logger.debug(DateTimeFormatter.ofPattern ( "YYYY-MM-dd HH:mm:ss").format(i.from));
	*/
	//"'"+String.valueOf(i.from.getYear())+"-"+String.format("%02d", i.from.getMonth().getValue())+"-01 00:00:00.000-00' and lastmodifieddate <"+
	//"'"+String.valueOf(i.to.getYear())  +"-"+String.format("%02d",   i.to.getMonth().getValue())+"-01 00:00:00.000-00'";
	// i.solr_query.put("modified", "["+String.valueOf(i.from.getYear())+"-"+String.format("%02d", i.from.getMonth().getValue())+"-01T00:00:00.00Z TO "+String.valueOf(i.to.getYear())  +"-"+String.format("%02d",i.to.getMonth().getValue())+"-01T00:00:00.00Z]");
	// i.solr_query.put("modified","["+DateTimeFormatter.ofPattern("YYYY-MM-dd'T'hh:mm:ss'Z'").format(i.from)+" TO "+DateTimeFormatter.ofPattern("YYYY-MM-dd'T'hh:mm:ss'Z'").format(i.to)+"]");
			

	

	
}
