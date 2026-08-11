package com.novamens.content.web.admin.markup;


import java.io.File;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.wicket.model.Model;

import com.novamens.datetime.DateTimeService;
import com.novamens.dom.KBFSStorageType;
import com.novamens.kbee.kbfs.KbeeMinioFileServer;

import com.novamens.kbee.kbfs.KbeeShardedMinioFileServer;

import com.novamens.kbee.kbfs.KbeeShardedOdilonFileServer;
import com.novamens.kbee.wicket.markup.html.areainfo.AreaInfoPanel;
import com.novamens.kbee.wicket.markup.html.areainfo.GridInfoPanel;
import com.novamens.kbfs.FileServerMinio;
import com.novamens.kbfs.FileServerOdilon;
import com.novamens.kbfs.FileServerS3;
import com.novamens.kbfs.v1.FileServerV1;
import com.novamens.metrics.SystemMetricsService;
import com.novamens.metrics.domain.DomainMetricsService;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.system.properties.SystemPropertiesService;
import com.novamens.wicket.util.BCElement;

import kbee.util.NumberFormatter;
import kbee.util.PropertiesFactory;
import kbee.util.Tuple;

public class SystemInfoKBFSPanel extends AbstractSystemInfoPanel {

	private static final long serialVersionUID = 1L;
														
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SystemInfoKBFSPanel.class.getName());
	
	//boolean kbfs1_enabled;
	boolean kbfs2_enabled;
	boolean odilon_enabled;
	
	
	public SystemInfoKBFSPanel() {
		this("info-panel");
	}
	
	/**
	 * @param id
	 */
	public SystemInfoKBFSPanel(String id) {
		super(id);
	}
	
	
	/**
	 * 
	 */
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		//kbfs1_enabled = ServiceLocator.getService(com.novamens.service.ApplicationServerService.class).isLocalFSEnabled();
		kbfs2_enabled=ServiceLocator.getService(com.novamens.service.ApplicationServerService.class).isMinioEnabled();
		odilon_enabled=ServiceLocator.getService(com.novamens.service.ApplicationServerService.class).isOdilonEnabled();
		
		AreaInfoPanel area = new AreaInfoPanel("info");
		add(area);
		
		area.setSections(AreaInfoPanel.ONE_SECTION);
		area.setCss("col-lg-12");
		
		area.addPanel(new GridInfoPanel("element",  KBFSInfo(), getLabel("key-metrics"), true ));
		area.addPanel(new GridInfoPanel("element", resourcesInfo(), getLabel("total-resources") , true));
	
		
		
		// Odilon ---
		
		FileServerOdilon fsodilon=ServiceLocator.getService(FileServerOdilon.class);
		
		{
			if (fsodilon instanceof KbeeShardedOdilonFileServer) {
				area.addPanel(new GridInfoPanel("element", shardManagerOdilonInfo(fsodilon),getLabel("odilon-shard-manager"), true));
				Map<Integer, FileServerOdilon> map = ((KbeeShardedOdilonFileServer) fsodilon).getShards();
				for (Entry<Integer, FileServerOdilon> entry: map.entrySet()) 
					area.addPanel(new GridInfoPanel("element",  OdilonInfo(entry.getValue()), 
						new Model<String>("Odilon Shard " + entry.getKey().toString()), true));
			}
			else {
				area.addPanel(new GridInfoPanel("element",  OdilonInfo(fsodilon), new Model<String>("Odilon"), true));
			}
		}

		
		// S3 ---
		//
		
		FileServerS3 s3=ServiceLocator.getService(FileServerS3.class);
		if (s3!=null)
			area.addPanel(new GridInfoPanel("element",  S3Info(s3), new Model<String>("Amazon S3"), true));

		
		area.addPanel(new GridInfoPanel("element",  KBFS1Info(), new Model<String>("File System"), true));
		area.addPanel(new GridInfoPanel("element",  KBFSGatewayInfo(), new Model<String>("Gateway"), true));
		
		
		
		// Minio ---
		
		FileServerMinio fsv2=ServiceLocator.getService(FileServerMinio.class);
		
		if (fsv2 instanceof KbeeShardedMinioFileServer) {
			area.addPanel(new GridInfoPanel("element", shardManagerInfo(fsv2),getLabel("kbfs-shard-manager"), true));
			Map<Integer, FileServerMinio> map = ((KbeeShardedMinioFileServer) fsv2).getShards();
			for (Entry<Integer, FileServerMinio> entry: map.entrySet()) 
				area.addPanel(new GridInfoPanel("element",  KBFSInfo(entry.getValue()), 
						new Model<String>("Minio Shard " + entry.getKey().toString()), true));
		}
		else {
			area.addPanel(new GridInfoPanel("element",  KBFSInfo(fsv2), new Model<String>("Minio"), true));
		}
		
		
	}

	public KBFSStorageType getDefaultKBFSStorageType() {
		String defaultStorage=getContentDao().findSystemParameterValueByKey("kbfs.storage.default",  ServiceLocator.getService(SystemPropertiesService.class).getDefaultKBFSService());
		logger.debug(defaultStorage);
		return KBFSStorageType.getByKey(defaultStorage);
	
	}
	
	protected BCElement getPageBCElement() {
		return new BCElement(getLabel("object-storage"));
	}

	/***
	 *
	 * 
	 * 
	 * @return
	 */
	private List<Tuple> KBFS1Info() {
		
		long start = System.currentTimeMillis();		
		
		List<Tuple> data = new ArrayList<Tuple>();

		try {
			
		
		if (ServiceLocator.getService(FileServerV1.class)==null)
			return data;
		
		File frep = new File(ServiceLocator.getService(FileServerV1.class).getRootDirectory());

		
		 
		
		if (frep!=null) {		
			double fr_tot = (double) frep.getTotalSpace()/GB;
			double fr_usa = (double) frep.getUsableSpace()/GB;
			double usa_por = (fr_tot>0 ?fr_usa / fr_tot : 0) * 100.0; 
			String usa_por_str = NumberFormatter.formatNumber(usa_por).trim(); 
			
			String pg=ServiceLocator.getService(FileServerV1.class).ping();
			if (pg==null)
				 pg="err";
			boolean isok = pg.toLowerCase().equals("ok");
			String s="<div class= \" "+ (isok?"success":"danger") +"\" />"+pg+"</div>";
			data.add(new Tuple("Ping", s));

			try {

				SystemMetricsService metrics_service = ServiceLocator.getService(SystemMetricsService.class);
				
				double p1m  = metrics_service.getMeterV1PutObject().getOneMinuteRate();
				double p5m  = metrics_service.getMeterV1PutObject().getFiveMinuteRate();
				double p15m = metrics_service.getMeterV1PutObject().getFifteenMinuteRate();
				long 	pc  = metrics_service.getMeterV1PutObject().getCount();
				
				double g1m 	= metrics_service.getMeterV1GetObject().getOneMinuteRate();
				double g5m 	= metrics_service.getMeterV1GetObject().getFiveMinuteRate();
				double g15m = metrics_service.getMeterV1GetObject().getFifteenMinuteRate();
				long gc 	= metrics_service.getMeterV1GetObject().getCount();
				
				String rate_p = NumberFormatter.formatNumber(p1m) 	+ "<span class=\"separator\">|</span>" +
								NumberFormatter.formatNumber(p5m) 	+ "<span class=\"separator\">|</span>" +
								NumberFormatter.formatNumber(p15m);
												
				String rate_g =	NumberFormatter.formatNumber(g1m)   + "<span class=\"separator\">|</span>" +
								NumberFormatter.formatNumber(g5m)	+ "<span class=\"separator\">|</span>" +
								NumberFormatter.formatNumber(g15m); 
	
				data.add(new Tuple("Endpoint", frep.getAbsolutePath()));
			 	 
				data.add(new Tuple("putObject req/sec (1m 5m 15m) ",  rate_p));
				data.add(new Tuple("getObject req/sec (1m 5m 15m) ",  rate_g));
																								
				data.add(new Tuple("putObject mean rate",  NumberFormatter.formatNumber(metrics_service.getMeterV1PutObject().getMeanRate(), getSessionUser().getLocale())+ " <span class=\"ago\">req/sec</span>"));
				data.add(new Tuple("getObject mean rate",  NumberFormatter.formatNumber(metrics_service.getMeterV1GetObject().getMeanRate(), getSessionUser().getLocale())+ " <span class=\"ago\">req/sec</span>"));

				data.add(new Tuple("putObject Total",  NumberFormatter.formatNumber(pc, getSessionUser().getLocale())));
				data.add(new Tuple("getObject Total",  NumberFormatter.formatNumber(gc, getSessionUser().getLocale())));
				
				// Counted directly from disk
				data.add(new Tuple("Files (Local Disk)", NumberFormatter.formatNumber(ServiceLocator.getService(FileServerV1.class).getTotalFiles())));
				
				DomainMetricsService doms = ServiceLocator.getService(DomainMetricsService.class);

				data.add(new Tuple("Files (DB count)", NumberFormatter.formatNumber(doms.getTotalResources(KBFSStorageType.KBFS1), getSessionUser().getLocale())));
				data.add(new Tuple("Size (Local Disk)", 
						ServiceLocator.getService(DateTimeService.class).formatFileSize(ServiceLocator.getService(FileServerV1.class).getSize(), getSessionUser().getLocale(), "ago")
				));
		
				data.add(new Tuple("Disk Total",  NumberFormatter.formatNumber(fr_tot) +  "<span class=\"ago\"> GB </span>"));
				data.add(new Tuple("Disk Usable", NumberFormatter.formatNumber(fr_usa)+  " <span class=\"ago\"> GB </span> (" +  usa_por_str + " %) "));
				
				try {
					DateTimeService service = ServiceLocator.getService(DateTimeService.class);
					User user = getSessionUser();
					String zid = service.getMapZoneIds().get(user.getTimeZone());
					if (zid==null)
							zid=ZoneId.systemDefault().getId();
					long time_measure = doms.getTimeMeasure(getDomain());
					OffsetDateTime date = OffsetDateTime.ofInstant( Instant.ofEpochMilli(time_measure), ZoneId.of(zid));
					
					String xs=ServiceLocator.getService(DateTimeService.class).timeElapsed(date, ZoneId.of(zid), user.getLocale(), DateTimeService.DATE_COLlOQUIAL_AGO, "ago");
					data.add(new Tuple( "Date ", xs));
				} catch (Exception e) {
					data.add(new Tuple("Date", e.getClass().getName() + " | " + e.getMessage()));
				}

				
				
			} catch (Exception e) {
				logger.error(e);
				data.add(new Tuple("Error ", e.getClass().getName() + " | " + e.getMessage()));
			}
		}
		

	} finally {
		long end = System.currentTimeMillis();
		if (logger.isDebugEnabled())
			data.add(new Tuple("Render time ", String.valueOf(end-start)+" <span class=\"ago\">ms</span>"));
		logger.debug("Render time " + String.valueOf(end-start)+"ms");
	}
		
		
		return data;
	}
	
	/**
	 * 
	 * 
	 * 
	 * @param fsv2
	 * @return
	 * 
	 * 
	 */
	private List<Tuple> shardManagerInfo(FileServerMinio fsv2) {

		long start = System.currentTimeMillis();
		
		List<Tuple> data = new ArrayList<Tuple>();

		try {
			
		
		SystemMetricsService metrics_service = ServiceLocator.getService(SystemMetricsService.class);
		
		double p1m 	= metrics_service.getMeterV2PutObject().getOneMinuteRate();
		double p5m 	= metrics_service.getMeterV2PutObject().getFiveMinuteRate();
		double p15m = metrics_service.getMeterV2PutObject().getFifteenMinuteRate();
		long pc 	= metrics_service.getMeterV2PutObject().getCount();

		double g1m 	= metrics_service.getMeterV2GetObject().getOneMinuteRate();
		double g5m 	= metrics_service.getMeterV2GetObject().getFiveMinuteRate();
		double g15m = metrics_service.getMeterV2GetObject().getFifteenMinuteRate();
		long gc 	= metrics_service.getMeterV2GetObject().getCount();

		int c_size		=	((KbeeShardedMinioFileServer) fsv2).getCacheSize();
		long c_usage	=	((KbeeShardedMinioFileServer) fsv2).getCacheUsage();
		

		long end2=System.currentTimeMillis();
		logger.debug("Minio Shard Manager Cache Size, Usage"+ String.valueOf(end2-start)+ " ms");
		
		if (this.kbfs2_enabled) {
			data.add(new Tuple( "Status", "enabled"));
			String pg=fsv2.ping();
			if (pg==null)
				 pg="err";
			boolean isok = pg.toLowerCase().equals("ok");
			
			String s="<div class= \" "+ (isok?"success":"danger") +"\" />"+pg+"</div>";
			data.add(new Tuple( "Ping", s));
		}
		else {
			data.add(new Tuple( "Ping", "Minio not enabled"));
			data.add(new Tuple( "Status", "Minio not enabled"));
		}
		
	/**	try {
			KBFSStorageType ty= getDefaultKBFSStorageType();
			data.add(new Tuple( "Default Storage Type ", ty.getLabel()));
			
		} catch (Exception e) {
			logger.error(e);
			KBFSStorageType ty= getDefaultKBFSStorageType();
			if (ty!=null)
				data.add(new Tuple( "Default Storage Type", ty.getLabel()));
		}
		**/
		
		String rate_p = 		NumberFormatter.formatNumber(p1m) + "<span class=\"separator\">|</span>" +
								NumberFormatter.formatNumber(p5m) + "<span class=\"separator\">|</span>" +
								NumberFormatter.formatNumber(p15m);
		
		String rate_g =			NumberFormatter.formatNumber(g1m) + "<span class=\"separator\">|</span>" +
								NumberFormatter.formatNumber(g5m) + "<span class=\"separator\">|</span>" +
								NumberFormatter.formatNumber(g15m); 
				
		data.add(new Tuple( "putObject reqs/sec (1m 5m 15m) ",  rate_p));
		data.add(new Tuple( "getObject reqs/sec (1m 5m 15m) ",  rate_g));
		
		data.add(new Tuple( "putObject Total",  NumberFormatter.formatNumber(pc)));
		data.add(new Tuple( "getObject Total",  NumberFormatter.formatNumber(gc)));
					
		data.add(new Tuple( "putObject mean rate",  NumberFormatter.formatNumber(metrics_service.getMeterV2PutObject().getMeanRate(), getSessionUser().getLocale())+ " <span class=\"ago\">req/sec</span>" ));
		data.add(new Tuple( "getObject mean rate",  NumberFormatter.formatNumber(metrics_service.getMeterV2GetObject().getMeanRate(), getSessionUser().getLocale())+ " <span class=\"ago\">req/sec</span>" ));
		
		long cache_hits=metrics_service.getCounterV2KBFSCacheHit().getCount();
		long cache_miss=metrics_service.getCounterV2KBFSCacheMiss().getCount();

		try {
			long totalc=metrics_service.getCounterV2KBFSCacheHit().getCount()+metrics_service.getCounterV2KBFSCacheMiss().getCount();
			String cache_rate = totalc>0?(NumberFormatter.formatNumber(metrics_service.getCounterV2KBFSCacheHit().getCount()*100.0/totalc)+"%"):"N/A";
			String cache_h =	NumberFormatter.formatNumber(cache_hits).trim() + "<span class=\"separator\">|</span>" + String.format("%9d", cache_miss).trim() + "<span class=\"separator\">|</span>" + cache_rate.trim();
			data.add(new Tuple( "getFile Cache (hit miss hits/total)",  cache_h));

			start=System.currentTimeMillis();
			DomainMetricsService doms = ServiceLocator.getService(DomainMetricsService.class);

			
			data.add(new Tuple( "Total Resources (Odilon, Minio, Archive)", NumberFormatter.formatNumber(doms.getTotalResources(KBFSStorageType.Odilon) + doms.getTotalResources(KBFSStorageType.Minio) + doms.getTotalResources(KBFSStorageType.MinioArchive))));
			data.add(new Tuple( "Total Hard Disk (Odilon, Minio, Archive)",	ServiceLocator.getService(DateTimeService.class).formatFileSize(doms.getTotalHardDisk(KBFSStorageType.Odilon) + doms.getTotalHardDisk(KBFSStorageType.Minio) + doms.getTotalHardDisk(KBFSStorageType.MinioArchive), getSessionUser().getLocale(), "ago") ));
			
			if (logger.isDebugEnabled()) {
				long end=System.currentTimeMillis();
				logger.debug("Minio Shard Manager Cache Size, Usage"+ String.valueOf(end-start)+ " ms");
			}
			
			data.add(new Tuple( "Cache objects",  NumberFormatter.formatNumber(c_size)));
			data.add(new Tuple( "Cache storage",  ServiceLocator.getService(DateTimeService.class).formatFileSize(c_usage, getSessionUser().getLocale(), "ago") ));
			
			for (Path root : FileSystems.getDefault().getRootDirectories()) {
			    try {
			    	FileStore store = Files.getFileStore(root);
					double fr_tot = (double) store.getTotalSpace()/GB;
					double fr_usa = (double) store.getUsableSpace()/GB;
					double usa_por = (fr_tot>0 ?fr_usa / fr_tot : 0) * 100.0; 
					String usa_por_str = NumberFormatter.formatNumber(usa_por).trim(); 
					data.add(new Tuple("Cache Usable: "+ root, NumberFormatter.formatNumber(fr_usa)+  " <span class=\"ago\"> GB </span> (" +  usa_por_str + " %) "));
			    } catch (IOException e) {
			    	data.add(new Tuple( "Error ",  	e.getClass().getName()));
			    }
			}
			
		} catch (Exception e) {
			data.add(new Tuple( "Error ",  	e.getClass().getName()));
			logger.error(e);
		}


		
		try {
			DomainMetricsService doms = ServiceLocator.getService(DomainMetricsService.class);
			DateTimeService service = ServiceLocator.getService(DateTimeService.class);
			User user = getSessionUser();
			String zid = service.getMapZoneIds().get(user.getTimeZone());
			if (zid==null)
					zid=ZoneId.systemDefault().getId();
			long time_measure = doms.getTimeMeasure(getDomain());
			OffsetDateTime date = OffsetDateTime.ofInstant( Instant.ofEpochMilli(time_measure), ZoneId.of(zid));
			
			String xs=ServiceLocator.getService(DateTimeService.class).timeElapsed(date, ZoneId.of(zid), user.getLocale(), DateTimeService.DATE_COLlOQUIAL_AGO, "ago");
			data.add(new Tuple( "Time  measured ", xs));
		} catch (Exception e) {
			data.add(new Tuple("Time  measured ", e.getClass().getName() + " | " + e.getMessage()));
		}

		
		} finally {
			long end = System.currentTimeMillis();
			if (logger.isDebugEnabled())
				data.add(new Tuple("Render time ", String.valueOf(end-start)+" <span class=\"ago\">ms</span>"));
			logger.debug("Render time " + String.valueOf(end-start)+"ms");
		}
		return data;
	}
	

	/**
	 * 
	 * 
	 * 
	 * @param fsv2
	 * @return
	 * 
	 * 
	 */
	private List<Tuple> shardManagerOdilonInfo(FileServerOdilon fsv2) {

		long start = System.currentTimeMillis();
		
		List<Tuple> data = new ArrayList<Tuple>();

		try {
			
		
		SystemMetricsService metrics_service = ServiceLocator.getService(SystemMetricsService.class);
		
		double p1m 	= metrics_service.getMeterOdilonPutObject().getOneMinuteRate();
		double p5m 	= metrics_service.getMeterOdilonPutObject().getFiveMinuteRate();
		double p15m = metrics_service.getMeterOdilonPutObject().getFifteenMinuteRate();
		long pc 	= metrics_service.getMeterOdilonPutObject().getCount();

		double g1m 	= metrics_service.getMeterOdilonGetObject().getOneMinuteRate();
		double g5m 	= metrics_service.getMeterOdilonGetObject().getFiveMinuteRate();
		double g15m = metrics_service.getMeterOdilonGetObject().getFifteenMinuteRate();
		long gc 	= metrics_service.getMeterOdilonGetObject().getCount();

		
		//int c_size		=	((KbeeShardedOdilonFileServer) fsv2).getCacheSize();
		//long c_usage	=	((KbeeShardedOdilonFileServer) fsv2).getCacheUsage();
		

		int c_size		=	0;
		long c_usage	=	0;

		long end2=System.currentTimeMillis();
		logger.debug("Odilon Shard Manager Cache Size, Usage"+ String.valueOf(end2-start)+ " ms");
		
		if (this.kbfs2_enabled) {
			data.add(new Tuple( "Status", "enabled"));
			String pg=fsv2.ping();
			if (pg==null)
				 pg="err";
			boolean isok = pg.toLowerCase().equals("ok");
			
			String s="<div class= \" "+ (isok?"success":"danger") +"\" />"+pg+"</div>";
			data.add(new Tuple( "Ping", s));
		}
		else {
			data.add(new Tuple( "Ping", "Odilon not enabled"));
			data.add(new Tuple( "Status", "Odilon not enabled"));
		}
		
		//try {
			//KBFSStorageType ty= getDefaultKBFSStorageType();
			//data.add(new Tuple( "Default Storage Type ", ty.getLabel()));
			
		//} catch (Exception e) {
		//	logger.error(e);
		//	KBFSStorageType ty= getDefaultKBFSStorageType();
		//	if (ty!=null)
		//		data.add(new Tuple( "Default Storage Type", ty.getLabel()));
		//}
		
		String rate_p = 		NumberFormatter.formatNumber(p1m) + "<span class=\"separator\">|</span>" +
								NumberFormatter.formatNumber(p5m) + "<span class=\"separator\">|</span>" +
								NumberFormatter.formatNumber(p15m);
		
		String rate_g =			NumberFormatter.formatNumber(g1m) + "<span class=\"separator\">|</span>" +
								NumberFormatter.formatNumber(g5m) + "<span class=\"separator\">|</span>" +
								NumberFormatter.formatNumber(g15m); 
				
		data.add(new Tuple( "putObject reqs/sec (1m 5m 15m) ",  rate_p));
		data.add(new Tuple( "getObject reqs/sec (1m 5m 15m) ",  rate_g));
		
		data.add(new Tuple( "putObject Total",  NumberFormatter.formatNumber(pc)));
		data.add(new Tuple( "getObject Total",  NumberFormatter.formatNumber(gc)));
					
		data.add(new Tuple( "putObject mean rate",  NumberFormatter.formatNumber(metrics_service.getMeterV2PutObject().getMeanRate(), getSessionUser().getLocale())+ " <span class=\"ago\">req/sec</span>" ));
		data.add(new Tuple( "getObject mean rate",  NumberFormatter.formatNumber(metrics_service.getMeterV2GetObject().getMeanRate(), getSessionUser().getLocale())+ " <span class=\"ago\">req/sec</span>" ));
		
		long cache_hits=metrics_service.getCounterV2KBFSCacheHit().getCount();
		long cache_miss=metrics_service.getCounterV2KBFSCacheMiss().getCount();

		try {
			long totalc=metrics_service.getCounterV2KBFSCacheHit().getCount()+metrics_service.getCounterV2KBFSCacheMiss().getCount();
			String cache_rate = totalc>0?(NumberFormatter.formatNumber(metrics_service.getCounterV2KBFSCacheHit().getCount()*100.0/totalc)+"%"):"N/A";
			String cache_h =	NumberFormatter.formatNumber(cache_hits).trim() + "<span class=\"separator\">|</span>" + String.format("%9d", cache_miss).trim() + "<span class=\"separator\">|</span>" + cache_rate.trim();
			data.add(new Tuple( "getFile Cache (hit miss hits/total)",  cache_h));

			start=System.currentTimeMillis();
			DomainMetricsService doms = ServiceLocator.getService(DomainMetricsService.class);

			
			data.add(new Tuple( "Total Resources (Odilon)",  NumberFormatter.formatNumber(doms.getTotalResources(KBFSStorageType.Odilon))));
			
			data.add(new Tuple( "Total Hard Disk (Odilon)", ServiceLocator.getService(DateTimeService.class).formatFileSize(doms.getTotalHardDisk(KBFSStorageType.Odilon), getSessionUser().getLocale(), "ago") ));
			
			if (logger.isDebugEnabled()) {
				long end=System.currentTimeMillis();
				logger.debug("Minio Shard Manager Cache Size, Usage"+ String.valueOf(end-start)+ " ms");
			}
			
			data.add(new Tuple( "Cache objects",  NumberFormatter.formatNumber(c_size)));
			data.add(new Tuple( "Cache storage",  ServiceLocator.getService(DateTimeService.class).formatFileSize(c_usage, getSessionUser().getLocale(), "ago") ));
			
			for (Path root : FileSystems.getDefault().getRootDirectories()) {
			    try {
			    	FileStore store = Files.getFileStore(root);
					double fr_tot = (double) store.getTotalSpace()/GB;
					double fr_usa = (double) store.getUsableSpace()/GB;
					double usa_por = (fr_tot>0 ?fr_usa / fr_tot : 0) * 100.0; 
					String usa_por_str = NumberFormatter.formatNumber(usa_por).trim(); 
					data.add(new Tuple("Cache Usable: "+ root, NumberFormatter.formatNumber(fr_usa)+  " <span class=\"ago\"> GB </span> (" +  usa_por_str + " %) "));
			    } catch (IOException e) {
			    	data.add(new Tuple( "Error ",  	e.getClass().getName()));
			    }
			}
			
		} catch (Exception e) {
			data.add(new Tuple( "Error ",  	e.getClass().getName()));
			logger.error(e);
		}


		
		try {
			DomainMetricsService doms = ServiceLocator.getService(DomainMetricsService.class);
			DateTimeService service = ServiceLocator.getService(DateTimeService.class);
			User user = getSessionUser();
			String zid = service.getMapZoneIds().get(user.getTimeZone());
			if (zid==null)
					zid=ZoneId.systemDefault().getId();
			long time_measure = doms.getTimeMeasure(getDomain());
			OffsetDateTime date = OffsetDateTime.ofInstant( Instant.ofEpochMilli(time_measure), ZoneId.of(zid));
			
			String xs=ServiceLocator.getService(DateTimeService.class).timeElapsed(date, ZoneId.of(zid), user.getLocale(), DateTimeService.DATE_COLlOQUIAL_AGO, "ago");
			data.add(new Tuple( "Time  measured ", xs));
		} catch (Exception e) {
			data.add(new Tuple("Time  measured ", e.getClass().getName() + " | " + e.getMessage()));
		}

		
		} finally {
			long end = System.currentTimeMillis();
			if (logger.isDebugEnabled())
				data.add(new Tuple("Render time ", String.valueOf(end-start)+" <span class=\"ago\">ms</span>"));
			logger.debug("Render time " + String.valueOf(end-start)+"ms");
		}
		return data;
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	private List<Tuple> S3Info(FileServerS3 s3) {
		
		
		List<Tuple> data = new ArrayList<Tuple>();
		
		long start = System.currentTimeMillis();
		
		try {
			
			SystemMetricsService metrics_service = ServiceLocator.getService(SystemMetricsService.class);
									

			double p1m 	= metrics_service.getMeterS3PutObject().getOneMinuteRate();
			double p5m 	= metrics_service.getMeterS3PutObject().getFiveMinuteRate();
			double p15m = metrics_service.getMeterS3PutObject().getFifteenMinuteRate();
			long pc 	= metrics_service.getMeterS3PutObject().getCount();

			double g1m 	= metrics_service.getMeterS3GetObject().getOneMinuteRate();
			double g5m 	= metrics_service.getMeterS3GetObject().getFiveMinuteRate();
			double g15m = metrics_service.getMeterS3GetObject().getFifteenMinuteRate();
			long gc 	= metrics_service.getMeterS3GetObject().getCount();

			//int c_size		=	s3.getCacheSize();
			//long c_usage	=	s3.getCacheUsage();

			
			
			
			if (s3.isEnabled()) {
				String pg=s3.ping();
				if (pg==null)
					 pg="err";
				data.add(new Tuple( "Status", "enabled"));
				boolean isok = pg.toLowerCase().equals("ok");
				String s="<div class= \" "+ (isok?"success":"danger") +"\" />"+pg+"</div>";
				data.add(new Tuple( "Ping", s));
			} else {
				data.add(new Tuple( "Ping", "S3 not enabled"));
				data.add(new Tuple( "Status", "S3 not enabled"));
			}
			
			
			data.add(new Tuple( "Environment",		 	s3.getEnvironment()));
			data.add(new Tuple( "Access Key", 			s3.getAccessKey()));
			// data.add(new Tuple( "Secret Key", 		isKbeeDomain() ? s3.getSecretKey() : "***"));
			
			data.add(new Tuple( "Secret Key",        isKbeeDomain() ? (s3.getSecretKey()!=null? s3.getSecretKey().substring(0, 3)+"...":"") : "***"));
			data.add(new Tuple( "Read only", 			s3.isReadOnly() ? "Yes" : "No"));
			

			
			String rate_p = 	NumberFormatter.formatNumber(p1m) + "<span class=\"separator\">|</span>" +
								NumberFormatter.formatNumber(p5m) + "<span class=\"separator\">|</span>" +
								NumberFormatter.formatNumber(p15m);
	
			String rate_g =		NumberFormatter.formatNumber(g1m) + "<span class=\"separator\">|</span>" +
								NumberFormatter.formatNumber(g5m) + "<span class=\"separator\">|</span>" +
								NumberFormatter.formatNumber(g15m); 
	
			data.add(new Tuple( "putObject reqs/sec (1m 5m 15m) ",  rate_p));
			data.add(new Tuple( "getObject reqs/sec (1m 5m 15m) ",  rate_g));
			
																															
			data.add(new Tuple( "putObject mean rate", NumberFormatter.formatNumber(metrics_service.getMeterS3PutObject().getMeanRate(), getSessionUser().getLocale())+ " <span class=\"ago\">req/sec</span>" ));
			data.add(new Tuple( "getObject mean rate", NumberFormatter.formatNumber(metrics_service.getMeterS3GetObject().getMeanRate(), getSessionUser().getLocale())+ " <span class=\"ago\">req/sec</span>" ));
			
			DomainMetricsService doms = ServiceLocator.getService(DomainMetricsService.class);
			
			data.add(new Tuple( "Total Resources",  NumberFormatter.formatNumber(doms.getTotalResources(KBFSStorageType.AmazonS3))));
			data.add(new Tuple( "Total Hard Disk", 							
			ServiceLocator.getService(DateTimeService.class).formatFileSize(doms.getTotalHardDisk(KBFSStorageType.AmazonS3) + doms.getTotalHardDisk(KBFSStorageType.AmazonS3), 
					getSessionUser().getLocale(), "ago")));
			
			try {
				DateTimeService service = ServiceLocator.getService(DateTimeService.class);
				User user = getSessionUser();
				String zid = service.getMapZoneIds().get(user.getTimeZone());
				if (zid==null)
						zid=ZoneId.systemDefault().getId();
				long time_measure = doms.getTimeMeasure(getDomain());
				OffsetDateTime date = OffsetDateTime.ofInstant( Instant.ofEpochMilli(time_measure), ZoneId.of(zid));
				
				String xs=ServiceLocator.getService(DateTimeService.class).timeElapsed(date, ZoneId.of(zid), user.getLocale(), DateTimeService.DATE_COLlOQUIAL_AGO, "ago");
				data.add(new Tuple( "Time  measured ", xs));
			} catch (Exception e) {
				data.add(new Tuple("Time  measured ", e.getClass().getName() + " | " + e.getMessage()));
			}
			
			
		} catch (Exception e) {
			logger.error(e);
			data.add(new Tuple( "Error",  e.getClass().getName() + " | " + e.getMessage()));
		}
		finally {
			long end=System.currentTimeMillis();
			if (logger.isDebugEnabled())
				data.add(new Tuple("Render time ", String.valueOf(end-start)+" <span class=\"ago\">ms</span>"));
		}
		return data;
		
	}

	/***
	 * @param fsv2
	 * @return
	 */
	private List<Tuple> KBFSInfo(FileServerMinio fsv2) {
		
		List<Tuple> data = new ArrayList<Tuple>();
		
		long start = System.currentTimeMillis();
		
		try {
			
			SystemMetricsService metrics_service = ServiceLocator.getService(SystemMetricsService.class);
			
			String shard=fsv2.getShard().toString();
			
			double p1m 	= metrics_service.getMeterV2ShardPutObject(shard).getOneMinuteRate();
			double p5m 	= metrics_service.getMeterV2ShardPutObject(shard).getFiveMinuteRate();
			double p15m = metrics_service.getMeterV2ShardPutObject(shard).getFifteenMinuteRate();
			long pc 	= metrics_service.getMeterV2ShardPutObject(shard).getCount();
		
			double g1m  = metrics_service.getMeterV2ShardGetObject(shard).getOneMinuteRate();
			double g5m  = metrics_service.getMeterV2ShardGetObject(shard).getFiveMinuteRate();
			double g15m = metrics_service.getMeterV2ShardGetObject(shard).getFifteenMinuteRate();
			long gc     = metrics_service.getMeterV2ShardGetObject(shard).getCount();
			
			
			if (this.kbfs2_enabled) {
				String pg=fsv2.ping();
				if (pg==null)
					 pg="err";
				data.add(new Tuple( "Status", "enabled"));
				boolean isok = pg.toLowerCase().equals("ok");
				String s="<div class= \" "+ (isok?"success":"danger") +"\" />"+pg+"</div>";
				data.add(new Tuple( "Ping", s));
			} else {
				data.add(new Tuple( "Ping", "Minio not enabled"));
				data.add(new Tuple( "Status", "Minio not enabled"));
			}
			
			if (fsv2!=null && (fsv2 instanceof KbeeMinioFileServer)) {
				data.add(new Tuple( "Fsid",		 	fsv2.getFSId()));
				data.add(new Tuple( "Endpoint",  	fsv2.getEndPoint()));
				data.add(new Tuple( "Access Key", 	fsv2.getAccessKey()));
				data.add(new Tuple( "Secret Key", 	fsv2.getSecretKey()));
			}
			
			data.add(new Tuple( "Shard", 		String.valueOf(fsv2.getShard())));
			data.add(new Tuple( "Probability", 	String.format("%6.2f",fsv2.getProbability())));
			data.add(new Tuple( "Read only", 	fsv2.isReadOnly() ? "yes" : "no"));
			
			String rate_p = 	NumberFormatter.formatNumber(p1m) + "<span class=\"separator\">|</span>" +
								NumberFormatter.formatNumber(p5m) + "<span class=\"separator\">|</span>" +
								NumberFormatter.formatNumber(p15m);
	
			String rate_g =		NumberFormatter.formatNumber(g1m) + "<span class=\"separator\">|</span>" +
								NumberFormatter.formatNumber(g5m) + "<span class=\"separator\">|</span>" +
								NumberFormatter.formatNumber(g15m); 
	
			data.add(new Tuple( "putObject reqs/sec (1m 5m 15m) ",  rate_p));
			data.add(new Tuple( "getObject reqs/sec (1m 5m 15m) ",  rate_g));
			
			data.add(new Tuple( "putObject Total", String.valueOf(pc)));
			data.add(new Tuple( "getObject Total", String.valueOf(gc)));
																															
			data.add(new Tuple( "putObject mean rate", NumberFormatter.formatNumber(metrics_service.getMeterV2PutObject(shard).getMeanRate(), getSessionUser().getLocale())+ " <span class=\"ago\">req/sec</span>" ));
			data.add(new Tuple( "getObject mean rate", NumberFormatter.formatNumber(metrics_service.getMeterV2GetObject(shard).getMeanRate(), getSessionUser().getLocale())+ " <span class=\"ago\">req/sec</span>" ));
			
			DomainMetricsService doms = ServiceLocator.getService(DomainMetricsService.class);
			
			data.add(new Tuple( "Total Resources",  NumberFormatter.formatNumber(doms.getTotalResources(KBFSStorageType.Minio, fsv2.getShard()))));
			data.add(new Tuple( "Total Hard Disk", ServiceLocator.getService(DateTimeService.class).formatFileSize(
			        doms.getTotalHardDisk(KBFSStorageType.Minio, fsv2.getShard()) +
			        doms.getTotalHardDisk(KBFSStorageType.Odilon, fsv2.getShard()) +
			        doms.getTotalHardDisk(KBFSStorageType.MinioArchive, fsv2.getShard()), 
					getSessionUser().getLocale(), "ago")));
			
			try {
				DateTimeService service = ServiceLocator.getService(DateTimeService.class);
				User user = getSessionUser();
				String zid = service.getMapZoneIds().get(user.getTimeZone());
				if (zid==null)
						zid=ZoneId.systemDefault().getId();
				long time_measure = doms.getTimeMeasure(getDomain());
				OffsetDateTime date = OffsetDateTime.ofInstant( Instant.ofEpochMilli(time_measure), ZoneId.of(zid));
				
				String xs=ServiceLocator.getService(DateTimeService.class).timeElapsed(date, ZoneId.of(zid), user.getLocale(), DateTimeService.DATE_COLlOQUIAL_AGO, "ago");
				data.add(new Tuple( "Time  measured ", xs));
			} catch (Exception e) {
				data.add(new Tuple("Time  measured ", e.getClass().getName() + " | " + e.getMessage()));
			}
			
			
		} catch (Exception e) {
			logger.error(e);
			data.add(new Tuple( "Error",  e.getClass().getName() + " | " + e.getMessage()));
		}
		finally {
			long end=System.currentTimeMillis();
			
			if (logger.isDebugEnabled())
				data.add(new Tuple("Render time ", String.valueOf(end-start)+" <span class=\"ago\">ms</span>"));
		}
		return data;
	}


	
	
	/***
	 * @param fsv2
	 * @return
	 */
	private List<Tuple> OdilonInfo(FileServerOdilon fsv2) {
		
		List<Tuple> data = new ArrayList<Tuple>();
		
		long start = System.currentTimeMillis();
		
		try {
			
			SystemMetricsService metrics_service = ServiceLocator.getService(SystemMetricsService.class);
			
			String shard=fsv2.getShard().toString();
			
			double p1m 	= metrics_service.getMeterOdilonShardPutObject(shard).getOneMinuteRate();
			double p5m 	= metrics_service.getMeterOdilonShardPutObject(shard).getFiveMinuteRate();
			double p15m = metrics_service.getMeterOdilonShardPutObject(shard).getFifteenMinuteRate();
			long pc 	= metrics_service.getMeterOdilonShardPutObject(shard).getCount();
		
			double g1m  = metrics_service.getMeterOdilonShardGetObject(shard).getOneMinuteRate();
			double g5m  = metrics_service.getMeterOdilonShardGetObject(shard).getFiveMinuteRate();
			double g15m = metrics_service.getMeterOdilonShardGetObject(shard).getFifteenMinuteRate();
			long gc     = metrics_service.getMeterOdilonShardGetObject(shard).getCount();
			
			
			if (this.odilon_enabled) {
				String pg=fsv2.ping();
				if (pg==null)
					 pg="err";
				data.add(new Tuple( "Status", "enabled"));
				boolean isok = pg.toLowerCase().equals("ok");
				String s="<div class= \" "+ (isok?"success":"danger") +"\" />"+pg+"</div>";
				data.add(new Tuple( "Ping", s));
			} else {
				data.add(new Tuple( "Ping", "Odilon not enabled"));
				data.add(new Tuple( "Status", "Odilon not enabled"));
			}
			
			if (fsv2!=null && (fsv2 instanceof KbeeMinioFileServer)) {
				data.add(new Tuple( "Fsid",		 	fsv2.getFSId()));
				data.add(new Tuple( "Endpoint",  	fsv2.getEndPoint()));
				data.add(new Tuple( "Access Key", 	fsv2.getAccessKey()));
				data.add(new Tuple( "Secret Key", 	fsv2.getSecretKey()));
			}
			
			data.add(new Tuple( "Shard", 		String.valueOf(fsv2.getShard())));
			data.add(new Tuple( "Probability", 	String.format("%6.2f",fsv2.getProbability())));
			data.add(new Tuple( "Read only", 	fsv2.isReadOnly() ? "yes" : "no"));
			
			String rate_p = 	NumberFormatter.formatNumber(p1m) + "<span class=\"separator\">|</span>" +
								NumberFormatter.formatNumber(p5m) + "<span class=\"separator\">|</span>" +
								NumberFormatter.formatNumber(p15m);
	
			String rate_g =		NumberFormatter.formatNumber(g1m) + "<span class=\"separator\">|</span>" +
								NumberFormatter.formatNumber(g5m) + "<span class=\"separator\">|</span>" +
								NumberFormatter.formatNumber(g15m); 
	
			data.add(new Tuple( "putObject reqs/sec (1m 5m 15m) ",  rate_p));
			data.add(new Tuple( "getObject reqs/sec (1m 5m 15m) ",  rate_g));
			
			data.add(new Tuple( "putObject Total", String.valueOf(pc)));
			data.add(new Tuple( "getObject Total", String.valueOf(gc)));
																															
			data.add(new Tuple( "putObject mean rate", NumberFormatter.formatNumber(metrics_service.getMeterV2PutObject(shard).getMeanRate(), getSessionUser().getLocale())+ " <span class=\"ago\">req/sec</span>" ));
			data.add(new Tuple( "getObject mean rate", NumberFormatter.formatNumber(metrics_service.getMeterV2GetObject(shard).getMeanRate(), getSessionUser().getLocale())+ " <span class=\"ago\">req/sec</span>" ));
			
			DomainMetricsService doms = ServiceLocator.getService(DomainMetricsService.class);
			
			data.add(new Tuple( "Total Resources",  NumberFormatter.formatNumber(doms.getTotalResources(KBFSStorageType.Odilon, fsv2.getShard()))));
			data.add(new Tuple( "Total Hard Disk", 							
			ServiceLocator.getService(DateTimeService.class).formatFileSize(doms.getTotalHardDisk(KBFSStorageType.Odilon, fsv2.getShard()) + doms.getTotalHardDisk(KBFSStorageType.MinioArchive, fsv2.getShard()), 
					getSessionUser().getLocale(), "ago")));
			
			try {
				DateTimeService service = ServiceLocator.getService(DateTimeService.class);
				User user = getSessionUser();
				String zid = service.getMapZoneIds().get(user.getTimeZone());
				if (zid==null)
						zid=ZoneId.systemDefault().getId();
				long time_measure = doms.getTimeMeasure(getDomain());
				OffsetDateTime date = OffsetDateTime.ofInstant( Instant.ofEpochMilli(time_measure), ZoneId.of(zid));
				
				String xs=ServiceLocator.getService(DateTimeService.class).timeElapsed(date, ZoneId.of(zid), user.getLocale(), DateTimeService.DATE_COLlOQUIAL_AGO, "ago");
				data.add(new Tuple( "Time measured ", xs));
			} catch (Exception e) {
				data.add(new Tuple("Time measured ", e.getClass().getName() + " | " + e.getMessage()));
			}
			
			try {
				fsv2.getInfo().forEach((k,v) -> data.add(new Tuple(k, v))); 
			} catch (Exception e) {
				data.add(new Tuple("Time measured ", e.getClass().getName() + " | " + e.getMessage()));
			}

			
		} catch (Exception e) {
			logger.error(e);
			data.add(new Tuple( "Error",  e.getClass().getName() + " | " + e.getMessage()));
		}
		finally {
			long end=System.currentTimeMillis();
			
			if (logger.isDebugEnabled())
				data.add(new Tuple("Render time ", String.valueOf(end-start)+" <span class=\"ago\">ms</span>"));
		}
		return data;
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	private List<Tuple> KBFSGatewayInfo() {
		
		long start = System.currentTimeMillis();
		
		List<Tuple> data = new ArrayList<Tuple>();
		
		try {
			SystemMetricsService metrics_service = ServiceLocator.getService(SystemMetricsService.class);
			
			double g1m  = metrics_service.getMeterExternalGetObject().getOneMinuteRate();
			double g5m  = metrics_service.getMeterExternalGetObject().getFiveMinuteRate();
			double g15m = metrics_service.getMeterExternalGetObject().getFifteenMinuteRate();
			long 	 gc = metrics_service.getMeterExternalGetObject().getCount();
			
			String rate_g =	NumberFormatter.formatNumber(g1m) + "<span class=\"separator\">|</span>" +
							NumberFormatter.formatNumber(g5m) + "<span class=\"separator\">|</span>" +
							NumberFormatter.formatNumber(g15m); 
	
			data.add(new Tuple( "getObject reqs/sec (1m 5m 15m) ",  rate_g));
			data.add(new Tuple( "getObject Total",  String.valueOf(gc)));
													
			DomainMetricsService doms = ServiceLocator.getService(DomainMetricsService.class);

			data.add(new Tuple( "Total Hard Disk (Gateway)",  NumberFormatter.formatFileSize( doms.getTotalHardDisk(KBFSStorageType.External)))); // TODO
			data.add(new Tuple( "Total Files (Gateway)",  NumberFormatter.formatNumber(doms.getTotalResources(KBFSStorageType.External))));
			
			try {
				DateTimeService service = ServiceLocator.getService(DateTimeService.class);
				User user = getSessionUser();
				String zid = service.getMapZoneIds().get(user.getTimeZone());
				if (zid==null)
						zid=ZoneId.systemDefault().getId();
				long time_measure = doms.getTimeMeasure(getDomain());
				OffsetDateTime date = OffsetDateTime.ofInstant( Instant.ofEpochMilli(time_measure), ZoneId.of(zid));
				
				String xs=ServiceLocator.getService(DateTimeService.class).timeElapsed(date, ZoneId.of(zid), user.getLocale(), DateTimeService.DATE_COLlOQUIAL_AGO, "ago");
				data.add(new Tuple( "Time  measured ", xs));
			} catch (Exception e) {
				data.add(new Tuple("Time  measured ", e.getClass().getName() + " | " + e.getMessage()));
			}

							
			
		} catch (Exception e) {
			logger.error(e);
			data.add(new Tuple( "Error",  e.getClass().getName() + " | " + e.getMessage()));
		}
		finally {
			long end=System.currentTimeMillis();
			if (logger.isDebugEnabled())
				data.add(new Tuple("Render time ", String.valueOf(end-start)+" <span class=\"ago\">ms</span>"));
		}
		
		
		return data;
	}


	
	/***
	 * 
	 * 
	 * 
	 * 
	 * 
	 */						
	protected List<Tuple> KBFSInfo() {
		
		long start = System.currentTimeMillis();
		List<Tuple> data = new ArrayList<Tuple>();
		
		try {
			
			boolean kbfs1_enabled=PropertiesFactory.getInstance("kbee").getProperties().getProperty("kbfs1.enabled", "yes").toLowerCase().trim().equals("yes");
			boolean kbfs2_enabled=PropertiesFactory.getInstance("kbee").getProperties().getProperty("kbfs2.enabled", "yes").toLowerCase().trim().equals("yes");
			boolean odilon_enabled=PropertiesFactory.getInstance("kbee").getProperties().getProperty("odilon.enabled", "no").toLowerCase().trim().equals("yes");
			
			try {
				KBFSStorageType ty= getDefaultKBFSStorageType();
				data.add(new Tuple( "Default Storage Type ", ty.getLabel()));
				
			} catch (Exception e) {
				logger.error(e);
				KBFSStorageType ty= getDefaultKBFSStorageType();
				if (ty!=null)
					data.add(new Tuple( "Default Storage Type ", ty.getLabel()));
			}

			
			try {
				
				FileServerS3 s3 = ServiceLocator.getService(FileServerS3.class);
				
				if (s3!=null && s3.isEnabled()) {
					String pg=s3.ping();
					if (pg==null)
						 pg="err";
					data.add(new Tuple( "Status", "enabled"));
					boolean isok = pg.toLowerCase().equals("ok");
					String s="<div class= \" "+ (isok?"success":"danger") +"\" />"+pg+"</div>";
					data.add(new Tuple( "Amazon S3 Ping", s));
				} else {
					data.add(new Tuple( "Amazon  S3 Ping", "Amazon  S3 not enabled"));
				}
				
				
				// KBFS 1 ---------------------------------------------------------------------
				//
				if (kbfs1_enabled) {
					String pg=ServiceLocator.getService(FileServerV1.class).ping();
					if (pg==null)
						 pg="err";
					boolean isok = pg.toLowerCase().equals("ok");
					String s="<div class= \" "+ (isok?"success":"danger") +"\" />"+pg+"</div>";
					data.add(new Tuple("File System (ping)", s));
				}
				else
					data.add(new Tuple("File System", "disabled"));

				
				// KBFS 2 ---------------------------------------------------------------------
				//
				if (kbfs2_enabled) {

					FileServerMinio fsv2=ServiceLocator.getService(FileServerMinio.class);
					
					if (fsv2 instanceof KbeeShardedMinioFileServer) {
						String sm_pg=fsv2.ping();
						if (sm_pg==null)
							sm_pg="err";
						boolean sm_isok = sm_pg.toLowerCase().equals("ok");
						String sm_s="<div class= \" "+ (sm_isok?"success":"danger") +"\" />"+sm_pg+"</div>";
						data.add(new Tuple( "Minio Shard Manager (ping)", sm_s));
						try {
							for (Entry<Integer, FileServerMinio> entry: ((KbeeShardedMinioFileServer) fsv2).getShards().entrySet()) {
								String p = entry.getValue().ping();
								if (p==null)
									p="err";
								boolean p_isok = p.toLowerCase().equals("ok");
								String px="<div class= \" "+ (p_isok?"success":"danger") +"\" />"+p+"</div>";
								data.add(new Tuple( "Minio_"+ String.valueOf(entry.getKey().intValue())+ " (ping)", px));
							}
						} catch (Exception e) {
							logger.error(e);
							data.add(new Tuple( "Error",  e.getClass().getName()));
						}
						try {
							for (Entry<Integer, FileServerMinio> entry: ((KbeeShardedMinioFileServer) fsv2).getShards().entrySet()) {
								String fsid = entry.getValue().getFSId();
								data.add(new Tuple( "Minio_"+ String.valueOf(entry.getKey().intValue())+" endpoint", 
										
										
										"<a href=\""+ entry.getValue().getEndPoint() +"\" target=\"_blank\">" + 
												entry.getValue().getEndPoint() +"</a>" +
										
										" <span class=\"ago\">(prob: "+ NumberFormatter.formatNumber(entry.getValue().getProbability()) + ") </span>"));
								data.add(new Tuple( "Minio_"+ String.valueOf(entry.getKey().intValue())+" FSId", fsid));
							}
							
						} catch (Exception e) {
							logger.error(e);
							data.add(new Tuple( "Error",  e.getClass().getName()));
						}	
					}
					else {
						
						String sm_pg=fsv2.ping();
						if (sm_pg==null)
							sm_pg="err";
						boolean sm_isok = sm_pg.toLowerCase().equals("ok");
						String sm_s="<div class= \" "+ (sm_isok?"success":"danger") +"\" />"+sm_pg+"</div>";
						data.add(new Tuple( "Minio Ping", sm_s));
					}
				}
				else
					data.add(new Tuple("Minio", "disabled"));
			} catch (Exception e) {
				logger.error(e);
				data.add(new Tuple( "Error",  e.getClass().getName()));
			}
			

			
			
			// Odilon ---------------------------------------------------------------------
			//
			try {
				if (odilon_enabled) {
	
					FileServerOdilon fsodilon=ServiceLocator.getService(FileServerOdilon.class);
					
					if (fsodilon instanceof KbeeShardedOdilonFileServer) {
						String sm_pg=fsodilon.ping();
						if (sm_pg==null)
							sm_pg="err";
						boolean sm_isok = sm_pg.toLowerCase().equals("ok");
						String sm_s="<div class= \" "+ (sm_isok?"success":"danger") +"\" />"+sm_pg+"</div>";
						data.add(new Tuple( "Odilon Shard Manager (ping)", sm_s));
						try {
							for (Entry<Integer, FileServerOdilon> entry: ((KbeeShardedOdilonFileServer) fsodilon).getShards().entrySet()) {
								String p = entry.getValue().ping();
								if (p==null)
									p="err";
								boolean p_isok = p.toLowerCase().equals("ok");
								String px="<div class= \" "+ (p_isok?"success":"danger") +"\" />"+p+"</div>";
								data.add(new Tuple( "Odilon_"+ String.valueOf(entry.getKey().intValue())+ " (ping)", px));
							}
						} catch (Exception e) {
							logger.error(e);
							data.add(new Tuple( "Error",  e.getClass().getName()));
						}
						try {
							for (Entry<Integer, FileServerOdilon> entry: ((KbeeShardedOdilonFileServer) fsodilon).getShards().entrySet()) {
								String fsid = entry.getValue().getFSId();
								data.add(new Tuple( "Odilon_"+ String.valueOf(entry.getKey().intValue())+" endpoint", 
										
										
										"<a href=\""+ entry.getValue().getEndPoint() +"\" target=\"_blank\">" + 
												entry.getValue().getEndPoint() +"</a>" +
										
										" <span class=\"ago\">(prob: "+ NumberFormatter.formatNumber(entry.getValue().getProbability()) + ") </span>"));
								data.add(new Tuple( "Odilon_"+ String.valueOf(entry.getKey().intValue())+" FSId", fsid));
							}
							
						} catch (Exception e) {
							logger.error(e);
							data.add(new Tuple( "Error",  e.getClass().getName()));
						}	
					}
					else {
						
						String sm_pg=fsodilon.ping();
						if (sm_pg==null)
							sm_pg="err";
						boolean sm_isok = sm_pg.toLowerCase().equals("ok");
						String sm_s="<div class= \" "+ (sm_isok?"success":"danger") +"\" />"+sm_pg+"</div>";
						data.add(new Tuple( "Odilon Ping", sm_s));
					}
				}
				else
					data.add(new Tuple("Odilon", "disabled"));
			} catch (Exception e) {
				logger.error(e);
				data.add(new Tuple( "Error",  e.getClass().getName()));
			}


			// -------------
			
			try {
				DomainMetricsService doms = ServiceLocator.getService(DomainMetricsService.class);
				DateTimeService service = ServiceLocator.getService(DateTimeService.class);
				User user = getSessionUser();
				String zid = service.getMapZoneIds().get(user.getTimeZone());
				if (zid==null)
						zid=ZoneId.systemDefault().getId();
				long time_measure = doms.getTimeMeasure(getDomain());
				OffsetDateTime date = OffsetDateTime.ofInstant( Instant.ofEpochMilli(time_measure), ZoneId.of(zid));
				String s=ServiceLocator.getService(DateTimeService.class).timeElapsed(date, ZoneId.of(zid), user.getLocale(), DateTimeService.DATE_COLlOQUIAL_AGO, "ago");
				data.add(new Tuple( "Time  measured", s));

			} catch (Exception e) {
				logger.error(e);
				data.add(new Tuple( "Error",  e.getClass().getName()));
			}	
			
		}
			finally {
			long end = System.currentTimeMillis();
			
			if (logger.isDebugEnabled())
				data.add(new Tuple("Render time ", String.valueOf(end-start)+" <span class=\"ago\">ms</span>"));
			logger.debug("File Service time " + String.valueOf(end-start)+" ms");
			}
			return data;
		}


	
	/***
	 * @return
	 */
	private List<Tuple> resourcesInfo() {

		long start = System.currentTimeMillis();
		
		List<Tuple> data = new ArrayList<Tuple>();
		
		SystemMetricsService metrics_service = ServiceLocator.getService(SystemMetricsService.class);

		DomainMetricsService doms = ServiceLocator.getService(DomainMetricsService.class);
		
		try {																
			
			data.add(new Tuple( "Resources Console", 	"<a target=\"_blank\" class=\"btnn-link\" href=\"/logs/resources\">/logs/resources</a>"));
			
			data.add(new Tuple( "Total Resources", 					   			NumberFormatter.formatNumber(doms.getTotalResources(), getSessionUser().getLocale())));
			data.add(new Tuple( "Total Resources File System",		 		    	NumberFormatter.formatNumber(doms.getTotalResources(KBFSStorageType.KBFS1), getSessionUser().getLocale())));
            data.add(new Tuple( "Total Resources Odilon",                       NumberFormatter.formatNumber(doms.getTotalResources(KBFSStorageType.Odilon), getSessionUser().getLocale())));
			
			data.add(new Tuple( "Total Resources Minio", 				    	NumberFormatter.formatNumber(doms.getTotalResources(KBFSStorageType.Minio), getSessionUser().getLocale())));
			data.add(new Tuple( "Total Resources Minio Archive", 		    	NumberFormatter.formatNumber(doms.getTotalResources(KBFSStorageType.MinioArchive), getSessionUser().getLocale())));
			
			data.add(new Tuple( "Total Resources Gateway",		 				NumberFormatter.formatNumber(doms.getTotalResources(KBFSStorageType.External), getSessionUser().getLocale())));
			data.add(new Tuple( "Total Resources Encrypted",	 				NumberFormatter.formatNumber(getContentDao().getTotalEncryptedResources(), getSessionUser().getLocale())));
			
			data.add(new Tuple( "Total Hard Disk Stored (KBFS1/2/Archive)",		NumberFormatter.formatFileSize(doms.getTotalStoredHardDisk(), getSessionUser().getLocale(), "ago")));
			data.add(new Tuple( "Total Hard Disk Gateway",		 				NumberFormatter.formatFileSize(doms.getTotalHardDisk(KBFSStorageType.External), getSessionUser().getLocale(), "ago")));
			
		} catch (Exception e) {
			logger.error(e);
			data.add(new Tuple( "Error",  e.getClass().getName()));
		}

		try {
			String login=String.format("%12.2f  <span class=\"separator\">|</span>   %12.2f  <span class=\"separator\">|</span>   %12.2f", 
					  metrics_service.getMeterLogin().getOneMinuteRate() * 60,
					  metrics_service.getMeterLogin().getFiveMinuteRate() * 60,
					  metrics_service.getMeterLogin().getFifteenMinuteRate() * 60);	
												
			String wp=String.format("%12.2f  <span class=\"separator\">|</span>   %12.2f   <span class=\"separator\">|</span>   %12.2f", 
					  metrics_service.getMeterWebPages().getOneMinuteRate() * 60,
					  metrics_service.getMeterWebPages().getFiveMinuteRate() * 60,
					  metrics_service.getMeterWebPages().getFifteenMinuteRate() * 60);	
											
			String em=String.format("%12.2f  <span class=\"separator\">|</span>   %12.2f   <span class=\"separator\">|</span>   %12.2f", 
					  metrics_service.getMeterEmails().getOneMinuteRate() * 60,
					  metrics_service.getMeterEmails().getFiveMinuteRate() * 60,
					  metrics_service.getMeterEmails().getFifteenMinuteRate() * 60);	
	
			data.add(new Tuple("Active users", String.valueOf(metrics_service.getCounterUsersLogged().getCount())));
	
			data.add(new Tuple("Login/min (1m 5m 15m)", login));
			data.add(new Tuple("Emails/min (1m 5m 15m)", em));
			data.add(new Tuple("Webpages/min (1m 5m 15m)", wp));
			
			try {
				DateTimeService service = ServiceLocator.getService(DateTimeService.class);
				User user = getSessionUser();
				String zid = service.getMapZoneIds().get(user.getTimeZone());
				if (zid==null)
						zid=ZoneId.systemDefault().getId();
				long time_measure = doms.getTimeMeasure(getDomain());
				OffsetDateTime date = OffsetDateTime.ofInstant( Instant.ofEpochMilli(time_measure), ZoneId.of(zid));
				String s=ServiceLocator.getService(DateTimeService.class).timeElapsed(date, ZoneId.of(zid), user.getLocale(), DateTimeService.DATE_COLlOQUIAL_AGO, "ago");
				data.add(new Tuple( "Time  measured ", s));
			} catch (Exception e) {
				logger.error(e);
				data.add(new Tuple( "Error",  e.getClass().getName()));
			}	


		} catch (Exception e) {
			logger.error(e);
			data.add(new Tuple( "Error",  e.getClass().getName()));
		}
		finally {
		
			long end=System.currentTimeMillis();
			
			if (logger.isDebugEnabled())
				data.add(new Tuple("Render time ", String.valueOf(end-start)+" <span class=\"ago\">ms</span>"));
		}
  		return data;
  		
	}
	

}
