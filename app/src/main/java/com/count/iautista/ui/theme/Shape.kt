package com.count.iautista.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// V2: raios maiores = mais amigável, sem perder seriedade
val IautistaShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small      = RoundedCornerShape(12.dp),
    medium     = RoundedCornerShape(16.dp),
    large      = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

val ShapeCard           = RoundedCornerShape(20.dp)   // cards principais
val ShapeButton         = RoundedCornerShape(16.dp)   // botões e ações
val ShapeChip           = RoundedCornerShape(24.dp)   // chips e filtros
val ShapeModal          = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
val ShapeCircle         = RoundedCornerShape(50)
val ShapeEmojiContainer = RoundedCornerShape(14.dp)   // container de emoji nos cards AAC
