package com.novamens.content.form;

import java.util.List;


/**
 * 1. Tarea Edicion  -> eform habilitado (form editable)
 * 2. Tarea ReadOnly -> "Viewer del Form" cuando la tarea es ReadOnly -> viewer del form o el default
 * 3. Auditoria      -> Captura HTML del Form (Process History) 
 * 
 * ---------------------------------------------------------------------------------------------------
 * 
 * 4. Biblioteca     -> Se muestra siempre el "Viewer del Form"  
 * 
 * ---------------------------------------------------------------------------------------------------
 * 
 * Especificacion del Form -> Condiciones para que se vea en el contexto que sea.
 * Si no tiene Archivos    -> Se el TEXTO
 * Si tiene Archivos       -> Se el PDF
 * 
 * ---------------------------------------------------------------------------------------------------
 *
 * BARRA -> Download Parte del Form y no del Documento -> Toolbar 
 * 
 */
public interface EForm  {
	
	public List<EFormComponent> getComponents();
	public List<EFormField<?>> getFields();
	public EFormField<?> getField(String name);
	public String getName();
	public String getDisplayName();
	public EDisposition getDisposition();
	
	
	public EFormAccessLevel getFormAccessLevel(); // Access Level -> Workflow | Portal | Internal Use

	// public EForm Usage getForm Usage();
	//
	public boolean isUseInline();     // Whether the form is displayed in Hit Panels
	public boolean isFileContainer(); // Whether the form is just a container of a pdf file (normally for signatures)
	public List<String> getBehaviors();
	
	public boolean isEnabled();
	public boolean isVisible(EFormData data);
	
	public String getCssClass();
	public String getViewer();
	
	public boolean hasToolbar();
}