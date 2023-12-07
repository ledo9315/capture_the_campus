import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Spinner
import androidx.navigation.NavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hsfl.leo_nelly.capturethecampus.Challenge
import com.hsfl.leo_nelly.capturethecampus.ChallengeAdapter
import com.hsfl.leo_nelly.capturethecampus.MainViewModel
import com.hsfl.leo_nelly.capturethecampus.R

class DialogManager(private val context: Context) {
    fun showRetryDialog(
        mainViewModel: MainViewModel,
        onPositiveButtonClicked: (String) -> Unit,
        onNegativeButtonClicked: () -> Unit
    ) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.custom_retry_dialog, null)
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

    fun showChallengeSelectionDialog(
        predefinedChallenges: List<Challenge>,
        mainViewModel: MainViewModel,
        navController: NavController
    ) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_select_challenge, null)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.challengeRecyclerView)
        val cancelButton = dialogView.findViewById<Button>(R.id.dialogNegativeButton)

        recyclerView.layoutManager = LinearLayoutManager(context)
        val challengeAdapter = ChallengeAdapter(predefinedChallenges)
        recyclerView.adapter = challengeAdapter

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        challengeAdapter.setOnItemClickListener { position ->
            val selectedChallenge = predefinedChallenges[position]
            mainViewModel.selectedChallenge.value = selectedChallenge
            dialog.dismiss()
            navController.navigate(R.id.action_startFragment_to_createFragment)
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }



}
