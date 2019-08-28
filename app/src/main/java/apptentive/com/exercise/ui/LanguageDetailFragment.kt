package apptentive.com.exercise.ui


import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import apptentive.com.exercise.R
import apptentive.com.exercise.util.DateUtil
import apptentive.com.exercise.util.InjectorUtils
import apptentive.com.exercise.viewmodels.LanguageDetailViewModel
import kotlinx.android.synthetic.main.fragment_language_detail.*

/**
 * A simple [Fragment] subclass.
 *
 */
class LanguageDetailFragment : Fragment() {
    private val args: LanguageDetailFragmentArgs by navArgs()
    private val viewModel: LanguageDetailViewModel by viewModels {
        InjectorUtils.provideLanguageDetailViewModelFactory(requireContext(), args.languageName)
    }

    //region Lifecycle

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_language_detail, container, false)

        viewModel.language.observe(viewLifecycleOwner, Observer {
            language_name.text = it.name
            language_description.text = it.description
            language_release_date.text = getString(R.string.language_release_date, DateUtil.prettyDate(it.releaseDate))
        })

        setHasOptionsMenu(true)

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.language_details_menu, menu)
        val favoriteItem = menu.findItem(R.id.menu_toggle_favorite)
        viewModel.language.observe(viewLifecycleOwner, Observer {
            favoriteItem.setIcon(if (it.favorite) R.drawable.ic_favorite_black_24dp else R.drawable.ic_favorite_border_black_24dp)
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_toggle_favorite -> {
                viewModel.toggleFavorite()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    //endregion
}
