[![Release](https://jitpack.io/v/Sublimis/SteadyScreenLib.svg)](https://jitpack.io/#Sublimis/SteadyScreenLib)

# ⛵ SteadyScreenLib for Android 🏝️

### Improve shaking screen legibility and possibly alleviate motion sickness while on the go.

- This library is only useful in symbiosis with
  the [Steady Screen](https://play.google.com/store/apps/details?id=com.sublimis.steadyscreen)
  service app installed.
- It allows compatible applications to easily counteract small device movements within
  their user interface.
- This can improve screen readability of a handheld device while walking or traveling.

## To use this library in your app

This library enables you to implement the functionality with
any [View](https://developer.android.com/reference/android/view/View).

1. Add the following line to your `build.gradle` file:
    ```groovy
    implementation 'com.github.Sublimis:SteadyScreenLib:2.0'
    ```
2. Call the `SteadyScreen.attachView(android.view.View)` when you want the View to receive the
   SteadyScreen service events.
3. Call the `SteadyScreen.releaseView(android.view.View)` when you want the View to stop receiving
   the SteadyScreen service events,
   or `SteadyScreen.destroy()` to release all View-s and unbind the service.
4. Install
   the [Steady Screen](https://play.google.com/store/apps/details?id=com.sublimis.steadyscreen)
   service app.
5. Enjoy!

If you need something more exotic, just follow the code in
[SteadyScreen.java](https://github.com/Sublimis/SteadyScreenLib/blob/main/app/src/main/java/lib/steadyscreen/SteadyScreen.java)
and adjust to your needs.

## Example

Below is an example implementation of the functionality in an [Activity](https://developer.android.com/reference/android/app/Activity).

```java
import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import lib.steadyscreen.SteadyScreen;

public class SteadyActivity extends Activity
{
	protected SteadyScreen SteadyScreen = null;
	protected View mSteadyLayout = null;

	@Override
	protected void onCreate(@Nullable final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		SteadyScreen = new SteadyScreen(this);
		// mSteadyLayout = ...
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		SteadyScreen.attachView(mSteadyLayout);
	}

	@Override
	protected void onStop()
	{
		super.onStop();

		SteadyScreen.releaseView(mSteadyLayout);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		SteadyScreen.destroy();
	}
}
```

## Custom views

If you want to receive raw SteadyScreen service events in your custom View, you can implement
the [ISteadyView](https://github.com/Sublimis/SteadyScreenLib/blob/main/app/src/main/java/lib/steadyscreen/ISteadyView.java)
interface.
