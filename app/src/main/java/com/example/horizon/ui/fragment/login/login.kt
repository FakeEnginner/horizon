package com.example.horizon.ui.fragment.login

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.horizon.Interface.FrameLayoutChanger
import com.example.horizon.MainActivity
import androidx.fragment.app.viewModels
import com.example.horizon.Repository.LoginRepository
import com.example.horizon.databinding.FragmentLoginBinding
import com.example.horizon.factory.LoginViewModelFactory
import com.example.horizon.model.sealedClass.LoginResult
import com.example.horizon.ui.fragment.dashboard.Dashboard
import com.example.horizon.ui.fragment.forgetpassword.ForgotPassword
import com.example.horizon.ui.fragment.signup.signup
import com.example.horizon.utils.Helper
import com.example.horizon.model.sealedClass.LoginNavigation
import com.example.horizon.viewModel.LoginViewModel

class login : Fragment(){
    val helper = Helper()
    private lateinit var binding : FragmentLoginBinding
    private var frameLayoutChanger: FrameLayoutChanger? = null
    private val viewModel: LoginViewModel by viewModels { LoginViewModelFactory(LoginRepository()) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.apply {
            Login.setOnClickListener {
                viewModel.login(usernameOr.text.toString(), password.text.toString())
            }
            signUplogin.setOnClickListener { viewModel.onSignUpClicked() }
            forgotPass.setOnClickListener { viewModel.onForgotPasswordClicked() }
        }
    }

    private fun observeViewModel() {
        viewModel.loginResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is LoginResult.Success -> navigateToDashboard()
                is LoginResult.Error -> showError(result.message)
                is LoginResult.Loading -> showLoading()
            }
        }
        viewModel.navigation.observe(viewLifecycleOwner) { navigation ->
            when (navigation) {
                is LoginNavigation.ToSignUp -> navigateToSignUp()
                is LoginNavigation.ToForgotPassword -> navigateToForgotPassword()
            }
        }
    }

    private fun navigateToDashboard() {
        frameLayoutChanger?.replaceFrameLayout()
        helper?.replacetoDashboardFragment(Dashboard(),requireFragmentManager())
        (requireActivity() as MainActivity).showDashboardContainer()
    }

    private fun navigateToSignUp() {
        helper?.replaceFragment(signup(),requireFragmentManager())
    }

    private fun navigateToForgotPassword() {
        helper?.replaceFragment(ForgotPassword(),requireFragmentManager())
    }

    private fun showError(message: String) {
        // Show error message to user
    }

    private fun showLoading() {
        // Show loading indicator
    }

}