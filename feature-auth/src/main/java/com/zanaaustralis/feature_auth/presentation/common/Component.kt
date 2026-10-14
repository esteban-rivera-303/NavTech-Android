package com.zanaaustralis.feature_auth.presentation.common

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zanaaustralis.core.common.ui.theme.NavTechTheme

/**
 * @author esteban.rivera01
 */

@Composable
fun CodeInputRow(
    code: List<String>,
    onValueChange: (index: Int, value: String) -> Unit
) {

    val focusRequesters = remember { List(6) { FocusRequester() } }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        code.forEachIndexed { index, value ->

            BasicTextField(
                value = value,
                onValueChange = { input ->

                    // 👉 pegar código completo
                    if (input.length > 1) {
                        input.take(6).forEachIndexed { i, c ->
                            onValueChange(i, c.toString())
                        }
                        return@BasicTextField
                    }

                    if (input.all { it.isDigit() }) {
                        onValueChange(index, input)

                        if (input.isNotEmpty() && index < 5) {
                            focusRequesters[index + 1].requestFocus()
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword
                ),
                textStyle = TextStyle(
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp
                ),
                modifier = Modifier
                    .weight(1f) // 🔥 clave para que entren los 6
                    .aspectRatio(1f)
                    .border(
                        1.dp,
                        NavTechTheme.colors.textDisabled,
                        RoundedCornerShape(12.dp)
                    )
                    .focusRequester(focusRequesters[index])
                    .onKeyEvent { event ->
                        if (event.key == Key.Backspace &&
                            event.type == KeyEventType.KeyDown
                        ) {
                            if (code[index].isEmpty() && index > 0) {
                                focusRequesters[index - 1].requestFocus()
                            }
                        }
                        false
                    },
                singleLine = true,
                decorationBox = {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        it()
                    }
                }
            )
        }
    }

    LaunchedEffect(Unit) {
        focusRequesters.first().requestFocus()
    }
}