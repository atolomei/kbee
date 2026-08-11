package com.novamens.kbee.content.service;

import java.util.Map;

import com.novamens.content.base.Content;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.DataSetMember;
import com.novamens.indexer.iql.IqlQuery;
import com.novamens.indexer.iql.IqlService;
import com.novamens.kbee.template.KbeeContentTemplateModel;
import com.novamens.kbee.text.KbeeTextTemplate;
import com.novamens.solr.indexer.iql.SolrIqlQuery;
import com.novamens.text.TextTemplate;

import kbee.util.logging.Logger;

public class KbeeIqlAccessService extends KbeeAccessService {
	
	private static Logger logger = Logger.getLogger(KbeeIqlAccessService.class.getName());
	
	public KbeeIqlAccessService(ClassifierTemplate template) {
		super(template);
	}
	
	public boolean isReadable(DataSetMember value) {
		return true;
	}
	
	@Override
	protected String getStatement(String pattern, 
			Classificable object, 
			Map<String, Object> parameters) {
		
		// object es el contenido (idoc) de contexto para el que se hace la busqueda.
		// el values criteria es un template (freemaker) que se evalua sobre el 
		// contenido para obtener la sentencia iql
		
		String statement = super.getStatement(pattern, object, parameters);
		
		try {
			
			TextTemplate iqltemplate = new KbeeTextTemplate(getRelationTemplate().getValuesCriteria());
			KbeeContentTemplateModel model = new KbeeContentTemplateModel((Content)object); 
			String iql = iqltemplate.process(model);
			IqlService iqlservice = getDomain().getService(IqlService.class);
			IqlQuery query = iqlservice.getNewQuery(iql);
			String solrcriteria = ((SolrIqlQuery)query).getSolrStatement();
			if (solrcriteria!=null && !"".equals(solrcriteria)) {
				statement += " AND ("+solrcriteria +")";
			}
		}
		catch (Exception e) {
			logger.error(e);
		}
		
		return statement;
	}
}