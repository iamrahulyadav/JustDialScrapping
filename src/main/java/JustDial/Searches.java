package JustDial;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

public class Searches extends Base implements Runnable {
	
	private String query;
	
	public Searches(String query) {
		this.query = query;
	}
	
	private void scrollDownThePageForSometime() {
		
		System.out.println("Started fetching records for : "+query);
		
		int i=0;
		JavascriptExecutor jse = (JavascriptExecutor)wd;
		while(i<4000) {
			jse.executeScript("window.scrollBy(0,2000)", "");
			i++;
		}
		
	}
	
	private List<Data> extractData() {
		
		System.out.println("Started extracting data from records for : "+query);
		
		Actions action = new Actions(wd);
		
		List<Data> data = new ArrayList<Data>();

		List<WebElement> listItems = wd.findElements(By.className("cntanr"));
		for(WebElement item : listItems) {
			WebElement one = item.findElement(By.tagName("section"))
				.findElement(By.className("colsp"))
				.findElement(By.tagName("section"))
				.findElement(By.className("store-details"));
			Data d = new Data();
			
			try {
				d.setName(one.findElement(By.tagName("h4")).findElement(By.tagName("span")).findElement(By.tagName("a")).getText());
			} catch(Exception e) {
				d.setName("");
			}
			
			try {
				d.setPhone(one.findElement(By.className("contact-info")).findElement(By.tagName("span")).findElement(By.tagName("a")).getText());
			} catch(Exception e) {
				d.setPhone("");
			}
			
			try {
				WebElement addressElement = one.findElement(By.className("address-info")).findElement(By.tagName("span")).findElement(By.tagName("a"));
				action.moveToElement(addressElement).build().perform();
				d.setAddress(addressElement.findElement(By.tagName("span")).getText());
			} catch(Exception e) {
				d.setAddress("");
			}
			
			data.add(d);
		}
		
		return data;
		
	}
	
	private void saveDataInExcel(List<Data> data) {
		
		System.out.println("Started saving data of records for : "+query);
		
		File file = new File(localPath+query+".csv");
		FileWriter outputFile = null;
		
		try {
			outputFile = new FileWriter(file);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Could not create/open File in Location : "+file.getAbsolutePath());
		}
		
		try {
			outputFile.write( Data.firstLineForExcel );
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Could not write firstLine of Excel File in Location : "+file.getAbsolutePath());
		}
		
		for(Data entry : data) {
			try {
				outputFile.write( entry.toStringForExcel() );
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Could not write File in Location : "+file.getAbsolutePath());
			}
		}
		
		try {
			outputFile.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Could not close File in Location : "+file.getAbsolutePath());
		}
		
		System.out.println("Successfully saved to : "+file);
		
	}
	
	private void searchInJustDial() {
		
		init(query);
		
		WebElement searchButton = wd.findElement(By.className("search-button"));
		searchButton.click();
		
		//try to scroll the page till the end, to get most results out of it
		scrollDownThePageForSometime();
		
		//change the wait time, since no more loading is needed, for client to communicate with server
		wd.manage().timeouts().implicitlyWait(1, TimeUnit.MILLISECONDS);
		
		//extract data
		List<Data> data = extractData();
		
		//save data in excel
		saveDataInExcel(data);
		
		//Close the browser, i.e. Web Driver
		wd.close();
		
	}
	
	public void run() {
		
		System.out.println("Thread Starts : "+query);
		searchInJustDial();
		System.out.println("Thread Ends : "+query);
		
	}

}
