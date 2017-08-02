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
import com.badlogic.gdx.math.Vector2;
import com.ray3k.munchman.entities.GhostEntity;
import com.ray3k.munchman.entities.PlayerEntity;
import com.ray3k.munchman.states.GameState;
import com.ray3k.munchman.states.LevelDesignerState;
import java.util.List;
import org.xguzm.pathfinding.grid.GridCell;

public class RetreatAI implements AI {
    private final static float ENEMY_SPEED = 100.0f;
    private boolean foundHome;
    private float targetX;
    private float targetY;

    public RetreatAI() {
        foundHome = false;
    }
    
    @Override
    public void step(float delta, GhostEntity entity, GameState gameState) {
        if (!foundHome) {
            entity.setMotion(ENEMY_SPEED, entity.getDirection());
        } else {
            moveTowardsPoint(targetX, targetY, entity, delta);
            if (MathUtils.isEqual(entity.getX(), targetX) && MathUtils.isEqual(entity.getY(), targetY)) {
                entity.setAi(new SpawnAI());
                entity.setSkin(entity.getPrimarySkin());
            }
        }
        
        int gridX = (int) ((entity.getX() - LevelDesignerState.GRID_DIMENSION / 2.0f) / LevelDesignerState.GRID_DIMENSION);
        int gridY = (int) ((entity.getY() - LevelDesignerState.GRID_DIMENSION / 2.0f) / LevelDesignerState.GRID_DIMENSION);
        
        if (MathUtils.isEqual(gridX * LevelDesignerState.GRID_DIMENSION, entity.getX() - LevelDesignerState.GRID_DIMENSION / 2.0f, 1f) && MathUtils.isEqual(gridY * LevelDesignerState.GRID_DIMENSION, entity.getY() - LevelDesignerState.GRID_DIMENSION / 2.0f, 1f)) {
            if (!foundHome) {
                List<GridCell> path = gameState.getNavFinder().findPath(gridX, gridY, gameState.getGhostPenX(), gameState.getGhostPenY(), gameState.getNavGrid());
                if (path != null && path.size() > 0) {
                    int targetGridX = path.get(0).x;
                    int targetGridY = path.get(0).y;

                    if (gridX < targetGridX) {
                        entity.setMotion(ENEMY_SPEED, 0.0f);
                    } else if (gridX > targetGridX) {
                        entity.setMotion(ENEMY_SPEED, 180.0f);
                    } else if (gridY < targetGridY) {
                        entity.setMotion(ENEMY_SPEED, 90.0f);
                    } else if (gridY > targetGridY) {
                        entity.setMotion(ENEMY_SPEED, 270.0f);
                    }
                } else {
                    entity.setMotion(0.0f, 0.0f);
                    foundHome = true;
                    targetX = (gameState.getGhostPenX() + .5f) * LevelDesignerState.GRID_DIMENSION;
                    targetY = (gameState.getGhostPenY() - 2.5f) * LevelDesignerState.GRID_DIMENSION;
                }
            }
        }
    }

    private static Vector2 temp1 = new Vector2();
    private static Vector2 temp2 = new Vector2();
    
    public void moveTowardsPoint(float x, float y, GhostEntity entity, float delta) {
        float originalX = entity.getX();
        float originalY = entity.getY();
        
        temp1.set(entity.getX(), entity.getY());
        temp2.set(x, y);
        temp2.sub(temp1).nor();
        
        temp1.set(ENEMY_SPEED * delta, 0);
        temp1.rotate(temp2.angle());
        
        if (entity.getX() != x) {
            entity.addX(temp1.x);
        }
        if (entity.getY() != y) {
            entity.addY(temp1.y);
        }
        
        if (originalX < x && entity.getX() > x || originalX > x && entity.getX() < x) {
            entity.setX(x);
        }
        
        if (originalY < y && entity.getY() > y || originalY > y && entity.getY() < y) {
            entity.setY(y);
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

}
