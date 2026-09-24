package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Kühlschrank Rezepte", appName)
  }

  @Test
  fun `check effective gemini key`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val service = com.example.data.ai.AiVisionService(context)
    val key = service.getEffectiveGeminiKey()
    org.junit.Assert.assertNotNull("Key should not be null", key)
    org.junit.Assert.assertTrue("Key should not be empty", key!!.isNotBlank())
  }

  @Test
  fun `check gemini key pool parsing`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val service = com.example.data.ai.AiVisionService(context)
    service.geminiCustomKey = "key_alpha, key_beta; key_gamma"
    val pool = service.getGeminiKeyPool()
    org.junit.Assert.assertTrue(pool.contains("key_alpha"))
    org.junit.Assert.assertTrue(pool.contains("key_beta"))
    org.junit.Assert.assertTrue(pool.contains("key_gamma"))
  }
}
