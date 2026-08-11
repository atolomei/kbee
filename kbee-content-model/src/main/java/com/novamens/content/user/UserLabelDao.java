package com.novamens.content.user;

import java.io.Serializable;
import java.util.List;

import com.novamens.content.model.LabelScope;
import com.novamens.dao.Dao;
import com.novamens.security.User;

public interface UserLabelDao  extends Dao {
	
	public UserLabel findLabelById(Serializable id);
	public List<UserLabel> findLabelsByUser(User user);
	
	public void update(UserLabel label);
	public void delete(UserLabel label);
	
	public void setLabels(User user, List<UserLabel> list);
	public List<UserLabel> findLabelsByUser(User user, LabelScope scope);
	
	
	
}