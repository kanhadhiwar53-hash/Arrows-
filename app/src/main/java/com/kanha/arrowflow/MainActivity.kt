package com.kanha.arrowflow
import android.app.Activity
import android.os.Bundle
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.view.*
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import kotlin.math.min

class MainActivity:Activity(){
 private lateinit var game:GameView
 override fun onCreate(b:Bundle?){super.onCreate(b);window.setBackgroundDrawable(ColorDrawable(Color.rgb(246,247,251)));game=GameView(this);setContentView(game)}
}
class GameView(private val ctx:Context):View(ctx){
 private val vm=GameViewModel(SaveManager(ctx));private val paint=Paint(Paint.ANTI_ALIAS_FLAG)
 private var boardTop=0f;private var cell=0f;private var hintId:Int?=null;private var complete=false
 private val bg=Color.rgb(246,247,251);private val ink=Color.rgb(28,34,52);private val accent=Color.rgb(91,83,220)
 override fun onDraw(c:Canvas){
  c.drawColor(bg);text(c,"ARROW FLOW",28f,36f,ink,22f);text(c,"Level "+vm.level,28f,70f,Color.DKGRAY,16f);text(c,"Coins "+vm.coins,width-120f,45f,ink,16f)
  val n=vm.size();cell=min(width-56f,(height-245f).coerceAtLeast(260f))/n;boardTop=105f;val left=(width-cell*n)/2f
  for(r in 0 until n)for(col in 0 until n){paint.color=Color.WHITE;c.drawRoundRect(left+col*cell+2,boardTop+r*cell+2,left+(col+1)*cell-2,boardTop+(r+1)*cell-2,12f,12f,paint)}
  vm.state().forEach{drawArrow(c,it,left,boardTop)}
  button(c,24f,height-110f,145f,56f,"Undo");button(c,164f,height-110f,145f,56f,"Hint");button(c,304f,height-110f,145f,56f,"Reset")
  if(complete){paint.color=0xEEFFFFFF.toInt();c.drawRect(0f,0f,width.toFloat(),height.toFloat(),paint);text(c,"LEVEL COMPLETE!",width/2-120f,height/2-35f,accent,26f);text(c,"★".repeat(vm.stars()),width/2-35f,height/2+10f,Color.rgb(245,175,45),28f);button(c,width/2-90f,height/2+55f,180f,58f,"Next Level")}
 }
 private fun drawArrow(c:Canvas,a:ArrowPiece,l:Float,t:Float){
  val x=l+a.col*cell;val y=t+a.row*cell;val cx=x+cell/2;val cy=y+cell/2;paint.color=when{a.id==hintId->Color.rgb(245,175,45);a.state==ArrowState.BLOCKED->Color.rgb(190,195,210);else->accent};val s=cell*.27f;val p=Path()
  when(a.direction){
   Direction.UP->{p.moveTo(cx,cy-s);p.lineTo(cx+s,cy);p.lineTo(cx+s*.38f,cy);p.lineTo(cx+s*.38f,cy+s);p.lineTo(cx-s*.38f,cy+s);p.lineTo(cx-s*.38f,cy);p.lineTo(cx-s,cy);p.close()}
   Direction.DOWN->{p.moveTo(cx,cy+s);p.lineTo(cx+s,cy);p.lineTo(cx+s*.38f,cy);p.lineTo(cx+s*.38f,cy-s);p.lineTo(cx-s*.38f,cy-s);p.lineTo(cx-s*.38f,cy);p.lineTo(cx-s,cy);p.close()}
   Direction.LEFT->{p.moveTo(cx-s,cy);p.lineTo(cx,cy-s);p.lineTo(cx,cy-s*.38f);p.lineTo(cx+s,cy-s*.38f);p.lineTo(cx+s,cy+s*.38f);p.lineTo(cx,cy+s*.38f);p.lineTo(cx,cy+s);p.close()}
   Direction.RIGHT->{p.moveTo(cx+s,cy);p.lineTo(cx,cy-s);p.lineTo(cx,cy-s*.38f);p.lineTo(cx-s,cy-s*.38f);p.lineTo(cx-s,cy+s*.38f);p.lineTo(cx,cy+s*.38f);p.lineTo(cx,cy+s);p.close()}
  };c.drawPath(p,paint)
 }
 private fun button(c:Canvas,x:Float,y:Float,w:Float,h:Float,label:String){paint.color=Color.WHITE;c.drawRoundRect(x,y,x+w,y+h,18f,18f,paint);paint.style=Paint.Style.STROKE;paint.color=0x18000000;paint.strokeWidth=2f;c.drawRoundRect(x,y,x+w,y+h,18f,18f,paint);paint.style=Paint.Style.FILL;text(c,label,x+18,y+35,ink,15f)}
 private fun text(c:Canvas,s:String,x:Float,y:Float,color:Int,size:Float){paint.color=color;paint.textSize=size;paint.typeface=Typeface.create("sans",Typeface.BOLD);paint.style=Paint.Style.FILL;c.drawText(s,x,y,paint)}
 override fun onTouchEvent(e:MotionEvent):Boolean{
  if(e.action!=MotionEvent.ACTION_UP)return true;val x=e.x;val y=e.y
  if(complete){if(y>height/2+45){vm.next();complete=false;invalidate()};return true}
  if(y>height-125){when{x<154->{vm.undo();hintId=null};x<310->{hintId=vm.hint()};else->{vm.reset();hintId=null}};invalidate();return true}
  val n=vm.size();val left=(width-cell*n)/2f
  if(y>=boardTop&&y<boardTop+cell*n&&x>=left&&x<left+cell*n){val col=((x-left)/cell).toInt();val row=((y-boardTop)/cell).toInt();val a=vm.state().firstOrNull{it.row==row&&it.col==col};if(a!=null){hintId=null;if(vm.tap(a.id)){vibrate();if(vm.complete())complete=true}else vibrate(25);invalidate()}}
  return true
 }
 private fun vibrate(ms:Long=12){val v=ctx.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator;if(v?.hasVibrator()==true)v.vibrate(VibrationEffect.createOneShot(ms,VibrationEffect.DEFAULT_AMPLITUDE))}
}
