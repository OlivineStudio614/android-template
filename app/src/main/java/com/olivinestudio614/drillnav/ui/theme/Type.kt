package com.olivinestudio614.drillnav.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.olivinestudio614.drillnav.R

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val SpecialElite = FontFamily(
    Font(GoogleFont("Special Elite"), provider)
)

val MilitaryTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = SpecialElite,
        fontWeight = FontWeight.Normal,
        fontSize = 48.sp,
        color = OffWhite
    ),
    headlineLarge = TextStyle(
        fontFamily = SpecialElite,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        color = OffWhite
    ),
    headlineMedium = TextStyle(
        fontFamily = SpecialElite,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        color = OffWhite
    ),
    titleLarge = TextStyle(
        fontFamily = SpecialElite,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        color = OffWhite
    ),
    bodyLarge = TextStyle(
        fontFamily = SpecialElite,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        color = OffWhite
    ),
    labelMedium = TextStyle(
        fontFamily = SpecialElite,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        color = OffWhite
    )
)
