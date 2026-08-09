package kbee.web.iql;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.novamens.dom.Domain;
import com.novamens.indexer.iql.IqlService;
import com.novamens.indexer.iql.Predicate;
import com.novamens.security.User;
import com.novamens.service.ObjectService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;


public class KbeeIqlHelpService implements ObjectService {
			
	private Domain  domain;

	private static kbee.util.logging.Logger logger  	= kbee.util.logging.Logger.getLogger(KbeeIqlHelpService.class.getName());
	
	public KbeeIqlHelpService() {
	}
	
	public KbeeIqlHelpService(Domain domain) {
		 this.domain = domain;
	}
	
	public Domain getDomain() {
		return domain;
	}
	
	public String getPredicatesHelp() {
		
		StringBuilder text = new StringBuilder();
		
		text.append("<div class=\"panel col-lg-12\"><p class=\"text col-lg-12\">Criteria can be expressed in IQL (<span class=\"predicate\">iDOC Query Language</span>),  a high level language that supports business domain queries.<br />"
	   + "IQL is a declarative query language similar to SQL, specifically designed for Document Management. <br />IQL Sentences are made of predicates plus logical operators (<b>AND</b>, <b>OR</b> and <b>NOT</b>) and <b>Parenthesis</b> for precedence.<br/><br/>"); 

		text.append(  "<p class=\"col-lg-12 text\"><h3>Examples</h3><ul class=\"col-lg-12 panel\" "
						   + " style=\"margin-bottom:20px; margin-top:0px;\">"
						   + "<li class=\"col-lg-12\"><span class=\"predicate\">source<span class=\"ago\"> ( onesite ) </span></li>"
						   + "<li class=\"col-lg-12\"><span class=\"predicate\">Library<span class=\"ago\"> ( Compliance ) </span></li>"
						   + "<li class=\"col-lg-12\"><span class=\"predicate\">ContentClass<span class=\"ago\"> ( Compliance File ) AND isHead(true)</span></li>"
						   + "<li class=\"col-lg-12\"><span class=\"predicate\">source<span class=\"ago\"> ( leasestar ) </span><span class=\"logical-operator\"> or </span><span class=\"predicate\">source</span><span class=\"ago\"> ( onesite ) </span><li class=\"col-lg-12\"><span class=\"predicate\">isTemplate<span class=\"ago\"> ( false ) </span><span class=\"logical-operator\"> and </span><span class=\"predicate\">isExternal</span><span class=\"ago\"> ( false ) </span></ul>");	

		
		text.append("<p> The available predicates are: </p>");
		text.append("</div>");

		IqlService iqlservice = getDomain().getService(IqlService.class);
		List<Predicate> predicates =iqlservice.getPredicateManager().getPredicates();
		
		predicates.sort(new Comparator<Predicate>() {

			@Override					
			public int compare(Predicate a, Predicate b) {
				try {
					return a.getName().compareToIgnoreCase(b.getName());
				} catch (Exception e) {
					logger.error(e);
				}
				return 0;
			}
			
			
		});
		
		predicates.sort(new Comparator<Predicate>() {
			@Override
			public int compare(Predicate a, Predicate b) {
				return a.getName().compareToIgnoreCase(b.getName());
			}
		});
		
		List<Predicate> p_canonical = new ArrayList<Predicate>();
		List<Predicate> p_model = new ArrayList<Predicate>();
		List<Predicate> p_timed = new ArrayList<Predicate>();
		List<Predicate> p_library = new ArrayList<Predicate>();
		
		for (Predicate predicate: predicates) {
			if (predicate.isCanonical())
				p_canonical.add(predicate);
			else if (predicate.isTimed())
				p_timed.add(predicate);
			else if (predicate.isLibrary())
				p_library.add(predicate);
			else
				p_model.add(predicate);
		}
		
		text.append("<div class=\"panel col-lg-12\">");
		
		text.append("<h3 class=\"col-lg-12\"> General </h3>");
		text.append("<ul class=\"col-lg-12 panel\">");
		for (Predicate predicate: p_canonical) {
			text.append("<li class=\"col-lg-12\"><span class=\"predicate\">");
			text.append(predicate.getName());
			text.append("</span>");
			
			text.append("<span class=\"ago\"> ( ");
			text.append(predicate.getHelpValueTypeDescription());
			text.append(" )</span>");
			
			
			text.append("<span style=\"float:right\" class=\"metadata\">");
			text.append(predicate.getClass().getSimpleName());
			text.append("</span>");
			text.append("</li>");
		}
		text.append("</ul>");
		
		text.append("<h3 class=\"col-lg-12\"> Information Model - Attributes and Classifiers </h3>");
		text.append("<ul class=\"col-lg-12 panel\">");
		for (Predicate predicate: p_model) {
			text.append("<li class=\"col-lg-12\"><span class=\"predicate\">");
			text.append(predicate.getName());
			text.append("</span>");
			
			
			text.append("<span class=\"ago\"> ( ");
			text.append(predicate.getHelpValueTypeDescription());
			text.append(" )</span>");
			
			
			text.append("<span style=\"float:right\" class=\"metadata\">");
			text.append(predicate.getClass().getSimpleName());
			text.append("</span>");

			text.append("</li>");
		}
		text.append("</ul>");
		
		
		
		text.append("<h3 class=\"col-lg-12\"> Timed </h3>");
		text.append("<ul class=\"col-lg-12 panel\">");
		for (Predicate predicate: p_timed) {
			text.append("<li class=\"col-lg-12\"><span class=\"predicate\">");
			text.append(predicate.getName());
			text.append("</span>");
			
			text.append("<span class=\"ago\"> ( ");
			text.append(predicate.getHelpValueTypeDescription());
			text.append(" )</span>");
			
			
			text.append("<span style=\"float:right\" class=\"metadata\">");
			text.append(predicate.getClass().getSimpleName());
			text.append("</span>");

			text.append("</li>");
		}
		text.append("</ul>");

		

		
		text.append("<h3 class=\"col-lg-12\"> Libray </h3>");
		text.append("<ul class=\"col-lg-12 panel\">");
		for (Predicate predicate: p_library) {
			text.append("<li class=\"col-lg-12\"><span class=\"predicate\">");
			text.append(predicate.getName());
			text.append("</span>");
			
			text.append("<span class=\"ago\"> ( ");
			text.append(predicate.getHelpValueTypeDescription());
			text.append(" )</span>");
			
			if (getSessionUser().getUserName().startsWith("root@")) {
				text.append("<span style=\"float:right\" class=\"metadata\">");
				text.append(predicate.getClass().getSimpleName());
				text.append("</span>");
			}

			text.append("</li>");
		}
		text.append("</ul>");

		text.append("</div>");
		
		return text.toString();
	}
	
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	public String getDueDateHelp() {
		
		StringBuilder text = new StringBuilder();
		
		text.append("<div class=\"panel col-lg-12\">"
				+ 	"<p class=\"text col-lg-12\">"
				+ "Case Expression: <b>CASE IQL THEN VALUE; ... DEFAULTCASE VALUE;</b><br /><br />"
				+ "Example:<br />"
				+ "<i>CASE type(Bond ONLY - Move in) THEN 1; <br />"
				+ "CASE type(Market unit) THEN 1; <br />"
				+ "CASE type(HUD MI) THEN 1; <br />"
				+ "CASE type(HOME ONLY - Move in) THEN 1; <br />"
				+ "CASE type(RD Move In) THEN 1; <br />"
				+ "CASE type(Other Local Program - MI) THEN 1; <br />"
				+ "CASE type(TC Move In) THEN 1; <br />"
				+ "CASE type(Bond ONLY - Recert) THEN 2; <br />"
				+ "CASE type(TC Recert) THEN 2; <br />"
				+ "CASE type(HUD Annual) THEN 2;<br />"
				+ "DEFAULTCASE 2;</i>"
				+	 "</p>"
				+ "</div>");
		
		return text.toString();
		
	}

	public String getRelatedQueriesHelp() {
		StringBuilder text = new StringBuilder();
		
		text.append("<div class=\"panel col-lg-12\">"
				+		 "<p class=\"text col-lg-12\"> IQL Sentence that retrieves files from the Libraries. It can use the File's Attributes and meta-data as macros in the form <b>$...$</b>.<br /> Examples:<br/><br/>"
				+ 			"(property($classifier:property$) or state($classifier:property.state$)) and workflowstatus(File Review) and iskbase(true) <br/><br/> "
				+ 			"Type(Checklist Template) and ChecklistTemplateSet($classifier:Document Type$) <br /><br/>"
				+ 			"((kbscope(Property^4) and property($classifier:property$)) or (kbscope(Property Management Co.) and (pmc($classifier:Property Management Co.$))) or (kbscope(State) and usstate($classifier:property.US State$)) or kbscope(Global)) and iskbase(true)<br/><br/>"
				+ 		"</p>"
				+ 	"</div>");
		
		return text.toString();

	}
}
