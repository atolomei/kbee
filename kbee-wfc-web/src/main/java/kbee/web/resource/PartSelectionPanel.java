package kbee.web.resource;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.novamens.content.base.Content;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.AttributeType;
import com.novamens.content.text.TextPart;
import com.novamens.kbee.content.text.KbeeText;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.Field;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;

@SuppressWarnings("serial")
public class PartSelectionPanel extends ModelPanel<Content> {
	private static final long serialVersionUID = 1L;
	
	boolean include; 
	
	public class IndexFragment extends Fragment {
		public IndexFragment(String id) {
			super(id, "index-fragment", PartSelectionPanel.this);
			setOutputMarkupId(true);
		}
		@Override
		public void onBeforeRender() {
			super.onBeforeRender();
			addOrReplace(getView());
		}
		@Override
		public void onInitialize() {
			super.onInitialize();
		}
		protected Component getView() {
			return new ListView<TextPart>("index-view", getParts()) {
				public void populateItem(ListItem<TextPart> item) {
					AjaxLink<Void> link = new AjaxLink<Void>("link") {
						public void onClick(AjaxRequestTarget target) {
							String href = PartSelectionPanel.this.getModelObject().getOId() + "#" + item.getModelObject().getName();
							if (isInclude()) href +="?include=true";
							target.appendJavaScript("returnvalue('"+href+"');");
						}
					};
					link.add(new Label("title", item.getModelObject().getTitle()));
					item.add(link);
				}
			};
		}
		protected List<TextPart> getParts() {
			String text = getText();
			if (text==null) return new ArrayList<TextPart>();
			KbeeText ktext = new KbeeText(text);
			List<TextPart> parts = ktext.getParts();
			return parts;
		}
	}

	public PartSelectionPanel(String id, IModel<Content> model) {
		super(id, model);
	}
	
	public String getText() {
		String text = null;
		Content content = getModelObject();
		for (AttributeTemplate template : content.getContentTemplate().getAttributes()) {
			if (AttributeType.HTML.equals(template.getAttribute().getType())) {
				List<String> texts = content.getAttributeValues(template.getAttribute());
				if (!texts.isEmpty()) {
					text = texts.isEmpty()	? null : texts.get(0);
					break;
				}
			}
		}
		return text;
	}
	
	
	public boolean isInclude() {
		return include;
	}

	public void setInclude(boolean include) {
		this.include = include;
	}

	public void onInitialize() {
		super.onInitialize();
		AjaxLink<Void> contentLink = new AjaxLink<Void>("content") {
			public void onClick(AjaxRequestTarget target) {
				String href = String.valueOf(PartSelectionPanel.this.getModelObject().getOId());
				if (isInclude()) href +="?include=true";
				target.appendJavaScript("returnvalue('"+href+"');");
			}
		};
		contentLink.add(new Label("title", getModelObject().getTitle()));
		add(contentLink);
		add(new IndexFragment("index"));
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		form.add(new BooleanField("include", new PropertyModel<Boolean>(this, "include")) {
			public void onUpdate(AjaxRequestTarget target) {
				updateModel();
			}
		});
		add(form);
		Field<?> field = (Field)form.get("include");
		field.isEnabled();
		
	}
}
