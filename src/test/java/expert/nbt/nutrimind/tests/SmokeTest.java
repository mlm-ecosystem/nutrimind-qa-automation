package expert.nbt.nutrimind.tests;

import com.microsoft.playwright.APIResponse;
import expert.nbt.nutrimind.config.Credentials;
import expert.nbt.nutrimind.pages.ChooseGameTypePage;
import expert.nbt.nutrimind.pages.LoginPage;
import expert.nbt.nutrimind.pages.RecoveryPage;
import expert.nbt.nutrimind.pages.RegistrationPage;
import expert.nbt.nutrimind.pages.CreateNewGameSoloPage;
import expert.nbt.nutrimind.pages.CreateNewGamePlayInTeamPage;
import io.qameta.allure.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.testng.Assert.*;

@SuppressWarnings("resource")
@Epic("NutriMind")
@Feature("Smoke")
@Severity(SeverityLevel.CRITICAL)
public final class SmokeTest extends BaseTest {
    @DataProvider(name = "primaryUser")
    public Object[][] primaryUser() { return new Object[][]{{config.primary()}}; }

    @Test(groups = "smoke", description = "Smoke - 1: Смена языка интерфейса")
    public void changeInterfaceLanguage() {
        String[][] translations = {
                {"RU", "Трансформационная игра"},
                {"UK", "Трансформаційна гра"},
                {"EN", "Transformation game"}
        };
        openLogin("UK");
        for (String[] expected : translations) {
            login().selectLanguage(expected[0]);
            assertThat(login().title()).isVisible();
            assertThat(login().subtitle()).hasText(expected[1]);
        }
    }

    @Test(groups = "smoke", dataProvider = "primaryUser", description = "Smoke - 2: Переход по ссылке «Правила игры»")
    public void openGameRules(Credentials user) {
        openLogin("EN");
        ChooseGameTypePage game = login().login(user);
        try {
            APIResponse response = game.openRules();
            try {
                assertEquals(response.status(), 200, "Game rules must load without an HTTP error");
                assertTrue(new String(response.body(), StandardCharsets.US_ASCII).startsWith("%PDF-"), "Game rules must be a PDF document");
            } finally { response.dispose(); }
        } finally { game.closeRules(); }
        game.openLogoutDialog();
        LoginPage signedOut = game.confirmLogout();
        assertThat(signedOut.page()).hasURL(config.game(""));
    }

    @Test(groups = "smoke", dataProvider = "primaryUser", description = "Smoke - 3: Авторизация с валидными учетными данными")
    public void loginWithValidCredentials(Credentials user) {
        openLogin("EN");
        ChooseGameTypePage game = login().login(user);
        assertThat(game.page()).hasURL(Pattern.compile("^https?://[^/?#]+/step-one(?:\\?.*)?$"));
        assertThat(game.heading()).hasText("Choose a game type");
        assertThat(game.gameOptions()).hasText(new String[]{"Play solo", "Play in a team"});
    }

    @Test(groups = "smoke", dataProvider = "primaryUser", description = "Smoke - 4: Авторизации с некорректным паролем и валидный email")
    public void rejectInvalidPassword(Credentials user) {
        openLogin("EN");
        login().submitCredentials(new Credentials("invalid-password", user.username(), "invalid-password"));
        assertThat(login().loginError()).isVisible();
        assertThat(login().page()).hasURL(config.game(""));
        login().openProtectedRoute();
        assertThat(login().page()).hasURL(config.game(""));
    }

    @Test(groups = "smoke", dataProvider = "primaryUser", description = "Smoke - 5: Авторизации с валидным паролем и невалидным email")
    public void rejectInvalidEmail(Credentials user) {
        openLogin("EN");
        login().submitCredentials(new Credentials("invalid-email", "invalid@test.com", user.password()));
        assertThat(login().loginError()).isVisible();
        assertThat(login().page()).hasURL(config.game(""));
        login().openProtectedRoute();
        assertThat(login().page()).hasURL(config.game(""));
    }

    @Test(groups = "smoke", dataProvider = "primaryUser", description = "Smoke - 6: Выход из авторизованной сессии")
    public void logoutFromAuthenticatedSession(Credentials user) {
        openLogin("UK");
        ChooseGameTypePage game = login().login(user);
        game.openLogoutDialog();
        assertThat(game.logoutDialog()).containsText("Ви впевнені що хочете вийти з системи?");
        LoginPage signedOut = game.confirmLogout();
        assertThat(signedOut.page()).hasURL(config.game(""));
        signedOut.openProtectedRoute();
        assertThat(signedOut.page()).hasURL(config.game(""));
    }

    @Test(groups = "smoke", description = "Smoke - 7: Открытие страницы регистрации")
    public void openRegistrationPage() {
        openLogin("UK");
        RegistrationPage registration = login().openRegistration();
        assertThat(registration.page()).hasURL(Pattern.compile("^https?://[^/?#]+/uk/signup(?:\\?.*)?$"));
        assertThat(registration.heading()).hasText("Реєстрація");
        assertThat(registration.formControls()).hasCount(4);
    }

    @Test(groups = "smoke", description = "Smoke - 8: Открытие страницы восстановления пароля")
    public void openPasswordRecoveryPage() {
        openLogin("RU");
        RecoveryPage recovery = login().openRecovery();
        recovery.openPasswordReset();
        assertThat(recovery.page()).hasURL(Pattern.compile("^https?://[^/?#]+/ru/request-password-reset(?:\\?.*)?$"));
        assertThat(recovery.heading()).hasText("Запросить сброс пароля");
        assertThat(recovery.formControls()).hasCount(2);
    }

    @Test(groups = {"smoke", "google"}, description = "Smoke - 9: Авторизация с помощью Google")
    public void loginWithGoogle() {
        ChooseGameTypePage game = login().openAuthenticatedGame();
        assertThat(game.page()).hasURL(Pattern.compile("^https?://[^/?#]+/step-one(?:\\?.*)?$"));
        assertThat(game.heading()).hasText("Choose a game type");
    }

    @Test(groups = "smoke", dataProvider = "primaryUser", description = "Smoke - 10: Выбор типа игры ROLE в одиночной игре")
    public void selectRoleInSoloGame(Credentials user) {
        openLogin("EN");
        CreateNewGameSoloPage soloGame = login().login(user).chooseSoloGame();
        soloGame.selectGameType("ROLE");
        assertThat(soloGame.page()).hasURL(Pattern.compile("^https?://[^/?#]+/create-game;view=one-player(?:\\?.*)?$"));
        assertThat(soloGame.selectedButton("ROLE")).hasText("Selected");
        assertThat(soloGame.createButton()).isEnabled();
    }

    @Test(groups = "smoke", dataProvider = "primaryUser", description = "Smoke - 11: Переключение между типами QUEST и ROLE в одиночной игре")
    public void switchQuestAndRoleInSoloGame(Credentials user) {
        openLogin("EN");
        CreateNewGameSoloPage soloGame = login().login(user).chooseSoloGame();
        soloGame.selectGameType("QUEST");
        soloGame.selectGameType("ROLE");
        soloGame.selectGameType("QUEST");
        assertThat(soloGame.page()).hasURL(Pattern.compile("^https?://[^/?#]+/create-game;view=one-player(?:\\?.*)?$"));
        assertThat(soloGame.selectedButton("QUEST")).hasText("Selected");
        assertThat(soloGame.selectButton("ROLE")).hasText("Select");
    }

    @Test(groups = "smoke", dataProvider = "primaryUser", description = "Smoke - 12: Блокирование создания командной игры без названия")
    public void preventCreatingTeamGameWithoutName(Credentials user) {
        openLogin("EN");
        CreateNewGamePlayInTeamPage teamGame = login().login(user).chooseTeamGame().selectOrganizer();
        assertThat(teamGame.page()).hasURL(Pattern.compile("^https?://[^/?#]+/create-game;view=many-players(?:\\?.*)?$"));
        assertThat(teamGame.gameName()).hasAttribute("required", "");
        assertThat(teamGame.createButton()).isDisabled();
    }
}
