package gr.dimvai.hearth.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gr.dimvai.hearth.R
import gr.dimvai.hearth.ui.theme.HeaderBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HearthHeader(
    title: String,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    showSettingsButton: Boolean = false,
    onSettingsClick: () -> Unit = {},
) {
    Column {
        TopAppBar(
            modifier = Modifier.height(110.dp),
            title = {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .statusBarsPadding()
                        .padding(start = 8.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    if (title == "Hearth") {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = null,
                            modifier = Modifier
                                .size(44.dp)
                                .padding(bottom = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 32.sp
                    )
                }
            },
            navigationIcon = {
                if (showBackButton) {
                    Box(
                        modifier = Modifier.fillMaxHeight().statusBarsPadding(),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.size(56.dp).padding(bottom = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            },
            actions = {
                if (showSettingsButton) {
                    Box(
                        modifier = Modifier.fillMaxHeight().statusBarsPadding(),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        IconButton(
                            onClick = onSettingsClick,
                            modifier = Modifier
                                .padding(bottom = 8.dp, end = 12.dp)
                                .size(56.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = HeaderBackground
            ),
            windowInsets = WindowInsets(0)
        )
        // Προσθήκη κενού χώρου κάτω από το header
        Spacer(modifier = Modifier.height(16.dp))
    }
}
