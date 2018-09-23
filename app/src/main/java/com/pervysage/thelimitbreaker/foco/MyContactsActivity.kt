package com.pervysage.thelimitbreaker.foco

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_my_contacts.*

class MyContactsActivity : AppCompatActivity() {

    private val PICK_CONTACTS=1
    private val PERMISSION_REQUEST=2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_contacts)

        ivAddContact.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                            this@MyContactsActivity,
                            android.Manifest.permission.READ_CONTACTS
                            )==PackageManager.PERMISSION_GRANTED
            ){
                startActivityForResult(Intent(this@MyContactsActivity,PickContactsActivity::class.java),PICK_CONTACTS)
            }else{
                ActivityCompat.requestPermissions(
                        this,
                        arrayOf(android.Manifest.permission.READ_CONTACTS),
                        PERMISSION_REQUEST
                )
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode==PERMISSION_REQUEST && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            startActivityForResult(Intent(this@MyContactsActivity,PickContactsActivity::class.java),PICK_CONTACTS)
        }else{
            Toast.makeText(this,"You denied the permission",Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==PICK_CONTACTS && resultCode== Activity.RESULT_OK){
            // fetch from db and display in rv
        }
    }
}
