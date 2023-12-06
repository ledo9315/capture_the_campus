import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import com.hsfl.leo_nelly.capturethecampus.MainViewModel
import com.hsfl.leo_nelly.capturethecampus.R

class DialogManager(private val context: Context) {
    fun showRetryDialog(
        mainViewModel: MainViewModel,
        onPositiveButtonClicked: (String) -> Unit,
        onNegativeButtonClicked: () -> Unit
    ) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.custom_dialog, null)
        val alertDialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        val input = dialogView.findViewById<EditText>(R.id.dialog_input)
        val spinner = dialogView.findViewById<Spinner>(R.id.dialog_spinner)
        val positiveButton = dialogView.findViewById<Button>(R.id.dialog_positive_button)
        val negativeButton = dialogView.findViewById<Button>(R.id.dialog_negative_button)

        val previousNames = mainViewModel.previousNames.value?.toTypedArray() ?: arrayOf()
        val adapter = ArrayAdapter(context, R.layout.custom_spinner_item, previousNames)
        spinner.adapter = adapter

        positiveButton.setOnClickListener {
            val selectedName = if (input.text.isNotEmpty()) {
                input.text.toString()
            } else {
                spinner.selectedItem.toString()
            }
            onPositiveButtonClicked(selectedName)
            alertDialog.dismiss()
        }

        negativeButton.setOnClickListener {
            onNegativeButtonClicked()
            alertDialog.dismiss()
        }

        alertDialog.show()
    }
}
