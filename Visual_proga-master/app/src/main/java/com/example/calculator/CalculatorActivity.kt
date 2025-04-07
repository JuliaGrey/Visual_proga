package com.example.calculator

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class CalculatorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_calculator)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val operation: TextView = findViewById(R.id.operation) as TextView
        val result: TextView = findViewById(R.id.result) as TextView
        val b_num7: TextView = findViewById(R.id.b_num7) as TextView
        val b_num8: TextView = findViewById(R.id.b_num8) as TextView
        val b_num9: TextView = findViewById(R.id.b_num9) as TextView
        val b_num_sc: TextView = findViewById(R.id.b_num_sc) as TextView
        val b_num_sc1: TextView = findViewById(R.id.b_num_sc1) as TextView
        val b_num4: TextView = findViewById(R.id.b_num4) as TextView
        val b_num5: TextView = findViewById(R.id.b_num5) as TextView
        val b_num6: TextView = findViewById(R.id.b_num6) as TextView
        val b_num_pr: TextView = findViewById(R.id.b_num_pr) as TextView
        val b_num_pt: TextView = findViewById(R.id.b_num_pt) as TextView
        val b_num1: TextView = findViewById(R.id.b_num1) as TextView
        val b_num2: TextView = findViewById(R.id.b_num2) as TextView
        val b_num3: TextView = findViewById(R.id.b_num3) as TextView
        val b_num_gh: TextView = findViewById(R.id.b_num_gh) as TextView
        val b_del: TextView = findViewById(R.id.b_del) as TextView
        val b_num000: TextView = findViewById(R.id.b_num000) as TextView
        val b_num_pnt: TextView = findViewById(R.id.b_num_pnt) as TextView
        val b_num0: TextView = findViewById(R.id.b_num0) as TextView
        val b_num_min: TextView = findViewById(R.id.b_num_min) as TextView
        val b_num_plus: TextView = findViewById(R.id.b_num_plus) as TextView
        val b_equal: TextView = findViewById(R.id.b_equal) as TextView
        val b_sqrt: TextView = findViewById(R.id.b_sqrt) as TextView
        val b_cos: TextView = findViewById(R.id.b_cos) as TextView
        val b_sin: TextView = findViewById(R.id.b_sin) as TextView
        val b_pi: TextView = findViewById(R.id.b_pi) as TextView
        b_cos.setOnClickListener { operation.append("cos(") }
        b_sin.setOnClickListener { operation.append("sin(") }
        b_sqrt.setOnClickListener { operation.append("sqrt(") }
        b_pi.setOnClickListener { operation.append("pi") }
        b_num1.setOnClickListener { operation.append("1") }
        b_num2.setOnClickListener { operation.append("2") }
        b_num3.setOnClickListener { operation.append("3") }
        b_num4.setOnClickListener { operation.append("4") }
        b_num5.setOnClickListener { operation.append("5") }
        b_num6.setOnClickListener { operation.append("6") }
        b_num7.setOnClickListener { operation.append("7") }
        b_num8.setOnClickListener { operation.append("8") }
        b_num9.setOnClickListener { operation.append("9") }
        b_num0.setOnClickListener {operation.append("0") }
        b_num000.setOnClickListener { operation.append("000") }
        b_num_pnt.setOnClickListener { operation.append(".") }
        b_num_pr.setOnClickListener { operation.append("%") }
        b_num_pt.setOnClickListener { operation.append("/") }
        b_num_gh.setOnClickListener { operation.append("*") }
        b_num_sc.setOnClickListener { operation.append("(") }
        b_num_sc1.setOnClickListener { operation.append(")") }
        b_num_min.setOnClickListener { operation.append("-") }
        b_num_plus.setOnClickListener { operation.append("+") }
        b_del.setOnClickListener {
            operation.text = ""
            result.text = ""
        }
        b_equal.setOnClickListener {
            val optext = operation.text.toString()
            if (optext.isNotEmpty()) {
                try {
                    val res = evaluateExpression(optext)
                    result.text = if (res == res.toLong().toDouble()) res.toLong().toString() else res.toString()
                } catch (e: Exception) {
                    result.text = "Error"
                }
            }
        }
    }

    fun evaluateExpression(expression: String): Double {
        val values = mutableListOf<Double>()
        val ops = mutableListOf<Char>()
        var i = 0
        while (i < expression.length) {
            if (expression[i] == ' ') {
                i++
                continue
            }
            if (expression[i].isDigit() || expression[i] == '.') {
                val start = i
                while (i < expression.length && (expression[i].isDigit() || expression[i] == '.')) {
                    i++
                }
                values.add(expression.substring(start, i).toDouble())
                continue
            }
            if (expression.startsWith("pi", i)) {
                values.add(3.1415926)
                i += 2
                continue
            }
            if (expression[i] == '(') {
                ops.add(expression[i])
            } else if (expression[i] == ')') {
                while (ops.isNotEmpty() && ops.last() != '(') {
                    values.add(applyOp(ops.removeAt(ops.size - 1), values.removeAt(values.size - 1), values.removeAt(values.size - 1)))
                }
                ops.removeAt(ops.size - 1)
            } else if (isOperator(expression[i])) {
                while (ops.isNotEmpty() && precedence(ops.last()) >= precedence(expression[i])) {
                    values.add(applyOp(ops.removeAt(ops.size - 1), values.removeAt(values.size - 1), values.removeAt(values.size - 1)))
                }
                ops.add(expression[i])
            } else if (expression.startsWith("sqrt", i)) {
                i += 4
                if (i < expression.length && expression[i] == '(') {
                    i++
                    val start = i
                    while (i < expression.length && expression[i] != ')') {
                        i++
                    }
                    val number = expression.substring(start, i).toDouble()
                    values.add(sqrt(number))
                    i++
                    continue
                }
            }
            else if (expression.startsWith("cos", i)) {
                i += 3
                if (i < expression.length && expression[i] == '(') {
                    i++
                    val start = i
                    while (i < expression.length && expression[i] != ')') {
                        i++
                    }
                    val number = Math.toRadians(expression.substring(start, i).toDouble())
                    values.add(cos(number))
                    i++
                    continue
                }
            }
            else if (expression.startsWith("sin", i)) {
                i += 3
                if (i < expression.length && expression[i] == '(') {
                    i++
                    val start = i
                    while (i < expression.length && expression[i] != ')') {
                        i++
                    }
                    val number = Math.toRadians(expression.substring(start, i).toDouble())
                    values.add(sin(number))
                    i++
                    continue
                }
            }
            i++
        }
        while (ops.isNotEmpty()) {
            values.add(applyOp(ops.removeAt(ops.size - 1), values.removeAt(values.size - 1), values.removeAt(values.size - 1)))
        }
        return values.last()
    }
    fun isOperator(c: Char): Boolean {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '%'
    }
    fun precedence(op: Char): Int {
        return when (op) {
            '+', '-' -> 1
            '*', '/', '%' -> 2
            else -> 0
        }
    }
    fun applyOp(op: Char, b: Double, a: Double): Double {
        return when (op) {
            '+' -> a + b
            '-' -> a - b
            '*' -> a * b
            '/' -> a / b
            '%' -> a % b
            else -> 0.0
        }
    }
}