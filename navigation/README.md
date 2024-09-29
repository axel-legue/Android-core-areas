## NAVIGATION

### 1. [Navigation Host](KNavigation Host)

The NavHost is a container that displays destinations from a navigation graph. It is responsible for
swapping the composable destinations as you navigate through the app. Each NavHost is associated
with a NavController that manages the navigation within the host.

### 2. [Navigation Controller](#Navigation Controller)

It holds the navigation graph and exposes methods that allow your app to move between the
destinations in the graph.
NavController class is the central navigation API. It tracks which destination the user has visited,
and allows the user to move between destinations.

Note: Each NavHost you create has its own corresponding NavController. The NavController provides
access to the NavHost's graph.

### 3. [Navigation Graph](#Navigation graph)

The navigation graph is a data structure that contains each destination within app and the
connections between them.

#### Destination types

There is 3 general types of destination:

1. Hosted : Fills the entire navigation host. That is, the size of a hosted destination is the same
   as the size of the navigation host and previous destinations are not visible. (Use cases : Main
   and details screens)
2. Dialog: Presents overlay UI components. This UI is not tied to the location of the navigation
   host or its size. Previous destinations are visible underneath the destination. (Use cases:
   Alerts, selections , forms)
3. Activity: Represents unique screens or features within the app. (Use cases: Serves as an exit
   point to the navigation graph that starts a new Android activity that is managed separately from
   the Navigation Component. In modern Android development, an app consists of a single activity.
   Activity destinations are therefore best used when interacting with third party activities or as
   part of the migration process)

##### Dialog Destinations

dialog destination refers to destinations within the app's navigation graph which take the form of
dialog windows, overlaying app UI elements and content.

**Note**: Dialog destinations implement the FloatingWindow interface. Your app treats any
destination
that implements this interface as a dialog destination.

Example:

```kotlin
    NavHost(navController = navController, startDestination = FriendsList) {
    composable<FriendsList> {
        FriendsListScreen(
            onNavigateToSettings = {
                navController.navigate(route = Settings)
            }
        )
    }

    // behaves essentially the same as composable, only it creates a dialog destination rather than a hosted destination.
    dialog<Settings> {
        SettingsScreen()
    }
}
```

Note: Because bottom sheets in Compose are not built on Dialog, they need their own destination
type. See the Accompanist Navigation Material documentation for an example implementation.

##### Activity Destinations

While it is best practice to have a single activity in your app, apps often use separate activities
for distinct components or screen within an app. Activity destinations can be useful in such cases.

Compose and Kotlin DSL
Adding an activity destination to your navigation graph is essentially the same in both Compose and
when using the Kotlin DSL with fragments. This is because when passing your NavGraph to your NavHost
composable, you use the same createGraph() lambda.

Example:

```kotlin DSL
fragment<MyFragment, MyRoute> {
    label = getString(R.string.fragment_title)
    // custom argument types, deepLinks
}

activity<MyActivity, MyRoute> {
    label = getString(R.string.activity_title)
    // custom argument types, deepLinks
}
```

By default, the Navigation library attaches the NavController to an Activity layout, and the active
navigation graph is scoped to the active Activity. If a user navigates to a different Activity, the
current navigation graph is no longer in scope. This means that an Activity destination should be
considered an endpoint within a navigation graph.

```XML
<?xml version="1.0" encoding="utf-8"?>
<navigation xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto" android:id="@+id/navigation_graph"
    app:startDestination="@id/simpleFragment">

    <activity android:id="@+id/sampleActivityDestination"
        android:name="com.example.android.navigation.activity.DestinationActivity"
        android:label="@string/sampleActivityTitle" />
</navigation>
```

This XML is equivalent to the following call to startActivity():

```kotlin
startActivity(Intent(context, DestinationActivity::class.java))
```

You might have cases where this approach is not appropriate. For example, you might not have a
compile-time dependency on the activity class, or you might prefer the level of indirection of going
through an implicit intent. The intent-filter in the manifest entry for the destination Activity
dictates how you need to structure the Activity destination.

For example, consider the following manifest file:

```XML
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.android.navigation.activity">
    <application>
        <activity android:name=".DestinationActivity">
            <intent-filter>
                <action android:name="android.intent.action.VIEW" />
                <data android:host="example.com" android:scheme="https" />
                <category android:name="android.intent.category.BROWSABLE" />
                <category android:name="android.intent.category.DEFAULT" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

The corresponding Activity destination needs to be configured with action and data attributes
matching those in the manifest entry:

```XML
<?xml version="1.0" encoding="utf-8"?>
<navigation xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto" android:id="@+id/navigation_graph"
    app:startDestination="@id/simpleFragment">
    <activity android:id="@+id/localDestinationActivity" android:label="@string/localActivityTitle"
        app:action="android.intent.action.VIEW" app:data="https://example.com"
        app:targetPackage="${applicationId}" />
</navigation>
```

Specifying targetPackage to the current applicationId limits the scope to the current application,
which includes the main app.

The same mechanism can be used for cases where you want a specific app to be the destination. The
following example defines a destination to be an app with an applicationId of
com.example.android.another.app.

```XML
<?xml version="1.0" encoding="utf-8"?>
<navigation xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto" android:id="@+id/navigation_graph"
    app:startDestination="@id/simpleFragment">
    <activity android:id="@+id/localDestinationActivity" android:label="@string/localActivityTitle"
        app:action="android.intent.action.VIEW" app:data="https://example.com"
        app:targetPackage="com.example.android.another.app" />
</navigation>
```

##### Nested graphs

To create a nested navigation graph using Compose, use the `NavGraphBuilder.navigation()` function.
`navigation` creates a nested graph rather than a new destination.

```kotlin
    NavHost(navController = navController, startDestination = Profile(name = "John Smith")) {
    composable<Profile> {
        // ...
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
        SettingsScreen()
    }

    // Nested navigation
    navigation<Game>(startDestination = Match) {
        composable<Match> {
            MatchScreen(
                onStartGame = { navController.navigate(route = InGame) }
            )
        }
        composable<InGame> {
            InGameScreen()
        }
    }
}
```

##### Deep links

A deep link is a link that takes you directly to a specific destination within an app.
There is two types of deep links:

1. Explicit deep links
2. Implicit deep links

###### Explicit Deep links

An explicit deep link is a single instance of a deep link that uses a `PendingIntent` to take users
to a specific location within your app.
When a user opens your app via an explicit deep link, the task back stack is cleared and replaced
with the deep link destination. When nesting graphs, the start destination from each level of
nesting—that is, the start destination from each <navigation> element in the hierarchy—is also added
to the stack. This means that when a user presses the Back button from a deep link destination, they
navigate back up the navigation stack just as though they entered your app from its entry point.

```kotlin
val pendingIntent = NavDeepLinkBuilder(context)
    .setGraph(R.navigation.nav_graph)
    .setDestination(R.id.android)
    .setArguments(args)
    .createPendingIntent()
```

By default, `NavDeepLinkBuilder` launches the explicit deep link into the `default launch Activity`
declared in the Manifest
To target a specific Activity, use:

```kotlin
val pendingIntent = NavDeepLinkBuilder(context)
    .setGraph(R.navigation.nav_graph)
    .setDestination(R.id.android)
    .setArguments(args)
    .setComponentName(DestinationActivity::class.java)
    .createPendingIntent()
```

If you have an existing `NavController`, you can also create a deep link by using `NavController.createDeepLink()`.

**_CAUTION :_** These APIs allow deep linking to any screen in your app. If a screen requires that the
user perform some action, such as logging in, before they can access that screen, follow the
guidance on Conditional Navigation to conditionally redirect the user when they reach that screen
using a deeplink

###### Implicit Deep links

It refers to a specific destination in the app. When the deeplink is invoked (ex: when user click on
a link) , android can then open your app to corresponding destination.

Deeps links can be matched by URI, intent action, and MIME types. Multiple match types can be
specified for a single deep link, but URI argument matching is prioritized first, follow by action,
and then MIME type

Deep link that contains a URI, an action, and a MIME type:

```XML

<fragment android:id="@+id/a" android:name="com.example.myapplication.FragmentA"
    tools:layout="@layout/a">
    <deepLink app:uri="www.example.com" app:action="android.intent.action.MY_ACTION"
        app:mimeType="type/subtype" />
</fragment>
```

To enable implicit deep linking, you must also make additions to your app's manifest.xml file. Add a
single <nav-graph> element to an activity that points to an existing navigation graph, as shown in
the following example:

```XML
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.myapplication">

    <application>

        <activity name=".MainActivity">
            ...

            <nav-graph android:value="@navigation/nav_graph" />

            ...

        </activity>
    </application>
</manifest>
```

When building your project, the Navigation component replaces the <nav-graph> element with
generated <intent-filter> elements to match all of the deep links in the navigation graph.

When triggering an implicit deep link, the state of the back stack depends on whether the implicit
Intent was launched with the Intent.FLAG_ACTIVITY_NEW_TASK flag. If the flag is set, the back stack
is cleared and the deep link destination is added to the stack (like explicit deep link).

If the flag is not set, you remain on the task stack of the previous app where the implicit deep
link was triggered. In this case, the Back button takes you back to the previous app, while the Up
button starts your app's task on the hierarchical parent destination within your navigation
graph.

###### Handling deep links

It is strongly recommended to always use the default `launchMode` of `standard` when using Navigation.
When using standard launch mode, Navigation automatically handles deep links by calling
`handleDeepLink()` to process any explicit or implicit deep links within the Intent. However, this
does not happen automatically if the `Activity` is re-used when using an alternate `launchMode` such as
`singleTop`. In this case, it is necessary to manually call `handleDeepLink()` in `onNewIntent()`, as
shown in the following example:

```kotlin
override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    navController.handleDeepLink(intent)
}
```

##### Encapsulated your code
WIP