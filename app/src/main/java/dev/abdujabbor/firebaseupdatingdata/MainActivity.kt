package dev.abdujabbor.firebaseupdatingdata

import android.R
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import dev.abdujabbor.firebaseupdatingdata.databinding.ActivityMainBinding
import dev.abdujabbor.firebaseupdatingdata.utils.MyConstants

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private val personCollectionRef = Firebase.firestore.collection("persons")
    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        var list = ArrayList<Int>()
        for (i in 1..140) {
            list.add(i)
        }
        val adapter = ArrayAdapter(this, R.layout.simple_selectable_list_item, list)
        binding.endCount.adapter = adapter
        binding.fromCount.adapter = adapter
        binding.fromCount.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                val selectedItem = parent?.getItemAtPosition(position).toString()
                MyConstants.fromCount = selectedItem.toInt()
                subscribeToRealTimeUpdated()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
        binding.endCount.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                val selectedItem = parent?.getItemAtPosition(position).toString()
                MyConstants.endCount = selectedItem.toInt()
                subscribeToRealTimeUpdated()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
        binding.btnUploadData.setOnClickListener {
            val person = getOldPerson()
            savePerson(person)
        }
        binding.btnUpdatePerson.setOnClickListener {
            val oldperson = getOldPerson()
            val newPersonMap = getNewPersonMap()
            updatePerson(oldperson,newPersonMap)
        }
        subscribeToRealTimeUpdated()
    }
    
    fun subscribeToRealTimeUpdated() = CoroutineScope(Dispatchers.IO).launch {
        personCollectionRef.whereLessThan("age", MyConstants.endCount)
            .whereGreaterThan("age", MyConstants.fromCount).orderBy("age")
            .addSnapshotListener { querysnapshot, error ->
                error?.let {
                    return@addSnapshotListener
                }
                val sb = java.lang.StringBuilder()
                for (document in querysnapshot?.documents!!) {
                    val person = document.toObject<Person>()
                    sb.append("$person\n")
                }
                binding.tvPersons.text = sb.toString()


            }
    }

    fun reciece() = CoroutineScope(Dispatchers.IO).launch {
        try {
            val querySnapshot = personCollectionRef.get().await()
            val sb = java.lang.StringBuilder()
            for (document in querySnapshot.documents) {
                val person = document.toObject<Person>()
                sb.append("$person\n")
            }
            withContext(Dispatchers.Main) {
                binding.tvPersons.text = sb.toString()
            }
        } catch (e: java.lang.Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getOldPerson(): Person {
        binding.apply {
            val firstName = etFirstName.text.toString()
            val lastName = etLastName.text.toString()
            val age = etAge.text.toString().toInt()
            return Person(firstName, lastName, age)
        }
    }

    private fun getNewPersonMap(): Map<String, Any> {
        binding.apply {
            val firstName = etNewFirstName.text.toString()
            val lastName = etNewLastName.text.toString()
            val age = etNewAge.text.toString()
            val map = mutableMapOf<String, Any>()
            if (firstName.isNotEmpty()) {
                map["firstName"] = firstName
            }
            if (lastName.isNotEmpty()) {
                map["lastName"] = lastName
            }
            if (age.isNotEmpty()) {
                map["age"] = age.toInt()
            }
            return map
        }
    }


    private fun updatePerson(person: Person, newPersonMap: Map<String, Any>) = CoroutineScope(Dispatchers.IO).launch {
        val personQuery = personCollectionRef
            .whereEqualTo("firstName", person.firstName)
            .whereEqualTo("lastName", person.lastName)
            .whereEqualTo("age", person.age)
            .get()
            .await()
        if(personQuery.documents.isNotEmpty()) {
            for(document in personQuery) {
                try {
                    //personCollectionRef.document(document.id).update("age", newAge).await()
                    personCollectionRef.document(document.id).set(
                        newPersonMap,
                        SetOptions.merge()
                    ).await()
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "No persons matched the query.", Toast.LENGTH_LONG).show()
            }
        }
    }
    //save person with save btn to firebase
    fun savePerson(person: Person) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                withContext(Dispatchers.Main) {
                    personCollectionRef.add(person).await()
                    Toast.makeText(this@MainActivity, "Succesfully saved", Toast.LENGTH_SHORT)
                        .show()
                }
            } catch (e: java.lang.Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}


