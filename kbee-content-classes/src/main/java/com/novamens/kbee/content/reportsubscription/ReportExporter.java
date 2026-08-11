package com.novamens.kbee.content.reportsubscription;

import java.io.File;
import java.util.Map;

public interface ReportExporter {

    File export(String reportConsoleClass, Map<String, Object> reportParameters, String fileName, String reportTitle);
}
