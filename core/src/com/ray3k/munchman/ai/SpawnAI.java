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

public class SpawnAI implements AI {
    private final static float ENEMY_SPEED = 10.0f;
    
    @Override
    public void step(float delta, GhostEntity entity, GameState gameState) {
        entity.setMotion(ENEMY_SPEED, 90.0f);
        
        int gridX = (int) ((entity.getX() - LevelDesignerState.GRID_DIMENSION / 2.0f) / LevelDesignerState.GRID_DIMENSION);
        int gridY = (int) ((entity.getY() - LevelDesignerState.GRID_DIMENSION / 2.0f) / LevelDesignerState.GRID_DIMENSION);

        if (MathUtils.isEqual(gridX * LevelDesignerState.GRID_DIMENSION, entity.getX() - LevelDesignerState.GRID_DIMENSION / 2.0f, 1f) && MathUtils.isEqual(gridY * LevelDesignerState.GRID_DIMENSION, entity.getY() - LevelDesignerState.GRID_DIMENSION / 2.0f, 1f)) {
            if (isGridEmpty(gridX, gridY, gameState)) {
                entity.setX(gridX * LevelDesignerState.GRID_DIMENSION + LevelDesignerState.GRID_DIMENSION / 2.0f);
                entity.setY(gridY * LevelDesignerState.GRID_DIMENSION + LevelDesignerState.GRID_DIMENSION / 2.0f);
                entity.setMotion(0.0f, 0.0f);
                entity.setAi(entity.getPrimaryAI());
            }
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
}
