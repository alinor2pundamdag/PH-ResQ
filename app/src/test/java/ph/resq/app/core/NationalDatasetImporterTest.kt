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

 @Test fun publicContactAndEvacuationImportsRollbackAllRowsWhenAnyRecordIsInvalid(){
  val validContact=PublicEmergencyContact("valid-contact",null,"NATIONAL","OTHER","Test hotline","911","https://example.gov.ph/hotline","Test official source",ContactVerification.OFFICIAL_GOVERNMENT_SOURCE,null,0L)
  val invalidContact=validContact.copy(id="invalid-contact",phone="not-a-phone")
  assertTrue(runCatching{repository.addPublicContacts(listOf(validContact,invalidContact))}.isFailure)
  assertTrue(repository.searchContacts("",10).isEmpty())

  val validCenter=NationalEvacuationCenter("valid-center",null,"Test Center","Test address",null,null,null,null,"UNKNOWN","UNKNOWN","https://example.gov.ph/center","Test official source",ContactVerification.OFFICIAL_GOVERNMENT_SOURCE)
  val invalidCenter=validCenter.copy(id="invalid-center",psgcCode="not-a-psgc")
  assertTrue(runCatching{repository.addEvacuationCenters(listOf(validCenter,invalidCenter))}.isFailure)
  assertTrue(repository.searchEvacuationCenters("",10).isEmpty())
 }

 @Test fun bundledNationalReferenceAndEmergencySeedsInstallWithHonestUnknownStates(){
  assertEquals(42_010,NationalDatasetImporter.importBundledBarangays(context,repository))
  val coverageAfterPsgc=repository.coverage()
  assertEquals(42_010,coverageAfterPsgc.expectedBarangays)
  assertEquals(42_010,coverageAfterPsgc.loadedBarangays)
  val corrected=repository.searchBarangays("Lambagan",10).single()
  assertEquals("1001322005",corrected.psgcCode)
  assertEquals("Lambagan",corrected.barangay)

  assertEquals(192,NationalDatasetImporter.importBundledPublicContacts(context,repository))
  assertTrue(repository.searchContacts("911",10).any{it.phone=="911"})
  assertTrue(repository.searchContacts("122",10).any{it.phone=="122"})
  assertTrue(repository.searchContacts("117",10).any{it.phone=="117"})
  assertTrue(repository.searchContacts("143",10).any{it.phone=="143"})
  assertTrue(repository.searchContacts("136",10).any{it.phone=="136"})
  assertTrue(repository.searchContacts("Quezon",100).any{it.officeName=="Quezon Provincial DRRMO"})
  val contactRows=repository.searchContacts("",250)
  assertEquals(192,contactRows.size)
  assertTrue(contactRows.all{it.expiresAt==0L})
  assertTrue(contactRows.any{it.sourceUrl=="https://ehotlines.e.gov.ph/" && it.verification==ContactVerification.OFFICIAL_GOVERNMENT_SOURCE})
  assertTrue(contactRows.any{it.sourceUrl=="https://quezon.gov.ph/quezon-province-emergency-hotlines/" && it.scope=="CITY_MUNICIPALITY"})
  assertEquals(0,repository.coverage().verifiedContacts)
  assertTrue(repository.coverage().contactBarangays>0)

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
  assertTrue(centers.all{it.verifiedAt==null})
  assertTrue(centers.any{it.psgcCode!=null})
  assertTrue(centers.all{it.psgcCode==null || it.psgcCode.matches(Regex("\\d{10}"))})
 }
}
