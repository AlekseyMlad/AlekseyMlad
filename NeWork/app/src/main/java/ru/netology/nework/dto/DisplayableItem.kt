package ru.netology.nework.dto

sealed interface DisplayableItem {
    val id: Long
}

data class PostItem(val post: Post) : DisplayableItem {
    override val id: Long = post.id
}

data class UserItem(val user: UserResponse) : DisplayableItem {
    override val id: Long = user.id
}

data class EventItem(val event: Event) : DisplayableItem {
    override val id: Long = event.id
}
