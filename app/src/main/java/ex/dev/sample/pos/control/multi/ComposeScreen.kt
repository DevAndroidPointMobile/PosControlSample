package ex.dev.sample.pos.control.multi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Compose UI shown on the MAIN display (displayId = 0).
 *
 * This screen is NOT shown on external displays.
 */
@Composable
fun MainComposeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFCCCC)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "COMPOSE SCREEN",
            fontWeight = FontWeight.Bold,
            fontSize = 48.sp
        )
    }
}
