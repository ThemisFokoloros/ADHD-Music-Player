package com.example.mymusicplayer_v1.data

import com.example.mymusicplayer_v1.R

data class Music(
    val name: String,
    val music: Int,
    val cover: Int?,
)

val musicList1 = listOf(

    Music(
        name = "Bilateral Tranquility Variation 1",
        music = R.raw.bilateral_tranquility_variation1_01,
        cover = null
    ),
    Music(
        name = "On A Piano A Cloud Nature Sounds",
        music = R.raw.on_a_piano_a_cloud_nature_sounds_02,
        cover = null
    )
    //more music to be added on a later date.
)


