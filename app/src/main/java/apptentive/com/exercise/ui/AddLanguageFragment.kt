package apptentive.com.exercise.ui


import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import apptentive.com.exercise.R
import apptentive.com.exercise.util.DateUtil
import apptentive.com.exercise.util.InjectorUtils
import apptentive.com.exercise.viewmodels.AddLanguageViewModel
import kotlinx.android.synthetic.main.fragment_add_language.*
import java.util.*

class AddLanguageFragment : Fragment() {
    private val viewModel: AddLanguageViewModel by viewModels {
        InjectorUtils.provideAddLanguageViewModelFactory(requireContext())
    }

    //region Lifecycle

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_language, container, false)
        viewModel.releaseDate.observe(viewLifecycleOwner, Observer { date ->
            release_date_edit_text.text = getString(R.string.language_release_date, DateUtil.prettyDate(date))
            release_date_change_button.setOnClickListener {
                showDatePicker(date, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                    val newDate = Calendar.getInstance().apply {
                        set(Calendar.YEAR, year)
                        set(Calendar.MONTH, month)
                        set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    }.time

                    viewModel.setReleaseDate(newDate)
                })
            }
        })

        setHasOptionsMenu(true)
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.language_add_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_save -> {
                if (addLanguage()) {
                    findNavController().popBackStack()
                }
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    //endregion

    //region Helpers

    private fun showDatePicker(date: Date, listener: DatePickerDialog.OnDateSetListener) {
        val calendar = Calendar.getInstance()
        calendar.time = date
        val dialog = DatePickerDialog(
            requireContext(),
            listener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        dialog.show()
    }

    private fun addLanguage(): Boolean {
        val name = name_edit_text.text.toString()
        if (name.isEmpty()) {
            name_edit_text.error = getString(R.string.error_language_name_required)
            return false
        }
        val description: String? = description_edit_text.text.toString()

        viewModel.addLanguage(name, description)

        return true
    }

    //endregion
}
