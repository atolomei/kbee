package com.novamens.kbee.content.webapi.type;

import java.util.ArrayList;
import java.util.List;

import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.ExternalSet;
import com.novamens.content.model.ModelElement;
import com.novamens.content.model.ModelElementTemplate;
import com.novamens.dom.Domain;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.model.KbeeDataSet;
import com.novamens.kbee.content.model.KbeeExternalSet;
import com.novamens.kbee.content.webapi.query.DataSetQuery;

import kbee.api.model.ApiProxy;
import kbee.api.model.ApiDataSet;
import kbee.api.model.IModelElement;

public class IDataSetAdapter implements Adapter<DataSet, ApiDataSet> {
	
	public IDataSetAdapter() {
	}
	
	public ApiDataSet adapt(DataSet dataset) {
		
		ApiDataSet idataset = new ApiDataSet();
		
		idataset.setDisplayName(dataset.getDisplayName());
		idataset.setAlias(dataset.getAlias());
		idataset.setDisplayNameEditable(dataset.isDisplayNameEditable());
		idataset.setDisplayNameRule(((KbeeDataSet)dataset).getDisplayNameTemplate());
		idataset.setSublineRule(((KbeeDataSet)dataset).getSublineTemplate());
		idataset.setDomain(dataset.getDomain().getName());
		idataset.setId(String.valueOf(dataset.getId()));
		idataset.setState(String.valueOf(dataset.getState().name()));
		idataset.setType(String.valueOf(dataset.getDataSetType().name()));
		idataset.setSize(getSize(dataset));
		idataset.setHierachical(dataset.isHierachical());
		idataset.setLastModifiedDate(dataset.getLastModifiedOffsetDateTime());
		idataset.setLastModifiedUser(new ApiUserProxy(dataset.getLastModifiedUser()));
		
		List<IModelElement> structure = new ArrayList<IModelElement>();
		for (ModelElementTemplate template : dataset.getStructure()) {
			if (template!=null) {
				IModelElement element = new IModelElement();
				element.setAttribute(getProxy(template.getElement()));
				element.setParent(template.getParent()!=null ? getProxy(template.getParent()) : null);
				element.setMutiplicity(template.getMultiplicity()!=null ? template.getMultiplicity().name() : null);
				structure.add(element);
			}
		}
		
		idataset.setStructure(structure);
		
		if (dataset instanceof ExternalSet) {
			idataset.setSubtype(String.valueOf(((KbeeExternalSet)dataset).getExternalSubtype()));
		}

		return idataset;	
	}
	
	public ApiProxy getProxy(ModelElement element) {
		ApiProxy proxy = new ApiProxy(UriHelper.getUri(element));
		proxy.setId(String.valueOf(element.getId()));
		proxy.setRel(element instanceof Classifier ? "classifier" : "attribute");
		proxy.setName(element.getAlias());
		return proxy;
	}
	
	public long getSize(DataSet dataset) {
		Query query = new DataSetQuery(getQueryIndex(dataset.getDomain()), dataset);
		ResultSet resultSet = query.execute();
		long size = resultSet.size();
		return size;
	}
	
	protected Index getQueryIndex(Domain domain) {
		return domain.getService(JavaIndexerService.class).getIndex();
	}
	
}
