package com.zanaaustralis.feature_auth.presentation.pin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zanaaustralis.core.common.ui.component.NavTechHeader
import com.zanaaustralis.core.common.ui.theme.NavTechTheme
import com.zanaaustralis.feature_auth.R

/**
 * @author esteban.rivera01
 */
@Composable
fun PinInput(
    onComplete: (String) -> Unit,
    error: String? = null,
    style: PinUiStyle
) {
    var pin by remember { mutableStateOf("") }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        PinDots(
            filledCount = pin.length,
            total = 4,
            style = style
        )

        Spacer(Modifier.height(24.dp))

        TextField(
            value = pin,
            onValueChange = {
                if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                    pin = it
                    if (it.length >= 4) {
                        onComplete(it)
                    }
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.width(1.dp).alpha(0f)
        )

        error?.let {
            Spacer(Modifier.height(12.dp))
            Text(
                text = it,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
@Composable
fun PinKeyboard(
    style: PinUiStyle,
    onNumber: (String) -> Unit,
    onDelete: () -> Unit,
    onBiometric: () -> Unit = {}
) {
    val buttons = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("face", "0", "←")
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        val keySize = when {
            maxWidth < 340.dp -> 56.dp
            maxWidth < 400.dp -> 60.dp
            else -> 64.dp
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            buttons.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    row.forEach { label ->
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            when (label) {
                                "face" -> PinIconKey(
                                    size = keySize,
                                    iconSize = 28.dp,
                                    icon = R.drawable.ic_face_id,
                                    tint = style.keyTextColor,
                                    contentDescription = "Biometría",
                                    onClick = onBiometric
                                )

                                "←" -> PinIconKey(
                                    size = keySize,
                                    iconSize = 28.dp,
                                    icon = R.drawable.baseline_keyboard_backspace_24,
                                    tint = style.keyTextColor,
                                    contentDescription = "Borrar",
                                    onClick = onDelete
                                )

                                else -> PinNumberKey(
                                    label = label,
                                    size = keySize,
                                    style = style,
                                    onClick = { onNumber(label) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PinNumberKey(
    label: String,
    size: Dp,
    style: PinUiStyle,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(style.keyBackgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = style.keyTextColor,
            fontSize = 28.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PinIconKey(
    size: Dp,
    iconSize: Dp,
    icon: Int,
    tint: Color,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}
/*@Composable
fun PinKeyboard(
    style: PinUiStyle,
    onNumber: (String) -> Unit,
    onDelete: () -> Unit
) {

    val buttons = listOf(
        listOf("1","2","3"),
        listOf("4","5","6"),
        listOf("7","8","9"),
        listOf("face","0","←")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        buttons.forEach { row ->

            Row(
                horizontalArrangement = Arrangement.spacedBy(64.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                row.forEach { label ->

                    when(label) {

                        "face" -> IconButton(onClick = { }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_face_id),
                                contentDescription = null,
                                tint = style.keyTextColor,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        "←" -> IconButton(onClick = onDelete) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_keyboard_backspace_24),
                                contentDescription = "Borrar",
                                tint = style.keyTextColor,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        else -> Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(
                                    style.keyBackgroundColor,
                                    CircleShape
                                )
                                .clickable { onNumber(label) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = style.keyTextColor,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}*/

@Composable
fun PinDots(
    filledCount: Int,
    total: Int = 6,
    style: PinUiStyle
) {

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

        repeat(total) { index ->

            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(
                        if (index < filledCount)
                            style.dotFilledColor
                        else
                            style.dotEmptyColor,
                        CircleShape
                    )
            )
        }
    }
}

/*@Composable
fun PinInput(
    onComplete: (String) -> Unit,
    error: String? = null
) {
    var pin by remember { mutableStateOf("") }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(
                            if (index < pin.length) NavTechTheme.colors.primary else Color.LightGray,
                            CircleShape
                        )
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        TextField(
            value = pin,
            onValueChange = {
                if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                    pin = it
                    if (it.length >= 4) {
                        onComplete(it)
                    }
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.width(1.dp).alpha(0f) // invisible
        )

        if (error != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = error,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun PinKeyboard(
    onNumber: (String) -> Unit,
    onDelete: () -> Unit
) {

    val buttons = listOf(
        listOf("1","2","3"),
        listOf("4","5","6"),
        listOf("7","8","9"),
        listOf("face","0","←")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        buttons.forEach { row ->

            Row(
                horizontalArrangement = Arrangement.spacedBy(64.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                row.forEach { label ->

                    when(label) {

                        "face" -> IconButton(
                            onClick = { *//* biometría futura *//* }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_face_id),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        "←" -> IconButton(
                            onClick = onDelete
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_keyboard_backspace_24),
                                contentDescription = "Borrar",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        else -> Text(
                            text = label,
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .size(64.dp)
                                .wrapContentSize()
                                .clickable { onNumber(label) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PinDots(
    filledCount: Int,
    total: Int = 6
) {

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        repeat(total) { index ->

            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(
                        if (index < filledCount)
                            Color.White
                        else
                            Color.White.copy(alpha = 0.4f),
                        CircleShape
                    )
            )
        }
    }
}*/


enum class PinStyle {
    AUTHORIZATION,
    CREATION
}

data class PinUiStyle(
    val textColor: Color,
    val backgroundColor: Color,
    val dotFilledColor: Color,
    val dotEmptyColor: Color,
    val keyTextColor: Color,
    val keyBackgroundColor: Color
)

val PinAuthStyle = PinUiStyle(
    textColor = Color.White,
    backgroundColor = Color.Transparent,
    dotFilledColor = Color.White,
    dotEmptyColor = Color.White.copy(alpha = 0.4f),
    keyTextColor = Color.White,
    keyBackgroundColor = Color.Transparent
)

val PinCreateStyle = PinUiStyle(
    textColor = Color.Black,
    backgroundColor = Color.White,
    dotFilledColor = Color(0xFF1E4FD8),
    dotEmptyColor = Color.LightGray,
    keyTextColor = Color.Black,
    keyBackgroundColor = Color(0xFFEAEAEA)
)
// Preview

@Preview(
    showBackground = true,
    name = "PinInput – Normal"
)
@Composable
fun PinInputPreview() {
    MaterialTheme {
        Surface {
            PinInput(
                onComplete = {},
                error = null,
                style = PinAuthStyle
            )
        }
    }
}

@Preview(
    showBackground = true,
    name = "PinInput – Error"
)
@Composable
fun PinInputErrorPreview() {
    MaterialTheme {
        Surface {
            PinInput(
                onComplete = {},
                error = "PIN incorrecto",
                style = PinCreateStyle
            )
        }
    }
}

@Preview(
    showBackground = true,
    name = "PinKeyboard"
)
@Composable
fun PinKeyboardPreview() {
    MaterialTheme {
        Surface {
            PinKeyboard(
                onNumber = {},
                onDelete = {},
                style = PinCreateStyle
            )
        }
    }
}
