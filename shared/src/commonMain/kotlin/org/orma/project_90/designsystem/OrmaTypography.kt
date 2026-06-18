package org.orma.project_90.designsystem

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import orma.shared.generated.resources.Res
import orma.shared.generated.resources.google_sans_bold
import orma.shared.generated.resources.google_sans_medium
import orma.shared.generated.resources.google_sans_regular
import orma.shared.generated.resources.google_sans_semibold

@Composable
fun GoogleSansFontFamily(): FontFamily = FontFamily(
    Font(Res.font.google_sans_regular, FontWeight.Normal),
    Font(Res.font.google_sans_medium, FontWeight.Medium),
    Font(Res.font.google_sans_semibold, FontWeight.SemiBold),
    Font(Res.font.google_sans_bold, FontWeight.Bold),
)

@Composable
internal fun ormaTypography(): Typography {
    val googleSans = GoogleSansFontFamily()

    return Typography(
        displayLarge = TextStyle(
            fontFamily = googleSans,
            fontWeight = FontWeight.Normal,
            fontSize = 44.sp,
            lineHeight = 48.sp,
            letterSpacing = 0.sp,
        ),
        displayMedium = TextStyle(
            fontFamily = googleSans,
            fontWeight = FontWeight.Normal,
            fontSize = 34.sp,
            lineHeight = 38.sp,
            letterSpacing = 0.sp,
        ),
        headlineMedium = TextStyle(
            fontFamily = googleSans,
            fontWeight = FontWeight.Normal,
            fontSize = 28.sp,
            lineHeight = 32.sp,
            letterSpacing = 0.sp,
        ),
        titleLarge = TextStyle(
            fontFamily = googleSans,
            fontWeight = FontWeight.Normal,
            fontSize = 26.sp,
            lineHeight = 30.sp,
            letterSpacing = 0.sp,
        ),
        titleMedium = TextStyle(
            fontFamily = googleSans,
            fontWeight = FontWeight.Normal,
            fontSize = 22.sp,
            lineHeight = 26.sp,
            letterSpacing = 0.sp,
        ),
        titleSmall = TextStyle(
            fontFamily = googleSans,
            fontWeight = FontWeight.Normal,
            fontSize = 17.sp,
            lineHeight = 22.sp,
            letterSpacing = 0.sp,
        ),
        bodyLarge = TextStyle(
            fontFamily = googleSans,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            letterSpacing = 0.sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = googleSans,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.sp,
        ),
        labelLarge = TextStyle(
            fontFamily = googleSans,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            lineHeight = 18.sp,
            letterSpacing = 0.sp,
        ),
        labelMedium = TextStyle(
            fontFamily = googleSans,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.sp,
        ),
        labelSmall = TextStyle(
            fontFamily = googleSans,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            letterSpacing = 1.5.sp,
        ),
    )
}
