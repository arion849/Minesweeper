package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.Preferences;

public class MyGDXgame extends ApplicationAdapter {
	private SpriteBatch batch;
	private OrthographicCamera camera;
	private Texture coveredCellTexture;
	private Texture[] uncoveredCellTextures;
	private Texture mineTexture;
	private Texture flagTexture;
	private Texture backgroundTexture;
	private Texture winTexture;
	private Texture loseTexture;
	private Grid grid;
	private Sound explosionSound;
	private Sound loseSound;
	private Sound tileBreakingSound;
	private Music backgroundMusic;
	private BitmapFont font;
	private Preferences prefs;
	private boolean gameOver;
	private boolean gameWon;
	private boolean firstClick;
	private long startTime;
	private float elapsedTime;
	private float bestTime;
	private static final int GRID_ROWS = 10;
	private static final int GRID_COLUMNS = 10;
	private static final int CELL_SIZE = 32; // Size of each cell
	private static final int TOTAL_MINES = 20; // Number of mines

	@Override
	public void create() {
		batch = new SpriteBatch();
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 500);

		// Load textures
		coveredCellTexture = new Texture(Gdx.files.internal("covered_cell.png"));
		uncoveredCellTextures = new Texture[9];
		for (int i = 0; i < 9; i++) {
			uncoveredCellTextures[i] = new Texture(Gdx.files.internal("uncovered_" + i + ".png"));
		}
		mineTexture = new Texture(Gdx.files.internal("mine.png"));
		flagTexture = new Texture(Gdx.files.internal("flag.png"));
		backgroundTexture = new Texture(Gdx.files.internal("background.png"));
		winTexture = new Texture(Gdx.files.internal("win.png"));
		loseTexture = new Texture(Gdx.files.internal("lose.png"));

		// Load sounds
		explosionSound = Gdx.audio.newSound(Gdx.files.internal("explosion.wav"));
		loseSound = Gdx.audio.newSound(Gdx.files.internal("lose1.wav"));
		tileBreakingSound = Gdx.audio.newSound(Gdx.files.internal("tile_breaking.wav"));
		backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("background_music.wav"));

		font = new BitmapFont();

		// Start playing background music
		backgroundMusic.setLooping(true);
		backgroundMusic.play();

		// Load best time from preferences
		prefs = Gdx.app.getPreferences("MyGDXgame");
		bestTime = prefs.getFloat("bestTime", -1); // Initialize to -1

		grid = new Grid(GRID_ROWS, GRID_COLUMNS, TOTAL_MINES);
		gameOver = false;
		gameWon = false;
		firstClick = true;
		elapsedTime = 0;
	}

	@Override
	public void render() {
		if (!gameOver && !gameWon && !firstClick) {
			elapsedTime = (System.currentTimeMillis() - startTime) / 1000f;
		}

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		camera.update();
		batch.setProjectionMatrix(camera.combined);

		batch.begin();
		batch.draw(backgroundTexture, 0, 0, 800, 500);

		float gridStartX = (float) (800 - GRID_COLUMNS * CELL_SIZE) / 2;
		float gridStartY = (float) (500 - GRID_ROWS * CELL_SIZE) / 2 - 25;
		grid.draw(batch, coveredCellTexture, flagTexture, uncoveredCellTextures, mineTexture, CELL_SIZE, gridStartX, gridStartY);

		font.draw(batch, "Time: " + String.format("%.1f", elapsedTime), 10, 490);

		if (gameOver) {
			batch.draw(loseTexture, (float) (800 - loseTexture.getWidth()) / 2, (float) (500 - loseTexture.getHeight()) / 2);
		} else if (gameWon) {
			batch.draw(winTexture, (float) (800 - winTexture.getWidth()) / 2, (float) (500 - winTexture.getHeight()) / 2);
		}

		// Draw best time
		if (bestTime != -1) {
			font.draw(batch, "Best Time: " + String.format("%.1f", bestTime), 10, 450);
		} else {
			font.draw(batch, "Best Time: N/A", 10, 450);
		}

		batch.end();

		if (!gameOver && !gameWon) {
			handleInput(gridStartX, gridStartY);
		}
	}

	private void handleInput(float gridStartX, float gridStartY) {
		if (Gdx.input.justTouched()) {
			Vector3 touchPos = new Vector3();
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos);
			int cellX = (int) ((touchPos.x - gridStartX) / CELL_SIZE);
			int cellY = GRID_ROWS - 1 - (int) ((touchPos.y - gridStartY) / CELL_SIZE);

			if (cellX >= 0 && cellX < GRID_COLUMNS && cellY >= 0 && cellY < GRID_ROWS) {
				if (firstClick) {
					firstClick = false;
					grid.ensureFirstClickSafe(cellY, cellX);
					startTime = System.currentTimeMillis();
				}
				if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
					boolean mineHit = grid.uncoverCell(cellY, cellX);
					if (mineHit) {
						System.out.println("Game Over! You hit a mine.");
						explosionSound.play(); // Play sound on mine hit
						loseSound.play(); // Play lose sound
						gameOver = true;
						backgroundMusic.stop(); // Stop background music on game over
					} else {
						tileBreakingSound.play(); // Play breaking sound when uncovering cell
						checkWinCondition();
					}
				} else if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
					grid.flagCell(cellY, cellX);
				}
			}
		}
	}

	private void checkWinCondition() {
		if (grid.isCleared()) {
			gameWon = true;
			backgroundMusic.stop(); // Stop background music on win
			if (bestTime == -1 || elapsedTime < bestTime) { // Check if the current time is better
				bestTime = elapsedTime;
				prefs.putFloat("bestTime", bestTime);
				prefs.flush();
			}
		}
	}

	@Override
	public void dispose() {
		batch.dispose();
		coveredCellTexture.dispose();
		for (Texture texture : uncoveredCellTextures) {
			texture.dispose();
		}
		mineTexture.dispose();
		flagTexture.dispose();
		backgroundTexture.dispose();
		winTexture.dispose();
		loseTexture.dispose();
		explosionSound.dispose();
		loseSound.dispose();
		tileBreakingSound.dispose();
		backgroundMusic.dispose();
		font.dispose();
	}
}
