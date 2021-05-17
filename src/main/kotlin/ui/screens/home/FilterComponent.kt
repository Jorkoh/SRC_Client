package ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.vectorXmlResource

@Composable
fun <T : Displayable> FilterComponent(
    title: String,
    selectedOption: T?,
    options: List<T>,
    onOptionSelected: (T?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val optionsWithAny = listOf(null).plus(options)

    Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { expanded = true }
                .alpha(if (selectedOption?.uiString != null) 1f else 0.4f)
        ) {
            Text(
                text = "$title: ${selectedOption?.uiString ?: "All"}",
                style = MaterialTheme.typography.subtitle1
            )
            Icon(imageVector = vectorXmlResource("ic_expand.xml"), contentDescription = null)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            for (option in optionsWithAny) {
                DropdownMenuItem(onClick = {
                    expanded = false
                    onOptionSelected(option)
                }) {
                    Text(
                        text = option?.uiString ?: "All",
                        modifier = Modifier.alpha(if (option?.uiString != null) 1f else 0.4f)
                    )
                }
            }
        }
    }
}

interface Displayable {
    val uiString: String
}