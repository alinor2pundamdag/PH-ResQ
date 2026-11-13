package ph.resq.app.core

import android.content.Context
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import javax.crypto.spec.SecretKeySpec

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34])
class NationalDatasetImporterTest{
 private lateinit var context:Context
 private lateinit var database:ResQDatabase
 private lateinit var repository:NationalReferenceRepository

 @Before fun setUp(){
  context=RuntimeEnvironment.getApplication()
  context.deleteDatabase("ph_resq_local.db")
  val cipher=EncryptedFieldCipher(testKeyProvider={SecretKeySpec(ByteArray(32){0x5a},"AES")})
  database=ResQDatabase(context,cipher)
  repository=NationalReferenceRepository(database)
 }

 @After fun tearDown(){
  database.close()
  context.deleteDatabase("ph_resq_local.db")
 }

 @Test fun bundledNationalReferenceAndEmergencySeedsInstallWithHonestUnknownStates(){
  assertEquals(42_010,NationalDatasetImporter.importBundledBarangays(context,repository))
  val coverageAfterPsgc=repository.coverage()
  assertEquals(42_010,coverageAfterPsgc.expectedBarangays)
  assertEquals(42_010,coverageAfterPsgc.loadedBarangays)
  val corrected=repository.searchBarangays("Lambagan",10).single()
  assertEquals("1001322005",corrected.psgcCode)
  assertEquals("Lambagan",corrected.barangay)

  assertEquals(102,NationalDatasetImporter.importBundledPublicContacts(context,repository))
  assertTrue(repository.searchContacts("911",10).any{it.phone=="911"})
  assertTrue(repository.searchContacts("122",10).any{it.phone=="122"})

  assertEquals(429,NationalDatasetImporter.importBundledEvacuationCenters(context,repository))
  val finalCoverage=repository.coverage()
  assertEquals(429,finalCoverage.evacuationCenters)
  assertEquals(0,finalCoverage.geocodedCenters)
  val centers=repository.searchEvacuationCenters("",500)
  assertEquals(429,centers.size)
  assertTrue(centers.all{it.operationalStatus=="UNKNOWN"})
  assertTrue(centers.all{it.latitude==null&&it.longitude==null})
  assertTrue(centers.any{it.address.contains("Quezon City")})
  assertTrue(centers.any{it.address.contains("Bay")})
 }
}
