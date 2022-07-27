package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.android.billingclient.api.SkuDetails

class MainActivity : AppCompatActivity(),  PurchaseClient.PurchaseClientListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun loadingPurchase(listSku: List<SkuDetails>) {
        TODO("Not yet implemented")
    }

    override fun isNoAdmob(state: Boolean) {
        TODO("Not yet implemented")
    }

    override fun loadingFailed() {
        TODO("Not yet implemented")
    }

    override fun purchaseSuccess() {
        TODO("Not yet implemented")
    }
}