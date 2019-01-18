package com.github.rezalotfi01.bwallet.data.db

import android.app.Application
import android.arch.lifecycle.LiveData
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.github.rezalotfi01.bwallet.data.model.CryptocurrencyInfo
import com.github.rezalotfi01.bwallet.data.model.ExchangeRate
import javax.inject.Inject

class ConversionPrefs @Inject constructor(context: Application) {

    inner class LiveCurrentExchangeRate:LiveData<ExchangeRate>() {

        private val mListener: SharedPreferences.OnSharedPreferenceChangeListener

        init {
            val value = mSharedPreferences.getFloat(getCurrencyCodeKey(),0f)
            updateValue(value)

            mListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
                val currencyCodeKey = getCurrencyCodeKey()
                if (key == currencyCodeKey  || key == KEY_SELECTED_CURRENCY) {
                    updateValue(sharedPreferences.getFloat(currencyCodeKey,0f))
                }
            }
        }

        private fun updateValue(value:Float) {
            setValue(ExchangeRate(value, getCurrencyCode()))
        }

        override fun onActive() {
            super.onActive()
            mSharedPreferences.registerOnSharedPreferenceChangeListener(mListener)
        }

        override fun onInactive() {
            super.onInactive()
            mSharedPreferences.unregisterOnSharedPreferenceChangeListener(mListener)
        }
    }

    private val TAG = ConversionPrefs::class.java.simpleName

    private val mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val KEY_SELECTED_CURRENCY = "selected_currency"

    val liveExchangeRate: LiveCurrentExchangeRate by lazy { LiveCurrentExchangeRate() }

    companion object {
        val currencyCodes = arrayOf( "AUD","BRL","CAD","CHF","CLP","CNY","CZK","DKK","EUR","GBP","HKD","HUF","IDR","ILS","INR","JPY","KRW","MXN","MYR","NOK","NZD","PHP","PKR","PLN","RUB","SEK","SGD","THB","TRY","TWD","USD","ZAR")
    }

    fun saveCurrency(currency: CryptocurrencyInfo) {
        val editor = mSharedPreferences.edit()

        for ((key,data) in currency.quotes) {
            val keyUppercase = key.toUpperCase()
            if (!currencyCodes.contains(keyUppercase)) continue
            editor.putFloat(getCurrencyCodeFromKey(keyUppercase), data.price)
            editor.putLong(getLastUpdateFromKey(keyUppercase),System.currentTimeMillis())
        }

        editor.apply()
    }

    fun changeCurrency(currencyId:Int) {
        if (currencyId < 0 || currencyId >= currencyCodes.size) return

        val editor = mSharedPreferences.edit()
        editor.putInt(KEY_SELECTED_CURRENCY,currencyId)
        editor.apply()
    }

    fun getLastUpdate(currencyCode:String? = null): Long {

        if (currencyCode == null)
            return  mSharedPreferences.getLong(getLastUpdateKey(), 0L)

        return mSharedPreferences.getLong(getLastUpdateFromKey(currencyCode), 0L)
    }

    fun getCurrencyExchangeRate(currencyCode: String):Float = mSharedPreferences.getFloat(getCurrencyCodeFromKey(currencyCode),0f)

    fun getCurrencyCode():String = currencyCodes[getCurrencyIndex()]

    fun getCurrencyIndex():Int = mSharedPreferences.getInt(KEY_SELECTED_CURRENCY,30) // 8 = EUR, 30 = USD

    private fun getCurrencyCodeKey():String = getCurrencyCodeFromKey(getCurrencyCode())

    private fun getLastUpdateKey():String = getLastUpdateFromKey(getCurrencyCode())

    private fun getLastUpdateFromKey(key:String):String = "UPDATED_$key"

    private fun getCurrencyCodeFromKey(key:String):String = "PRICE_$key"
}