package org.orma.project_90.designsystem

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun OrmaWebTypographyProvider(
    content: @Composable () -> Unit,
) {
    val googleSans = GoogleSansFontFamily()
    OrmaTypographyOverride(
        typography = Typography(
            displayLarge = TextStyle(
                fontFamily = googleSans,
                fontWeight = FontWeight.Medium,
                fontSize = 44.sp,
                lineHeight = 48.sp,
                letterSpacing = 0.sp,
            ),
            displayMedium = TextStyle(
                fontFamily = googleSans,
                fontWeight = FontWeight.Medium,
                fontSize = 34.sp,
                lineHeight = 38.sp,
                letterSpacing = 0.sp,
            ),
            displaySmall = TextStyle(
                fontFamily = googleSans,
                fontWeight = FontWeight.Medium,
                fontSize = 30.sp,
                lineHeight = 34.sp,
                letterSpacing = 0.sp,
            ),
            headlineLarge = TextStyle(
                fontFamily = googleSans,
                fontWeight = FontWeight.Medium,
                fontSize = 32.sp,
                lineHeight = 36.sp,
                letterSpacing = 0.sp,
            ),
            headlineMedium = TextStyle(
                fontFamily = googleSans,
                fontWeight = FontWeight.Medium,
                fontSize = 28.sp,
                lineHeight = 32.sp,
                letterSpacing = 0.sp,
            ),
            headlineSmall = TextStyle(
                fontFamily = googleSans,
                fontWeight = FontWeight.Medium,
                fontSize = 24.sp,
                lineHeight = 28.sp,
                letterSpacing = 0.sp,
            ),
            titleLarge = TextStyle(
                fontFamily = googleSans,
                fontWeight = FontWeight.Medium,
                fontSize = 26.sp,
                lineHeight = 30.sp,
                letterSpacing = 0.sp,
            ),
            titleMedium = TextStyle(
                fontFamily = googleSans,
                fontWeight = FontWeight.Medium,
                fontSize = 22.sp,
                lineHeight = 26.sp,
                letterSpacing = 0.sp,
            ),
            titleSmall = TextStyle(
                fontFamily = googleSans,
                fontWeight = FontWeight.Medium,
                fontSize = 17.5.sp,
                lineHeight = 22.sp,
                letterSpacing = 0.sp,
            ),
            bodyLarge = TextStyle(
                fontFamily = googleSans,
                fontWeight = FontWeight.Normal,
                fontSize = 15.5.sp,
                lineHeight = 22.sp,
                letterSpacing = 0.sp,
            ),
            bodyMedium = TextStyle(
                fontFamily = googleSans,
                fontWeight = FontWeight.Normal,
                fontSize = 14.5.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.sp,
            ),
            bodySmall = TextStyle(
                fontFamily = googleSans,
                fontWeight = FontWeight.Normal,
                fontSize = 12.5.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.sp,
            ),
            labelLarge = TextStyle(
                fontFamily = googleSans,
                fontWeight = FontWeight.Medium,
                fontSize = 15.5.sp,
                lineHeight = 18.sp,
                letterSpacing = 0.sp,
            ),
            labelMedium = TextStyle(
                fontFamily = googleSans,
                fontWeight = FontWeight.Medium,
                fontSize = 13.5.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.sp,
            ),
            labelSmall = TextStyle(
                fontFamily = googleSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.5.sp,
                lineHeight = 14.sp,
                letterSpacing = 1.35.sp,
            ),
        ),
        content = content,
    )
}
