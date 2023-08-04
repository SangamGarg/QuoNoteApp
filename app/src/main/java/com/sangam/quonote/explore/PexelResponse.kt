package com.sangam.quonote.explore

data class PexelsResponse(
    val photos: List<PexelsPhoto>
)

data class PexelsPhoto(
    val id: Int,
    val src: PexelsPhotoUrls
)

data class PexelsPhotoUrls(
    val small: String,
    val original: String,
    val tiny:String
)