package test.com.novamens.kbee.asp;

import java.io.File;

import javax.swing.JEditorPane;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;


import com.novamens.service.ServiceLocator;
import com.novamens.spring.service.SpringServiceLocator;
import com.novamens.thumbnail.ThumbnailService;


public class ThumbnailServiceTest {
	   private static int      DISPLAY_NUMBER  = 99;
	    private static String   XVFB            = "/usr/bin/Xvfb";
	    private static String   XVFB_COMMAND    = XVFB + " :" + DISPLAY_NUMBER;
	    private static String   URL             = "http://www.google.com/";
	    private static String   RESULT_FILENAME = "c:\\temp\\screenshot.png";

	
	@SuppressWarnings("unused")
	private boolean isImage(File file) {
		return file.getName().toLowerCase().matches("^.*\\.(png|jpg|webp|gif|bmp)$"); 
	}
						
	@SuppressWarnings("unused")
	private boolean isPdf(File file) {
		return file.getName().toLowerCase().matches("^.*\\.pdf"); 
	}
	
	@BeforeAll
	public void setUp() throws Exception {
		ServiceLocator.setInstance(new SpringServiceLocator("kbee"));
	}
	
	@AfterAll
	public void tearDown() throws Exception {
	}
	
	/**
	 *
	 * adds 1.000 files to the server
	 * reads 1.000 files and calculate checksum
	 * 
	 * removes 100 files
	 * get files removed
	 * 
	 */
	//@Test
	public void test() {

			long to,t1;
			

			
			try {
			
				FirefoxOptions options = new FirefoxOptions();
				options.setHeadless(true);
				//options.setBinary("d:\\temp\\geckodriver.exe");
				WebDriver driver = new FirefoxDriver(options);
				driver.get("https://www.clarin.com/");
				File scrFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
				// Now you can do whatever you need to do with it, for example copy somewhere
				FileUtils.copyFile(scrFile, new File("c:\\temp\\screenshot.png"));
 			     
			     System.out.print("OK");
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			     
	}
	
	   private static final int WIDTH  = 800;
	   private static final int HEIGHT = 600;
	   
//	@Test
//	public void test2() {
//		  final Display  display = new Display();
//	      final Shell shell = new Shell();
//	      shell.setLayout(new FillLayout());
//	      final Browser browser = new Browser(shell, SWT.EMBEDDED );
//          browser.addProgressListener(new ProgressListener() {
//	         @Override
//	         public void changed( ProgressEvent event ) {}
//
//	         @Override
//	         public void completed( ProgressEvent event ) {
//	            shell.forceActive();
//	            display.asyncExec(new Runnable() {
//
//	               @Override
//	               public void run() {
//	                  grab(display, shell, browser);
//	               }
//	            });
//
//	         }
//	      });
//	      browser.setUrl("http://www.google.com");
//
//	      shell.setSize(WIDTH, HEIGHT);
//	      shell.open();
//
//	      while ( !shell.isDisposed() ) {
//	         if ( !display.readAndDispatch() ) display.sleep();
//	      }
//	      display.dispose();
//	}
//	
//	   private static void grab( final Display display, final Shell shell, final Browser browser ) {
//		      final Image image = new Image(display, browser.getBounds());
//		      GC gc = new GC(browser);
//		      gc.copyArea(image, 0, 0);
//		      gc.dispose();
//
//		      ImageLoader loader = new ImageLoader();
//		      loader.data = new ImageData[] { image.getImageData() };
//		      loader.save("c:\\temp\\foo.png", SWT.IMAGE_PNG);
//		      image.dispose();
//
//		      shell.dispose();
//		   }

	
    public static void takeSnapShot(WebDriver webdriver,String fileWithPath) throws Exception{

        //Convert web driver object to TakeScreenshot

        TakesScreenshot scrShot =((TakesScreenshot)webdriver);

        //Call getScreenshotAs method to create image file

                File SrcFile=scrShot.getScreenshotAs(OutputType.FILE);

            //Move image file to new destination

                File DestFile=new File(fileWithPath);

                //Copy file at destination

                FileUtils.copyFile(SrcFile, DestFile);

    }
	

	
}
