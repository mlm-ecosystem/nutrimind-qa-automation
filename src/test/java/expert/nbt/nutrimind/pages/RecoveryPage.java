package expert.nbt.nutrimind.pages;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.*;
import expert.nbt.nutrimind.config.TestConfig;

// Pages are borrowed from BrowserSession; closing the context releases all tabs.
@SuppressWarnings("resource")
public final class RecoveryPage extends BasePage {
    public RecoveryPage(Page page, TestConfig routes) { super(page, routes); }
    private Locator resetLink() { return link("сбросить его."); }
    private Locator email() { return page().getByPlaceholder("Email", new Page.GetByPlaceholderOptions().setExact(true)); }
    public Locator heading() {
        return page().getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Запросить сброс пароля").setExact(true));
    }
    public Locator formControls() {
        return email().or(button("Отправить")).filter(new Locator.FilterOptions().setVisible(true));
    }
    @Override protected void waitForPageContent() {
        resetLink().waitFor();
    }
    public void openPasswordReset() {
        resetLink().click();
        waitForPageLoad();
        email().waitFor();
    }
}
