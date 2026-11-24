package expert.nbt.nutrimind.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import expert.nbt.nutrimind.config.TestConfig;
import java.util.regex.Pattern;

@SuppressWarnings("resource")
public final class CreateNewGameSoloPage extends BasePage {
    private final Runnable privacy;

    public CreateNewGameSoloPage(Page page, TestConfig routes, Runnable privacy) {
        super(page, routes);
        this.privacy = privacy;
    }
    private Locator gameType(String type) {
        return page().getByText(Pattern.compile("^" + type + "$", Pattern.CASE_INSENSITIVE)).locator("xpath=..");
    }
    public Locator selectedButton(String type) {
        return gameType(type).getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Selected").setExact(true));
    }
    public Locator selectButton(String type) {
        return gameType(type).getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Select").setExact(true));
    }
    public Locator createButton() { return button("Create"); }
    @Override protected void waitForPageContent() {
        page().getByText(Pattern.compile("^QUEST$", Pattern.CASE_INSENSITIVE)).waitFor();
    }
    public void selectGameType(String type) { selectButton(type).click(); }

    public EditingRoomPage createGame() {
        createButton().click();
        page().waitForCondition(() -> !page().url().contains("/create-game;view=one-player"));
        return ready(new EditingRoomPage(page(), routes(), privacy));
    }
}
