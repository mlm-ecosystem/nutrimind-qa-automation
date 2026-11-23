package expert.nbt.nutrimind.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import expert.nbt.nutrimind.config.TestConfig;
import java.util.regex.Pattern;

@SuppressWarnings("resource")
public final class WaitingForPlayersPage extends BasePage {
    private final Runnable privacy;

    public WaitingForPlayersPage(Page page, TestConfig routes, Runnable privacy) {
        super(page, routes);
        this.privacy = privacy;
    }

    public EditingRoomPage openEditData() {
        editDataButton().locator("xpath=..")
                .evaluate("element => element.click()");
        return ready(new EditingRoomPage(page(), routes(), privacy));
    }

    public Locator editDataButton() {
        return page().getByText(Pattern.compile("^Edit(?: data)?$", Pattern.CASE_INSENSITIVE))
                .filter(new Locator.FilterOptions().setVisible(true));
    }

    @Override public void waitUntilReady() {
        editDataButton().waitFor();
    }
}
