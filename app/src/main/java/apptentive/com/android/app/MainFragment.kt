package apptentive.com.android.app


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels


/**
 * A simple [Fragment] subclass.
 *
 */
class MainFragment : Fragment() {

    private val viewModel: MainFragmentViewModel by viewModels {
        InjectorUtils.provideMainFragmentViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false)
    }


}
