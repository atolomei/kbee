package kbee.util;

import java.io.Serializable;

public class Tuple implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public String label;
	public String value;
	
	public String  [] arr;
	public Integer [] colw;
	
	public String helpKey;
	

	public String getHelpKey() {
		return helpKey;
	}
	
	
	public Tuple(String label, String[] arr) {
		this(label, arr, null);
	}
	
	
	public Tuple(String label, String[] arr,  Integer [] colw) {
		this.label = label;
		this.arr=arr;
		this.colw=colw;
	}

	
	public Tuple(String label, String value) {
		this.label = label;
		this.value = value;
	}
					
	public String[] getStrArray() {
		return arr;
	}
	
			
	public Integer[] getColWidth() {
		return this.colw;
	}
	
	public String getLabel() {
		return label;
	}
	
	public String getValue() {
		return value;
	}
	
	public String getValue0()  {try  {return arr[0];}	catch  (Exception e) {return e.getClass().getName()+" | " + e.getMessage();}}
	public String getValue1()  {try  {return arr[1];}	catch  (Exception e) {return e.getClass().getName()+" | " + e.getMessage();}}
	public String getValue2()  {try  {return arr[2];}	catch  (Exception e) {return e.getClass().getName()+" | " + e.getMessage();}}
	public String getValue3()  {try  {return arr[3];}	catch  (Exception e) {return e.getClass().getName()+" | " + e.getMessage();}}
	public String getValue4()  {try  {return arr[4];}	catch  (Exception e) {return e.getClass().getName()+" | " + e.getMessage();}}
	public String getValue5()  {try  {return arr[5];}	catch  (Exception e) {return e.getClass().getName()+" | " + e.getMessage();}}
	public String getValue6()  {try  {return arr[6];}	catch  (Exception e) {return e.getClass().getName()+" | " + e.getMessage();}}
	public String getValue7()  {try  {return arr[7];}	catch  (Exception e) {return e.getClass().getName()+" | " + e.getMessage();}}
	public String getValue8()  {try  {return arr[8];}	catch  (Exception e) {return e.getClass().getName()+" | " + e.getMessage();}}
	public String getValue9()  {try  {return arr[9];}	catch  (Exception e) {return e.getClass().getName()+" | " + e.getMessage();}}
	public String getValue10() {try  {return arr[10];}	catch  (Exception e) {return e.getClass().getName()+" | " + e.getMessage();}}
	public String getValue11() {try  {return arr[11];}	catch  (Exception e) {return e.getClass().getName()+" | " + e.getMessage();}}
	public String getValue12() {try  {return arr[12];}	catch  (Exception e) {return e.getClass().getName()+" | " + e.getMessage();}}
	
	
	
	
}


