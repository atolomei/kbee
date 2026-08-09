package kbee.web.report;

import java.math.RoundingMode;
import java.text.NumberFormat;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.indexer.query.SearchResult;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

public class IntegerColumn extends ReportColumn {
														
	private static final long serialVersionUID = 1L;
																								
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(IntegerColumn.class.getName());
	
	private NumberFormat nf;
	private int  fc = 0;

	// private String labelCss = "number-md"; 
	private boolean sparse_highligter = false;

	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	
	 
	
	public IntegerColumn(String id, IModel<String> displayModel, String sortProperty) {
		super(id, displayModel, sortProperty);
		this.nf = NumberFormat.getInstance(getSessionUser().getLocale());
		nf.setMinimumFractionDigits(0);
		nf.setMaximumFractionDigits(0);
		nf.setRoundingMode(RoundingMode.HALF_UP);
	}
	
	
	@Override
	protected IModel<String> getLabelModel(SearchResult result) {
		if (result.getObject()==null) 
			return new Model<String>("err");
		try {
			
			Integer number = getNumber(result);
			return  new Model<String>(number!=null?nf.format(number):"");
		}
		catch (NumberFormatException e) {
			logger.error(e);
			return  new Model<String>("");
		}
	}
	
	
	public IModel<String> getCellAsString(SearchResult result) {
		
		if (result.getObject()==null) 
			return new Model<String>("err");
		
		try {
			Integer number = getNumber(result);
			return  new Model<String>(String.valueOf(number));
		}
		catch (NumberFormatException e) {
			return  new Model<String>("");
		}
		
	}

	
	private Integer getNumber(SearchResult result) {
		Row row = (Row)result.getObject();
		try {
			
			if (row.get(getId())==null || row.get(getId()).length()==0)
				return  null;
			
			Integer number = Integer.valueOf(row.get(getId()));
			return number;
		}
		catch (NumberFormatException e) {
			logger.error(e);
			throw(e);
		}
	}
	
	
	
	@Override
	protected String getLabelCss(IModel<SearchResult> model) {
		try {
		if (ishighlightNonZero()) {
			Row row = (Row) model.getObject().getObject();
			String str=row.get(getId());
			return getLabelCss()+ (str!=null&&  getNumber(model.getObject())==0? "" : " info");
		}
		else
			return getLabelCss();
		}
		catch (Exception e ) {
			logger.error(e);
			return getLabelCss();
		}
	}
	
	
	@Override
	public String getCssClass() {
		return "col col-xs-1 col-md-1 col-lg-1 ui-resizable centered";
	}
	
	
	
	public void setHighlightNonZero(boolean b) {
		this.sparse_highligter =b;
	}
	
	public boolean ishighlightNonZero() {
		return this.sparse_highligter;
	}
	
	
	public void setMaximumFractionDigits(int n) {
		this.fc=n;
		nf.setMinimumFractionDigits(this.fc);
	}
	protected int getMaximumFractionDigits() {
		return this.fc;	
	}

}
