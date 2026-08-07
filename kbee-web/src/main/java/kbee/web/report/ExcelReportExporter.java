package kbee.web.report;

import com.novamens.indexer.query.Query;
import com.novamens.kbee.content.reportsubscription.ReportExporter;

import kbee.web.console.tools.GridExportQueryExcel;

import org.apache.wicket.Application;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.protocol.http.BufferedWebResponse;
import org.apache.wicket.protocol.http.mock.MockHttpServletRequest;
import org.apache.wicket.protocol.http.mock.MockHttpSession;
import org.apache.wicket.protocol.http.mock.MockServletContext;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ExcelReportExporter implements ReportExporter {

    private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ExcelReportExporter.class.getName());

    @Override
    public File export(String reportConsoleClass, Map<String, Object> reportParameters, String fileName, String reportTitle) {
        
    	ReportConsole reportConsole;

        Class<?> clazz = null;
        boolean contextCreated = false;
        try {
            if(ThreadContext.getRequestCycle() == null) {
                Application application = Application.get("wicket");

                MockServletContext context = new MockServletContext(application, "");
                Request request = new ServletWebRequest(new MockHttpServletRequest(application, new MockHttpSession(context), new MockServletContext(application, Url.parse("/").toString())), Url.parse("/").toString());
                BufferedWebResponse response = new BufferedWebResponse(null);
                RequestCycle cycle = application.createRequestCycle(request, response);

                contextCreated=true;
                ThreadContext.setRequestCycle(cycle);
                ThreadContext.setApplication(application);
            }

            clazz = Class.forName(reportConsoleClass);
            reportConsole = (ReportConsole) clazz.newInstance();
            Query query = reportConsole.newQuery();

            if (reportParameters != null) {
                query.getParameters().putAll(reportParameters);
            }

            GridExportQueryExcel gridExport = new GridExportQueryExcel();
            return gridExport.export(query, reportConsole.getColumns(), fileName+".xlsx", reportTitle);

        } catch (ClassNotFoundException e) {
            logger.error(e, "Exception while exporting report.");
        } catch (IllegalAccessException e) {
            logger.error(e, "Exception while exporting report.");
        } catch (InstantiationException e) {
            logger.error(e, "Exception while exporting report.");
        } catch (IOException e) {
            logger.error(e, "Exception while exporting report.");
        }finally {
            if(contextCreated)
                ThreadContext.detach();
        }
        return null;
    }
}
