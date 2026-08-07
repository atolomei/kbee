package com.novamens.dom;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

import com.novamens.security.PersistentEnum;



public class DomPersistentEnumUserType<T extends PersistentEnum> implements org.hibernate.usertype.UserType {


	
	@Override
	public java.lang.Object deepCopy(java.lang.Object value) throws HibernateException {
		return value;
	}
	
	@Override
	public Serializable disassemble(java.lang.Object value) throws HibernateException {
		return (Serializable)value;
	}
	
	@Override
	public boolean equals(java.lang.Object x, java.lang.Object y) throws HibernateException {
		return x == y;
	}

	@Override
	public int hashCode(java.lang.Object x) throws HibernateException {
		return x == null ? 0 : x.hashCode();
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	
	@Override
	public java.lang.Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, java.lang.Object owner) throws HibernateException, SQLException {
		
		int id = rs.getInt(names[0]);
		
		if(rs.wasNull()) {
			return null;
		}
		
		for(java.lang.Object value : returnedClass().getEnumConstants()) {
			if(id == ((PersistentEnum) value).getId()) {
				return value;
			}
		}
		throw new IllegalStateException("Unknown " + returnedClass().getSimpleName() + " id");
	}
	
	
	@Override
	public int[] sqlTypes() {
		return new int[]{Types.INTEGER};
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class returnedClass() {
		return null;
	}

	@Override
	public void nullSafeSet(PreparedStatement st, java.lang.Object value, int index,
			SharedSessionContractImplementor session) throws HibernateException, SQLException {
		if (value == null) {
			st.setNull(index, Types.INTEGER);
		} 
		else {
			st.setInt(index,((PersistentEnum)value).getId());
		}
	}

	@Override
	public java.lang.Object assemble(Serializable cached, java.lang.Object owner) throws HibernateException {
		return cached;
	}

	@Override
	public java.lang.Object replace(java.lang.Object original, java.lang.Object target, java.lang.Object owner)
			throws HibernateException {
		return original;
	}
}



