package press

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Looper
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.android.schedulers.AndroidSchedulers
import me.saket.press.shared.di.SharedComponent
import me.saket.press.shared.sync.SyncCoordinator
import me.saket.press.shared.sync.Syncer
import press.di.AppComponent
import press.home.HomeActivity
import javax.inject.Inject

abstract class PressApp : Application() {
  companion object {
    lateinit var component: AppComponent
  }

  @Inject lateinit var syncer: Syncer
  @Inject lateinit var syncCoordinator: SyncCoordinator

  abstract fun buildDependencyGraph(): AppComponent

  override fun onCreate() {
    super.onCreate()
    component = buildDependencyGraph()
    SharedComponent.initialize(this)
    component.inject(this)

    RxAndroidPlugins.setInitMainThreadSchedulerHandler {
      AndroidSchedulers.from(Looper.getMainLooper(), true)
    }

    doOnActivityResume { activity ->
      if (activity is HomeActivity) {
        syncCoordinator.sync()
      }
    }
  }

  private fun doOnActivityResume(action: (Activity) -> Unit) {
    registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
      override fun onActivityResumed(activity: Activity) {
        action(activity)
      }

      override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
      override fun onActivityStarted(activity: Activity) = Unit
      override fun onActivityPaused(activity: Activity) = Unit
      override fun onActivityStopped(activity: Activity) = Unit
      override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
      override fun onActivityDestroyed(activity: Activity) = Unit
    })
  }
}
