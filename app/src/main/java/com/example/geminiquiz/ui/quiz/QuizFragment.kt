package com.example.geminiquiz.ui.quiz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.geminiquiz.ChatUiEvent
import com.example.geminiquiz.ChatViewModel
import com.example.geminiquiz.databinding.FragmentQuizBinding
import kotlinx.coroutines.launch

class QuizFragment : Fragment() {

    private var _binding: FragmentQuizBinding? = null
    private val binding get() = _binding!!

    private lateinit var chatViewModel: ChatViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        chatViewModel = ViewModelProvider(this).get(ChatViewModel::class.java)

        _binding = FragmentQuizBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textViewOne: TextView = binding.textViewOne
        val textViewTwo: TextView = binding.textViewTwo
        val textViewThree: TextView = binding.textViewThree
        val textViewFour: TextView = binding.textViewFour

        val editTextUser: EditText = binding.editTextUser
        val buttonSend: Button = binding.buttonSend

        val textViewTrueOrFalse = mutableMapOf<TextView, Boolean>()

        fun generateStatements(userInput: String): List<Pair<String, Boolean>> {
            return listOf(
                Pair("Write 1 false statement about the following topic and don't tell if it's true or false: $userInput", false),
                Pair("Write 1 false statement about the following topic and don't tell if it's true or false: $userInput", false),
                Pair("Write 1 false statement about the following topic and don't tell if it's true or false: $userInput", false),
                Pair("Write 1 true statement about the following topic and don't tell if it's true or false: $userInput", true)
            )
        }

        buttonSend.setOnClickListener {
            val userInput = editTextUser.text.toString()

            if (userInput.isNotBlank()) {
                val statements = generateStatements(userInput).shuffled()

                lifecycleScope.launch {
                    statements.forEach { (statement, _) ->
                        chatViewModel.onEvent(ChatUiEvent.SendPrompt(statement))
                    }
                }
            }
        }

        lifecycleScope.launch {
            chatViewModel.chatState.collect { chatState ->
                val shuffledStatements = chatState.chatList.filter { !it.isFromUser }
                val finalStatements = shuffledStatements.take(4).map { it.prompt }

                val statementsTrueOrFalse = generateStatements("dummyInput").shuffled()
                val statementsPairs = finalStatements.zip(statementsTrueOrFalse.map { it.second })

                val textViews = listOf(textViewOne, textViewTwo, textViewThree, textViewFour)
                val shuffledTextViews = textViews.shuffled()

                shuffledTextViews.forEachIndexed { index, textView ->
                    textView.text = statementsPairs.getOrNull(index)?.first ?: "Statement ${index + 1}"
                    textViewTrueOrFalse[textView] = statementsPairs.getOrNull(index)?.second ?: false
                }
            }
        }

        val checkAnswer = View.OnClickListener { view ->
            val isTrue = textViewTrueOrFalse[view]
            if (isTrue == true) {
                Toast.makeText(requireContext(), "You won!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "You lost!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.textViewOne.setOnClickListener(checkAnswer)
        binding.textViewTwo.setOnClickListener(checkAnswer)
        binding.textViewThree.setOnClickListener(checkAnswer)
        binding.textViewFour.setOnClickListener(checkAnswer)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}