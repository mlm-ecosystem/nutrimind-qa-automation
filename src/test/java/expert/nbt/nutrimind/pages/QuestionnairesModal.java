package expert.nbt.nutrimind.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import expert.nbt.nutrimind.config.TestConfig;

@SuppressWarnings("resource")
public final class QuestionnairesModal extends BasePage {
    private final Runnable privacy;

    public QuestionnairesModal(Page page, TestConfig routes, Runnable privacy) {
        super(page, routes);
        this.privacy = privacy;
    }

    private Locator dialog() {
        return page().getByRole(AriaRole.DIALOG).filter(new Locator.FilterOptions()
                .setHasText("List of Questionnaires"));
    }

    public Locator title() {
        return dialog().getByText("List of Questionnaires", new Locator.GetByTextOptions().setExact(true));
    }

    public Locator copyButton() {
        return dialog().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Clear").setExact(true)).first();
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
    }
}
