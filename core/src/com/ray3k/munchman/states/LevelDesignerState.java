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
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.ray3k.munchman.Core;
import com.ray3k.munchman.EntityManager;
import com.ray3k.munchman.InputManager;
import com.ray3k.munchman.State;
import com.ray3k.munchman.levels.LevelElement;

public class LevelDesignerState extends State {
    private OrthographicCamera camera;
    private Viewport viewport;
    private InputManager inputManager;
    private Skin skin;
    private Stage stage;
    private Table table;
    private EntityManager entityManager;
    private Label modeLabel;
    private LevelElement[][] grid;
    public static final float GRID_DIMENSION = 23.0f;
    private LevelElement previewElement;
    private boolean inputFlag;
    private boolean dialogUp;
    private TextButton saveButton;
    
    private static enum Mode {
       GHOST_PEN, WALLS, MUNCHMAN, POWER_BALLS
    }
    private Mode mode;
    
    public LevelDesignerState(Core core) {
        super(core);
    }
    
    @Override
    public void start() {
        dialogUp = false;
        grid = new LevelElement[32][28];
        inputFlag = false;
        mode = Mode.GHOST_PEN;
        inputManager = new InputManager(); 
        
        camera = new OrthographicCamera();
        viewport = new ScreenViewport(camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        viewport.apply();
        
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
        
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
        
        previewElement = new LevelElement(this);
        previewElement.setDepth(-100);
        nextType();
    }
    
    private void createStageElements() {
        InputListener enterExitListener = new InputListener() {
            @Override
            public void exit(InputEvent event, float x, float y, int pointer,
                    Actor toActor) {
                super.exit(event, x, y, pointer, toActor);
                dialogUp = false;
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer,
                    Actor fromActor) {
                super.enter(event, x, y, pointer, fromActor);
                dialogUp = true;
            }
        };
        
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);
        
        modeLabel = new Label("Ghost Pen", skin);
        root.add(modeLabel).colspan(2);
        
        root.row();
        root.defaults().space(50.0f);
        TextButton textButton = new TextButton("Next Type", skin);
        root.add(textButton).expandY().bottom();
        
        textButton.addListener(enterExitListener);
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                nextType();
            }
        });
        
        saveButton = new TextButton("Save", skin);
        root.add(saveButton).expandY().bottom();
        
        saveButton.addListener(enterExitListener);
        
        saveButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                showSaveDialog();
            }
        });
    }
    
    private void showSaveDialog() {
        dialogUp = true;
        final TextField textField = new TextField("", skin);
        final Dialog dialog = new Dialog("", skin) {
            @Override
            protected void result(Object object) {
                super.result(object);
                dialogUp = false;
                saveMap(textField.getText());
            }
            
        };
        Label label = new Label("Enter a level name", skin);
        dialog.getContentTable().add(label).colspan(2);
        
        dialog.getContentTable().row();
        dialog.getContentTable().add(textField).growX().pad(25.0f).colspan(2);
        
        dialog.getContentTable().row();
        TextButton button = new TextButton("OK", skin);
        dialog.getContentTable().add(button);
        
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                dialog.hide();
                dialogUp = false;
                saveMap(textField.getText());
            }
        });
        
        button = new TextButton("Cancel", skin);
        dialog.getContentTable().add(button);
        
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                dialog.hide();
                dialogUp = false;
            }
        });
        
        dialog.key(Keys.ENTER, true);
        
        dialog.show(stage);
        stage.setKeyboardFocus(textField);
    }
    
    private void saveMap(String name) {
        FileHandle parent = new FileHandle(Core.DATA_PATH + "/levels");
        parent.mkdirs();
        
        FileHandle saveFile = parent.child(name + ".lvl");
        
        String[][] gridValues = new String[grid.length][grid[0].length];
        
        for (int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid[x].length; y++) {
                if (grid[x][y] != null) {
                    String value = grid[x][y].getMode().toString();
                    
                    if (grid[x][y].getMode() == LevelElement.Mode.WALL) {
                        value += grid[x][y].getSkeleton().getSkin().getName();
                    }
                    
                    gridValues[x][y] = value;
                }
            }
        }
        
        Json json = new Json();
        saveFile.writeString(json.toJson(gridValues), false);
        getCore().getStateManager().loadState("menu");
    }
    
    @Override
    public void draw(SpriteBatch spriteBatch, float delta) {
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        camera.update();
        getCore().getTwoColorPolygonBatch().setProjectionMatrix(camera.combined);
        getCore().getTwoColorPolygonBatch().begin();
        entityManager.draw(spriteBatch, delta);
        getCore().getTwoColorPolygonBatch().end();
        
        stage.draw();
    }

    @Override
    public void act(float delta) {
        float borderX = (Gdx.graphics.getWidth() - GRID_DIMENSION * grid.length) / 2.0f;
        float borderY = (Gdx.graphics.getHeight() - GRID_DIMENSION * grid[0].length) / 2.0f;
        int gridX = MathUtils.clamp(MathUtils.floor((Gdx.input.getX() - borderX) / GRID_DIMENSION), 0, grid.length - 1 - previewElement.getCellWidth() + 1);
        int gridY = MathUtils.clamp(MathUtils.floor((Gdx.graphics.getHeight() - Gdx.input.getY() - borderY) / GRID_DIMENSION), 0, grid[0].length - 1 - previewElement.getCellHeight() + 1);
        float x = gridX * GRID_DIMENSION + borderX;
        float y = gridY * GRID_DIMENSION + borderY;
        previewElement.setPosition(x, y);
        
        if (!dialogUp) {
            if (Gdx.input.isButtonPressed(Buttons.LEFT)) {
                if (!inputFlag) {
                    if (isCellClear(gridX, gridY)) {
                        grid[gridX][gridY] = new LevelElement(this);
                        grid[gridX][gridY].setGridX(gridX);
                        grid[gridX][gridY].setGridY(gridY);
                        grid[gridX][gridY].setPosition(x, y);
                        if (mode != null) switch (mode) {
                            case GHOST_PEN:
                                grid[gridX][gridY].initGhostPen();
                                nextType();
                                inputFlag = true;
                                break;
                            case WALLS:
                                grid[gridX][gridY].initWall();
                                updateWall(gridX, gridY);
                                updateWall(gridX - 1, gridY);
                                updateWall(gridX + 1, gridY);
                                updateWall(gridX, gridY - 1);
                                updateWall(gridX, gridY + 1);
                                break;
                            case MUNCHMAN:
                                grid[gridX][gridY].initMunchMan();
                                nextType();
                                inputFlag = true;
                                break;
                            case POWER_BALLS:
                                grid[gridX][gridY].initBall();
                                inputFlag = true;
                                break;
                            default:
                                break;
                        }
                    }
                }
            } else if (Gdx.input.isButtonPressed(Buttons.RIGHT)) {
                if (!inputFlag) {
                    LevelElement element = getElement(gridX, gridY);
                    if (element != null) {
                        element.dispose();
                        grid[element.getGridX()][element.getGridY()] = null;
                        
                        if (element.getMode() == LevelElement.Mode.GHOST_PEN) {
                            nextType();
                            inputFlag = true;
                        } else if (element.getMode() == LevelElement.Mode.MUNCH_MAN) {
                            nextType();
                            inputFlag = true;
                        }

                        updateWall(gridX, gridY);
                        updateWall(gridX - 1, gridY);
                        updateWall(gridX + 1, gridY);
                        updateWall(gridX, gridY - 1);
                        updateWall(gridX, gridY + 1);
                    }
                }
            } else {
                inputFlag = false;
            }
        }
        
        entityManager.act(delta);
        
        stage.act(delta);
    }
    
    private boolean isCellClear(int gridX, int gridY) {
        
        return getElement(gridX, gridY) == null;
    }
    
    private LevelElement getElement(int gridX, int gridY) {
        LevelElement returnValue = grid[gridX][gridY];
        
        if (returnValue == null) {
            Rectangle rectangle = new Rectangle();
            for (int x = 0; x < grid.length; x++) {
                for (int y = 0; y < grid[x].length; y++) {
                    LevelElement element = grid[x][y];
                    if (element != null) {
                        rectangle.setPosition(x, y);
                        rectangle.setSize(element.getCellWidth(), element.getCellHeight());
                        if (rectangle.contains(gridX + .5f, gridY + .5f)) {
                            returnValue = element;
                            break;
                        }
                    }
                }
            }
        }
        
        return returnValue;
    }
    
    private void updateWall(int gridX, int gridY) {
        if (wallExists(gridX, gridY)) {
            boolean left = wallExists(gridX - 1, gridY);
            boolean right = wallExists(gridX + 1, gridY);
            boolean top = wallExists(gridX, gridY + 1);
            boolean bottom = wallExists(gridX, gridY - 1);
            
            if (left && !right && !top && !bottom) {
                grid[gridX][gridY].initWall("right");
            } else if (!left && right && !top && !bottom) {
                grid[gridX][gridY].initWall("left");
            } else if (left && right && !top && !bottom) {
                grid[gridX][gridY].initWall("horizontal");
            } else if (!left && !right && top && !bottom) {
                grid[gridX][gridY].initWall("bottom");
            } else if (!left && right && top && !bottom) {
                grid[gridX][gridY].initWall("bottom-left");
            } else if (left && !right && top && !bottom) {
                grid[gridX][gridY].initWall("bottom-right");
            } else if (left && right && top && bottom) {
                grid[gridX][gridY].initWall("four-way");
            } else if (left && right && top && !bottom) {
                grid[gridX][gridY].initWall("t-bottom");
            } else if (!left && right && top && bottom) {
                grid[gridX][gridY].initWall("t-left");
            } else if (left && !right && top && bottom) {
                grid[gridX][gridY].initWall("t-right");
            } else if (left && right && !top && bottom) {
                grid[gridX][gridY].initWall("t-top");
            } else if (!left && !right && !top && bottom) {
                grid[gridX][gridY].initWall("top");
            } else if (!left && right && !top && bottom) {
                grid[gridX][gridY].initWall("top-left");
            } else if (left && !right && !top && bottom) {
                grid[gridX][gridY].initWall("top-right");
            } else if (!left && !right && top && bottom) {
                grid[gridX][gridY].initWall("vertical");
            } else {
                grid[gridX][gridY].initWall("single");
            }
        }
    }
    
    private boolean wallExists(int gridX, int gridY) {
        return gridX < grid.length && gridX >= 0 && gridY < grid[0].length && gridY >= 0 && grid[gridX][gridY] != null && grid[gridX][gridY].getMode() == LevelElement.Mode.WALL;
    }
    
    private void nextType() {
        if (findElement(LevelElement.Mode.GHOST_PEN) == null) {
            mode = Mode.GHOST_PEN;
            previewElement.initGhostPen();
            modeLabel.setText("Ghost Pen");
            previewElement.getSkeleton().findSlot("ghost pen").getColor().set(Color.PURPLE);
            saveButton.setDisabled(true);
        } else if (findElement(LevelElement.Mode.MUNCH_MAN) == null) {
            mode = Mode.MUNCHMAN;
            previewElement.initMunchMan();
            modeLabel.setText("Munch Man");
            previewElement.getSkeleton().findSlot("bottom").getColor().set(Color.PURPLE);
            previewElement.getSkeleton().findSlot("top").getColor().set(Color.PURPLE);
            saveButton.setDisabled(true);
        } else if (mode == Mode.WALLS) {
            mode = Mode.POWER_BALLS;
            previewElement.initBall();
            modeLabel.setText("Power Balls");
            previewElement.getSkeleton().findSlot("point").getColor().set(Color.PURPLE);
            saveButton.setDisabled(false);
        } else {
            mode = Mode.WALLS;
            previewElement.initWall();
            modeLabel.setText("Walls");
            previewElement.getSkeleton().findSlot("wall").getDarkColor().set(Color.PURPLE);
            saveButton.setDisabled(false);
        }
    }
    
    private LevelElement findElement(LevelElement.Mode mode) {
        LevelElement returnValue = null;
        
        for (int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid[x].length; y++) {
                LevelElement element = grid[x][y];
                if (element != null && element.getMode() == mode) {
                    returnValue = element;
                    break;
                }
            }
        }
        
        return returnValue;
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
        viewport.update(width, height);
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
        stage.getViewport().update(width, height, true);
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public InputManager getInputManager() {
        return inputManager;
    }
}
