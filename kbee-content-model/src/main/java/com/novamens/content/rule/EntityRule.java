package com.novamens.content.rule;

import com.novamens.content.base.Rule;
import com.novamens.content.model.EntityMember;

public interface EntityRule extends Rule {
	public EntityMember getEntity();
}
