package test.com.novamens.kbee.idoc.webapi;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import javax.swing.JButton;
import javax.swing.JFrame;

import java.nio.file.StandardWatchEventKinds;


public class Application {
	
	
	

	
	
    public static void main(String args[]) {
    	
    	
		Thread thread = new Thread(new Watcher());
		thread.setDaemon(true);
		thread.setName("Scheduler");
		thread.start();
    	
    	
        JFrame frame = new JFrame("Mi primera GUI");       
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);       
        frame.setSize(300, 300);      
        JButton button1 = new JButton("Presionar");      
        frame.getContentPane().add(button1);      
        frame.setVisible(true);    
    }
}
