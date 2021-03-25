/**
 * The view handles outputs and inputs without encoding any logic about the game.
 *
 * @author Luise Woehlke
 */
public final class TextView
{
	public TextView() { }

	public final void displayNewGameMessage() {
		System.out.println("\n---- NEW GAME STARTED ----");
	}

	public final void displayForcedWinningNrMessage() {
		System.out.println("In that case, the number of pieces that have to be in a row for winning will have to be 4 as well.");
	}

	public final void displayConcedeMessage(char player) {
		System.out.println("\n---- PLAYER " + player + " CONCEDED ----");
	}

	public final void displayFullMessage() {
		System.out.println("\n---- BOARD FULL - GAME OVER ----");
	}

	public final void displayWinMessage(char player) {
		System.out.println("---- PLAYER " + player + " WINS! ----");
	}

	public final boolean askMultiplayer() {
		System.out.print("Would you like to play with a friend? (y/n): ");
		return yesNoValidation();
	}

	public final int askNrRows() {
		int nrRows;
		System.out.print("How many rows would you like? (4-9): ");
		while (true) {
			nrRows = InputUtil.readIntFromUser();
			if (4 <= nrRows && 9 >= nrRows) {
				return nrRows;
			}
			System.out.print("Please choose a number from 4 to 9: ");
		}
	}

	public final int askNrCols() {
		int nrCols;
		System.out.print("How many columns would you like? (4-9): ");
		while (true) {
			nrCols = InputUtil.readIntFromUser();
			if (4 <= nrCols && 9 >= nrCols) {
				return nrCols;
			}
			System.out.print("Please choose a number from 4 to 9: ");
		}
	}

	public final int askWinningNr(String reason, int maxNr) {
		switch (reason) {
			case "first ask":
				System.out.print("How many pieces should be in a row to win? (4-" + maxNr + "): ");
				break;
			case "nr invalid":
				System.out.print("Please choose a number between 4 and " + maxNr + ": ");
				break;
		}
		return InputUtil.readIntFromUser();
	}

	public final char askUserSymbol() {
		System.out.print("Which symbol would you like? (x/o): ");
		return xoValidation();
	}

	public final boolean askGoFirst() {
		System.out.print("Would you like to go first? (y/n): ");
		return yesNoValidation();
	}

	public final char askTurnOfPlayer() {
		System.out.print("Who should go first? (x/o): ");
		return xoValidation();
	}

	public final int askForMove(char player, String reason) {
		switch (reason) {
			case "first ask":
				System.out.print("Player " + player + ", select a free column or concede with 0: ");
				break;
			case "col doesn't exist":
				System.out.print("This column doesn't exist. Try again: ");
				break;
			case "col full":
				System.out.print("This column is full. Try again: ");
				break;
		}
		return InputUtil.readIntFromUser();
	}
	
	public final void displayBoard(Model model) {
		int nrCols = model.getNrCols();
		char[][] board = model.getBoard();
		StringBuilder boardStr = new StringBuilder();
		StringBuilder colNrs = buildColNrsStr(nrCols);
		String rowDivider = "-".repeat(nrCols * 2 + 1);

		boardStr.append("\n" + colNrs);
		boardStr.append("\n" + rowDivider + "\n");
		for (char[] row : board) {
			boardStr.append("|");
			for (char field : row) {
				boardStr.append(field + "|");
			}
			boardStr.append("\n" + rowDivider + "\n");
		}
		boardStr.append(colNrs + "\n");

		System.out.println(boardStr);
	}

	private StringBuilder buildColNrsStr(int nrCols) {
		StringBuilder colNrs = new StringBuilder();
		for (int i = 1; i <= nrCols; i++) {
			colNrs.append(" " + i);
		}
		return colNrs;
	}

	public final boolean askToPlayAgain() {
		System.out.print("\nWould you like to play again? (y/n): ");
		return yesNoValidation();
	}

	private boolean yesNoValidation() {
		char input;
		while (true) {
			input = InputUtil.readCharFromUser();
			if (input == 'y' || input == 'Y') {
				return true;
			}
			if (input == 'n' || input == 'N') {
				return false;
			}
			System.out.print("Please type either y or n: ");
		}
	}

	private char xoValidation() {
		char input;
		while (true) {
			input = InputUtil.readCharFromUser();
			if (input == 'x' || input == 'o') {
				return input;
			}
			System.out.print("Please type either x or o: ");
		}
	}

}
