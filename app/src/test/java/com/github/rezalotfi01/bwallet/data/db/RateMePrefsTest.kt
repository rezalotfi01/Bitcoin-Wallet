package com.github.rezalotfi01.bwallet.data.db

import android.app.Application
import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

class RateMePrefsTest {

    lateinit var sharedPreferences:SharedPreferences
    lateinit var context:Application
    lateinit var editor:SharedPreferences.Editor

    val TWO_DAYS = 1000 * 60 * 60 * 48 //2 days in ms

    @Before
    fun setupTests() {
        sharedPreferences = Mockito.mock(SharedPreferences::class.java)
        context = Mockito.mock(Application::class.java)
        editor = Mockito.mock(SharedPreferences.Editor::class.java)

        Mockito.`when`(sharedPreferences.edit()).thenReturn(editor)
        Mockito.`when`(sharedPreferences.getBoolean(anyString(), anyBoolean())).thenAnswer { it.getArgument(1) }
    }


    inline fun <reified T : Any> argumentCaptor() = ArgumentCaptor.forClass(T::class.java)

    @Test
    fun isTimeToShow_FirstTime_False() {
        Mockito.`when`(sharedPreferences.getBoolean(anyString(), anyBoolean())).thenAnswer { it.getArgument(1) }
        Mockito.`when`(sharedPreferences.getLong(anyString(), anyLong())).thenAnswer { it.getArgument(1) }

        val rateMePreferences = RateMePrefs(sharedPreferences)

        assertEquals(false, rateMePreferences.isTimeToShow())
    }

    @Test
    fun isTimeToShow_AfterNeverShowAgain_False() {
        Mockito.`when`(sharedPreferences.getBoolean(anyString(), anyBoolean())).thenReturn(true)

        val rateMePreferences = RateMePrefs(sharedPreferences)

        assertEquals(false, rateMePreferences.isTimeToShow())
    }

    @Test
    fun isTimeToShow_FirstTime_TimeSetup() {

        Mockito.`when`(sharedPreferences.getBoolean(anyString(), anyBoolean())).thenAnswer { it.getArgument(1) }
        Mockito.`when`(sharedPreferences.getLong(anyString(), anyLong())).thenAnswer { it.getArgument(1) }

        val timeCaptor = argumentCaptor<Long>()
        val prefCaptor = argumentCaptor<String>()

        val rateMePreferences = RateMePrefs(sharedPreferences)

        rateMePreferences.isTimeToShow()

        verify(editor,times(1) ).putLong(prefCaptor.capture(),timeCaptor.capture())

        val currentTime = System.currentTimeMillis()
        val minValue = currentTime - TWO_DAYS + 1000 * 60 * 14
        val maxValue = currentTime - TWO_DAYS + 1000 * 60 * 16

        assertTrue("Initial time is out of range!", timeCaptor.value in (minValue)..(maxValue))
    }

    @Test
    fun isTimeToShow_AfterTwoDays_True() {

        Mockito.`when`(sharedPreferences.getBoolean(anyString(), anyBoolean())).thenAnswer { it.getArgument(1) }
        Mockito.`when`(sharedPreferences.getLong(anyString(), anyLong())).thenReturn( System.currentTimeMillis() - TWO_DAYS )

        val rateMePreferences = RateMePrefs(sharedPreferences)

        assertEquals(true, rateMePreferences.isTimeToShow())
    }

    @Test
    fun setShowed() {

        val timeCaptor = argumentCaptor<Long>()
        val prefCaptor = argumentCaptor<String>()

        val rateMePreferences = RateMePrefs(sharedPreferences)
        rateMePreferences.setShowed()

        verify(editor,times(1) ).putLong(prefCaptor.capture(),timeCaptor.capture())

        val currentTime = System.currentTimeMillis()
        val minValue = currentTime - 1000
        val maxValue = currentTime + 1000

        assertTrue("Time is out of range!", timeCaptor.value in (minValue)..(maxValue))
    }

    @Test
    fun setNeverShowAgain() {

        val rateMePreferences = RateMePrefs(sharedPreferences)
        rateMePreferences.setNeverShowAgain()

        verify(editor,times(1) ).putBoolean(anyString(), eq(true))
    }
}