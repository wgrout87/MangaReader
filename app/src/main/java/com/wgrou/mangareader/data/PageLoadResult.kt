package com.wgrou.mangareader.data

import java.io.File

sealed class PageLoadResult {

    data class Success(
        val pages: List<File>
    ) : PageLoadResult()

    object MissingChapter : PageLoadResult()

    object NoPages : PageLoadResult()

}