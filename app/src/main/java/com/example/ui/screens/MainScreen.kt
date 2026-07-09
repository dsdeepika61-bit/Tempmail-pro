package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.text.Html
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.MainViewModel
import com.example.api.MessageDetail
import com.example.api.MessageIntro
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(viewModel = viewModel, onNavigateToInbox = {
                navController.navigate("inbox")
            }, onNavigateToDetail = { id ->
                viewModel.openMessage(id)
                navController.navigate("detail/$id")
            })
        }
        composable("inbox") {
            InboxScreen(viewModel = viewModel, onNavigateBack = {
                navController.popBackStack()
            }, onNavigateToDetail = { id ->
                viewModel.openMessage(id)
                navController.navigate("detail/$id")
            })
        }
        composable("detail/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: return@composable
            EmailDetailScreen(viewModel = viewModel, onNavigateBack = {
                viewModel.closeMessage()
                navController.popBackStack()
            })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: MainViewModel, onNavigateToInbox: () -> Unit, onNavigateToDetail: (String) -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val address by viewModel.authManager.addressFlow.collectAsState(initial = null)
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White.copy(alpha = 0.8f)
                ),
                title = { Text("TempMail AI", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
                actions = {
                    IconButton(onClick = { viewModel.refreshInbox() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.onBackground)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (address == null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                        Text("Welcome to TempMail AI", style = MaterialTheme.typography.titleLarge)
                        Text("Get a temporary, disposable email address instantly.", style = MaterialTheme.typography.bodyMedium)
                        
                        if (uiState.isLoading) {
                            CircularProgressIndicator()
                        } else {
                            Button(onClick = { viewModel.generateNewEmail() }) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generate New Email")
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(40.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = androidx.compose.foundation.shape.CircleShape
                            ) {
                                Text(
                                    "ACTIVE SESSION",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                        
                        Column {
                            Text("Generated Email Address", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.8f))
                            Text(
                                text = address!!,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("email", address)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f).height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Copy", fontWeight = FontWeight.Bold)
                            }
                            
                            Button(
                                onClick = { viewModel.generateNewEmail() },
                                modifier = Modifier.size(48.dp),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = Color.White
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Get New Mail")
                            }
                        }
                    }
                }
                
                val otpRegex = "\\b\\d{4,8}\\b".toRegex()
                val recentOtpMessage = uiState.messages.firstOrNull { !it.seen && (otpRegex.find(it.subject) != null || otpRegex.find(it.intro) != null) }
                val topOtpMatch = recentOtpMessage?.let { otpRegex.find(it.subject) ?: otpRegex.find(it.intro) }
                
                if (topOtpMatch != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFecfdf5)), // Emerald 50
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFd1fae5)) // Emerald 100
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(40.dp),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                                    color = Color(0xFF10b981) // Emerald 500
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.padding(8.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("VERIFICATION CODE FOUND", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669), letterSpacing = 1.sp)
                                    Text(topOtpMatch.value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF064e3b), letterSpacing = 4.sp)
                                }
                            }
                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("otp", topOtpMatch.value)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "OTP Copied", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10b981), contentColor = Color.White), // Emerald 500
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                modifier = Modifier.height(40.dp)
                            ) {
                                Text("Copy OTP", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recent Messages", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                    TextButton(onClick = onNavigateToInbox) {
                        Text("View All (${uiState.messages.size})", fontWeight = FontWeight.Bold)
                    }
                }
                
                if (uiState.isLoading) {
                    CircularProgressIndicator()
                } else if (uiState.messages.isEmpty()) {
                    Text("No messages yet", color = Color.Gray)
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.messages.take(3)) { message ->
                            EmailListItem(
                                message = message,
                                onClick = { onNavigateToDetail(message.id) },
                                onDelete = { viewModel.deleteMessage(message.id) }
                            )
                        }
                    }
                }

                TextButton(onClick = { viewModel.deleteCurrentAccount() }) {
                    Text("Delete Account", color = MaterialTheme.colorScheme.error)
                }
            }

            uiState.error?.let {
                Text(text = "Error: $it", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(viewModel: MainViewModel, onNavigateBack: () -> Unit, onNavigateToDetail: (String) -> Unit) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inbox") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshInbox() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.messages.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Inbox is empty", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(uiState.messages) { message ->
                    EmailListItem(
                        message = message,
                        onClick = { onNavigateToDetail(message.id) },
                        onDelete = { viewModel.deleteMessage(message.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailListItem(message: MessageIntro, onClick: () -> Unit, onDelete: () -> Unit) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }
    
    val otpRegex = "\\b\\d{4,8}\\b".toRegex()
    val otpMatch = otpRegex.find(message.subject) ?: otpRegex.find(message.intro)

    SwipeToDismissBox(
        state = rememberSwipeToDismissBoxState(
            confirmValueChange = {
                if (it == SwipeToDismissBoxValue.EndToStart) {
                    onDelete()
                    true
                } else false
            }
        ),
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
            }
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp, vertical = 0.dp)
                .clickable { onClick() },
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.Top) {
                // Initial Circle
                val initial = message.from.address.firstOrNull()?.uppercase() ?: "?"
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = initial,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = message.from.name.ifEmpty { message.from.address.substringBefore("@") }, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, modifier = Modifier.weight(1f))
                        Text(text = formatDate(message.createdAt), style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(start = 8.dp))
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = message.subject.ifEmpty { "(No Subject)" }, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground, maxLines = 1)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = message.intro, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1)
                    
                    if (otpMatch != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                                color = Color(0xFFecfdf5) // Emerald 50
                            ) {
                                Text(
                                    "OTP: ${otpMatch.value}",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF059669) // Emerald 600
                                )
                            }
                        }
                    }
                }
                
                if (!message.seen) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.size(10.dp).align(Alignment.CenterVertically)) {
                        Surface(modifier = Modifier.fillMaxSize(), shape = androidx.compose.foundation.shape.CircleShape, color = MaterialTheme.colorScheme.primary) {}
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailDetailScreen(viewModel: MainViewModel, onNavigateBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val detail = uiState.selectedMessage
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = { Text("Email", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    detail?.let {
                        IconButton(onClick = {
                            viewModel.deleteMessage(it.id)
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isMessageLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (detail != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text(text = detail.subject.ifEmpty { "(No Subject)" }, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(detail.from.name.ifEmpty { detail.from.address.substringBefore("@") }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(detail.from.address, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    Text(formatDate(detail.createdAt), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(16.dp))
                
                val otpRegex = "\\b\\d{4,8}\\b".toRegex()
                val plainText = detail.text ?: ""
                val otpMatch = otpRegex.find(plainText) ?: otpRegex.find(detail.subject)
                
                if (otpMatch != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFecfdf5)), // Emerald 50
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFd1fae5)) // Emerald 100
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("VERIFICATION CODE FOUND", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669), letterSpacing = 1.sp)
                                Text(otpMatch.value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF064e3b), letterSpacing = 4.sp)
                            }
                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("otp", otpMatch.value)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "OTP Copied", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10b981), contentColor = Color.White),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                            ) {
                                Text("Copy", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text(
                    text = plainText,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Message not found", color = Color.Gray)
            }
        }
    }
}

fun formatDate(isoString: String): String {
    try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val date = parser.parse(isoString)
        val formatter = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        return if (date != null) formatter.format(date) else isoString
    } catch (e: Exception) {
        return isoString
    }
}
