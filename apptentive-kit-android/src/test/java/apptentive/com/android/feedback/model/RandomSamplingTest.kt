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
    fun getOrPutRandomValueDebugTest() {
        repeat(10) {
            assertEquals(50.0, randomSampling.getOrPutRandomValue(it.toString(), true), 0.0)
        }
    }

    @Test
    fun getOrPutRandomValueTest() {
        val idNumbersList = mutableListOf<String>()
        repeat(100) { idNumber ->
            idNumbersList.add("id#$idNumber")
        }

        val randomNumbersMap = mutableMapOf<String, Double>()
        idNumbersList.forEach { id ->
            val newRandomNumber = randomSampling.getOrPutRandomValue(id, false)
            assertTrue(newRandomNumber > 0.0 && newRandomNumber < 100.0)
            randomNumbersMap[id] = newRandomNumber
        }

        randomNumbersMap.forEach { (id, randomNumber) ->
            val retrieveRandomNumber = randomSampling.getOrPutRandomValue(id, false)
            assertEquals(randomNumber, retrieveRandomNumber, 0.0)
        }
    }

    @Test
    fun getRandomValueTest() {
        repeat(100) {
            val randomValue = randomSampling.getRandomValue()
            assertTrue(randomValue < 100.0)
            assertTrue(randomValue >= 0.0)
        }
    }
}
