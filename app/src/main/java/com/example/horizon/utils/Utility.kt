package com.example.horizon.utils
import android.app.AlertDialog
import android.content.Context

object Utility {
    val apiUrls: String = "http://192.168.1.13:3000"
    fun showAlert(context: Context,
                  title: String="",
                  message: String="",
                  onYes: Runnable? =null,
                  onNo: Runnable?=null){
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle(title)
        alertDialogBuilder.setMessage(message)
        alertDialogBuilder.setPositiveButton("Yes"){dialog, which ->
            onYes?.run()
        }
        alertDialogBuilder.setNegativeButton("No"){dialog, which ->
            onNo?.run()
        }
        alertDialogBuilder.show()
    }
}