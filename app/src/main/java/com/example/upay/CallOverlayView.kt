package com.example.upay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CallOverlayView(onForwardClick: () -> Unit, onReportClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Incoming Call Action",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 24.dp) // Increased bottom padding for title
            )
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onForwardClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50), // Green color
                        contentColor = Color.White
                    ),
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp) // Added weight and padding
                ) {
                    Text("Forward")
                }
                Button(
                    onClick = onReportClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336), // Red color
                        contentColor = Color.White
                    ),
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp) // Added weight and padding
                ) {
                    Text("Report")
                }
            }
        }
    }
}
