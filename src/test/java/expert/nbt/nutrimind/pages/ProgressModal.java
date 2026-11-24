package expert.nbt.nutrimind.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import expert.nbt.nutrimind.config.TestConfig;
import java.util.regex.Pattern;

@SuppressWarnings("resource")
public final class ProgressModal extends BasePage {
    private final Runnable privacy;

    public ProgressModal(Page page, TestConfig routes, Runnable privacy) {
        super(page, routes);
        this.privacy = privacy;
    }

    private Locator dialog() {
        return page().getByRole(AriaRole.DIALOG).filter(new Locator.FilterOptions().setHasText("Progress"));
    }

    public Locator title() {
        return dialog().getByText("Progress", new Locator.GetByTextOptions().setExact(true));
    }

    public Locator idColumn() {
        return dialog().getByText("ID", new Locator.GetByTextOptions().setExact(true));
    }

    public Locator nameColumn() {
        return dialog().getByText("Name", new Locator.GetByTextOptions().setExact(true));
    }

    public Locator continueButton() {
        return dialog().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName(Pattern.compile("continue", Pattern.CASE_INSENSITIVE)));
    }

    public ChooseGameTypePage continueToGameTypes() {
        continueButton().click();
        return ready(new ChooseGameTypePage(page(), routes(), privacy));
    }

    @Override protected void waitForPageContent() {
        title().waitFor();
        continueButton().waitFor();
    }
}
