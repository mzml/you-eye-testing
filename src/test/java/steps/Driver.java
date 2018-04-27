package steps;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;

import java.io.FileInputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Selenium Driver Class
 */
public class Driver {
	// local variables
	private static Driver instance = null;
	private static final int IMPLICIT_TIMEOUT = 0;
	private ThreadLocal<WebDriver> webDriver = new ThreadLocal<WebDriver>();
	private ThreadLocal<String> sessionId = new ThreadLocal<String>();
	private ThreadLocal<String> sessionBrowser = new ThreadLocal<String>();
	private ThreadLocal<String> sessionPlatform = new ThreadLocal<String>();
	private ThreadLocal<String> sessionVersion = new ThreadLocal<String>();
	private String getEnv = null;
	private Properties props = new Properties();
	private ThreadLocal<WebDriver> mobileDriver;

	// constructor
	private Driver() {
	}

	/**
	 * getInstance method to retrieve active driver instance
	 *
	 * @return Driver
	 */
	public static Driver getInstance() {
		if (instance == null) {
			instance = new Driver();
		}

		return instance;
	}

	/**
	 * setDriver method to create driver instance
	 *
	 * @param browser
	 * @param environment
	 * @param platform
	 * @param optPreferences
	 * @throws Exception
	 */
	@SafeVarargs
	public final void setDriver(String browser, String platform, String environment,
			Map<String, Object>... optPreferences) throws Exception {

		DesiredCapabilities caps = null;
		String getPlatform = null;
		props.load(new FileInputStream("Global_VARS.SE_PROPS"));

		switch (browser) {
		case "firefox":
			caps = DesiredCapabilities.firefox();

			FirefoxOptions ffOpts = new FirefoxOptions();
			FirefoxProfile ffProfile = new FirefoxProfile();

			ffProfile.setPreference("browser.autofocus", true);
			ffProfile.setPreference("browser.tabs.remote.autostart.2", false);

			caps.setCapability(FirefoxDriver.PROFILE, ffProfile);
			caps.setCapability("marionette", true);

			// then pass them to the local WebDriver
			if (environment.equalsIgnoreCase("local")) {
				System.setProperty("webdriver.gecko.driver", props.getProperty("gecko.driver.windows.path"));
				webDriver.set(new FirefoxDriver(ffOpts.merge(caps)));
			}

			break;
		case "chrome":
			caps = DesiredCapabilities.chrome();

			ChromeOptions chOptions = new ChromeOptions();
			Map<String, Object> chromePrefs = new HashMap<String, Object>();

			chromePrefs.put("credentials_enable_service", false);

			chOptions.setExperimentalOption("prefs", chromePrefs);
			chOptions.addArguments("--disable-plugins", "--disable-extensions", "--disable-popup-blocking");

			caps.setCapability(ChromeOptions.CAPABILITY, chOptions);
			caps.setCapability("applicationCacheEnabled", false);

			if (environment.equalsIgnoreCase("local")) {
				System.setProperty("webdriver.chrome.driver", props.getProperty("chrome.driver.windows.path"));
				webDriver.set(new ChromeDriver(chOptions.merge(caps)));
			}

			break;
		case "internet explorer":
			caps = DesiredCapabilities.internetExplorer();

			InternetExplorerOptions ieOpts = new InternetExplorerOptions();

			ieOpts.requireWindowFocus();
			ieOpts.merge(caps);

			caps.setCapability("requireWindowFocus", true);

			if (environment.equalsIgnoreCase("local")) {
				System.setProperty("webdriver.ie.driver", props.getProperty("ie.driver.windows.path"));
				webDriver.set(new InternetExplorerDriver(ieOpts.merge(caps)));
			}

			break;
		case "iphone":
		case "ipad":
			if (browser.equalsIgnoreCase("ipad")) {
				caps = DesiredCapabilities.ipad();
			}

			else {
				caps = DesiredCapabilities.iphone();
			}

			caps.setCapability("appName", "https://myapp.com/myApp.zip");

			caps.setCapability("udid", "12345678"); // physical device
			caps.setCapability("device", "iPhone"); // or iPad

			mobileDriver.set(new IOSDriver<MobileElement>(new URL("http://127.0.0.1:4723/wd/hub"), caps));

			break;
		case "android":
			caps = DesiredCapabilities.android();

			caps.setCapability("appName", "https://myapp.com/myApp.apk");

			caps.setCapability("udid", "12345678"); // physical device
			caps.setCapability("device", "android");
			mobileDriver.set(new AndroidDriver<MobileElement>(new URL("http://127.0.0.1:4723/wd/hub"), caps));
			break;
		}

		getEnv = environment;
		getPlatform = platform;
		sessionId.set(((RemoteWebDriver) webDriver.get()).getSessionId().toString());
		sessionBrowser.set(caps.getBrowserName());
		sessionVersion.set(caps.getVersion());
		sessionPlatform.set(getPlatform);

		System.out.println("\n*** TEST ENVIRONMENT = " + getSessionBrowser().toUpperCase() + "/"
				+ getSessionPlatform().toUpperCase() + "/" + getEnv.toUpperCase() + "/Selenium Version="
				+ props.getProperty("selenium.revision") + "/Session ID=" + getSessionId() + "\n");

		getDriver().manage().timeouts().implicitlyWait(IMPLICIT_TIMEOUT, TimeUnit.SECONDS);
		getDriver().manage().window().maximize();
	}

	/**
	 * getDriver method to retrieve active driver
	 *
	 * @return WebDriver
	 */
	public WebDriver getDriver() {
		return webDriver.get();
	}

	/**
	 * closeDriver method to close active driver
	 *
	 */
	public void closeDriver() {
		try {
			getDriver().quit();
		}

		catch (Exception e) {
			// do something
		}
	}

	/**
	 * getSessionId method to retrieve active id
	 *
	 * @return String
	 * @throws Exception
	 */
	public String getSessionId() throws Exception {
		return sessionId.get();
	}

	/**
	 * getSessionBrowser method to retrieve active browser
	 * 
	 * @return String
	 * @throws Exception
	 */
	public String getSessionBrowser() throws Exception {
		return sessionBrowser.get();
	}

	/**
	 * getSessionVersion method to retrieve active version
	 *
	 * @return String
	 * @throws Exception
	 */
	public String getSessionVersion() throws Exception {
		return sessionVersion.get();
	}

	/**
	 * getSessionPlatform method to retrieve active platform
	 * 
	 * @return String
	 * @throws Exception
	 */
	public String getSessionPlatform() throws Exception {
		return sessionPlatform.get();
	}

}