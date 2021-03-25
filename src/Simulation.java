/**
 * Simulation is a subclass of Model because both are states of the game.
 * The simulations store possible future states of the game along with
 * their associated scores for use in calculations of the bot. Since these
 * scores can be reused with minor changes for subsequent simulations, they
 * are stored in detail in windows.
 *
 * @author Luise Woehlke
 */
public class Simulation extends Model {

    private static final int MAX_NR_TURNS = 6;

    private final Windows X_WINDOWS;
    private final Windows O_WINDOWS;
    private final int MOVE_ROW;
    private final int MOVE_COL;

    private static char botSymbol;

    /**
     * how many turns this simulation is in the future
     */
    private int nrTurns;

    /**
     * A simulation refers to all possible following simulations and its preceding simulation.
     */
    private Simulation[] nextSimulations;
    private Simulation priorSimulation;

    private int score;
    private boolean botWon;
    private boolean userWon;

    /**
     * Creates the very first simulation from the model. It also creates
     * the first windows and adds next Simulations.
     *
     * @param model the model
     * @param botSymbol the bot's symbol
     */
    public Simulation(Model model, char botSymbol) {
        super();
        Simulation.botSymbol = botSymbol;
        priorSimulation = null;
        nrRows = model.nrRows;
        nrCols = model.nrCols;
        winningNr = model.winningNr;
        nrTurns = 0;
        MOVE_ROW = -1;
        MOVE_COL = -1;
        botWon = false;
        userWon = false;
        board = deepCopyBoard(model.board);
        turnOfPlayer = model.turnOfPlayer;
        switchPlayer();
        X_WINDOWS = new Windows(nrRows, nrCols, winningNr);
        O_WINDOWS = new Windows(nrRows, nrCols, winningNr);
        score = 0;
        addNextSimulations();
    }

    /**
     * Creates a simulation from its preceding simulation and the column
     * of the move it simulates. It then filters and scores the new simulation's
     * windows and calculates its overall score. It finally adds next simulations
     * and discards the ones that are bad.
     *
     * @param prior the preceding simulation
     * @param moveCol column of the simulated move
     */
    public Simulation(Simulation prior, int moveCol) {
        super();
        score = prior.score;
        priorSimulation = prior;
        nrRows = prior.nrRows;
        nrCols = prior.nrCols;
        winningNr = prior.winningNr;
        nrTurns = prior.nrTurns + 1;
        board = deepCopyBoard(prior.board);
        turnOfPlayer = prior.turnOfPlayer;
        switchPlayer();
        this.MOVE_COL = moveCol;
        MOVE_ROW = calcMoveRow(moveCol);
        botWon = prior.botWon || isBotWin();
        userWon = prior.userWon || isUserWin();
        X_WINDOWS = new Windows(prior.X_WINDOWS);
        O_WINDOWS = new Windows(prior.O_WINDOWS);

        makeMove(moveCol);
        filterWindows();
        scoreWindows();
        score = calcScore();

        if (nrTurns < MAX_NR_TURNS && !isFull()) {
            addNextSimulations();
            if (turnOfPlayer != botSymbol) {
                discardBadNextSimulations();
            }
        } else {
            nextSimulations = new Simulation[0];
        }
    }

    private void addNextSimulations() {
        nextSimulations = new Simulation[getNrFreeCols()];
        int i = 0;
        for (int col = 0; col < nrCols; col++) {
            if (!isColFull(col)) {
                nextSimulations[i] = new Simulation(this, col);
                i++;
            }
        }
    }

    private void filterWindows() {
        if (turnOfPlayer == 'x') {
            O_WINDOWS.filter(MOVE_ROW, MOVE_COL);
        } else {
            X_WINDOWS.filter(MOVE_ROW, MOVE_COL);
        }
    }

    private void scoreWindows() {
        if (turnOfPlayer == 'x') {
            X_WINDOWS.score(MOVE_ROW, MOVE_COL);
        } else {
            O_WINDOWS.score(MOVE_ROW, MOVE_COL);
        }
    }

    /**
     * Calculates the total score of this simulation by subtracting the
     * user's score from the bot's score.
     *
     * @return the score of this simulation
     */
    private int calcScore() {
        if (botSymbol == 'o') {
            return O_WINDOWS.getScore() - X_WINDOWS.getScore();
        } else {
            return X_WINDOWS.getScore() - O_WINDOWS.getScore();
        }
    }

    private void discardBadNextSimulations() {
        discardIfLossCertain();
        discardIfImpasse();
        discardOthersIfWinFound();
    }

    /**
     * discards all next simulations where the user wins in one move
     */
    private void discardIfLossCertain() {
        for (Simulation nextBotSimulation : nextSimulations) {
            if (!nextBotSimulation.getBotWon()) {
                for (Simulation nextUserSimulation : nextBotSimulation.nextSimulations) {
                    if (nextUserSimulation.getUserWon()) {
                        nextBotSimulation.discard();
                        break;
                    }
                }
            }
        }
    }

    /**
     * Discards all next simulations whose next simulations have all been discarded.
     * This prevents the bot from reaching an impasse.
     */
    private void discardIfImpasse() {
        if (nrTurns < MAX_NR_TURNS - 3) {
            for (Simulation nextBotSimulation : nextSimulations) {
                if (!nextBotSimulation.getBotWon()) {
                    for (Simulation nextUserSimulation : nextBotSimulation.nextSimulations) {
                        if (!nextUserSimulation.isFull()
                                && nextUserSimulation.nextSimulations.length == 0) {
                            nextBotSimulation.discard();
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * if a next simulation leads to a certain win, discard all other
     * next simulations
     */
    private void discardOthersIfWinFound() {
        for (Simulation nextBotSimulation : nextSimulations) {
            if (!nextBotSimulation.getBotWon()
                    && nextBotSimulation.isWinCertain()) {
                nextBotSimulation.discardAlternativeSimulations();
            }
        }
    }

    /**
     * discards all simulations with moves that could be done alternatively
     * to this simulation's move
     */
    private void discardAlternativeSimulations() {
        for (Simulation simulation : priorSimulation.nextSimulations) {
            if (simulation != this) {
                simulation.discard();
            }
        }
    }

    /**
     * discards the current simulation
     */
    private void discard() {
        Simulation[] temp = new Simulation[priorSimulation.nextSimulations.length - 1];
        int j = 0;
        for (int i = 0; i < priorSimulation.nextSimulations.length; i++) {
            if (priorSimulation.nextSimulations[i] != this) {
                temp[j] = priorSimulation.nextSimulations[i];
                j++;
            }
        }
        priorSimulation.nextSimulations = temp;
    }

    /**
     * Recurses through the simulations until it finds the fifth simulations,
     * where it adds next simulations. Then it discards simulations based on
     * the information the new simulations provide.
     */
    public void addSixthSimulations() {
        if (nrTurns == MAX_NR_TURNS - 1) {
            addNextSimulations();
            discardWithNewInformation();
        } else {
            for (Simulation simulation : nextSimulations) {
                simulation.addSixthSimulations();
            }
        }
    }

    /**
     * Based on the new simulations, this method can now discard simulations
     * if there is a certain win, loss, or impasse.
     */
    private void discardWithNewInformation() {
        if (turnOfPlayer != botSymbol) {
            priorSimulation.priorSimulation.discardOthersIfWinFound();
        } else {
            priorSimulation.discardIfLossCertain();
            priorSimulation.priorSimulation.priorSimulation.recursiveDiscardIfImpasse();
        }
    }

    /**
     * Discards all next simulations whose next simulations have all been discarded.
     * This prevents the bot from reaching an impasse. Once simulations have been
     * discarded of course, we need to run this again, hence recursiveness.
     */
    private void recursiveDiscardIfImpasse() {
        if (nrTurns < MAX_NR_TURNS - 2) {
            for (Simulation nextBotSimulation : nextSimulations) {
                if (!nextBotSimulation.getBotWon()) {
                    for (Simulation nextUserSimulation : nextBotSimulation.nextSimulations) {
                        if (!nextUserSimulation.isFull()
                                && nextUserSimulation.nextSimulations.length == 0) {
                            nextBotSimulation.discard();
                            break;
                        }
                    }
                }
            }
            if (nrTurns > 1) {
                priorSimulation.priorSimulation.recursiveDiscardIfImpasse();
            }
        }
    }

    public void delPriorSimulation() {
        priorSimulation = null;
    }

    public void setScore(int score) {
        this.score = score;
    }

    /**
     * decrements nrTurns of ALL simulations after a move has been made
     */
    public void recursiveDecrementNrTurns() {
        nrTurns--;
        for (Simulation simulation : nextSimulations) {
            simulation.recursiveDecrementNrTurns();
        }
    }

    /**
     * returns true if for every next user move, there is a (simulated) bot
     * move that wins the game
     *
     * @return whether the bot wins in 2 moves
     */
    private boolean isWinCertain() {
        if (nrTurns > MAX_NR_TURNS - 2) {
            return false;
        }

        for (Simulation simulation : nextSimulations) {
            if (!simulation.isBotWinInOne()) {
                return false;
            }
        }
        return true;
    }

    private boolean isBotWinInOne() {
        for (Simulation simulation : nextSimulations) {
            if (simulation.getBotWon()) {
                return true;
            }
        }
        return false;
    }

    private boolean isBotWin() {
        if (turnOfPlayer == botSymbol) {
            return isWin(MOVE_COL, botSymbol);
        } else {
            return false;
        }
    }

    private boolean isUserWin() {
        if (turnOfPlayer != botSymbol) {
            return isWin(MOVE_COL, turnOfPlayer);
        } else {
            return false;
        }
    }

    public int getScore() {
        return score;
    }

    public boolean getBotWon() {
        return botWon;
    }

    public boolean getUserWon() {
        return userWon;
    }

    public int getMoveCol() {
        return MOVE_COL;
    }

    public int getNrTurns() {
        return nrTurns;
    }

    public Simulation getPriorSimulation() {
        return priorSimulation;
    }

    public Simulation[] getNextSimulations() {
        return nextSimulations;
    }

    private int getNrFreeCols() {
        int nrFreeCols = 0;
        for (int col = 0; col < nrCols; col++) {
            if (!isColFull(col)) {
                nrFreeCols++;
            }
        }
        return nrFreeCols;
    }

}
