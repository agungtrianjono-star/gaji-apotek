package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
    assertEquals("Gaji Nirmala Farma", appName)
  }

  @Test
  fun `test database initialization and seeding`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = AppDatabase.getDatabase(context)
    assertNotNull(db)
    
    val dao = db.payrollDao()
    // Flow requires a coroutine to read, first() will resume when a list is emitted
    val pegawais = dao.getAllPegawai().first()
    println("Seeded pegawais size: ${pegawais.size}")
    for (p in pegawais) {
      println("Pegawai: ${p.nama}, ${p.jabatan}")
    }
    assertEquals(5, pegawais.size)
  }

  @Test
  fun `test viewModel initialization`() = runBlocking {
    val application = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = com.example.ui.PayrollViewModel(application)
    assertNotNull(vm)
    
    // Check if we can collect allPegawai
    val employees = vm.allPegawai.first()
    println("VM employees size: ${employees.size}")
    
    // Check filtered Slips
    val slips = vm.filteredSlips.first()
    println("VM slips size: ${slips.size}")
  }
}
