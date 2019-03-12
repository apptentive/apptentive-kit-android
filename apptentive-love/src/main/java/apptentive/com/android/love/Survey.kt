package apptentive.com.android.love

data class SurveyResponse(val identifier: String)

class Survey(identifier: String, vararg responses: SurveyResponse) : LoveEntity(identifier)