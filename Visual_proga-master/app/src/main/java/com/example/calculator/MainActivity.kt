package com.example.calculator
import android.content.Intent
import net.objecthunter.exp4j.ExpressionBuilder
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val calcButton: Button = findViewById(R.id.calculator_activity)
        calcButton.setOnClickListener {
            val intent = Intent(this, CalculatorActivity::class.java)
            startActivity(intent)
        }
        val MP3Button: Button = findViewById(R.id.MP3_activity)
        MP3Button.setOnClickListener {
            val intent = Intent(this, MP3Activity::class.java)
            startActivity(intent)
        }
        val GPSButton: Button = findViewById(R.id.GPS_activity)
        GPSButton.setOnClickListener {
            val intent = Intent(this, GPS::class.java)
            startActivity(intent)
        }
    }
}
