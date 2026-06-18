package org.orma.project_90.designsystem

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object OrmaSpacing {
    val CompactRowPadding = 12.dp
    val MediumRowPadding = 14.dp
    val CompactHorizontalPadding = 16.dp
    val ScreenPadding = 20.dp
    val LargeScreenPadding = 24.dp
    val SectionGap = 28.dp
    val SectionGapLarge = 30.dp
    val CenteredContentPadding = 32.dp
    val NavigationBottomPadding = 36.dp
    val HeroBottomOffset = 40.dp

    val BadgeHorizontalPadding = 12.dp
    val BadgeVerticalPadding = 6.dp
    val PrimaryButtonHorizontalPadding = 28.dp
    val PrimaryButtonVerticalPadding = 13.dp
    val CheckoutButtonVerticalPadding = 17.dp
}

object OrmaRadii {
    val Skeleton = 6.dp
    val CheckoutButton = 14.dp
    val SmallCard = 16.dp
    val Field = 18.dp
    val StandardCell = 20.dp
    val PremiumCard = 24.dp
    val Sheet = 28.dp
    val Capsule = 999.dp
}

object OrmaShapes {
    val Skeleton = RoundedCornerShape(OrmaRadii.Skeleton)
    val CheckoutButton = RoundedCornerShape(OrmaRadii.CheckoutButton)
    val SmallCard = RoundedCornerShape(OrmaRadii.SmallCard)
    val Field = RoundedCornerShape(OrmaRadii.Field)
    val StandardCell = RoundedCornerShape(OrmaRadii.StandardCell)
    val PremiumCard = RoundedCornerShape(OrmaRadii.PremiumCard)
    val Sheet = RoundedCornerShape(OrmaRadii.Sheet)
    val Capsule = RoundedCornerShape(OrmaRadii.Capsule)
}

object OrmaMotion {
    const val DirectInteractionMillis = 300
    const val EntranceMillis = 650
    const val SkeletonMillis = 800

    val StandardEasing: Easing = FastOutSlowInEasing
}

object OrmaElevation {
    val None: Dp = 0.dp
    val Subtle: Dp = 3.dp
    val Medium: Dp = 6.dp
    val Prominent: Dp = 10.dp
}

internal val OrmaMaterialShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = OrmaShapes.SmallCard,
    medium = OrmaShapes.StandardCell,
    large = OrmaShapes.PremiumCard,
    extraLarge = OrmaShapes.PremiumCard,
)
