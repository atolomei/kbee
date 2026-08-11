package com.novamens.content.service;

import java.util.List;

import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.service.ObjectService;

public interface DataSetMemberService extends ObjectService {
	public String getSubline();
	public List<String> getPaths();
	public List<DataSetMember> getAggregations(DataSet ds);
}