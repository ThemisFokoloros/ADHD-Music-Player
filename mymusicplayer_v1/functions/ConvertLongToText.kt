package com.example.mymusicplayer_v1.functions


fun convertLongToText(long: Long):String{
    val sec = long/1000
    val minutes = sec/60
    val seconds = sec%60

    val minutesString = if (minutes < 10){
        "0${minutes}"
    }else{
        minutes.toString()
    }
    val secondsString = if (seconds < 10){
        "0${seconds}"
    }else{
        seconds.toString()
    }
    return "$minutesString:$secondsString"
}