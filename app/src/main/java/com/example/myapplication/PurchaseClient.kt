package com.example.myapplication

import android.app.Activity
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED
import com.android.billingclient.api.BillingClient.BillingResponseCode.OK

open class PurchaseClient(
    private val activity: Activity,
    private val purchaseClientListener: PurchaseClientListener
) : PurchasesUpdatedListener {
    private val proVersion = "noads_sup"
    private val pro2Version = "noads_sup"

    interface PurchaseClientListener {
        fun loadingPurchase(listSku: List<SkuDetails>)
        fun isNoAdmob(state: Boolean)
        fun loadingFailed()
        fun purchaseSuccess()
    }

    private lateinit var billingClient: BillingClient
    private val skuList = listOf(proVersion,pro2Version)
    private var skuDetails: SkuDetails? = null

    fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(activity)
            .enablePendingPurchases()
            .setListener(this)
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == OK) {
                    // The BillingClient is ready. You can query purchases here.
//                    Timber.d("Setup Billing Done")
                    val purchaseResult = billingClient.queryPurchases(BillingClient.SkuType.SUBS)
                    purchaseResult.let {
                        val listPurChase = purchaseResult.purchasesList
                        if (listPurChase != null) {
                            if (listPurChase.size > 0) {
//                                Timber.d("áđâsd $listPurChase")
                                if (checkPurChase(listPurChase)) {
                                    purchaseClientListener.isNoAdmob(true)
                                } else {
                                    loadAllSKUs()
                                }
                            } else {
                                loadAllSKUs()
                            }
                        } else {
                            loadAllSKUs()
                        }

                    }
                } else {
                    purchaseClientListener.loadingFailed()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
//                Timber.d("disconect")
            }
        })
    }

    private fun checkPurChase(listPurChase: List<Purchase>): Boolean {
//        Timber.d("checkPurChase")
        for (item in listPurChase) {
            if (item.sku == (proVersion)) {
//                Timber.d("checkPurChase pro")
                purchaseClientListener.purchaseSuccess()
                if (item.purchaseState == 1) {
                    return true
                }
            }
        }
        return false
    }

    private fun loadAllSKUs() = if (billingClient.isReady) {
        purchaseClientListener.isNoAdmob(false)
        val params = SkuDetailsParams
            .newBuilder()
            .setSkusList(skuList)
            .setType(BillingClient.SkuType.SUBS)
            .build()
        billingClient.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
            // Process the result.
//            Timber.d("$skuDetailsList")
            if (billingResult.responseCode == OK && skuDetailsList?.isNotEmpty() == true) {
                for (skuDetails in skuDetailsList) {
                    if (skuDetails.sku == proVersion) {
                        this.skuDetails = skuDetails
                        purchaseClientListener.loadingPurchase(skuDetailsList)
                    }
                }
            }
        }

    } else {
        println("Billing Client not ready")
    }

    private fun acknowledgePurchase(purchaseToken: String) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()
        billingClient.acknowledgePurchase(params) { billingResult ->
            val responseCode = billingResult.responseCode
            val debugMessage = billingResult.debugMessage
//            Timber.d(debugMessage)
//            Timber.d(responseCode.toString())
        }
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        if (billingResult.responseCode == OK && purchases != null) {
            for (purchase in purchases) {
                acknowledgePurchase(purchase.purchaseToken)
            }
            purchaseClientListener.isNoAdmob(true)
//            FirebaseAnalytics.getInstance(activity).logEvent(CLICK_PURCHASE_SUCCESS, null)

        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
//            Timber.d("User Cancelled")
//            Timber.d(billingResult.debugMessage.toString())
            purchaseClientListener.isNoAdmob(false)
//            FirebaseAnalytics.getInstance(activity).logEvent(CLICK_PURCHASE_CANCEL, null)

        } else {
//            Timber.d(billingResult.debugMessage.toString())
            purchaseClientListener.isNoAdmob(false)
//            FirebaseAnalytics.getInstance(activity).logEvent(CLICK_PURCHASE_CANCEL, null)
        }
    }

    fun makeProVersionPurchase(skuDetails: SkuDetails) {
        // sale
        skuDetails.let {
            val flowParams =
                BillingFlowParams.newBuilder()
                    .setSkuDetails(skuDetails)
                    .build()
            val billingResult = billingClient.launchBillingFlow(activity, flowParams)
            when (billingResult.responseCode) {
                ITEM_ALREADY_OWNED -> {

                }

                OK -> {
//                    Timber.d("Success purchase")
                    billingClient.endConnection()
                    purchaseClientListener.purchaseSuccess()
                }

                else -> {
//                    Timber.d("Error")
                }
            }
            billingClient.endConnection()
        }
    }

}
