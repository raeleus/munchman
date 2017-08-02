/*
 * The MIT License
 *
 * Copyright 2017 Raymond Buckley.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.ray3k.munchman.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.ray3k.munchman.Core;
import com.ray3k.munchman.Entity;
import com.ray3k.munchman.EntityManager;
import com.ray3k.munchman.InputManager;
import com.ray3k.munchman.State;
import com.ray3k.munchman.ai.AI;
import com.ray3k.munchman.ai.AggressiveAI;
import com.ray3k.munchman.ai.CunningAI;
import com.ray3k.munchman.ai.RandomAI;
import com.ray3k.munchman.ai.RetreatAI;
import com.ray3k.munchman.ai.ScaredAI;
import com.ray3k.munchman.ai.SpawnAI;
import com.ray3k.munchman.entities.DotEntity;
import com.ray3k.munchman.entities.GhostEntity;
import com.ray3k.munchman.entities.GhostPenEntity;
import com.ray3k.munchman.entities.PlayerEntity;
import com.ray3k.munchman.entities.PowerBallEntity;
import com.ray3k.munchman.entities.SpawnGhostTimerEntity;
import com.ray3k.munchman.entities.WallEntity;
import java.util.List;
import org.xguzm.pathfinding.grid.GridCell;
import org.xguzm.pathfinding.grid.NavigationGrid;
import org.xguzm.pathfinding.grid.finders.AStarGridFinder;
import org.xguzm.pathfinding.grid.finders.GridFinderOptions;

public class GameState extends State {
    private String selectedCharacter;
    private FileHandle selectedLevel;
    private int score;
    private static int highscore = 0;
    private OrthographicCamera gameCamera;
    private Viewport gameViewport;
    private OrthographicCamera uiCamera;
    private Viewport uiViewport;
    private InputManager inputManager;
    private Skin skin;
    private Stage stage;
    private Table table;
    private Label scoreLabel;
    private EntityManager entityManager;
    private Entity[][] grid;
    private GridCell[][] navCells;
    private NavigationGrid<GridCell> navGrid;
    private AStarGridFinder<GridCell> navFinder;
    private PlayerEntity playerEntity;
    private int ghostPenX;
    private int ghostPenY;
    private Array<GhostEntity> ghosts;
    private boolean soundToggle;
    
    public GameState(Core core) {
        super(core);
    }
    
    @Override
    public void start() {
        soundToggle = false;
        ghosts = new Array<GhostEntity>();
        score = 0;
        
        inputManager = new InputManager(); 
        
        uiCamera = new OrthographicCamera();
        uiViewport = new ScreenViewport(uiCamera);
        uiViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        uiViewport.apply();
        
        uiCamera.position.set(uiCamera.viewportWidth / 2, uiCamera.viewportHeight / 2, 0);
        
        gameCamera = new OrthographicCamera();
        gameViewport = new ScreenViewport(gameCamera);
        gameViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        gameViewport.apply();
        
        gameCamera.position.set(gameCamera.viewportWidth / 2, gameCamera.viewportHeight / 2, 0);
        
        skin = getCore().getAssetManager().get(Core.DATA_PATH + "/skin/munch-man-ui.json", Skin.class);
        stage = new Stage(new ScreenViewport());
        
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(inputManager);
        inputMultiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(inputMultiplexer);
        
        table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
        
        entityManager = new EntityManager();
        
        createStageElements();
        loadLevel(selectedLevel);
    }
    
    private void loadLevel(FileHandle fileHandle) {
        if (fileHandle != null) {
            DotEntity.dotCount = 0;
            Json json = new Json();
            String[][] values = json.fromJson(String[][].class, fileHandle);

            grid = new Entity[values.length][values[0].length];
            navCells = new GridCell[values.length][values[0].length];

            Array<Color> levelColors = new Array<Color>(new Color[]{Color.RED, Color.PURPLE, Color.YELLOW, Color.CYAN, Color.GREEN, Color.ORANGE, Color.PINK, Color.VIOLET});
            Color levelColor = levelColors.random();

            int playerX = 0;
            int playerY = 0;

            for (int x = 0; x < values.length; x++) {
                for (int y = 0; y < values[x].length; y++) {
                    if (navCells[x][y] == null) {
                        navCells[x][y] = new GridCell(x, y, true);
                    }

                    if (values[x][y] != null) {
                        if (values[x][y].startsWith("WALL")) {
                            WallEntity wallEntity = new WallEntity(this);
                            wallEntity.setPosition(x * LevelDesignerState.GRID_DIMENSION, y * LevelDesignerState.GRID_DIMENSION);
                            wallEntity.setSkin(values[x][y].substring(4));
                            wallEntity.setColor(levelColor);
                            grid[x][y] = wallEntity;
                            navCells[x][y] = new GridCell(x, y, false);
                        } else if (values[x][y].equals("BALL")) {
                            PowerBallEntity entity = new PowerBallEntity(this);
                            entity.setPosition(x * LevelDesignerState.GRID_DIMENSION, y * LevelDesignerState.GRID_DIMENSION);
                            grid[x][y] = entity;
                        } else if (values[x][y].equals("GHOST_PEN")) {
                            GhostPenEntity entity = new GhostPenEntity(this);
                            entity.setPosition(x * LevelDesignerState.GRID_DIMENSION, y * LevelDesignerState.GRID_DIMENSION);
                            ghostPenX = x + 3;
                            ghostPenY = y + 4;
                            for (int i = x; i < x + 6 && i < grid.length; i++) {
                                for (int j = y; j < y + 4 && j < grid[0].length; j++) {
                                    grid[i][j] = entity;
                                    navCells[i][j] = new GridCell(i, j, false);
                                }
                            }
                        } else if (values[x][y].equals("MUNCH_MAN")) {
                            playerEntity = new PlayerEntity(this);
                            if (selectedCharacter.equals("ms-munch-man")) {
                                playerEntity.setSkin("MsMunchman");
                            }
                            playerEntity.setPosition(x * LevelDesignerState.GRID_DIMENSION + LevelDesignerState.GRID_DIMENSION / 2.0f, y * LevelDesignerState.GRID_DIMENSION + LevelDesignerState.GRID_DIMENSION / 2.0f);
                            playerEntity.setTargetGridX(x);
                            playerEntity.setTargetGridY(y);
                            playerX = x;
                            playerY = y;
                            grid[x][y] = playerEntity;
                        }
                    }
                }
            }

            navGrid = new NavigationGrid<GridCell>(navCells, true);
            GridFinderOptions opt = new GridFinderOptions();
            opt.allowDiagonal = false;
            opt.isYDown = false;
            navFinder = new AStarGridFinder<GridCell>(GridCell.class, opt);

            for (int x = 0; x < values.length; x++) {
                for (int y = 0; y < values[x].length; y++) {
                    if (grid[x][y] == null) {
                        List<GridCell> path = navFinder.findPath(playerX, playerY, x, y, navGrid);

                        if (path != null) {
                            DotEntity entity = new DotEntity(this);
                            entity.setPosition(x * LevelDesignerState.GRID_DIMENSION, y * LevelDesignerState.GRID_DIMENSION);
                        }
                    }
                }
            }

            spawnGhost(new RandomAI(), "orange");
            new SpawnGhostTimerEntity(this, 3, new AggressiveAI(), "red");
            new SpawnGhostTimerEntity(this, 6, new CunningAI(), "blue");
            new SpawnGhostTimerEntity(this, 9, new RandomAI(), "pink");
            
            gameCamera.position.set(grid.length * LevelDesignerState.GRID_DIMENSION / 2.0f, grid[0].length * LevelDesignerState.GRID_DIMENSION / 2.0f, 0);
        }
    } 
    
    public void spawnGhost(AI primaryAI, String skin) {
        GhostEntity ghostEntity = new GhostEntity(this);
        ghostEntity.setPrimaryAI(primaryAI);
        ghostEntity.setSkin(skin);
        ghostEntity.setPrimarySkin(skin);
        ghostEntity.setPosition(ghostPenX * LevelDesignerState.GRID_DIMENSION + LevelDesignerState.GRID_DIMENSION * .5f, (ghostPenY - 1) * LevelDesignerState.GRID_DIMENSION + LevelDesignerState.GRID_DIMENSION * .5f);
        ghosts.add(ghostEntity);
        playerEntity.addPlayerGridListener(ghostEntity);
    }
    
    public void makeGhostsScared() {
        for (GhostEntity ghost : ghosts) {
            if (!(ghost.getAi() instanceof RetreatAI) && !(ghost.getAi() instanceof SpawnAI)) {
                ghost.setAi(new ScaredAI());
                ghost.setSkin("scared");
            }
        }
    }
    
    public void makeGhostsRandom() {
        for (GhostEntity ghost : ghosts) {
            if (!(ghost.getAi() instanceof RetreatAI)) {
                ghost.setAi(new RandomAI());
            } else {
                ghost.setPrimaryAI(new RandomAI());
            }
        }
    }

    public GridCell[][] getNavCells() {
        return navCells;
    }

    public NavigationGrid<GridCell> getNavGrid() {
        return navGrid;
    }

    public AStarGridFinder<GridCell> getNavFinder() {
        return navFinder;
    }
    
    private void createStageElements() {
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);
        
        scoreLabel = new Label("0", skin);
        root.add(scoreLabel).expandY().padTop(25.0f).top();
    }
    
    @Override
    public void draw(SpriteBatch spriteBatch, float delta) {
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        gameCamera.update();
        getCore().getTwoColorPolygonBatch().setProjectionMatrix(gameCamera.combined);
        getCore().getTwoColorPolygonBatch().begin();
        entityManager.draw(spriteBatch, delta);
        getCore().getTwoColorPolygonBatch().end();
        
        stage.draw();
    }

    @Override
    public void act(float delta) {
        entityManager.act(delta);
        
        stage.act(delta);
    }

    @Override
    public void dispose() {
    }

    @Override
    public void stop() {
        stage.dispose();
    }
    
    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height);
        gameCamera.position.set(grid.length * LevelDesignerState.GRID_DIMENSION / 2.0f, grid[0].length * LevelDesignerState.GRID_DIMENSION / 2.0f, 0);
        
        uiViewport.update(width, height);
        uiCamera.position.set(uiCamera.viewportWidth / 2, uiCamera.viewportHeight / 2, 0);
        stage.getViewport().update(width, height, true);
    }

    public String getSelectedCharacter() {
        return selectedCharacter;
    }

    public void setSelectedCharacter(String selectedCharacter) {
        this.selectedCharacter = selectedCharacter;
    }

    public FileHandle getSelectedLevel() {
        return selectedLevel;
    }

    public void setSelectedLevel(FileHandle selectedLevel) {
        this.selectedLevel = selectedLevel;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public InputManager getInputManager() {
        return inputManager;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
        scoreLabel.setText(Integer.toString(score));
        if (score > highscore) {
            highscore = score;
        }
    }
    
    public void addScore(int score) {
        this.score += score;
        scoreLabel.setText(Integer.toString(this.score));
        if (this.score > highscore) {
            highscore = this.score;
        }
    }

    public Entity[][] getGrid() {
        return grid;
    }

    public int getGhostPenX() {
        return ghostPenX;
    }

    public int getGhostPenY() {
        return ghostPenY;
    }
    
    public void playBallSound() {
        soundToggle = !soundToggle;
        if (soundToggle) {
            getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/ball1.wav", Sound.class).play(.05f);
        } else {
            getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/ball2.wav", Sound.class).play(.05f);
        }
    }
    
    public void playDeathSound() {
        getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/death.wav", Sound.class).play();
    }
    
    public void playGhostSound() {
        getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/ghost return.wav", Sound.class).play();
    }
}