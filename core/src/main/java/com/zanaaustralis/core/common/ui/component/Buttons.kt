package com.zanaaustralis.core.common.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zanaaustralis.core.common.ui.theme.NavTechTheme

/**
 * @author esteban.rivera01
 */

@Composable
fun NavTechButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    val colors = NavTechTheme.colors

    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (enabled) colors.primary else Color(0xFFE0E0E0),
            contentColor = if (enabled) Color.White else Color(0xFF9E9E9E),
            disabledContainerColor = Color(0xFFE0E0E0),
            disabledContentColor = Color(0xFF9E9E9E)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (enabled) 2.dp else 0.dp
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color =  colors.primary,
                strokeWidth = 2.dp,
                modifier = Modifier.size(18.dp)
            )
        } else {
            Text(
                text = text,
                style = NavTechTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun NavTechButtonSecondary(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val colors = NavTechTheme.colors

    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = 1.dp,
            color = colors.primary
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = colors.primary
        )
    ) {

        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )

    }
}

@Composable
fun NavTechButtonGhost(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val colors = NavTechTheme.colors

    TextButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {

        Text(
            text = text,
            color = colors.textSecondary,
            style = MaterialTheme.typography.labelLarge
        )

    }
}

@Composable
fun NavTechButtonDanger(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val colors = NavTechTheme.colors

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.error,
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {

        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )

    }
}

@Preview(showBackground = true)
@Composable
fun PreviewNavTechButtons() {

    NavTechTheme {

        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            NavTechButton(
                text = "Comenzar",
                onClick = {}
            )

            NavTechButtonSecondary(
                text = "Verificar",
                onClick = {}
            )

            NavTechButtonGhost(
                text = "Cancelar",
                onClick = {}
            )

            NavTechButtonDanger(
                text = "Eliminar dispositivo",
                onClick = {}
            )
        }

    }
}