# Services overview

### Definition

**Application component that can perform long-running operations in the background.**
No user interfaces.
Once started, light continue running for some time, even after the user switches to another
application.
A component can bind to a service and interact with it and even perform interprocess communication (
IPC).

Example : handle network transactions, play music, perform file I/O, interact with a content
provider.

#### Foreground services

It perform some operation that is noticeable to the user.
Must display a notification that cannot be dismissed unless the service is stopped or removed from
the foreground.
Continue running even when the user isn't interacting with the app.

#### Background services

Perform operation that isn't directly noticed by the user.
Example: Downloading large amounts of data, Updating app data periodically

#### Bound services

A service is **bound** when an application component binds to it by calling `bindService()`
Offers a client-server interface that allow components to interact with the service, send requests,
receive results etc...
It runs only as long as another application component is bound to it.

Started and bound services can work both ways. It can be started (run indefinitely) and also allow
binding.
regardless of the type of services, any application can use the service in the same way that any
component can use an activity - by starting it with an intent.
Service can be declared as **private** in the manifest and block access from other applications.

##### Choosing between a service and a thread

* Services run in the background, even when the user isn’t interacting with the app.
* Only create a service if it’s needed for background tasks.
* For tasks only required during user interaction, use a new thread in an activity or other
  component.
* Example: For playing music only during an activity, start and stop a thread in the activity’s
  lifecycle methods.
* Use thread pools, executors, or Kotlin coroutines for managing background tasks instead of the
  Thread class.
* Services run on the main thread by default; create a new thread in the service for intensive
  tasks.

#### The basics

* Create a service by subclassing Service and overriding key lifecycle methods.
* Important callback methods to override:
    * `onStartCommand()`: Runs when `startService()` is called. Must stop itself with `stopSelf()`
      or `stopService()`.
    * `onBind()`: Runs when `bindService()` is called. Returns an `IBinder` for client
      communication; return null if binding isn’t needed.
    * `onCreate()`: Runs for one-time setup when the service is created.
    * `onDestroy()`: Runs during service destruction to clean up resources.
* If started with `startService()`, the service runs until explicitly stopped.
* If started with `bindService()`, the service stops when unbound from all clients.
* Services are more likely to be killed when memory is low but can be made more persistent if bound
  to an activity in the foreground.
* Long-running services should be designed to handle potential restarts gracefully.

##### Declaring a service in the manifest

services must be declared in manifest file
`<manifest ... >
...
<application ... >
<service android:name=".ExampleService" />
...
</application>
</manifest>`

##### Key Attributes:

* android:description: Describes the service for users; reference a localizable string resource.
* android:directBootAware: Specifies if the service runs before device unlock; default is "false."
* android:enabled: Defines if the service is system-instantiable; default is "true."
* android:exported: Controls external app access; default is "false" unless intent filters are
  present.
* android:foregroundServiceType: Defines service purpose (e.g., "location" for GPS-based services).
* android:icon: Specifies an icon for the service; falls back to the app icon if not set.
* android:isolatedProcess: Runs the service in an isolated, permission-restricted process if set
  to "true."
* android:label: User-friendly name; falls back to the app’s label if not set.
* android:name: Fully qualified class name of the Service subclass.
* android:permission: Restricts access to the service to entities with a specific permission.
* android:process: Defines the process where the service runs; can be private (colon-prefixed) or
  global.

Caution:
A service runs in the same process as the application in which it is declared and in the main thread
of that application by default. If your service performs intensive or blocking operations while the
user interacts with an activity from the same application, the service slows down activity
performance. To avoid impacting application performance, start a new thread inside the service.