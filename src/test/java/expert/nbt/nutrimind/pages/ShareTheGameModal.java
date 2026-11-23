package expert.nbt.nutrimind.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import expert.nbt.nutrimind.config.TestConfig;

@SuppressWarnings("resource")
public final class ShareTheGameModal extends BasePage {
    public ShareTheGameModal(Page page, TestConfig routes) {
        super(page, routes);
    }

    private Locator dialog() {
        return page().getByRole(AriaRole.DIALOG).filter(
                new Locator.FilterOptions().setHasText("Share the game"));
    }

    public Locator title() {
        return dialog().getByText("Share the game", new Locator.GetByTextOptions().setExact(true));
    }

    public Locator copyButton() {
        return dialog().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Clear").setExact(true));
    }

    public Locator closeButton() {
        return dialog().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Close").setExact(true));
    }

    @Override protected void waitForPageContent() {
        title().waitFor();
    }
}
