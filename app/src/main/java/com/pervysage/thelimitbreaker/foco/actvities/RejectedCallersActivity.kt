package com.pervysage.thelimitbreaker.foco.actvities

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.pervysage.thelimitbreaker.foco.R
import com.pervysage.thelimitbreaker.foco.utils.scheduleDeleteOldRejectedCallers
import kotlinx.android.synthetic.main.activity_rejected_callers.*

class RejectedCallersActivity : AppCompatActivity() {

    data class RejectedCallerInfo(
            val name: String,
            val number: String,
            val time: String
    )

    private lateinit var sharedPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rejected_callers)

        scheduleDeleteOldRejectedCallers(this)

        sharedPrefs = getSharedPreferences(getString(R.string.SHARED_PREF_KEY), Context.MODE_PRIVATE)
        val smsStatus = sharedPrefs.getBoolean(getString(R.string.SMS_TO_CALLER), false)
        val rejectedCallers = sharedPrefs.getString(getString(R.string.REJECTED_CALLERS_KEY), "")
                ?: ""
        val rejectedNumbers = sharedPrefs.getString(getString(R.string.REJECTED_NUMBERS_KEY), "")
                ?: ""

        val rejectedTime = sharedPrefs.getString(getString(R.string.REJECTED_TIME), "") ?: ""

        val listRejectedCallers = rejectedCallers.split(";").filter { it != "" }
        val listRejectedNumbers = rejectedNumbers.split(";").filter { it != "" }
        val listRejectedTime = rejectedTime.split(";").filter { it != "" }

        ivCloseRejected.setOnClickListener {
            finish()
        }

        ivDeleteRejected.setOnClickListener {
            with(sharedPrefs.edit()) {
                putString(getString(R.string.REJECTED_TIME), "")
                putString(getString(R.string.REJECTED_NUMBERS_KEY), "")
                putString(getString(R.string.REJECTED_CALLERS_KEY), "")
            }.commit()
            rvRejectedCallers.visibility=View.GONE
            tvNoRejectedCallsLabel.visibility=View.VISIBLE
            tvRejectedInfo.visibility=View.GONE
        }
        if (listRejectedCallers.isEmpty() && listRejectedNumbers.isEmpty()) {
            tvNoRejectedCallsLabel.visibility = View.VISIBLE
            rvRejectedCallers.visibility = View.GONE
            tvRejectedInfo.visibility=View.GONE
        } else {
            tvNoRejectedCallsLabel.visibility = View.GONE
            rvRejectedCallers.visibility = View.VISIBLE
            tvRejectedInfo.visibility=View.VISIBLE
            val listRejected = ArrayList<RejectedCallerInfo>()
            for (i in listRejectedCallers.indices) {
                listRejected.add(RejectedCallerInfo(
                        listRejectedCallers[i],
                        listRejectedNumbers[i],
                        listRejectedTime[i]
                ))
            }
            val adapter = RejectedCallerAdapter(listRejected, this, smsStatus)
            rvRejectedCallers.adapter = adapter
            rvRejectedCallers.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        }

    }


    class RejectedCallerAdapter(private val callersList: List<RejectedCallerInfo>,
                                private val context: Context,
                                private val smsStatus: Boolean) : RecyclerView.Adapter<RejectedCallerAdapter.ViewHolder>() {
        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
            val li = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val itemView = li.inflate(R.layout.layout_rejected_caller, p0, false)
            return ViewHolder(itemView)
        }

        override fun getItemCount() = callersList.size

        override fun onBindViewHolder(holder: ViewHolder, p1: Int) {
            holder.bind(callersList[p1], smsStatus)
        }

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvRejectedName = itemView.findViewById<TextView>(R.id.tvRejectedName)
            private val tvRejectedNumber = itemView.findViewById<TextView>(R.id.tvRejectedNumber)
            private val tvMessageSentLabel = itemView.findViewById<TextView>(R.id.tvMessageSentLabel)
            private val tvRejectedTime = itemView.findViewById<TextView>(R.id.tvRejectedTime)

            fun bind(info: RejectedCallerInfo, smsStatus: Boolean) {
                tvRejectedName.text = "Name : ${info.name}"
                tvRejectedNumber.text = "Number : ${info.number}"
                tvRejectedTime.text = info.time

                if (!smsStatus)
                    tvMessageSentLabel.visibility = View.GONE
            }
        }
    }
}
