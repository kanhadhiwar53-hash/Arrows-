package com.kanha.arrowflow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DailyChallengeManager {
 fun todayKey():String=SimpleDateFormat("yyyy-MM-dd",Locale.US).format(Date())
 fun puzzleForToday():PuzzleState{
  val seed=todayKey().replace("-","").toIntOrNull()?:1
  return PuzzleGenerator.level(40+(seed%30))
 }
}
