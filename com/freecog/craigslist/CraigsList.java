package com.freecog.craigslist;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

/**
 * The CraigsList class exposes a few simple services for doing job searches
 * across all Craigs List sites.
 * 
 * @author Matt Brandt
 */
public class CraigsList {
	private static final String BASE_URL = "http://geo.craigslist.org/iso/";
	
	// paths for specific searches
	private static final String ALL_JOBS = "jjj/";
	private static final String SOFTWARE_JOBS = "sof/";
	private static final String WEB_JOBS = "web/";
	
	// holds search stats
	private int _sitesSearched;
	private int _sitesWithResults;
	private int _resultsFound;
	
	private HashMap<String, String> _countryCodes;
	private String _directory;
	
	/**
	 * Creates an instance of CraigsList.
	 * @param directory The directory to place the search result files.
	 */
	public CraigsList(String directory) {
		_directory = directory;
		loadCountryCodes();
	}

	/**
	 * Initialize the counters to zero.
	 */
	private void resetStatsCounter() {
		_sitesSearched = 0;
		_sitesWithResults = 0;
		_resultsFound = 0;
	}
	
	/**
	 * <p>Search for jobs all over the world. Creates a html page for each search
	 * term in the String[] and each country that was searched.</p>
	 * <b>Example files:</b><br />
	 * searchTerms = {"grilled cheese flipper", "cheese sandwich maker", "cheese"}
	 * <ul>
	 *   <li>Canada_grilled_cheese_flipper_job_results.html</li>
	 *   <li>Canada_cheese_sandwich_maker_job_results.html</li>
	 *   <li>Canada_cheese_job_results.html</li>
	 *   <li>USA_grilled_cheese_flipper_job_results.html</li>
	 *   <li>USA_cheese_sandwich_maker_job_results.html</li>
	 *   <li>USA_cheese_job_results.html</li>
	 * </ul>
	 * @param searchTerms The search terms to be used.
	 * @param telecommute True, find only telecommute jobs. False find all jobs
	 * 			including telecommute jobs.
	 */
	public void findAllJobsGlobally(String[] searchTerms, boolean telecommute) {
		searchAllCountries(searchTerms, ALL_JOBS, telecommute);
	}
	
	/**
	 * <p>Search for jobs all over the world. Creates a html page for each search
	 * term in the String[] and each country that was searched. <em>See 
	 * findAllJobsGlobally() for example output files.</em></p>
	 * 
	 * @param searchTerms The search terms to be used.
	 * @param telecommute True, find only telecommute jobs. False find all jobs
	 * 			including telecommute jobs.
	 */
	public void findAllSoftwareJobsGlobally(String[] searchTerms, boolean telecommute) {
		searchAllCountries(searchTerms, SOFTWARE_JOBS, telecommute);
	}
	
	/**
	 * <p>Search for Web Development jobs all over the world. Creates a html 
	 * page for each search term in the String[] and each country that was 
	 * searched. <em>See findAllJobsGlobally() for example output files.</em></p>
	 * 
	 * @param searchTerms The search terms to be used.
	 * @param telecommute True, find only telecommute jobs. False find all jobs
	 * 			including telecommute jobs.
	 */
	public void findAllWebJobsGlobally(String[] searchTerms, boolean telecommute) {
		searchAllCountries(searchTerms, WEB_JOBS, telecommute);
	}
	
	private String createFileName(String term, String country) {
		return country + "_" + term.replace(" ", "_") + "_job_results.html";
	}
	
	private String getSearchStats() {
		return "Sites seached: " + _sitesSearched +
				" | Sites with results: " + _sitesWithResults +
				" | Results found: " + _resultsFound;
	}
	
	/**
	 * Does a Global/Worldwide search for jobs on Craigslist
	 * 
	 * @param searchTerms The search terms to be used.
	 * @param searchType Specifies the resource to search for. 
	 * 			Examples: ALL_JOBS, SOFTWARE_JOBS, WEB_JOBS
	 * @param telecommute True, find only telecommute jobs. False find all jobs
	 * 			including telecommute jobs.
	 */
	private void searchAllCountries(String[] searchTerms,
									String searchType,
									boolean telecommute) {
		resetStatsCounter();
		for(String country : _countryCodes.keySet()) {
			System.out.println("\n\nCountry: " + country + "\n\n");
			String url = BASE_URL + _countryCodes.get(country);
			for(String searchTerm : searchTerms) {
				String fileName = createFileName(searchTerm, country);
				writePageHeader(searchTerm, fileName);
				
				StringBuffer results = doJobSearch(searchTerm, telecommute, url, searchType);
				String stats = "<h3>" + getSearchStats() + "</h3>\n";
				
				writeToFile(stats, fileName);
				writeToFile(results.toString(), fileName);
				resetStatsCounter();
				writePageTail(fileName);
			}
		}
	}
	
	/**
 	 * @param searchTerm Search field value
	 * @param telecommuteJobs True, look for only telecommute jobs.
	 * False, to look
	 * @param resource The resource locator
	 * @return A StringBuffer containing search results 
	 */
	private StringBuffer doJobSearch(String searchTerm, Boolean telecommuteJobs,
												String baseUrl,
												String resource) {
		StringBuffer results = new StringBuffer();
		WebDriver driver = new HtmlUnitDriver();
		driver.get(baseUrl);
		ArrayList<String> links = findAllLinks(driver);
		for (String link : links) {
			System.out.println("Visiting Page: " + link);
			_sitesSearched += 1;
			driver.get(link + resource);
			driver.findElement(By.id("query")).sendKeys(searchTerm);
			if (telecommuteJobs) {
				driver.findElement(By.name("addOne")).click();
			}
			driver.findElement(By.xpath("//input[@value='Search']")).click();
			results.append( getAllLinksFromParagraphs(driver) );
		}
		
		return results;
	}
	
	/**
	 * Pulls all links contained in paragraphs from the driver.
	 * 
	 * @param driver A WebDriver object instantiated to a webpage.
	 * @return A StringBuffer containing all links pulled from the page.
	 */
	private StringBuffer getAllLinksFromParagraphs(WebDriver driver) {
		Boolean writePage = true;
		List<WebElement> paragraphs = driver.findElements(By.tagName("p"));
		StringBuffer results = new StringBuffer();
		for (WebElement paragraph : paragraphs) {
			String href = paragraph.findElement(By.tagName("a")).toString();
			System.out.println("Visiting page: " + driver.getTitle());
			if ( writePage && href.length() != 0) {
				results.append("<br /><br />Site: " + driver.getTitle() + 
						" --> <a href='" + driver.getCurrentUrl() + "'>" +
								"results page</a><br />");
				_resultsFound += 1;
				_sitesWithResults += 1;
				results.append(href + paragraph.getText() + "<a/><br />");
				writePage = false;
			} else if (href.length() != 0) {
				_resultsFound += 1;
				results.append(href + paragraph.getText() + "<a/><br />");
			} 
		}
		
		return results;
	}

	/**
	 * Pulls out the URI from an <a href> tag.
	 * 
	 * @param href the href tag to get the URI from
	 * @return the URI. Example - http://freecog.com
	 */
	private String getURI(String href) {
		href = href.replaceAll("<a href=\"", "");
		href = href.replaceAll("\">", "");

		return href;
	}

	/**
	 * Finds all of the links on http://geo.craigslist.org/iso/us 
	 * starter page except for: "craigslist", "w", and "or suggest 
	 * a new one" links.
	 * 
	 * @param driver A WebDriver object instantiated to a webpage.
	 * @return An list of all URIs on the webpage.
	 */
	private ArrayList<String> findAllLinks(WebDriver driver) {
		ArrayList<String> results = new ArrayList<String>();
		List<WebElement> hrefs = driver.findElements(By.tagName("a"));
		for (WebElement href : hrefs) {
			String text = href.getText();
			if (!text.equals("craigslist") && !text.equals("w")
					&& !text.equals("or suggest a new one")) {
				String link = getURI(href.toString());
				results.add(link);
			}
		}

		return results;
	}

	/**
	 * Initializes the country codes HashMap
	 */
	private void loadCountryCodes() {
		_countryCodes = new HashMap<String, String>();
		_countryCodes.put("Australia", "au");
		_countryCodes.put("Canada", "ca");
		_countryCodes.put("Japan", "jp");
		_countryCodes.put("New_Zealand", "nz");
		_countryCodes.put("South_Africa", "za");
		_countryCodes.put("UK", "gb");
		_countryCodes.put("USA", "us");
	}
	
	
	/**
	 * Write the top of the html page.
	 * 
	 * @param searchTerm Adds the searchTerm to the page title and 
	 * creates a h3 element with the same information.
	 * @param jobCategory Adds the job category being searched to 
	 * an h4 element.
	 * @param fileName
	 */
	private void writePageHeader(String searchTerm, String fileName) {
		String header = "<html>\n<head>" +
						"<title>Job Search || " + searchTerm + "</title>" +
						"</head>\n<body>\n";
		writeToFile(header, fileName);
	}
	
	private void writePageTail(String fileName) {
		writeToFile("\n</body>\n</html>", fileName);
	}

	/**
	 * Write a string to the log file.
	 * 
	 * @param text The String to record.
	 */
	private void writeToFile(String text, String fileName) {
		try {
			FileWriter file = new FileWriter(_directory + fileName, true);
			PrintWriter out = new PrintWriter(file);
			out.println(text);
			out.close();
		} catch (IOException e) {}
	}

	public static void main(String args[]) {
		// matt's telecommuting job search
		String[] mattSearch = {"qa", "software tester", "automated testing"};
		String mattDir = "/home/matt/public_html/matt_jobs/";
		
		CraigsList matt = new CraigsList(mattDir);
		matt.findAllJobsGlobally(mattSearch, true); 
	}

}
