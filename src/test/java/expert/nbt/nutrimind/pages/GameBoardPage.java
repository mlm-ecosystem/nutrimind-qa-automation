package expert.nbt.nutrimind.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import expert.nbt.nutrimind.config.TestConfig;

@SuppressWarnings("resource")
public final class GameBoardPage extends BasePage {
    public GameBoardPage(Page page, TestConfig routes) {
        super(page, routes);
    }

    public Locator waitingForTurn() {
        return page().getByText("Waiting for your turn", new Page.GetByTextOptions().setExact(true));
    }

    public Locator makeAMoveButton() {
        return page().getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Make a move").setExact(true));
    }

    public Locator goalDetailsLink() {
        return page().getByText("More", new Page.GetByTextOptions().setExact(true))
                .filter(new Locator.FilterOptions().setVisible(true));
    }

    public GoalDescriptionModal openGoalDescription() {
        goalDetailsLink().click();
        return ready(new GoalDescriptionModal(page(), routes()));
    }

    @Override protected void waitForPageContent() {
        waitingForTurn().waitFor();
        makeAMoveButton().waitFor();
    }
}
