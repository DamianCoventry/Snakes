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

package com.snakegame.rules;

import com.snakegame.application.*;
import com.snakegame.client.*;

import java.io.*;
import java.util.*;

// https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93controller
public class GameController implements IGameController {
    private static final int s_MaxPlayers = 2;
    private static final long s_SlowestSnakeSpeed = 200;
    private static final long s_FastestSnakeSpeed = 125;
    private static final long s_SnakeSpeedPowerUpAdjustment = 15;
    private static final long s_SnakeSpeedLevelAdjustment = 12;
    private static final long s_PowerUpInitialTimeoutMs = 3750;
    private static final long s_PowerUpSubsequentTimeoutMs = 15000;
    private static final long s_PowerUpExpireTimeoutMs = 6000;
    private static final long s_InsertWallsTimeoutMs = 12000;

    private final IAppStateContext m_Context;
    private final Random m_Rng;

    private ArrayList<String> m_LevelFileNames;
    private Snake[] m_Snakes;
    private GameField m_GameField;
    private PowerUp m_PowerUp;
    private Number m_Number;
    private Mode m_Mode;

    private long m_SnakeTimeoutMs;
    private int m_SnakeTimeoutId;
    private int m_PowerUpTimeoutId;
    private int m_WallsTimeoutId;
    private int m_CurrentLevel;

    public GameController(IAppStateContext context) {
        m_Context = context;
        m_Rng = new Random();
        m_Mode = Mode.SINGLE_PLAYER; // startNewGame() will change this
        m_Snakes = null; // startNewGame() will allocate this
        m_CurrentLevel = 0;
        m_SnakeTimeoutId = 0;
        m_PowerUpTimeoutId = 0;
        discoverLevelFiles();
    }

    @Override
    public Mode getMode() {
        return m_Mode;
    }

    @Override
    public GameField getGameField() {
        return m_GameField;
    }

    @Override
    public Snake[] getSnakes() {
        return m_Snakes;
    }

    @Override
    public SubtractSnakeResult subtractSnake(int player) {
        if (player < 0 || player > s_MaxPlayers - 1) {
            throw new RuntimeException("Invalid player index");
        }
        if (m_Snakes[player].getNumLives() == 0) {
            return SubtractSnakeResult.NO_SNAKES_REMAIN;
        }
        m_Snakes[player].decrementLives();
        return SubtractSnakeResult.SNAKE_AVAILABLE;
    }

    @Override
    public boolean isLastLevel() {
        return m_CurrentLevel == m_LevelFileNames.size() - 1;
    }

    @Override
    public int getCurrentLevel() {
        return m_CurrentLevel;
    }

    @Override
    public int getLevelCount() {
        return m_LevelFileNames.size();
    }

    @Override
    public void startNewGame(long nowMs, Mode mode) throws IOException {
        m_CurrentLevel = 0;
        m_Mode = mode;

        Vector2i minBounds = new Vector2i(0, 0);
        Vector2i maxBounds = new Vector2i(GameField.WIDTH - 1, GameField.HEIGHT - 1);

        m_Snakes = new Snake[m_Mode == Mode.TWO_PLAYERS ? 2 : 1];
        m_Snakes[0] = new Snake(0, m_Context.getView(), Snake.Direction.Right, minBounds, maxBounds);
        if (m_Mode == Mode.TWO_PLAYERS) {
            m_Snakes[1] = new Snake(1, m_Context.getView(), Snake.Direction.Left, minBounds, maxBounds);
        }

        loadLevelFile(m_CurrentLevel);
    }

    @Override
    public void loadNextLevel(long nowMs) throws IOException {
        if (m_CurrentLevel < m_LevelFileNames.size() - 1) {
            ++m_CurrentLevel;
        }
        loadLevelFile(m_CurrentLevel);
    }

    @Override
    public void resetAfterSnakeDeath(long nowMs) {
        m_GameField.clearPowerUpsAndNumbers();
        insertNumber(Number.Type.NUM_1);
        for (var snake : m_Snakes) {
            snake.moveToStartPosition();
        }
    }

    @Override
    public void start(long nowMs) {
        stop(nowMs);
        scheduleSnakeMovement();
        scheduleInsertPowerUp(s_PowerUpInitialTimeoutMs);
        scheduleInsertWalls();
    }

    @Override
    public void stop(long nowMs) {
        removeSnakeMovementTimeout();
        removePowerUpTimeout();
        removeWallsTimeout();
    }

    @Override
    public void think(long nowMs) {
        // No work to do
    }

    private void discoverLevelFiles() {
        m_LevelFileNames = new ArrayList<>();
        File directory = new File("levels");
        for (var file : Objects.requireNonNull(directory.listFiles())) {
            if (file.isFile() && file.getName().endsWith(".txt")) {
                m_LevelFileNames.add(file.getName());
            }
        }
        if (m_LevelFileNames.isEmpty()) {
            throw new RuntimeException("There are no level files");
        }
        m_LevelFileNames.sort(String::compareToIgnoreCase);
    }

    private void insertPowerUp(PowerUp.Type type) {
        if (m_PowerUp != null) {
            m_GameField.removePowerUp(m_PowerUp);
        }
        m_PowerUp = new PowerUp(type, chooseRandomEmptyCell());
        m_GameField.insertPowerUp(m_PowerUp);
    }

    private void insertNumber(Number.Type type) {
        if (m_Number != null) {
            m_GameField.removeNumber(m_Number);
        }
        m_Number = new Number(type, chooseRandomEmptyCell());
        m_GameField.insertNumber(m_Number);
    }

    private void loadLevelFile(int level) throws IOException {
        GameFieldFile file = new GameFieldFile("levels/" + m_LevelFileNames.get(level), m_Mode == Mode.TWO_PLAYERS);
        m_GameField = file.getGameField();

        moveSnakesToNewStartPositions();
        setSnakeMovementSpeedForCurrentLevel();
        insertNumber(Number.Type.NUM_1);

        m_Context.getView().setAppStateContext(m_Context);
    }

    private void setSnakeMovementSpeedForCurrentLevel() {
        m_SnakeTimeoutMs = Math.max(s_FastestSnakeSpeed, s_SlowestSnakeSpeed - (s_SnakeSpeedLevelAdjustment * m_CurrentLevel));
    }

    private void moveSnakesToNewStartPositions() {
        m_Snakes[0].setStartPosition(m_GameField.getPlayer1Start());
        m_Snakes[0].moveToStartPosition();
        if (m_Mode == Mode.TWO_PLAYERS) {
            m_Snakes[1].setStartPosition(m_GameField.getPlayer2Start());
            m_Snakes[1].moveToStartPosition();
        }
    }

    private void scheduleSnakeMovement() {
        m_SnakeTimeoutId = m_Context.addTimeout(m_SnakeTimeoutMs, (callCount) -> {
            moveSnakesForwards();
            CollisionResult r = performCollisionDetection();
            if (r.collisionOccurred()) {
                if (r.getResult() == CollisionResult.Result.BOTH_SNAKES) {
                    m_Context.changeState(new SnakeDyingAppState(m_Context));
                }
                else {
                    m_Context.changeState(new SnakeDyingAppState(m_Context, r.getPlayer()));
                }
            }
            return TimeoutManager.CallbackResult.KEEP_CALLING;
        });
    }

    private void refreshSnakeMovementTimeout() {
        removeSnakeMovementTimeout();
        scheduleSnakeMovement();
    }

    private void scheduleInsertPowerUp(long timeoutMs) {
        removePowerUpTimeout();
        m_PowerUpTimeoutId = m_Context.addTimeout(timeoutMs, (callCount) -> {
            insertRandomPowerUp();
            return TimeoutManager.CallbackResult.REMOVE_THIS_CALLBACK;
        });
    }

    private PowerUp.Type chooseRandomPowerUpType(boolean includeTheRandomPowerUp) {
        ArrayList<PowerUp.Type> powerUps = new ArrayList<>(8);
        if (isIncreaseSpeedPowerUpRelevant()) {
            powerUps.add(PowerUp.Type.INC_SPEED);
        }
        if (isDecreaseSpeedPowerUpRelevant()) {
            powerUps.add(PowerUp.Type.DEC_SPEED);
        }
        if (isIncreaseLivesPowerUpRelevant()) {
            powerUps.add(PowerUp.Type.INC_LIVES);
        }
        if (isDecreaseLivesPowerUpRelevant()) {
            powerUps.add(PowerUp.Type.DEC_LIVES);
        }
        if (isDecreasePointsPowerUpRelevant()) {
            powerUps.add(PowerUp.Type.DEC_POINTS);
        }
        if (isDecreaseLengthPowerUpRelevant()) {
            powerUps.add(PowerUp.Type.DEC_LENGTH);
        }
        // Increase points is always relevant
        powerUps.add(PowerUp.Type.INC_POINTS);
        if (includeTheRandomPowerUp) {
            powerUps.add(PowerUp.Type.RANDOM);
        }
        return powerUps.get(m_Rng.nextInt(powerUps.size()));
    }

    private boolean isIncreaseSpeedPowerUpRelevant() {
        // If the snakes are moving slower than the fastest speed then this power up is relevant
        return m_SnakeTimeoutMs > s_FastestSnakeSpeed;
    }

    private boolean isDecreaseSpeedPowerUpRelevant() {
        // If the snakes are moving faster than the slowest speed then this power up is relevant
        return m_SnakeTimeoutMs < s_SlowestSnakeSpeed;
    }

    private boolean isIncreaseLivesPowerUpRelevant() {
        // If either snake has fewer than the max remaining lives then this power up is relevant
        return m_Snakes[0].getNumLives() < Snake.s_MaxNumLives ||
                (m_Snakes.length > 1 && m_Snakes[1].getNumLives() < Snake.s_MaxNumLives);
    }

    private boolean isDecreaseLivesPowerUpRelevant() {
        // If either snake has at least 1 life remaining then this power up is relevant
        return m_Snakes[0].getNumLives() > 0 ||
                (m_Snakes.length > 1 && m_Snakes[1].getNumLives() > 0);
    }

    private boolean isDecreasePointsPowerUpRelevant() {
        // If either snake greater than 1000 points then this power up is relevant
        return m_Snakes[0].getPoints() > Snake.s_PowerUpPointsBonus ||
                (m_Snakes.length > 1 && m_Snakes[1].getPoints() > Snake.s_PowerUpPointsBonus);
    }

    private boolean isDecreaseLengthPowerUpRelevant() {
        // If either snake is able to shrink in length then this power up is relevant
        return m_Snakes[0].getBodyParts().size() > Snake.s_MinBodyParts ||
                (m_Snakes.length > 1 && m_Snakes[1].getBodyParts().size() > Snake.s_MinBodyParts);
    }

    private void insertRandomPowerUp() {
        insertPowerUp(chooseRandomPowerUpType(true));
        scheduleExpirePowerUp();
    }

    private void scheduleExpirePowerUp() {
        removePowerUpTimeout();
        m_PowerUpTimeoutId = m_Context.addTimeout(s_PowerUpExpireTimeoutMs, (callCount1) -> {
            if (m_PowerUp != null) {
                m_GameField.removePowerUp(m_PowerUp);
                m_PowerUp = null;
            }
            scheduleInsertPowerUp(s_PowerUpInitialTimeoutMs);
            return TimeoutManager.CallbackResult.REMOVE_THIS_CALLBACK;
        });
    }

    private void scheduleInsertWalls() {
        removeWallsTimeout();
        m_WallsTimeoutId = m_Context.addTimeout(s_InsertWallsTimeoutMs, (callCount1) -> {
            insertWalls();
            return TimeoutManager.CallbackResult.KEEP_CALLING;
        });
    }

    public void insertWalls() {
        Snake.Direction[] directions = {
                Snake.Direction.Left, Snake.Direction.Up,
                Snake.Direction.Right, Snake.Direction.Down
        };

        Vector2i increment;
        switch (directions[m_Rng.nextInt(4)]) {
            case Left: increment = new Vector2i(-1, 0); break;
            case Right: increment = new Vector2i(1, 0); break;
            case Up: increment = new Vector2i(0, 1); break;
            default: case Down: increment = new Vector2i(0, -1); break;
        }

        Vector2i location = chooseRandomEmptyCell();
        int numWalls = m_Rng.nextInt(4) + 1;
        for (int i = 0; i < numWalls; ++i) {
            if (isCellEmpty(location)) {
                m_GameField.insertWall(location);
            }
            location = m_GameField.clampCoordinates(location.add(increment));
        }
    }

    private void removeWallsTimeout() {
        if (m_WallsTimeoutId != 0) {
            m_Context.removeTimeout(m_WallsTimeoutId);
            m_WallsTimeoutId = 0;
        }
    }

    private void removeSnakeMovementTimeout() {
        if (m_SnakeTimeoutId != 0) {
            m_Context.removeTimeout(m_SnakeTimeoutId);
            m_SnakeTimeoutId = 0;
        }
    }

    private void removePowerUpTimeout() {
        if (m_PowerUpTimeoutId != 0) {
            m_Context.removeTimeout(m_PowerUpTimeoutId);
            m_PowerUpTimeoutId = 0;
        }
    }

    private void moveSnakesForwards() {
        for (var snake : m_Snakes) {
            snake.moveForwards();
        }
    }

    private static class CollisionResult {
        private final Result m_Result;
        private final int m_Player;
        public enum Result { SINGLE_SNAKE, BOTH_SNAKES, NO_COLLISION }
        public CollisionResult() {
            m_Result = Result.NO_COLLISION;
            m_Player = -1;
        }
        public CollisionResult(boolean bothSnakes, int player) {
            m_Result = bothSnakes ? Result.BOTH_SNAKES :  Result.SINGLE_SNAKE;
            m_Player = bothSnakes ? -1 : Math.min(1, Math.max(0, player));
        }
        public boolean collisionOccurred() {
            return m_Result != Result.NO_COLLISION;
        }
        public Result getResult() {
            return m_Result;
        }
        public int getPlayer() {
            return m_Player;
        }
    }

    private CollisionResult performCollisionDetection() {
        CollisionResult r = collideSnakesWithWalls();
        if (r.collisionOccurred()) {
            return r;
        }
        r = collideSnakesWithThemselves();
        if (r.collisionOccurred()) {
            return r;
        }
        r = collideSnakesWithEachOther();
        if (r.collisionOccurred()) {
            return r;
        }

        for (var snake : m_Snakes) {
            checkSnakeForCollisionWithNumber(snake);
            checkSnakeForCollisionWithPowerUp(snake);
        }
        return r;
    }

    private CollisionResult collideSnakesWithWalls() {
        boolean player1Colliding = isSnakesOutOfBounds(m_Snakes[0]) || isSnakeCollidingWithWall(m_Snakes[0]);
        boolean player2Colliding = m_Snakes.length > 1 && (isSnakesOutOfBounds(m_Snakes[1]) || isSnakeCollidingWithWall(m_Snakes[1]));
        if (player1Colliding) {
            if (player2Colliding) {
                return new CollisionResult(true, -1);
            }
            return new CollisionResult(false, 0);
        }
        if (player2Colliding) {
            return new CollisionResult(false, 1);
        }
        return new CollisionResult();
    }

    private boolean isSnakesOutOfBounds(Snake snake) {
        Vector2i position = snake.getBodyParts().getFirst().m_Location;
        return position.m_X < 0 || position.m_Z < 0 || position.m_X >= GameField.WIDTH || position.m_Z >= GameField.HEIGHT;
    }

    private CollisionResult collideSnakesWithThemselves() {
        boolean player1Colliding = m_Snakes[0].isCollidingWithItself();
        boolean player2Colliding = m_Snakes.length > 1 && m_Snakes[1].isCollidingWithItself();
        if (player1Colliding) {
            if (player2Colliding) {
                return new CollisionResult(true, -1);
            }
            return new CollisionResult(false, 0);
        }
        if (player2Colliding) {
            return new CollisionResult(false, 1);
        }
        return new CollisionResult();
    }

    private CollisionResult collideSnakesWithEachOther() {
        if (m_Snakes.length > 1) {
            boolean player1Colliding = m_Snakes[0].isCollidingWith(m_Snakes[1]);
            boolean player2Colliding = m_Snakes[1].isCollidingWith(m_Snakes[0]);
            if (player1Colliding) {
                if (player2Colliding) {
                    return new CollisionResult(true, -1);
                }
                return new CollisionResult(false, 0);
            }
            if (player2Colliding) {
                return new CollisionResult(false, 1);
            }
        }
        return new CollisionResult();
    }

    private boolean isSnakeCollidingWithWall(Snake snake) {
        return m_GameField.getCellType(snake.getBodyParts().getFirst().m_Location) == GameField.CellType.WALL;
    }

    private void checkSnakeForCollisionWithNumber(Snake snake) {
        if (isSnakeCollidingWithNumber(snake) && m_Number != null) {
            awardNumber(m_Number.getType(), snake);
        }
    }

    private void checkSnakeForCollisionWithPowerUp(Snake snake) {
        if (isSnakeCollidingWithPowerUp(snake) && m_PowerUp != null) {
            removePowerUpTimeout();
            awardPowerUp(m_PowerUp.getType(), snake, true);
        }
    }

    private boolean isSnakeCollidingWithNumber(Snake snake) {
        return m_GameField.getCellType(snake.getBodyParts().getFirst().m_Location) == GameField.CellType.NUMBER;
    }

    private boolean isSnakeCollidingWithPowerUp(Snake snake) {
        return m_GameField.getCellType(snake.getBodyParts().getFirst().m_Location) == GameField.CellType.POWER_UP;
    }

    private void awardNumber(Number.Type numberType, Snake snake) {
        snake.awardNumber(numberType);

        if (m_Number != null) {
            m_GameField.removeNumber(m_Number);
            m_Number = null;
        }

        Number.Result r = Number.getNextInSeries(numberType);
        if (r.m_LevelComplete) {
            m_Context.changeState(new LevelCompleteAppState(m_Context));
        }
        else {
            insertNumber(r.m_Type);
        }
    }

    private void awardPowerUp(PowerUp.Type powerUpType, Snake snake, boolean scheduleNext) {
        if (m_PowerUp != null) {
            m_GameField.removePowerUp(m_PowerUp);
            m_PowerUp = null;
        }

        switch (powerUpType) {
            case INC_SPEED:
                m_SnakeTimeoutMs = Math.max(s_FastestSnakeSpeed, m_SnakeTimeoutMs - s_SnakeSpeedPowerUpAdjustment);
                refreshSnakeMovementTimeout();
                break;
            case DEC_SPEED:
                m_SnakeTimeoutMs = Math.min(s_SlowestSnakeSpeed, m_SnakeTimeoutMs + s_SnakeSpeedPowerUpAdjustment);
                refreshSnakeMovementTimeout();
                break;
            case RANDOM: {
                awardPowerUp(chooseRandomPowerUpType(false), snake, false);
                break;
            }
            default:
                snake.awardPowerUp(powerUpType);
                break;
        }

        if (scheduleNext) {
            scheduleInsertPowerUp(s_PowerUpSubsequentTimeoutMs);
            m_Context.getView().startPowerUpAnimation(snake.getId(), powerUpType);
        }
    }

    private Vector2i chooseRandomEmptyCell() {
        ArrayList<Vector2i> emptyFieldCells = m_GameField.getEmptyCells();

        emptyFieldCells = removeCellsCloseToCell(emptyFieldCells, m_GameField.getPlayer1Start());
        if (m_Mode == Mode.TWO_PLAYERS) {
            emptyFieldCells = removeCellsCloseToCell(emptyFieldCells, m_GameField.getPlayer2Start());
        }

        ArrayList<Vector2i> emptyCells = new ArrayList<>(emptyFieldCells.size());
        for (var emptyFieldCell : emptyFieldCells) {
            if (isNeitherSnakeUsingThisCell(emptyFieldCell)) {
                emptyCells.add(emptyFieldCell);
            }
        }

        return emptyCells.get(m_Rng.nextInt(emptyCells.size()));
    }

    private ArrayList<Vector2i> removeCellsCloseToCell(ArrayList<Vector2i> inCells, Vector2i compareMe) {
        ArrayList<Vector2i> outCells = new ArrayList<>(inCells.size());
        for (var inCell : inCells) {
            if (compareMe.magnitude(inCell) >= (Snake.s_MinBodyParts * 2)) {
                outCells.add(inCell);
            }
        }
        return outCells;
    }

    private boolean isNeitherSnakeUsingThisCell(Vector2i cell) {
        for (var snake : m_Snakes) {
            for (var bodyPart : snake.getBodyParts()) {
                if (bodyPart.m_Location.equals(cell)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isCellEmpty(Vector2i location) {
        return m_GameField.getCellType(location) == GameField.CellType.EMPTY && isNeitherSnakeUsingThisCell(location);
    }
}
