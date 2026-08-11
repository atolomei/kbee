package com.novamens.kbee.content.multidimensional;

import com.novamens.kbee.security.acl.KbeePermission;

public class MemberReaderExtractor extends MemberPermissionExtractor {
	
	public MemberReaderExtractor() {
		super(KbeePermission.READ);
	}
}