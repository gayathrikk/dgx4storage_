package dd.project;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeDriver;

import io.github.bonigarcia.wdm.WebDriverManager;
public class BrowserLaunching {
	public static void main(String[] args) {
        // Setup EdgeDriver using WebDriverManager
        WebDriverManager.edgedriver().setup();
        
        // Initialize Edge browser
        WebDriver driver = new EdgeDriver();
        
        // Maximize window
        driver.manage().window().maximize();
        
        // Launch the URL
        driver.get("https://brainportal.humanbrain.in/publicview/index.html");
        
        // Get actual title of the page
        String actualTitle = driver.getTitle();
        System.out.println("Actual Page Title: " + actualTitle);
        
        // Update the expected title to match the actual title
        String expectedTitle = "SGBC -IITM"; // Update this value
        
        // Verify the page title
        if (actualTitle.equals(expectedTitle)) {
            System.out.println("Test Passed: Brain Portal launched successfully.");
        } else {
            System.out.println("Test Failed: Incorrect page title - " + actualTitle);
        }
        
        // Close the browser
        driver.quit();
    }
}