package com.example.geminiquiz

import com.example.geminiquiz.data.Chat

data class ChatState (
    val chatList: MutableList<Chat> = mutableListOf(),
    val prompt: String = "",
)