package ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import ui.utils.CustomDropdownMenu

@Composable
fun <T : Displayable> SettingComponent(
    title: String,
    selectedOption: T?,
    options: List<T>,
    addAllOption: Boolean = true,
    onOptionSelected: (T?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val actualOptions = if (addAllOption) listOf(null).plus(options) else options

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
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
        CustomDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            for (option in actualOptions) {
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        onOptionSelected(option)
                    },
                    modifier = Modifier.height(42.dp)
                ) {
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