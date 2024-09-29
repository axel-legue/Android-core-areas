package com.skitt31.navigation

import kotlinx.serialization.Serializable


// The param of the data class are the arguments that will be passed to the destination.
// Nullable arguments are optional.
@Serializable
data class Profile(val name: String, val nickName: String? = null)

@Serializable
object FriendsList

@Serializable
object Settings

// Route for nested graph
@Serializable object Game

// Routes inside nested graph
@Serializable object Match
@Serializable data class InGame(val name: String)
