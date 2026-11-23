package expert.nbt.nutrimind.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import expert.nbt.nutrimind.config.TestConfig;

public abstract class BasePage {
    private final Page browserPage;
    private final TestConfig routes;

    protected BasePage(Page page, TestConfig routes) { browserPage = page; this.routes = routes; }
    public final Page page() { return browserPage; }
    protected final TestConfig routes() { return routes; }
    protected final Locator button(String name) {
        return browserPage.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(name).setExact(true));
    }
    protected final Locator link(String name) {
        return browserPage.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(name).setExact(true));
    }

    protected final void waitForPageLoad() {
        browserPage.waitForLoadState(LoadState.DOMCONTENTLOADED);
        browserPage.waitForLoadState(LoadState.NETWORKIDLE);
    }

    protected final <T extends BasePage> T ready(T destination) {
        destination.waitUntilReady();
        return destination;
    }

    public final void waitUntilReady() {
        waitForPageLoad();
        waitForPageContent();
    }

    protected abstract void waitForPageContent();
}
