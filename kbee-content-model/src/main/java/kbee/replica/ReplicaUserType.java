package kbee.replica;

import com.novamens.content.base.PersistentEnumUserType;

public class ReplicaUserType extends PersistentEnumUserType<ReplicaType> {
	@Override
	public Class<ReplicaType> returnedClass() {
		return  ReplicaType.class;
	}
}