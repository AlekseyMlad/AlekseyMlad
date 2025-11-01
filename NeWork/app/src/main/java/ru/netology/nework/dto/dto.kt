package ru.netology.nework.dto

enum class AttachmentType {
    IMAGE, VIDEO, AUDIO
}

data class Attachment(
    val url: String,
    val type: AttachmentType,
)

data class Coordinates(
    val lat: Double,
    val long: Double,
)

data class Post(
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorJob: String? = null,
    val authorAvatar: String? = null,
    val content: String,
    val published: String,
    val coords: Coordinates? = null,
    val link: String? = null,
    val mentionIds: Set<Long> = emptySet(),
    val mentionedMe: Boolean = false,
    val likeOwnerIds: Set<Long> = emptySet(),
    val likedByMe: Boolean = false,
    val attachment: Attachment? = null,
    val users: Map<Long, UserPreview> = emptyMap(),
)

data class UserPreview(
    val id: Long,
    val name: String,
    val avatar: String? = null,
)

data class Token(
    val id: Long,
    val token: String,
    val avatar: String? = null,
)

data class Job(
    val id: Long,
    val name: String,
    val position: String,
    val start: String,
    val finish: String? = null,
    val link: String? = null,
)

data class JobItem(val job: Job) : DisplayableItem {
    override val id: Long = job.id
}

data class Media(val url: String)

enum class EventType {
    OFFLINE, ONLINE
}

data class Event(
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorJob: String? = null,
    val authorAvatar: String? = null,
    val content: String,
    val datetime: String,
    val published: String,
    val coords: Coordinates? = null,
    val type: EventType,
    val likeOwnerIds: Set<Long> = emptySet(),
    val likedByMe: Boolean = false,
    val speakerIds: Set<Long> = emptySet(),
    val participantsIds: Set<Long> = emptySet(),
    val participatedByMe: Boolean = false,
    val attachment: Attachment? = null,
    val link: String? = null,
    val speakers: List<UserPreview> = emptyList(),
    val participants: List<UserPreview> = emptyList(),
    val users: Map<Long, UserPreview> = emptyMap(),
)

data class UserResponse(
    val id: Long,
    val login: String,
    val name: String,
    val avatar: String? = null,
)
