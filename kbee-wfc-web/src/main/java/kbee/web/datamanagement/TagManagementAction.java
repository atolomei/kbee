package kbee.web.datamanagement;


import com.novamens.wicket.markup.html.panel.KBPanel;

public abstract class TagManagementAction extends KBPanel {
    
	private static final long serialVersionUID = 1L;

	public TagManagementAction(String id) {
        super(id);
    }

    public abstract String getActionName();

    public abstract Object getModifierInstance();
}
