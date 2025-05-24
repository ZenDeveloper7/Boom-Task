package com.zen.boom.task.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.zen.boom.task.Session
import com.zen.boom.task.Session.getUserId
import com.zen.boom.task.model.VideoModel
import com.zen.boom.task.network.Resource
import com.zen.boom.task.viewmodels.VideoViewModel
import timber.log.Timber

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun HomeFeedScreen(
    navController: NavController,
    viewModel: VideoViewModel = viewModel()
) {
    val context = LocalContext.current
    val videoList = remember { mutableStateListOf<VideoModel>() }
    val pagerState = rememberPagerState { videoList.size }

    LaunchedEffect(Unit) {
        viewModel.getFeed(1)
    }

    val feedResponse by viewModel.feedVideoMutableStateFlow.collectAsState()

    LaunchedEffect(feedResponse) {
        when (feedResponse) {
            is Resource.Success -> {
                videoList.addAll((feedResponse as Resource.Success<List<VideoModel>>).data)
            }

            is Resource.Error -> {

            }

            else -> {

            }
        }

    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (videoList.isNotEmpty()) {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                var videoReady by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxSize()) {
                    // Show thumbnail until video is ready
                    if (!videoReady) {
                        GlideImage(
                            model = videoList[page].thumbnailUrl,
                            contentDescription = "Video Thumbnail",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    VideoPlayer(
                        url = videoList[page].videoUrl,
                        onVideoCompleted = {
                            viewModel.viewVideo(videoList[page].id ?: "")
                        },
                        onVideoReady = { videoReady = true } // Add this callback to VideoPlayer
                    )
                    FloatingActionButton(
                        onClick = { viewModel.likeVideo(videoList[page].id ?: "") },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                    ) {
                        var isLiked = remember {
                            videoList[page].metadata.likes.contains(getUserId())
                        }
                        Icon(
                            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.Favorite,
                            contentDescription = if (isLiked) "Liked" else "Like",
                            modifier = Modifier.clickable {
                                if (!isLiked) {
                                    viewModel.likeVideo(videoList[page].id ?: "")
                                    isLiked = true
                                }
                            }
                        )
                    }
                }
            }
            LaunchedEffect(pagerState.currentPage) {
                if (videoList.isNotEmpty()) {
                    viewModel.viewVideo(videoList[pagerState.currentPage].id ?: "")
                }
            }
        } else {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        FloatingActionButton(
            onClick = { navController.navigate("upload") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Upload")
        }
    }
}
