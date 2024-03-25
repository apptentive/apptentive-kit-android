package apptentive.com.android.feedback

import apptentive.com.android.TestCase
import apptentive.com.android.feedback.utils.FileUtil
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import org.junit.Test
import java.net.URL

class PrefetchManagerTest : TestCase() {

    private val prefetchFromManifest = listOf(
        URL("https://variety.com/wp-content/uploads/2022/12/Disney-Plus.png"),
        URL("https://app-icons.apptentive.com/521f9bba68e2758b6d00098d_1664232881.png")
    )
    @Test
    fun testDownloadPrefetchResources() {
        val prefetchManager = spyk(PrefetchManager)

        // Mock the prefetched files list
        every { prefetchManager.prefetchedFileURIFromDisk } returns mutableListOf()

        // Mock the downloadFile method
        every { prefetchManager.downloadFile(any(), any()) } answers { /* do nothing */ }

        // Call the method under test
        prefetchManager.downloadPrefetchableResources(prefetchFromManifest)

        // Verify that downloadFile was called for each file in the manifest
        verify(exactly = prefetchFromManifest.size) { prefetchManager.downloadFile(any(), any()) }
    }

    @Test
    fun testDeleteOutdatedResourcesFromLocal() {
        // Mock the behavior of prefetchedFilesFromLocal on the spy
        val mockLocalFiles = mutableListOf("file1.jpg", "file2.jpg", "file4.jpg")

        for (file in mockLocalFiles) {
            PrefetchManager.prefetchedFileURIFromDisk.add(file)
        }

        // Mock the FileUtil.deleteFile method
        mockkObject(FileUtil)
        every { FileUtil.deleteFile(any() as String?) } just Runs

        val prefetchFromManifest = listOf("file1.jpg", "file2.jpg", "file3.jpg")

        // Call the method under test on the spy
        PrefetchManager.deleteOutdatedResourcesFromLocal(prefetchFromManifest)

        // Verify that FileUtil.deleteFile was called for outdated files
        verify(exactly = 0) { FileUtil.deleteFile("file1.jpg") }
        verify(exactly = 0) { FileUtil.deleteFile("file2.jpg") }
        verify(exactly = 1) { FileUtil.deleteFile("file4.jpg") }
    }

    @Test
    fun testGetFileNameFromFilePath() {
        val result = PrefetchManager.getFileNameFromFilePath("/path/to/your/file.txt")
        assertEquals("file.txt", result)
    }

    @Test
    fun testGetAsHashCodeNames() {
        val sampleUrls = listOf(
            URL("https://example.com/file1.txt"),
            URL("https://example.com/file2.jpg"),
            URL("https://example.com/file3.png")
        )

        // Act
        val result = PrefetchManager.getAsHashCodeNames(sampleUrls)

        // Assert
        assertEquals(
            listOf(
                "${sampleUrls[0].toString().hashCode()}",
                "${sampleUrls[1].toString().hashCode()}",
                "${sampleUrls[2].toString().hashCode()}"
            ),
            result
        )
    }

    @Test
    fun testGetHashCodedFileNameFromUrlWithMalformedUrl() {
        val hashCodedFileName = PrefetchManager.getHashCodedFileNameFromUrl("malformed-url")
        assertEquals("malformed-url".hashCode().toString(), hashCodedFileName)
    }
}
