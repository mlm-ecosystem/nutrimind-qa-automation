package expert.nbt.nutrimind.tests;

import com.microsoft.playwright.APIResponse;
import expert.nbt.nutrimind.config.Credentials;
import expert.nbt.nutrimind.pages.ChooseGameTypePage;
import expert.nbt.nutrimind.pages.LoginPage;
import expert.nbt.nutrimind.pages.RecoveryPage;
import expert.nbt.nutrimind.pages.RegistrationPage;
import expert.nbt.nutrimind.pages.CreateNewGameSoloPage;
import expert.nbt.nutrimind.pages.CreateNewGamePlayInTeamPage;
import expert.nbt.nutrimind.pages.EditingRoomPage;
import expert.nbt.nutrimind.pages.WaitingForPlayersPage;
import expert.nbt.nutrimind.pages.ShareTheGameModal;
import expert.nbt.nutrimind.pages.JoinTheGamePage;
import expert.nbt.nutrimind.pages.DashboardPage;
import expert.nbt.nutrimind.pages.PersonalReferralLinkModal;
import expert.nbt.nutrimind.pages.QuestionnairesModal;
import expert.nbt.nutrimind.pages.PlansModal;
import expert.nbt.nutrimind.pages.ProgressModal;
import expert.nbt.nutrimind.pages.GameBoardPage;
import expert.nbt.nutrimind.pages.GoalDescriptionModal;
import expert.nbt.nutrimind.support.UiAssertions;
import io.qameta.allure.*;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.regex.Pattern;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

@SuppressWarnings("resource")
@Epic("NutriMind")
@Feature("Smoke")
@Severity(SeverityLevel.CRITICAL)
public final class SmokeTest extends BaseTest {
    @DataProvider(name = "primaryUser")
    public Object[][] primaryUser() { return new Object[][]{{config.primary()}}; }

    @DataProvider(name = "secondaryUser")
    public Object[][] secondaryUser() { return new Object[][]{{config.secondary()}}; }

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
            UiAssertions.visible(login().title(), "the login title is visible in " + expected[0]);
            UiAssertions.text(login().subtitle(), expected[1], "the subtitle is translated to " + expected[1]);
        }
    }

    @Test(groups = "smoke", dataProvider = "primaryUser", description = "Smoke - 2: Переход по ссылке «Правила игры»")
    public void openGameRules(Credentials user) {
        openLogin("EN");
        ChooseGameTypePage game = login().login(user);
        try {
            APIResponse response = game.openRules();
            try {
                assertEquals(response.status(), 200, "Expected: game rules load without an HTTP error. Actual HTTP status: " + response.status() + ".");
                assertTrue(new String(response.body(), StandardCharsets.US_ASCII).startsWith("%PDF-"), "Expected: game rules are a PDF document. Actual: response is not a PDF.");
            } finally { response.dispose(); }
        } finally { game.closeRules(); }
        game.openLogoutDialog();
        LoginPage signedOut = game.confirmLogout();
        UiAssertions.url(signedOut.page(), config.game(""), "the user returns to the login page after logout");
    }

    @Test(groups = "smoke", dataProvider = "primaryUser", description = "Smoke - 3: Авторизация с валидными учетными данными")
    public void loginWithValidCredentials(Credentials user) {
        openLogin("EN");
        ChooseGameTypePage game = login().login(user);
        UiAssertions.url(game.page(), Pattern.compile("^https?://[^/?#]+/step-one(?:\\?.*)?$"), "successful login opens the game-type selection page");
        UiAssertions.text(game.heading(), "Choose a game type", "the game-type selection heading is displayed");
        UiAssertions.texts(game.gameOptions(), new String[]{"Play solo", "Play in a team"}, "both game-type options are displayed");
    }

    @Test(groups = "smoke", dataProvider = "primaryUser", description = "Smoke - 4: Авторизации с некорректным паролем и валидный email")
    public void rejectInvalidPassword(Credentials user) {
        openLogin("EN");
        login().submitCredentials(new Credentials("invalid-password", user.username(), "invalid-password"));
        UiAssertions.visible(login().loginError(), "an authentication error is displayed for an invalid password");
        UiAssertions.url(login().page(), config.game(""), "the user remains on the login page after an invalid password");
        login().openProtectedRoute();
        UiAssertions.url(login().page(), config.game(""), "a protected page remains unavailable after an invalid password");
    }

    @Test(groups = "smoke", dataProvider = "primaryUser", description = "Smoke - 5: Авторизации с валидным паролем и невалидным email")
    public void rejectInvalidEmail(Credentials user) {
        openLogin("EN");
        login().submitCredentials(new Credentials("invalid-email", "invalid@test.com", user.password()));
        UiAssertions.visible(login().loginError(), "an authentication error is displayed for an invalid email");
        UiAssertions.url(login().page(), config.game(""), "the user remains on the login page after an invalid email");
        login().openProtectedRoute();
        UiAssertions.url(login().page(), config.game(""), "a protected page remains unavailable after an invalid email");
    }

    @Test(groups = "smoke", dataProvider = "primaryUser", description = "Smoke - 6: Выход из авторизованной сессии")
    public void logoutFromAuthenticatedSession(Credentials user) {
        openLogin("UK");
        ChooseGameTypePage game = login().login(user);
        game.openLogoutDialog();
        UiAssertions.containsText(game.logoutDialog(), "Ви впевнені що хочете вийти з системи?", "the logout confirmation is displayed");
        LoginPage signedOut = game.confirmLogout();
        UiAssertions.url(signedOut.page(), config.game(""), "the user returns to the login page after confirming logout");
        signedOut.openProtectedRoute();
        UiAssertions.url(signedOut.page(), config.game(""), "a protected page is unavailable after logout");
    }

    @Test(groups = "smoke", description = "Smoke - 7: Открытие страницы регистрации")
    public void openRegistrationPage() {
        openLogin("UK");
        RegistrationPage registration = login().openRegistration();
        UiAssertions.url(registration.page(), Pattern.compile("^https?://[^/?#]+/uk/signup(?:\\?.*)?$"), "the registration page opens");
        UiAssertions.text(registration.heading(), "Реєстрація", "the registration heading is displayed");
        UiAssertions.count(registration.formControls(), 4, "the registration form has four controls");
    }

    @Test(groups = "smoke", description = "Smoke - 8: Открытие страницы восстановления пароля")
    public void openPasswordRecoveryPage() {
        openLogin("RU");
        RecoveryPage recovery = login().openRecovery();
        recovery.openPasswordReset();
        UiAssertions.url(recovery.page(), Pattern.compile("^https?://[^/?#]+/ru/request-password-reset(?:\\?.*)?$"), "the password-recovery page opens");
        UiAssertions.text(recovery.heading(), "Запросить сброс пароля", "the password-recovery heading is displayed");
        UiAssertions.count(recovery.formControls(), 2, "the password-recovery form has two controls");
    }

    @Test(groups = {"smoke", "google"}, description = "Smoke - 9: Авторизация с помощью Google")
    public void loginWithGoogle() {
        ChooseGameTypePage game = login().openAuthenticatedGame();
        UiAssertions.url(game.page(), Pattern.compile("^https?://[^/?#]+/step-one(?:\\?.*)?$"), "Google login opens the game-type selection page");
        UiAssertions.text(game.heading(), "Choose a game type", "the game-type selection heading is displayed after Google login");
    }

    @Test(groups = "smoke", dataProvider = "primaryUser", description = "Smoke - 10: Выбор типа игры ROLE в одиночной игре")
    public void selectRoleInSoloGame(Credentials user) {
        openLogin("EN");
        CreateNewGameSoloPage soloGame = login().login(user).chooseSoloGame();
        soloGame.selectGameType("ROLE");
        UiAssertions.url(soloGame.page(), Pattern.compile("^https?://[^/?#]+/create-game;view=one-player(?:\\?.*)?$"), "the solo-game creation page opens");
        UiAssertions.text(soloGame.selectedButton("ROLE"), "Selected", "ROLE is selected");
        UiAssertions.enabled(soloGame.createButton(), "Create is enabled after selecting ROLE");
    }

    @Test(groups = "smoke", dataProvider = "primaryUser", description = "Smoke - 11: Переключение между типами QUEST и ROLE в одиночной игре")
    public void switchQuestAndRoleInSoloGame(Credentials user) {
        openLogin("EN");
        CreateNewGameSoloPage soloGame = login().login(user).chooseSoloGame();
        soloGame.selectGameType("QUEST");
        soloGame.selectGameType("ROLE");
        soloGame.selectGameType("QUEST");
        UiAssertions.url(soloGame.page(), Pattern.compile("^https?://[^/?#]+/create-game;view=one-player(?:\\?.*)?$"), "the solo-game creation page remains open");
        UiAssertions.text(soloGame.selectedButton("QUEST"), "Selected", "QUEST is selected after switching back");
        UiAssertions.text(soloGame.selectButton("ROLE"), "Select", "ROLE is not selected after switching back to QUEST");
    }

    @Test(groups = "smoke", dataProvider = "primaryUser", description = "Smoke - 12: Блокирование создания командной игры без названия")
    public void preventCreatingTeamGameWithoutName(Credentials user) {
        openLogin("EN");
        CreateNewGamePlayInTeamPage teamGame = login().login(user).chooseTeamGame().selectOrganizer();
        UiAssertions.url(teamGame.page(), Pattern.compile("^https?://[^/?#]+/create-game;view=many-players(?:\\?.*)?$"), "the team-game creation page opens");
        UiAssertions.attribute(teamGame.gameName(), "required", "", "Game name is required");
        UiAssertions.disabled(teamGame.createButton(), "Create is disabled when Game name is empty");
    }

    @Test(groups = "smoke", dataProvider = "primaryUser", description = "Smoke - 13: Блокирование создания командной игры с названием из пробелов")
    public void preventCreatingTeamGameWithWhitespaceOnlyName(Credentials user) {
        openLogin("EN");
        CreateNewGamePlayInTeamPage teamGame = login().login(user).chooseTeamGame().selectOrganizer();
        teamGame.enterGameName("   ");
        UiAssertions.url(teamGame.page(), Pattern.compile("^https?://[^/?#]+/create-game;view=many-players(?:\\?.*)?$"), "the team-game creation page remains open");
        UiAssertions.disabled(teamGame.createButton(), "Create is disabled when Game name contains only spaces");
    }

    @Test(groups = "smoke", dataProvider = "primaryUser", description = "Smoke - 14: Создание защищённой командной игры")
    public void createProtectedTeamGame(Credentials user) {
        openLogin("EN");
        CreateNewGamePlayInTeamPage teamGame = login().login(user).chooseTeamGame().selectOrganizer();
        teamGame.enterGameName("Smoke14-" + UUID.randomUUID().toString().substring(0, 8));
        teamGame.enterSecurityCode("1234");
        teamGame.selectNumberOfPlayers(2);
        UiAssertions.value(teamGame.securityCode(), "1234", "Security code accepts 1234");
        EditingRoomPage gameRoom = teamGame.createGame();
        UiAssertions.url(gameRoom.page(), Pattern.compile("^https?://[^/?#]+/[^?#]*[0-9][^?#]*(?:\\?.*)?$"), "the created room ID is present in the URL");
    }

    @Test(groups = "smoke", dataProvider = "primaryUser", description = "Smoke - 15: Удаление только что созданной командной игры")
    public void deleteNewTeamGame(Credentials user) {
        openLogin("EN");
        CreateNewGamePlayInTeamPage teamGame = login().login(user).chooseTeamGame().selectOrganizer();
        teamGame.enterGameName("Smoke15-" + UUID.randomUUID().toString().substring(0, 8));
        teamGame.enterSecurityCode("1234");
        teamGame.selectNumberOfPlayers(2);
        UiAssertions.value(teamGame.securityCode(), "1234", "Security code accepts 1234");
        EditingRoomPage gameRoom = teamGame.createGame();
        UiAssertions.url(gameRoom.page(), Pattern.compile("^https?://[^/?#]+/[^?#]*[0-9][^?#]*(?:\\?.*)?$"), "the created room ID is present in the URL");
        gameRoom.cancelGame();
        ChooseGameTypePage gameTypes = gameRoom.confirmCancellation();
        UiAssertions.text(gameTypes.heading(), "Choose a game mode", "the game-mode selection page is displayed after deletion");
    }

    @Test(groups = "smoke", dataProvider = "primaryUser", description = "Smoke - 16: Валидация обязательного поля Player name при редактировании")
    public void requirePlayerNameWhenEditing(Credentials user) {
        openLogin("EN");
        CreateNewGamePlayInTeamPage teamGame = login().login(user).chooseTeamGame().selectOrganizer();
        teamGame.enterGameName("Smoke16-" + UUID.randomUUID().toString().substring(0, 8));
        teamGame.enterSecurityCode("1234");
        teamGame.selectNumberOfPlayers(2);
        EditingRoomPage initialEditingRoom = teamGame.createGame();
        UiAssertions.notBlank(initialEditingRoom.playerName(), "Player name is prefilled after creating the room");
        initialEditingRoom.enterPlayerName("Player One");
        WaitingForPlayersPage gameRoom = initialEditingRoom.saveEdits();
        UiAssertions.visible(gameRoom.editDataButton(), "Save completes and the Edit data control is displayed");
        EditingRoomPage editingRoom = gameRoom.openEditData();
        editingRoom.clearPlayerName();
        UiAssertions.disabled(editingRoom.saveButton(), "Save is disabled when Player name is empty");
        UiAssertions.attributeContains(editingRoom.playerNameField(), "class", "mat-form-field-invalid",
                "Player name is highlighted as invalid");
    }

    @Test(groups = "smoke", dataProvider = "primaryUser", description = "Smoke - 17: Открытие модального окна Поделиться")
    public void openShareTheGameModal(Credentials user) {
        openLogin("EN");
        CreateNewGamePlayInTeamPage teamGame = login().login(user).chooseTeamGame().selectOrganizer();
        teamGame.enterGameName("Smoke17-" + UUID.randomUUID().toString().substring(0, 8));
        teamGame.enterSecurityCode("1234");
        teamGame.selectNumberOfPlayers(2);
        EditingRoomPage editingRoom = teamGame.createGame();
        editingRoom.enterPlayerName("Player One");
        WaitingForPlayersPage gameRoom = editingRoom.saveEdits();
        ShareTheGameModal shareModal = gameRoom.openShareTheGame();
        UiAssertions.text(shareModal.title(), "Share the game", "the Share the game modal title is displayed");
        UiAssertions.visible(shareModal.copyButton(), "the Copy button is displayed in the Share the game modal");
        UiAssertions.visible(shareModal.closeButton(), "the Close button is displayed in the Share the game modal");
    }

    @Test(groups = "smoke", dataProvider = "secondaryUser", description = "Smoke - 18: Закрытие модального окна Поделиться кнопкой Закрыть")
    public void closeShareTheGameModal(Credentials user) {
        openLogin("EN");
        CreateNewGamePlayInTeamPage teamGame = login().login(user).chooseTeamGame().selectOrganizer();
        teamGame.enterGameName("Smoke18-" + UUID.randomUUID().toString().substring(0, 8));
        teamGame.enterSecurityCode("1234");
        teamGame.selectNumberOfPlayers(2);
        EditingRoomPage editingRoom = teamGame.createGame();
        editingRoom.enterPlayerName("Player Two");
        WaitingForPlayersPage gameRoom = editingRoom.saveEdits();
        ShareTheGameModal shareModal = gameRoom.openShareTheGame();
        UiAssertions.visible(shareModal.closeButton(), "the Close button is displayed in the Share the game modal");
        WaitingForPlayersPage waitingForPlayers = shareModal.close();
        UiAssertions.visible(waitingForPlayers.editDataButton(), "the player waiting page is displayed after closing the modal");
    }

    @Test(groups = "smoke", dataProvider = "secondaryUser", description = "Smoke - 19: Вход в защищённую комнату с корректным кодом")
    public void joinProtectedRoomWithCorrectCode(Credentials user) {
        openLogin("EN");
        JoinTheGamePage games = login().login(user).chooseTeamGame().selectGuest();
        UiAssertions.disabled(games.joinButton(), "Join is disabled until the security code is entered");
        games.enterSecurityCode("1234");
        EditingRoomPage room = games.joinRoom();
        UiAssertions.url(room.page(), Pattern.compile("^https?://[^/?#]+/[^?#]*[0-9][^?#]*(?:\\?.*)?$"),
                "the joined room ID is present in the URL");
    }

    @Test(groups = "smoke", dataProvider = "secondaryUser", description = "Smoke - 20: Вход в защищённую комнату с некорректным кодом")
    public void rejectIncorrectProtectedRoomCode(Credentials user) {
        openLogin("EN");
        JoinTheGamePage games = login().login(user).chooseTeamGame().selectGuest();
        UiAssertions.disabled(games.joinButton(), "Join is disabled until the security code is entered");
        games.enterSecurityCode("0000");
        games.joinButton().click();
        UiAssertions.visible(games.incorrectPasswordDialog(), "an incorrect-room-password dialog is displayed");
    }

    @Test(groups = "smoke", dataProvider = "primaryUser", description = "Smoke - 21: Создание одиночной ROLE комнаты")
    public void createSoloRoleRoom(Credentials user) {
        openLogin("EN");
        CreateNewGameSoloPage soloGame = login().login(user).chooseSoloGame();
        soloGame.selectGameType("ROLE");
        EditingRoomPage room = soloGame.createGame();
        UiAssertions.url(room.page(), Pattern.compile("^https?://[^/?#]+/[^?#]*[0-9][^?#]*(?:\\?.*)?$"),
                "the created solo ROLE room ID is present in the URL");
        UiAssertions.text(room.heading(), "Editing a room", "the Editing a room page is displayed for ROLE");
    }

    @Test(groups = "smoke", dataProvider = "primaryUser", description = "Smoke - 22: Создание одиночной QUEST комнаты")
    public void createSoloQuestRoom(Credentials user) {
        openLogin("EN");
        CreateNewGameSoloPage soloGame = login().login(user).chooseSoloGame();
        soloGame.selectGameType("QUEST");
        EditingRoomPage room = soloGame.createGame();
        UiAssertions.url(room.page(), Pattern.compile("^https?://[^/?#]+/[^?#]*[0-9][^?#]*(?:\\?.*)?$"),
                "the created solo QUEST room ID is present in the URL");
        UiAssertions.text(room.heading(), "Editing a room", "the Editing a room page is displayed for QUEST");
    }

    @Test(groups = "smoke", dataProvider = "primaryUser", description = "Smoke - 23: Открытие страницы «Личный кабинет»")
    public void openPersonalAccount(Credentials user) {
        openLogin("EN");
        ChooseGameTypePage game = login().login(user);
        game.openNavigationMenu();
        UiAssertions.visible(game.dashboardMenuItem(), "the Dashboard item is displayed in the navigation menu");
        DashboardPage account = game.openDashboard();
        UiAssertions.text(account.heading(), "Dashboard", "the Dashboard page heading is displayed");
    }

    @Test(groups = "smoke", dataProvider = "primaryUser", description = "Smoke - 24: Открытие модального окна «Личная реферальная ссылка»")
    public void openPersonalReferralLinkModal(Credentials user) {
        openLogin("EN");
        ChooseGameTypePage game = login().login(user);
        game.openNavigationMenu();
        PersonalReferralLinkModal referralLink = game.openPersonalReferralLink();
        UiAssertions.text(referralLink.title(), "Personal referral link", "the Personal referral link modal title is displayed");
        UiAssertions.visible(referralLink.internalLink(), "the Internal link field is displayed");
        UiAssertions.visible(referralLink.closeButton(), "the Close button is displayed in the modal");
    }

    @Test(groups = "smoke", dataProvider = "primaryUser", description = "Smoke - 25: Закрытие модального окна «Личная реферальная ссылка»")
    public void closePersonalReferralLinkModal(Credentials user) {
        openLogin("EN");
        ChooseGameTypePage game = login().login(user);
        game.openNavigationMenu();
        PersonalReferralLinkModal referralLink = game.openPersonalReferralLink();
        ChooseGameTypePage gameTypes = referralLink.close();
        UiAssertions.text(gameTypes.heading(), "Choose a game type", "the game-type selection page remains displayed after closing the modal");
    }

    @Test(groups = "smoke", dataProvider = "primaryUser", description = "Smoke - 26: Открытие модального окна «Опросники»")
    public void openQuestionnairesModal(Credentials user) {
        openLogin("EN");
        ChooseGameTypePage game = login().login(user);
        game.openNavigationMenu();
        QuestionnairesModal questionnaires = game.openQuestionnaires();
        UiAssertions.text(questionnaires.title(), "List of Questionnaires", "the List of Questionnaires modal title is displayed");
        UiAssertions.visible(questionnaires.copyButton(), "a questionnaire copy button is displayed");
        UiAssertions.visible(questionnaires.closeButton(), "the Close button is displayed in the modal");
    }

    @Test(groups = "smoke", dataProvider = "primaryUser", description = "Smoke - 27: Закрытие модального окна «Опросники»")
    public void closeQuestionnairesModal(Credentials user) {
        openLogin("EN");
        ChooseGameTypePage game = login().login(user);
        game.openNavigationMenu();
        QuestionnairesModal questionnaires = game.openQuestionnaires();
        ChooseGameTypePage gameTypes = questionnaires.close();
        UiAssertions.text(gameTypes.heading(), "Choose a game type", "the game-type selection page remains displayed after closing the modal");
    }

    @Test(groups = "smoke", dataProvider = "primaryUser", description = "Smoke - 28: Открытие модального окна «Тарифы»")
    public void openPlansModal(Credentials user) {
        openLogin("EN");
        ChooseGameTypePage game = login().login(user);
        game.openNavigationMenu();
        PlansModal plans = game.openPlans();
        UiAssertions.text(plans.title(), "Plans", "the Plans modal title is displayed");
        UiAssertions.count(plans.cards(), 4, "four plan cards are displayed");
        UiAssertions.count(plans.chooseButtons(), 4, "each plan card has a Choose button");
    }

    @Test(groups = "smoke", dataProvider = "primaryUser", description = "Smoke - 29: Закрытие модального окна «Тарифы»")
    public void closePlansModal(Credentials user) {
        openLogin("EN");
        ChooseGameTypePage game = login().login(user);
        game.openNavigationMenu();
        PlansModal plans = game.openPlans();
        ChooseGameTypePage gameTypes = plans.close();
        UiAssertions.text(gameTypes.heading(), "Choose a game type", "the game-type selection page remains displayed after closing the modal");
    }

    @Test(groups = "smoke", dataProvider = "primaryUser", description = "Smoke - 30: Открытие модального окна «Успеваемость»")
    public void openProgressModal(Credentials user) {
        openLogin("EN");
        ChooseGameTypePage game = login().login(user);
        game.openNavigationMenu();
        ProgressModal progress = game.openProgress();
        UiAssertions.text(progress.title(), "Progress", "the Progress modal title is displayed");
        UiAssertions.visible(progress.idColumn(), "the ID progress column is displayed");
        UiAssertions.visible(progress.nameColumn(), "the Name progress column is displayed");
    }

    @Test(groups = "smoke", dataProvider = "primaryUser", description = "Smoke - 31: Закрытие модального окна «Успеваемость»")
    public void closeProgressModal(Credentials user) {
        openLogin("EN");
        ChooseGameTypePage game = login().login(user);
        game.openNavigationMenu();
        ProgressModal progress = game.openProgress();
        ChooseGameTypePage gameTypes = progress.continueToGameTypes();
        UiAssertions.text(gameTypes.heading(), "Choose a game type", "the game-type selection page remains displayed after continuing");
    }

    @Test(groups = "smoke", dataProvider = "primaryUser", description = "Smoke - 32: Отображение ожидания хода в одиночной игре")
    public void showWaitingForTurnInSoloGame(Credentials user) {
        openLogin("EN");
        CreateNewGameSoloPage soloGame = login().login(user).chooseSoloGame();
        soloGame.selectGameType("QUEST");
        EditingRoomPage editingRoom = soloGame.createGame();
        editingRoom.selectQuest("Stress and adaptation");
        editingRoom.enterPlayerName("Player One");
        WaitingForPlayersPage preparingForGame = editingRoom.saveEdits();
        GameBoardPage gameBoard = preparingForGame.startTheGame();
        UiAssertions.visible(gameBoard.waitingForTurn(), "the Waiting for your turn block is displayed");
        UiAssertions.enabled(gameBoard.makeAMoveButton(), "Make a move is enabled for the current player");
    }

    @Test(groups = "smoke", dataProvider = "primaryUser", description = "Smoke - 33: Открытие модального окна Описание цели на экране игры")
    public void openGoalDescriptionModal(Credentials user) {
        openLogin("EN");
        CreateNewGameSoloPage soloGame = login().login(user).chooseSoloGame();
        soloGame.selectGameType("QUEST");
        EditingRoomPage editingRoom = soloGame.createGame();
        editingRoom.selectQuest("Stress and adaptation");
        editingRoom.enterPlayerName("Player One");
        WaitingForPlayersPage preparingForGame = editingRoom.saveEdits();
        GameBoardPage gameBoard = preparingForGame.startTheGame();
        GoalDescriptionModal goalDescription = gameBoard.openGoalDescription();
        UiAssertions.text(goalDescription.title(), "Goal description", "the Goal description modal title is displayed");
        UiAssertions.visible(goalDescription.description(), "the goal description text is displayed");
        UiAssertions.visible(goalDescription.gotItButton(), "the Got it button is displayed");
    }

    @Test(groups = "smoke", dataProvider = "secondaryUser", description = "Smoke - 34: Закрытие модального окна Описание цели на экране игры")
    public void closeGoalDescriptionModal(Credentials user) {
        openLogin("EN");
        CreateNewGameSoloPage soloGame = login().login(user).chooseSoloGame();
        soloGame.selectGameType("QUEST");
        EditingRoomPage editingRoom = soloGame.createGame();
        editingRoom.selectQuest("Stress and adaptation");
        editingRoom.enterPlayerName("Player Two");
        WaitingForPlayersPage preparingForGame = editingRoom.saveEdits();
        GameBoardPage gameBoard = preparingForGame.startTheGame();
        GoalDescriptionModal goalDescription = gameBoard.openGoalDescription();
        GameBoardPage gameAfterClosingModal = goalDescription.close();
        UiAssertions.visible(gameAfterClosingModal.waitingForTurn(), "the game board is displayed after closing the modal");
        UiAssertions.enabled(gameAfterClosingModal.makeAMoveButton(), "Make a move remains enabled after closing the modal");
    }
}
