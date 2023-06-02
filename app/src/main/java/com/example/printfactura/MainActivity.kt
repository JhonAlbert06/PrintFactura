package com.example.printfactura

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.example.printfactura.ui.theme.PrintFacturaTheme
import java.io.OutputStream
import java.util.*
import android.text.SpannableStringBuilder
import androidx.compose.material3.ExperimentalMaterial3Api

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PrintFacturaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PrintButton()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun PrintButton() {
    val context = LocalContext.current
    var isPrinting = false
    val impresoraNameState = remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = impresoraNameState.value,
            onValueChange = { impresoraNameState.value = it },
            label = { Text(text = "Nombre de la impresora") },
            modifier = Modifier.padding(16.dp)
        )

        Button(
            onClick = {
                if (!isPrinting) {
                    isPrinting = true
                    printFactura(context, impresoraNameState.value)
                    isPrinting = false
                }
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Imprimir")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
fun printFactura(context: Context, impresoraName: String) {
    // Verificar si se tienen los permisos necesarios
    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH
        ) != PackageManager.PERMISSION_GRANTED ||
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_ADMIN
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        // Salir de la función si los permisos no están concedidos
        return
    }

    // Aquí va el código para imprimir, adaptado según lo mencionado anteriormente
    // Obtén la instancia del BluetoothAdapter
    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    // Asegúrate de que el Bluetooth esté activado y disponible en tu dispositivo
    if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
        // Manejar caso cuando el Bluetooth no está disponible o no está activado
        return
    }

    // Busca y empareja la impresora Bluetooth
    val dispositivosEmparejados: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
    val impresora: BluetoothDevice? = dispositivosEmparejados?.firstOrNull { device ->
        device.name?.startsWith(impresoraName, ignoreCase = true) == true
    }

    // Verifica si se encontró la impresora
    if (impresora == null) {
        // Manejar caso cuando la impresora no se encontró
        return
    }

    // Establece la conexión Bluetooth con la impresora
    val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // UUID para el perfil SPP (Serial Port Profile)
    val socket: BluetoothSocket = impresora.createRfcommSocketToServiceRecord(uuid)

    // Conecta el socket
    socket.connect()

    // Obtén el OutputStream del socket Bluetooth
    val outputStream: OutputStream = socket.outputStream

    // Genera el contenido de la factura
    val contenidoFactura = generarContenidoFactura() // Asegúrate de definir y rellenar la función generarContenidoFactura() con tu lógica de generación de facturas

    // Envía los datos a la impresora Bluetooth
    enviarDatosImpresoraBluetooth(contenidoFactura, outputStream)

    // Cierra el OutputStream y el socket Bluetooth
    outputStream.close()
    socket.close()
}

fun generarContenidoFactura(): String {

    val productos = listOf(
        "Producto A",
        "Producto B",
        "Producto C",
        "Producto D",
        "Producto E",
        "Producto F",
        "Producto G",
        "Producto H",
        "Producto I",
        "Producto J"
    )
    val builder = SpannableStringBuilder()

    builder.appendLine("--------------------------------")
    builder.appendLine("Factura")
    builder.appendLine("--------------------------------")

    builder.appendLine("Detalle de la compra:")
    builder.appendLine("--------------------------------")
    builder.appendLine("Cant/Precio/Desc  ITBIS  Valor")
    builder.appendLine("--------------------------------")

    productos.forEachIndexed { index, producto ->
        val cantidad = (index + 1).toString()
        val precio = "$10"
        val itbis = "3.4"
        val subtotal = "$40"

        builder.appendLine(producto)
        builder.appendLine("   $cantidad X $precio       $itbis     $subtotal")
    }
    builder.appendLine("")

    builder.appendLine("-------------------------")
    builder.appendLine("Cantidad Total: 5")
    builder.appendLine("Total a Pagar: $65")
    builder.appendLine("-------------------------")
    builder.appendLine("\n\n\n")
    return builder.toString()
}

fun enviarDatosImpresoraBluetooth(datos: String, outputStream: OutputStream) {
    val bytes = datos.toByteArray()
    outputStream.write(bytes)
    outputStream.flush()
}

@RequiresApi(Build.VERSION_CODES.S)
@Preview(showBackground = true)
@Composable
fun PrintButtonPreview() {
    PrintFacturaTheme {
        PrintButton()
    }
}
