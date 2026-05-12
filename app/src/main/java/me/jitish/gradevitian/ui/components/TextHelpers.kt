package me.jitish.gradevitian.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

fun boldTextParts(
    text: String,
    vararg partsToBold: String,
    ignoreCase: Boolean = false
): AnnotatedString = buildAnnotatedString {
    val parts = partsToBold.filter { it.isNotEmpty() }
    if (parts.isEmpty()) {
        append(text)
        return@buildAnnotatedString
    }

    var currentIndex = 0
    while (currentIndex < text.length) {
        val nextMatch = parts
            .mapNotNull { part ->
                val start = text.indexOf(part, startIndex = currentIndex, ignoreCase = ignoreCase)
                if (start >= 0) part to start else null
            }
            .minWithOrNull(compareBy<Pair<String, Int>> { it.second }.thenByDescending { it.first.length })

        if (nextMatch == null) {
            append(text.substring(currentIndex))
            break
        }

        val (part, start) = nextMatch
        if (start > currentIndex) {
            append(text.substring(currentIndex, start))
        }

        val end = start + part.length
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append(text.substring(start, end))
        }
        currentIndex = end
    }
}
