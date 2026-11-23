package expert.nbt.nutrimind.pages;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.*;
import expert.nbt.nutrimind.config.TestConfig;

// Pages are borrowed from BrowserSession; closing the context releases all tabs.
@SuppressWarnings("resource")
public final class RegistrationPage extends BasePage {
    public RegistrationPage(Page page, TestConfig routes) { super(page, routes); }
    private Locator email() { return page().getByPlaceholder("E-mail", new Page.GetByPlaceholderOptions().setExact(true)); }
    private Locator submitButton() { return button("Реєстрація"); }
    public Locator heading() {
        return page().getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Реєстрація").setExact(true));
    }
    public Locator formControls() {
        Locator password = page().getByPlaceholder("Пароль", new Page.GetByPlaceholderOptions().setExact(true));
        return email().or(password).or(submitButton()).or(link("Google"))
                .filter(new Locator.FilterOptions().setVisible(true));
    }
    @Override protected void waitForPageContent() {
        email().waitFor(); submitButton().waitFor();
    }
}
