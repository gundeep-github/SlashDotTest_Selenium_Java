package slashdot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * 
 * @author gundeepvohra
 * webdriver version used: 4.0.0-alpha-1
 */

public class SlashDotTest {

	private WebDriver driver;

	@BeforeClass
	public void setupClass() {
		WebDriverManager.firefoxdriver().setup(); //initalize firefox driver to update with latest version
		driver = new FirefoxDriver();
		driver.get("http://slashdot.org/"); //get base url
		driver.manage().window().maximize();  //maximize window
	}

	@AfterMethod
	public void separateTestOutput() {
		System.out.println("============================================");
	}

	@AfterClass
	public void teardown() {
		if (driver != null) {
			driver.quit(); //close browser if there is an instance of river
		}
	}

	@Test(description = "Print how many articles (highlighted in green) are on the page")
	public void testArticlesCount() {
		int count = driver
				.findElements(By.xpath("//header[not(div[@class='ntv-sponsored-disclaimer'])]/h2[@class='story']"))
				.size(); // excluded the sponsored article highlighted in brown
		System.out.println("Number of articles highlighted in green : " + count);
		Assert.assertEquals(count, 15, "Articles expected and actual count doesn't match.");
	}

	@Test(description = "Print a list of unique icons (highlighted in red) used on article titles and how many times was\n"
			+ "it used")
	public void testUniqueIconsCount() {
		List<WebElement> icons = driver.findElements(By.xpath("//img[contains(@src,'64.png')]"));
		List<String> altTxt = new ArrayList<String>();
		for (WebElement icon : icons) {
			altTxt.add(icon.getAttribute("alt")); //get alt text of list of icons
		}

		Map<String, Integer> images = new HashMap<String, Integer>();
		for (String i : altTxt) {
			Integer j = images.get(i);
			images.put(i, (j == null) ? 1 : j + 1); //increment count as per occurrence with ternary operator
		}

		for (Map.Entry<String, Integer> val : images.entrySet()) { //iterate the map and get key length and value as int
			System.out.println("Element " + val.getKey() + " " + "occurs" + ": " + val.getValue() + " times");
			Assert.assertEquals(driver.findElements(By.xpath("//img[@alt='" + val.getKey() + "'] [@width='64']")).size(),
					val.getValue().intValue(),
					"Element " + val.getKey() + " expected count not matching with actual value.");
		}
	}

	@Test(description = "Vote for some random option on the daily poll, Return the number of people that have voted for that same option")
	public void testVoteRandomOption() {
		var pollInput = "//form[@id='pollBooth']//label";
		var voteNow = "//button[@class='btn-polls']";
		var voteText = "//div[@class='units-6']";
		List<WebElement> options = driver.findElements(By.xpath(pollInput));
		Random random = new Random();
		int index = random.nextInt(options.size()); //to randomly click on any poll value
		Actions actions = new Actions(driver);
		JavascriptExecutor executor = (JavascriptExecutor) driver;
		WebElement pollValue = driver.findElement(By.xpath(pollInput));
		executor.executeScript("arguments[0].scrollIntoView({block: \"center\"});", pollValue); //to scroll to center of page in order to access the poll section
		String selectedValue = options.get(index).getText();
		actions.moveToElement(options.get(index)).click().build().perform();

		WebElement voteNowBtn = driver.findElement(By.xpath(voteNow));
		voteNowBtn.click(); //click vote now button after selecting option

		new WebDriverWait(driver, 15)
				.until(driver -> executor.executeScript("return document.readyState").equals("complete")); //wait for page to load
		actions.keyDown(Keys.CONTROL).sendKeys(Keys.HOME).perform(); //scroll upto top of page
		WebElement voteTxtLbl = driver.findElement(By.xpath(voteText));
		Assert.assertTrue(voteTxtLbl.getText().contains("You've already voted"), "Vote given was not recorded");

		WebElement selectedElement = driver
				.findElement(By.xpath("//div[.='" + selectedValue + "']/parent::div//div[@class='poll-bar-text']"));
		System.out.println(
				"Number of votes for " + selectedValue + " : " + selectedElement.getText().trim().split("/")[0]); //get the actual number of votes for selected option
	}
}
