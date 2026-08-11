package test.com.novamens.kbee.idoc.webapi;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.jupiter.api.Test;

import com.novamens.kbee.idoc.webapi.client.KbeeApiService;

import kbee.api.model.*;
import kbee.api.service.ApiException;

public class UploadTest {
	
	/**
	 * 
	 */
	@Test
	public void test0() {
		try {
			
			// Servidor, usuario y contraseña
			//
 			//KbeeApiService api = new KbeeApiService("https://test.kbee.io/api");
 			//KbeeApiService api = new KbeeApiService("http://10.201.4.28:8080/api");
 			KbeeApiService api = new KbeeApiService("http://localhost:8080/api");
			api.setUser("root@indraiml");
			api.setPassword("1Aqqqqqq");

			// Un documento KBEE se componen de un título, atributos -tales como Tipo de Documento, Fecha, Estado, etc.-,
			// y una cantidad de objectos binarios tales como 
			// archivos pdf, word, audio, video, etc.
			//
			ApiFile file = new ApiFile();
			file.setDomain("indraiml");

			// ClassName es la plantilla de contenido. La plantilla determina los atributos que puede tener
			//
			file.setClassName("file");
			
			file.setTitle("TEST");
			
			// Los atributos  "type" "referencedate" "status", están definidos en el Modelo de Información
			//
			file.setAttribute("type", "Documento");
			file.setAttribute("referencedate", "2022-10-01");
			file.setAttribute("status", "Final");
			
			//file.addResource(new IMultipartResource(new File("d:\\temp\\abeja-kbee.png")));
			file.addResource(new IBinaryResource(new File("d:\\temp\\eco.jpg")));
			//file.addResource(new IMultipartResource(new File("d:\\temp\\eco.jpg")));
			//file.addResource(new IMultipartResource(new File("d:\\temp\\peli.mp4")));
			
			// Una nombre de aplicación y un id pueden funcionar como un indentificador alternativo generado por el cliente;
			//
			file.setApplication("iml");
			file.setExternalId("11");

			ITransaction response = api.update(file);

			// como respuesta de la transaccion se incluyen la uri y el id del documento creado
			//
			String uri = response.getTarget().getHRef();
			String id = response.getTarget().getId();
			// System.out.println(uri);

			
			// Con la uri o el id se puede recuperar el documento
			// La uri con el id externo se forma con la siguiente expresión: /file/{aplicacion}/{dominio}/{id}
			// Para el documento de este test la url es /file/iml/indraiml/1
			// Si no se especifica un id externo uri se forma con los ids internos del file
			//
			file = api.getFile(uri);
		
			// Se puede recuperar por su id interno
			file = api.getFileById(id);
			
			file = api.getFileByExternalId("iml","10");
			
			// System.out.println(file.getId());
		}
		catch (ApiException e) {
			 System.out.println(e.getHttpStatus());
			 System.out.println(e.getErrorCode());
			 System.out.println(e.getMessage());
		}
		catch (Exception e) {
			e.printStackTrace();
			 System.out.println(e.getMessage());
		}
		
		catch (Throwable e) {
			 System.out.println(e.getMessage());
		}


	}
	
	//@Test
	public void upload() {
    	String id = "";
    	
		try {
			
			// Servidor, usuario y contraseÃ±a
			KbeeApiService api = new KbeeApiService("https://test.kbee.io/api");
			api.setUser("root@indraiml");
			api.setPassword("1Aqqqqqq");

			// Un documento KBEE se componen de un tÃ­tulo, atributos -tales como Tipo de Documento, Fecha, Estado, etc.-,
			// y una cantidad de objectos binarios tales como 
			// archivos pdf, word, audio, video, etc.
			ApiFile file = new ApiFile();			

			// ClassName es la plantilla de contenid. La plantilla determina los atributos que puede tener 
			file.setClassName("file");
			
//			file.setTitle("Contrato de alquiler 20/10/2020");
			file.setTitle("title");
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
   	    	String referencedate = sdf.format(new Date());
			
			// los atributos  "type" "referencedate" "status", estÃ¡n definidos en el Modelo de InformaciÃ³n 
//			file.setAttribute("type", "Documento");
//			file.setAttribute("referencedate", "2022-04-12");
//			file.setAttribute("status", "Final");
			file.setAttribute("type", "Documento");
			file.setAttribute("referencedate", referencedate);
			file.setAttribute("status", "Final");
			
//			file.addResource(new IMultipartResource(new File("C:\\Temporal\\pdf_iml\\pdf\\ListadoDiario.pdf")));
			file.addResource(new IMultipartResource(new File("C:\\temp\\fallo.png")));
			
			// Una nombre de aplicaciÃ³n y un id pueden funcionar como un indentificador alternativo generado por el cliente;
			file.setApplication("iml");
			file.setExternalId("1");

			ITransaction response = api.update(file);

			// como respuesta de la transaccion se incluyen la uri y el id del documento creado
			String uri = response.getTarget().getHRef();
			id = response.getTarget().getId();
			// System.out.println(uri);

			// con la uri o el id se puede recuperar el documento
			file = api.getFile(uri);
			
			file = api.getFileById(id);
			
			// System.out.println(file.getId());
		}
		catch (ApiException e) {
			// System.out.println(e.getHttpStatus());
			// System.out.println(e.getErrorCode());
			// System.out.println(e.getMessage());
		}
		catch (Exception e) {
			// System.out.println(e.getMessage());
		}
		
	}
}
