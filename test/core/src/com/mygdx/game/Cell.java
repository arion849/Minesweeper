package com.mygdx.game;

public class Cell {
    private boolean isMine; // Indicates if the cell contains a mine
    private int adjacentMines; // Number of adjacent mines

    // Getter for mine status
    public boolean isMine() {
        return isMine;
    }

    // Setter for mine status
    public void setMine(boolean isMine) {
        this.isMine = isMine;
    }

    // Getter for the number of adjacent mines
    public int getAdjacentMines() {
        return adjacentMines;
    }

    // Setter for the number of adjacent mines
    public void setAdjacentMines(int adjacentMines) {
        this.adjacentMines = adjacentMines;
    }
}
