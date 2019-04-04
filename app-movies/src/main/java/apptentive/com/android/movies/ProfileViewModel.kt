package apptentive.com.android.movies

import androidx.lifecycle.ViewModel
import apptentive.com.android.love.Person

class ProfileViewModel(val person: Person) : ViewModel() {
    val usernames = arrayOf("Melody Jones")
    val emails = arrayOf("melody@apptentive.com")
}