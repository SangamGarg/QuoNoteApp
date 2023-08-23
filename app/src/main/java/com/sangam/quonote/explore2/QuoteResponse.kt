package com.sangam.quonote.explore2
data class QuoteResponse(
    val quotes: List<QuoteResponseItem>
)
data class QuoteResponseItem(
    val quote: String,
    val author:String
)