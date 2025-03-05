package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Grid {
    private Cell[][] cells; // 2D array of cells
    private int rows; // Number of rows
    private int columns; // Number of columns
    private int totalMines; // Total number of mines
    private boolean[][] revealedCells; // Revealed status of cells
    private boolean[][] flaggedCells; // Flagged status of cells

    // Constructor for initializing the grid
    public Grid(int rows, int columns, int totalMines) {
        this.rows = rows;
        this.columns = columns;
        this.totalMines = totalMines;
        this.cells = new Cell[rows][columns];
        initializeGrid();
        placeMines(totalMines);
        calculateAdjacentMines();
    }

    // Constructor for loading a saved game state
    public Grid(boolean[][] revealedCells, boolean[][] flaggedCells) {
        this.rows = revealedCells.length;
        this.columns = revealedCells[0].length;
        this.totalMines = countMines();
        this.revealedCells = revealedCells;
        this.flaggedCells = flaggedCells;
        this.cells = new Cell[rows][columns];
        initializeGrid();
    }

    // Initialize grid with cells
    private void initializeGrid() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                cells[row][col] = new Cell();
            }
        }
        if (revealedCells == null) {
            revealedCells = new boolean[rows][columns];
        }
        if (flaggedCells == null) {
            flaggedCells = new boolean[rows][columns];
        }
    }

    // Place mines randomly on the grid
    private void placeMines(int totalMines) {
        int minesPlaced = 0;
        while (minesPlaced < totalMines) {
            int row = (int) (Math.random() * rows);
            int col = (int) (Math.random() * columns);
            if (!cells[row][col].isMine()) {
                cells[row][col].setMine(true);
                minesPlaced++;
            }
        }
    }

    // Calculate the number of adjacent mines for each cell
    private void calculateAdjacentMines() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                if (!cells[row][col].isMine()) {
                    int adjacentMines = countAdjacentMines(row, col);
                    cells[row][col].setAdjacentMines(adjacentMines);
                }
            }
        }
    }

    // Count the number of adjacent mines around a given cell
    private int countAdjacentMines(int row, int col) {
        int count = 0;
        for (int i = row - 1; i <= row + 1; i++) {
            for (int j = col - 1; j <= col + 1; j++) {
                if (i >= 0 && i < rows && j >= 0 && j < columns && cells[i][j].isMine()) {
                    count++;
                }
            }
        }
        return count;
    }

    // Count the total number of mines in the grid
    private int countMines() {
        int count = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                if (cells[row][col].isMine()) {
                    count++;
                }
            }
        }
        return count;
    }

    // Draw the grid and cells on the screen
    public void draw(SpriteBatch batch, Texture coveredCellTexture, Texture flagTexture, Texture[] uncoveredCellTextures, Texture mineTexture, int cellSize, float startX, float startY) {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                float x = startX + col * cellSize;
                float y = startY + (rows - 1 - row) * cellSize;
                if (revealedCells[row][col]) {
                    if (cells[row][col].isMine()) {
                        batch.draw(mineTexture, x, y, cellSize, cellSize);
                    } else {
                        batch.draw(uncoveredCellTextures[cells[row][col].getAdjacentMines()], x, y, cellSize, cellSize);
                    }
                } else {
                    if (flaggedCells[row][col]) {
                        batch.draw(flagTexture, x, y, cellSize, cellSize);
                    } else {
                        batch.draw(coveredCellTexture, x, y, cellSize, cellSize);
                    }
                }
            }
        }
    }

    // Uncover a cell and handle game logic
    public boolean uncoverCell(int row, int col) {
        if (row >= 0 && row < rows && col >= 0 && col < columns && !revealedCells[row][col] && !flaggedCells[row][col]) {
            revealedCells[row][col] = true;
            if (cells[row][col].isMine()) {
                return true; // Mine hit
            } else if (cells[row][col].getAdjacentMines() == 0) {
                uncoverAdjacentCells(row, col);
            }
        }
        return false; // No mine hit
    }

    // Uncover adjacent cells if the current cell has no adjacent mines
    private void uncoverAdjacentCells(int row, int col) {
        for (int i = row - 1; i <= row + 1; i++) {
            for (int j = col - 1; j <= col + 1; j++) {
                if (i >= 0 && i < rows && j >= 0 && j < columns && !revealedCells[i][j] && !cells[i][j].isMine()) {
                    uncoverCell(i, j);
                }
            }
        }
    }

    // Flag or unflag a cell
    public void flagCell(int row, int col) {
        if (row >= 0 && row < rows && col >= 0 && col < columns && !revealedCells[row][col]) {
            flaggedCells[row][col] = !flaggedCells[row][col];
        }
    }

    // Check if the game is won (all non-mine cells are revealed)
    public boolean isCleared() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                if (!cells[row][col].isMine() && !revealedCells[row][col]) {
                    return false;
                }
            }
        }
        return true;
    }

    // Ensure the first click is safe by repositioning mines
    public void ensureFirstClickSafe(int row, int col) {
        if (cells[row][col].isMine() || cells[row][col].getAdjacentMines() > 0) {
            resetGrid();
            placeMines(totalMines);
            calculateAdjacentMines();
            ensureFirstClickSafe(row, col); // Recursive call to ensure the first clicked cell is safe
        }
    }

    // Reset the grid state
    private void resetGrid() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                cells[row][col] = new Cell();
                revealedCells[row][col] = false;
                flaggedCells[row][col] = false;
            }
        }
    }
}
