package com.novamens.kbee.content.multidimensional;

import com.novamens.kbee.security.acl.KbeePermission;

public class MemberChildWriterExtractor extends MemberPermissionExtractor {
	
	public MemberChildWriterExtractor() {
		super(KbeePermission.CHILDS);
	}
}
