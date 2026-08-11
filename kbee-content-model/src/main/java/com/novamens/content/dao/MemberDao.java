package com.novamens.content.dao;

import java.util.List;

import com.novamens.content.model.DataSetMember;
import com.novamens.dao.Dao;

public interface MemberDao extends Dao {
	public DataSetMember findMemberById(String value);
	public DataSetMember findMemberByExternalId(String value);
	public List<DataSetMember> findMembersLike(String value);
	public List<DataSetMember> findAll(String iqlstatement);
}
