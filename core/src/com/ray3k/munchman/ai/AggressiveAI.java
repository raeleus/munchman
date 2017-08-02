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
import com.ray3k.munchman.entities.GhostEntity;
import com.ray3k.munchman.entities.PlayerEntity;
import com.ray3k.munchman.states.GameState;
import com.ray3k.munchman.states.LevelDesignerState;
import java.util.List;
import org.xguzm.pathfinding.grid.GridCell;

public class AggressiveAI implements AI {
    private final static float ENEMY_SPEED = 100.0f;
    private int playerGridX;
    private int playerGridY;
    
    @Override
    public void step(float delta, GhostEntity entity, GameState gameState) {
        int gridX = (int) ((entity.getX() - LevelDesignerState.GRID_DIMENSION / 2.0f) / LevelDesignerState.GRID_DIMENSION);
        int gridY = (int) ((entity.getY() - LevelDesignerState.GRID_DIMENSION / 2.0f) / LevelDesignerState.GRID_DIMENSION);

        if (MathUtils.isEqual(gridX * LevelDesignerState.GRID_DIMENSION, entity.getX() - LevelDesignerState.GRID_DIMENSION / 2.0f, 1) && MathUtils.isEqual(gridY * LevelDesignerState.GRID_DIMENSION, entity.getY() - LevelDesignerState.GRID_DIMENSION / 2.0f, 1)) {
            List<GridCell> path = gameState.getNavFinder().findPath(gridX, gridY, playerGridX, playerGridY, gameState.getNavGrid());
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
            }
        }
    }

    @Override
    public void setPlayerGridX(int x) {
        playerGridX = x;
    }

    @Override
    public void setPlayerGridY(int y) {
        playerGridY = y;
    }

    @Override
    public void setPlayerDirection(PlayerEntity.Direction direction) {
    }

}
