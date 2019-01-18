package com.github.rezalotfi01.bwallet.di

import com.journeyapps.barcodescanner.BarcodeEncoder
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class BarcodeModule {

    @Provides
    @Singleton
    fun provideBarcodeEncoder(): BarcodeEncoder {
        return BarcodeEncoder()
    }

}