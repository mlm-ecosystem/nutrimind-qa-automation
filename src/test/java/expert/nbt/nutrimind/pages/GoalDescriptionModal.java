package expert.nbt.nutrimind.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import expert.nbt.nutrimind.config.TestConfig;

@SuppressWarnings("resource")
public final class GoalDescriptionModal extends BasePage {
    public GoalDescriptionModal(Page page, TestConfig routes) {
        super(page, routes);
    }

    private Locator dialog() {
        return page().getByRole(AriaRole.DIALOG).filter(new Locator.FilterOptions()
                .setHasText("Goal description"));
    }

    public Locator title() {
        return dialog().getByText("Goal description", new Locator.GetByTextOptions().setExact(true));
    }

    public Locator description() {
        // The description has no semantic role in the application.
        return dialog().locator(".block-descriptions");
    }

    public Locator gotItButton() {
        return dialog().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Got it").setExact(true));
    }

    public GameBoardPage close() {
        gotItButton().click();
        return ready(new GameBoardPage(page(), routes()));
    }

    @Override protected void waitForPageContent() {
        title().waitFor();
        gotItButton().waitFor();
    }
}
