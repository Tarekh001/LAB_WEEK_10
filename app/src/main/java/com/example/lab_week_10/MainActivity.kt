package com.example.lab_week_10

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.lab_week_10.database.Total
import com.example.lab_week_10.database.TotalDatabase
import com.example.lab_week_10.database.TotalObject
import com.example.lab_week_10.viewmodels.TotalViewModel
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object { const val ID = 1L }

    private val db by lazy { prepareDatabase() }
    private val viewModel by lazy {
        ViewModelProvider(this)[TotalViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        initializeValueFromDatabase()
        prepareViewModel()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initializeValueFromDatabase() {
        val list = db.totalDao().getTotal(ID)
        if (list.isEmpty()) {
            db.totalDao().insert(Total(id = ID, total = TotalObject(0, Date().toString())))
        } else {
            viewModel.setTotal(list.first().total.value)
        }
    }

    private fun prepareViewModel() {
        viewModel.total.observe(this) { value ->
            findViewById<android.widget.TextView>(R.id.text_total).text =
                getString(R.string.text_total, value)
        }

        findViewById<android.widget.Button>(R.id.button_increment).setOnClickListener {
            viewModel.incrementTotal()
        }
    }

    override fun onPause() {
        super.onPause()
        val value = viewModel.total.value ?: 0
        val date = Date().toString()
        db.totalDao().update(Total(ID, TotalObject(value, date)))
    }

    override fun onStart() {
        super.onStart()
        val list = db.totalDao().getTotal(ID)
        if (list.isNotEmpty()) {
            Toast.makeText(this, "Last updated: ${list.first().total.date}", Toast.LENGTH_LONG).show()
        }
    }

    private fun prepareDatabase(): TotalDatabase = Room.databaseBuilder(
        applicationContext,
        TotalDatabase::class.java,
        "total-database"
    ).allowMainThreadQueries()
        .fallbackToDestructiveMigration()
        .build()
}