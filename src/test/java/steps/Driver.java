package steps;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;

/**
 * 
 * @author mzml
 *
 */
public class Driver {
	// local variables
	private static Driver instance = null;
	private String browserHandle = null;
	private static final int IMPLICIT_TIMEOUT = 0;
	private ThreadLocal<WebDriver> webDriver = new ThreadLocal<WebDriver>();
	private ThreadLocal<AppiumDriver<MobileElement>> mobileDriver = new ThreadLocal<AppiumDriver<MobileElement>>();
	private ThreadLocal<String> sessionId = new ThreadLocal<String>();
	private ThreadLocal<String> sessionBrowser = new ThreadLocal<String>();
	private ThreadLocal<String> sessionPlatform = new ThreadLocal<String>();
	private ThreadLocal<String> sessionVersion = new ThreadLocal<String>();
	private String getEnv = null;

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
	 * setDriver method
	 *
	 * @param browser
	 * @param environment
	 * @param platform
	 * @param optPreferences
	 * @throws Exception
	 */
	@SafeVarargs
	public final void setDriver(String browser, String environment, String platform,
			Map<String, Object>... optPreferences) throws Exception {

		DesiredCapabilities caps = null;
		String localHub = "http://127.0.0.1:4723/wd/hub";
		String getPlatform = null;

		getEnv = "local";
		getPlatform = platform;

		if (browser.equalsIgnoreCase("iphone") || browser.equalsIgnoreCase("android")) {

			sessionId.set(((IOSDriver<MobileElement>) mobileDriver.get()).getSessionId().toString());

			sessionId.set(((AndroidDriver<MobileElement>) mobileDriver.get()).getSessionId().toString());

			sessionBrowser.set(browser);
			sessionVersion.set(caps.getCapability("deviceName").toString());
			sessionPlatform.set(getPlatform);
		} else {
			sessionId.set(((RemoteWebDriver) webDriver.get()).getSessionId().toString());

			sessionBrowser.set(caps.getBrowserName());
			sessionVersion.set(caps.getVersion());
			sessionPlatform.set(getPlatform);
		}

		switch (browser) {
		case "firefox":
			caps = DesiredCapabilities.firefox();

			FirefoxOptions ffOpts = new FirefoxOptions();
			FirefoxProfile ffProfile = new FirefoxProfile();
			ffProfile.setPreference("browser.autofocus", true);

			caps.setCapability(FirefoxDriver.PROFILE, ffProfile);
			caps.setCapability("marionette", true);
			if (optPreferences.length > 0) {
				processFFProfile(ffProfile, optPreferences);
			}
			webDriver.set(new FirefoxDriver(caps));

			// Selenium 3.7.x
			// webDriver.set(new FirefoxDriver(ffOpts.merge(caps)));
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
			if (optPreferences.length > 0) {
				processCHOptions(chOptions, optPreferences);
			}
			webDriver.set(new ChromeDriver(caps));

			// Selenium 3.7.x
			// webDriver.set(new ChromeDriver(chOptions.merge(caps)));

			break;

		case "internet explorer":
			caps = DesiredCapabilities.internetExplorer();

			InternetExplorerOptions ieOpts = new InternetExplorerOptions();
			ieOpts.requireWindowFocus();

			ieOpts.merge(caps);
			caps.setCapability("requireWindowFocus", true);
			if (optPreferences.length > 0) {
				processDesiredCaps(caps, optPreferences);
			}
			webDriver.set(new InternetExplorerDriver(caps));

			// Selenium 3.7.x
			// webDriver.set(new InternetExplorerDriver(ieOpts.merge(caps)));
			break;

		case "safari":
			caps = DesiredCapabilities.safari();
			caps = DesiredCapabilities.safari();

			SafariOptions safariOpts = new SafariOptions();
			safariOpts.setUseCleanSession(true);

			caps.setCapability(SafariOptions.CAPABILITY, safariOpts);
			caps.setCapability("autoAcceptAlerts", true);

			webDriver.set(new SafariDriver(caps));

			// Selenium 3.7.x
			// webDriver.set(new SafariDriver(safariOpts.merge(caps)));

			break;

		case "microsoftedge":
			caps = DesiredCapabilities.edge();
			caps = DesiredCapabilities.edge();

			EdgeOptions edgeOpts = new EdgeOptions();
			edgeOpts.setPageLoadStrategy("normal");

			caps.setCapability(EdgeOptions.CAPABILITY, edgeOpts);
			caps.setCapability("requireWindowFocus", true);

			webDriver.set(new EdgeDriver(caps));

			// Selenium 3.7.x
			// webDriver.set(new EdgeDriver(edgeOpts.merge(caps)));
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
			caps.setCapability("device", "Android");

			mobileDriver.set(new AndroidDriver<MobileElement>(new URL("http://127.0.0.1:4723/wd/hub"), caps));
			break;
		}

	}

	/**
	 * overloaded setDriver method to switch driver to specific WebDriver if running
	 * concurrent drivers
	 *
	 * @param driver
	 *            WebDriver instance to switch to
	 */
	public void setDriver(WebDriver driver) {
		webDriver.set(driver);

		sessionId.set(((RemoteWebDriver) webDriver.get()).getSessionId().toString());

		sessionBrowser.set(((RemoteWebDriver) webDriver.get()).getCapabilities().getBrowserName());

		sessionPlatform.set(((RemoteWebDriver) webDriver.get()).getCapabilities().getPlatform().toString());

		setBrowserHandle(getDriver().getWindowHandle());
	}

	/**
	 * overloaded setDriver method to switch driver to specific AppiumDriver if
	 * running concurrent drivers
	 *
	 * @param driver
	 *            AppiumDriver instance to switch to
	 */
	public void setDriver(AppiumDriver<MobileElement> driver) {
		mobileDriver.set(driver);

		sessionId.set(mobileDriver.get().getSessionId().toString());

		sessionBrowser.set(mobileDriver.get().getCapabilities().getBrowserName());

		sessionPlatform.set(mobileDriver.get().getCapabilities().getPlatform().toString());
	}

	/**
	 * getDriver method will retrieve the active WebDriver
	 *
	 * @return WebDriver
	 */
	public WebDriver getDriver() {
		return webDriver.get();
	}

	/**
	 * getDriver method will retrieve the active AppiumDriver
	 *
	 * @param mobile
	 *            boolean parameter
	 * @return AppiumDriver
	 */
	public AppiumDriver<MobileElement> getDriver(boolean mobile) {
		return mobileDriver.get();
	}

	/**
	 * getCurrentDriver method will retrieve the active WebDriver or AppiumDriver
	 *
	 * @return WebDriver
	 */
	public WebDriver getCurrentDriver() {
		if (getInstance().getSessionBrowser().contains("iphone") || getInstance().getSessionBrowser().contains("ipad")
				|| getInstance().getSessionBrowser().contains("android")) {

			return getInstance().getDriver(true);
		}

		else {
			return getInstance().getDriver();
		}
	}

	/**
	 * driverWait method pauses the driver in seconds
	 *
	 * @param seconds
	 *            to pause
	 */
	public void driverWait(long seconds) {
		try {
			Thread.sleep(TimeUnit.SECONDS.toMillis(seconds));
		}

		catch (InterruptedException e) {
			// do something
		}
	}

	/**
	 * driverRefresh method reloads the current browser page
	 */
	public void driverRefresh() {
		getCurrentDriver().navigate().refresh();
	}

	/**
	 * closeDriver method quits the current active driver
	 */
	public void closeDriver() {
		try {
			getCurrentDriver().quit();
		}

		catch (Exception e) {
			// do something
		}
	}

	/**
	 * getSessionId method gets the browser or mobile id of the active session
	 *
	 * @return String
	 */
	public String getSessionId() {
		return sessionId.get();
	}

	/**
	 * getSessionBrowser method gets the browser or mobile type of the active
	 * session
	 *
	 * @return String
	 */
	public String getSessionBrowser() {
		return sessionBrowser.get();
	}

	/**
	 * getSessionVersion method gets the browser or mobile version of the active
	 * session
	 * 
	 * @return String
	 */
	public String getSessionVersion() {
		return sessionVersion.get();
	}

	/**
	 * getSessionPlatform method gets the browser or mobile platform of the active
	 * session
	 *
	 * @return String
	 */
	public String getSessionPlatform() {
		return sessionPlatform.get();
	}
}