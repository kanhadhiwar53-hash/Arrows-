package com.kanha.arrowflow
import android.graphics.Color

data class GameTheme(val name:String,val background:Int,val surface:Int,val accent:Int,val ink:Int)

object ThemeManager {
 val themes=listOf(
  GameTheme("Classic",Color.rgb(246,247,251),Color.WHITE,Color.rgb(91,83,220),Color.rgb(28,34,52)),
  GameTheme("Dark",Color.rgb(20,22,30),Color.rgb(34,37,48),Color.rgb(111,238,210),Color.WHITE),
  GameTheme("Ocean",Color.rgb(232,247,252),Color.WHITE,Color.rgb(23,132,196),Color.rgb(18,55,75)),
  GameTheme("Sunset",Color.rgb(255,244,238),Color.WHITE,Color.rgb(224,92,104),Color.rgb(75,42,55))
 )
 fun get(name:String)=themes.firstOrNull{it.name==name}?:themes.first()
 fun next(name:String):GameTheme{
  val i=themes.indexOfFirst{it.name==name}.coerceAtLeast(0)
  return themes[(i+1)%themes.size]
 }
}
