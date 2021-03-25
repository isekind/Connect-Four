/**
 * The model is providing the game's rules & status.
 * It has no information about its view or players.
 *
 * @author Luise Woehlke
 */
public class Model {

	// ===========================================================================
	// ================================ CONSTANTS ================================
	// ===========================================================================
	// The most common version of Connect Four has 6 rows and 7 columns.
	public static final int DEFAULT_NR_ROWS = 6;
	public static final int DEFAULT_NR_COLS = 7;
	public static final int DEFAULT_WINNING_NR = 4;
	public static final char DEFAULT_PLAYER = 'x';
	
	// ========================================================================
	// ================================ FIELDS ================================
	// ========================================================================
	protected int nrRows;
	protected int nrCols;
	protected int winningNr;
	protected char turnOfPlayer;
	protected char[][] board;
	
	// =============================================================================
	// ================================ CONSTRUCTOR ================================
	// =============================================================================
	public Model()
	{
		nrRows = DEFAULT_NR_ROWS;
		nrCols = DEFAULT_NR_COLS;
		winningNr = DEFAULT_WINNING_NR;
		turnOfPlayer = DEFAULT_PLAYER;
		board = new char[DEFAULT_NR_ROWS][];
		for (int i = 0; i < DEFAULT_NR_ROWS; i++) {
			board[i] = new char[DEFAULT_NR_COLS];
			for (int j = 0; j < DEFAULT_NR_COLS; j++) {
				board[i][j] = ' ';
			}
		}
	}
	
	// ====================================================================================
	// ================================ MODEL INTERACTIONS ================================
	// ====================================================================================

	/**
	 * Checks whether the game would be won after the given move by
	 * checking whether it completes a horizontal, vertical, ascending diagonal,
	 * or descending diagonal row of the required length
	 *
	 * @param moveCol column of given move
	 * @param player who makes the move
	 * @return whether the player wins with the given move
	 */
	public boolean isWin(int moveCol, char player) {
		int moveRow = calcMoveRow(moveCol);
		return isWinHorizontal(moveRow, moveCol, player)
				|| isWinVertical(moveRow, moveCol, player)
				|| isWinAscending(moveRow, moveCol, player)
				|| isWinDescending(moveRow, moveCol, player);
	}

	/**
	 * Checks whether the given move completes a horizontal winning row by
	 * counting the pieces the player already has to the right and left of the new piece
	 * and returning true if it's the required nr for a win
	 *
	 * @param moveRow row of given move
	 * @param moveCol column of given move
	 * @param player who makes the move
	 * @return whether the given move completes a horizontal winning row
	 */

	protected boolean isWinHorizontal(int moveRow, int moveCol, char player) {
		int piecesRight = 0;
		while (piecesRight + 1 < winningNr
				&& moveCol + piecesRight + 1 <= nrCols - 1
				&& board[moveRow][moveCol + piecesRight + 1] == player) {
			piecesRight++;
		}
		if (piecesRight + 1 == winningNr) {
			return true;
		}

		int piecesLeft = 0;
		while (piecesLeft + 1 < winningNr
				&& moveCol - piecesLeft - 1 >= 0
				&& board[moveRow][moveCol - piecesLeft - 1] == player) {
			piecesLeft++;
			if (piecesRight + piecesLeft + 1 == winningNr) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks whether the given move completes a vertical winning row by
	 * counting the pieces the player already has above and below the new piece
	 * and returning true if it's the required nr for a win
	 *
	 * @param moveRow row of given move
	 * @param moveCol column of given move
	 * @param player who makes the move
	 * @return whether the given move completes a vertical winning row
	 */
	protected boolean isWinVertical(int moveRow, int moveCol, char player) {
		int piecesUp = 0;
		while (piecesUp + 1 < winningNr
				&& moveRow + piecesUp + 1 <= nrRows - 1
				&& board[moveRow + piecesUp + 1][moveCol] == player) {
			piecesUp++;
		}
		if (piecesUp + 1 == winningNr) {
			return true;
		}

		int piecesDown = 0;
		while (piecesDown + 1 < winningNr
				&& moveRow - piecesDown - 1 >= 0
				&& board[moveRow - piecesDown - 1][moveCol] == player) {
			piecesDown++;
			if (piecesUp + piecesDown + 1 == winningNr) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks whether the given move completes an ascending diagonal winning row by
	 * counting the pieces the player already has to the upper right and lower left of the new piece
	 * and returning true if it's the required nr for a win
	 *
	 * @param moveRow row of given move
	 * @param moveCol column of given move
	 * @param player who makes the move
	 * @return whether the given move completes an ascending diagonal winning row
	 */
	protected boolean isWinAscending(int moveRow, int moveCol, char player) {
		int piecesUpRight = 0;
		while (piecesUpRight + 1 < winningNr
				&& moveRow + piecesUpRight + 1 <= nrRows - 1
				&& moveCol + piecesUpRight + 1 <= nrCols - 1
				&& board[moveRow + piecesUpRight + 1][moveCol + piecesUpRight + 1] == player) {
			piecesUpRight++;
		}
		if (piecesUpRight + 1 == winningNr) {
			return true;
		}

		int piecesDownLeft = 0;
		while (piecesDownLeft + 1 < winningNr
				&& moveRow - piecesDownLeft - 1 >= 0
				&& moveCol - piecesDownLeft - 1 >= 0
				&& board[moveRow - piecesDownLeft - 1][moveCol - piecesDownLeft - 1] == player) {
			piecesDownLeft++;
			if (piecesUpRight + piecesDownLeft + 1 == winningNr) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks whether the given move completes a descending diagonal winning row by
	 * counting the pieces the player already has to the upper left and lower right of the new piece
	 * and returning true if it's the required nr for a win
	 *
	 * @param moveRow row of given move
	 * @param moveCol column of given move
	 * @param player who makes the move
	 * @return whether the given move completes a descending diagonal winning row
	 */
	protected boolean isWinDescending(int moveRow, int moveCol, char player) {
		int piecesUpLeft = 0;
		while (piecesUpLeft + 1 < winningNr
				&& moveRow + piecesUpLeft + 1 <= nrRows - 1
				&& moveCol - piecesUpLeft - 1 >= 0
				&& board[moveRow + piecesUpLeft + 1][moveCol - piecesUpLeft - 1] == player) {
			piecesUpLeft++;
		}
		if (piecesUpLeft + 1 == winningNr) {
			return true;
		}

		int piecesDownRight = 0;
		while (piecesDownRight + 1 < winningNr
				&& moveRow - piecesDownRight - 1 >= 0
				&& moveCol + piecesDownRight + 1 <= nrCols - 1
				&& board[moveRow - piecesDownRight - 1][moveCol + piecesDownRight + 1] == player) {
			piecesDownRight++;
			if (piecesUpLeft + piecesDownRight + 1 == winningNr) {
				return true;
			}
		}

		return false;
	}

	public void makeMove(int col) {
		board[calcMoveRow(col)][col] = turnOfPlayer;
	}

	public void switchPlayer() {
		turnOfPlayer = getOppositeSymbol(turnOfPlayer);
	}

	public void resizeBoard(int nrRows, int nrCols) {
		this.nrRows = nrRows;
		this.nrCols = nrCols;
		board = new char[nrRows][];
		for (int i = 0; i < nrRows; i++) {
			board[i] = new char[nrCols];
			for (int j = 0; j < nrCols; j++) {
				board[i][j] = ' ';
			}
		}
	}

	public void setWinningNr(int winningNr) {
		this.winningNr = winningNr;
	}

	public void setTurnOfPlayer(char player) {
		turnOfPlayer = player;
	}

	/**
	 * calculates in which row a move in the given column would go by finding the
	 * lowest row that has an empty field in that column
	 */
	protected int calcMoveRow(int col) {
		int row = nrRows - 1;
		while (true) {
			if (board[row][col] == ' ') {
				return row;
			}
			row--;
		}
	}

	/**
	 * checks whether the winningNr has to be 4 due to the board dimensions
	 */
	public boolean isForcedWinningNr() {
		return nrRows == 4 && nrCols == 4;
	}

	public boolean isValidWinningNr(int nr) {
		return nr >= 4 && nr <= getMaxWinningNr();
	}

	public boolean isCol(int col) {
		return col >= 1 && col <= nrCols;
	}

	public boolean isColFull(int col) {
		return board[0][col] != ' ';
	}

	public boolean isFull() {
		for (int col = 0; col < nrCols; col++) {
			if (board[0][col] == ' ') {
				return false;
			}
		}
		return true;
	}

	/**
	 * checks whether board would be full after the given move
	 */
	public boolean isFull(int move) {
		int moveCol = move - 1;
		if (calcMoveRow(moveCol) != 0) {
			return false;
		}
		for (int col = 0; col < nrCols; col++) {
			if (col != moveCol && board[0][col] == ' ') {
				return false;
			}
		}
		return true;
	}
	
	// =========================================================================
	// ================================ GETTERS ================================
	// =========================================================================
	
	public int getNrCols() {return nrCols;}

	public char[][] getBoard() {return board;}

	protected char[][] deepCopyBoard(char[][] original) {
		char[][] copy = new char[nrRows][];
		for (int i = 0; i < nrRows; i++) {
			copy[i] = new char[nrCols];
			if (nrCols >= 0) System.arraycopy(original[i], 0, copy[i], 0, nrCols);
		}
		return copy;
	}

	public char getTurnOfPlayer() {return turnOfPlayer;}

	public int getMaxWinningNr() {return Math.max(nrRows, nrCols);}

	public char getOppositeSymbol(char symbol) {
		if (symbol == 'x') {
			return 'o';
		} else {
			return 'x';
		}
	}

}
