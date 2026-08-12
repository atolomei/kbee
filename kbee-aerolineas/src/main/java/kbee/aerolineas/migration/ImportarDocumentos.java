package kbee.aerolineas.migration;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;


import org.apache.logging.log4j.LogManager;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.wicket.util.file.File;
import org.apache.wicket.util.io.IOUtils;
import org.apache.xerces.xni.parser.XMLDocumentFilter;
import org.ccil.cowan.tagsoup.Parser;
import org.cyberneko.html.HTMLConfiguration;

import org.hibernate.SessionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.api.client.util.Charsets;
import com.google.common.io.CharStreams;
import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.base.ContentResource;
import com.novamens.content.base.CustomAttribute;
import com.novamens.content.base.Relation;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.base.ResourceFolder;
import com.novamens.content.base.ResourceNode;
import com.novamens.content.base.ResourceTag;
import com.novamens.content.base.Source;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.AttributeType;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.Multiplicity;
import com.novamens.content.model.ObjectId;
import com.novamens.content.model.PersonMember;
import com.novamens.content.model.RelationTemplate;
import com.novamens.content.resource.KBFile;
import com.novamens.content.service.ContentFactoryService;
import com.novamens.content.service.ContentService;
import com.novamens.content.service.DOMObjectService;
import com.novamens.content.service.kbfs.KBFSResourceService;
import com.novamens.content.tree.TreeNode;
import com.novamens.content.tree.TreeService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.dom.Versionable;
import com.novamens.indexer.iql.IllegalArgumentException;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.query.TextQuery;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.base.KbeeContent;
import com.novamens.kbee.content.base.KbeeCustomAttribute;
import com.novamens.kbee.content.base.KbeeResourceContainer;
import com.novamens.kbee.content.command.AsyncCommand;
import com.novamens.kbee.content.document.KbeeIDoc;
import com.novamens.kbee.content.model.KbeeRelation;
import com.novamens.kbee.content.resource.KBFileImpl;
import com.novamens.kbee.content.resource.KbeeResourceNode;
import com.novamens.kbee.content.text.ElementRemover;
import com.novamens.kbee.idoc.webapi.client.Base64;
import com.novamens.kbee.kbfs.LengthAwareInputStreamWrapper;
import com.novamens.kbee.lock.LockTransactionSynchronization;
import com.novamens.kbfs.FileServerException;
import com.novamens.lock.ValueLockerService;
import com.novamens.logging.LogDao;
import com.novamens.logging.UpdateEvent;
import com.novamens.repository.DomRepository;
import com.novamens.repository.DomRepositoryService;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;
import com.novamens.solr.indexer.query.SolrParametersQuery;
import com.novamens.system.parameters.SystemParameterService;
import com.novamens.transaction.Transaction;
import com.novamens.util.KbeeRuntimeException;

import io.odilon.util.FileNameNormalizer;
import kbee.api.model.ApiFile;
import kbee.api.model.ApiProxy;
import kbee.api.model.ApiValue;
import kbee.api.model.ApiAttributeProxy;
import kbee.api.model.IAttributeValues;
import kbee.api.model.ApiClassificable;
import kbee.api.model.ICustomAttributeValue;
import kbee.api.service.ApiError;
import kbee.api.service.ApiException;
import kbee.api.model.ApiResource;
import kbee.util.PropertiesFactory;
import kbee.util.logging.Logger;


public class ImportarDocumentos extends AsyncCommand {
	
	private static Logger logger = new Logger(LogManager.getLogger(ImportarDocumentos.class.getName()));
	 
	private static String[] fields = { "id", "documento", 
			"tipo", "descripcion", "empresa", 
			"carpetas", "revision", "revision-fecha", 
			"creacion-fecha", "autor", "modificacion", 
			"parte", "partes", 
			"vigencia-desde", "vigencia-hasta", 
			"version", "permisos", "files",  "index", "relations", "uri"
	};
	
	private List<Map<String, String>> rows = null;
	private int i = 0;
	private int updates=0;
	private Map<String, String> lastrow;
	private String output;
	private String path = "sistemas";
	private String type = null;	
	private String rowid = null; 
	private String bcvurl;
	private String bcvuser;
	private String bcvpassword;
	private String logsPath;
	private String onlymeta = null;
	private Map<String, Integer> foldersMap = new HashMap<>();
	
	BcvService bcvService;

	public ImportarDocumentos() {
		setName("Importar Documentos AA");
	}
	
	public static Map<String, String> values(String... values) {
		Map<String, String> map = new HashMap<String, String>();
		String key = null, value = null;
		for (int v=0; v<values.length; v++) {
			if (key==null) {
				key = values[v];
			}
			else {
				value = values[v];
				map.put(key, value);
				key=null;
			}
		}
		return map;
	}

	public void executeAsync() {
		

		
		try {
			
			
			String lote = (String)getParameter("lote");
			path = (String)getParameter("path");
			type = (String)getParameter("type");
			rowid = (String)getParameter("rowid");
			output = (String)getParameter("output");
			onlymeta = (String)getParameter("onlymeta");
			long limit = getParameter("limit")!=null
					? Long.valueOf((String)getParameter("limit"))
					: Long.MAX_VALUE;
			if (lote==null) {
				setResultComments("el parámetro lote es obligatorio");
				throw new RuntimeException("sin lote");
			}
			
	 	 	bcvurl = PropertiesFactory.getInstance("kbee").getProperties().getProperty("aerolineas.bcv.url", "http://localhost:8116").trim();
	 	 	bcvuser = PropertiesFactory.getInstance("kbee").getProperties().getProperty("aerolineas.bcv.user", "root").trim();
	 	 	bcvpassword = PropertiesFactory.getInstance("kbee").getProperties().getProperty("aerolineas.bcv.password", "..resurrecto$").trim();
	 		logsPath = PropertiesFactory.getInstance("kbee").getProperties().getProperty("aerolineas.logs", "logs").trim();
	 		
	 		
			setLogger(getLoggerName(lote));
	 		
			com.novamens.hibernate.session.Session.setApi(true);
			com.novamens.hibernate.session.Session.open();
			ServiceLocator.getService(SecurityService.class).authenticate("root@aerolineas");
		
			
			String id = null;
			List<ApiFile> history = null;
			for (i=0; i<getRows().size() && updates<limit; i++) {
				
				Map<String, String> row = getRows().get(i);
				
				if (lastrow!=null && row.get("id").equals(lastrow.get("id"))) {
					row.put("carpetas", lastrow.get("carpetas"));
				}
				
				lastrow = row;
				
				ApiFile file = buildFile(getRows().get(i));
				
				if (!"Reporte".equals(output) && !"Folders".equals(output)) {	
					getLogger().info(file.getExternalId() + ", start proc");
				}
				
				boolean ok = true;
				
				if (file.getClassName()==null) {
					String version = row.get("version");
					getLogger().info(file.getExternalId() + ", ERROR, sin clase ("+version+")");
					ok=false;
				}
				else
				if (file.getLastModifiedUser()==null) {
					String autor = row.get("autor");
					String version = row.get("version");
					if (autor!=null) autor = autor.replace(",", " ");
					getLogger().info(file.getExternalId() + ", ERROR, sin usuario " + autor + " ("+version+")");
					ok=false;
				}
				else
				if (file.getClassName()==null) {
					String version = row.get("version");
					getLogger().info(file.getExternalId() + ", ERROR, sin clase ("+version+")");
					ok=false;
				}

				
				if (ok && enable(file)) {	
					id = file.getExternalId();
					try {
						ServiceLocator.getService(ValueLockerService.class).lock(id);
						String nextid = i<getRows().size()-1 ? getRows().get(i+1).get("id") : null;
						if (history==null) 
							history = new ArrayList<ApiFile>();
						history.add(file);
						if (nextid!=null && !nextid.equals(id)) {
							processHistory(history);
							history = null;
						}
					}
					finally {
						ServiceLocator.getService(ValueLockerService.class).unlock(id);
					}
				}
				else {
					if (!enable(file)) {
						delete(file);
						getLogger().info(file.getExternalId() + ", WARN, no in path");
					}
					else {
						getLogger().info(file.getExternalId() + ", WARN, no OK");
					}
				}
			}

			if (history!=null) {
				processHistory(history);
			}
			getLogger().info("END CON " + i + "/"+ getRows().size());
			
			end();
			rows = null;
		}
		catch (Exception e) {
			e.printStackTrace();
			getLogger().error(e);
			logger.error(e);
			getLogger().info("END CON " + e.getMessage());
			logger.error(e);
			stop();
		}
	}
	
	private void processHistory(List<ApiFile> history) {
		if ("Reporte".equals(output)) {
			printHistory(history);
		}
		else {
			if ("Folders".equals(output)) {
				printFolders(history);
			}
			else {
				if ("History".equals(output)) {
					List<ApiFile> rebuild = rebuild(history);
					if (rebuild==null) {
						getLogger().info(history.get(0).getExternalId()+", ERROR NULL");
					}
					else {
						if (rebuild.size()!=history.size()) {
							getLogger().info(history.get(0).getExternalId()+", ERROR SIZE");
							updateHistory(rebuild);
						}
						else {
							getLogger().info(history.get(0).getExternalId()+", ok");
						}
					}
				}
				else {
					updateHistory(history);
				}	
			}
		}
	}
	
	
	private List<ApiFile> rebuild(List<ApiFile> history) {
		try {
		ApiFile head = history.get(0);
		List<ApiFile> remote = getBcvService().getHistory(head.getControlAttributeValue("uri"));
		return remote;
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return null;
		
	}
	
	private BcvService getBcvService() {
		if (bcvService == null) {
			bcvService = BcvService.builder()
			.url(bcvurl)
			.user(bcvuser)
			.password(bcvpassword)
			.logger(getLogger())
			.build();
		}
		return bcvService;
	}
	
	
	public boolean enable(ApiFile file) {
		try {
			for (String folder : getFolders(file)) {
				if (folder.startsWith("BCV/Papelera")) {
					return false;
				}
			}
			if (path==null) return true;
			for (String folder : getFolders(file)) {
				if (folder.startsWith("Papelera")) {
					return false;
				}
				if (folder.toLowerCase().contains(path.toLowerCase())) {
					return true;
				}	
			}
		}
		catch (Exception e) {
			getLogger().info(file.getExternalId() + ", ERROR, "+e.getMessage());
			logger.error(e);
		}
		return false;
	}
	
	@Override
	public double getProgress() {
		return (double) i/(double) getTotalItems() * 100;
	}

	@Override
	public long getTotalItems() {
		return getRows().size();
		
	}
	
	@Override
	public long getTotalItemsProcessed() {
		return i;
	}
	
	private ApiFile buildFile(Map<String,String> row) {
		
		
		if ("IAP".equals(row.get("tipo"))) {
			System.out.println("iap");
		}
		
		if ("Tecnico".equals(row.get("tipo"))) {
			System.out.println("TEC");
		}

		
		ApiFile file = new ApiFile();
		
		String id = row.get("id");
		String folders = row.get("carpetas");
		String title = row.get("documento");
		String revision = row.get("revision");
		String fecharevision = row.get("revision-fecha");
		String username = row.get("autor");

		String parte = row.get("parte");
		String partesvalue = row.get("partes");
		List<String> partes = new ArrayList<String>();
		if (partesvalue!=null) {
			String values[] = partesvalue.split(",");
			for (int v=0; v<values.length; v++) {
				partes.add(values[v].trim());
			}
		}
		List<String> relateds = new ArrayList<String>();
		String relationsvalue = row.get("relations");
		if (relationsvalue!=null) {
			String values[] = relationsvalue.split(",");
			for (int v=0; v<values.length; v++) {
				relateds.add(values[v].trim());
			}
		}
		String modified = row.get("modificacion") + " 00:00:00";
		String validityfrom = row.get("vigencia-desde");
		String validityto = row.get("vigencia-hasta");
		String description = row.get("descripcion");
		if (description!=null) {
			if (description.contains("|n")) {
				System.out.println(description);
			}
			description = description.replace("|n", "</br>");
		}
		String versionvalue = row.get("version");
		int version = versionvalue!=null && !"".equals(versionvalue) ? Integer.valueOf(row.get("version")) : -1;
		String files = row.get("files");
		String template = null;
		
		file.setExternalId(id);
		
		template = getTemplate(row);
		file.setClassName(template);
		if (template==null) {
			return file;
		}
		
		file.setLastModifiedUser(new ApiProxy(username,null));



		
		
		LocalDateTime lastmodified = null;
		try {
			if (modified.charAt(1)=='/') modified = "0"+modified;
			lastmodified = LocalDateTime.parse(modified, DateTimeFormatter.ofPattern("dd/M/yyyy HH:mm:ss"));
			file.setLastModifiedDate(OffsetDateTime.of(lastmodified, OffsetDateTime.now().getOffset()));
		}
		catch (Exception e) {
			file.setLastModifiedDate(OffsetDateTime.now());
			e.printStackTrace();
		}

		file.setTitle(title);
		if (version>=0)
		file.setVersion(version);
		file.setApplication("bcv");
		
		if (revision!=null && !"".equals(revision) && !file.getClassName().equals("Registro")) {
			file.setAttribute("Número Revisión", revision);
		}	
		
		LocalDateTime fecharevisiontime = null;
		if (fecharevision!=null && !"".equals(fecharevision) && !"-".equals(fecharevision)) {
			try {
				if (fecharevision.charAt(1)=='/') fecharevision = "0"+fecharevision;
				fecharevisiontime = LocalDateTime.parse(fecharevision + " 00:00:00", DateTimeFormatter.ofPattern("dd/M/yyyy HH:mm:ss"));
				String value = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(fecharevisiontime);
				if (file.getClassName().equals("Registro")) 
					file.setAttribute("Fecha", value);
				else
					file.setAttribute("Fecha Revisión", value);
					
			}
			catch (DateTimeException e) {
				e.printStackTrace();
			}
		}
		
		if (file.getClassName().equals("Registro") && file.getAttributeValue("Fecha")==null) {
			try {
				String value = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(lastmodified);
				file.setAttribute("Fecha", value);
			}
			catch (DateTimeException e) {
				e.printStackTrace();
			}
		}
		
		if (validityfrom!=null && !"".equals(validityfrom)) {
			try {
				LocalDateTime time = LocalDateTime.parse(validityfrom+ " 00:00:00", DateTimeFormatter.ofPattern("d/M/yyyy HH:mm:ss"));
				String value = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(time);
				file.setAttribute("Visualización Inicial", value);
			}
			catch (DateTimeException e) {
				e.printStackTrace();
			}
		}

		if (validityto!=null && !"".equals(validityto)) {
			try {
				LocalDateTime time = LocalDateTime.parse(validityto+ " 00:00:00", DateTimeFormatter.ofPattern("d/M/yyyy HH:mm:ss"));
				String value = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(time);
				file.setAttribute("Visualización Final", value);
			}
			catch (DateTimeException e) {
				e.printStackTrace();
			}
		}

		if (parte!=null && !"".equals(parte)) {
			file.setAttribute("numero", parte);
			file.setAttribute("codigo", parte);
			file.setControlAttribute("fixed", "true");
		}	
		
		if (!"URL".equals(row.get("tipo"))) {
			if (!partes.isEmpty()) {
				for (String value : partes) {
					if (!"".equals(value)) {
						file.setAttribute("Parte", value);
					}	
				}
			}
		}
		
		file.setAttribute("estado", "Final");
		String tipo = row.get("tipo");
		if ("Embarque".equals(tipo)) tipo ="Documento";
		if ("Tecnico".equals(tipo)) tipo ="Documento Técnico";
		if ("Documento".equals(tipo)) tipo = "Documento Corporativo";
		file.setAttribute("tipodocumento", tipo);
		file.setCustomAttribute("carpetas", folders);
		if ("IAP".equals(row.get("tipo"))) {
			file.setAttribute("numero", title);
		}	
		else {	
			file.setAttribute("titulo", title);
		}
		file.setAttribute("descripcion", description);
		file.setControlAttribute("descripcion", description);
		file.setControlAttribute("creation", row.get("creacion-fecha"));
		file.setControlAttribute("uri", row.get("uri"));
		file.setControlAttribute("index", row.get("index"));
		file.setControlAttribute("files", files);
		
		String url = "URL".equals(row.get("tipo")) ? row.get("partes") : null;
		if (url!=null) {
			file.setAttribute("url", url);
		}	

		List<ApiProxy> relations = new ArrayList<ApiProxy>();
		for (String related : relateds) {
			if (!"".equals(related) && !"-".equals(related)) {
				ApiProxy relation= new ApiProxy();
				relation.setId(related);
				if ("IAP".equals(row.get("tipo"))) {
					relation.setRel("Documento");
				}
				else {
					relation.setRel("Relacionado");
				}
				relations.add(relation);
			}
		}
		file.setRelationships(relations);
		
		file.setResources(getResources(file));
		
		
		if (file.getAttributeValue("Número Revisión")==null) {
			ApiFile checked = getRemoteFile(file);
			if (checked!=null) {
				file = checked;
			}
		}
		
		return  file;
	}
	
	
	private void updateHistory(List<ApiFile> history) {
		int versioncontext = 0;
		boolean headcontext = false;
		Transaction transaction = null;
		boolean update = false, relationError = false;
		List<Content>  updates = new ArrayList<>() ;
		try {
			transaction = beginTransaction();
			com.novamens.hibernate.session.Session.setApi(true);
			List<Content> versions = new ArrayList<>();

			Content head = getContentDao().findContentByExternalId("bcv", history.get(0).getExternalId());
			if (head!=null) {
				versions.add(head);
				Content version = (Content)((Versionable<?>)head).getPreviousVersion();
				while (version!=null) {
					versions.add(version);
					version = (Content)((Versionable<?>)version).getPreviousVersion();
				}
			}
			int vn=0;
			for (ApiFile version : history) {
				if (vn++==0 || validVersion(version)) {
					int versionnumber = version.getVersion();
					
					versioncontext = versionnumber;
					headcontext = vn==0;
					
					Content content = null;
					for (Content v : versions) {
						if (v.getVersion()==versionnumber) {
							content = v;
							break;
						}
					}
					
					if (content == null)	{
						content = createContent(version);
						if (head==null) {
							if (versions.isEmpty()) {
								head = content;
							}
							else {
								head = versions.get(0);
							}
							
						}
						content.setOId(head.getOId());
					}	
					
					List<String> contentUpdates = update(content, version);
					
					if (!contentUpdates.isEmpty()) {
						update = true;
						this.updates++;
					}
					
					if (contentUpdates.contains("relation error")) {
						relationError = true;
					}
					
					updates.add(content);
				}
			}
			if (update) {
				Content prev = null;
				for (int v=updates.size()-1; v>=0; v--) {
					Content version = updates.get(v);
					KbeeContent kversion = (KbeeContent)getContentDao().reload(version);
					kversion.setHeadVersion(v==0);
					kversion.setPreviousVersion(prev);
					prev = kversion;
					getContentDao().save(kversion);
				}
				for (Content c : versions) {
					boolean found = false;
					for (Content u : updates) {
						if (u.getId().equals(c.getId())) {
							found = true;
							break;
						}
					}
					if (!found) {
						getContentDao().delete(c);
					}
				}
				transaction.commit();
				getLogger().info(history.get(0).getExternalId() + ", (ok meta)");
			}	
			else {
				transaction.rollback();
				getLogger().info(history.get(0).getExternalId() + ", (sin cambios meta)");
			}
		}
		catch (ApiException | IOException e) {
			e.printStackTrace();
			if (!history.isEmpty()) {
				String context = " ("+versioncontext+")";
				if (headcontext) {
					context += "(head)";
				}
				getLogger().info(history.get(0).getExternalId() + context + ", ERROR");
			}
			getLogger().error(e);
			logger.error(e);
			transaction.rollback();
		}
		
		if ("true".equals(onlymeta)) {
			return;
		}
		
		boolean resourcesError = false;
		try {
			List<String> resourcesUpdates = new ArrayList<>();
			int vn=0;
			for (ApiFile version : history) {
				int versionnumber = version.getVersion();
				headcontext = vn++==0;
				versioncontext = versionnumber;
				Content content = null;
				for (Content v : updates) {
					if (v.getVersion()==versionnumber) {
						content = v;
						break;
					}
				}
				if (content!=null) {
					content = (Content)getContentDao().reload(content);
					resourcesUpdates.addAll(updateResources(content, version));
				}
			}
			if (!resourcesUpdates.isEmpty()) {
				getLogger().info(history.get(0).getExternalId() + ", (ok resources)");
			}
			else {
				getLogger().info(history.get(0).getExternalId() + ", (resources sin cambios)");
			}
		}	
		catch (ApiException | IOException e) {
			e.printStackTrace();
			if (!history.isEmpty()) {
				String context = " ("+versioncontext+")";
				if (headcontext) {
					context += "(head)";
				}
				getLogger().info(history.get(0).getExternalId() + context + ", ERROR");
			}
			getLogger().error(e);
			logger.error(e);
			resourcesError = true;
		}
		
	
		boolean lessThanOneSecond = false;
		if (!updates.isEmpty() && !history.isEmpty()) {
			OffsetDateTime t1 = updates.get(0).getLastModifiedOffsetDateTime();
			OffsetDateTime t2 = history.get(0).getLastModifiedDate();
			Duration d = Duration.between(t1, t2);
			lessThanOneSecond = d.abs().compareTo(Duration.ofSeconds(1)) < 0;
		}
		update=true;
		if ((update && !relationError && !resourcesError) || 
			(!update && !relationError && !resourcesError && !lessThanOneSecond)) {
			try {
				transaction = beginTransaction();
				for (ApiFile version : history) {
					int versionnumber = version.getVersion();
					Content content = null;
					for (Content v : updates) {
						if (v.getVersion()==versionnumber) {
							content = v;
							break;
						}
					}
					if (content!=null) {
						content = (Content)getContentDao().reload(content);
						content.setLastModifiedOffsetDateTime(version.getLastModifiedDate());
						content.setCheckinOffsetDateTime(version.getLastModifiedDate());
						getSessionFactory().getCurrentSession().save(content);
					}
				}
				transaction.commit();
				getLogger().info(history.get(0).getExternalId() + ", (ok timestamp)");
			}
			catch (Exception e) {
				getLogger().error(e);
				logger.error(e);
				transaction.rollback();
				throw e;
			}
		}
		
		
		
	}
	
	private void delete(ApiFile file) {
		Transaction transaction = null;
		try {
			transaction = beginTransaction();
			com.novamens.hibernate.session.Session.setApi(true);
			Content head = getContentDao().findContentByExternalId("bcv", file.getExternalId());
			if (head!=null) {
				head.getService(ContentService.class).recycle();
				getLogger().info(file.getExternalId() + ", (recycled)");
			}	
			transaction.commit();
		}
		catch (Exception e) {
			e.printStackTrace();
			getLogger().info(file.getExternalId() + ", ERROR");
			transaction.rollback();
		}
	}

	
	private void printHistory(List<ApiFile> history) {
		String message = "";
		
		ApiFile file = history.get(0);
		
		message += ";";
		
		message +=  file.getExternalId() + ";";
		
		String tipo = file.getClassName();
		if ("tecnico".equals(tipo)) {
			tipo= "manual";
		}
		message +=  tipo + ";";
		
		message +=  file.getTitle() + ";";
		
		String folderstring = file.getCustomAttributeValue("carpetas");
		folderstring = folderstring.replace(";", " ");
		
		
		
		message += folderstring + ";";

		String fecha = file.getAttributeValue("Fecha Revisión");
		if (fecha==null) fecha="";
		message += fecha + ";";
		String numero =file.getAttributeValue("Número Revisión");
		if (numero==null) numero="";
		message += numero;
		

		getLogger().info(message);
	}
	
	private void printFolders(List<ApiFile> history) {
		ApiFile file = history.get(0);
		String foldersstring = file.getCustomAttributeValue("carpetas");
		StringTokenizer tokenizer = new StringTokenizer(foldersstring, ",");
		while (tokenizer.hasMoreTokens()) {
			String folderstring = tokenizer.nextToken().trim();
			if ("BCV/Alcance General/Logística Embarque COMAT/2015/02".equals(folderstring)) {
				System.out.print(2);
			}
			if (!foldersMap.containsKey(folderstring)) {
				String folderName = folderstring.substring(4).trim();
				try {
					int count1 = countFolderInRows(folderstring);
					int count2 = countFolderInBase(folderstring);
					foldersMap.put(folderstring, count1);
					String message = "";
					message += folderName + ",";
					message += count1 + ",";
					message += count2 + ",";
					String status = count1==count2 ? "OK": "DIFF";
					message += status;
					getLogger().info(message);
				}
				catch (Exception e) {
					e.printStackTrace();
					String message = "";
					message += folderName + ",,,";
					message += "ERROR";
					getLogger().info(message);
				}
				
			}
			
		}
	}
	
	private int countFolderInRows(String folderString) {
		int count = 0;
		Set<String> counts = new HashSet<>();
		for (int k=0; k<getRows().size(); k++) {
			Map<String, String> row = getRows().get(k);
			String folders = row.get("carpetas");
			String id = row.get("id");
			StringTokenizer tokeinzer = new StringTokenizer(folders, ",");
			while (tokeinzer.hasMoreTokens()) {
				String token = tokeinzer.nextToken();
				if (token.trim().equals(folderString) && !counts.contains(id)) {
					count++;
					counts.add(id);
					break;
				}
			}
		}	
		return count;
	}
	
	protected int countFolderInBase(String folderString) {
		int count = 0;
		Index index = getDomain().getService(JavaIndexerService.class).getIndex();
		SolrParametersQuery query = new SolrParametersQuery(index);
		List<DataSetMember> folders=findAllMembersByPath(folderString);
		
		if (folders==null || folders.isEmpty()) {
			return 0;
		}
	
		List<String> members = new ArrayList<String>();
		String member ="";
		for (DataSetMember folder : folders) {
			for (String path : getPaths(folder, folder)) {
				if (!member.equals("")) {
					member += "|";
				}
				member += "clsf06member/"+path;
			}
		}
		members.add(member);
		
		query.setParameter("head", "true");
		query.setParameter("members", members);
		
		ResultSet result = query.execute();
		
		count = result.size();
		
		return count;
	}


	private List<String> getPaths(DataSetMember member, DataSetMember child) {
		List<String> paths = new ArrayList<>();
		if (member.getParents().isEmpty()) {
			paths.add(String.valueOf(member.getId()));
		}
		else {
			for (DataSetMember parent : member.getParents()) {
				if (!parent.equals(child)) {
					for (String path : getPaths(parent, child)) {
						path = path + "/" +
								String.valueOf(member.getId());
						paths.add(path);
					}
				}
			}
		}
		return paths;
	}

	
	public boolean validVersion(ApiFile file) {
		String value = file.getAttributeValue("Fecha Revisión");
		OffsetDateTime filetime = file.getLastModifiedDate();
		if (value!=null) {
			try {
				LocalDate date = LocalDate.parse(value);
				LocalTime time = LocalTime.MIDNIGHT;
				ZoneOffset offset = ZoneOffset.UTC;
				filetime = OffsetDateTime.of(date, time, offset);
			}
			catch (Exception e) {
			}
		}
		OffsetDateTime now = OffsetDateTime.now();
		OffsetDateTime reference = now.minusYears(5);
		return (filetime.isAfter(reference));
	}

	
	private String getTemplate(Map<String,String> data) {
		String template = null;
		String type = data.get("tipo").toLowerCase();
		if ("embarque".equals(type)) type = "Documento";
		if ("Documento".equals(type)) type = "Documento Corporativo";
		template = type;
		return template;
	}
	
	private List<String> update(Content content, ApiFile file) throws IOException {
		List<String> updates = new ArrayList<String>();
		
		OffsetDateTime t1 = content.getLastModifiedOffsetDateTime();
		OffsetDateTime t2 = file.getLastModifiedDate();
		Duration d = Duration.between(t1, t2);
		boolean lessThanOneSecond = d.abs().compareTo(Duration.ofSeconds(1)) < 0;
		boolean fixed = "true".equals(file.getControlAttributeValue("fixed"));
		if (lessThanOneSecond && !fixed) {
			Classifier classifier = getClassifier("Carpeta");
			int f1 = content.getClassification(classifier).size();
			int f2 = 0;
			String foldersstring = file.getCustomAttributeValue("carpetas");
			StringTokenizer tokenizer = new StringTokenizer(foldersstring, ",");
			while (tokenizer.hasMoreTokens()) {
				tokenizer.nextToken();
				f2++;
			}			
			
			if (f1==f2) {
				User user = content.getLastModifiedUser();
				if (user!=null) {
					String username = user.getLasName() + ", " + user.getFirstName();
					if (username.equals(file.getLastModifiedUser().getName())) {
						return updates;
					}
				}
			}

		}
		
		if (!equals(content.getTitle(), file.getTitle())) {
			content.setTitle(file.getTitle());
			updates.add("Title");
		}
		
		if (file.getClassName()!=null &&  !content.getContentTemplate().getName().toLowerCase().equals(file.getClassName().toLowerCase())) {
			ContentTemplate template = getTemplateByName(file.getClassName());
			file.removeAttribute("template");
			if (template!=null) {
				content.setContentTemplate(template);
				updates.add("Template");
			}
		}
		
 		
		updates.addAll(setAttributes(content, file));
		
		updates.addAll(setCustomAttributes(content, file));
		
		updates.addAll(setFolders(content, file));
		
		updates.addAll(setRelations(content, file));
		
		updates.addAll(setUser(content, file));
		
		if (!updates.isEmpty()) {
			
			
			UpdateEvent event = new UpdateEvent(content, "Migración BCV");
			LogDao dao = (LogDao)ServiceLocator.getService(BeansService.class).getBean("logDao");
			dao.update(event);
			
			getContentDao().flush();
			
			content.getLastModifiedUser();
			
			getSessionFactory().getCurrentSession().save(content);
		}
		
		return updates;
	}
	
	private List<String> updateResources(Content content, ApiFile file) throws IOException {
		List<String> updates = new ArrayList<String>();
		OffsetDateTime t1 = content.getLastModifiedOffsetDateTime();
		OffsetDateTime t2 = file.getLastModifiedDate();
		Duration d = Duration.between(t1, t2);
		boolean lessThanOneSecond = d.abs().compareTo(Duration.ofSeconds(1)) < 0;
		if (lessThanOneSecond) {
			return updates;
		}
		List<ApiResource> resources = getRemoteResources(content, file);
		file.setResources(resources);
		updates.addAll(setResources(content, file));
		if (!updates.isEmpty()) {
			this.updates++;
		}
		return updates;
	}
	
	
	protected List<String> getFolders(ApiFile file) {
		List<String> folders = new ArrayList<String>();
		
		
		String foldersstring = file.getCustomAttributeValue("carpetas");
		StringTokenizer tokenizer = new StringTokenizer(foldersstring, ",");
		
		Domain domain = ServiceLocator.getService(UserService.class).getDomain();
		
		DataSet dataset = getContentDao().findDataSetByName("Carpeta", domain.getId());
		Classifier classifier = null;
		for (Classifier c : getContentDao().getClassifiers(domain)) {
			if (c.getDataSet().equals(dataset)) {
				classifier = c;
			}
		}
		Assert.isTrue(dataset!=null && classifier!=null, "no dataset");
		

		while (tokenizer.hasMoreTokens()) {
			String folderstring = tokenizer.nextToken();

					folders.add(folderstring);
		}


		return folders;
		
	}

	
	protected List<String> setFolders(Classificable classificable, ApiFile file) {
		List<String> updates = new ArrayList<String>();
		
		String foldersstring = file.getCustomAttributeValue("carpetas");
		StringTokenizer tokenizer = new StringTokenizer(foldersstring, ",");
		
		Domain domain = ServiceLocator.getService(UserService.class).getDomain();
		
		DataSet dataset = getContentDao().findDataSetByName("Carpeta", domain.getId());
		Classifier classifier = null;
		for (Classifier c : getContentDao().getClassifiers(domain)) {
			if (c.getDataSet().equals(dataset)) {
				classifier = c;
			}
		}
		Assert.isTrue(dataset!=null && classifier!=null, "no dataset");
		
		List<DataSetMember> folders = new ArrayList<DataSetMember>();
		List<Classification> existingfolders = classificable.getClassification(classifier);
		
		while (tokenizer.hasMoreTokens()) {
			String folderstring = tokenizer.nextToken();
			StringTokenizer foldertokenizer = new StringTokenizer(folderstring, ",");
			boolean newfolder = false;
			while (foldertokenizer.hasMoreTokens()) {
				String folderid = foldertokenizer.nextToken().trim();
				DataSetMember folder = !folderid.startsWith("kb:") 
					? findMemberByPath(folderid)
					: findMemberByEXternalId(folderid.substring(3));
				if (folder!=null) {
					boolean found = false;
					for (Classification classification : existingfolders) {
						if (classification!=null && classification.getDataSetMember().equals(folder)) {
							found = true;
							break;
						}
					}
					if (!found) {
						if (updates.isEmpty())
							updates.add("Carpeta BCV");
					}
					folders.add(folder);
				}
				else {
					getLogger().info(((Content)classificable).getExternalId() + ", ERROR (foler not found)" + folderid);
				}
			}	
			if (newfolder)
				getContentDao().flush();
		}
		
		if (!updates.isEmpty()) {
			getContentDao().save((Content)classificable);
			classificable.setClassification(classifier, folders);
		}
		
		return updates;
	}
	
	public List<String> setUser(Content content, ApiFile file) {
		List<String> updates = new ArrayList<String>();
		User user;
		
		Person person = getUserByName(content, file.getLastModifiedUser().getName());
		if (person==null) {
			getLogger().info(content.getExternalId() + ", ERROR (user not found)" + file.getLastModifiedUser().getName());
			updates.add("relation error");
		}
		else {
			UserProfile userProfile = person.getProfile(UserProfile.class);
			user = userProfile.getUser();
			if (content.getLastModifiedUser()==null || !content.getLastModifiedUser().equals(user)) {
				content.setLastModifiedUser(user);
				updates.add("user");
			}
		}
		return updates;
	}
	
	protected List<String> setAttributes(Classificable classificable, ApiClassificable iclassificable) {
		List<String> updates = new ArrayList<String>();
		Set<String> names = new HashSet<String>();
		for (IAttributeValues attributevalue : iclassificable.getAttributes()) {
			ApiAttributeProxy iattribute = attributevalue.getAttribute();
			String attributename = iattribute.getName().toLowerCase(); 
			if (!names.contains(attributename) && !getValues(attributename, iclassificable).isEmpty()) {
				Classifier classifier = getClassifier(attributename, classificable);
				if (classifier!=null) {
					names.add(attributename);
					if (update(classificable, classifier, getValues(attributename, iclassificable))) {
						classifiy(classificable, classifier, getValues(attributename, iclassificable));
						updates.add(iattribute.getName());
					}
				}
				else {
					Attribute attribute = getAttribute(attributename, classificable);
					if (attribute!=null) {
						names.add(attributename);
						if (update(classificable, attribute, getValues(attributename, iclassificable))) {
							setAttribute(classificable, attribute, getValues(attributename, iclassificable));
							updates.add(iattribute.getName());
						}
					}
					else {
						throw new ApiException(HttpStatus.PRECONDITION_FAILED,  ApiError.INVALID_ATTRIBUTE, iattribute.getName());
					}
				}
			}
		}
		for (Classifier classifier : getClassifiers(classificable)) {
			if (!names.contains(classifier.getName().toLowerCase())) {
				if (classifier.isMandatory()) {
					throw new ApiException(HttpStatus.PRECONDITION_FAILED, ApiError.ATTRIBUTE_IS_REQUIRED, classifier.getName());
				}
			}
		}
		for (AttributeTemplate template : getAttributes(classificable)) {
			if (!names.contains(template.getAttribute().getName().toLowerCase())) {
				if (template.getAttribute().isRequired()) {
					throw new ApiException(HttpStatus.PRECONDITION_FAILED, ApiError.ATTRIBUTE_IS_REQUIRED, template.getAttribute().getName());
				}
				else {
					if (update(classificable, template.getAttribute(), new ArrayList<ApiValue>())) {
						classificable.setAttributeValues(template.getAttribute(), new ArrayList<String>());
					}
				}
			}
		}
		return updates;
	}
	
	protected List<ApiValue> getValues(String attributename, ApiClassificable file) {
		List<ApiValue> values = new ArrayList<ApiValue>();
		for (IAttributeValues attributevalues : file.getAttributes()) {
			if (attributename.equals(attributevalues.getAttribute().getName().toLowerCase())) {
				for (ApiValue value : attributevalues.getValues()) {
					if (value.getHRef()!=null) {
						value = getValue(value.getHRef()); 
					}
					if (value.getValue()!=null) {
						if (!values.contains(value))
						values.add(value);
					}
				}
			}
		}
		return values;
	}
	
	protected ApiValue getValue(String url) {
		String fragments[] = url.split("/");
		int n = fragments.length;
		if (n>1 && "externalvalue".equals(fragments[n-1])) {
			throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.VALUE_NOT_FOUND);
		}
		else {
			String id = fragments[n-1];
			ApiValue value = new ApiValue();
			value.setId(id);
			return value;
		}
	}
	
	protected void classifiy(Classificable content, Classifier classifier, List<ApiValue> values) {
		if ((classifier.getMultiplicity().equals(Multiplicity.M01)|| classifier.getMultiplicity().equals(Multiplicity.M11)) && values.size()>1) {
			throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.INVALID_MULTIPLICITY, classifier.getName());
		}
		if (classifier.getDataSetType().equals(DataSetType.DATE)) {
			List<OffsetDateTime> dates = new ArrayList<OffsetDateTime>();
			for (ApiValue value : values) {
				DateTimeFormatter dateformat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
				LocalDateTime datetime = LocalDateTime.parse(value.getValue() + " 00:00:00", dateformat);
				OffsetDateTime localvalue = OffsetDateTime.of(datetime, OffsetDateTime.now().getOffset());
				dates.add(localvalue);
			}
			((Content)content).setValues(classifier, dates);
		}
		else {
			List<DataSetMember> members = new ArrayList<DataSetMember>();
			for (ApiValue value : values) {
				DataSetMember member;
				if (value.getId()!=null) {
					member = findMemberById(value.getId());
				}	
				else {
					member = findMemberByValue(classifier.getDataSet(), value.getValue());
				}
				members.add(member);
			}
			content.setClassification(classifier, members);
		}	
	}
	
	protected DataSetMember findMemberById(String value) {
		try {
			DataSetMember member = getContentDao().findMemberById(Long.valueOf(value));
			if (member==null || !member.getDomain().equals(getDomain())) {
				throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.VALUE_NOT_FOUND);
			}
			return member;
		}
		catch(ContentMgmtException | NumberFormatException e) {
			e.printStackTrace();
			if (logger.isDebugEnabled()) {
				logger.error(e);
			}
			else {
				logger.info("error "+e.getMessage());
			}
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.INTERNAL_ERROR, e.getMessage());
		}
	}
	
	protected DataSetMember findMemberByEXternalId(String value) {
		try {
			DataSetMember member = getContentDao().findMemberByExternalId(value);
			if (member==null || !member.getDomain().equals(getDomain())) {
				throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.VALUE_NOT_FOUND);
			}
			return member;
		}
		catch(ContentMgmtException | NumberFormatException e) {
			e.printStackTrace();
			if (logger.isDebugEnabled()) {
				logger.error(e);
			}
			else {
				logger.info("error "+e.getMessage());
			}
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.INTERNAL_ERROR, e.getMessage());
		}
	}
	
	protected DataSetMember findMemberByValue(DataSet dataSet, String value) {
		try {
			DataSetMember member = getContentDao().findMemberByValue(dataSet, value);
			if (member==null) {
				new LockTransactionSynchronization("ds"+String.valueOf(dataSet.getId()));
				member = getContentDao().findMemberByValue(dataSet, value);
				if (member==null) {
					member = dataSet.createMember();
					member.setStrValue(value);
					member.getService(DOMObjectService.class).update();
				}
			}
			return member;
		}
		catch(ContentMgmtException e) {
			e.printStackTrace();
			if (logger.isDebugEnabled()) {
				logger.error(e);
			}
			else {
				logger.info("error "+e.getMessage());
			}
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.INTERNAL_ERROR, e.getMessage());
		}
	}
	
	protected boolean update(Classificable content, Attribute attribute, List<ApiValue> values) {
		for (ApiValue value1 : values) {
			boolean found = false;
			for (String value2 : content.getAttributeValues(attribute)) {
				if (value1.getValue().equals(value2)) {
					found = true;
					break;
				}
				else {
					if (attribute.getType().equals(AttributeType.DATE) || 
							attribute.getType().equals(AttributeType.VALIDITY_TO) ||
							attribute.getType().equals(AttributeType.VALIDITY_FROM)) {
						try {
							if (formatDate(value1.getValue()).equals(value2)) {
								found = true;
								break;
							}
						}
						catch (DateTimeParseException e) {
						}
					}
				}
			}
			if (!found && !"".equals(value1.getValue().trim())) {
				return true;
			}
		}
		return false;
	}
	
	protected boolean update(Classificable content, Classifier classifier, List<ApiValue> values) {
		List<DataSetMember> members = new ArrayList<DataSetMember>();
		for (Classification classification : content.getClassification()) {
			if (classification.getClassifier().equals(classifier)) {
				members.add(classification.getDataSetMember());
			}
		}
		if (members.size()!=values.size())
			return true;
		for (ApiValue value : values) {
			boolean classified = false;
			for (DataSetMember member : members) {
				if (value.getId()!=null) {
					if (String.valueOf(member.getId()).equals(value.getId())) {
						classified = true;
						break;
					}
				}
				else {
					if (member.getDisplayName().equalsIgnoreCase(value.getDisplayName())) {
						classified = true;
						break;
					}
				}
			}
			if (!classified && value!=null)
				return true;
		}
		return false;
	}
	
	private void setAttribute(Classificable content, Attribute attribute, List<ApiValue> values) {
		if ((attribute.getMultiplicity().equals(Multiplicity.M01)|| attribute.getMultiplicity().equals(Multiplicity.M11)) && values.size()>1) {
			throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.INVALID_MULTIPLICITY, attribute.getName());
		}
		if (attribute.getType().equals(AttributeType.DATE) ||
				attribute.getType().equals(AttributeType.VALIDITY_FROM) ||
				attribute.getType().equals(AttributeType.VALIDITY_TO)){
			List<String> datevalues = new ArrayList<String>();
			for (ApiValue value : values) {
				try {
					datevalues.add(formatDate(value.getValue()));
				}
				catch (DateTimeParseException e) {
					throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.INVALID_DATE, attribute.getName());
				}
			}
			content.setAttributeValues(attribute, datevalues);
		}
		else {
			List<String> stringvalues = new ArrayList<String>();
			for (ApiValue value : values) {
				stringvalues.add(value.getValue());
			}
			content.setAttributeValues(attribute, stringvalues);
		}
	}
	
	private List<String> setCustomAttributes(Content content, ApiFile file) {
		List<String> updates = new ArrayList<String>();
 		List<CustomAttribute> attributes = content.getUserDefinedAttributes();
 		List<CustomAttribute> newattributes = new ArrayList<CustomAttribute>();
		boolean updated = false;
		if (file.getCustomAttributes()==null) return updates;
		for (ICustomAttributeValue attributevalue : file.getCustomAttributes()) {
			String attribute = attributevalue.getAttribute();
			String value = attributevalue.getValue();
			if (attribute!=null && value!=null && !"".equals(value.trim())) {
				newattributes.add(new KbeeCustomAttribute(attribute, value));
			}
		}
		if ((attributes!=null && attributes.size()!=newattributes.size()) || (attributes==null && !newattributes.isEmpty())) {
			updated = true;
		}
		if (!updated && attributes!=null) {
			for (int i=0; i<attributes.size(); i++) {
				CustomAttribute attribute = attributes.get(i);
				CustomAttribute newattribute = newattributes.get(i);
				if (!attribute.equals(newattribute)) {
					updated=true;
					break;
				}
			}
		}
		if (updated) {
			content.setUserDefinedAttributes(newattributes);
			updates.add("User Attributes");
		}
		return updates;
	}
	
	private Content createContent(ApiFile file) {
		try {
			String classname = file.getClassName();
			if ("documento".equals(classname)) classname = "Documento Corporativo";
			if ("tecnico".equals(classname)) classname = "Documento Técnico";
			Content content = ServiceLocator.getService(ContentFactoryService.class).create(classname, false, true);
			content.setCheckinOffsetDateTime(OffsetDateTime.now());
			getContentDao().save(content);
			
			String externalId = file.getExternalId();
			if (externalId!=null) externalId = externalId.toLowerCase();
			((KbeeContent)content).setExternalId(externalId);
			((Versionable<?>)content).setVersion(file.getVersion());
			
			String sourcename = file.getApplication();
			Source source = getContentDao().findSourceByName(sourcename);
			if (source==null) {
				new LockTransactionSynchronization(sourcename);
				source = getContentDao().findSourceByName(sourcename);
				if (source == null) {
					source = ServiceLocator.getService(ContentFactoryService.class).createSource(sourcename, sourcename, content.getDomain());
				}
			}
			((KbeeContent)content).setSource(source);
			
			return content;
		}
		catch (ContentCreationException | ContentMgmtException e) {
			logger.error(e);
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.INTERNAL_ERROR, e.getMessage());
		}
	}
	
//	
//	private Person getUserByHref(Content content, String href) {
//		
//	}

	

	
	private Person getUserByName(Content content, String username) {
		Person person = null;
		String lastname = null, firstname = null;
		int index = username.indexOf(",");
		if (index>0) { 
			lastname = username.substring(0, index);
			firstname = username.substring(index+1).trim();
		}
		else {
			lastname = username;
		}
		
		String stm = "type:datasetmember AND lastname:"+lastname;
		if (firstname!=null) stm +=" AND firstname:"+firstname;
		
		TextQuery query = new TextQuery(stm);
		QueryResponse response = (QueryResponse)getIndex().execute(query);
		SolrDocumentList results = response.getResults();
		
		
		int f=0;
		boolean aa = false;
		for (int i=0; i<results.size() ; i++) {
			SolrDocument solrdocument = results.get(i);
			person = (Person)getContentDao().findObjectById(new ObjectId(solrdocument.getFieldValue("id"))) ;
			if (person!=null && 
					lastname.equals(person.getLastName()) &&
					(person.getFirstName()==null || 
					person.getFirstName().contains(firstname) ||
					firstname.contains(person.getFirstName()))) {
				PersonMember member = (PersonMember)person;
				Classifier empresa = getClassifier("Empresa");
				for (Classification classification : member.getClassification(empresa)) {
					if (classification!=null && "Aerolíneas Argentinas".equals(classification.getStrValue())) {
						aa = true;
						break;
					}
				}	
				if (f++>0) {
					if (!aa) {
						getLogger().info(content.getExternalId() + ", ERROR (mas de un user)" + username);
						person = null;
					}
				}
				if (aa) {
					break;
				}
			}
			else {
				getLogger().info(content.getExternalId() + ", ERROR (user not found)" + username);
				person = null;
			}
		}
		
		
		return person;
	}
	

	
	private List<ApiResource> getResources(ApiFile ifile) {
		List<ApiResource> resources= new ArrayList<ApiResource>();
		String id = ifile.getControlAttributeValue("files");
		String url = ifile.getControlAttributeValue("url");
		
		if (url!=null) {
			ApiResource resource = new ApiResource();
			resource.setHRef(url);
			resource.setControlAttribute("url", url);
			resources.add(resource);
		}
		else {
			if (id.equals("")) return null;
			File folder = new File("migration/data/"+id.charAt(0)+"/"+id);
			if (!folder.exists()) return null;
			String childs[] = folder.list();
			for (int f=0; f<childs.length; f++) {
				String filename = childs[f];
				if (!filename.startsWith(".") && !"kbee-content".equals(filename)) {
					ApiResource resource = new ApiResource();
					String filepath = folder.getPath()+File.separator+filename;
					File file = new File(filepath);
					if (!file.isDirectory()) {
						resource.setHRef(filepath);
						resource.setName(file.getName());
						resource.setControlAttribute("size", String.valueOf(file.length()));
						resources.add(resource);
					}
				}
			}
		}
		
		return resources;
	}
	
	
	private List<ApiResource> getRemoteResources(Content content, ApiFile file) throws IOException {
		List<ApiResource> updates = new ArrayList<>();
		String date = file.getControlAttributeValue("creation");
		
		String tipo = file.getAttributeValue("tipodocumento").toLowerCase();
		if ("documento".equals(tipo)) {
			tipo = "document";
			for (Classification c : content.getClassification(getClassifier("carpeta"))) {
				if (c!=null && c.getDataSetMember()!=null) {
					List<TreeNode> nodes = ServiceLocator.getService(TreeService.class).getNodes(c.getDataSetMember());
					for (TreeNode node : nodes) {
						for (TreeNode path : node.getPath().getNodes()) {
							if (path.getDisplayName().contains("Logística Embarque COMAT")) {
								tipo ="shipment";
								break;
							}
						}
					}
				}
			}
		}
		if ("tecnico".equals(tipo)) {
			tipo ="manual";
		}
		if ("documento técnico".equals(tipo)) {
			tipo ="manual";
		}
		if ("url".equals(tipo)) {
			return new ArrayList<>();
		}
		updates = getResources(tipo, date, file.getExternalId(), file.getControlAttributeValue("uri"));
		return updates;
	}
	
	private List<String> setResources(Content content, ApiFile file) throws IOException {
		Transaction transaction = null;
		
		List<String> updates = new ArrayList<String>();
		

		String tipo = file.getAttributeValue("tipodocumento").toLowerCase();
		if ("url".equals(tipo)) {
			return updates;
		}
		
		
		List<ApiResource> files = new ArrayList<>();
		List<ApiResource> trs = new ArrayList<>();
		
		for (ApiResource resource : file.getResources()) {
			if ("tr".equals(resource.getTag().getName())) {
				trs.add(resource);
			}
			else {
				files.add(resource);
			}
		}
		
		List<ResourceNode> nodes = new ArrayList<>();
		List<ResourceFolder> folders = new ArrayList<>();

		//while (retry) {
			try {
				transaction = beginTransaction();
				int i = 0, s = files.size();
				for (ApiResource iresource : files) {
					//if (i>=page) {
						ResourceNode node;
						if (iresource.getHRef()!=null)
							setProperties(iresource.getHRef(), iresource);
						Resource resource = getResource(content, iresource);
						ResourceFolder parent =  getFolder(
							iresource.getControlAttributeValue("folder"), 
							folders);
						if ("true".equals(iresource.getControlAttributeValue("isfolder"))) {
							ResourceFolder folder = resource==null 
								? ServiceLocator.getService(ContentFactoryService.class).createFolder(iresource.getName())
								: (ResourceFolder)resource;		
							node = new KbeeResourceNode(folder, parent); 
							folders.add(folder);
						}
						else {
							KBFile kbfile =  resource==null
								? getFile(iresource)
								: (KBFile)resource;		
							node = new KbeeResourceNode(kbfile, parent);
						}
						nodes.add(node);
						
						if (resource==null && updates.isEmpty()) {
							updates.add("Add File");
						}
						
						if ((i>0 && i%50==0) || i==s-1) {
							ResourceTag tag = getResourceTag("archivo");
							if (!updates.isEmpty()) {
								setIndex(nodes, file);
								((ResourceContainer)content).setResourceNodes(nodes, tag);
								getSessionFactory().getCurrentSession().save(content);
							}
							transaction.commit();
							transaction = beginTransaction();
							com.novamens.hibernate.session.Session.setApi(true);
						}
						System.out.println(i);
					//}
					i++;
				}
			}
			catch (ApiException | IOException e) {
				getLogger().error(e);
				transaction.rollback();
					throw e;
			}
		//}
		
		
		List<ResourceNode> trnodes = new ArrayList<>();
		for (ApiResource iresource : trs) {
			
			Resource resource = getResource(content, iresource);
			KBFile kbfile = resource==null
					? getFile(iresource)
					: (KBFile)resource;		
			ResourceNode node = new KbeeResourceNode(kbfile, null);
			trnodes.add(node);			
		}
		
		if (!trnodes.isEmpty()) {
			ResourceTag tag = getResourceTag("tr");
			((ResourceContainer)content).setResourceNodes(trnodes, tag);
			getSessionFactory().getCurrentSession().save(content);
		}
		
		transaction.commit();
			
		return updates;
	}
	
	private ResourceFolder getFolder(String name, List<ResourceFolder> folders) {
		if (name==null) return null;
		for (ResourceFolder folder : folders) {
			if (name.equals(folder.getName())) {
				return folder;
			}
		}
		String name2 = FileNameNormalizer.normalize(name);
		for (ResourceFolder folder : folders) {
			if (name2.equals(folder.getName())) {
				return folder;
			}
		}
		throw new ApiException(HttpStatus.PRECONDITION_FAILED, ApiError.INTERNAL_ERROR, "folder not found");
	}
		
	private Resource getResource(Content content, ApiResource iresource) {
		String externalId = iresource.getControlAttributeValue("externalId");
		String path = iresource.getControlAttributeValue("path");
		if (externalId!=null && externalId.startsWith("U")) {
			for (ContentResource contentresource : ((KbeeResourceContainer)content).getContentResources()) {
				if (contentresource!=null) {
					Resource resource = contentresource.getResource();
					 if (resource instanceof KBFileImpl) {
						 if (resource.getName().equals(iresource.getName())) {
							 return resource;
						 }
					 }
				}
			}
			return null;
		}
		else {
			for (ContentResource contentresource : ((KbeeResourceContainer)content).getContentResources()) {
				if (contentresource!=null) {
					Resource resource = contentresource.getResource();
					 if (resource instanceof KBFileImpl) {
						 if (externalId!=null && externalId.equals(((KBFileImpl)resource).getExternalId()) &&
							 path.equals(getPath(contentresource, content))) {
							 return resource;
						 }
					 }
					 if (resource instanceof ResourceFolder) {
						 if (iresource.getName().equals(resource.getName()) &&
							 path.equals(getPath(contentresource, content))) {
							 return resource;
						 }
					 }
				}
			}
			return null;
		}
	}
	
	private String getPath(ContentResource resource, Content content) {
		String path = "";
		
		if (resource.getFolder()!=null) {
			ResourceFolder folder = (ResourceFolder)resource.getFolder();
			ResourceFolder parent = folder;
			while (parent!=null) {
				if (!path.equals("")) {
					path = "/" + path;
				}
				path = parent.getName() + path;
				folder = parent;
				parent = null;
				for (ContentResource node : ((KbeeIDoc)content).getContentResources()) {
					if (node!=null && node.getResource().getId().equals(folder.getId())) {
						parent = node.getFolder();
						break;
					}
				}
			}
		}
		
		return path;
	}
	
	private void setIndex(List<ResourceNode> nodes, ApiFile file) {
		String index = file.getControlAttributeValue("index");
		List<String> indexes = new ArrayList<>();
		if (index!=null) {
			String indexstrings[] = index.split("\\|");
			for (int i=0; i<indexstrings.length; i++) {
				indexes.add(indexstrings[i]);
			}
		}
		if (!indexes.isEmpty() && indexes.size()<nodes.size()) {
			for (ResourceNode node : nodes) {
				if (!(node instanceof ResourceFolder)) {
					for (String indexurl : indexes) {
						String path[] = indexurl.split("/");
						String indexname = path.length>0 ? path[path.length-1] : null;
						if (indexname!=null && indexname.equals(node.getName())) {
							((KbeeResourceNode)node).setIndex(true);
						}
					}	
				}
			}
		}
	}
	
	private KBFile getFile(ApiResource resource) throws IOException {
		KBFile kbfile = null;
		String id = resource.getControlAttributeValue("externalId");
		if (id!=null) {
			kbfile = findFileByEXternalId(resource);
		}
		if (kbfile==null) {
			kbfile = createAndUpload(resource);
			kbfile.setUploadOffsetDateTime(resource.getLastModifiedDate());
			kbfile.setLastModifiedOffsetDateTime(resource.getLastModifiedDate());
			getSessionFactory().getCurrentSession().save(kbfile);
		}
		return kbfile;
	}
	
	private KBFile createAndUpload(ApiResource resource) throws IOException {
		
		try {
			String urlstring ="";
			boolean checkct = false;
			
			String externalId = resource.getControlAttributeValue("externalId");
					
			if (externalId==null || !externalId.startsWith("U")) {
				urlstring = resource.getHRef();
				urlstring = urlstring.replace("href=", "");
				urlstring = urlstring.replace("\"", "");
				urlstring = bcvurl +  urlstring;
			}
			else {
				urlstring = resource.getHRef();
				int w = urlstring.indexOf("webdav");
				urlstring = bcvurl + "/bcv/resources/kbee:" + urlstring.substring(w+6);
				urlstring = urlstring.replace("\"", "");
				urlstring = urlstring.replace("o%3F", "o%CC%81");
				urlstring = urlstring.replace("a%3F", "a%CC%81");
				urlstring = urlstring.replace("i%3F", "i%CC%81");
				urlstring = urlstring.replace("%3F", "%E2%80%93");
				checkct = true;
			}
			
			URL url = new URL(urlstring);
			
			URLConnection connection = url.openConnection();
			
			 String base64Credentials = Base64.toString((bcvuser+":"+bcvpassword).getBytes());
			 connection.setRequestProperty("Authorization", "Basic " + base64Credentials);
			 ((HttpURLConnection)connection).getResponseCode();
			
			InputStream is = connection.getInputStream();
			long contentLength = connection.getContentLengthLong();
			String ct = connection.getContentType();
			
			if (checkct && ct.equals("text/html; charset=utf-8")) {
				throw new IOException("wrong ct");
			}
			
			String filepath = resource.getName();
			
			filepath = filepath.replace("\"", "");
			
			KBFileImpl kbfile = (KBFileImpl) ServiceLocator.getService(ContentFactoryService.class).createKBFileNoTrx(filepath);
			
			kbfile.setLastModifiedUser(getUser());
			if (resource.getTitle()!=null)
				kbfile.setTitle(resource.getTitle());
			if (resource.getLastModifiedDate()!=null)
				kbfile.setLastModifiedOffsetDateTime(resource.getLastModifiedDate());
			else
				kbfile.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			kbfile.setDomain(getDomain());
			kbfile.setUploadUser(getUser());
			if (resource.getControlAttributeValue("externalId")!=null) { 
				kbfile.setExternalId(resource.getControlAttributeValue("externalId"));
			}
			else {
				throw new IllegalArgumentException("sin external");
			}
			
			if (resource.getLastModifiedDate()!=null)

				kbfile.setUploadOffsetDateTime(resource.getLastModifiedDate());
			else
				kbfile.setUploadOffsetDateTime(OffsetDateTime.now());
			kbfile.setState(ObjectState.ENABLED);
			
			if (!FileNameNormalizer.isValidFileName(filepath)) {
				throw new IOException("");
			}
			
			try {
				InputStream is1 = contentLength>0 
						? new LengthAwareInputStreamWrapper(is, contentLength)
						: is;		
				kbfile.getService(KBFSResourceService.class).putObject(filepath, is1);
			} 
			catch (FileServerException | ServiceNotFoundException e) {
				logger.error(e);
				throw new IOException(e);
			} 
			finally {
				if (is!=null)
					IOUtils.closeQuietly(is);
			} 
			
			
			return kbfile;
		}
		finally {
		}
	}
	
	protected List<String> setRelations(Content content, ApiFile file) {
		List<String> updates = new ArrayList<String>();
		
		List<Relation> relations = new ArrayList<Relation>();
		for (ApiProxy relationproxy : file.getRelationships()) {
			if (relationproxy.getRel()!=null) {
				RelationTemplate template = getRelation(content, relationproxy.getRel());
				
				if (template==null) {
					throw new ApiException(HttpStatus.PRECONDITION_FAILED, ApiError.INVALID_RELATION, relationproxy.getRel());
				}
				
				String targetId = relationproxy.getId();
				
				if (targetId==null) {
					throw new ApiException(HttpStatus.NOT_FOUND, ApiError.INVALID_RELATION);
				}
				
				if (!targetId.trim().equals(file.getExternalId().trim())) {
					new LockTransactionSynchronization(targetId);
	
					Content target = getContent(targetId);
					
					if (target==null || !target.getDomain().equals(content.getDomain())) {
						updates.add("relation error");
						getLogger().info(file.getExternalId() + ", ERROR, Invalid Relation "+relationproxy.getId());
					}
					else {
						KbeeRelation relation = new KbeeRelation();
						relation.setTemplate(template);
						relation.setTarget(target);
						relations.add(relation);
					}
				}
			}
		}

		boolean update = false;
		if (content.getRelations().size()==relations.size()) {
			for (Relation relation1 : content.getRelations()) {
				boolean found = false;
				for (Relation relation2 : relations) {
					if (relation2.equals(relation1)) {
						found = true;
						break;
					}
				}
				if (!found) {
					update = true;
					break;
				}
			}
		}
		else {
			update = true;
		}
		
		if (update) {
			updates.add("relations");
			content.setRelations(relations);
		}
		
		return updates;
	}
	


	

	
	private synchronized List<Map<String, String>> getRows() {
		 
		if (this.rows!=null)
			return this.rows;
		
		type = (String)getParameter("type");
		rowid = (String)getParameter("rowid");
		
		
		BufferedReader reader = null;
		try {
			
			List<Map<String, String>> rows = new ArrayList<Map<String, String>>();
 		
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(getFileName()), "UTF-8"));
			
			String line;
			
			Set<String> ids = new HashSet<String>();
			//int count = 0;
			reader.readLine();
			while ((line = reader.readLine()) != null) {
				Map<String,String> row = new HashMap<String, String>();
 				
				
				if (line!=null && line.endsWith(";")) line +=" ";
				
				String columns[] = line.split(";");
				
				int fieldCount = columns.length;
				if (fieldCount >= fields.length-1) {
					if (fieldCount == fields.length+1) {
						System.out.print(line);
					}
					for (int i=0; i<columns.length; i++) {
						String columnValue = String.valueOf(columns[i].trim());
						String field = fields[i];
						row.put(field, columnValue);
					}
					
					
					ids.add(row.get("id"));
					
					boolean add = false;
					if (rowid!=null) {
						if (rowid.contains(",")) {
							String[] id = rowid.split(",");
							List<String> values = Arrays.asList(id);
							add = values.contains(row.get("id"));
						}
						else {
							add = rowid.equals(row.get("id"));
						}
					}
					else {
						if (type!=null) {
							String rowtype = row.get("tipo");
							if ("Embarque".equals(rowtype))
								rowtype = "Documento";
							add = type.equals(rowtype);
						}
						else {
							add = "Embarque".equals(row.get("tipo")) || 
								"Documento".equals(row.get("tipo")) || 
								"IAP".equals(row.get("tipo")) || 
								"Tecnico".equals(row.get("tipo")) ||
								"URL".equals(row.get("tipo"));
						}
					}
					
					if (add) {
						//count++;
						rows.add(row);
					}
					else {
						getLogger().warn("NOT INCLUDE "+row.get("id"));
					}
				}
				else {
					if (line!=null && line.endsWith(";")) line +=" ";
						for (int i=0; i<columns.length; i++) {
							String columnValue = String.valueOf(columns[i].trim());
							String field = fields[i];
							row.put(field, columnValue);
						}
					if ("Embarque".equals(row.get("tipo")) || "Documento".equals(row.get("tipo")) || "IAP".equals(row.get("tipo")) || "Tecnico".equals(row.get("tipo")) || "URL".equals(row.get("tipo"))) {
						getLogger().warn(",  WARN COLUMNAS , "+row.get("id"));
					}	
				}
			}

			this.rows = rows;
		}
		catch (IOException e) {
			logger.error(e);
			throw new ContentMgmtException(e);
		}
		finally {
			try {
				if (reader!=null)
				reader.close();
			}
			catch (IOException e) {
				logger.error(e);
				throw new ContentMgmtException(e);
			}
		}
		
		return this.rows;
	}
	
	
	private List<ApiResource> getResources(String type, String date, String id, String uri) {
		List<ApiResource> resources = new ArrayList<>();
		try {
			
			String datefield[] = date.split("/");
			String urltype = "documento corporativo".equals(type) ? "document" : type;
			String urldoc = bcvurl+"/webdav/aerolineas-btv/content/bcv/documents/"+urltype+"/"+datefield[2]+"/"+datefield[1]+"/"+id+"/kbee-content";
			urldoc = uri.replace("kbee:", bcvurl+"/webdav");
			String urlstring = urldoc + "/kbee-content";
			URL url = new URL(urlstring);
			URLConnection connection = url.openConnection();
			
			// String base64Credentials = Base64.toString("root:..resurrecto$".getBytes());
			 String base64Credentials = Base64.toString((bcvuser+":"+bcvpassword).getBytes());
			 connection.setRequestProperty("Authorization", "Basic " + base64Credentials);
			
			DocumentBuilderFactory factory =	DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			
			Document doc = builder.parse(connection.getInputStream());
			
			
			
			List<String> trs = new ArrayList<String>();
			NodeList trnodes = doc.getElementsByTagName("TR");
			String trurlnode = null;
			if (trnodes!=null) {
				for (int n=0; n<trnodes.getLength(); n++) {
					org.w3c.dom.Node tr = trnodes.item(n);
					NamedNodeMap attributes = tr.getAttributes();
					org.w3c.dom.Node href = attributes.getNamedItem("href");
					trurlnode = href.toString();
					trurlnode = trurlnode.replace("\"", "");
					trurlnode = trurlnode.substring(5);
					trs.add(urldoc+"/"+trurlnode);
				}
			}
			
			String container = "";
			for (String trhref : trs) {
				resources.addAll(readTR(trhref, container));
			}
			
			NodeList files = doc.getElementsByTagName("Files");
			String filesurlnode = null;
			for (int n=0; n<files.getLength(); n++) {
				org.w3c.dom.Node file = files.item(n);
				NamedNodeMap attributes = file.getAttributes();
				org.w3c.dom.Node href = attributes.getNamedItem("href");
				filesurlnode = href.toString();
			}
			
			if (filesurlnode==null) return resources;
			
			filesurlnode = filesurlnode.replace("\"", "");
			String filesurlpath[] = filesurlnode.split("/");
			int l =  filesurlpath.length;
			container = filesurlpath[l-1];
			String filesurlstring = filesurlnode.contains("versions") 
					? bcvurl+"/webdav/aerolineas-btv/content/files/versions/"+urltype+"/"+filesurlpath[l-3]+"/"+filesurlpath[l-2]+"/"+filesurlpath[l-1]
					: bcvurl+"/webdav/aerolineas-btv/content/files/"+urltype+"/"+filesurlpath[l-3]+"/"+filesurlpath[l-2]+"/"+filesurlpath[l-1];
			filesurlstring += "/";
			URL filesurl = new URL(filesurlstring);
			URLConnection filesconnection = filesurl.openConnection();
			filesconnection.setRequestProperty("Authorization", "Basic " + base64Credentials);
			
			
			String result = CharStreams.toString(new InputStreamReader(filesconnection.getInputStream(), StandardCharsets.UTF_8));
			//result = Normalizer.normalize(result, Normalizer.Form.NFC);
			Document filesdoc = cleanText(result, StandardCharsets.UTF_8);
			
			org.w3c.dom.Node ul = null;
			NodeList ullist = filesdoc.getElementsByTagName("ul");
			for (int n=0; n<ullist.getLength(); n++) {
				ul = ullist.item(n);
				break;
			}
			
			NodeList lilist = ul.getChildNodes();
			for (int n=0; n<lilist.getLength(); n++) {
				org.w3c.dom.Node li= lilist.item(n);
				NodeList alist = li.getChildNodes();
				for (int a=0; a<alist.getLength(); a++) {
					org.w3c.dom.Node anode= alist.item(a);
					NamedNodeMap attributes = anode.getAttributes();
					org.w3c.dom.Node href = attributes.getNamedItem("href");
					if (n>0) {
						System.out.println(href.toString());
						ApiResource resource = new ApiResource();
						resource.setTag(new ApiProxy("archivo",""));
						resource.setControlAttribute("path", "");
						resource.setControlAttribute("container", container);
						String hrefstring = URLDecoder.decode(href.toString(), StandardCharsets.ISO_8859_1);
						String filepath[] = hrefstring.split("/");
						String name = filepath[filepath.length-1];
						if ("\"".equals(name)) {
							name = filepath[filepath.length-2];
							String norm = FileNameNormalizer.normalize(name);		
							if (!name.equals(norm)) {
								System.out.print(name);
							}
							resource.setName(norm);
							resource.setControlAttribute("isfolder", "true");

							resources.add(resource);
							resources.addAll(readFolder(href.toString(), filepath[filepath.length-2], container, new ArrayList<>()));
						}
						else 
						if (!name.contains("kbee-content")) {
							name = name.replace("\"", "");
							String norm = FileNameNormalizer.normalize(name);
							if (!name.equals(norm)) {
								System.out.print(name);
							}
							resource.setName(norm);
							resource.setHRef(href.toString());
							resources.add(resource);
						}
					}
				}	
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ApiException(HttpStatus.PRECONDITION_FAILED, ApiError.INTERNAL_ERROR, e.getMessage());
		}
		return resources;
	}
	

	

	
	
	private ApiFile getRemoteFile(ApiFile file) {
		URLConnection connection = null;
		InputStream is = null;
		try {
			
			String uri = file.getControlAttributeValue("uri");
			uri = uri.replace("kbee:", "/webdav");
			String urlstring = bcvurl+uri;
			URL url = new URL(urlstring);
			connection = url.openConnection();
			
			String base64Credentials = Base64.toString((bcvuser+":"+bcvpassword).getBytes());
			connection.setRequestProperty("Authorization", "Basic " + base64Credentials);
			

			is = connection.getInputStream();

			Parser tagsoupParser = new Parser();
			SAXSource source = new SAXSource(tagsoupParser, new InputSource(is));

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();

			Document doc = builder.newDocument();

			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.transform(source, new DOMResult(doc));
			

			boolean updated = false;
			
			NodeList nodes = doc.getElementsByTagName("Revision");
			if (nodes.getLength()>0) {
				org.w3c.dom.Node node = nodes.item(0);
				String revision = node.getTextContent();
				if (!revision.equals(file.getAttributeValue("Número Revisión"))) {
					file.removeAttribute("Número Revisión");
					file.setAttribute("Número Revisión", revision);
					updated = true;
				}
			}
			
			nodes = doc.getElementsByTagName("RevisionDate");
			if (nodes.getLength()>0) {
				org.w3c.dom.Node node = nodes.item(0);
				String fecharevision = node.getTextContent();
					if (fecharevision!=null && !"".equals(fecharevision) && !"-".equals(fecharevision)) {
						try {
							if (fecharevision.charAt(1)=='/') fecharevision = "0"+fecharevision;
							String fixed = fecharevision.replaceFirst(
								    "(\\d{4}-\\d{2}-\\d{2})([+-]\\d{2}:\\d{2})",
								    "$1T00:00$2"
								);
							OffsetDateTime odt = OffsetDateTime.parse(fixed);
							String value = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(odt);
							if (!value.equals((file.getAttributeValue("Fecha Revisión")))) {
								file.removeAttribute("Fecha Revisión");
								file.setAttribute("Fecha Revisión", value);
								updated = true;
							}
						}
						catch (DateTimeException e) {
							e.printStackTrace();
						}
					}
			}
			
			if (updated) {
				file.setControlAttribute("fixed", "true");
			}
			
			return file;
			
		}
		catch (Exception e) {
			getLogger().info(file.getExternalId() + ", ERROR, "+e.getMessage());
			e.printStackTrace();
			return null;
		}
		finally {
			if (is!=null) {
				try {
					is.close();
				}
				catch (IOException e) {
					
				}
			}
		}
	}

	
	
	private void setProperties(String hrefstring, ApiResource resource) throws IOException {
		String href = null;
		String id = null;
		try {
		   href = hrefstring.substring(6, hrefstring.length()-1);
		   String url = bcvurl+href;
		   
		   

	        String body = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"+
	           "<D:propfind xmlns:D=\"DAV:\">"+
	                "<D:allprop/>"+
	            "</D:propfind>";
	           

	        HttpClient client = HttpClient.newBuilder()
	                .authenticator(new java.net.Authenticator() {
	                    @Override
	                    protected java.net.PasswordAuthentication getPasswordAuthentication() {
	                        return new java.net.PasswordAuthentication(
	                                bcvuser, bcvpassword.toCharArray());
	                    }
	                })
	                .build();

	        HttpRequest request = HttpRequest.newBuilder()
	                .uri(URI.create(url))
	                .method("PROPFIND", HttpRequest.BodyPublishers.ofString(body))
	                .header("Depth", "1")
	                .header("Content-Type", "application/xml")
	                .build();

	        HttpResponse<String> response =
	                client.send(request, HttpResponse.BodyHandlers.ofString());

	        String bodyvalue = response.body();
	        System.out.println(bodyvalue);
	        
	        int i0  = bodyvalue.indexOf("<K:id>");
	        int i1 = bodyvalue.indexOf("</K:id>");
	        
	        id = bodyvalue.substring(i0+6,i1);
			resource.setControlAttribute("externalId", id);
			
		      int d0  = bodyvalue.indexOf("<D:creationdate>");
		      int d1 = bodyvalue.indexOf("</D:creationdate>");
		      
		      String datestring =  bodyvalue.substring(d0+16,d1);
		      OffsetDateTime odt = OffsetDateTime.parse(datestring);

		      resource.setLastModifiedDate(odt);

		}
		catch (Exception e) {
			getLogger().error("Getting id in "+href);
			e.printStackTrace();
			

			int w = href.indexOf("webdav");
			String urlstring = bcvurl + "/bcv/resources/kbee:" + href.substring(w+6);
			
			String name = resource.getName();
			urlstring = urlstring.replace("o%3F", "o%CC%81");
			urlstring = urlstring.replace("a%3F", "a%CC%81");
			urlstring = urlstring.replace("i%3F", "i%CC%81");
			urlstring = urlstring.replace("%3F", "%E2%80%93");
			URL url = new URL(urlstring);
			
			HttpURLConnection connection =  (HttpURLConnection) url.openConnection();
			String base64Credentials = Base64.toString((bcvuser+":"+bcvpassword).getBytes());
			connection.setRequestProperty("Authorization", "Basic " + base64Credentials);
			int status = connection.getResponseCode();

			if (status >= 200 && status < 300) {
			    System.out.println("Connection OK: " + status);
			    String container = resource.getControlAttributeValue("container");
			    String decoded = URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8);
			    //int l = connection.getContentLength();
			    String ct = connection.getContentType();
				if (ct.equals("text/html; charset=utf-8")) {
					throw new IOException("wrong ct");
				}
				else {
					name = decoded.substring(decoded.lastIndexOf('/') + 1);
			    	name = FileNameNormalizer.normalize(name);
			    	resource.setName(name);
					resource.setControlAttribute("externalId", "U"+container);
			    	resource.setLastModifiedDate(OffsetDateTime.now());
				}
			}
			else {
				throw new IOException(e);
			}
		}
	}
	
	private List<ApiResource> readFolder (String hrefvalue, String foldername, String container, List<String> parents) {
		List<ApiResource> resources = new ArrayList<>();
		try {
			
			String urlstring = hrefvalue;
			urlstring = urlstring.replace("href=", "");
			urlstring = urlstring.replace("\"", "");
			urlstring = bcvurl+urlstring;
			
			URL filesurl = new URL(urlstring);
			URLConnection filesconnection = filesurl.openConnection();
			String base64Credentials = Base64.toString((bcvuser+":"+bcvpassword).getBytes());
			filesconnection.setRequestProperty("Authorization", "Basic " + base64Credentials);
			
			String result = CharStreams.toString(new InputStreamReader(filesconnection.getInputStream(), Charsets.UTF_8));
			Document filesdoc = cleanText(result, StandardCharsets.UTF_8);
			
			org.w3c.dom.Node ul = null;
			NodeList ullist = filesdoc.getElementsByTagName("ul");
			for (int n=0; n<ullist.getLength(); n++) {
				ul = ullist.item(n);
				break;
			}
			
			List<String> localparents = new ArrayList<>();
			localparents.addAll(parents);
			localparents.add(foldername);
			String path = "";
			for (String parent : localparents) {
				if (!"".equals(path))
					path += "/";
				path += parent;
			}
			
			NodeList lilist = ul.getChildNodes();
			for (int n=0; n<lilist.getLength(); n++) {
				org.w3c.dom.Node li= lilist.item(n);
				NodeList alist = li.getChildNodes();
				for (int a=0; a<alist.getLength(); a++) {
					org.w3c.dom.Node anode= alist.item(a);
					NamedNodeMap attributes = anode.getAttributes();
					org.w3c.dom.Node href = attributes.getNamedItem("href");
					if (n>0) {
						System.out.println(href.toString());
						ApiResource resource = new ApiResource();
						resource.setTag(new ApiProxy("archivo",""));
						resource.setControlAttribute("folder", foldername);
						resource.setControlAttribute("path", path);
						resource.setControlAttribute("container", container);
						String hrefstring = URLDecoder.decode(href.toString(), StandardCharsets.ISO_8859_1);
						String filepath[] = hrefstring.split("/");
						String name = filepath[filepath.length-1];
						if ("\"".equals(name)) {
							resource.setName(filepath[filepath.length-2]);
							resource.setControlAttribute("isfolder", "true");
							resources.add(resource);
							hrefstring = hrefstring.replace(" ", "%20");
							resources.addAll(readFolder(hrefstring, filepath[filepath.length-2], container, localparents));
						}
						else 
						if (!name.contains("kbee-content")) {
							name = name.replace("\"", "");
							String norm = FileNameNormalizer.normalize(name);		
							if (!name.equals(norm)) {
								System.out.print(name);
							}
							resource.setName(norm);
							resource.setHRef(href.toString());
							resources.add(resource);
						}
					}
				}	
			}
		}
		catch (Exception e) {
			logger.error(e);
			logger.info((bcvuser+":"+bcvpassword));
			logger.info("url"+hrefvalue);
			getLogger().info((bcvuser+":"+bcvpassword));
			getLogger().info("url"+hrefvalue);
			getLogger().error(e);
			throw new ApiException(HttpStatus.PRECONDITION_FAILED, ApiError.INTERNAL_ERROR, e.getMessage());
		}
		
		return resources;
		
	}
	
	private List<ApiResource> readTR(String urlstring, String container) {
		List<ApiResource> resources = new ArrayList<>();
		try {
			URL url = new URL(urlstring);
			URLConnection connection = url.openConnection();
			String base64Credentials = Base64.toString((bcvuser+":"+bcvpassword).getBytes());
			connection.setRequestProperty("Authorization", "Basic " + base64Credentials);
			
			DocumentBuilderFactory factory =	DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			
			Document doc = builder.parse(connection.getInputStream());
			
			String title = null;
			NodeList titlenodes = doc.getElementsByTagName("Title");
			for (int n=0; n<titlenodes.getLength(); n++) {
				org.w3c.dom.Node titlenode = titlenodes.item(n);
				title = titlenode.getTextContent();
			}	
			
			NodeList rdnodes = doc.getElementsByTagName("RevisionDate");
			OffsetDateTime revisiondate = null;
			for (int n=0; n<rdnodes.getLength(); n++) {
				org.w3c.dom.Node rdnode = rdnodes.item(n);
				String datestr = rdnode.getTextContent();
				datestr = datestr.substring(0,10)+"T00:00Z";
				revisiondate = OffsetDateTime.parse(datestr);
			}	
			
			NodeList files = doc.getElementsByTagName("Files");
			String filesurlnode = null;
			for (int n=0; n<files.getLength(); n++) {
				org.w3c.dom.Node file = files.item(n);
				NamedNodeMap attributes = file.getAttributes();
				org.w3c.dom.Node href = attributes.getNamedItem("href");
				filesurlnode = href.toString();
			}	
			
			filesurlnode = filesurlnode.replace("\"", "");
			String filesurlpath[] = filesurlnode.split("/");
			int l =  filesurlpath.length;
			String filesurlstring = filesurlnode.contains("versions") 
					? bcvurl + "/webdav/aerolineas-btv/content/files/versions/tr/"+filesurlpath[l-3]+"/"+filesurlpath[l-2]+"/"+filesurlpath[l-1]
					: bcvurl + "/webdav/aerolineas-btv/content/files/tr/"+filesurlpath[l-3]+"/"+filesurlpath[l-2]+"/"+filesurlpath[l-1];
			filesurlstring += "/";
			URL filesurl = new URL(filesurlstring);
			URLConnection filesconnection = filesurl.openConnection();
			filesconnection.setRequestProperty("Authorization", "Basic " + base64Credentials);
			
			
			 String result = CharStreams.toString(new InputStreamReader(filesconnection.getInputStream(), Charsets.UTF_8));
				Document filesdoc = cleanText(result, StandardCharsets.UTF_8);
			
			
			org.w3c.dom.Node ul = null;
			NodeList ullist = filesdoc.getElementsByTagName("ul");
			for (int n=0; n<ullist.getLength(); n++) {
				ul = ullist.item(n);
				break;
			}
			
			NodeList lilist = ul.getChildNodes();
			for (int n=0; n<lilist.getLength(); n++) {
				org.w3c.dom.Node li= lilist.item(n);
				NodeList alist = li.getChildNodes();
				for (int a=0; a<alist.getLength(); a++) {
					org.w3c.dom.Node anode= alist.item(a);
					NamedNodeMap attributes = anode.getAttributes();
					org.w3c.dom.Node href = attributes.getNamedItem("href");
					if (n>0) {
						System.out.println(href.toString());
						ApiResource resource = new ApiResource();
						resource.setTag(new ApiProxy("tr",""));
						resource.setControlAttribute("path", "");
						resource.setTitle(title);
						resource.setLastModifiedDate(revisiondate);
						String hrefstring = URLDecoder.decode(href.toString(), StandardCharsets.ISO_8859_1);
						String filepath[] = hrefstring.split("/");
						String name = filepath[filepath.length-1];
						if ("\"".equals(name)) {
							resource.setName(filepath[filepath.length-2]);
							resource.setControlAttribute("isfolder", "true");
							resources.add(resource);
							for (ApiResource r :readFolder(href.toString(), filepath[filepath.length-2], container, new ArrayList<>())) {
								r.setTag(new ApiProxy("tr",""));
								resources.add(r);
							}
						}
						else 
						if (!name.contains("kbee-content")) {
							name = name.replace("\"", "");
							String norm = FileNameNormalizer.normalize(name);		
							if (!name.equals(norm)) {
								System.out.print(name);
							}
							resource.setName(norm);
							resource.setHRef(href.toString());
							setProperties(href.toString(), resource);
							resources.add(resource);
						}
					}
				}	
			}
			
		}
		catch (Exception e) {
			logger.error(e);
			logger.info((bcvuser+":"+bcvpassword));
			logger.info("url"+urlstring);
			getLogger().info((bcvuser+":"+bcvpassword));
			getLogger().info("url"+urlstring);
			getLogger().error(e);
			throw new ApiException(HttpStatus.PRECONDITION_FAILED, ApiError.INTERNAL_ERROR, e.getMessage());
		}
		return resources;
	}
	
	
	private DataSetMember findMemberByPath(String value) {
		try {
			String o = new String(value);
			value = value.replace("'","%");
			value = value.replace("  ", "%");
	        String hql = "FROM KbeeDataSetMember D WHERE D.alternative_display like '%" + value.trim()+"' OR  D.alternative_display like '%" + value.trim() +",%'";
	        org.hibernate.query.Query<?> query = getSessionFactory().getCurrentSession().createQuery(hql);
	        List results = query.list();
	        List<DataSetMember> members = (List<DataSetMember>) results;
	        if (members.isEmpty()) {
				value = o.replaceAll("\\s{2,}", " ");
				value = value.replace(" ", "%");
		        String hql2 = "FROM KbeeDataSetMember D WHERE D.alternative_display like '%" + value.trim()+"' OR  D.alternative_display like '%" + value.trim() +",%'";
		        org.hibernate.query.Query<?> query2 = getSessionFactory().getCurrentSession().createQuery(hql2);
		        List results2 = query2.list();
		        List<DataSetMember> members2 = (List<DataSetMember>) results2;
		        if (members2.isEmpty()) {
		        	return null;
		        }
		        return members2.get(0);
	        }
	        return members.get(0);
		}
		catch(Exception e) {
			getLogger().error(e);
			e.printStackTrace();
			if (logger.isDebugEnabled()) {
				logger.error(e);
			}
			else {
				logger.info("error "+e.getMessage());
			}
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.INTERNAL_ERROR, e.getMessage());
		}
	}
	
	@SuppressWarnings("rawtypes")
	private List<DataSetMember> findAllMembersByPath(String value) {
		try {
			value = value.replace("'","%");
			value = value.replace("  ", "%");
	        String hql = "FROM KbeeDataSetMember D WHERE D.alternative_display like '%" + value.trim()+"' OR  D.alternative_display like '%" + value.trim() +",%'";
	        org.hibernate.query.Query<?> query = getSessionFactory().getCurrentSession().createQuery(hql);
	        List results = query.list();
	        List<DataSetMember> members = (List<DataSetMember>) results;
	        if (members.isEmpty()) return null;
	        return members;
		}
		catch(Exception e) {
			getLogger().error(e);
			e.printStackTrace();
			if (logger.isDebugEnabled()) {
				logger.error(e);
			}
			else {
				logger.info("error "+e.getMessage());
			}
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.INTERNAL_ERROR, e.getMessage());
		}
	}
	
    @SuppressWarnings("rawtypes")
	private KBFile findFileByEXternalId(ApiResource resource) {
		try {
			String id = resource.getControlAttributeValue("externalId");
	        String hql = "FROM KBFileImpl F WHERE F.externalId = '" + id+ "'";
	        org.hibernate.query.Query<?> query = getSessionFactory().getCurrentSession().createQuery(hql);
			List results = query.list();
	        if (results.isEmpty()) return null;
	        if (id.startsWith("U")) {
	        	for (Object object : results) {
		        	KBFileImpl file = (KBFileImpl)object;
		        	if (file.getName().equals(resource.getName())) {
		        		return file;
		        	}
	        	}
	        	return null;
	        }
	        else {
	        	KBFileImpl file = (KBFileImpl)results.get(0);
	        	return file;
	        }	
		}
		catch(Exception e) {
			getLogger().error(e);
			e.printStackTrace();
			if (logger.isDebugEnabled()) {
				logger.error(e);
			}
			else {
				logger.info("error "+e.getMessage());
			}
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.INTERNAL_ERROR, e.getMessage());
		}
	}
	
	
	private ContentTemplate getTemplateByName(String name) {
		ContentTemplate template = getContentDao().findContentTemplateByName(name, getDomain().getId());
		return template;
	}
	
	private RelationTemplate getRelation(Content content, String relationname) {
		for (RelationTemplate template : content.getContentTemplate().getRelations()) {
			if (template.getName().toLowerCase().equals(relationname.toLowerCase())) {
				return template;
			}
		}
		return null;
	}
	
	
	private Classifier getClassifier(String name) {
		for (Classifier classifier : getContentDao().getClassifiers(getDomain())) {
			if (classifier.getAlias()!=null && name.toLowerCase().equals(classifier.getAlias().toLowerCase())) {
				return classifier;
			}
		}
		return null;
	}
	
	protected Classifier getClassifier(String classifiername, Classificable classificable) {
		for (Classifier classifier : getClassifiers(classificable)) {
			if (classifiername.equals(classifier.getName().toLowerCase()) ||
					classifiername.equals(classifier.getAlias().toLowerCase())) {
				return classifier;
			}
		}
		return null;
	}
	
	protected Attribute getAttribute(String attributename, Classificable member) {
		for (AttributeTemplate template : getAttributes(member)) {
			if (attributename.equals(template.getAttribute().getName().toLowerCase()) || 
				attributename.equals(template.getAttribute().getAlias().toLowerCase())) {
				return template.getAttribute();
			}
		}
		return null;
	}
	
	protected List<Classifier> getClassifiers(Classificable classificable){
		List<Classifier> classifiers = new ArrayList<Classifier>();
		for (ClassifierTemplate template : ((Content)classificable).getContentTemplate().getClassifiers()) {
			classifiers.add(template.getClassifier());
		};
		return classifiers;
	}
	
	protected List<AttributeTemplate> getAttributes(Classificable classificable) {
		return ((Content)classificable).getContentTemplate().getAttributes();
	}
	
	private String formatDate(String value) throws DateTimeParseException {
		DateTimeFormatter inputdateformat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime localdatetime = LocalDateTime.parse(value + " 00:00:00", inputdateformat);
		OffsetDateTime date = OffsetDateTime.of(localdatetime, OffsetDateTime.now().getOffset());
		DateTimeFormatter dateformat = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
		String datevalue = dateformat.format(date);
		return datevalue;
	}
	
	private String getFileName() {
		return ServiceLocator.getService(SystemParameterService.class).getParameter("aerolineas.users.file", "migration"+File.separator+"docs.csv");
	}
	
	private String getLoggerName(String lote) {
		String name = logsPath +"/importacion-" + lote.toLowerCase() + "-";
		DateFormat format = new SimpleDateFormat("MM-dd-yyyy");
		name += format.format(new Date());
		name += "-" + String.valueOf(getId()) + ".log";
		return name;
	}
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
//	private SecurityDao getSecurityDao() {
//		return (SecurityDao)ServiceLocator.getService(BeansService.class).getBean("securityDao");
//	}
	
	private Index getIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}
	
	private User getUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	protected <R> DomRepository<R> getRepository(Class<R> objectclass) {
		DomRepository<R> repository = ServiceLocator.getService(DomRepositoryService.class).getRepository(objectclass);
		return repository;
	}
	
	private SessionFactory getSessionFactory() {
		return (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
	}
	
	public Document cleanText(String text, Charset cs) {
		
		if (text==null || text.length()==0) 
			return null;
		
	    InputSource source = new InputSource(new java.io.StringReader(text));
	    
		ElementRemover remover = new ElementRemover();
		
		remover.acceptElement("body", null);
		remover.acceptElement("head", null);
        remover.acceptElement("link",  new String[] { "rel", "type", "href", "media" });
		remover.acceptElement("html", null);

		remover.acceptElement("strong", null);
		remover.acceptElement("em", null);
		remover.acceptElement("br", null);
		
		remover.acceptElement("span", new String[] { "class", "style" });
		remover.acceptElement("input", new String[] { "class", "type", "name"});
		remover.acceptElement("div", new String[] { "class", "style" });
				
		remover.acceptElement("p", new String[] { "class", "style" });
		
		remover.acceptElement("h1", new String[] { "class" });
		remover.acceptElement("h2", new String[] { "class" });
		remover.acceptElement("h3", new String[] { "class" });
		remover.acceptElement("h4", new String[] { "class" });
		remover.acceptElement("h5", new String[] { "class" });
		remover.acceptElement("h6", new String[] { "class" });
		
		remover.acceptElement("ul", new String[] { "class" });
		remover.acceptElement("li", new String[] { "class" });
		
		remover.acceptElement("b", null);
		remover.acceptElement("i", null);
		remover.acceptElement("u", null);
		remover.acceptElement("img", new String[] { "class", "src", "style", "width", "alt",  "height" });
		remover.acceptElement("a", new String[] { "href" });
		
		remover.removeElement("script");
		remover.removeElement("embed");
		
		org.cyberneko.html.filters.Writer writer =  new org.cyberneko.html.filters.Writer();
		
		XMLDocumentFilter[] filters = {
				remover,
				writer,
		};
		
		org.apache.xerces.parsers.DOMParser parser = new org.apache.xerces.parsers.DOMParser(new HTMLConfiguration());
		try {
			parser.setProperty("http://cyberneko.org/html/properties/filters", filters);
			parser.setFeature("http://cyberneko.org/html/features/insert-namespaces", false);
			parser.setFeature("http://cyberneko.org/html/features/override-namespaces", true);
			parser.setProperty("http://cyberneko.org/html/properties/names/elems", "lower");
			parser.setProperty("http://cyberneko.org/html/properties/default-encoding", "UTF-8");  
			parser.parse(source);
		} 
		catch (SAXException e) {
			throw new KbeeRuntimeException(e);
		} 
		catch (IOException e) {
			throw new KbeeRuntimeException(e);
		}
		
		Document document = parser.getDocument();
		
		return document;
	}
	
	private Content getContent(String id) {
		Content content = getContentDao().findContentByExternalId("bcv", id);
		return content;
	}
	
	public static String dom2String(Element element) {
		StringWriter output = new StringWriter();
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.transform(new DOMSource(element), new StreamResult(output));
			String xml = output.toString();
			return xml;	
		}
		catch (TransformerException e) {
			return "ERROR dom2String";
		}
	}
	
	private ResourceTag getResourceTag(String name) {
		for (ResourceTag t : getRepository(ResourceTag.class).findAll(getDomain())) {
			if (t.getName().toLowerCase().equals(name.toLowerCase())) {
				return t;
			}
		};
		return null;
	}
	
	private boolean equals(String s1, String s2) {
		if (s1!=null && !s1.equals(s2))
			return false;
		if (s2!=null && !s2.equals(s1))
			return false;
		return true;
	}
}