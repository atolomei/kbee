package com.novamens.kbee.content.multidimensional;

import com.novamens.kbee.security.acl.KbeePermission;

public class MemberWriterExtractor extends MemberPermissionExtractor {
	
	public MemberWriterExtractor() {
		super(KbeePermission.WRITE);
	}
}