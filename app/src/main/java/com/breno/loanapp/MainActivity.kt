package com.breno.loanapp

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import androidx.activity.compose.setContent
import androidx.activity.ComponentActivity
import java.text.NumberFormat
import java.math.RoundingMode
import java.math.MathContext
import java.math.BigDecimal
import java.util.Locale
import android.os.Bundle

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MaterialTheme {
        LoanCalculatorScreen()
      }
    }
  }
}

@Composable
fun LoanCalculatorScreen() {
  var valueLoaned by remember { mutableStateOf("") }
  var interestRate by remember { mutableStateOf("") }
  var loanTerm by remember { mutableStateOf("") }
  var message by remember { mutableStateOf<String?>(null) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.Top,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Spacer(modifier = Modifier.height(32.dp))

    TextField(
      value = valueLoaned,
      // Updates the loan amount input as the user types
      onValueChange = { valueLoaned = it },
      label = { Text("Valor a ser emprestado") }
    )

    Spacer(modifier = Modifier.height(16.dp))

    TextField(
      value = interestRate,
      // Updates the interest rate, dividing it by 100 to represent percentage
      onValueChange = { interestRate = it },
      label = { Text("Juros ao mês (em %)") }
    )

    Spacer(modifier = Modifier.height(16.dp))

    TextField(
      value = loanTerm,
      // Updates the number of months (loan term) input
      onValueChange = { loanTerm = it },
      label = { Text("Número de meses") }
    )

    Spacer(modifier = Modifier.height(16.dp))

    Button(
      onClick = {
        val initial = valueLoaned.toDoubleOrNull()
        val interest = interestRate.toDoubleOrNull()?.div(100)
        val months = loanTerm.toIntOrNull()

        if (initial != null && interest != null && months != null && months > 0)
        {
          val monthlyInstallmentValue = calculateLoan(initial, interest, months)
          val totalPaid = monthlyInstallmentValue * months
          message =
            "Valor das parcelas: " +
                "${convertValuesToLocalCurrency(monthlyInstallmentValue)}\n" +
                "Valor total pago no final: " +
                convertValuesToLocalCurrency(totalPaid)
        } else {
          message = "Preencha todos os campos"
        }
      },
      colors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFF66BB6A),
        contentColor = Color.White
      ),
      shape = RoundedCornerShape(16.dp),
      modifier = Modifier.width(112.dp)
    ) {
      Text("Calcular")
    }

    Spacer(modifier = Modifier.height(16.dp))

    message?.let {
      Text(
        text = it, // 'it' is the non-null 'message' being used safely.
        color = if (it.contains("Preencha")) Color.Red else Color.Black,
        style = MaterialTheme.typography.bodyMedium
      )
    }
  }
}

// Calculates the monthly loan installment using BigDecimal for precision
fun calculateLoan(
  loanValue: Double,
  rate: Double,
  months: Int
): Double {
  val loan = BigDecimal(loanValue.toString())
  val interestRate = BigDecimal(rate.toString())

  // loanAmount * interestRate / (1 - (1 + interestRate)^(-months))
  val result = loan.multiply(interestRate).divide(
    BigDecimal(1).subtract(
      BigDecimal(1).add(interestRate).pow(-months, MathContext.DECIMAL64)
    ),
    RoundingMode.HALF_EVEN
  )
  return result.toDouble()
}

// Formats a double value to Brazilian currency format
fun convertValuesToLocalCurrency(value: Double): String {
  val formattedValue =
    NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(value)
  return if (value % 1.0 == 0.0)
    formattedValue.replace(",00", "") // Remove decimal part if it's zero
  else formattedValue
}
