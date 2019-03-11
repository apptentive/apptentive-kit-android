package apptentive.com.android.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import apptentive.com.android.app.dummy.DummyContent

class MainActivity : AppCompatActivity(), ItemFragment.OnListFragmentInteractionListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onListFragmentInteraction(item: DummyContent.Beverage?) {
    }
}
