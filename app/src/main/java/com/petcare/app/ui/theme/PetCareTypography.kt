package com.petcare.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.petcare.app.R

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage   = "com.google.android.gms",
    certificates      = R.array.com_google_android_gms_fonts_certs,
)

private val Nunito = FontFamily(
    Font(googleFont = GoogleFont("Nunito"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Nunito"), fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Nunito"), fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = GoogleFont("Nunito"), fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = GoogleFont("Nunito"), fontProvider = provider, weight = FontWeight.ExtraBold),
)

val PetCareTypography = Typography(
    displayLarge  = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 57.sp),
    displayMedium = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold,      fontSize = 45.sp),
    displaySmall  = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold,      fontSize = 36.sp),
    headlineLarge = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold,      fontSize = 32.sp),
    headlineMedium= TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold,      fontSize = 28.sp),
    headlineSmall = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.SemiBold,  fontSize = 24.sp),
    titleLarge    = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold,      fontSize = 22.sp),
    titleMedium   = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.SemiBold,  fontSize = 16.sp),
    titleSmall    = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.SemiBold,  fontSize = 14.sp),
    bodyLarge     = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Normal,    fontSize = 16.sp),
    bodyMedium    = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Normal,    fontSize = 14.sp),
    bodySmall     = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Normal,    fontSize = 12.sp),
    labelLarge    = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.SemiBold,  fontSize = 14.sp),
    labelMedium   = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Medium,    fontSize = 12.sp),
    labelSmall    = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Medium,    fontSize = 11.sp),
)
