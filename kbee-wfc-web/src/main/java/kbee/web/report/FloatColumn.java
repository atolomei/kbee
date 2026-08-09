package kbee.web.report;

import java.math.RoundingMode;
import java.text.NumberFormat;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;


import com.novamens.indexer.query.SearchResult;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

public class FloatColumn extends ReportColumn {
			
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(FloatColumn.class.getName());
	
	private NumberFormat nf;
	private int  fc = 2;

	private boolean sparse_highligter = false;


	
	public FloatColumn(String id, IModel<String> displayModel, String sortProperty) {
		super(id, displayModel, sortProperty);

		this.nf = NumberFormat.getInstance(getSessionUser().getLocale());
		nf.setMinimumFractionDigits(fc);
		nf.setMaximumFractionDigits(getMaximumFractionDigits());
		nf.setRoundingMode(RoundingMode.HALF_UP);
	}
	
	
	public IModel<String> getCellAsString(SearchResult result) {
		
		if (result.getObject()==null) 
			return new Model<String>("err");
		
		try {
			Float number = getNumber(result);
			return  new Model<String>(String.valueOf(number));
		}
		catch (NumberFormatException e) {
			return  new Model<String>("");
		}
		
	}
	
	
	@Override
	protected IModel<String> getLabelModel(SearchResult result) {
	
		if (result.getObject()==null) 
			return new Model<String>("err");
		
		//Row row = (Row)result.getObject();
		try {
			
			//Float number = Float.valueOf(row.get(getId()));
			//String strvalue = nf.format(number);
			
			Float number = getNumber(result);
			return  new Model<String>(nf.format(number));
		}
		catch (NumberFormatException e) {
			return  new Model<String>("");
		}
	}
	
	private Float getNumber(SearchResult result) {
		Row row = (Row)result.getObject();
		try {
			Float number = Float.valueOf(row.get(getId()));
			return number;
		}
		catch (NumberFormatException e) {
			logger.error(e);
			throw(e);
		}
	}
	
	
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	
	@Override
	protected String getLabelCss(IModel<SearchResult> model) {
		try {
		if (ishighlightNonZero()) {
			Row row = (Row) model.getObject().getObject();
			String str=row.get(getId());
			return getLabelCss() + (str!=null&&  getNumber(model.getObject())==0? "" : " info");
		}
		else
			return getLabelCss();
		}
		catch (Exception e ) {
			logger.error(e);
			return "number-mdx";
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
