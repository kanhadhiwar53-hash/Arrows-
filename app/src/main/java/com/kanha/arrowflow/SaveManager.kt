package com.kanha.arrowflow
import android.content.Context
class SaveManager(context:Context){
 private val p=context.getSharedPreferences("arrow_flow",Context.MODE_PRIVATE)
 var level:Int get()=p.getInt("level",1) set(v){p.edit().putInt("level",v).apply()}
 var coins:Int get()=p.getInt("coins",0) set(v){p.edit().putInt("coins",v).apply()}
}
