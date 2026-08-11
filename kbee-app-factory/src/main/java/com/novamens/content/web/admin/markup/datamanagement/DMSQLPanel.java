package com.novamens.content.web.admin.markup.datamanagement;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.entity.Person;
import com.novamens.content.web.sql.markup.SQLGatewayPage;
import com.novamens.content.web.sql.markup.SQLToolsPanel;
import com.novamens.security.Identifiable;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KeyValue;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.DummyBlockPanel;

import kbee.web.error.ErrorPanel;
import kbee.web.panel.ListSimplePanel;


/**
 * 
 * Only kbee root can access this panel
 * 
 *
 */
public class DMSQLPanel extends AbstractDataManagementPanel {
							
	final boolean is_root = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();

	private static final long serialVersionUID = 1L;

	
	public class KVI<T extends Serializable> extends KeyValue<T> implements Identifiable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public KVI(Serializable k, T v) {
			super(k, v);
		}

		@Override
		public Serializable getId() {
			return key;
		}
		
		
		
	}
	
	public DMSQLPanel() {
		this("info-panel");
	}

	public DMSQLPanel(String id) {
		super(id);

		
		if (is_root && isDomainKbee()) {
			
			SQLToolsPanel panel = new SQLToolsPanel("tools", new ObjectModel<Person>(getPerson())) {
				private static final long serialVersionUID = 1L;
				@Override
				protected void onNewQuery(AjaxRequestTarget target, String text) {
					setResponsePage(new SQLGatewayPage(text));
				}
				@Override
				public boolean isVisible() {
					return ServiceLocator.getService(SecurityService.class).isRoot();
				}
			};
			
			panel.add(new AttributeModifier("class", "col-lg-12 col-md-12 col-xs-12"));
			
			 
			add(panel);
			 
					
			ListSimplePanel<KVI<String>> pa= new ListSimplePanel<KVI<String>>("useful-queries", "useful-queries", getItems()) {
				
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				protected void onClick(IModel<KVI<String>> modelObject) {
					setResponsePage(new SQLGatewayPage(modelObject.getObject().getValue()));
				}
				@Override
				protected String getTitleMeta() {
					return " ("+String.valueOf(getItems().size()+")");
				}
				
				protected IModel<String> getItemAbstract(IModel<KVI<String>> modelObject) {
					return new Model<String>(modelObject.getObject().getValue());
				}
				
				@Override
				protected IModel<String> getItemLabelMeta(IModel<KVI<String>> modelObject) {
					return null;
				}
			};
			
			pa.setExpand(true);
			pa.setTitle( new Model<String>("Useful queries"));
			add(pa);

			//WrapperPanel wpanel = new WrapperPanel("info", panel);
			//add(wpanel);
		}
		else {
			add(new ErrorPanel("tools", "Authorization Error", "Only root user can access this panel"));
			add(new DummyBlockPanel("useful-queries"));
			
		}
	}
	
	
	List<IModel<KVI<String>>>  lq=null;

	private List<IModel<KVI<String>>> getItems() {
		
		if (lq!=null)
			return lq;

		lq  =new ArrayList<IModel<KVI<String>>>();

		lq.add( new Model<KVI<String>> (  new KVI<String>("Content - Classifiers for a Content (id=50928)", "select C.id, C.title, CL.name, DM.strvalue from content C, kb_classifier CL, dataset D, datasetmember DM, classification F where  C.id=F.content_id and CL.dataset_id=D.id and CL.id=F.classifier_id and F.datasetmember_id=DM.id and C.id=50928")  ));
		lq.add( new Model<KVI<String>> (  new KVI<String>("Content - Attributes for a Content (id=50928)", "select C.id, C.title, CL.name, DM.strvalue from content C, kb_classifier CL, dataset D, datasetmember DM, classification F where  C.id=F.content_id and CL.dataset_id=D.id and CL.id=F.classifier_id and F.datasetmember_id=DM.id and C.id=50928")  ));
		lq.add( new Model<KVI<String>> (  new KVI<String>("Content - list of Content published by root@windsor", "select title, date(checkindate), id, oid, version from content where ishead=true and lastmodifieduser=(select id from users where username='root@windsor') and date(checkindate)>='2018-05-01' order by checkindate;")  ));

		lq.add( new Model<KVI<String>> (  new KVI<String>("Security - List of Users with Domain", "select PE.firstname, PE.lastname, U.username, D.id, D.name from entity E, profile P, userprofile  UP, Person PE, Users U, Domain D where  (D.id=P.domain_id) and (U.id=UP.user_id) and (UP.id=P.id) and (E.id=P.entity) and (PE.entity_id=E.id)")  ));;
		
		lq.add( new Model<KVI<String>> (  new KVI<String>("DB - List Databases", "select datname, pg_size_pretty(pg_database_size( datname )) from  (select datname from pg_database) query order by pg_database_size( datname ) desc")  ));
		
		
		lq.add( new Model<KVI<String>> (  new KVI<String>("DB - Tables of a schema ('public')", "SELECT * FROM pg_catalog.pg_tables where schemaname='public' order by lower(tablename)")  ));
		
		lq.add( new Model<KVI<String>> (  new KVI<String>("DB - Table Structure ('content')", "select column_name, data_type, character_maximum_length from INFORMATION_SCHEMA.COLUMNS where table_name ='content' order by column_name")  ));
		
		
		lq.add( new Model<KVI<String>> (  new KVI<String>("Resources - Total, encrypted, not encrypted", 
				"select total, total_stored, total_kbfs1, total_external, total_s3, not_enc, enc from (select count(*) As Total from kfile) A, (select count(*) As Total_stored from kfile where storagemode=2  or storagemode=3) D, " + 
				"(select count(*) As Total_kbfs1 from kfile where storagemode=1) E, " + 
				"(select count(*) As Total_external from kfile where storagemode=20) F, " + 
				"(select count(*) As total_s3 from kfile where storagemode=10) H, " + 
				"(select count(*) As Not_enc from kfile where ISENCRYPTED=false) B,  " + 
				"(select count(*) As Enc from kfile where ISENCRYPTED=true) C")  ));
				
		
		
		
		lq.add( new Model<KVI<String>> (  new KVI<String>("DB - Pid started more than 1 hour ago",	"SELECT		  pid,		  usename,		  datname,		  state,		  NOW() - pg_stat_activity.query_start AS duration,		  wait_event_type,		  wait_event,		  query		FROM pg_stat_activity		WHERE (NOW() - pg_stat_activity.query_start) > interval '60 minutes'	ORDER BY duration DESC"		)  ));
		lq.add( new Model<KVI<String>> (  new KVI<String>("DB - Table sizes", 
				"select \r\n" + 
				"       iot.relname as table_name,\r\n" + 
				"       pg_size_pretty(pg_total_relation_size(iot.relid)) as \r\n" + 
				"      total_size,\r\n" + 
				"       pg_size_pretty(pg_relation_size(iot.relid)) as data_size,\r\n" + 
				"       pg_size_pretty(pg_total_relation_size(iot.relid) - pg_relation_size(iot.relid))as external_size,\r\n" + 
				"       ut.n_live_tup as nrows,\r\n" + 
				"iot.schemaname as table_schema\r\n" + 
				"\r\n" + 
				"from pg_catalog.pg_statio_user_tables iot inner join pg_stat_user_tables ut on iot.relid = ut.relid\r\n" + 
				"order by pg_total_relation_size(iot.relid) desc,\r\n" + 
				"         pg_relation_size(iot.relid) desc")  ));

		
		/**
		 * 
		 * select   round( EXTRACT(EPOCH FROM ts)),    round(hard_disk_usage/1000000000.0, 0)  hd_total_gb from kb_usage_stat where domain_id= (select id from domain where name='kbee')  and  (ts>'2021 11 01 00:00:00 CDT' and ts<='2023 09 10 21:00:00 CDT')  order by ts
		 * 
		 */
		
		
		lq.add( new Model<KVI<String>> (  new KVI<String>("Factory - hard disk usage all for a range)", "select  ts \"date\", EXTRACT(EPOCH FROM ts) \"dateLong\",    hard_disk_usage/1000000000.0  hd_total_gb from kb_usage_stat where domain_id= (select id from domain where name='kbee')  and  (ts>'2018 10 01 02:00:00 CDT' and ts<='2020 09 10 21:00:00 CDT')  order by ts")  ));
		lq.add( new Model<KVI<String>> (  new KVI<String>("Factory - KB Usage Stats", "select date(ts) as Day, D.name, hard_disk_usage as bytes, contents, resources from kb_usage_stat H, domain D where H.domain_id=D.id order by D.id, Day")  ));
		lq.add( new Model<KVI<String>> (  new KVI<String>("DB - Long running queries", 	"SELECT		  pid,		  usename,		  datname,		  state,		  NOW() - pg_stat_activity.query_start AS duration,		  wait_event_type,		  wait_event,		  query		FROM pg_stat_activity		WHERE (NOW() - pg_stat_activity.query_start) > interval '5 minutes'		ORDER BY duration DESC")  ));
		lq.add( new Model<KVI<String>> (  new KVI<String>("DB - Table needs vacuum", 
		"SELECT " + 
		"  schemaname," + 
		"  relname," + 
		"  n_dead_tup," + 
		"  n_live_tup," + 
		"  n_dead_tup / n_live_tup   AS percent_dead_tuples FROM pg_stat_user_tables WHERE n_live_tup > 0 ORDER BY n_dead_tup DESC;")  ));
		
		lq.add( new Model<KVI<String>> (  new KVI<String>("DB - Indexes",	"SELECT		  schemaname,		  relname,		  indexrelname,		  idx_scan		FROM pg_stat_user_indexes"		)  ));
		lq.add( new Model<KVI<String>> (  new KVI<String>("DB - Current DB Size",	"SELECT pg_size_pretty(pg_database_size(current_database()))"		)  ));
		lq.add( new Model<KVI<String>> (  new KVI<String>("DB - Current DB Table size",	"SELECT schemaname AS table_schema,		  relname AS table_name,		  pg_size_pretty(pg_relation_size(relid)) AS data_size		FROM pg_catalog.pg_stat_user_tables		ORDER BY pg_relation_size(relid) desc"		)  ));
		lq.add( new Model<KVI<String>> (  new KVI<String>("DB - Current DB Index size",	"SELECT schemaname AS table_schema,		  relname AS table_name,		  indexrelname AS index_name,		  idx_scan AS id_scans,		  pg_size_pretty(pg_relation_size(relid)) AS data_size		FROM pg_catalog.pg_stat_user_indexes		ORDER BY pg_relation_size(relid) DESC")));
		lq.add( new Model<KVI<String>> (  new KVI<String>("DB - Temp files", "SELECT datname, temp_files, temp_bytes		FROM pg_stat_database")));
				
		/**
			SELECT schemaname AS table_schema,		  relname AS table_name,		  indexrelname AS index_name,		  idx_scan AS id_scans,		  pg_size_pretty(pg_relation_size(relid)) AS data_size		FROM pg_catalog.pg_stat_user_indexes		ORDER BY pg_relation_size(relid) DESC
		**/
		/**
			SELECT pg_cancel_backend(<pid>);
			SELECT pg_terminate_backend(<pid>);
		 **/
		
		/**
		 * 
		 * 
 			delete from kb_cronjob where name='Cancel Idle Transaction';
			insert into  kb_cronjob (id, lastmodifieduser, name, description, cronexpression, clazz, parameter) 
			values 
			(
			(select nextval('objectid_sequence')), 
			(select id from users where username='root@kbee'), 
			'Cancel Idle Transaction',  
		    'Cancel TRX that have been idle for more than 2.5h',
			'38 50 * * * *', 
			'com.novamens.kbee.content.service.datamanagement.SQLCronJobRequest', 
			'SELECT pg_terminate_backend(pid) from (select pid from pg_stat_activity where pid <> pg_backend_pid() and  state  like ''idle in transaction%''   and now()- xact_start > ''150 minute''\:\:interval) AS ACT');

		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 */
		lq.sort( new Comparator<IModel<KVI<String>> >() {
			@Override
			public int compare(IModel<KVI<String>> a, IModel<KVI<String>> b) {
				try {
				return a.getObject().getKey().toString().compareToIgnoreCase(b.getObject().getKey().toString());
				} catch (Exception e) {
					return 0;
				}
			}
		});
		return lq;
	}

	@Override
	protected BCElement getPageBCElement() {
		return new BCElement(new Model<String>("SQL Gateway"));
	}
	

	protected boolean isDomainKbee() {
		try {
			return getPerson().getDomain().getName().toLowerCase().trim().equals("kbee");
		} 
		catch (Exception e) {
			return false;
		}
	}
}
