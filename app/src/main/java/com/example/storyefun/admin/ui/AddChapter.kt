package com.example.storyefun.admin.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.storyefun.data.repository.ChapterRepository
import com.example.storyefun.ui.theme.LocalAppColors
import com.example.storyefun.viewModel.ChapterViewModel

@Composable
fun AddChapterScreen(navController: NavController, bookId: String, volumeId: String) {
    val theme = LocalAppColors.current
    val context = LocalContext.current
    val repository = remember { ChapterRepository() }
    val viewModel: ChapterViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return ChapterViewModel(repository, bookId, volumeId) as T
            }
        }
    )

    // Lấy trạng thái từ ViewModel
    val chapters by viewModel.chapters.collectAsState()
    // Lấy trực tiếp từ ViewModel mà không cần derivedStateOf
    val imageUris = viewModel.imageUris
    val isUploading = viewModel.isUploading

    // Launcher để chọn ảnh
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        viewModel.updateImageUris(uris)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header với gradient tím
        Box(modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Add chapter",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Danh sách chương đã có
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            items(chapters) { chapter ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .shadow(5.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = chapter.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        chapter.content.forEach { url ->
                            Image(
                                painter = rememberAsyncImagePainter(url),
                                contentDescription = "Chapter Image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            color = Color(0xFFE0E0E0)
        )

        // Phần thêm chương mới
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            OutlinedTextField(
                value = viewModel.title,
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text("📌 Chapter Title", fontSize = 16.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 20.sp),
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(5.dp, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = theme.buttonOrange),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    "Chọn ảnh minh họa",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Hiển thị các ảnh đã chọn
            LazyColumn(modifier = Modifier.heightIn(max = 250.dp)) {
                items(imageUris) { uri ->
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .padding(bottom = 12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    viewModel.uploadChapter {
                        // Xử lý sau khi upload thành công
                    }
                },
                enabled = viewModel.title.isNotBlank() && imageUris.isNotEmpty() && !isUploading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(5.dp, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = theme.buttonOrange,
                    disabledContainerColor = Color(0xFFD3D3D3) // Xám nhạt khi disabled
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    if (isUploading) "Đang tải lên..." else "⬆️ Tải lên chương mới",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}