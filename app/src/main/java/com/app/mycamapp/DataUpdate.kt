package com.app.mycamapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.app.mycamapp.databinding.ActivityDataUpdateBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class DataUpdate : AppCompatActivity() {

    private lateinit var binding: ActivityDataUpdateBinding
    private lateinit var database : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDataUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.updateBtn.setOnClickListener {

            val nid= binding.NID.text.toString()
            val name = binding.Name.text.toString()
            val gender = binding.Gender.text.toString()
            val age = binding.Age.text.toString()

            updateData(nid,name,gender,age)
           val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)

        }

    }

    private fun updateData(nid: String, name: String, gender: String, age: String) {

        database = FirebaseDatabase.getInstance().getReference("Users")
        val user = mapOf<String,String>(
            "Name" to name,
            "gender" to gender,
            "age" to age
        )

        database.child(nid).updateChildren(user).addOnSuccessListener {
            binding.NID.text.clear()
            binding.Name.text.clear()
            binding.Gender.text.clear()
            binding.Age.text.clear()
            Toast.makeText(this,"Successfuly Updated", Toast.LENGTH_SHORT).show()
            //intent change
            //implement code here


        }.addOnFailureListener{

            Toast.makeText(this,"Failed to Update", Toast.LENGTH_SHORT).show()

        }}
}
