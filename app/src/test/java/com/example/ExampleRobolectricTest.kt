package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.crypto.E2EEManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
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
    assertEquals("WhatsApp", appName)
  }

  @Test
  fun `verify E2EE AES-256-GCM encryption and decryption`() {
    val plaintext = "Mensaje secreto cifrado de extremo a extremo"
    val contactId = "contact_test_123"

    val encrypted = E2EEManager.encrypt(plaintext, contactId)
    assertNotEquals(plaintext, encrypted.ciphertextBase64)
    assertEquals("AES-256-GCM", encrypted.algorithm)

    val decrypted = E2EEManager.decrypt(encrypted.ciphertextBase64, encrypted.ivBase64, contactId)
    assertEquals(plaintext, decrypted)
  }

  @Test
  fun `verify 60 digit safety code generation`() {
    val code = E2EEManager.generate60DigitSecurityCode("userA", "userB")
    val blocks = code.split(" ")
    assertEquals(12, blocks.size)
    blocks.forEach { block ->
      assertEquals(5, block.length)
      assertTrue(block.all { it.isDigit() })
    }
  }
}
