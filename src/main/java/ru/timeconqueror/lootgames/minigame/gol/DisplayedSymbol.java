package ru.timeconqueror.lootgames.minigame.gol;

public class DisplayedSymbol {
    private final long clickedTime;
    private final Symbol symbol;

    public DisplayedSymbol(long clickedTime, Symbol symbol) {
        this.clickedTime = clickedTime;
        this.symbol = symbol;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public long getClickedTime() {
        return clickedTime;
    }
}