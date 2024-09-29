package com.skitt31.navigation

import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.navigation
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute


@Composable
fun NavigationNavHost(
    navController: NavHostController
) {
    NavHost(navController = navController, startDestination = Profile(name = "John Smith")) {
        composable<Profile> { backStackEntry ->
            // We can obtain route instance with NavBackStackEntry.toRoute() or SavedStateHandle.toRoute().
            // Note: By using the parameters of the route class you can pass data to the given destination with full type safety.
            // For example, Profile.name ensures that name is always a String.
            val profile: Profile = backStackEntry.toRoute()
            ProfileScreen(
                profile = profile,
                onNavigateToFriendsList = {
                    // We can navigate to a route by passing an instance of the route class. always pass lamb
                    navController.navigate(route = FriendsList)
                },
                onExplicitDeepLinkSelected = {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.android-core.com/aisha")
                    )
                    val pendingIntent = TaskStackBuilder.create(navController.context).run {
                        addNextIntentWithParentStack(intent)
                        getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                    }
                    pendingIntent.send()
                }
            )
        }

        composable<FriendsList> {
            FriendsListScreen(
                onNavigateToProfile = {
                    navController.navigate(
                        route = Profile(name = "Aisha Devi")
                    )
                },
                onNavigateToSettings = {
                    navController.navigate(route = Settings)
                }
            )
        }

        // behaves essentially the same as composable, only it creates a dialog destination rather than a hosted destination.
        dialog<Settings> {
            SettingsScreen(
                onNavigateToGame = {
                    navController.navigate(route = Game)
                }
            )
        }

        // Nested navigation
        navigation<Game>(startDestination = Match) {
            composable<Match> {
                MatchScreen(
                    onStartGame = { navController.navigate(route = InGame) }
                )
            }
            composable<InGame>(
                deepLinks = listOf(
                    navDeepLink {
                        uriPattern = "https://www.android-core.com/{name}"
                        action = Intent.ACTION_VIEW
                    }
                ),
            ) { backStackEntry ->
                val inGame: InGame = backStackEntry.toRoute()
                InGameScreen(inGame)
            }
        }
    }
}


// Define the ProfileScreen composable.
@Composable
fun ProfileScreen(
    profile: Profile,
    onNavigateToFriendsList: () -> Unit,
    onExplicitDeepLinkSelected: () -> Unit,
) {
    Column {
        Text("Profile for ${profile.name}")
        Button(onClick = { onNavigateToFriendsList() }) {
            Text("Go to Friends List")
        }

        Button(onClick = { onExplicitDeepLinkSelected() }) {
            Text("Go to In Game Screen")
        }
    }
}

// Define the FriendsListScreen composable.
@Composable
fun FriendsListScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Column {
        Text("FriendsListScreen")
        Button(onClick = { onNavigateToProfile() }) {
            Text("Go to Profile")
        }
        Button(onClick = { onNavigateToSettings() }) {
            Text("Go to Settings")
        }
    }
}

@Composable
fun SettingsScreen(onNavigateToGame: () -> Unit) {
    Column {
        Text("SettingsScreen")
        Button(onClick = { onNavigateToGame() }) {
            Text("Start Game")
        }
    }
}

@Composable
fun MatchScreen(onStartGame: () -> Unit) {
    Text("MatchScreen")
    Button(onClick = { onStartGame() }) {
        Text("Start Game")
    }
}

@Composable
fun InGameScreen(inGame: InGame) {
    Text("InGameScreen for ${inGame.name}")
}