import java.util.Arrays;

/**
 * To score a simulation, I consider how many possibilities for a chain
 * of the winning size (a "window") are still open on the board and how
 * many pieces the player already has in each of these windows.
 *
 * To represent the windows, I give each field a number, its row
 * representing the first and its column the second digit. The field
 * with the smallest nr in a window represents the window.
 *
 * @author Luise Woehlke
 */
public class Windows {

    private static int nrRows;
    private static int nrCols;
    private static int winningNr;

    /**
     * arrays holding the window representations
     */
    private int[] horizontal;
    private int[] vertical;
    private int[] ascending;
    private int[] descending;

    /**
     * arrays holding a score for each window
     */
    private int[] horizontalScores;
    private int[] verticalScores;
    private int[] ascendingScores;
    private int[] descendingScores;

    private int totalScore;

    /**
     * Creates the first windows for the first simulationQuo and
     * initialises them. Also calculates their initial total score.
     *
     * @param nrRows nr of simulation rows
     * @param nrCols nr of simulation columns
     * @param winningNr winningNr of simulation
     */
    public Windows(int nrRows, int nrCols, int winningNr) {
        Windows.nrRows = nrRows;
        Windows.nrCols = nrCols;
        Windows.winningNr = winningNr;

        int nrHorizontal = nrRows * (nrCols - winningNr + 1);
        int nrVertical = nrCols * (nrRows - winningNr + 1);
        int nrAscending = (nrRows - winningNr + 1) * (nrCols - winningNr + 1);

        horizontal = new int[nrHorizontal];
        vertical = new int[nrVertical];
        ascending = new int[nrAscending];
        descending = new int[nrAscending];

        horizontalScores = new int[nrHorizontal];
        verticalScores = new int[nrVertical];
        ascendingScores = new int[nrAscending];
        descendingScores = new int[nrAscending];

        initialiseHorizontal();
        initialiseVertical();
        initialiseAscending();
        initialiseDescending();

        totalScore = nrHorizontal + nrVertical + nrAscending + nrAscending;
    }

    /**
     * Creates the windows for a simulation by copying the windows of its
     * preceding simulation.
     *
     * @param prior windows of the preceding simulation
     */
    public Windows(Windows prior) {
        horizontal = deepCopy(prior.horizontal);
        vertical = deepCopy(prior.vertical);
        ascending = deepCopy(prior.ascending);
        descending = deepCopy(prior.descending);

        horizontalScores = deepCopy(prior.horizontalScores);
        verticalScores = deepCopy(prior.verticalScores);
        ascendingScores = deepCopy(prior.ascendingScores);
        descendingScores = deepCopy(prior.descendingScores);
        totalScore = prior.totalScore;
    }

    /**
     * Creates the representations for the horizontal windows.
     * Sets every window's score equal to 1 for being a possible
     * winning window.
     */
    private void initialiseHorizontal() {
        int iOfWindow = 0;
        for (int i = 0; i < nrRows; i++) {
            for (int j = 0; j < nrCols - winningNr + 1; j++) {
                horizontal[iOfWindow] = i * 10 + j;
                horizontalScores[iOfWindow] = 1;
                iOfWindow++;
            }
        }
    }

    /**
     * Creates the representations for the vertical windows.
     * Sets every window's score equal to 1 for being a possible
     * winning window.
     */
    private void initialiseVertical() {
        int iOfWindow = 0;
        for (int i = 0; i < nrRows - winningNr + 1; i++) {
            for (int j = 0; j < nrCols; j++) {
                vertical[iOfWindow] = i * 10 + j;
                verticalScores[iOfWindow] = 1;
                iOfWindow++;
            }
        }
    }

    /**
     * Creates the representations for the ascending diagonal windows.
     * Sets every window's score equal to 1 for being a possible
     * winning window.
     */
    private void initialiseAscending() {
        int iOfWindow = 0;
        for (int i = winningNr - 1; i < nrRows; i++) {
            for (int j = 0; j < nrCols - winningNr + 1; j++) {
                ascending[iOfWindow] = i * 10 + j;
                ascendingScores[iOfWindow] = 1;
                iOfWindow++;
            }
        }
    }

    /**
     * Creates the representations for the descending diagonal windows.
     * Sets every window's score equal to 1 for being a possible
     * winning window.
     */
    private void initialiseDescending() {
        int iOfWindow = 0;
        for (int i = 0; i < nrRows - winningNr + 1; i++) {
            for (int j = 0; j < nrCols - winningNr + 1; j++) {
                descending[iOfWindow] = i * 10 + j;
                descendingScores[iOfWindow] = 1;
                iOfWindow++;
            }
        }
    }

    /**
     * Deletes windows that can't hold a winning chain anymore
     * due to being broken up by the opponent's move. Subtracts
     * the score of the deleted window from the total score.
     *
     * @param row row of opponent's move
     * @param col column of opponent's move
     */
    public void filter(int row, int col) {
        int iOfWindow;
        for (int i = winningNr - 1; i >= 0 ; i--) {
            iOfWindow = Arrays.binarySearch(horizontal, row * 10 + col - i);
            if (iOfWindow >= 0) {
                totalScore -= horizontalScores[iOfWindow];
                rmHorizontal(iOfWindow);
            }

            iOfWindow = Arrays.binarySearch(vertical, row * 10 + col - i * 10);
            if (iOfWindow >= 0) {
                totalScore -= verticalScores[iOfWindow];
                rmVertical(iOfWindow);
            }

            iOfWindow = Arrays.binarySearch(ascending, row * 10 + col + i * 10 - i);
            if (iOfWindow >= 0) {
                totalScore -= ascendingScores[iOfWindow];
                rmAscending(iOfWindow);
            }

            iOfWindow = Arrays.binarySearch(descending, row * 10 + col - i * 10 - i);
            if (iOfWindow >= 0) {
                totalScore -= descendingScores[iOfWindow];
                rmDescending(iOfWindow);
            }
        }
    }

    /**
     * Increases the score of the windows where the last move went.
     * The score is the winningNr to the power of the nr of pieces
     * in the window. Increases the total score accordingly.
     *
     * @param row row of move
     * @param col column of move
     */
    public void score(int row, int col) {
        int iOfWindow;
        for (int i = winningNr - 1; i >= 0 ; i--) {
            iOfWindow = Arrays.binarySearch(horizontal, row * 10 + col - i);
            if (iOfWindow >= 0) {
                totalScore -= horizontalScores[iOfWindow];
                horizontalScores[iOfWindow] *= winningNr;
                totalScore += horizontalScores[iOfWindow];
            }

            iOfWindow = Arrays.binarySearch(vertical, row * 10 + col - i * 10);
            if (iOfWindow >= 0) {
                totalScore -= verticalScores[iOfWindow];
                verticalScores[iOfWindow] *= winningNr;
                totalScore += verticalScores[iOfWindow];
            }

            iOfWindow = Arrays.binarySearch(ascending, row * 10 + col + i * 10 - i);
            if (iOfWindow >= 0) {
                totalScore -= ascendingScores[iOfWindow];
                ascendingScores[iOfWindow] *= winningNr;
                totalScore += ascendingScores[iOfWindow];
            }

            iOfWindow = Arrays.binarySearch(descending, row * 10 + col - i * 10 - i);
            if (iOfWindow >= 0) {
                totalScore -= descendingScores[iOfWindow];
                descendingScores[iOfWindow] *= winningNr;
                totalScore += descendingScores[iOfWindow];
            }
        }
    }

    /**
     * removes a window from the horizontal array and its score
     * from the horizontalScores array
     *
     * @param i the index of the window
     */
    private void rmHorizontal(int i) {
        int[] temp = new int[horizontal.length - 1];
        System.arraycopy(horizontal,0, temp, 0, i);
        System.arraycopy(horizontal, i+1, temp, i, temp.length - i);
        horizontal = new int[temp.length];
        System.arraycopy(temp,0, horizontal, 0, temp.length);

        System.arraycopy(horizontalScores,0, temp, 0, i);
        System.arraycopy(horizontalScores, i+1, temp, i, temp.length - i);
        horizontalScores = new int[temp.length];
        System.arraycopy(temp,0, horizontalScores, 0, temp.length);
    }

    /**
     * removes a window from the vertical array and its score
     * from the verticalScores array
     *
     * @param i the index of the window
     */
    private void rmVertical (int i) {
        int[] temp = new int[vertical.length - 1];
        System.arraycopy(vertical,0, temp, 0, i);
        System.arraycopy(vertical, i+1, temp, i, temp.length - i);
        vertical = new int[temp.length];
        System.arraycopy(temp,0, vertical, 0, temp.length);

        System.arraycopy(verticalScores,0, temp, 0, i);
        System.arraycopy(verticalScores, i+1, temp, i, temp.length - i);
        verticalScores = new int[temp.length];
        System.arraycopy(temp,0, verticalScores, 0, temp.length);
    }

    /**
     * removes a window from the ascending array and its score
     * from the ascendingScores array
     *
     * @param i the index of the window
     */
    private void rmAscending(int i) {
        int[] temp = new int[ascending.length - 1];
        System.arraycopy(ascending,0, temp, 0, i);
        System.arraycopy(ascending, i+1, temp, i, temp.length - i);
        ascending = new int[temp.length];
        System.arraycopy(temp,0, ascending, 0, temp.length);

        System.arraycopy(ascendingScores,0, temp, 0, i);
        System.arraycopy(ascendingScores, i+1, temp, i, temp.length - i);
        ascendingScores = new int[temp.length];
        System.arraycopy(temp,0, ascendingScores, 0, temp.length);
    }

    /**
     * removes a window from the descending array and its score
     * from the descendingScores array
     *
     * @param i the index of the window
     */
    private void rmDescending(int i) {
        int[] temp = new int[descending.length - 1];
        System.arraycopy(descending,0, temp, 0, i);
        System.arraycopy(descending, i+1, temp, i, temp.length - i);
        descending = new int[temp.length];
        System.arraycopy(temp,0, descending, 0, temp.length);

        System.arraycopy(descendingScores,0, temp, 0, i);
        System.arraycopy(descendingScores, i+1, temp, i, temp.length - i);
        descendingScores = new int[temp.length];
        System.arraycopy(temp,0, descendingScores, 0, temp.length);
    }

    public int getScore() {
        return totalScore;
    }

    private int[] deepCopy(int[] original) {
        int[] copy = new int[original.length];
        System.arraycopy(original, 0, copy, 0, original.length);
        return copy;
    }

}
