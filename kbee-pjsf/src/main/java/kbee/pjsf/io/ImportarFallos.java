package kbee.pjsf.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.base.Relation;
import com.novamens.content.base.Source;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.EntityMember;
import com.novamens.content.service.ContentFactoryService;
import com.novamens.kbee.content.base.KbeeContent;
import com.novamens.kbee.content.command.AsyncCommand;
import com.novamens.kbee.content.model.KbeeDataSetMember;
import com.novamens.kbee.content.model.KbeeRelation;
import com.novamens.kbee.content.script.KbeeClassificableScriptWrapper;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.transaction.Transaction;

import kbee.api.service.ApiError;
import kbee.api.service.ApiException;
import kbee.pjsf.rag.DocumentReferencesResponse;
import kbee.pjsf.rag.DocumentVoicesResponse;
import kbee.pjsf.rag.LegalReference;
import kbee.pjsf.rag.RagClient;
import kbee.pjsf.rag.VoiceNode;
import kbee.pjsf.rag.VoicePath;
import kbee.util.PropertiesFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import software.amazon.awssdk.services.iotanalytics.paginators.ListDatasetContentsIterable;


public class ImportarFallos extends AsyncCommand {

    private static final Logger logger =
            LoggerFactory.getLogger(ImportarFallos.class);

    private FileParser parser = new FileParser();
    private RagClient ragService;
	private String logsPath;


    public ImportarFallos() {
		setName("Importar Fallos PJSF");
    }

    @Override
	public void executeAsync() {
    	
    	try {
    		

    		logsPath = PropertiesFactory
	 				.getInstance("kbee")
	 				.getProperties()
	 				.getProperty("pjsf.logs", "logs").trim();
    		
			setLogger(getLoggerName("1"));
			com.novamens.hibernate.session.Session.setApi(true);
			com.novamens.hibernate.session.Session.open();
			ServiceLocator.getService(SecurityService.class).authenticate("root@pjsf");
			
			ragService = new RagClient(
				"http://localhost:8081/",
				"admin",
				"kbeerag");
		

	    	Path folder = Path.of("fallos");
	    	
	        if (!Files.isDirectory(folder)) {
	            throw new IllegalArgumentException(
	                    "Directorio inválido: " + folder
	            );
	        }
	
	        logger.info("Iniciando importación desde {}", folder);
	
	        try (Stream<Path> files = Files.list(folder)) {
	
	            files.filter(Files::isRegularFile)
	                 .filter(this::isSupportedFile)
	                 .sorted()
	                 .forEach(this::processPath);
	        }
	
	        logger.info("Importación finalizada");
    	}
		catch (Exception e) {
			e.printStackTrace();
			getLogger().error(e);
			getLogger().info("END CON " + e.getMessage());
			stop();
		}
    }

    private boolean isSupportedFile(Path path) {

        return path.getFileName()
                   .toString()
                   .toLowerCase(java.util.Locale.ROOT)
                   .endsWith(".txt");
    }

    private void processPath(Path path) {

        try {

            logger.info("Procesando archivo {}", path);

            ApiFile file = createApiFile(path);

            List<TextFile> parsedFiles = parser.parse(file);
            
            List<TextFile> sumarios = new ArrayList<>();
            TextFile fallo = null;


            for (TextFile parsedFile : parsedFiles) {
            	if ("sumario".equals(parsedFile.type())) {
            		//if (sumarios.isEmpty()) {
            			sumarios.add(parsedFile);
            		//}
            	}
            	else {
            		fallo = parsedFile;
            	}
            }
            
    		KbeeContent kbfallo = processFallo(fallo);

    		if (kbfallo!=null) {
	            for (TextFile summario : sumarios) {
	            	processSumario(kbfallo, summario);
	            }
    		}

            logger.debug(
                    "Archivo {}: {} documentos generados",
                    path.getFileName(),
                    parsedFiles.size()
            );

        } catch (Exception e) {

            logger.error(
                    "Error procesando archivo {}",
                    path,
                    e
            );
        }
    }

    
    protected void processSumario(KbeeContent fallo, TextFile file) {
		Transaction transaction = null;
    	try {
    		long start = System.currentTimeMillis();
    		transaction = beginTransaction();
			com.novamens.hibernate.session.Session.setApi(true);
			
	    	String id = file.id();
	    	id = id.replace("sumario-fallo-", "");
	    	KbeeContent sumario = getOrCreateSumario(id);

    		List<String> updates = updateSumario(fallo, sumario, file);
    		
	   		if (!updates.isEmpty()) {
					getContentDao().save(sumario);
					transaction.commit();
		    		long end = System.currentTimeMillis();
		    		double seconds = (end - start) / 1000.0;
		    		System.out.printf("%s, (ok) en %.3f segundos%n", file.id(), seconds);
					getLogger().info(file.id() + ", (ok)");
    		}
    		else {
	    	   		transaction.rollback();
    		}
		}
		catch (Exception e) {
			e.printStackTrace();
			getLogger().error(e);
			transaction.rollback();
		}
    	
    }

    
    protected KbeeContent processFallo(TextFile file) {
		Transaction transaction = null;
    	try {
    		long start = System.currentTimeMillis();
    		
    		transaction = beginTransaction();
			com.novamens.hibernate.session.Session.setApi(true);
			
	    	String id = file.id();
	    	id = id.replace("fallo-", "");
	    	KbeeContent content = getOrCreateFallo(id);
	    	
    		List<String> updates = updateFallo(content, file);
    		
    		if (!updates.isEmpty()) {
				getContentDao().save(content);
				transaction.commit();
	    		long end = System.currentTimeMillis();
	    		double seconds = (end - start) / 1000.0;
	    		System.out.printf("%s, (ok) en %.3f segundos%n", file.id(), seconds);
				getLogger().info(file.id() + ", (ok)");
    		}
    		else {
	    		long end = System.currentTimeMillis();
	    		double seconds = (end - start) / 1000.0;
	    		System.out.printf("%s, (no update) en %.3f segundos%n", file.id(), seconds);
    	   		transaction.rollback();
    		}
    		
    		return content;
    	}
    	catch (Exception e) {
			getLogger().error(e);
    		transaction.rollback();
    		return null;
    	}
    }
    
	@SuppressWarnings("unchecked")
    protected List<String> updateFallo(KbeeContent fallo, TextFile file) throws Exception {
    	
    	String id = fallo.getExternalId();
    	
    	List<String> updates = new ArrayList<String>();
    	
    	String title = file.title();
    	
    	String text = textToHtml(file.text());
    	
    	OffsetDateTime fecha = file.date();
    	
    	String cita =  (String)file
    		.metadata(FileParser.META_CITATION);
    	
    	String paginaInicio =  String.valueOf((Integer)file
        		.metadata(FileParser.META_START_PAGE));

    	String paginaFin =  String.valueOf((Integer)file
        		.metadata(FileParser.META_END_PAGE));

    	String tomo =  (String)file
        		.metadata(FileParser.META_VOLUME_YEAR);
    	
    	String saij =  (String)file
        		.metadata(FileParser.META_SAIJ_NUMBER);

    	String cuij =  (String)file
        		.metadata(FileParser.META_CUIJ_NUMBER);
    	
    	String resolucion =  (String)file
        		.metadata(FileParser.META_RESOLUTION_NUMBER);
    	
    	EntityMember tribunal = getTribunal(
    		(String)file.metadata(FileParser.META_TRIBUNAL));
    	
		List<EntityMember> jueces = getJueces(
        	(List<String>)file.metadata(FileParser.META_JUDGES));
		
		List<EntityMember> disidentes = new ArrayList<>();
		
		for (EntityMember juez : jueces) {
			String nombre = juez.getStrValue();
			String apellido = nombre.substring(nombre.lastIndexOf(",")+1).trim();
			if (apellido!=null) {
				String tag = apellido.toUpperCase() + " (en disidencia)";
				if (text.indexOf(tag)>0) {
					disidentes.add(juez);
				}
			}
		}
		
    	List<String> citasJurispudencia = new ArrayList<>();
    	List<String> referenciasNormativas = new ArrayList<>();
		if (getList(fallo, "citajurisprudencia").isEmpty() &&
				getList(fallo, "referencia").isEmpty()) {
			DocumentReferencesResponse references = getRefereces(file.text());
	    	for (LegalReference reference : references.getJurisprudence()) {
	    		String citation = reference.getCitation();
	    	    citation = citation.trim();

	    	    if (citation.length() >= 2
	    	            && citation.startsWith("\"")
	    	            && citation.endsWith("\"")) {
	    	        citation = citation.substring(1, citation.length() - 1);
	    	    }
	    		citation += "|" + reference.getEvidence(); 
	    		citasJurispudencia.add(citation);
	    	}
	    	for (LegalReference reference : references.getRegulations()) {
	    		referenciasNormativas.add(reference.getCitation());
	    	}
		}
		else {
			for (Object object : getList(fallo, "citajurisprudencia")) {
				citasJurispudencia.add(object.toString());
			}
			for (Object object : getList(fallo, "referencia")) {
				referenciasNormativas.add(object.toString());
			}
		}
   	
		DataSetMember tipo = getMember(
			"tipo", 
			"Fallo");
    	
		if (!equals(get(fallo, "id"), id)) {
			set(fallo, "id", id);
			updates.add("id");
		}
		
		if (!equals(get(fallo, "caratula"), title)) {
			set(fallo, "caratula", title);
			updates.add("title");
		}
    	
		if (!equals(get(fallo, "texto"), text)) {
			set(fallo, "texto", text);
			updates.add("text");
		}
		
		if (!equals(getDate(fallo, "fecha"), fecha)) {
			set(fallo, "fecha", fecha);
			updates.add("fecha");
		}
		
		if (!equals(get(fallo, "cita"), cita)) {
			set(fallo, "cita", cita);
			updates.add("cita");
		}
		
		if (!equals(get(fallo, "numerosaij"), saij)) {
			set(fallo, "numerosaij", saij);
			updates.add("numero saij");
		}
		
		if (!equals(get(fallo, "numerocuij"), cuij)) {
			set(fallo, "numerocuij", cuij);
			updates.add("numero cuij");
		}
		
		if (!equals(get(fallo, "paginainicio"), paginaInicio)) {
			set(fallo, "paginainicio", paginaInicio);
			updates.add("pagina inicio");
		}
		
		if (!equals(get(fallo, "paginafin"), paginaFin)) {
			set(fallo, "paginafin", paginaFin);
			updates.add("pagina fin");
		}
		
		if (!equals(get(fallo, "tomo"), tomo)) {
			set(fallo, "tomo", tomo);
			updates.add("tomo");
		}
		
		if (!equals(get(fallo, "resolucion"), resolucion)) {
			set(fallo, "resolucion", resolucion);
			updates.add("resolucion");
		}
		
		if (!equals(get(fallo, "tribunal"), tribunal)) {
			set(fallo, "tribunal", tribunal);
			updates.add("tribunal");
		}
		
		if (!equals(getList(fallo, "juez"), jueces)) {
			set(fallo, "juez", jueces);
			updates.add("jueces");
		}
		
		if (!equals(getList(fallo, "disidencia"), disidentes)) {
			set(fallo, "disidencia", disidentes);
			updates.add("disidentes");
		}
		
		
		if (!equals(getList(fallo, "citajurisprudencia"), citasJurispudencia)) {
			set(fallo, "citajurisprudencia", citasJurispudencia);
			updates.add("citas");
		}
		
		if (!equals(getList(fallo, "referencia"), referenciasNormativas)) {
			set(fallo, "referencia", referenciasNormativas);
			updates.add("referencias");
		}
		
		if (!equals(get(fallo, "tipodocumento"), tipo)) {
			set(fallo, "tipodocumento", tipo);
			updates.add("tipo");
		}
		
		String resumen = (String)get(fallo, "resumen");
		
		if (resumen==null) {
			resumen = getResumen(file.text());
	    	String html = textToHtml(resumen);
			set(fallo, "resumen", html);
			updates.add("resumen");

		}
		
		return updates;
    }
    
    @SuppressWarnings("unchecked")
	protected List<String> updateSumario(KbeeContent fallo, KbeeContent sumario, TextFile file) throws Exception {
    	List<String> updates = new ArrayList<String>();
    	
    	String text = file.text();
    	
    	SumarioParser.ParsedSumario parsed =
    		    SumarioParser.parse(text);
    	
    	String html = textToHtml(parsed.getTexto());
    	
    	List<String> citas = parsed.getCitas();
    	
    	List<String> referencias = parsed.getReferenciasNormativas();
    	
		OffsetDateTime date = getDate(fallo, "fecha");
    	
		DataSetMember tipo = getMember(
				"tipo", 
				"Sumario");

    	
    	List<String> terms = (List<String>)file
    			.metadata(FileParser.META_THESAURUS_TERMS);
    			
    	List<DataSetMember> voces = getVoces(terms);
    	
		if (!equals(get(sumario, "texto"), html)) {
			set(sumario, "texto", html);
			updates.add("text");
		}
		
		if (!equals(getList(sumario, "voz"), voces)) {
				set(sumario, "voz", voces);
			updates.add("voces");
		}
		
		if (!equals(getRelation(sumario, "fallo"), fallo)) {
			set(sumario, "fallo", fallo);
			updates.add("fallo");
		}
		
		if (sumario.getTitle()==null ||
			"_".equals(sumario.getTitle()) ||
			"".equals(sumario.getTitle())) {
			String titulo = getTituloSumario(file.text());
			set(sumario, "titulo", titulo);
			updates.add("titulo");
		}
		
		if (!equals(getDate(sumario, "fecha"), date)) {
			set(sumario, "fecha", date);
			updates.add("fecha");
		}
		
		if (!equals(getList(sumario, "cita"), citas)) {
			set(sumario, "cita", citas);
			updates.add("citas");
		}
		
		if (!equals(getList(sumario, "referencia"), referencias)) {
			set(sumario, "referencia", referencias);
			updates.add("referencias");
		}
		
		if (!equals(get(sumario, "tipodocumento"), tipo)) {
			set(sumario, "tipodocumento", tipo);
			updates.add("tipo");
		}
    	
		return updates;
    }
    
    
    private List<DataSetMember> getVoces(List<String> terms) throws IOException, InterruptedException {
    	List<DataSetMember> voces = new ArrayList<>();
    	if (terms!=null) {
	    	for (String term : terms) {
	    		voces.addAll(getVoces(term));
	    	}
    	}
    	return voces;
    }
    
    private String getTituloSumario(String text) throws IOException, InterruptedException {
		String title = ragService.summaryTitle(text);
		return title;
    }
    
    private String getResumen(String text) throws IOException, InterruptedException {
		String resumen = ragService.writeSummary(text);
		return resumen;
    }
    
    private DocumentReferencesResponse getRefereces(String text) throws IOException, InterruptedException {
    	DocumentReferencesResponse resumen = ragService.getReferences(text);
		return resumen;
    }
    
    private List<DataSetMember> getVoces(String term) throws IOException, InterruptedException {
    	List<DataSetMember> voces = findVozByAlternativeDisplay(term);
    	if (voces == null)  {
    		voces = new ArrayList<>();
    		List<VoicePath> voices = ragService.equivalentVoices(term);
    		for (VoicePath voicePath : voices) {
    			DataSetMember voz = createVoz(voicePath);
	    		if (voz!=null) {
	    			
		    		String alternativeDisplay = voz.getAlternativeDisplayName();
		    		alternativeDisplay = alternativeDisplay!=null
		    			? alternativeDisplay + ", " + term
		    			: term;		
					voz.setAlternativeDisplayName(alternativeDisplay);
					getContentDao().save(voz);
					voces.add(voz);
	    		}
    		}
    		if (voices.isEmpty()) {
    			DataSetMember noDef = getMember(
    					"tesauro", 
    					"NoDef");
	    		String alternativeDisplay = noDef.getAlternativeDisplayName();
	    		alternativeDisplay = alternativeDisplay!=null
	    			? alternativeDisplay + ", " + term
	    			: term;		
				noDef.setAlternativeDisplayName(alternativeDisplay);
				getContentDao().save(noDef);
				getContentDao().flush();
    		}
    	}
    	List<DataSetMember> finalvoces = new ArrayList<>();
    	for (DataSetMember voz : voces) {
    		if (!"NoDef".equals(voz.getStrValue())) {
    			finalvoces.add(voz);
    		}
    	}
    	return finalvoces;
    }
    
	private DataSetMember createVoz(VoicePath path) {
		DataSetMember vozmember = null;
		DataSetMember broadermember = null;
		DataSet dataset =  getDataSet("Tesauro");
		for (VoiceNode voz : path.getPath()) {
			vozmember = getContentDao().findMemberByExternalId(voz.getId());
			if (vozmember==null) {
				vozmember= dataset.createMember();
				vozmember.setExternalId(voz.getId());
				vozmember.setStrValue(voz.getPreferredLabel());
				if (broadermember!=null) {
					List<DataSetMember> parents = List.of(broadermember);
					((KbeeDataSetMember)vozmember).setParents(parents);
					set(vozmember, "general", broadermember);
				}
				broadermember = vozmember;
				getContentDao().save(vozmember);
				getContentDao().flush();
				vozmember = (DataSetMember)getContentDao().reload(vozmember);
			}
			else {
				broadermember = vozmember;
			}
		}
		return vozmember;
	}
    
	private List<DataSetMember> findVozByAlternativeDisplay(String term) {
		try {
	        String hql = "FROM KbeeDataSetMember D WHERE D.alternative_display like '%" 
	        	+ term.trim()
	        	+"' OR  D.alternative_display like '%" 
	        	+ term.trim() +",%'";
	        org.hibernate.query.Query<?> query = getSessionFactory().getCurrentSession().createQuery(hql);
	        List results = query.list();
	        List<DataSetMember> members = (List<DataSetMember>) results;
	        if (members.isEmpty()) {
	        	return null;
	        }
	        return members;
		}
		catch(Exception e) {
			e.printStackTrace();
			if (logger.isDebugEnabled()) {
				logger.error(e.getMessage(), e);
			}
			else {
				logger.info("error "+e.getMessage());
			}
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, 
				ApiError.INTERNAL_ERROR, e.getMessage());
		}
	}
    
    public static class JudgeName {

        private final String firstName;
        private final String lastName;

        public JudgeName(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public String firstName() {
            return firstName;
        }

        public String lastName() {
            return lastName;
        }
    }
    
    @Data
    @AllArgsConstructor
    class FullName {
    	public String firstName;
    	public String lastName;
    	public String normalized() {
    		return firstName + ", "+lastName;
    	}
    }

    private FullName getFullName(String fullName) {

        if (fullName == null || fullName.isBlank()) {
            return null;
        }

        String normalized = fullName.trim().replaceAll("\\s+", " ");
        String[] parts = normalized.split(" ");

        int surnameStart = parts.length;

        for (int i = parts.length - 1; i >= 0; i--) {

            String part = parts[i];

            if (part.equals(part.toUpperCase(Locale.ROOT))
                    && part.codePoints().anyMatch(Character::isLetter)) {
                surnameStart = i;
            } else {
                break;
            }
        }

        if (surnameStart == 0 || surnameStart == parts.length) {
            return null;
        }

        String firstName = String.join(
                " ", java.util.Arrays.copyOfRange(parts, 0, surnameStart));

        String lastName = Arrays.stream(parts, surnameStart, parts.length)
                .map(part -> part.substring(0, 1).toUpperCase(Locale.ROOT)
                        + part.substring(1).toLowerCase(Locale.ROOT))
                .collect(Collectors.joining(" "));
        
        
        return new FullName(firstName, lastName);
    }
    
    
	private List<EntityMember> getJueces(List<String> names) {
		List<EntityMember> jueces = new ArrayList<>();
		for (String name : names) {
			EntityMember juez = getJuez(name);
			jueces.add(juez);
		}
		return jueces;
	}

    
	private EntityMember getJuez(String name) {
		DataSet dataset =  getDataSet("Juez");
		FullName fullName = getFullName(name);
		DataSetMember juez = getContentDao().findMemberByValue(dataset, fullName.normalized());
		if (juez == null) {
			juez = dataset.createMember();
			juez.setStrValue(fullName.normalized());
			juez.setDomain(dataset.getDomain());
			getContentDao().save(juez);
			set(juez, "nombre", fullName.getFirstName());
			set(juez, "apellido", fullName.getLastName());
			getContentDao().save(juez);
			juez = (DataSetMember)getContentDao().reload(juez);
		}
		return (EntityMember)juez;
	}

    
	private EntityMember getTribunal(String name) {
		DataSet dataset =  getDataSet("Tribunal");
		DataSetMember tribunal = getContentDao().findMemberByValue(dataset, name);
		if (tribunal == null) {
			tribunal= dataset.createMember();
			tribunal.setStrValue(name);
			getContentDao().save(tribunal);
			tribunal = (DataSetMember)getContentDao().reload(tribunal);
		}
		return (EntityMember)tribunal;
	}
	
	
	private DataSetMember getMember(String datasetname, String value) {
		DataSet dataset =  getDataSet(datasetname);
		DataSetMember member = getContentDao().findMemberByValue(dataset, value);
		if (member == null) {
			member = dataset.createMember();
			member.setStrValue(value);
			getContentDao().save(member);
			member = (DataSetMember)getContentDao().reload(member);
		}
		return member;
	}
    
    private KbeeContent getOrCreateFallo(String id) {
		KbeeContent content = (KbeeContent)getContentDao().findContentByExternalId("pjsf", id);
		if (content==null) {
			content = (KbeeContent)createContent("fallo");
			content.setExternalId(id);
		}
		return content;
     }
    
    
    private KbeeContent getOrCreateSumario(String id) {
		KbeeContent content = (KbeeContent)getContentDao().findContentByExternalId("pjsf", id);
		if (content==null) {
			content = (KbeeContent)createContent("sumario");
			content.setExternalId(id);
		}
		return content;
     }
    
	private Content createContent(String classname) {
		try {
 			Content content = ServiceLocator
				.getService(ContentFactoryService.class)
				.create(classname, false, true);
			content.setCheckinOffsetDateTime(OffsetDateTime.now());
			getContentDao().save(content);
			Source source = getContentDao().findSourceByName("pjsf");
			((KbeeContent)content).setSource(source);
			return content;
		}
		catch (ContentCreationException | ContentMgmtException e) {
			logger.error(e.getLocalizedMessage(), e);
			throw new ApiException(
				HttpStatus.INTERNAL_SERVER_ERROR, 
				ApiError.INTERNAL_ERROR, e.getMessage());
		}
	}
	
	
	private Object get(Classificable content, String attribute) {
		Object value = (new KbeeClassificableScriptWrapper(content)).getValue(attribute);
		if (value instanceof KbeeClassificableScriptWrapper) {
			value = ((KbeeClassificableScriptWrapper)value).getObject();
		}
		return value;
	}
	
	private List<?> getList(KbeeContent content, String attribute) {
		return (new KbeeClassificableScriptWrapper(content)).getValues(attribute);
	}
	
	private KbeeContent getRelation(KbeeContent content, String name) {
		KbeeContent related = null;
		for (Relation relation : (new KbeeClassificableScriptWrapper(content)).getRelations(name)) {
			related = (KbeeContent)relation.getTarget();
			break;
		};
		return related;
	}
	
	private OffsetDateTime getDate(KbeeContent content, String attribute) {
		return (new KbeeClassificableScriptWrapper(content)).getDateTime(attribute);
	}
	
	private void set(Classificable content, String attribute, Object value) {
		(new KbeeClassificableScriptWrapper(content)).setValue(attribute, value);
	}
	
	private void set(KbeeContent content, String relation, KbeeContent target) {
		content.setRelations(new ArrayList<>());
		(new KbeeClassificableScriptWrapper(content)).addRelation(relation, target);
	}
	
	private boolean equals(Object s1, Object s2) {
		if (s1!=null && !s1.equals(s2))
			return false;
		if (s2!=null && !s2.equals(s1))
			
			return false;
		return true;
	}
	
	private boolean equals(KbeeContent s1, KbeeContent s2) {
		Long id1 = s1!=null ? s1.getId() : null;
		Long id2 = s2!=null ? s2.getId() : null;
		return equals(id1, id2);
	}

	
	private boolean equals(List<?> s1, List<?> s2) {
		if (s1!=null && !s1.equals(s2))
			return false;
		if (s2!=null && !s2.equals(s1))
			return false;
		return true;
	} 

	
//	private boolean equals(String s1, String s2) {
//		if (s1!=null && !s1.equals(s2))
//			return false;
//		if (s2!=null && !s2.equals(s1))
//			return false;
//		return true;
//	}
	
	
	    public String textToHtml(String text) {

	        if (text == null || text.isBlank()) {
	            return "";
	        }

	        // Normalizar saltos de línea
	        String normalized = text
	                .replace("\r\n", "\n")
	                .replace("\r", "\n");

	        // Separar párrafos por una o más líneas vacías
	        return Arrays.stream(normalized.split("\\n\\s*\\n"))
	                .map(String::trim)
	                .filter(p -> !p.isEmpty())
	                .map(this::escapeHtml)
	                .map(p -> p.replaceAll("\\n", " "))
	                .map(p -> "<p>" + p + "</p>")
	                .collect(Collectors.joining("\n"));
	    }

	    private String escapeHtml(String text) {
	        return text
	                .replace("&", "&amp;")
	                .replace("<", "&lt;")
	                .replace(">", "&gt;")
	                .replace("\"", "&quot;")
	                .replace("'", "&#39;");
	    }
	
    private ApiFile createApiFile(Path path) {
    	return new PathFile(path);
    }
    
	private DataSet getDataSet(String name) {
		for (DataSet dataset : getContentDao().getDataSets(getDomain())) {
			if (name.toLowerCase().equals(dataset.getAlias().toLowerCase()) || name.equals(dataset.getName())) {
				return dataset;
			}
		}
		return null;
	}
	
	private String getLoggerName(String lote) {
		String name = logsPath +"/importacion-" + lote.toLowerCase() + "-";
		DateFormat format = new SimpleDateFormat("MM-dd-yyyy");
		name += format.format(new Date());
		name += "-" + String.valueOf(getId()) + ".log";
		return name;
	}
	
	private SessionFactory getSessionFactory() {
		return (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
	}
    
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}