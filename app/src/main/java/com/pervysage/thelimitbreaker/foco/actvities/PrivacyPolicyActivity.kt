package com.pervysage.thelimitbreaker.foco.actvities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import com.pervysage.thelimitbreaker.foco.R
import kotlinx.android.synthetic.main.activity_privacy_policy.*

class PrivacyPolicyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_policy)
        tvPrivacyPolicy.text = Html.fromHtml(getString(R.string.PRIVACY_POLICY))
    }
}
