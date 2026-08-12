package com.novamens.solr.indexer.service;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import org.apache.lucene.search.BooleanQuery;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.BaseHttpSolrClient.RemoteSolrException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.HighlightParams;

import com.novamens.indexer.java.KbeeJavaIndex;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.TextQuery;
import com.novamens.indexer.service.Document;
import com.novamens.indexer.service.IndexerException;

import com.novamens.metrics.SystemMetricsService;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.multidimensional.SolrCube;
import com.novamens.solr.indexer.multidimensional.SolrFacet;


/**
 * 
 * 
 * 1. SolrSchema in Solr 8.x
 * 
 * schema.xml
 * 
 * The field must exist
 * 		<field name="organization"		type="string"   	indexed="true"  stored="true"  multiValued="false" />
 * 
 * 
 * 2. Project: kbee-content
 *    content-index-context.xml
 *    <bean class="com.novamens.indexer.java.AttributeFieldSchema">
					<property name="fieldName" value="title"/>
					<property name="path" value="name"/>
	  </bean>
 * 
 * 
 * 
 * 3. KbeeContentDao
 *    findObjectById must retrieve the Object
 * 
 * 
 * 4. JavaContentIndexFactory
 *    if the Java class is not Content
 *    the default-schema must be added
 *    
 *
 */
public class SolrIndex extends KbeeJavaIndex {
				
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SolrIndex.class.getName());

		
	private SolrCore solrServer;
	private SolrCube cube;
	 
	public static String DOCUMENT_ID_FIELD = "id";
	public static String DOCUMENT_LASTMODIFIEDTIME_FIELD = "lastmodifiedtime";
	
	
	public void commit() throws IndexerException {
		try {
			getServer().commit();
		}
		catch (IOException e) {
			logger.error(e);
			throw new IndexerException(e);
		}
		catch (SolrServerException e) {
			logger.error(e);
			throw new IndexerException(e);
		}
	}
	
	/**
	 * 
	 * Cada documento se indexa 2 veces 
	 * 
	 * Cuando se actualiza se ejecuta la indexación de la metainfo en 1 pasada 
	 * 2 pasada (via scheduler) documentos con attachments
	 * 
	 * La indexación sincronica se hace en el AfterCommit 
	 * la asincronica es transaccional con la DB TRX 
	 * 
	 */
	public void indexDocument(Document document) throws IndexerException {
		indexDocument(document, false);
	}
	
	public void indexDocument(Document document, boolean force) throws IndexerException {
		try {
			SolrInputDocument solrdoc = new SolrInputDocument();
			
			for (String fieldName : document.getFieldNames()) {
				solrdoc.addField(fieldName, document.getFieldValue(fieldName));
			}
			
			solrdoc.addField(DOCUMENT_ID_FIELD, document.getId());

			if (document.getLastModifiedOffsetDateTime()!=null) {
				SolrDocument version = get(document.getId());
				if (version!=null && !force) {
					Object lastmodfiedvalue = version.getFieldValue(DOCUMENT_LASTMODIFIEDTIME_FIELD);
					if (lastmodfiedvalue!=null) {
						if ((Long)lastmodfiedvalue >= Long.valueOf(document.getLastModifiedOffsetDateTime().toInstant().toEpochMilli())) {
							logger.warn("INDEX OPTIMISTIC LOCK FAIL");
							return;
						}
					}
				}
				solrdoc.addField(DOCUMENT_LASTMODIFIEDTIME_FIELD, document.getLastModifiedOffsetDateTime().toInstant().toEpochMilli());
			}
			
			getServer().add(solrdoc);
			ServiceLocator.getService(SystemMetricsService.class).getMeterIndexTasks().mark();
		}
		catch (IOException e) {
			logger.error(e);
			throw new IndexerException(e);
		}
		catch (SolrServerException e) {
			logger.error(e);
			throw new IndexerException(e);
		}
		catch (SolrException e) {
			logger.error(e);
			throw new IndexerException(e);
		}
	}
	
	public void reindexDocument(Document document, String... field) throws IndexerException {
		try {
			SolrInputDocument solrdoc = new SolrInputDocument();
			
			for (String fieldName : document.getFieldNames()) {
				boolean set = false;
				for (int i=0; i<field.length; i++) {
					if (field[i].equals(fieldName)) {
						set = true;
						break;
					}
				}
				if (set) {
					Map<String,Object> fieldModifier = new HashMap<>(1);
					fieldModifier.put("set", document.getFieldValue(fieldName));
					solrdoc.addField(fieldName, fieldModifier);
				}
			}
			
			solrdoc.addField(DOCUMENT_ID_FIELD, document.getId());

//			if (document.getLastModifiedOffsetDateTime()!=null) {
//				SolrDocument version = get(document.getId());
//				if (version!=null) {
//					Object lastmodfiedvalue = version.getFieldValue(DOCUMENT_LASTMODIFIEDTIME_FIELD);
//					if (lastmodfiedvalue!=null) {
//						if ((Long)lastmodfiedvalue >= Long.valueOf(document.getLastModifiedOffsetDateTime().toInstant().toEpochMilli())) {
//							logger.warn("INDEX OPTIMISTIC LOCK FAIL");
//							return;
//						}
//					}
//				}
//				solrdoc.addField(DOCUMENT_LASTMODIFIEDTIME_FIELD, document.getLastModifiedOffsetDateTime().toInstant().toEpochMilli());
//			}
			
			getServer().add(solrdoc);
			ServiceLocator.getService(SystemMetricsService.class).getMeterIndexTasks().mark();
		}
		catch (IOException e) {
			logger.error(e);
			throw new IndexerException(e);
		}
		catch (SolrServerException e) {
			logger.error(e);
			throw new IndexerException(e);
		}
		catch (SolrException e) {
			logger.error(e);
			throw new IndexerException(e);
		}
	}

	
	public void delete(Serializable id) throws IndexerException {
		try {
			getServer().deleteById(id.toString());
		}
		catch (IOException e) {
			logger.error(e);
			throw new IndexerException(e);
		}
		catch (SolrServerException e) {
			logger.error(e);
			throw new IndexerException(e);
		}
		catch (SolrException e) {
			logger.error(e);
			throw new IndexerException(e);
		}
	}

	public SolrDocument get(Serializable id) throws IndexerException {
		try {
			SolrDocument document = null;
			SolrQuery query = new SolrQuery();
			query.setQuery(DOCUMENT_ID_FIELD+":"+id.toString());
			query.setFields("id", DOCUMENT_LASTMODIFIEDTIME_FIELD);
			query.set("cache", "false");
			query.setIncludeScore(false);
			query.setFacet(false);
			QueryResponse response = getServer().query(query);
			SolrDocumentList result = response.getResults();
			if (result.size()==1) {
				document = result.get(0);
			}
			return document;
		}
		catch(IOException e) {
			logger.error(e);
			throw new IndexerException(e);
		}
		catch(SolrServerException e) {
			logger.error(e);
			throw new IndexerException(e);
		}

	}
	
	// Retorna resutset solr nativo
	public Object execute(TextQuery query) throws IndexerException {
		
		
		try {
			
			SolrQuery solrquery = new SolrQuery();
			
			String statement = query.getStatement();
			solrquery.setQuery(statement);
			
			solrquery.setStart(query.getOffset());
			solrquery.setRows(query.getPageSize());

 			solrquery.setHighlight(query.isHighlight());
			
			if (query.isHighlight()) {
				solrquery.addHighlightField("text");
				if (query.getHighlightMaxChars()!=0) {
					solrquery.set(HighlightParams.MAX_CHARS, query.getHighlightMaxChars());
				}
				solrquery.setHighlightRequireFieldMatch(true);
				solrquery.setHighlightSnippets(300);
				//solrquery.setHighlightFragsize(100);
			}
	
			solrquery.set("df", query.getDefaultField());
			
			if (query.fields()!=null)
				solrquery.setFields(query.fields());
			
			if (!query.getCache())
				solrquery.set("cache", "false");
			
			solrquery.setIncludeScore(true);
			
			if (query.getSortField()!=null && !"".equals(query.getSortField()) && !"relevance".equals(query.getSortField())) {
				if (query.getSortField().contains(",")) {
					StringTokenizer tokenizer = new StringTokenizer(query.getSortField(), ",");
					while (tokenizer.hasMoreTokens()) {
						solrquery.addSort(tokenizer.nextToken().trim(), query.isAscending() ?  ORDER.asc : ORDER.desc);
					}
				}
				else {
					solrquery.setSort(query.getSortField(), query.isAscending() ?  ORDER.asc : ORDER.desc);
				}
			}
			
			if (getCube()!=null && query.isFaceted()) {
				solrquery.setFacet(true);
				solrquery.setFacetMinCount(1);
				solrquery.setFacetLimit(-1);
				for (Facet facet : getCube().getFacets()) {
					if (facet instanceof SolrFacet)
						((SolrFacet)facet).setParameters(solrquery);
					else
						solrquery.addFacetField(facet.getName());
				}
			}
			BooleanQuery.setMaxClauseCount(2048);
			
			long t1=0, t2;
			
			if (logger.isDebugEnabled()) {
				t1 = System.currentTimeMillis();
			}

 			QueryResponse response = getServer().query(solrquery);
			
			if (logger.isDebugEnabled()) {
				t2 = System.currentTimeMillis();
				logger.debug(query.toString()+ " (" + (t2-t1)+" ms)");
			}
			
			return response;
		}
		catch (RemoteSolrException e) {
			logger.error(e);
			throw new IndexerException(e);
		}
		catch (SolrServerException e) {
			logger.error(e);
			throw new IndexerException(e);
		}
		catch (Exception e) {
			logger.error(e);
			throw new IndexerException(e);
		}
	}
	
	// simplifica la interfase de select con múltiples parametros. solo deberia usarse esta
	public QueryResponse select(com.novamens.solr.indexer.query.SolrQuery query, int start) throws IndexerException {
		
		try {
			
			SolrQuery solrquery = new SolrQuery();
			
			String statement = query.getStatement();
			String filterstatement = query.getSolrFilterStatement();
			
			if ((statement==null || "".equals(statement)) && filterstatement!=null && !"".equals(filterstatement)) {
				solrquery.setQuery(filterstatement);
			}
			else {
				solrquery.setQuery(statement);
					if (filterstatement!=null)
						solrquery.setFilterQueries(filterstatement);
			}
			
			solrquery.setStart(start);
			solrquery.setRows(query.getPageSize());

 			solrquery.setHighlight(query.includeSnippets());
			
			if (query.includeSnippets()) {
				solrquery.addHighlightField(query.getHighlightField());
				if (query.getHighlightMaxChars()!=0) {
					solrquery.set(HighlightParams.MAX_CHARS, query.getHighlightMaxChars());
				}
				solrquery.setHighlightRequireFieldMatch(true);
				solrquery.setHighlightSnippets(300);
			}
	
			if (query.isTextQuery()) {
				solrquery.set("defType", "dismax");
				solrquery.set("qf", query.getQueryFields());
				solrquery.set("mm", "2");
			}
			else {
				solrquery.set("df", query.getDefaultField());
			}
			
			if (query.fields()!=null)
				solrquery.setFields(query.fields());
			
			if (!query.getCache())
				solrquery.set("cache", "false");
			
			solrquery.setIncludeScore(true);
			
			if (query.getSortField()!=null && !"".equals(query.getSortField()) && !"relevance".equals(query.getSortField())) {
				if (query.getSortField().contains(",")) {
					StringTokenizer tokenizer = new StringTokenizer(query.getSortField(), ",");
					while (tokenizer.hasMoreTokens()) {
						solrquery.addSort(tokenizer.nextToken().trim(), query.isAscending() ?  ORDER.asc : ORDER.desc);
					}
				}
				else {
					solrquery.setSort(query.getSortField(), query.isAscending() ?  ORDER.asc : ORDER.desc);
				}
			}
			
			if (getCube()!=null && query.includeFacets()) {
				solrquery.setFacet(true);
				solrquery.setFacetMinCount(1);
				solrquery.setFacetLimit(-1);
				for (Facet facet : getCube().getFacets()) {
					if (facet instanceof SolrFacet)
						((SolrFacet)facet).setParameters(solrquery);
					else
						solrquery.addFacetField(facet.getName());
				}
			}
			BooleanQuery.setMaxClauseCount(2048);
			
			long t1=0, t2;
			
			if (logger.isDebugEnabled()) {
				t1 = System.currentTimeMillis();
			}

 			QueryResponse response = getServer().query(solrquery);
			
			if (logger.isDebugEnabled()) {
				t2 = System.currentTimeMillis();
				logger.debug(query.toString()+ " (" + (t2-t1)+" ms)");
			}
			
			return response;
		}
		catch (RemoteSolrException e) {
			logger.error(e);
			throw new IndexerException(e);
		}
		catch (SolrServerException e) {
			logger.error(e);
			throw new IndexerException(e);
		}
		catch (Exception e) {
			logger.error(e);
			throw new IndexerException(e);
		}
	}
	
	public QueryResponse select(String statement) throws IndexerException {
		return select(statement, null, null, true, 0, 25, false, false, 0, null, false, false);
	}
	
	public QueryResponse select(String statement, String filterstatment, String sortfield, boolean ascending, int start, int pagesize) throws IndexerException {
		return select(statement, filterstatment, sortfield, ascending, start, pagesize, true, false, null);
	}
	
	public QueryResponse select(String statement, String filterstatment, String sortfield, boolean ascending, int start, int pagesize, boolean facets, boolean highlight, String fields[]) throws IndexerException {
		return select(statement, filterstatment, sortfield, ascending, start, pagesize, facets, highlight, 0, fields, true, false);
	}
	
	public QueryResponse select(String statement, 
			String filterstatement, 
			String sortfield, 
			boolean ascending, 
			int start, 
			int pagesize, 
			boolean facets, 
			boolean highlight, 
			int highlightchars, 
			String fields[], 
			boolean cache,
			boolean text) throws IndexerException {
		
		try {
			
			logger.debug(filterstatement + " / " + statement);
			
			SolrQuery query = new SolrQuery();
			
			if ((statement==null || "".equals(statement)) && filterstatement!=null && !"".equals(filterstatement)) {
				query.setQuery(filterstatement);
			}
			else {
				query.setQuery(statement);
				if (filterstatement!=null)
					query.setFilterQueries(filterstatement);
			}
			
			query.setStart(start);
			query.setRows(pagesize);

 			query.setHighlight(highlight);
			
			if (highlight) {
				query.addHighlightField("title,text, metainfo,portaltext");
				if (highlightchars!=0) {
					query.set(HighlightParams.MAX_CHARS, highlightchars);
				}
				query.setHighlightRequireFieldMatch(true);
				query.setHighlightSnippets(300);
			}
			
			if (text) {
			//if (text &&	!query.getQuery().contains("*")) {
				query.set("defType", "edismax");
				query.set("qf", "title^3.0 metainfo^2.0 text^1.0 portaltext^1.0");
				query.set("mm", "1");	
				//query.set("mm", "2<75%");	
			}
			
			if (fields!=null)
				query.setFields(fields);
			
			if (!cache)
				query.set("cache", "false");
			
			query.setIncludeScore(true);

			query.getQuery();
			
			if (sortfield!=null && !"".equals(sortfield) && !"relevance".equals(sortfield)) {
				if (sortfield.contains(",")) {
					StringTokenizer tokenizer = new StringTokenizer(sortfield, ",");
					while (tokenizer.hasMoreTokens()) {
						ORDER order = ascending ?  ORDER.asc : ORDER.desc;
						String clause = tokenizer.nextToken().trim();
						if (clause.contains("asc")) {
							order = ORDER.asc;
							clause = clause.replace("asc", "").trim();
						}
						query.addSort(clause, order);
					}
				}
				else {
					query.setSort(sortfield, ascending ?  ORDER.asc : ORDER.desc);
				}
			}
			
			if (getCube()!=null && facets) {
				query.setFacet(true);
				query.setFacetMinCount(1);
				query.setFacetLimit(-1);
				for (Facet facet : getCube().getFacets()) {
					if (facet instanceof SolrFacet)
						((SolrFacet)facet).setParameters(query);
					else
						query.addFacetField(facet.getName());
				}
			}
			BooleanQuery.setMaxClauseCount(2048);
 			QueryResponse response = getServer().query(query);
			
			
			return response;
		}
		catch (RemoteSolrException e) {
			logger.error(e);
			throw new IndexerException(e);
		}
		catch (SolrServerException e) {
			logger.error(e);
			throw new IndexerException(e);
		}
		catch (Exception e) {
			logger.error(e);
			throw new IndexerException(e);
		}
	}
	
	public void setCube(SolrCube cube) {
		this.cube = cube;
	}

	public SolrCube getCube() {
		return cube;
	}
	
	public SolrCore getServer() {
		return solrServer;
	}

	public void setServer(SolrCore server) {
		this.solrServer = server;
	}

	@Override
	public String getLastModifiedOffsetDateTimeColloquial() {
		return null;
	}

	@Override
	public void setDefaultAudit() {
	}
}
