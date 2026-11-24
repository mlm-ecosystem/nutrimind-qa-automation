package expert.nbt.nutrimind.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import expert.nbt.nutrimind.config.TestConfig;

@SuppressWarnings("resource")
public final class PlansModal extends BasePage {
    private final Runnable privacy;

    public PlansModal(Page page, TestConfig routes, Runnable privacy) {
        super(page, routes);
        this.privacy = privacy;
    }

    private Locator dialog() {
        return page().getByRole(AriaRole.DIALOG).filter(new Locator.FilterOptions().setHasText("Plans"));
    }

    public Locator title() {
        return dialog().getByText("Plans", new Locator.GetByTextOptions().setExact(true));
    }

    public Locator cards() {
        // Plan cards do not expose a semantic role in the application.
        return dialog().locator(".tariffs-info");
    }

    public Locator chooseButtons() {
        return dialog().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Choose").setExact(true));
    }

    public Locator closeButton() {
        return dialog().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Close").setExact(true));
    }

    public ChooseGameTypePage close() {
        closeButton().click();
        return ready(new ChooseGameTypePage(page(), routes(), privacy));
    }

    @Override protected void waitForPageContent() {
        title().waitFor();
        cards().nth(3).waitFor();
    }
}
