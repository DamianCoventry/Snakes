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
// Designed and implemented for Massey University course 159.261 Game Programming (Assignment 1)
//

package com.snakegame.rules;

import com.snakegame.client.Texture;

import java.io.IOException;

public interface IGameWorld {
    void close();
    void reset(long nowMs) throws IOException;

    enum Mode { SINGLE_PLAYER, TWO_PLAYERS}
    Mode getMode();

    GameField getGameField();
    Snake[] getSnakes();
    Texture getGameOverTexture();
    Texture getGameWonTexture();
    Texture getGetReadyTexture();
    Texture getPlayer1DiedTexture();
    Texture getPlayer2DiedTexture();
    Texture getBothPlayersDiedTexture();

    enum SubtractSnakeResult {SNAKE_AVAILABLE, NO_SNAKES_REMAIN}
    SubtractSnakeResult subtractSnake(int player);

    void start(long nowMs);
    void stop(long nowMs);
    void think(long nowMs) throws IOException;
    void draw3d(long nowMs);
    void draw2d(long nowMs);
}