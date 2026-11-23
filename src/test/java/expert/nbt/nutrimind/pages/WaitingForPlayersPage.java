package expert.nbt.nutrimind.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;
import expert.nbt.nutrimind.config.TestConfig;
import java.util.regex.Pattern;

@SuppressWarnings("resource")
public final class WaitingForPlayersPage extends BasePage {
    private final Runnable privacy;

    public WaitingForPlayersPage(Page page, TestConfig routes, Runnable privacy) {
        super(page, routes);
        this.privacy = privacy;
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
        page().getByText(Pattern.compile("^Cancel$", Pattern.CASE_INSENSITIVE))
                .filter(new Locator.FilterOptions().setVisible(true)).waitFor();
    }
}
