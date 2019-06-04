package gun0912.tedimagepicker.sample


import androidx.multidex.MultiDexApplication

import com.squareup.leakcanary.LeakCanary

class MyApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        setLeakCanary()
    }

    private fun setLeakCanary() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        LeakCanary.install(this)
    }
}
