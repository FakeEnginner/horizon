package com.example.horizon.ui.fragment.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.horizon.Interface.FrameLayoutChanger
import com.example.horizon.MainActivity
import com.example.horizon.R
import com.example.horizon.Repository.SignupRepository
import com.example.horizon.databinding.FragmentSignupBinding
import com.example.horizon.factory.SignUpViewModelFactory
import com.example.horizon.model.sealedClass.SignUpResult
import com.example.horizon.model.sealedClass.SignupNavigation
import com.example.horizon.ui.fragment.dashboard.Dashboard
import com.example.horizon.ui.fragment.login.login
import com.example.horizon.utils.Helper
import com.example.horizon.viewModel.SignUpViewModel


class signup: Fragment() {
    val helper = Helper()
    lateinit var binding: FragmentSignupBinding
    private var frameLayoutChanger: FrameLayoutChanger? = null
    private val viewModel: SignUpViewModel by viewModels { SignUpViewModelFactory(SignupRepository())}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignupBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.apply {
            createaccount.setOnClickListener {
                viewModel.signUp(usernameOr.text.toString() , password.text.toString() , cnfpassword.text.toString())
            }
            logintxt.setOnClickListener {
                viewModel.onLoginClicked()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.signUpResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is SignUpResult.sucess -> navigateToDashboard()
                is SignUpResult.Error -> showError(result.message)
                is SignUpResult.Loading -> showLoading()
            }
        }
        viewModel.navigation.observe(viewLifecycleOwner) { navigation ->
            when (navigation) {
                is SignupNavigation.ToLogin -> onLoginClicked()
                is SignupNavigation.ToDashboard -> navigateToDashboard()
            }
        }
    }

    private fun navigateToDashboard() {
        frameLayoutChanger?.replaceFrameLayout()
        helper?.replacetoDashboardFragment(Dashboard(),requireFragmentManager())
        (requireActivity() as MainActivity).showDashboardContainer()
    }

    private fun onLoginClicked() {
        helper?.replaceFragment(login(),requireFragmentManager())
    }

    private fun showError(message: String) {
        // Show error message to user
    }

    private fun showLoading() {
        // Show loading indicator
    }


}