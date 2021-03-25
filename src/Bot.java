/**
 * The bot is used when the user chooses to play against the computer.
 * It chooses its moves by looking what board they result in 6 moves into the future
 * if both players always make the best move.
 * Because the simulated future boards can be reused for the calculation of subsequent
 * bot moves, these simulations are stored as Simulation objects.
 *
 * @author Luise Woehlke
 */
public class Bot {

    public static final char DEFAULT_SYMBOL = 'o';

    private char symbol;
    private boolean used;

    /**
     * an extra simulation for the current state of the game
     * also the bot's link to the simulations
     */
    private Simulation simulationQuo;

    public Bot(Model model) {
        used = true;
        symbol = DEFAULT_SYMBOL;
        this.simulationQuo = new Simulation(model, symbol);
    }

    /**
     * sets the bot's symbol and creates the simulations using the model
     *
     * @param model the initial state of the game
     * @param symbol the bot's symbol
     */
    public void initialise(Model model, char symbol) {
        this.symbol = symbol;
        simulationQuo = new Simulation(model, symbol);
    }

    /**
     * Calculates the bot's moves. First, it looks for an obvious move. If there
     * is none, it calculates the best move using all simulations.
     *
     * @return a move for the bot
     */
    public int calcMove() {
        int move = getObviousMove();
        if (move == 0) {
            move = calcBestBotMove(simulationQuo).getMoveCol() + 1;
        }
        return move;
    }

    /**
     * Returns an arbitrary move if all simulations have been discarded
     * because the game is lost anyway. If there is only one nextSimulation left, it
     * returns its move as it is the only choice. If one of the nextSimulations is
     * a win for the bot, it returns that. If none of the above, it returns 0.
     *
     * @return an obvious move, otherwise 0
     */
    private int getObviousMove() {
        if (simulationQuo.getNextSimulations().length == 0) {
            for (int col = 0; col < simulationQuo.getNrCols(); col++) {
                if (!simulationQuo.isColFull(col)) {
                    return col + 1;
                }
            }
        }

        if (simulationQuo.getNextSimulations().length == 1) {
            return simulationQuo.getNextSimulations()[0].getMoveCol() + 1;
        }

        for (Simulation simulation : simulationQuo.getNextSimulations()) {
            if (simulation.getBotWon()) {
                return simulation.getMoveCol() + 1;
            }
        }

        return 0;
    }

    /**
     * Calculates the best move for the bot, given the last move of the user.
     * It first tries to find an obvious move. If there is none, it
     * assumes each player always makes the best move and calculates
     * the resulting boards after 6 moves for each possible next move of the bot.
     * Then, it selects the move that leads to the best board.
     *
     * More specifically, it first calculates the best user move for each possible bot move,
     * and then selects the bot move whose best user move is best for the bot (i.e., worst for the user).
     *
     * @param lastUserMove Simulation with the last user move
     * @return Simulation with the best bot move
     */
    private Simulation calcBestBotMove(Simulation lastUserMove) {
        Simulation bestBotMove = calcObviousBotMove(lastUserMove);

        if (bestBotMove == null) {
            Simulation[] nextUserMoves = new Simulation[lastUserMove.getNextSimulations().length];
            for (int i = 0; i < nextUserMoves.length; i++) {
                nextUserMoves[i] = calcBestUserMove(lastUserMove.getNextSimulations()[i]);
            }

            Simulation worstUserMove = calcWorstForUser(nextUserMoves);
            bestBotMove = worstUserMove.getPriorSimulation();
            bestBotMove.setScore(worstUserMove.getScore());
        }

        return bestBotMove;
    }

    /**
     * Tries to get an obvious move. If the board is full in the next Simulation,
     * it returns that Simulation (since it will be the only Simulation). Sets
     * the score to 0 so a full board is seen as a draw.
     *
     * @param lastUserMove Simulation with the last user move
     * @return Simulation with an obvious move, otherwise null
     */
    private Simulation calcObviousBotMove(Simulation lastUserMove) {
        if (lastUserMove.getNextSimulations()[0].isFull()) {
            lastUserMove.getNextSimulations()[0].setScore(0);
            return lastUserMove.getNextSimulations()[0];
        }

        return null;
    }

    /**
     * finds the simulation in a list of simulations that is worst for
     * the user, i.e., that has the highest score
     *
     * @param simulations list of simulations
     * @return the simulation with the highest score
     */
    private Simulation calcWorstForUser(Simulation[] simulations) {
        Simulation worstForUser = simulations[0];
        for (int i = 1; i < simulations.length; i++) {
            if (simulations[i].getScore() > worstForUser.getScore()) {
                worstForUser = simulations[i];
            }
        }
        return worstForUser;
    }

    /**
     * Calculates the best move for the user, given the last move of the bot.
     * It first tries to find an obvious move. If there is none, it
     * assumes each player always makes the best move and calculates
     * the resulting boards after 6 moves for each possible next move of the user.
     * Then, it selects the move that leads to the best board.
     *
     * More specifically, it first calculates the best bot move for each possible user move,
     * and then selects the user move whose best bot move is best for the user (i.e., worst for the bot).
     *
     * @param lastBotMove Simulation with the last bot move
     * @return Simulation with the best user move
     */
    private Simulation calcBestUserMove(Simulation lastBotMove) {
        Simulation bestUserMove = calcObviousUserMove(lastBotMove);

        if (bestUserMove == null) {
            Simulation[] nextBotMoves = new Simulation[lastBotMove.getNextSimulations().length];
            for (int i = 0; i < nextBotMoves.length; i++) {
                nextBotMoves[i] = calcBestBotMove(lastBotMove.getNextSimulations()[i]);
            }

            Simulation worstBotMove = calcWorstForBot(nextBotMoves);
            bestUserMove = worstBotMove.getPriorSimulation();
            bestUserMove.setScore(worstBotMove.getScore());
        }

        return bestUserMove;
    }

    /**
     * Tries to get an obvious move. If the board is full in the next simulation,
     * it returns that simulation (since it will be the only simulation). Sets
     * the score to 0 so a full board is seen as a draw. If it's the 6th turn in the
     * next simulation, it returns the 6th simulation with the lowest score since there are
     * no future simulations to consider.
     *
     * @param lastBotMove Simulation with the last bot move
     * @return Simulation with an obvious move, otherwise null
     */
    private Simulation calcObviousUserMove(Simulation lastBotMove) {
        if (lastBotMove.getNextSimulations()[0].isFull()) {
            lastBotMove.getNextSimulations()[0].setScore(0);
            return lastBotMove.getNextSimulations()[0];
        }

        if (lastBotMove.getNrTurns() == 5) {
            return calcWorstForBot(lastBotMove.getNextSimulations());
        }

        return null;
    }

    /**
     * finds the simulation in a list of simulations that is worst for
     * the bot, i.e., that has the lowest score
     *
     * @param simulations list of simulations
     * @return the simulation with the lowest score
     */
    private Simulation calcWorstForBot(Simulation[] simulations) {
        Simulation worstForBot = simulations[0];
        for (int i = 1; i < simulations.length; i++) {
            if (simulations[i].getScore() < worstForBot.getScore()) {
                worstForBot = simulations[i];
            }
        }
        return worstForBot;
    }

    /**
     * changes the simulationQuo after a move, deletes the old simulationQuo,
     * updates the nrTurns of all simulations accordingly, and finally adds the
     * simulations of the 6th turn from now
     *
     * @param moveCol column of the latest move
     */
    public void runSimulations(int moveCol) {
        simulationQuo = getNextSimulationQuo(moveCol);
        simulationQuo.delPriorSimulation();
        simulationQuo.recursiveDecrementNrTurns();
        simulationQuo.addSixthSimulations();
    }

    /**
     * Gets the simulation that matches the move that was just made.
     * If all simulations have been discarded, however, it just returns
     * the current simulationQuo because the game is lost anyway.
     *
     * @param moveCol column of the latest move
     */
    private Simulation getNextSimulationQuo(int moveCol) {
        for (Simulation simulation : simulationQuo.getNextSimulations()) {
            if (simulation.getMoveCol() == moveCol) {
                return simulation;
            }
        }
        return simulationQuo;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public boolean isUsed() {
        return used;
    }

    public char getSymbol() {
        return symbol;
    }

}
