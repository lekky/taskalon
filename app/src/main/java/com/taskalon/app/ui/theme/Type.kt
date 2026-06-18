@file:OptIn(ExperimentalTextApi::class)

package com.taskalon.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.taskalon.app.R
import com.taskalon.app.data.AppFont

/**
 * The design's brand families, loaded as Google downloadable fonts via Play Services
 * (`font_certs.xml` holds the standard provider certificates). If the provider is
 * unavailable at runtime, Compose falls back to the platform default automatically.
 */
private val googleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private fun brandFamily(name: String): FontFamily {
    val font = GoogleFont(name)
    return FontFamily(
        Font(font, googleFontProvider, FontWeight.Normal),
        Font(font, googleFontProvider, FontWeight.Medium),
        Font(font, googleFontProvider, FontWeight.SemiBold),
        Font(font, googleFontProvider, FontWeight.Bold),
    )
}

private val SplineSans = brandFamily("Spline Sans")
private val IbmPlexSans = brandFamily("IBM Plex Sans")
private val Newsreader = brandFamily("Newsreader")
private val JetBrainsMono = brandFamily("JetBrains Mono")

/** Maps the user's font choice to its brand [FontFamily]. */
fun AppFont.toFontFamily(): FontFamily = when (this) {
    AppFont.SPLINE -> SplineSans
    AppFont.PLEX -> IbmPlexSans
    AppFont.SERIF -> Newsreader
    AppFont.MONO -> JetBrainsMono
}

fun taskalonTypography(family: FontFamily) = Typography(
    bodyLarge = TextStyle(fontFamily = family, fontSize = 15.sp),
    bodyMedium = TextStyle(fontFamily = family, fontSize = 13.sp),
    labelLarge = TextStyle(fontFamily = family, fontSize = 14.sp, fontWeight = FontWeight.Medium),
)

/**
 * The bespoke type scale from the handoff. Apply the app font at the call site with
 * `.copy(fontFamily = LocalAppFontFamily.current)`.
 */
object TkText {
    val wordmark = TextStyle(fontSize = 19.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.01).em)
    val pageTitle = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.02).em)
    val sheetTitle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
    val sectionLabel = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.08.em)
    val editorTitle = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.015).em, lineHeight = 31.sp)
    val cardTitle = TextStyle(fontSize = 15.5.sp, fontWeight = FontWeight.SemiBold, lineHeight = 21.7.sp)
    val cardNotes = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Normal, lineHeight = 18.85.sp)
    val body = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Normal)
    val bodyMed = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium)
    val chip = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium)
    val chipActive = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    val badge = TextStyle(fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
    val badgeStrong = TextStyle(fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
    val toast = TextStyle(fontSize = 13.5.sp, fontWeight = FontWeight.Medium)
}
