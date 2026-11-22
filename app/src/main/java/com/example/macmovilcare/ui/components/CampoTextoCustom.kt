package com.example.macmovilcare.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.macmovilcare.ui.theme.AzulVariante
import com.example.macmovilcare.ui.theme.GrisBorde
import com.example.macmovilcare.ui.theme.TextoOscuro
import com.example.macmovilcare.ui.theme.TextoSecundario

@Composable
fun CampoTextoCustom(
    valor: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String? = null,
    modifier: Modifier = Modifier.fillMaxWidth(),
    singleLine: Boolean = true,
    minLines: Int = 1,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 13.sp) },
        textStyle = TextStyle(fontSize = 14.sp, color = TextoOscuro),
        leadingIcon = leadingIcon,
        isError = error != null,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AzulVariante,
            unfocusedBorderColor = GrisBorde,
            focusedLabelColor = AzulVariante,
            unfocusedLabelColor = TextoSecundario,
            focusedContainerColor = Color(0xFF151F22),
            unfocusedContainerColor = Color(0xFF151F22),
            cursorColor = AzulVariante
        ),
        supportingText = if (error != null) {
            {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        } else null,
        singleLine = singleLine,
        minLines = minLines,
        modifier = modifier
    )
}