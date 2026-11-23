package expert.nbt.nutrimind.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;
import expert.nbt.nutrimind.config.TestConfig;
import java.util.regex.Pattern;

@SuppressWarnings("resource")
public final class CreateNewGamePlayInTeamPage extends BasePage {
    private final Runnable privacy;

    public CreateNewGamePlayInTeamPage(Page page, TestConfig routes, Runnable privacy) {
        super(page, routes);
        this.privacy = privacy;
    }

    public CreateNewGamePlayInTeamPage selectOrganizer() {
        page().getByText(Pattern.compile("^Organizer$", Pattern.CASE_INSENSITIVE))
                .filter(new Locator.FilterOptions().setVisible(true)).locator("xpath=..").click();
        waitUntilReady();
        return this;
    }

    public Locator gameName() {
        return page().getByRole(com.microsoft.playwright.options.AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Enter game name").setExact(true));
    }

    public void enterGameName(String name) {
        gameName().fill(name);
    }

    public Locator securityCode() {
        return page().getByRole(com.microsoft.playwright.options.AriaRole.TEXTBOX,
                new Page.GetByRoleOptions().setName("Enter the room password").setExact(true));
    }

    public void enterSecurityCode(String code) {
        securityCode().fill(code);
    }

    public void selectNumberOfPlayers(int count) {
        page().getByText(Pattern.compile("^" + count + "$"))
                .filter(new Locator.FilterOptions().setVisible(true)).locator("xpath=../..").click();
    }

    public Locator createButton() {
        return page().getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Create").setExact(true));
    }

    public EditingRoomPage createGame() {
        createButton().click();
        page().waitForCondition(() -> !page().url().contains("/create-game;view=many-players"));
        return ready(new EditingRoomPage(page(), routes(), privacy));
    }

    @Override public void waitUntilReady() {
        page().getByText(Pattern.compile("^Game creation$", Pattern.CASE_INSENSITIVE)).waitFor();
    }
}
