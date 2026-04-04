package com.example.melodist.ui.components.images

/**
 * Upscales a YouTube thumbnail URL to a target size.
 * Handles both `w226-h226` and `=w120-h120` and `=s120` formats.
 */
fun upscaleThumbnailUrl(url: String?, targetSize: Int): String? {
    if (url == null) return null
    return url
        .replace(Regex("w\\d+-h\\d+"), "w${targetSize}-h${targetSize}")
        .replace(Regex("=w\\d+-h\\d+"), "=w${targetSize}-h${targetSize}")
        .replace(Regex("=s\\d+"), "=s${targetSize}")
}

