package com.novamens.kbee.content.model;

import com.novamens.content.base.PersistentEnumUserType;
import com.novamens.content.model.AttributeType;

public class AttributeTypeUserType extends PersistentEnumUserType<AttributeType> {

	@Override
	public Class<AttributeType> returnedClass() {
		return AttributeType.class;
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
	}
	*/

}
