package com.novamens.kbee.timer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import com.novamens.timer.CallBack;

public class CallBackType implements UserType{

	@Override
	public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner) throws SQLException {
		ByteArrayInputStream istream = null;
		try {
			istream = new ByteArrayInputStream(rs.getBytes(names[0]));
			ObjectInputStream oistream = new ObjectInputStream(istream);
			CallBack callBack = (CallBack)oistream.readObject();
			return callBack;
		}
		catch (IOException e) {
			throw new SQLException(e);
		}
		catch (ClassNotFoundException e) {
			throw new SQLException(e);
		}
		finally {
			if (istream!=null) {
				try {
					istream.close();
				}
				catch (IOException e) {
					throw new SQLException(e);
				}
			}
		}	
	}

	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session) throws SQLException {
		if (value == null) {
			st.setNull(index, Types.BINARY);
		} 
		else {
			ByteArrayOutputStream ostream = null;
			try {
				ostream = new ByteArrayOutputStream();	
				ObjectOutputStream oostream = new ObjectOutputStream(ostream);
				oostream.writeObject(value);
				oostream.flush();
				ostream.close();
				st.setBytes(index, ostream.toByteArray());
			}
			catch (IOException e) {
				throw new SQLException(e);
			}
			finally {
				if (ostream!=null) {
					try {
						ostream.close();
					}
					catch (IOException e) {
						throw new SQLException(e);
					}
				}
			}
		}
	}
	
	@Override
	public Class<CallBack> returnedClass() {
		return CallBack.class;
	}
	
	@Override
	public int[] sqlTypes() { 
		return new int[] {Types.BINARY}; 
	}
	
	@Override
	public boolean isMutable() {
		return false; 
	}
	
	@Override
	public Object deepCopy(Object value) {
		return value;
	}
	
	@Override
	public int hashCode(Object x) {
		return x.hashCode();
	}
	
	@Override
	public boolean equals(Object x, Object y) {
		return Objects.equals(x, y);
	}
	
	@Override
	public Serializable disassemble(Object o) {
		return (Serializable) o;
	}
	
	@Override
	public Object assemble(Serializable cached, Object owner) {
		return cached;
	}
	
	@Override
	public Object replace(Object o, Object target, Object owner) {
		return o;
	}
}
