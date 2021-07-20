//
// Snake Game
// https://en.wikipedia.org/wiki/Snake_(video_game_genre)
//
// Based on the 1976 arcade game Blockade, and the 1991 game Nibbles
// https://en.wikipedia.org/wiki/Blockade_(video_game)
// https://en.wikipedia.org/wiki/Nibbles_(video_game)
//
// This implementation is Copyright (c) 2021, Damian Coventry
// All rights reserved
// Written for Massey University course 159.261 Game Programming (Assignment 1)
//

package com.snakegame.application;

import com.snakegame.client.*;
import com.snakegame.opengl.GLTexture;
import com.snakegame.rules.IGameController;

import javax.imageio.ImageIO;
import java.io.*;

public class GameWonAppState implements IAppState {
    private final IAppStateContext m_AppStateContext;
    private final IGameView m_View;
    private final int m_Player;
    private final boolean m_BothSnakes;
    private GLTexture m_GameWonTexture;

    public GameWonAppState(IAppStateContext context, int player) {
        m_AppStateContext = context;
        m_View = m_AppStateContext.getView();
        m_Player = player;
        m_BothSnakes = false;
    }

    public GameWonAppState(IAppStateContext context) {
        m_AppStateContext = context;
        m_View = m_AppStateContext.getView();
        m_Player = -1;
        m_BothSnakes = true;
    }

    @Override
    public void begin(long nowMs) throws IOException {
        if (m_AppStateContext.getController().getMode() == IGameController.Mode.TWO_PLAYERS) {
            if (m_BothSnakes) {
                long p0 = m_AppStateContext.getController().getSnakes()[0].getPoints();
                long p1 = m_AppStateContext.getController().getSnakes()[1].getPoints();
                if (p0 > p1) {
                    m_GameWonTexture = new GLTexture(ImageIO.read(new File("images\\GameWonByPlayer1.png")));
                }
                else if (p1 > p0) {
                    m_GameWonTexture = new GLTexture(ImageIO.read(new File("images\\GameWonByPlayer2.png")));
                }
                else {
                    m_GameWonTexture = new GLTexture(ImageIO.read(new File("images\\GameWonByBothPlayers.png")));
                }
            } else if (m_Player == 0) {
                m_GameWonTexture = new GLTexture(ImageIO.read(new File("images\\GameWonByPlayer1.png")));
            } else {
                m_GameWonTexture = new GLTexture(ImageIO.read(new File("images\\GameWonByPlayer2.png")));
            }
        }
        else {
            m_GameWonTexture = new GLTexture(ImageIO.read(new File("images\\GameWonByPlayer1.png")));
        }
        m_AppStateContext.addTimeout(3500, (callCount) -> {
            m_AppStateContext.changeState(new RunningMenuAppState(m_AppStateContext));
            return TimeoutManager.CallbackResult.REMOVE_THIS_CALLBACK;
        });
    }

    @Override
    public void end(long nowMs) {
        m_GameWonTexture.freeNativeResource();
    }

    @Override
    public void processKey(long window, int key, int scanCode, int action, int mods) {
        // No work to do
    }

    @Override
    public void think(long nowMs) throws IOException {
        // No work to do
    }

    @Override
    public void draw3d(long nowMs) {
        m_View.draw3d(nowMs);
    }

    @Override
    public void draw2d(long nowMs) {
        m_View.draw2d(nowMs);
        m_View.drawCenteredImage(m_GameWonTexture);
    }
}
