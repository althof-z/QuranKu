package com.example.quranku

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.quranku.ApiClient
import com.example.quranku.PredictRequest
import com.example.quranku.databinding.FragmentInfoBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InfoFragment : Fragment() {
    private var _binding: FragmentInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInfoBinding.inflate(inflater, container, false)

        binding.btnPredict.setOnClickListener {
            predict()
        }

        return binding.root
    }

    private fun predict() {
        try {
            val features = listOf(
                binding.inputSepalLength.text.toString().toFloat(),
                binding.inputSepalWidth.text.toString().toFloat(),
                binding.inputPetalLength.text.toString().toFloat(),
                binding.inputPetalWidth.text.toString().toFloat()
            )

            val request = PredictRequest(features)
            ApiClient.retrofit.predict(request).enqueue(object : Callback<com.example.quranku.PredictResponse> {
                override fun onResponse(
                    call: Call<com.example.quranku.PredictResponse>,
                    response: Response<com.example.quranku.PredictResponse>
                ) {
                    if (response.isSuccessful) {
                        binding.textResult.text = "Predicted Class: ${response.body()?.predicted_class}"
                    } else {
                        binding.textResult.text = "Prediction failed. Code: ${response.code()}"
                    }
                }

                override fun onFailure(call: Call<com.example.quranku.PredictResponse>, t: Throwable) {
                    binding.textResult.text = "Error: ${t.message}"
                }
            })
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Invalid input: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
