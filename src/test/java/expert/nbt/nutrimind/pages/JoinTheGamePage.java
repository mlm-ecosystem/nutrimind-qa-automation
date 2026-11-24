package expert.nbt.nutrimind.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import expert.nbt.nutrimind.config.TestConfig;
import java.util.regex.Pattern;

@SuppressWarnings("resource")
public final class JoinTheGamePage extends BasePage {
    private final Runnable privacy;

    public JoinTheGamePage(Page page, TestConfig routes, Runnable privacy) {
        super(page, routes);
        this.privacy = privacy;
    }

    private Locator firstProtectedRoom() {
        // Room cards and the code input have no accessible relation, so scope to a protected card by its input.
        return page().locator(".item-room:has(input)").first();
    }

    private Locator securityCodeInput() {
        return firstProtectedRoom().locator("input").filter(new Locator.FilterOptions().setVisible(true));
    }

    public Locator joinButton() {
        return firstProtectedRoom().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName(Pattern.compile("^Join")));
    }

    public void enterSecurityCode(String code) {
        securityCodeInput().fill(code);
    }

    public EditingRoomPage joinRoom() {
        joinButton().click();
        return ready(new EditingRoomPage(page(), routes(), privacy));
    }

    public Locator incorrectPasswordDialog() {
        return page().getByRole(AriaRole.DIALOG).filter(new Locator.FilterOptions()
                .setHasText("Incorrect room password."));
    }

    @Override protected void waitForPageContent() {
        page().getByText("Join the game", new Page.GetByTextOptions().setExact(true)).waitFor();
        page().getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("^Join"))).first().waitFor();
    }
}
