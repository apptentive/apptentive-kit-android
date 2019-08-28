package apptentive.com.exercise.ui


import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import apptentive.com.exercise.R
import apptentive.com.exercise.adapters.LanguageListAdapter
import apptentive.com.exercise.data.Language
import apptentive.com.exercise.data.SortMode
import apptentive.com.exercise.databinding.FragmentLanguageListBinding
import apptentive.com.exercise.util.InjectorUtils
import apptentive.com.exercise.viewmodels.LanguageListViewModel

class LanguageListFragment : Fragment() {
    private val viewModel: LanguageListViewModel by viewModels {
        InjectorUtils.provideLanguageListViewModelFactory(requireContext())
    }

    //region Lifecycle

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentLanguageListBinding.inflate(inflater, container, false)
        context ?: return binding.root

        val adapter = LanguageListAdapter()
        adapter.languageClickListener = object : LanguageListAdapter.LanguageClickListener {
            override fun onClick(language: Language) {
                // TODO: separate business logic from UI component
                openLanguageDetails(language.name)
            }
        }
        binding.itemList.adapter = adapter
        viewModel.itemList.observe(viewLifecycleOwner, Observer { items ->
            adapter.submitList(items)
        })
        binding.fab.setOnClickListener {
            // TODO: separate business logic from UI component
            openAddLanguage()
        }

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.language_list_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_toggle_favorite -> {
                item.isChecked = !item.isChecked
                viewModel.filterByFavorite(item.isChecked)
                return true
            }
            R.id.menu_order_default -> {
                item.isChecked = !item.isChecked
                viewModel.sort(SortMode.DEFAULT)
                return true
            }
            R.id.menu_order_name -> {
                item.isChecked = !item.isChecked
                viewModel.sort(SortMode.NAME)
                return true
            }
            R.id.menu_order_release_date -> {
                item.isChecked = !item.isChecked
                viewModel.sort(SortMode.RELEASE_DATE)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    //endregion

    //region Helpers

    private fun openLanguageDetails(languageName: String) {
        val directions = LanguageListFragmentDirections.actionLanguageDetails(languageName)
        findNavController().navigate(directions)
    }

    private fun openAddLanguage() {
        findNavController().navigate(R.id.action_language_add)
    }

    //endregion
}
