/**
 * The controller is the link between the Model and the view.
 * It also specifies the sequence of events.
 *
 * @author Luise Woehlke
 */
public final class Controller {

	private final Model model;
	private final TextView view;
	private final Bot bot;
	
	public Controller(Model model, TextView view, Bot bot) {
		this.model = model;
		this.view = view;
		this.bot = bot;
	}

	/**
	 * loops through multiple games
	 */
	public void startSession() {
		do {
			changeSettings();
			view.displayNewGameMessage();
			startGame();
		} while (view.askToPlayAgain());
	}

	/**
	 * loops through player turns
	 */
	private void startGame() {
		int move;
		boolean gameEnded;

		do {
			view.displayBoard(model);
			move = getMove();
			gameEnded = endGameIfNecessary(move);
			if (!gameEnded) {
				model.makeMove(move - 1);
				if (bot.isUsed()) {
					bot.runSimulations(move - 1);
				}
				model.switchPlayer();
			}
		} while (!gameEnded);
	}

	private int getMove() {
		if (bot.isUsed() && model.getTurnOfPlayer() == bot.getSymbol()) {
			return bot.calcMove();
		}

		return userMoveValidation();
	}

	/**
	 * before every game, this lets the user change the nr of rows,
	 * cols, their symbol, whether to play against the computer
	 * (and if so, if they go first), and how many pieces have to be in a row to win ("winningNr")
	 */
	private void changeSettings() {
		bot.setUsed(!view.askMultiplayer());
		model.resizeBoard(view.askNrRows(), view.askNrCols());
		model.setWinningNr(winningNrValidation());

		if (bot.isUsed()) {
			char botSymbol = model.getOppositeSymbol(view.askUserSymbol());
			if (view.askGoFirst()) {
				model.setTurnOfPlayer(model.getOppositeSymbol(botSymbol));
			} else {
				model.setTurnOfPlayer(botSymbol);
			}
			bot.initialise(model, botSymbol);
		} else {
			model.setTurnOfPlayer(view.askTurnOfPlayer());
		}
	}

	/**
	 * checks if player conceded, won, or the board is full
	 *
	 * @param move the player's last move
	 * @return whether game is over
	 */
	private boolean endGameIfNecessary(int move) {
		if (move == 0) {
			view.displayConcedeMessage(model.getTurnOfPlayer());
			return true;
		}

		if (model.isWin(move - 1, model.getTurnOfPlayer())) {
			model.makeMove(move - 1);
			view.displayBoard(model);
			view.displayWinMessage(model.getTurnOfPlayer());
			return true;
		}

		if (model.isFull(move)) {
			model.makeMove(move - 1);
			view.displayBoard(model);
			view.displayFullMessage();
			return true;
		}

		return false;
	}

	/**
	 * gets a winningNr >=4 from the user where it is possible to win
	 */
	private int winningNrValidation() {
		int winningNr;
		if (model.isForcedWinningNr()) {
			view.displayForcedWinningNrMessage();
			winningNr = 4;
		} else {
			winningNr = view.askWinningNr("first ask", model.getMaxWinningNr());
			while (!model.isValidWinningNr(winningNr)) {
				winningNr = view.askWinningNr("nr invalid", model.getMaxWinningNr());
			}
		}
		return winningNr;
	}

	private int userMoveValidation() {
		int move;
		String reason = "first ask";

		do {
			move = view.askForMove(model.getTurnOfPlayer(), reason);
			if (move == 0) {
				return 0;
			}
			if (!model.isCol(move)) {
				reason = "col doesn't exist";
			} else {
				if (model.isColFull(move - 1)) {
					reason = "col full";
				} else {
					return move;
				}
			}
		} while (true);
	}

}
