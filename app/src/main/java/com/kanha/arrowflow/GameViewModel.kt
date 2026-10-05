package com.kanha.arrowflow

class GameViewModel(
    private val save: SaveManager,
    private val startingLevel: Int = save.level,
    puzzle: PuzzleState? = null
) {
    var level = startingLevel
    var coins = save.coins
    var moves = 0
    private var puzzleState = puzzle ?: PuzzleGenerator.level(level)
    private var initialArrowCount = puzzleState.arrows.size
    private var engine = PuzzleEngine(puzzleState)

    fun state() = engine.arrows()
    fun size() = puzzleState.size

    fun tap(id: Int) = engine.move(id).also { if (it) moves++ }
    fun undo() = engine.undo().also { if (it && moves > 0) moves-- }
    fun reset() { engine.reset(); moves = 0 }
    fun hint() = engine.hint()
    fun complete() = engine.complete()

    fun markCampaignComplete() {
        if (level >= save.level) save.level = level + 1
    }

    fun next() {
        level++
        markCampaignComplete()
        coins += 10
        save.coins = coins
        load(PuzzleGenerator.level(level))
    }

    fun load(puzzle: PuzzleState) {
        puzzleState = puzzle
        initialArrowCount = puzzle.arrows.size
        engine = PuzzleEngine(puzzle)
        moves = 0
    }

    fun stars() = when {
        moves <= initialArrowCount -> 3
        moves <= initialArrowCount + 3 -> 2
        else -> 1
    }
}
