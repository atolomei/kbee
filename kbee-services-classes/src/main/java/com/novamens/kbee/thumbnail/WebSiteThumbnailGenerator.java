package com.novamens.kbee.thumbnail;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import com.novamens.thumbnail.ThumbnailGenerator;
import com.novamens.thumbnail.ThumbnailSize;

import kbee.util.logging.Logger;
 
public class WebSiteThumbnailGenerator implements ThumbnailGenerator {
	
	 private static Logger logger = Logger.getLogger(WebSiteThumbnailGenerator.class.getName());

	public void generate(File file, OutputStream stream, ThumbnailSize size) throws IOException {
		
	}

	@SuppressWarnings("deprecation")
	public void generate(String url, OutputStream stream, ThumbnailSize size) throws IOException {
		
		WebDriver driver = null;
		try {
			FirefoxOptions options = new FirefoxOptions();
			options.setHeadless(true);
			driver = new FirefoxDriver(options);
			//WebDriver driver = new ChromeDriver();
			driver.get(url);
			File scrFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
			// Now you can do whatever you need to do with it, for example copy somewhere
			FileUtils.copyFile(scrFile, stream);
			//driver.quit();
		}
		catch (Exception e) {
			logger.error(e);
		}
		finally {
			if (driver!=null) { 
				driver.quit();
			}	
		}
		
	}
	
	public boolean accept(File file) {
		return false;
	}
}
