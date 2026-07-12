package com.example.ui

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.data.AppContext
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

actual object PlatformAdManager {
    var activeActivity: Activity? = null
    private var mInterstitialAd: InterstitialAd? = null
    private var isAdLoading = false
    private const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    fun loadAd(context: Context) {
        if (mInterstitialAd != null || isAdLoading) return
        isAdLoading = true
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d("PlatformAdManager", "Ad failed to load: ${adError.message}")
                    mInterstitialAd = null
                    isAdLoading = false
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d("PlatformAdManager", "Ad loaded successfully.")
                    mInterstitialAd = interstitialAd
                    isAdLoading = false
                }
            }
        )
    }

    actual fun showAdWithAction(onAdDismissedOrFailed: () -> Unit) {
        val activity = activeActivity
        if (activity != null && mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d("PlatformAdManager", "Ad dismissed.")
                    mInterstitialAd = null
                    loadAd(activity)
                    onAdDismissedOrFailed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                    Log.d("PlatformAdManager", "Ad failed to show.")
                    mInterstitialAd = null
                    loadAd(activity)
                    onAdDismissedOrFailed()
                }
            }
            mInterstitialAd?.show(activity)
        } else {
            Log.d("PlatformAdManager", "Ad not ready or activeActivity is null. Executing action directly.")
            activeActivity?.let { loadAd(it) }
            onAdDismissedOrFailed()
        }
    }
}
