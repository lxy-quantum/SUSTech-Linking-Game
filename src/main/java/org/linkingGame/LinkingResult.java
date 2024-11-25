package org.linkingGame;

import java.util.ArrayList;

public class LinkingResult {
    protected boolean success;
    protected ArrayList<Tuple> tuples = new ArrayList<>();

    public LinkingResult(boolean success) {
        this.success = success;
    }

    public void addTuple(int row, int col) {
        tuples.add(new Tuple(row, col));
    }
}

class Tuple {
    protected int row, col;

    public Tuple(int row, int col) {
        this.row = row;
        this.col = col;
    }
}


