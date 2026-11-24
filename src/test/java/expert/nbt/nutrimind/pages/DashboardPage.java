package expert.nbt.nutrimind.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import expert.nbt.nutrimind.config.TestConfig;

@SuppressWarnings("resource")
public final class DashboardPage extends BasePage {
    public DashboardPage(Page page, TestConfig routes) {
        super(page, routes);
    }

    public Locator heading() {
        return page().getByText("Dashboard", new Page.GetByTextOptions().setExact(true))
                .filter(new Locator.FilterOptions().setVisible(true));
    }

    @Override protected void waitForPageContent() {
        heading().waitFor();
    }
}
