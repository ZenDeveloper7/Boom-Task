package com.zen.boom.task.screens

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.JsonObject
import com.zen.boom.task.model.Metadata
import com.zen.boom.task.model.VideoModel
import com.zen.boom.task.network.Resource
import com.zen.boom.task.viewmodels.VideoViewModel
import java.io.ByteArrayOutputStream
import java.util.UUID

@Composable
fun UploadVideoScreen(
    navController: NavController,
    videoViewModel: VideoViewModel = viewModel(),
) {
    val context = LocalContext.current
    var videoUri by remember { mutableStateOf<Uri?>(null) }
    var videoThumbnail by remember { mutableStateOf<Bitmap?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableFloatStateOf(0f) }
    var downloadUrl by remember { mutableStateOf<String?>(null) }
    var thumbnailUrl by remember { mutableStateOf<String?>(null) }
    var title by remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        videoUri = uri
        videoThumbnail = uri?.let { getVideoThumbnail(context, it) }
    }

    val uploadResponse by videoViewModel.uploadVideoMutableStateFlow.collectAsState()

    LaunchedEffect(uploadResponse) {
        if (uploadResponse is Resource.Success) {
            Toast.makeText(
                context,
                (uploadResponse as Resource.Success<JsonObject>).data.toString(),
                Toast.LENGTH_SHORT
            ).show()
            navController.navigate("home")
        } else if (uploadResponse is Resource.Error) {
            Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        videoThumbnail?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Video Thumbnail",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(550.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = title,
                onValueChange = { title = it },
                label = { Text("Enter Video Title") })
        }

        if (isUploading) {
            Spacer(modifier = Modifier.height(20.dp))
            LinearProgressIndicator(
                modifier = Modifier,
                color = ProgressIndicatorDefaults.linearColor,
                trackColor = ProgressIndicatorDefaults.linearTrackColor,
                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                progress = { uploadProgress / 100f },
            )
            Text("Uploading: ${uploadProgress.toInt()}%")
        }

        downloadUrl?.let {
            Text("Upload complete!")
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (videoUri != null && videoThumbnail != null && !isUploading && downloadUrl == null) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter), onClick = {
                        if (title.isEmpty()) {
                            Toast.makeText(
                                context, "Please enter a title for the video", Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            isUploading = true
                            val storageRef = Firebase.storage.reference
                            val videoFileName = "videos/${UUID.randomUUID()}.mp4"
                            val thumbFileName = "thumbnails/${UUID.randomUUID()}.jpg"
                            val videoRef = storageRef.child(videoFileName)
                            val thumbRef = storageRef.child(thumbFileName)

                            // Upload video
                            val uploadVideoTask = videoRef.putFile(videoUri!!)

                            uploadVideoTask
                                .addOnProgressListener { taskSnapshot ->
                                    val progress =
                                        100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                                    uploadProgress = progress.toFloat()
                                }
                                .addOnSuccessListener {
                                    videoRef.downloadUrl.addOnSuccessListener { videoUrl ->
                                        // Now upload thumbnail
                                        val baos = ByteArrayOutputStream()
                                        videoThumbnail!!.compress(
                                            Bitmap.CompressFormat.JPEG, 80, baos
                                        )
                                        val thumbBytes = baos.toByteArray()

                                        val uploadThumbTask = thumbRef.putBytes(thumbBytes)
                                        uploadThumbTask.addOnSuccessListener {
                                            thumbRef.downloadUrl.addOnSuccessListener { thumbUrl ->
                                                downloadUrl = videoUrl.toString()
                                                thumbnailUrl = thumbUrl.toString()
                                                isUploading = false
                                                videoViewModel.uploadVideo(
                                                    VideoModel(
                                                        title = title,
                                                        videoUrl = downloadUrl!!,
                                                        thumbnailUrl = thumbnailUrl!!,
                                                        metadata = Metadata(
                                                            uploadedAt = System.currentTimeMillis(),
                                                            likes = emptyList(),
                                                            views = emptyList(),
                                                            uploadedBy = ""
                                                        )
                                                    )
                                                )
                                            }
                                        }.addOnFailureListener {
                                            Toast.makeText(
                                                context,
                                                "Thumbnail upload failed",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            isUploading = false
                                        }
                                    }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        context, "Video upload failed", Toast.LENGTH_SHORT
                                    ).show()
                                    isUploading = false
                                }
                        }
                    }) {
                    Text("Upload Video")
                }
            } else {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    onClick = {
//                        launcher.launch("video/*")
                        videoViewModel.uploadVideo(
                            VideoModel(
                                title = "First Video",
                                videoUrl = "https://firebasestorage.googleapis.com/v0/b/boom-task.firebasestorage.app/o/videos%2F6b4edbea-c9b7-4f19-b467-e624e7120469.mp4?alt=media&token=8633fea2-3be9-42e9-9b7a-ef7061a55801",
                                thumbnailUrl = "https://firebasestorage.googleapis.com/v0/b/boom-task.firebasestorage.app/o/thumbnails%2Fea9f8a39-5f2d-479f-894a-4ac130ca8273.jpg?alt=media&token=7919a40f-20e0-4cb5-a138-817d19745b76",
                                metadata = Metadata(
                                    uploadedAt = 1747916689003,
                                    likes = emptyList(),
                                    views = emptyList(),
                                    uploadedBy = ""
                                )
                            )
                        )
                    }) {
                    Text("Select Video")
                }
            }
        }
    }
}


fun getVideoThumbnail(context: Context, uri: Uri): Bitmap? {
    return try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, uri)
        val bitmap = retriever.frameAtTime // defaults to first frame
        retriever.release()
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}