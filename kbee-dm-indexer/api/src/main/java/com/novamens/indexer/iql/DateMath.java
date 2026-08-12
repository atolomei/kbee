package com.novamens.indexer.iql;

import com.novamens.indexer.service.IndexerException;

public class DateMath {
	public static String getArgument(String value) throws IndexerException {
		String argument = value; 
		if (value.indexOf("%")>=0) {
			if (value.endsWith("-%")) {
				if (value.endsWith("%-%")) {
					String from = value.substring(0, 4) + "-01-01";
					String to = value.substring(0, 4) + "-12-31";
					argument =  "[" + from + "T00:00:00Z TO " + to + "T23:59:59Z]";
				}
				else {
					String from = value.substring(0, value.length()-1) + "01";
					String to = value.substring(0, value.length()-1) + "31";
					argument =  "[" + from + "T00:00:00Z TO " + to + "T23:59:59Z]";
				}
			}
			else {
				if (value.endsWith("%")) {
					if (value.startsWith("%")) value= value.substring(1);
					if (value.length()==4) {
						String from = value.substring(0, 3) + "0-01-01";
						String to = value.substring(0, 3) + "9-12-31";
						argument =  "[" + from + "T00:00:00Z TO " + to + "T23:59:59Z]";
					}
					if (value.length()==5) {
						String from = value.substring(0, 4) + "-01-01";
						String to = value.substring(0, 4) + "-12-31";
						argument =  "[" + from + "T00:00:00Z TO " + to + "T23:59:59Z]";
					}
				}
			}
		}
		else {
			argument =  "[" + value + "T00:00:00Z TO " + value + "T23:59:59Z]";
		}
		return argument;
	}	
}
