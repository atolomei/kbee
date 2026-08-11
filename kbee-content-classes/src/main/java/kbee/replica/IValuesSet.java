package kbee.replica;

import java.util.ArrayList;
import java.util.List;

import kbee.api.model.ApiValue;
import kbee.api.model.ApiDataSet;
import kbee.api.model.IResultSet;
import kbee.api.service.ApiService;

public class IValuesSet extends IResultSet<ApiValue> {
	private static final long serialVersionUID = 1L;
	
	private long total = -1;
	private int index = 0;
	ApiService api;
	IResultSet<ApiValue> resultSet = null;
	private List<ApiDataSet> datasets = null;
	
	public IValuesSet(ApiService api) {
		super(null);
		this.api=api;
	}
	
	@Override
	public boolean hasNext() {
		if (index>=getDataSets().size()) {
			return false;
		}	
		if (resultSet==null) {
			resultSet = api.getValues(getDataSets().get(index));
		}	
		if (!resultSet.hasNext()) {
			resultSet=null;
			index++;
			return hasNext();
		}
		return true;
	}
	
	@Override
	public ApiValue next() {
		return resultSet.next();
	}
	
	@Override
	public long getSize() {
		if (total<0) {
			for (ApiDataSet dataset : getDataSets()) {
				total += api.getValues(dataset).getSize();
			}
		}
		return total;
	}
	
	private List<ApiDataSet> getDataSets() {
		if (datasets==null) {
			datasets = new ArrayList<>();
			for (ApiDataSet dataset : api.getDataSets()) {
				if (!"DATE".equals(dataset.getType()) && !"USER".equals(dataset.getType()) && !"EXTERNAL".equals(dataset.getType())) {
					datasets.add(dataset);	
				}
			}
		}
		return datasets;
	}
}