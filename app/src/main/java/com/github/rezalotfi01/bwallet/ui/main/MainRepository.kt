package com.github.rezalotfi01.bwallet.ui.main

import android.util.Log
import com.github.rezalotfi01.bwallet.data.db.AppDatabase
import com.github.rezalotfi01.bwallet.data.db.WalletRecord
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class MainRepository @Inject constructor(private val appDatabase: AppDatabase) {

    val TAG = MainRepository::class.java.simpleName

    fun getHistory() : Flowable<List<WalletRecord>> {
        return appDatabase.walletRecordDao().getHistory()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }


    fun getFavourite() :Flowable<List<WalletRecord>> {
        return appDatabase.walletRecordDao().getFavorites()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun deleteRecord(record: WalletRecord) {
        Observable.fromCallable {
            appDatabase.walletRecordDao().removeRecordFromHistory(record.address)
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { Log.e(TAG, Log.getStackTraceString(it)) }
                .subscribe ()
    }

    //TODO: refactor Repositories, Duplicated in DetailViewModel
    fun saveRecordToHistory(record: WalletRecord) {

        Observable.fromCallable {
            appDatabase.walletRecordDao().addWalletRecord(record)
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { Log.e(TAG,Log.getStackTraceString(it)) }
                .subscribe ()
    }

    fun favouriteRecord(walletID: String) {
        Observable.fromCallable {
            appDatabase.walletRecordDao().favouriteRecord(walletID)
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { Log.e(TAG,Log.getStackTraceString(it)) }
                .subscribe ()
    }

    fun unfavouriteRecord(walletID: String) {
        Observable.fromCallable {
            appDatabase.walletRecordDao().unfavouriteRecord(walletID)
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { Log.e(TAG,Log.getStackTraceString(it)) }
                .subscribe ()
    }
}