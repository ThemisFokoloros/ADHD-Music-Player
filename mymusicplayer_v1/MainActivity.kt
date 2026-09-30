package com.example.mymusicplayer_v1


import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.with
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.mymusicplayer_v1.data.Music
import com.example.mymusicplayer_v1.data.musicList1
import com.example.mymusicplayer_v1.ui.ControlButton
import com.example.mymusicplayer_v1.ui.theme.MyMusicPlayer_v1Theme
import com.example.mymusicplayer_v1.ui.theme.darkColor
import com.example.mymusicplayer_v1.ui.theme.lightColor
import com.example.mymusicplayer_v1.functions.convertLongToText
import kotlinx.coroutines.delay


class MainActivity : ComponentActivity() {

    lateinit var player:ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        player = ExoPlayer.Builder(this).build()

        setContent {
            MyMusicPlayer_v1Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MusicPlayerMainScreen(musicList = musicList1, player)
                }
            }
        }
    }

    //The main UI screen
    @OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
    @Composable
    fun MusicPlayerMainScreen(musicList : List<Music>, player : ExoPlayer) {

        //initializing the pager state ,crucial for correct vinyl animation
        //and transition from song to song
        val pagerState = rememberPagerState(pageCount = { musicList.count() })
        var playingIndex by remember {
            mutableIntStateOf(0)
        }
        //If we change song through the vinyl animation the player starts
        //from the beginning playing the song
        LaunchedEffect(pagerState.currentPage) {
            playingIndex = pagerState.currentPage
            player.seekTo(pagerState.currentPage, 0)
        }

        //We add the music we want to play to the media player
        LaunchedEffect(Unit){
            musicList.forEach{
                val path = "android.resource://"+packageName+"/"+it.music
                val mediaItem = MediaItem.fromUri(Uri.parse(path))
                player.addMediaItem(mediaItem)
            }
        }
        player.prepare() // we enable the player

        //value to control the player.
        var playing: Boolean? by remember {
            mutableStateOf(false)
        }

        var currentPosition by remember {
            mutableLongStateOf(0)
        }

        var songChange by remember {
            mutableLongStateOf(0)
        }

        var totalDuration by remember {
            mutableLongStateOf(0)
        }

        var progressSize by remember {
            mutableStateOf(IntSize(0,0))
        }

        var progressChange by remember {
            mutableLongStateOf(0)
        }

        LaunchedEffect(
            key1 = player.currentPosition,
            key2 = player.isPlaying,
            key3 = progressChange
        ) {
            delay(100)
            currentPosition = player.currentPosition
            progressChange = 0
        }

        LaunchedEffect(player.duration) {
            delay(100)
            if (player.duration > 0) {
                totalDuration = player.duration
            }
        }

        LaunchedEffect(key1 = player.currentMediaItemIndex, key2 = songChange){
            playingIndex = player.currentMediaItemIndex
            pagerState.animateScrollToPage(playingIndex, animationSpec = tween(500))
            songChange = 0
        }
        
        var percentReached = currentPosition.toFloat()/(if (totalDuration > 0) totalDuration else 0).toFloat()
        if (percentReached.isNaN()) {
            percentReached = 0f
        }

        //Let's start with the main UIs construction using jetpack compose
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        lightColor
                    )
                ), contentAlignment = Alignment.Center
        ){
            val configuration = LocalConfiguration.current

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val textColor by animateColorAsState(
                    targetValue = if (lightColor[0].luminance() > .5f) Color(
                        0xff414141
                    )else Color.White,
                    animationSpec = tween(2000),
                    label = "White color letters"
                )
                AnimatedContent(targetState = playingIndex, transitionSpec = {
                    (scaleIn() + fadeIn()) togetherWith (scaleOut() + fadeOut())
                }, label = "The Name of the song") {
                    Text(
                        text = musicList[it].name,
                        fontSize = 30.sp,
                        color = textColor,
                        textAlign = TextAlign.Center,
                        lineHeight = 1.5.em,
                        style = LocalTextStyle.current.merge(
                            TextStyle(
                                lineHeightStyle = LineHeightStyle(
                                    alignment = LineHeightStyle.Alignment.Center,
                                    trim = LineHeightStyle.Trim.Both
                                )
                            )
                        )
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
                //Here we add the vinyl animation
                Spacer(modifier = Modifier.height(54.dp))
                //Here we add the progression bar
                Row (
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ){
                  Text(text = convertLongToText(currentPosition),
                      modifier = Modifier.width(55.dp),
                      color = textColor,
                      textAlign = TextAlign.Center
                  )
                  // Progress Box
                  Box (
                      modifier = Modifier
                          .fillMaxWidth()
                          .weight(1f)
                          .height(8.dp)
                          .padding(horizontal = 8.dp)
                          .clip(CircleShape)
                          .background(Color.White)
                          .onGloballyPositioned {
                              progressSize = it.size
                          }
                          .pointerInput(Unit) {
                              detectTapGestures {
                                  val xPos = it.x
                                  val whereIClicked =
                                      (xPos.toLong() * totalDuration) / progressSize.width.toLong()
                                  player.seekTo(whereIClicked)
                                  progressChange = 1
                              }
                          },
                      contentAlignment = Alignment.CenterStart
                  ){
                      //Status Box
                      Box(
                          modifier = Modifier
                              .fillMaxWidth(fraction = if (playing != null) percentReached else 0f)
                              .fillMaxHeight()
                              .clip(RoundedCornerShape(8.dp))
                              .background(Color(0xff414141))
                      )
                  }
                  Text(text = convertLongToText(totalDuration),
                      modifier = Modifier.width(55.dp),
                      color = textColor,
                      textAlign = TextAlign.Center
                  )
                }
                //Now the control Buttons
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ControlButton(
                        icon = R.drawable.ic_fast_rewind,
                        size = 60.dp,
                        bgColor = Color.Transparent,
                        onClick = {
                            if(true){
                                player.seekToPreviousMediaItem()
                                songChange = 1
                            }
                        }
                    )
                    ControlButton(
                        icon = if (playing == true) R.drawable.ic_pause else R.drawable.ic_play,
                        size = 80.dp,
                        bgColor = Color.White,
                        onClick = {
                            if(playing == true){
                                player.pause()
                                playing = player.isPlaying
                            }else{
                                player.play()
                                playing = player.isPlaying
                            }

                        }
                    )
                    ControlButton(
                        icon = R.drawable.ic_fast_forward,
                        size = 60.dp,
                        bgColor = Color.Transparent,
                        onClick = {
                            if(true){
                                player.seekToNextMediaItem()
                                songChange = 2
                            }
                        }
                    )
                }
            }
        }
    }

}





