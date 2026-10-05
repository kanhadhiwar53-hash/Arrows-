package com.kanha.arrowflow
import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View

class HomeView(
 context:Context,
 private val onPlay:()->Unit,
 private val onDaily:()->Unit
):View(context){
 private val save=SaveManager(context)
 private val paint=Paint(Paint.ANTI_ALIAS_FLAG)
 private fun theme()=ThemeManager.get(save.selectedTheme)

 override fun onDraw(c:Canvas){
  val t=theme();c.drawColor(t.background)
  text(c,"ARROW FLOW",28f,58f,t.ink,30f,true)
  text(c,"Plan the path. Clear the board.",28f,88f,t.ink,15f,false)
  card(c,24f,120f,width-48f,118f,t.surface)
  text(c,"Continue",46f,158f,t.ink,18f,true)
  text(c,"Level "+save.level,46f,190f,t.ink,28f,true)
  pill(c,width-132f,150f,92f,44f,"PLAY",t.accent,Color.WHITE)

  card(c,24f,258f,width-48f,100f,t.surface)
  text(c,"Daily Challenge",46f,294f,t.ink,18f,true)
  text(c,if(save.dailyCompleted)"Completed • Streak "+save.dailyStreak else "Today's puzzle is ready",46f,326f,t.ink,14f,false)
  pill(c,width-142f,286f,102f,44f,"DAILY",t.accent,Color.WHITE)

  text(c,"Theme",28f,400f,t.ink,15f,true)
  pill(c,28f,418f,150f,48f,save.selectedTheme,t.surface,t.ink)
  text(c,"Tap theme to switch",194f,449f,t.ink,13f,false)

  text(c,"Coins "+save.coins,28f,505f,t.ink,15f,true)
  text(c,"Offline ready",28f,535f,t.ink,14f,false)
 }

 private fun card(c:Canvas,x:Float,y:Float,w:Float,h:Float,color:Int){paint.color=color;paint.style=Paint.Style.FILL;c.drawRoundRect(x,y,x+w,y+h,24f,24f,paint)}
 private fun pill(c:Canvas,x:Float,y:Float,w:Float,h:Float,label:String,bg:Int,fg:Int){paint.color=bg;paint.style=Paint.Style.FILL;c.drawRoundRect(x,y,x+w,y+h,h/2,h/2,paint);text(c,label,x+18,y+h*.66f,fg,14f,true)}
 private fun text(c:Canvas,s:String,x:Float,y:Float,color:Int,size:Float,bold:Boolean){paint.color=color;paint.textSize=size;paint.typeface=Typeface.create("sans",if(bold)Typeface.BOLD else Typeface.NORMAL);paint.style=Paint.Style.FILL;c.drawText(s,x,y,paint)}
 override fun onTouchEvent(e:MotionEvent):Boolean{
  if(e.action!=MotionEvent.ACTION_UP)return true
  when{
   e.y in 120f..238f -> onPlay()
   e.y in 258f..358f -> onDaily()
   e.y in 410f..475f -> {save.selectedTheme=ThemeManager.next(save.selectedTheme).name;invalidate()}
  }
  return true
 }
}
