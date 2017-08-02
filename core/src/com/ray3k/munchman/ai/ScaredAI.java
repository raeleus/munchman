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

package com.ray3k.munchman.ai;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.ray3k.munchman.entities.GhostEntity;
import com.ray3k.munchman.entities.PlayerEntity;
import com.ray3k.munchman.states.GameState;
import com.ray3k.munchman.states.LevelDesignerState;

public class ScaredAI implements AI {
    private final static float ENEMY_SPEED = 50.0f;
    private final static float CHANGE_DIR_TIME = 5.0f;
    private float changeDirTimer;
    private final static float RESET_TIME = 10.0f;
    private float resetTimer;

    public ScaredAI() {
        changeDirTimer = CHANGE_DIR_TIME;
        resetTimer = RESET_TIME;
    }
    
    @Override
    public void step(float delta, GhostEntity entity, GameState gameState) {
        changeDirTimer -= delta;
        resetTimer -= delta;
        entity.setMotion(ENEMY_SPEED, entity.getDirection());
        
        int gridX = (int) ((entity.getX() - LevelDesignerState.GRID_DIMENSION / 2.0f) / LevelDesignerState.GRID_DIMENSION);
        int gridY = (int) ((entity.getY() - LevelDesignerState.GRID_DIMENSION / 2.0f) / LevelDesignerState.GRID_DIMENSION);

        if (MathUtils.isEqual(gridX * LevelDesignerState.GRID_DIMENSION, entity.getX() - LevelDesignerState.GRID_DIMENSION / 2.0f, 1f) && MathUtils.isEqual(gridY * LevelDesignerState.GRID_DIMENSION, entity.getY() - LevelDesignerState.GRID_DIMENSION / 2.0f, 1f)) {
            if (MathUtils.isEqual(entity.getDirection(), 0.0f, 1.0f) && !isGridEmpty(gridX + 1, gridY, gameState)) {
                newDirection(gridX, gridY, gameState, entity);
            } else if (MathUtils.isEqual(entity.getDirection(), 180.0f, 1.0f) && !isGridEmpty(gridX - 1, gridY, gameState)) {
                newDirection(gridX, gridY, gameState, entity);
            } else if (MathUtils.isEqual(entity.getDirection(), 90.0f, 1.0f) && !isGridEmpty(gridX, gridY + 1, gameState)) {
                newDirection(gridX, gridY, gameState, entity);
            } else if (MathUtils.isEqual(entity.getDirection(), 270.0f, 1.0f) && !isGridEmpty(gridX, gridY - 1, gameState)) {
                newDirection(gridX, gridY, gameState, entity);
            } else if (MathUtils.isEqual(entity.getSpeed(), 0.0f)) {
                newDirection(gridX, gridY, gameState, entity);
            } else if (changeDirTimer < 0 && isAtCorner(entity, gridX, gridY, gameState)) {
                changeDirTimer = CHANGE_DIR_TIME;
                newDirectionCorner(gridX, gridY, gameState, entity);
            }
        }
        
        if (resetTimer < 0) {
            entity.setAi(entity.getPrimaryAI());
            entity.setSkin(entity.getPrimarySkin());
        }
    }
    
    private void newDirection(int gridX, int gridY, GameState gameState, GhostEntity entity) {
        Array<Choice> choices = new Array<Choice>();
        if (isGridEmpty(gridX + 1, gridY, gameState)) {
            choices.add(new Choice(0));
        }
        if (isGridEmpty(gridX - 1, gridY, gameState)) {
            choices.add(new Choice(1));
        }
        if (isGridEmpty(gridX, gridY + 1, gameState)) {
            choices.add(new Choice(2));
        }
        if (isGridEmpty(gridX, gridY - 1, gameState)) {
            choices.add(new Choice(3));
        }
        
        switch (choices.random().dir) {
            case 0:
                entity.setMotion(ENEMY_SPEED, 0.0f);
                break;
            case 1:
                entity.setMotion(ENEMY_SPEED, 180.0f);
                break;
            case 2:
                entity.setMotion(ENEMY_SPEED, 90.0f);
                break;
            case 3:
                entity.setMotion(ENEMY_SPEED, 270.0f);
                break;
            default:
                break;
        }
    }
    
    private void newDirectionCorner(int gridX, int gridY, GameState gameState, GhostEntity entity) {
        Array<Choice> choices = new Array<Choice>();
        
        if (MathUtils.isEqual(entity.getDirection(), 0.0f, 1.0f) || MathUtils.isEqual(entity.getDirection(), 180.0f, 1.0f)) {
            if (isGridEmpty(gridX, gridY + 1, gameState)) {
                choices.add(new Choice(2));
            }
            if (isGridEmpty(gridX, gridY - 1, gameState)) {
                choices.add(new Choice(3));
            }
        } else if (MathUtils.isEqual(entity.getDirection(), 90.0f, 1.0f) || MathUtils.isEqual(entity.getDirection(), 270.0f, 1.0f)) {
            if (isGridEmpty(gridX + 1, gridY, gameState)) {
                choices.add(new Choice(0));
            }
            if (isGridEmpty(gridX - 1, gridY, gameState)) {
                choices.add(new Choice(1));
            }
        }
        
        switch (choices.random().dir) {
            case 0:
                entity.setMotion(ENEMY_SPEED, 0.0f);
                break;
            case 1:
                entity.setMotion(ENEMY_SPEED, 180.0f);
                break;
            case 2:
                entity.setMotion(ENEMY_SPEED, 90.0f);
                break;
            case 3:
                entity.setMotion(ENEMY_SPEED, 270.0f);
                break;
            default:
                break;
        }
    }
    
    private class Choice {
        int dir;

        public Choice(int dir) {
            this.dir = dir;
        }
    }

    @Override
    public void setPlayerGridX(int x) {
    }

    @Override
    public void setPlayerGridY(int y) {
    }

    @Override
    public void setPlayerDirection(PlayerEntity.Direction direction) {
    }

    private boolean isGridEmpty(int x, int y, GameState gameState) {
        boolean returnValue = true;
        
        if (x < 0 || y < 0 || x > gameState.getGrid().length - 1 || y > gameState.getGrid()[0].length - 1) {
            returnValue = false;
        } else {
            returnValue = gameState.getNavCells()[x][y].isWalkable();
        }
        
        return returnValue;
    }
    
    private boolean isAtCorner(GhostEntity entity, int x, int y, GameState gameState) {
        boolean returnValue = false;
        
        if (MathUtils.isEqual(entity.getDirection(), 0.0f, 1.0f) || MathUtils.isEqual(entity.getDirection(), 180.0f, 1.0f)) {
            returnValue = isGridEmpty(x, y - 1, gameState) || isGridEmpty(x, y + 1, gameState);
        } else if (MathUtils.isEqual(entity.getDirection(), 90.0f, 1.0f) || MathUtils.isEqual(entity.getDirection(), 270.0f, 1.0f)) {
            returnValue = isGridEmpty(x - 1, y, gameState) || isGridEmpty(x + 1, y, gameState);
        }
        
        return returnValue;
    }
}
