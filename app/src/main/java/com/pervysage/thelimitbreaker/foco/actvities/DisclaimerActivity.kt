package com.pervysage.thelimitbreaker.foco.actvities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.pervysage.thelimitbreaker.foco.R
import kotlinx.android.synthetic.main.activity_disclaimer.*

class DisclaimerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_disclaimer)

        btnOK.setOnClickListener {
            startActivity(Intent(this,PermissionsActivity::class.java))
        }
    }
}
