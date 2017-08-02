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

package com.ray3k.munchman.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.AnimationStateData;
import com.esotericsoftware.spine.Event;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonBounds;
import com.esotericsoftware.spine.SkeletonData;
import com.ray3k.munchman.Core;
import com.ray3k.munchman.Entity;
import com.ray3k.munchman.ai.RetreatAI;
import com.ray3k.munchman.ai.ScaredAI;
import com.ray3k.munchman.ai.SpawnAI;
import com.ray3k.munchman.states.GameState;
import com.ray3k.munchman.states.LevelDesignerState;

public class PlayerEntity extends Entity {
    private Skeleton skeleton;
    private AnimationState animationState;
    private SkeletonBounds skeletonBounds;
    private GameState gameState;
    private final static float PLAYER_SPEED = 100.0f;
    public static enum Direction {
        LEFT, RIGHT, UP, DOWN, NONE
    }
    private Direction desDirection;
    private Direction queuedDirection;
    private int targetGridX;
    private int targetGridY;
    private Array<PlayerGridListener> playerGridListeners;
    private boolean hit;

    public PlayerEntity(final GameState gameState) {
        super(gameState.getEntityManager(), gameState.getCore());
        setDepth(-100);
        playerGridListeners = new Array<PlayerGridListener>();
        this.gameState = gameState;
        SkeletonData skeletonData = getCore().getAssetManager().get(Core.DATA_PATH + "/spine/munchman.json", SkeletonData.class);
        skeleton = new Skeleton(skeletonData);
        skeleton.setSkin("Munchman");
        AnimationStateData animationStateData = new AnimationStateData(skeletonData);
        animationStateData.setDefaultMix(.25f);
        animationState = new AnimationState(animationStateData);
        animationState.setAnimation(0, "standing", true);
        animationState.addListener(new AnimationState.AnimationStateAdapter() {
            @Override
            public void event(AnimationState.TrackEntry entry, Event event) {
                if (event.getData().getName().equals("gameover")) {
                    new GameOverTimerEntity(gameState, 3.0f);
                    PlayerEntity.this.dispose();
                }
            }
            
        });
        
        skeletonBounds = new SkeletonBounds();
        skeletonBounds.update(skeleton, true);
        desDirection = Direction.NONE;
        queuedDirection = Direction.NONE;
        hit = false;
    }
    
    @Override
    public void create() {
    }

    @Override
    public void act(float delta) {
        skeleton.setPosition(getX(), getY());
        animationState.update(delta);
        skeleton.updateWorldTransform();
        animationState.apply(skeleton);
        
        if (!hit) {
            if (Gdx.input.isKeyJustPressed(Keys.RIGHT)) {
                if (isGridEmpty(targetGridX + 1, targetGridY)) {
                    desDirection = Direction.RIGHT;
                    queuedDirection = Direction.NONE;
                } else {
                    queuedDirection = Direction.RIGHT;
                }
            } else if (Gdx.input.isKeyJustPressed(Keys.LEFT)) {
                if (isGridEmpty(targetGridX - 1, targetGridY)) {
                    desDirection = Direction.LEFT;
                    queuedDirection = Direction.NONE;
                } else {
                    queuedDirection = Direction.LEFT;
                }
            } else if (Gdx.input.isKeyJustPressed(Keys.UP)) {
                if (isGridEmpty(targetGridX, targetGridY + 1)) {
                    desDirection = Direction.UP;
                    queuedDirection = Direction.NONE;
                } else {
                    queuedDirection = Direction.UP;
                }
            } else if (Gdx.input.isKeyJustPressed(Keys.DOWN)) {
                if (isGridEmpty(targetGridX, targetGridY - 1)) {
                    desDirection = Direction.DOWN;
                    queuedDirection = Direction.NONE;
                } else {
                    queuedDirection = Direction.DOWN;
                }
            }

            if (MathUtils.isEqual(targetGridX * LevelDesignerState.GRID_DIMENSION, getX() - LevelDesignerState.GRID_DIMENSION / 2.0f) && MathUtils.isEqual(targetGridY * LevelDesignerState.GRID_DIMENSION, getY() - LevelDesignerState.GRID_DIMENSION / 2.0f)) {
                switch (desDirection) {
                    case RIGHT:
                        if (isGridEmpty(targetGridX + 1, targetGridY)) {
                            targetGridX++;
                            setMotion(PLAYER_SPEED, 0.0f);
                            if (animationState.getCurrent(0).getAnimation().getName().equals("standing")) {
                                animationState.setAnimation(0, "walk", true);
                            }
                            skeleton.getRootBone().setRotation(0.0f);
                            skeleton.setFlipX(false);
                        } else {
                            setMotion(0.0f, 0.0f);
                            animationState.setAnimation(0, "standing", true);
                            skeleton.getRootBone().setRotation(0.0f);
                            skeleton.setFlipX(false);
                        }
                        break;
                    case LEFT:
                        if (isGridEmpty(targetGridX - 1, targetGridY)) {
                            targetGridX--;
                            setMotion(PLAYER_SPEED, 180.0f);
                            if (animationState.getCurrent(0).getAnimation().getName().equals("standing")) {
                                animationState.setAnimation(0, "walk", true);
                            }
                            skeleton.getRootBone().setRotation(0.0f);
                            skeleton.setFlipX(true);
                        } else {
                            setMotion(0.0f, 0.0f);
                            animationState.setAnimation(0, "standing", true);
                            skeleton.getRootBone().setRotation(0.0f);
                            skeleton.setFlipX(true);
                        }
                        break;
                    case UP:
                        if (isGridEmpty(targetGridX, targetGridY + 1)) {
                            targetGridY++;
                            setMotion(PLAYER_SPEED, 90.0f);
                            if (animationState.getCurrent(0).getAnimation().getName().equals("standing")) {
                                animationState.setAnimation(0, "walk", true);
                            }
                            skeleton.getRootBone().setRotation(90.0f);
                            skeleton.setFlipX(false);
                        } else {
                            setMotion(0.0f, 0.0f);
                            animationState.setAnimation(0, "standing", true);
                            skeleton.getRootBone().setRotation(90.0f);
                            skeleton.setFlipX(false);
                        }
                        break;
                    case DOWN:
                        if (isGridEmpty(targetGridX, targetGridY - 1)) {
                            targetGridY--;
                            setMotion(PLAYER_SPEED, 270.0f);
                            if (animationState.getCurrent(0).getAnimation().getName().equals("standing")) {
                                animationState.setAnimation(0, "walk", true);
                            }
                            skeleton.getRootBone().setRotation(270.0f);
                            skeleton.setFlipX(false);
                        } else {
                            setMotion(0.0f, 0.0f);
                            animationState.setAnimation(0, "standing", true);
                            skeleton.getRootBone().setRotation(270.0f);
                            skeleton.setFlipX(false);
                        }
                        break;
                }
            }
            
            if (queuedDirection != Direction.NONE) {
                switch (queuedDirection) {
                    case RIGHT:
                        if (isGridEmpty(targetGridX + 1, targetGridY)) {
                            desDirection = Direction.RIGHT;
                            queuedDirection = Direction.NONE;
                        }
                        break;
                    case LEFT:
                        if (isGridEmpty(targetGridX - 1, targetGridY)) {
                            desDirection = Direction.LEFT;
                            queuedDirection = Direction.NONE;
                        }
                        break;
                    case UP:
                        if (isGridEmpty(targetGridX, targetGridY + 1)) {
                            desDirection = Direction.UP;
                            queuedDirection = Direction.NONE;
                        }
                        break;
                    case DOWN:
                        if (isGridEmpty(targetGridX, targetGridY - 1)) {
                            desDirection = Direction.DOWN;
                            queuedDirection = Direction.NONE;
                        }
                        break;
                }
            }
            
            firePlayerGridEvent(targetGridX, targetGridY);
        }
        
        skeletonBounds.update(skeleton, true);
        if (!hit) {
            for (Entity entity : gameState.getEntityManager().getEntities()) {
                if (entity instanceof DotEntity) {
                    if (skeletonBounds.aabbContainsPoint(entity.getX() + LevelDesignerState.GRID_DIMENSION / 2.0f, entity.getY() + LevelDesignerState.GRID_DIMENSION / 2.0f)) {
                        entity.dispose();
                        gameState.addScore(10);
                        gameState.playBallSound();
                        if (DotEntity.dotCount <= 0) {
                            new CongratulationsTimerEntity(gameState, 2.0f);
                        }
                    }
                } else if (entity instanceof PowerBallEntity) {
                    if (skeletonBounds.aabbContainsPoint(entity.getX() + LevelDesignerState.GRID_DIMENSION / 2.0f, entity.getY() + LevelDesignerState.GRID_DIMENSION / 2.0f)) {
                        entity.dispose();
                        gameState.makeGhostsScared();
                        gameState.playBallSound();
                    }
                } else if (entity instanceof GhostEntity) {
                    if (skeletonBounds.aabbIntersectsSkeleton(((GhostEntity) entity).getSkeletonBounds())) {
                        if (((GhostEntity) entity).getAi() instanceof ScaredAI) {
                            ((GhostEntity) entity).setAi(new RetreatAI());
                            ((GhostEntity) entity).setSkin("dead");
                            gameState.playGhostSound();
                        } else if (!(((GhostEntity) entity).getAi() instanceof RetreatAI) && !(((GhostEntity) entity).getAi() instanceof SpawnAI)) {
                            gameState.makeGhostsRandom();
                            animationState.setAnimation(0, "die", false);
                            setMotion(0.0f, 0.0f);
                            skeleton.getRootBone().setRotation(0.0f);
                            hit = true;
                            gameState.playDeathSound();
                        }
                    }
                }
            }
        }
    }
    
    private boolean isGridEmpty(int x, int y) {
        boolean returnValue = true;
        
        if (x < 0 || y < 0 || x > gameState.getGrid().length - 1 || y > gameState.getGrid()[0].length - 1) {
            returnValue = false;
        } else {
            returnValue = gameState.getNavCells()[x][y].isWalkable();
        }
        
        return returnValue;
    }

    @Override
    public void act_end(float delta) {
    }

    @Override
    public void draw(SpriteBatch spriteBatch, float delta) {
        getCore().getSkeletonRenderer().draw(getCore().getTwoColorPolygonBatch(), skeleton);
    }

    @Override
    public void destroy() {
    }

    @Override
    public void collision(Entity other) {
    }

    public int getTargetGridX() {
        return targetGridX;
    }

    public void setTargetGridX(int targetGridX) {
        this.targetGridX = targetGridX;
    }

    public int getTargetGridY() {
        return targetGridY;
    }

    public void setTargetGridY(int targetGridY) {
        this.targetGridY = targetGridY;
    }
    
    public void addPlayerGridListener(PlayerGridListener listener) {
        playerGridListeners.add(listener);
    }
    
    private void firePlayerGridEvent(int gridX, int gridY) {
        for (PlayerGridListener listener : playerGridListeners) {
            Direction direction = queuedDirection != Direction.NONE ? queuedDirection : desDirection;
            if (MathUtils.isZero(getSpeed())) direction = Direction.NONE;
            listener.newGridPointReached(gridX, gridY, direction);
        }
    }
    public void setSkin(String name) {
        skeleton.setSkin(name);
    }
}
