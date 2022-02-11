package me.skinnyjeans.gmd.managers;

import me.skinnyjeans.gmd.models.Difficulty;

import java.util.UUID;

public class DifficultyManager {

    private final MainManager MAIN_MANAGER;

    public DifficultyManager(MainManager mainManager) {
        MAIN_MANAGER = mainManager;
    }

    public Difficulty getDifficulty(UUID uuid) { return new Difficulty(""); }
}