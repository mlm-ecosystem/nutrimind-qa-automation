package expert.nbt.nutrimind.pages;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import expert.nbt.nutrimind.config.TestConfig;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.regex.Pattern;

@SuppressWarnings("resource")
public final class GameSelectionPage extends BasePage {
    private final Runnable privacy;
    private String language;
    private Page rulesPage;
    private Download rulesDownload;
    private String rulesOrigin;

    public GameSelectionPage(Page page, TestConfig routes, Runnable privacy) {
        super(page, routes); this.privacy = privacy;
    }
    public Locator heading() {
        return page().getByText(Pattern.compile("^(Оберіть тип гри|Choose a game type)$"));
    }
    // Responsive markup duplicates some labels; only visible options belong to the form.
    public Locator gameOptions() {
        return page().getByText(Pattern.compile("^Play (solo|in a team)$", Pattern.CASE_INSENSITIVE))
                .filter(new Locator.FilterOptions().setVisible(true));
    }
    public Locator rulesLink() { return link("Rules of the game"); }
    public Locator logoutDialog() { return page().getByRole(AriaRole.DIALOG); }
    @Override public void waitUntilReady() { heading().waitFor(); }

    public APIResponse openRules() {
        rulesOrigin = page().url();
        rulesPage = page();
        rulesDownload = null;
        Consumer<Download> onDownload = downloaded -> rulesDownload = downloaded;
        Consumer<Page> onPopup = opened -> {
            rulesPage = opened;
            opened.onDownload(onDownload);
        };
        page().onDownload(onDownload);
        page().onPopup(onPopup);
        try {
            rulesLink().click();
            // Playwright events run on the owning thread; no state is shared between tests.
            page().waitForCondition(() -> rulesDownload != null
                    || (rulesPage != page() && !rulesPage.url().equals("about:blank"))
                    || !page().url().equals(rulesOrigin));
            String contentUrl;
            if (rulesDownload != null) {
                rulesDownload.path();
                contentUrl = rulesDownload.url();
            }
            else {
                rulesPage.waitForLoadState();
                contentUrl = rulesPage.url();
            }
            return page().request().get(contentUrl);
        } finally {
            page().offDownload(onDownload);
            page().offPopup(onPopup);
            if (rulesPage != page() && !rulesPage.isClosed()) rulesPage.offDownload(onDownload);
        }
    }
    public void closeRules() {
        if (rulesPage != null && rulesPage != page() && !rulesPage.isClosed()) rulesPage.close();
        if (rulesOrigin != null && !page().url().equals(rulesOrigin)) page().goBack();
        waitUntilReady();
    }
    public void openLogoutDialog() {
        language = page().getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("^(UK|EN)$", Pattern.CASE_INSENSITIVE)))
                .innerText().trim().toUpperCase(Locale.ROOT);
        // The icon-only menu has no useful accessible name; scope its role by visible icon text.
        page().getByRole(AriaRole.BUTTON).filter(new Locator.FilterOptions()
                .setHas(page().getByText("menu", new Page.GetByTextOptions().setExact(true)))).click();
        page().getByRole(AriaRole.MENUITEM, new Page.GetByRoleOptions()
                .setName(language.equals("EN") ? "Log out" : "Вийти із системи").setExact(true)).click();
        logoutDialog().waitFor();
    }
    public LoginPage confirmLogout() {
        logoutDialog().getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions()
                .setName(language.equals("EN") ? "Yes" : "Так").setExact(true)).click();
        LoginPage signedOut = new LoginPage(page(), routes(), privacy);
        signedOut.selectLanguage(language);
        return signedOut;
    }
}
