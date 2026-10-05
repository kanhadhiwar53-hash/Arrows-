package com.kanha.arrowflow
class GameViewModel(private val save:SaveManager){
 var level=save.level;var coins=save.coins;var moves=0
 private var engine=PuzzleEngine(PuzzleGenerator.level(level))
 fun state()=engine.arrows();fun size()=PuzzleGenerator.level(level).size
 fun tap(id:Int)=engine.move(id).also{if(it)moves++}
 fun undo()=engine.undo();fun reset(){engine.reset();moves=0};fun hint()=engine.hint();fun complete()=engine.complete()
 fun next(){level++;save.level=level;coins+=10;save.coins=coins;engine=PuzzleEngine(PuzzleGenerator.level(level));moves=0}
 fun stars()=when{moves<=state().size->3;moves<=state().size+3->2;else->1}
}
