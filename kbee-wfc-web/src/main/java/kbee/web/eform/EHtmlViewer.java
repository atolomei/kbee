package kbee.web.eform;


import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.w3c.dom.Element;

import com.novamens.content.base.Content;
import com.novamens.content.form.EFormContentData;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;
import com.novamens.content.model.ContentId;
import com.novamens.content.text.AncordResolver;
import com.novamens.content.text.ImageResolver;
import com.novamens.content.text.Text;
import com.novamens.kbee.content.text.KbeeText;

@SuppressWarnings("serial")
public class EHtmlViewer extends EValueViewer {
	private static final long serialVersionUID = 1L;
	
	public EHtmlViewer(String id, EFormField<?> field, IModel<EFormData> data) {
		super(id, field, data);
	}
	
	protected IModel<String> getValueModel() {
		IModel<String> model = new Model<String>() {
			public String getObject() {
				Text text = KbeeText.textOf((String)getDataModel().getObject().getData(getField()));
				if (isEmpty(text)) {
					return null;
				}	
				String strvalue = text.getText(new AncordResolver() {
					@Override
					public Element resolve(Element ancord) {
						return ancord;
					}
				}, new ImageResolver() {
					@Override
					public Element resolve(Element image) {
						String src = image.getAttribute("src");
						Content content = ((EFormContentData)getData()).getContent();
						src = "/resource/content/"+(new ContentId(content).toString()) +"/" + src;
						image.setAttribute("src", src);
						return image;
					}
				});
				return strvalue;
			}
			protected boolean isEmpty(Text text) {
				if (text==null || text.asString()==null)
					return true;
				String value = text.asString();
				value = value.replace("<p class=\"last\">","");
				value = value.replace("<p>","");
				value = value.replace("</p>","");
				value = value.replace("<br>","");
				value = value.replace("<br/>","");
				if ("".equals(value.trim()))
					return true;
				return false;
			}
		};
		return model;
	}

	@Override
	protected String getValueCss() {
		return "eform-html";
	}
} 