package expert.nbt.nutrimind.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import expert.nbt.nutrimind.config.TestConfig;

@SuppressWarnings("resource")
public final class PersonalReferralLinkModal extends BasePage {
    private final Runnable privacy;

    public PersonalReferralLinkModal(Page page, TestConfig routes, Runnable privacy) {
        super(page, routes);
        this.privacy = privacy;
    }

    private Locator dialog() {
        return page().getByRole(AriaRole.DIALOG).filter(new Locator.FilterOptions()
                .setHasText("Personal referral link"));
    }

    public Locator title() {
        return dialog().getByText("Personal referral link", new Locator.GetByTextOptions().setExact(true));
    }

    public Locator internalLink() {
        return dialog().getByText("Internal link", new Locator.GetByTextOptions().setExact(true));
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
