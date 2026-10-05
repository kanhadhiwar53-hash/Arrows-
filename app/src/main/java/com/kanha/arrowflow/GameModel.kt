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

object PuzzleSolver {
 fun solve(p:PuzzleState):List<Int>? {
  if(p.arrows.size>62)return null
  val memo=HashSet<Long>()
  fun dfs(mask:Long):List<Int>? {
   if(mask==0L)return emptyList()
   if(!memo.add(mask))return null
   for(i in p.arrows.indices){
    val bit=1L shl i
    if(mask and bit==0L)continue
    val a=p.arrows[i]
    val dr=when(a.direction){Direction.UP->-1;Direction.DOWN->1;else->0}
    val dc=when(a.direction){Direction.LEFT->-1;Direction.RIGHT->1;else->0}
    var r=a.row+dr;var c=a.col+dc;var blocked=false
    while(r in 0 until p.size&&c in 0 until p.size){
     for(j in p.arrows.indices)if(j!=i&&(mask and (1L shl j))!=0L&&p.arrows[j].row==r&&p.arrows[j].col==c){blocked=true;break}
     if(blocked)break;r+=dr;c+=dc
    }
    if(!blocked){val rest=dfs(mask xor bit);if(rest!=null)return listOf(a.id)+rest}
   }
   return null
  }
  return dfs(if(p.arrows.size==0)0L else (1L shl p.arrows.size)-1)
 }
}

object PuzzleGenerator {
 fun level(number:Int):PuzzleState {
  val size=when{number<11->4;number<31->5;number<71->6;else->7}
  val count=when{size<5->6;size<6->9;size<7->13;else->18}
  val dirs=Direction.entries
  val random=kotlin.random.Random(number)
  repeat(60){
   val cells=(0 until size).flatMap{r->(0 until size).map{c->r to c}}.shuffled(random)
   val arrows=cells.take(count).mapIndexed{index,(r,c)->ArrowPiece(index+1,r,c,dirs[random.nextInt(dirs.size)])}
   val puzzle=PuzzleState(size,arrows)
   if(PuzzleValidator.isValid(puzzle)&&PuzzleSolver.solve(puzzle)!=null)return puzzle
  }
  return fallback(size,count)
 }
 private fun fallback(size:Int,count:Int):PuzzleState {
  val list=mutableListOf<ArrowPiece>();var id=1
  for(r in 0 until size)if(list.size<count)list+=ArrowPiece(id++,r,0,Direction.LEFT)
  for(c in 1 until size)if(list.size<count)list+=ArrowPiece(id++,size-1,c,Direction.DOWN)
  for(c in 1 until size)if(list.size<count)list+=ArrowPiece(id++,0,c,Direction.UP)
  return PuzzleState(size,list)
 }
}
object PuzzleValidator {
 fun isValid(p:PuzzleState)=p.size in 4..12&&p.arrows.size<=62&&p.arrows.distinctBy{it.id}.size==p.arrows.size&&p.arrows.distinctBy{it.row to it.col}.size==p.arrows.size
 fun isSolvable(p:PuzzleState)=isValid(p)&&PuzzleSolver.solve(p)!=null
}
