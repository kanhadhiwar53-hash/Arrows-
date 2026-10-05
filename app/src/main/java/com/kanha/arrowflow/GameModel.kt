package com.kanha.arrowflow
enum class Direction { UP, DOWN, LEFT, RIGHT }
enum class ArrowState { BLOCKED, AVAILABLE, ESCAPING, ESCAPED }
data class ArrowPiece(val id:Int,val row:Int,val col:Int,val direction:Direction,var state:ArrowState=ArrowState.AVAILABLE)
data class PuzzleState(val size:Int,val arrows:List<ArrowPiece>)
class PuzzleEngine(private val puzzle:PuzzleState) {
 private val active=LinkedHashMap<Int,ArrowPiece>()
 val history=ArrayDeque<Int>()
 init { puzzle.arrows.forEach { active[it.id]=it.copy() }; refresh() }
 fun arrows()=active.values.toList()
 fun isAvailable(id:Int):Boolean {
  val a=active[id]?:return false
  val dr=when(a.direction){Direction.UP->-1;Direction.DOWN->1;else->0}
  val dc=when(a.direction){Direction.LEFT->-1;Direction.RIGHT->1;else->0}
  var r=a.row+dr;var c=a.col+dc
  while(r in 0 until puzzle.size&&c in 0 until puzzle.size){if(active.values.any{it.id!=a.id&&it.row==r&&it.col==c})return false;r+=dr;c+=dc};return true
 }
 fun move(id:Int)=if(isAvailable(id)){active.remove(id);history.addLast(id);refresh();true}else false
 fun undo()=history.removeLastOrNull()?.let{id->active[id]=puzzle.arrows.first{it.id==id}.copy();refresh();true}?:false
 fun reset(){active.clear();puzzle.arrows.forEach{active[it.id]=it.copy()};history.clear();refresh()}
 fun complete()=active.isEmpty()
 fun hint()=active.keys.firstOrNull{isAvailable(it)}
 private fun refresh(){active.values.forEach{it.state=if(isAvailable(it.id))ArrowState.AVAILABLE else ArrowState.BLOCKED}}
}
object PuzzleGenerator {
 fun level(number:Int):PuzzleState {
  val size=when{number<11->4;number<31->5;number<71->6;else->7}
  val count=when{size<5->6;size<6->9;size<7->13;else->18}
  val dirs=Direction.entries
  val cells=(0 until size).flatMap{r->(0 until size).map{c->r to c}}.shuffled(kotlin.random.Random(number))
  val arrows=mutableListOf<ArrowPiece>();var id=1
  cells.take(count).forEach{(r,c)->arrows+=ArrowPiece(id++,r,c,dirs[(id+number)%4])}
  return PuzzleState(size,arrows)
 }
}
object PuzzleValidator { fun isValid(p:PuzzleState)=p.size in 4..12&&p.arrows.distinctBy{it.id}.size==p.arrows.size }
