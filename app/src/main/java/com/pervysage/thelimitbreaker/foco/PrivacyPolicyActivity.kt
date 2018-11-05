package com.pervysage.thelimitbreaker.foco

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import kotlinx.android.synthetic.main.activity_privacy_policy.*
import kotlin.reflect.jvm.internal.impl.renderer.RenderingFormat

class PrivacyPolicyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_policy)
        tvPrivacyPolicy.text = Html.fromHtml(getString(R.string.PRIVACY_POLICY))
    }
}
