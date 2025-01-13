[![Maven Central Version](https://img.shields.io/maven-central/v/io.github.sublimis.steadyscreen/steadyscreen-lib)](https://central.sonatype.com/artifact/io.github.sublimis.steadyscreen/steadyscreen-lib)

# ⛵ SteadyScreenLib for Android and Wear 🏝️

### Improve screen readability of a handheld device while walking or traveling

- Easily implement
  the [Steady Screen](https://play.google.com/store/apps/details?id=com.sublimis.steadyscreen)
  feature into any application, to counteract small device movements within the user interface.

## How to use

1. Add the library as a dependency, e.g. in your `build.gradle` file:
    ```groovy
    implementation 'io.github.sublimis.steadyscreen:steadyscreen-lib:2.0'
    ```
   Make sure that `mavenCentral()` is included in your repositories list.
2. Create a SteadyScreen instance:
    ```java
    SteadyScreen mSteadyScreen = new SteadyScreen(android.content.Context);
    ```
3. Start/stop receiving events for a View:
    ```java
    // Start receiving SteadyScreen events
    mSteadyScreen.attachView(android.view.View);
    
    // Stop receiving SteadyScreen events
    mSteadyScreen.releaseView(android.view.View);
    ```

   (Optional) Enable/disable the SteadyScreen instance:
    ```java
    // Enable the SteadyScreen feature
    mSteadyScreen.setEnabled(true);
    
    // Disable the SteadyScreen feature
    mSteadyScreen.setEnabled(false);
    
    // Check the current enabled state
    mSteadyScreen.isEnabled();
    ```
   
   (Optional) Call `mSteadyScreen.destroy()` to release all views and unbind the service when you finish.
4. **Install the free
   official [Steady Screen](https://play.google.com/store/apps/details?id=com.sublimis.steadyscreen)
   service app.**
5. Done!

This works with any [View](https://developer.android.com/reference/android/view/View),
including [ViewGroup](https://developer.android.com/reference/android/view/ViewGroup) and various
layouts.

If no `SteadyScreen.attachView(android.view.View)` gets called or all views are released, the library is completely passive and does nothing.
The same is true when the official service app is not installed.

Follow the code in
[SteadyScreen.java](https://github.com/Sublimis/SteadyScreenLib/blob/main/app/src/main/java/io/github/sublimis/steadyscreen/SteadyScreen.java)
and adapt if you need something more exotic.

## Example

Below is an example implementation in
an [Activity](https://developer.android.com/reference/android/app/Activity).

```java
import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import io.github.sublimis.steadyscreen.SteadyScreen;

public class SteadyActivity extends Activity
{
	protected SteadyScreen mSteadyScreen = null;
	protected View mSteadyLayout = null;

	@Override
	protected void onCreate(@Nullable final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		mSteadyScreen = new SteadyScreen(this);
		mSteadyLayout = findViewById(R.id.steadyLayout);
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		mSteadyScreen.attachView(mSteadyLayout);
	}

	@Override
	protected void onStop()
	{
		super.onStop();

		mSteadyScreen.releaseView(mSteadyLayout);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		mSteadyScreen.destroy();
	}
}
```

## Custom views

Custom View can receive raw SteadyScreen service events by implementing
the [ISteadyView](https://github.com/Sublimis/SteadyScreenLib/blob/main/app/src/main/java/io/github/sublimis/steadyscreen/ISteadyView.java)
interface.
