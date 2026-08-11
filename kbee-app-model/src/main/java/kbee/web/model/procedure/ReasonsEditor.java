package kbee.web.model.procedure;

import java.util.ArrayList;

import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.kbee.content.workflow.KbeeReason;
import com.novamens.workflow.Reason;

import kbee.web.form.RelationEditor;

/**
 * 
 * <p>'Reason' to ask for a Peer Review. They are used by productivity reports.</p>
 *
 * @param <T>
 */
@SuppressWarnings("serial")
public class ReasonsEditor<T> extends RelationEditor<T, Reason> {
	private static final long serialVersionUID = 1L;
	
	public ReasonsEditor() {
		super("reasons");
	}
	
	@Override
	protected List<Property<?>> getProperties() {
		List<Property<?>> properties = new ArrayList<Property<?>>();
		
		properties.add(new Property<String>() {
			@Override
			public String getName() {
				return "code";
			}
		});
		
		properties.add(new Property<String>() {
			@Override
			public String getName() {
				return "label";
			}
			@Override
			public boolean getTitle() {
				return true;
			}
		});
		
		return properties;
	}

	protected Property<?> getKey() {
		return null;
	}
	
	@Override
	protected Reason getNewValue() {
		return new KbeeReason();
	}
	
	public boolean helpInfo() {
		return true;
	}
	
	@Override
	public IModel<String> getHelp() {
		
		//return new StringResourceModel("peer-selection", this, null);
		return new Model<String>("When selecting a Peer Review, it is possible to add a 'reason'. These tags are used for Reports.");
	}
}
