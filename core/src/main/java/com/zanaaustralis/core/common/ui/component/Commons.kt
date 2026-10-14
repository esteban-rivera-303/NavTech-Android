package com.zanaaustralis.core.common.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zanaaustralis.core.R
import com.zanaaustralis.core.common.ui.theme.NavTechTheme

/**
 * @author esteban.rivera01
 */

@Composable
fun NavTechHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    showBack: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    rightContent: (@Composable RowScope.() -> Unit)? = null
) {

    val colors = NavTechTheme.colors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(170.dp)
            .background(colors.primary)
    ) {

        // 🔷 Logo decorativo
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = 20.dp)
                .size(160.dp)
                .rotate(18f)
                .alpha(0.08f)
        )

        // 🔙 Back + acciones
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            if (showBack && onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }

            Row {
                rightContent?.invoke(this)
            }
        }

        // 📝 Textos
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {

            Text(
                text = title,
                style = NavTechTheme.typography.headlineMedium,
                color = Color.White
            )

            if (subtitle != null) {
                Spacer(Modifier.height(6.dp))

                Text(
                    text = subtitle,
                    style = NavTechTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
fun NavTechWarningCard(
    pendingFields: List<String>
) {

    if (pendingFields.isEmpty()) return

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF7EED4)
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFFE09000)
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    "Aún necesitas validar lo siguiente",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(8.dp))

            pendingFields.forEach {
                Text("• $it")
            }

            Spacer(Modifier.height(6.dp))

            Text(
                "Por favor completar para continuar.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun NavTechVerificationCard(
    index: String,
    title: String,
    subtitle: String,
    verified: Boolean,
    onClick: () -> Unit
) {

    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Row(verticalAlignment = Alignment.CenterVertically) {

                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(
                                Color(0xFFFF8A00),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            index,
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    Text(
                        title,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (!verified) {

                    Box(
                        modifier = Modifier
                            .border(
                                1.dp,
                                Color.Red,
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 2.dp)
                    ) {

                        Text(
                            "Pendiente",
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(Modifier.height(8.dp))

            if (!verified) {

                TextButton(onClick = onClick) {
                    Text("Verificar aquí")
                }
            }
        }
    }
}

@Composable
fun NavTechPhoneInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Escribe tu número"
) {

    val colors = NavTechTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(colors.surface, RoundedCornerShape(12.dp))
            .border(1.dp, colors.border, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = "+56",
            style = NavTechTheme.typography.bodyMedium,
            color = colors.textPrimary
        )

        Spacer(Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .width(1.dp)
                .height(24.dp)
                .background(colors.divider)
        )

        Spacer(Modifier.width(8.dp))

        BasicTextField(
            value = value,
            onValueChange = { input ->
                // 🔒 solo números
                val filtered = input.filter { it.isDigit() }
                onValueChange(filtered)
            },
            modifier = Modifier.weight(1f),
            textStyle = NavTechTheme.typography.bodyMedium.copy(
                color = colors.textPrimary
            ),
            singleLine = true,
            decorationBox = { innerTextField ->

                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = NavTechTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                }

                innerTextField()
            }
        )
    }
}