package expert.nbt.nutrimind.tests;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import expert.nbt.nutrimind.config.TestConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import org.testng.annotations.Test;

public final class GoogleAuthenticationStateSetupTest {
    private static final Path STATE_FILE = Path.of(".secrets", "google-storage-state.json");
    private static final Path SESSION_FILE = Path.of(".secrets", "google-session-storage.json");

    @Test(description = "One-time manual Google authentication state export")
    public void saveGoogleAuthenticationState() throws IOException {
        TestConfig config = TestConfig.load();
        Files.createDirectories(STATE_FILE.getParent());
        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
             BrowserContext context = browser.newContext(new Browser.NewContextOptions().setLocale("en-US"))) {
            Page page = context.newPage();
            page.navigate(config.game(""));
            System.out.println("Sign in with Google in the opened browser. The files will be saved after the game page opens.");
            page.waitForURL(Pattern.compile("^https?://[^/?#]+/step-one(?:\\?.*)?$"),
                    new Page.WaitForURLOptions().setTimeout(300_000));
            context.storageState(new BrowserContext.StorageStateOptions().setPath(STATE_FILE).setIndexedDB(true));
            String sessionStorage = (String) page.evaluate("JSON.stringify(sessionStorage)");
            Files.writeString(SESSION_FILE, sessionStorage);
            System.out.println("Saved authenticated state in .secrets/. Do not commit or share these files.");
        }
    }
}
