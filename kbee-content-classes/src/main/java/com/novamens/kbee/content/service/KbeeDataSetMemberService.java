package com.novamens.kbee.content.service;

import java.util.ArrayList;
import java.util.List;

import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.ExtractionRule;
import com.novamens.content.service.DataSetMemberService;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.query.AggregationQuery;

import kbee.util.logging.Logger;

public class KbeeDataSetMemberService implements DataSetMemberService {
			
	Logger logger = Logger.getLogger(KbeeDataSetMemberService.class.getName());
	
	private DataSetMember member  = null;
	
	public KbeeDataSetMemberService() {
	}

	public KbeeDataSetMemberService(DataSetMember member) {
		 this.member = member;
	}
	
	
	public DataSetMember getMember() {
		return member;
	}
	
	public String getSubline() {
		String subline = "";
		ExtractionRule rule = getMember().getDataSet().getSublineRule();
		if (rule!=null) {
			subline= (String)rule.extract(getMember());
		}
		return subline;
	}
	
	public List<String> getPaths() {
		return getPaths(member, member);
	}
	
	public List<DataSetMember> getAggregations(DataSet ds) {
		List<DataSetMember> aggregations = new ArrayList<>();
        Query query = new AggregationQuery(getQueryIndex(), ds, getMember());
        ResultSet resultSet = query.execute();
        while (resultSet.hasNext()) {
        	aggregations.add((DataSetMember)resultSet.next().getObject());
        }
        return aggregations;
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
	
	protected Index getQueryIndex() {
		return getMember()
			.getDomain()
			.getService(JavaIndexerService.class).getIndex();
	}
}
