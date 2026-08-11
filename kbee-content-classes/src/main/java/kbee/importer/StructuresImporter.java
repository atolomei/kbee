package kbee.importer;

import java.util.ArrayList;
import java.util.List;

import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetElementTemplate;
import com.novamens.content.model.Multiplicity;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.model.KbeeAttribute;
import com.novamens.kbee.content.model.KbeeClassifier;
import com.novamens.kbee.content.model.KbeeDataSet;
import com.novamens.kbee.content.model.KbeeDataSetElementTemplate;

import kbee.api.model.ApiProxy;
import kbee.api.model.ApiClassifier;
import kbee.api.model.ApiDataSet;
import kbee.api.model.IModelAttribute;
import kbee.api.model.IModelElement;
import kbee.api.service.ApiService;

@Deprecated
public class StructuresImporter extends ClassificablesImporter {
	
	private int total = 0;
	private int updated = 0;

	public StructuresImporter(ApiService server, Domain domain, LocalMatcher matcher) {
		super(server, matcher);
		setDomain(domain);
	}
	
	public void execute() throws ContentMgmtException  {
		int i = 0;
		try {
			for (ApiDataSet remote : getRemoteDataSets()) {
				DataSet local = getLocal(KbeeDataSet.class, remote);
				if (local!=null && (remote.getLastModifiedDate().isAfter(local.getLastModifiedOffsetDateTime()) || forceUpdate()) && !"USER".equals(remote.getType())) {
					syncStructure(remote, local);
					update(local);
					updated++;
					setProgress(i);
					logger.info("DataSet "+local.getDisplayName());
				}
				else {
					logger.info("DataSet "+remote.getDisplayName() + " not modified");
				}
			}
		}
		catch (Throwable e) {
			logger.error(e);
			throw new ContentMgmtException(e);
		}
	}

	@Override
	public int getTotal() {
		if (total == 0) {
			total = getRemoteDataSets().size();
		}
		return total;
	}

	@Override
	public String getResult() {
		String result = "<p>"+String.valueOf(getTotal())+" datasets processed. ";
		result += String.valueOf(updated)+" datasets updated</p>";
		return result;
	}
	
	private void syncStructure(ApiDataSet remote, DataSet local) {
		List<DataSetElementTemplate> structure = new ArrayList<DataSetElementTemplate>();
		for (IModelElement itemplate : remote.getStructure()) {
			DataSetElementTemplate template = new KbeeDataSetElementTemplate();
			if ("classifier".equals(itemplate.getAttribute().getRel())) {
				((KbeeDataSetElementTemplate)template).setClassifier(getClassifier(itemplate.getAttribute()));
			}
			else {
				((KbeeDataSetElementTemplate)template).setAttribute(getAttribute(itemplate.getAttribute()));
			}
			((KbeeDataSetElementTemplate)template).setMultiplicity(Multiplicity.valueOf(itemplate.getMutiplicity()));
			structure.add(template);
		}
		((KbeeDataSet)local).setStructure(structure);
	}
	
	private Classifier getClassifier(ApiProxy proxy) {
		ApiClassifier remote = getServer().getClassifier(proxy.getId());
		Classifier local = getLocal(KbeeClassifier.class, remote);
		return local;
	}
	
	private Attribute getAttribute(ApiProxy proxy) {
		IModelAttribute remote = getServer().getAttribute(proxy.getId());
		Attribute local = getLocal(KbeeAttribute.class, remote);
		return local;
	}
	
	private List<ApiDataSet> getRemoteDataSets() {
		return getServer().getDataSets();
	}
}