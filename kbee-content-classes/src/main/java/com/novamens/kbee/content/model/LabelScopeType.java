package com.novamens.kbee.content.model;

import com.novamens.content.base.PersistentEnumUserType;
import com.novamens.content.model.LabelScope;

public class LabelScopeType extends PersistentEnumUserType<LabelScope> {

	@Override
	public Class<LabelScope> returnedClass() {
		return LabelScope.class;
	}

	/*
	public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
			return super.nullSafeGet(rs, names, owner);
	}

	public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
		if (value == null) {
			st.setNull(index, Types.INTEGER);
		} 
		else {
			st.setInt(index, ((PersistentEnum)value).getId());
		}
	}*/

}
