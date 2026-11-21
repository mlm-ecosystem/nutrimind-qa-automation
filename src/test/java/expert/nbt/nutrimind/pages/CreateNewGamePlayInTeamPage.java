package expert.nbt.nutrimind.pages;

import com.microsoft.playwright.Page;
import expert.nbt.nutrimind.config.TestConfig;
import java.util.regex.Pattern;

@SuppressWarnings("resource")
public final class CreateNewGamePlayInTeamPage extends BasePage {
    public CreateNewGamePlayInTeamPage(Page page, TestConfig routes) {
        super(page, routes);
    }
    @Override public void waitUntilReady() {
        page().getByText(Pattern.compile("^Create a new game$", Pattern.CASE_INSENSITIVE)).waitFor();
    }
}
