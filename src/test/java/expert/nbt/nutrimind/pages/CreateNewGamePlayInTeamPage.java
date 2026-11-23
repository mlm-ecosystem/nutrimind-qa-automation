package expert.nbt.nutrimind.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;
import expert.nbt.nutrimind.config.TestConfig;
import java.util.regex.Pattern;

@SuppressWarnings("resource")
public final class CreateNewGamePlayInTeamPage extends BasePage {
    public CreateNewGamePlayInTeamPage(Page page, TestConfig routes) {
        super(page, routes);
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

    public Locator createButton() {
        return page().getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Create").setExact(true));
    }

    @Override public void waitUntilReady() {
        page().getByText(Pattern.compile("^Game creation$", Pattern.CASE_INSENSITIVE)).waitFor();
    }
}
