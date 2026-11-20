package expert.nbt.nutrimind.pages;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import expert.nbt.nutrimind.config.TestConfig;
import expert.nbt.nutrimind.config.Credentials;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("resource")
public final class LoginPage extends BasePage {
    private static final Logger log = LoggerFactory.getLogger(LoginPage.class);
    private final Runnable privacy;
    private String language = "UK";

    public LoginPage(Page page, TestConfig routes, Runnable privacy) {
        super(page, routes); this.privacy = privacy;
    }
    private String text(String uk, String ru, String en) {
        return switch (language) { case "RU" -> ru; case "EN" -> en; default -> uk; };
    }
    private Locator email() { return page().getByLabel("E-mail", new Page.GetByLabelOptions().setExact(true)); }
    public Locator password() {
        return page().getByLabel(text("Пароль", "Пароль", "Password"), new Page.GetByLabelOptions().setExact(true));
    }
    public Locator submitButton() { return button(text("Увійти", "Войти", "Enter")); }
    public Locator title() { return page().getByText(Pattern.compile("^Nutri\\s*Mind$")); }
    public Locator subtitle() {
        return page().getByText(text("Трансформаційна гра", "Трансформационная игра", "Transformation game"),
                new Page.GetByTextOptions().setExact(true));
    }
    private Locator recoveryLink() { return link(text("Відновити пароль", "Восстановить пароль", "Recover password")); }
    private Locator registrationLink() { return link(text("Зареєструватися", "Зарегистрироваться", "Register")); }
    public Locator languageSwitcher() {
        return page().getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(Pattern.compile("^(UK|RU|EN)$", Pattern.CASE_INSENSITIVE)));
    }
    public Locator loginError() {
        return page().getByText("User name and password not found. Check again!", new Page.GetByTextOptions().setExact(true))
                .filter(new Locator.FilterOptions().setVisible(true)).first();
    }
    public void open() { log.info("Opening login page"); page().navigate(routes().game("")); }
    public void reload() { page().reload(); }
    @Override public void waitUntilReady() {
        email().waitFor(); password().waitFor(); submitButton().waitFor();
    }
    public void selectLanguage(String language) {
        if (!java.util.Set.of("UK", "RU", "EN").contains(language)) throw new IllegalArgumentException("Unsupported language code");
        log.info("Selecting interface language: {}", language);
        if (!languageSwitcher().innerText().trim().equalsIgnoreCase(language)) {
            languageSwitcher().click();
            page().getByRole(AriaRole.MENUITEM, new Page.GetByRoleOptions()
                    .setName(Pattern.compile("^" + language + "$", Pattern.CASE_INSENSITIVE))).click();
        }
        this.language = language;
        waitUntilReady();
    }
    public void submitCredentials(Credentials credentials) {
        privacy.run();
        email().fill(credentials.username()); password().fill(credentials.password()); submitButton().click();
    }
    public GameSelectionPage login(Credentials credentials) {
        submitCredentials(credentials);
        return ready(new GameSelectionPage(page(), routes(), privacy));
    }
    public RecoveryPage openRecovery() {
        log.info("Opening recovery entry page");
        return ready(new RecoveryPage(page().waitForPopup(() -> recoveryLink().click()), routes()));
    }
    public RegistrationPage openRegistration() {
        log.info("Opening registration page");
        return ready(new RegistrationPage(page().waitForPopup(() -> registrationLink().click()), routes()));
    }
    public void openProtectedRoute() { page().navigate(routes().game("step-one")); }
}
