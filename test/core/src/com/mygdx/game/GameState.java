package com.mygdx.game;

import java.io.Serializable;

class GameState implements Serializable {
    private static final long serialVersionUID = 1L;
    int[][] gridState;
    boolean[][] revealedCells;
    boolean[][] flaggedCells;
    float elapsedTime;

    GameState(int[][] gridState, boolean[][] revealedCells, boolean[][] flaggedCells, float elapsedTime) {
        this.gridState = gridState;
        this.revealedCells = revealedCells;
        this.flaggedCells = flaggedCells;
        this.elapsedTime = elapsedTime;
    }
}
