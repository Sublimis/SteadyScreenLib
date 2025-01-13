/*
 * Copyright 2025 Sublimis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.sublimis.steadyscreen;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.IBinder;
import android.os.SystemClock;
import android.view.View;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;

/**
 * Interface to the <a href="https://play.google.com/store/apps/details?id=com.sublimis.steadyscreen">SteadyScreen</a> service app
 * which allows compatible applications to easily counteract small screen movements within their GUI.
 * <p>
 * Find out more at <a href="https://github.com/Sublimis/SteadyScreenLib/">https://github.com/Sublimis/SteadyScreenLib/</a>.
 *
 * @author Sublimis
 * @version 2.0 (2025-01)
 */
public class SteadyScreen
{
	public static final String ServicePackage = "com.sublimis.steadyscreen";
	public static final String ServiceComponent = ServicePackage + ".SteadyService";
	public static final String ActionMove = ServicePackage + ".ACTION.MOVE";
	public static final String ParamTimestamp = ServicePackage + ".TIMESTAMP";
	public static final String ParamMoveCoordX = ActionMove + ".X";
	public static final String ParamMoveCoordY = ActionMove + ".Y";
	public static final String ParamServiceAppName = ServicePackage + ".APP.NAME";
	public static final String ParamServiceVersionCode = ServicePackage + ".APP.VERSION.CODE";
	public static final String ParamServiceVersionName = ServicePackage + ".APP.VERSION.NAME";
	public static final String ParamServiceVersionDate = ServicePackage + ".APP.VERSION.DATE";
	public static final String ParamSensorRate = ServicePackage + ".PARAM.SENSOR_RATE";
	public static final String ServicePermission = "com.sublimis.steadyscreen.permission.SERVICE";

	public static final int InvalidCoord = Integer.MIN_VALUE;

	protected final AtomicBoolean IsEnabled = new AtomicBoolean(true);

	protected final long UndoTimeout = TimeUnit.SECONDS.toNanos(2);
	protected final long UndoCheckTimeout = TimeUnit.SECONDS.toNanos(1);
	protected final long MetaInfoTimeout = TimeUnit.SECONDS.toNanos(5);

	protected volatile long mLastActionTime = 0, mLastUndoCheckTime = 0, mLastMetaInfoTime = 0;

	protected final Context MyContext;
	protected final Set<View> Views = new LinkedHashSet<>(), ViewsTmp = new LinkedHashSet<>();

	protected final AtomicBoolean IsServiceBound = new AtomicBoolean(false);
	protected final ServiceConnection ServiceConnection = new ServiceConnection()
	{
		@Override
		public void onServiceConnected(final ComponentName name, final IBinder service)
		{
		}

		@Override
		public void onServiceDisconnected(final ComponentName name)
		{
		}
	};

	protected final AtomicBoolean IsReceiving = new AtomicBoolean(false);
	protected final BroadcastReceiver Receiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			if (intent != null && ActionMove.equals(intent.getAction()))
			{
				final long timestamp = SystemClock.elapsedRealtimeNanos();

				final double xRaw = intent.getDoubleExtra(ParamMoveCoordX, InvalidCoord);
				final double yRaw = intent.getDoubleExtra(ParamMoveCoordY, InvalidCoord);

				final int xScr = (int) xRaw;
				final int yScr = (int) yRaw;

				ViewsTmp.clear();

				synchronized (Views)
				{
					ViewsTmp.addAll(Views);
				}

				final boolean postUndoCheck = isUndoCheckNeeded(timestamp);

				for (final View view : ViewsTmp)
				{
					performSteadyViewAction(view, xScr, yScr, postUndoCheck);

					if (view instanceof ISteadyView)
					{
						final ISteadyView steadyView = (ISteadyView) view;

						steadyView.steadyViewAction(xRaw, yRaw);

						if (isMetaInfoNeeded(timestamp))
						{
							steadyView.steadyScreenMetaInfo(intent);
							mLastMetaInfoTime = timestamp;
						}
					}
				}

				ViewsTmp.clear();

				if (postUndoCheck)
				{
					mLastUndoCheckTime = timestamp;
				}

				mLastActionTime = timestamp;
			}
		}
	};

	public SteadyScreen(@NonNull final Context context)
	{
		MyContext = context;
	}

	/**
	 * Get the SteadyScreen feature enabled state.
	 *
	 * @return {@code true} if this SteadyScreen instance is enabled.
	 */
	public boolean isEnabled()
	{
		return IsEnabled.get();
	}

	/**
	 * Set the SteadyScreen feature enabled state.
	 *
	 * @param enabled {@code true} to enable the SteadyScreen feature instance, {@code false} to disable.
	 * @return {@code true} if SteadyScreen feature enabled state was changed as a result of this call.
	 */
	public boolean setEnabled(final boolean enabled)
	{
		final boolean retVal = IsEnabled.compareAndSet(!enabled, enabled);

		if (retVal)
		{
			if (enabled)
			{
				assertActive();
			}
			else
			{
				assertInactive();
			}
		}

		return retVal;
	}

	/**
	 * Attach the View that will receive SteadyService events.
	 * If the feature is enabled and the SteadyService is not running, it will be started.
	 *
	 * @param view The View to attach.
	 * @return {@code true} if the feature is enabled and SteadyService was successfully bound.
	 */
	public boolean attachView(final View view)
	{
		return addView(view);
	}

	/**
	 * Release the view from the SteadyScreen.
	 * If no more views are attached, the SteadyService will be unbound.
	 *
	 * @param view The View to release.
	 */
	public void releaseView(final View view)
	{
		removeView(view);
	}

	/**
	 * Clear all views, but do not unbind the SteadyService.
	 */
	public void clear()
	{
		synchronized (Views)
		{
			Views.clear();
		}
	}

	/**
	 * Release all views, and unbind the SteadyService.
	 */
	public void destroy()
	{
		synchronized (Views)
		{
			Views.clear();

			assertInactive();
		}
	}

	protected boolean addView(final View view)
	{
		boolean retVal = false;

		synchronized (Views)
		{
			if (view != null)
			{
				Views.add(view);
			}

			if (false == Views.isEmpty())
			{
				if (isEnabled())
				{
					retVal = assertActive();
				}
			}
		}

		return retVal;
	}

	protected void removeView(final View view)
	{
		synchronized (Views)
		{
			if (view != null)
			{
				Views.remove(view);
			}

			if (Views.isEmpty())
			{
				assertInactive();
			}
		}
	}

	@MainThread
	protected void performSteadyViewAction(@NonNull final View view, final int x, final int y, final boolean postUndoCheck)
	{
		if (x != InvalidCoord)
		{
			view.setTranslationX(x);
		}

		if (y != InvalidCoord)
		{
			view.setTranslationY(y);
		}

		if (postUndoCheck)
		{
			postUndoCheck(view);
		}
	}

	@MainThread
	protected void undoSteadyViewAction(@NonNull final View view)
	{
		view.setTranslationX(0);
		view.setTranslationY(0);
	}

	protected void postUndoCheck(@NonNull final View view)
	{
		view.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				if (isUndoNeeded())
				{
					undoSteadyViewAction(view);
				}
			}
		}, TimeUnit.NANOSECONDS.toMillis(UndoTimeout + UndoCheckTimeout) + 500);
	}

	protected boolean isUndoNeeded()
	{
		final long timestamp = SystemClock.elapsedRealtimeNanos();

		return timestamp - mLastActionTime > UndoTimeout;
	}

	protected boolean isUndoCheckNeeded(final long timestamp)
	{
		return timestamp - mLastUndoCheckTime > UndoCheckTimeout;
	}

	protected boolean isMetaInfoNeeded(final long timestamp)
	{
		return timestamp - mLastMetaInfoTime > MetaInfoTimeout;
	}

	protected boolean assertActive()
	{
		boolean retVal = false;

		synchronized (Views)
		{
			retVal = serviceBind();
			registerReceiver();
		}

		return retVal;
	}

	protected void assertInactive()
	{
		synchronized (Views)
		{
			unregisterReceiver();
			serviceUnbind();
		}
	}

	protected boolean serviceBind()
	{
		boolean retVal = false;

		synchronized (IsServiceBound)
		{
			if (IsServiceBound.compareAndSet(false, true))
			{
				final Intent intent = new Intent()
						.setComponent(new ComponentName(ServicePackage, ServiceComponent))
						.setAction(MyContext.getPackageName());

				if (MyContext.bindService(intent, ServiceConnection, Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT))
				{
					retVal = true;
				}
				else
				{
					IsServiceBound.set(false);
				}
			}
			else
			{
				retVal = true;
			}
		}

		return retVal;
	}

	protected void serviceUnbind()
	{
		synchronized (IsServiceBound)
		{
			if (IsServiceBound.compareAndSet(true, false))
			{
				MyContext.unbindService(ServiceConnection);
			}
		}
	}

	@SuppressLint("UnspecifiedRegisterReceiverFlag")
	protected void registerReceiver()
	{
		if (IsReceiving.compareAndSet(false, true))
		{
			final IntentFilter filter = new IntentFilter(ActionMove);

			if (Build.VERSION.SDK_INT >= VERSION_CODES.TIRAMISU)
			{
				MyContext.registerReceiver(Receiver, filter, ServicePermission, null, Context.RECEIVER_EXPORTED);
			}
			else
			{
				MyContext.registerReceiver(Receiver, filter, ServicePermission, null);
			}
		}
	}

	protected void unregisterReceiver()
	{
		if (IsReceiving.compareAndSet(true, false))
		{
			MyContext.unregisterReceiver(Receiver);
		}
	}

	public static class MetaInfo
	{
		/**
		 * SteadyService app name.
		 */
		public final String paramServiceAppName;

		/**
		 * SteadyService app version code (version as an integer).
		 */
		public final int paramServiceVersionCode;

		/**
		 * SteadyService app version name (version as a string).
		 */
		public final String paramServiceVersionName;

		/**
		 * SteadyService app date. [YYYY-MM-DD]
		 */
		public final String paramServiceVersionDate;

		/**
		 * Sensor rate as requested by the SteadyService app. [Hz]
		 */
		public final double paramSensorRate;

		protected MetaInfo(@NonNull final Intent intent)
		{
			this.paramServiceAppName = intent.getStringExtra(ParamServiceAppName);
			this.paramServiceVersionCode = intent.getIntExtra(ParamServiceVersionCode, -1);
			this.paramServiceVersionName = intent.getStringExtra(ParamServiceVersionName);
			this.paramServiceVersionDate = intent.getStringExtra(ParamServiceVersionDate);
			this.paramSensorRate = intent.getDoubleExtra(ParamSensorRate, -1);
		}

		@NonNull
		public static MetaInfo getInstance(@NonNull final Intent intent)
		{
			return new MetaInfo(intent);
		}
	}
}
