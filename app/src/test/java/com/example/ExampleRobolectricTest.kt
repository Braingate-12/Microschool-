package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.FileUtils
import org.junit.Assert.assertEquals
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
    assertEquals("Microschool", appName)
  }

  @Test
  fun `formatFileSize handles small and large files accurately`() {
    assertEquals("500 B", FileUtils.formatFileSize(500L))
    assertEquals("1.5 KB", FileUtils.formatFileSize(1536L))
    assertEquals("45.0 MB", FileUtils.formatFileSize(45L * 1024 * 1024))
    assertEquals("1.2 GB", FileUtils.formatFileSize((1.2 * 1024 * 1024 * 1024).toLong()))
  }
}
