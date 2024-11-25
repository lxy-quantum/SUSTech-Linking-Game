package org.linkingGame;

import java.util.Random;

public class Game {

    // row length
    int rowSize;

    // col length
    int colSize;

    // board content
    int[][] board;

    public Game(int[][] board) {
        this.board = board;
        this.rowSize = board.length;
        this.colSize = board[0].length;
    }

    // randomly initialize the game board
    public static int[][] setUpBoard(int row, int col) {
        //TODO: pairs
        Random rand = new Random();
        int[][] board = new int[row][col];
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                board[i][j] = rand.nextInt(12);
            }
        }
        return board;
    }

    // judge the validity of an operation
    public LinkingResult judge(int row1, int col1, int row2, int col2){
        if ((board[row1][col1] != board[row2][col2]) || (row1 == row2 && col1 == col2)) {
            return new LinkingResult(false);
        }

        // one line
        if (isDirectlyConnected(row1, col1, row2, col2, board)) {
            LinkingResult linkingResult = new LinkingResult(true);
            if (row1 == row2) {
                int minCol = Math.min(col1, col2);
                int maxCol = Math.max(col1, col2);
                for (int col = minCol + 1; col < maxCol; col++) {
                    linkingResult.addTuple(row1, col);
                }
            } else if (col1 == col2) {
                int minRow = Math.min(row1, row2);
                int maxRow = Math.max(row1, row2);
                for (int row = minRow + 1; row < maxRow; row++) {
                    linkingResult.addTuple(row, col1);
                }
            }
            return linkingResult;
        }

        // two lines
        if((row1 != row2) && (col1 != col2)){
            if(board[row1][col2] == 0 && isDirectlyConnected(row1, col1, row1, col2, board)
            && isDirectlyConnected(row1, col2, row2, col2, board)) {
                LinkingResult linkingResult = new LinkingResult(true);
                int minCol = Math.min(col1, col2);
                int maxCol = Math.max(col1, col2);
                for (int col = minCol + 1; col < maxCol; col++) {
                    linkingResult.addTuple(row1, col);
                }
                int minRow = Math.min(row1, row2);
                int maxRow = Math.max(row1, row2);
                for (int row = minRow + 1; row < maxRow; row++) {
                    linkingResult.addTuple(row, col2);
                }
                linkingResult.addTuple(row1, col2);
                return linkingResult;
            }
            if(board[row2][col1] == 0 && isDirectlyConnected(row2, col2, row2, col1, board)
            && isDirectlyConnected(row2, col1, row1, col1, board)) {
                LinkingResult linkingResult = new LinkingResult(true);
                int minCol = Math.min(col1, col2);
                int maxCol = Math.max(col1, col2);
                for (int col = minCol + 1; col < maxCol; col++) {
                    linkingResult.addTuple(row2, col);
                }
                int minRow = Math.min(row1, row2);
                int maxRow = Math.max(row1, row2);
                for (int row = minRow + 1; row < maxRow; row++) {
                    linkingResult.addTuple(row, col1);
                }
                linkingResult.addTuple(row2, col1);
                return linkingResult;
            }
        }

        // three lines
        if(row1 != row2) {
            for (int i = 0; i < board[0].length; i++) {
                if (board[row1][i] == 0 && board[row2][i] == 0 &&
                        isDirectlyConnected(row1, col1, row1, i, board) && isDirectlyConnected(row1, i, row2, i, board)
                        && isDirectlyConnected(row2, col2, row2, i, board)){
                    LinkingResult linkingResult = new LinkingResult(true);
                    int minCol = Math.min(col1, i);
                    int maxCol = Math.max(col1, i);
                    for (int col = minCol + 1; col < maxCol; col++) {
                        linkingResult.addTuple(row1, col);
                    }
                    int minRow = Math.min(row1, row2);
                    int maxRow = Math.max(row1, row2);
                    for (int row = minRow + 1; row < maxRow; row++) {
                        linkingResult.addTuple(row, i);
                    }
                    minCol = Math.min(col2, i);
                    maxCol = Math.max(col2, i);
                    for (int col = minCol + 1; col < maxCol; col++) {
                        linkingResult.addTuple(row2, col);
                    }
                    linkingResult.addTuple(row1, i);
                    linkingResult.addTuple(row2, i);
                    return linkingResult;
                }
            }
        }
        if(col1 != col2) {
            for (int j = 0; j < board.length; j++){
                if (board[j][col1] == 0 && board[j][col2] == 0 &&
                        isDirectlyConnected(row1, col1, j, col1, board) && isDirectlyConnected(j, col1, j, col2, board)
                        && isDirectlyConnected(row2, col2, j, col2, board)){
                    LinkingResult linkingResult = new LinkingResult(true);
                    int minRow = Math.min(row1, j);
                    int maxRow = Math.max(row1, j);
                    for (int row = minRow + 1; row < maxRow; row++) {
                        linkingResult.addTuple(row, col1);
                    }
                    int minCol = Math.min(col1, col2);
                    int maxCol = Math.max(col1, col2);
                    for (int col = minCol + 1; col < maxCol; col++) {
                        linkingResult.addTuple(j, col);
                    }
                    minRow = Math.min(j, row2);
                    maxRow = Math.max(j, row2);
                    for (int row = minRow + 1; row < maxRow; row++) {
                        linkingResult.addTuple(row, col2);
                    }
                    linkingResult.addTuple(j, col1);
                    linkingResult.addTuple(j, col2);
                    return linkingResult;
                }
            }
        }

        return new LinkingResult(false);
    }

    // judge whether
    private boolean isDirectlyConnected(int row1, int col1, int row2, int col2, int[][] board) {
        if (row1 == row2) {
            int minCol = Math.min(col1, col2);
            int maxCol = Math.max(col1, col2);
            for (int col = minCol + 1; col < maxCol; col++) {
                if (board[row1][col] != 0) {
                    return false;
                }
            }
            return true;
        } else if (col1 == col2) {
            int minRow = Math.min(row1, row2);
            int maxRow = Math.max(row1, row2);
            for (int row = minRow + 1; row < maxRow; row++) {
                if (board[row][col1] != 0) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

}
