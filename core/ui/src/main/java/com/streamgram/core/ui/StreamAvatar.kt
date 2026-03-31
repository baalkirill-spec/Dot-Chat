package com.streamgram.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun StreamAvatar(
    imageUrl: String?,
    fallbackLabel: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
) {
    val avatarModifier = modifier
        .size(size)
        .clip(CircleShape)

    if (imageUrl.isNullOrBlank()) {
        Box(
            modifier = avatarModifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = fallbackLabel.take(2).uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    } else {
        AsyncImage(
            model = imageUrl,
            contentDescription = fallbackLabel,
            modifier = avatarModifier,
            contentScale = ContentScale.Crop,
        )
    }
}
