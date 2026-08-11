package test.com.novamens.kbee.idoc.webapi;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import com.novamens.kbee.idoc.webapi.client.KbeeApiService;

import kbee.api.model.ApiFile;
import kbee.api.model.IBinaryResource;
import kbee.api.model.ITransaction;
import kbee.api.service.ApiException;

public class Watcher implements Runnable {


public void run(){
	try {
		WatchService watcher;
		watcher = FileSystems.getDefault().newWatchService();
	
		Path dir = Paths.get("d://temp");
		
		try {
			WatchKey key = dir.register(watcher,
				StandardWatchEventKinds.ENTRY_CREATE,
				StandardWatchEventKinds.ENTRY_DELETE,
				StandardWatchEventKinds.ENTRY_MODIFY);
		} 
		catch (IOException x) {
			System.err.println(x);
		}
	
		for (;;) {

			// wait for key to be signaled
			WatchKey key;
			try {
				key = watcher.take();
			} 
			catch (InterruptedException x) {
				return;
			}

			for (WatchEvent<?> event: key.pollEvents()) {
				WatchEvent.Kind<?> kind = event.kind();

				// This key is registered only
				// for ENTRY_CREATE events,
				// but an OVERFLOW event can
				// occur regardless if events
				// are lost or discarded.
				if (kind == StandardWatchEventKinds.OVERFLOW) {
					continue;
				}

				// The filename is the
				// context of the event.
				WatchEvent<Path> ev = (WatchEvent<Path>)event;
				Path filename = ev.context();

				// Verify that the new
				//  file is a text file.
				try {
					// Resolve the filename against the directory.
					// If the filename is "test" and the directory is "foo",
					// the resolved name is "test/foo".
					Path child = dir.resolve(filename);
					if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
						send(child);
					}
				} 
				catch (Exception x) {
					System.err.println(x);
					continue;
				}

			}

			// Reset the key -- this step is critical if you want to
			// receive further watch events.  If the key is no longer valid,
			// the directory is inaccessible so exit the loop.
			boolean valid = key.reset();
			if (!valid) {
				break;
			}
		}
	}
	catch (Exception e) {
		e.printStackTrace();
		throw new RuntimeException();
	}
}

	public void send(Path path) {
	
		try {
			KbeeApiService api = new KbeeApiService("http://localhost:8080/api");
			
			api.setUser("root@windsor");
			api.setPassword("1Aqqqqqq");
				
			ApiFile file = new ApiFile();
			
			file.setClassName("Resource");
			file.setTitle("TEST");
			file.setApplication("onesitedm");
			file.setDomain("windsor");
			file.setExternalId("002");
			
			file.setAttribute("File Type", "Resource");
			
			File wfile = path.toFile();
			
			file.addResource(new IBinaryResource(wfile));
			
			ITransaction response = api.update(file);
	
			// System.out.println(response);
		}
		catch (ApiException e) {
			// System.out.println(e.getHttpStatus());
			// System.out.println(e.getErrorCode());
			// System.out.println(e.getMessage());
		}
		catch (Exception e) {
			e.printStackTrace();
			// System.out.println(e.getMessage());
		}
	}
}
