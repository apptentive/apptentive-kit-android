package apptentive.com.android.feedback.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RandomSamplingTest {

    private lateinit var randomSampling: RandomSampling

    @Before
    fun setup() {
        randomSampling = RandomSampling()
    }

    @Test
    fun getOrPutRandomDebugTest() {
        repeat(10) {
            assertEquals(50.0, randomSampling.getOrPutRandom(it.toString(), true), 0.0)
        }
    }

    @Test
    fun getOrPutRandomGetOrPutTest() {
        val idNumbersList = mutableListOf<String>()
        repeat(100) { idNumber ->
            idNumbersList.add("id#$idNumber")
        }

        val randomNumbersMap = mutableMapOf<String, Double>()
        idNumbersList.forEach { id ->
            val newRandomNumber = randomSampling.getOrPutRandom(id)
            assertTrue(newRandomNumber > 0.0 && newRandomNumber < 100.0)
            randomNumbersMap[id] = newRandomNumber
        }

        randomNumbersMap.forEach { (id, randomNumber) ->
            val retrieveRandomNumber = randomSampling.getOrPutRandom(id)
            assertEquals(randomNumber, retrieveRandomNumber, 0.0)
        }
    }
}
