package com.novamens.content.form;

import java.util.List;

public interface ETableModel extends EFieldModel<List<?>> {
	static String GetTypeLabel() {
		return "Table";
	}
}