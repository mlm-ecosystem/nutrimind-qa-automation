package expert.nbt.nutrimind.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import expert.nbt.nutrimind.config.TestConfig;
import java.util.regex.Pattern;

@SuppressWarnings("resource")
public final class EditingRoomPage extends BasePage {
    private final Runnable privacy;

    public EditingRoomPage(Page page, TestConfig routes, Runnable privacy) {
        super(page, routes);
        this.privacy = privacy;
    }

    public Locator playerName() {
        return page().getByRole(AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Player name").setExact(true));
    }

    public Locator playerNameField() {
        return playerName().locator("xpath=ancestor::mat-form-field");
    }

    public Locator saveButton() {
        return page().getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Save").setExact(true));
    }

    public void clearPlayerName() {
        playerName().fill("");
        playerName().press("Tab");
    }

    public WaitingForPlayersPage saveEdits() {
        saveButton().click();
        return new WaitingForPlayersPage(page(), routes(), privacy);
    }

    public void cancelGame() {
        page().getByText(Pattern.compile("^Cancel$", Pattern.CASE_INSENSITIVE))
                .filter(new Locator.FilterOptions().setVisible(true)).locator("xpath=..").click();
    }

    public ChooseGameTypePage confirmCancellation() {
        button("Yes").click();
        return ready(new ChooseGameTypePage(page(), routes(), privacy));
    }

    @Override public void waitUntilReady() {
        playerName().waitFor();
    }
}
