package kbee.web.dataset;

import org.apache.wicket.model.IModel;

import com.novamens.content.web.security.markup.GroupMembersEditor;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.security.acl.Group;

@Deprecated
public class MemberGroupEditor extends ModelPanel<Group> {
	private static final long serialVersionUID = 1L;

	public MemberGroupEditor(String id, IModel<Group> model) {
		super(id, model);
		add (new GroupMembersEditor("editor", model));
	}
}
