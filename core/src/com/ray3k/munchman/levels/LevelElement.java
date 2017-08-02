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

package com.ray3k.munchman.levels;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.AnimationStateData;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonData;
import com.ray3k.munchman.Core;
import com.ray3k.munchman.Entity;
import com.ray3k.munchman.states.LevelDesignerState;

public class LevelElement extends Entity {
    private Skeleton skeleton;
    private AnimationState animationState;
    private int cellWidth;
    private int cellHeight;
    public static enum Mode {
        MUNCH_MAN, GHOST_PEN, WALL, BALL
    }
    private Mode mode;
    private int gridX, gridY;
    
    public LevelElement(LevelDesignerState levelDesignerState) {
        super(levelDesignerState.getEntityManager(), levelDesignerState.getCore());
    }

    @Override
    public void create() {
        gridX = 0;
        gridY = 0;
    }
    
    public void initMunchMan() {
        SkeletonData skeletonData = getCore().getAssetManager().get(Core.DATA_PATH + "/spine/munchman.json", SkeletonData.class);
        skeleton = new Skeleton(skeletonData);
        skeleton.setSkin("Munchman");
        AnimationStateData animationStateData = new AnimationStateData(skeletonData);
        animationStateData.setDefaultMix(.25f);
        animationState = new AnimationState(animationStateData);
        animationState.setAnimation(0, "levelElement", true);
        
        cellWidth = 1;
        cellHeight = 1;
        mode = Mode.MUNCH_MAN;
    }
    
    public void initGhostPen() {
        SkeletonData skeletonData = getCore().getAssetManager().get(Core.DATA_PATH + "/spine/ghostpen.json", SkeletonData.class);
        skeleton = new Skeleton(skeletonData);
        AnimationStateData animationStateData = new AnimationStateData(skeletonData);
        animationStateData.setDefaultMix(.25f);
        animationState = new AnimationState(animationStateData);
        animationState.setAnimation(0, "animation", true);
        
        cellWidth = 6;
        cellHeight = 4;
        mode = Mode.GHOST_PEN;
    }
    
    public void initWall() {
        initWall("single");
    }
    
    public void initWall(String skin) {
        SkeletonData skeletonData = getCore().getAssetManager().get(Core.DATA_PATH + "/spine/wall.json", SkeletonData.class);
        skeleton = new Skeleton(skeletonData);
        skeleton.setSkin(skin);
        skeleton.findSlot("wall").getDarkColor().set(Color.RED);
        AnimationStateData animationStateData = new AnimationStateData(skeletonData);
        animationStateData.setDefaultMix(.25f);
        animationState = new AnimationState(animationStateData);
        animationState.setAnimation(0, "animation", true);
        
        cellWidth = 1;
        cellHeight = 1;
        mode = Mode.WALL;
    }
    
    public void initBall() {
        SkeletonData skeletonData = getCore().getAssetManager().get(Core.DATA_PATH + "/spine/ball.json", SkeletonData.class);
        skeleton = new Skeleton(skeletonData);
        skeleton.setSkin("ball");
        AnimationStateData animationStateData = new AnimationStateData(skeletonData);
        animationStateData.setDefaultMix(.25f);
        animationState = new AnimationState(animationStateData);
        animationState.setAnimation(0, "animation", true);
        
        cellWidth = 1;
        cellHeight = 1;
        mode = Mode.BALL;
    }

    @Override
    public void act(float delta) {
        skeleton.setPosition(getX(), getY());
        animationState.update(delta);
        skeleton.updateWorldTransform();
        animationState.apply(skeleton);
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

    public int getCellWidth() {
        return cellWidth;
    }

    public int getCellHeight() {
        return cellHeight;
    }

    public Mode getMode() {
        return mode;
    }

    public int getGridX() {
        return gridX;
    }

    public void setGridX(int gridX) {
        this.gridX = gridX;
    }

    public int getGridY() {
        return gridY;
    }

    public void setGridY(int gridY) {
        this.gridY = gridY;
    }

    public Skeleton getSkeleton() {
        return skeleton;
    }
}
