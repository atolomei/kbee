package com.novamens.kbee.timer;



import java.io.Serializable;

import com.novamens.timer.CallBack;

public class KbeeDummyCallback implements CallBack, Serializable {
	private static final long serialVersionUID = 1L;

	public void execute() {
		// // System.out.println("CALLBACK");
	}
}
