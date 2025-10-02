package com.example.wellnesstracker.utils

import android.app.Activity
import android.content.Intent
import com.example.wellnesstracker.R

object TransitionUtils {

    fun navigateWithSlideTransition(
        activity: Activity,
        destination: Class<out Activity>,
        forward: Boolean = true
    ) {
        val intent = Intent(activity, destination)
        activity.startActivity(intent)

        if (forward) {
            activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        } else {
            activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        activity.finish()
    }

    fun navigateWithFadeTransition(activity: Activity, destination: Class<out Activity>) {
        val intent = Intent(activity, destination)
        activity.startActivity(intent)
        activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        activity.finish()
    }
}