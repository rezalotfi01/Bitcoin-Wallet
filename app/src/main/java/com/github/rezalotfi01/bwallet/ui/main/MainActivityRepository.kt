package com.github.rezalotfi01.bwallet.ui.main

import android.util.Log
import com.github.rezalotfi01.bwallet.data.api.CoinmarketcapApiService
import com.github.rezalotfi01.bwallet.data.db.ConversionPrefs
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class MainActivityRepository @Inject constructor(private val coinmarketcapApi: CoinmarketcapApiService, private val prefsModule: ConversionPrefs) {

    val TAG = MainActivityViewModel::class.java.simpleName
    private var disposable:Disposable? = null

    fun clear() {
        disposable?.dispose()
    }

    fun updateCurrency() {
         disposable = coinmarketcapApi.getBitcoinPrice(prefsModule.getCurrencyCode())
        .subscribeOn(Schedulers.io())
        .subscribe( {
            data -> prefsModule.saveCurrency(data.data)
        }, {
            error -> Log.e(TAG, Log.getStackTraceString(error))
        })
    }

    fun getLastCurrencyUpdate():Long = prefsModule.getLastUpdate()

}