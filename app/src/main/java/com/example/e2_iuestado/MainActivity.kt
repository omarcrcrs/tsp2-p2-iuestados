package com.example.e2_iuestado

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.e2_iuestado.ui.theme.E2_iuestadoTheme
import java.text.NumberFormat
import java.util.Locale

// ───────────────────────── Estilo personal ─────────────────────────
// Un solo lugar para retocar la vibra de la app sin tocar la lógica.
private val VerdePipBoy = Color(0xFF1B5E20)   // guiño Fallout, legible sobre LightGray
private val FondoApp = Color(0xFFB0BEC5)      // azul grisáceo suave (Blue Grey 200)

// ───────────────────────── Lógica pura ─────────────────────────
/**
 * Monto total = precio por litro × litros (+ propina, solo si el Switch está activo).
 * Va separada de la UI para poder probarla con tests unitarios más adelante.
 */
fun calcularMontoTotal(
    precioPorLitro: Double,
    litros: Double,
    propina: Double,
    conPropina: Boolean
): Double = precioPorLitro * litros + if (conPropina) propina else 0.0

/** Formato en pesos mexicanos; fijar el Locale evita el símbolo "¤" que sale en el Preview. */
fun formatearMoneda(monto: Double): String =
    NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-MX")).format(monto)

// ───────────────────────── Activity ─────────────────────────
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            E2_iuestadoTheme(){
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = FondoApp
                ) { innerPadding ->
                    CostGasLayout(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

// ───────────────────────── UI ─────────────────────────
@Composable
fun CostGasLayout(modifier: Modifier = Modifier) {
    // Estado: el texto de cada campo y el Switch sobreviven a rotaciones (rememberSaveable)
    var precioTexto by rememberSaveable { mutableStateOf("") }
    var litrosTexto by rememberSaveable { mutableStateOf("") }
    var propinaTexto by rememberSaveable { mutableStateOf("") }
    var conPropina by rememberSaveable { mutableStateOf(false) }

    // Valor derivado: se recalcula solo cada vez que cambia algún estado
    val montoTotal = calcularMontoTotal(
        precioPorLitro = precioTexto.toDoubleOrNull() ?: 0.0,
        litros = litrosTexto.toDoubleOrNull() ?: 0.0,
        propina = propinaTexto.toDoubleOrNull() ?: 0.0,
        conPropina = conPropina
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FondoApp)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,           // centrado vertical respecto a la pantalla
        horizontalAlignment = Alignment.CenterHorizontally  // centrado horizontal
    ) {
        Text(
            text = "Calcular Monto de Gasolina",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        CampoNumerico(
            valor = precioTexto,
            alCambiar = { precioTexto = it },
            etiqueta = "Ingresa precio por litro de Gasolina",
            icono = R.drawable.ic_precio
        )
        CampoNumerico(
            valor = litrosTexto,
            alCambiar = { litrosTexto = it },
            etiqueta = "Litros",
            icono = R.drawable.ic_litros
        )
        CampoNumerico(
            valor = propinaTexto,
            alCambiar = { propinaTexto = it },
            etiqueta = "Propina",
            icono = R.drawable.ic_propina,
            habilitado = conPropina // el campo solo se puede editar si el Switch está activo
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "¿Deseas agregar la propina?")
            Spacer(modifier = Modifier.padding(horizontal = 8.dp))
            Switch(
                checked = conPropina,
                onCheckedChange = { conPropina = it },
                colors = SwitchDefaults.colors(checkedTrackColor = VerdePipBoy)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Un Text no es editable por el usuario → cumple "no pueda ser modificado"
        Text(
            text = "Monto Total: ${formatearMoneda(montoTotal)}",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = VerdePipBoy,
            textAlign = TextAlign.Center
        )
    }
}

/** Campo de texto numérico reutilizable: ícono (drawable propio) a la izquierda, teclado decimal. */
@Composable
private fun CampoNumerico(
    valor: String,
    alCambiar: (String) -> Unit,
    etiqueta: String,
    @DrawableRes icono: Int,
    habilitado: Boolean = true
) {
    TextField(
        value = valor,
        onValueChange = alCambiar,
        enabled = habilitado,
        singleLine = true,
        label = { Text(etiqueta) },
        leadingIcon = {
            Icon(
                painter = painterResource(id = icono),
                contentDescription = etiqueta,
                tint = Color.Unspecified, // respeta los colores originales del ícono
                modifier = Modifier
                    .size(24.dp)
                    .alpha(if (habilitado) 1f else 0.4f)
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth()
    )
}

// ───────────────────────── Preview ─────────────────────────
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CostGasLayoutPreview() {
        CostGasLayout()
    }
