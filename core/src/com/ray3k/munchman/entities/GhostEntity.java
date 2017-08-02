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

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.AnimationStateData;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonBounds;
import com.esotericsoftware.spine.SkeletonData;
import com.ray3k.munchman.Core;
import com.ray3k.munchman.Entity;
import com.ray3k.munchman.ai.AI;
import com.ray3k.munchman.ai.SpawnAI;
import com.ray3k.munchman.states.GameState;

public class GhostEntity extends Entity implements PlayerGridListener {
    private Skeleton skeleton;
    private AnimationState animationState;
    private SkeletonBounds skeletonBounds;
    private GameState gameState;
    private AI ai;
    private AI primaryAI;
    private String primarySkin;

    public GhostEntity(final GameState gameState) {
        super(gameState.getEntityManager(), gameState.getCore());
        this.gameState = gameState;
        setDepth(-10);
        SkeletonData skeletonData = getCore().getAssetManager().get(Core.DATA_PATH + "/spine/ghost.json", SkeletonData.class);
        skeleton = new Skeleton(skeletonData);
        skeleton.setSkin("blue");
        AnimationStateData animationStateData = new AnimationStateData(skeletonData);
        animationStateData.setDefaultMix(.25f);
        animationState = new AnimationState(animationStateData);
        animationState.setAnimation(0, "walk", true);
        
        skeletonBounds = new SkeletonBounds();
        skeletonBounds.update(skeleton, true);
        
        ai = new SpawnAI();
    }

    @Override
    public void newGridPointReached(int gridX, int gridY, PlayerEntity.Direction direction) {
        ai.setPlayerGridX(gridX);
        ai.setPlayerGridY(gridY);
        ai.setPlayerDirection(direction);
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
        
        ai.step(delta, this, gameState);
        
        skeletonBounds.update(skeleton, true);
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

    public AI getAi() {
        return ai;
    }

    public void setAi(AI ai) {
        this.ai = ai;
    }

    public AI getPrimaryAI() {
        return primaryAI;
    }

    public void setPrimaryAI(AI primaryAI) {
        this.primaryAI = primaryAI;
    }
    
    public void setSkin(String skin) {
        skeleton.setBonesToSetupPose();
        skeleton.setSkin(skin);
    }

    public String getPrimarySkin() {
        return primarySkin;
    }

    public void setPrimarySkin(String primarySkin) {
        this.primarySkin = primarySkin;
    }

    public SkeletonBounds getSkeletonBounds() {
        return skeletonBounds;
    }
}
