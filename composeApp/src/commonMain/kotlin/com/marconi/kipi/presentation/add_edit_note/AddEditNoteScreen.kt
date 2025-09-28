package com.marconi.kipi.presentation.add_edit_note

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.github.skydoves.colorpicker.compose.ColorPickerController
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.marconi.kipi.presentation.add_edit_note.components.RichTextToolbar
import com.marconi.kipi.presentation.add_edit_note.components.RichTextField
import com.marconi.kipi.presentation.add_edit_note.components.TransparentHintTextField
import com.marconi.kipi.ui.theme.FontTypes
import com.marconi.kipi.ui.theme.getFontFamily
import kotlinx.coroutines.launch
import kipi.composeapp.generated.resources.Res
import kipi.composeapp.generated.resources.black_label
import kipi.composeapp.generated.resources.font_color
import kipi.composeapp.generated.resources.font_family
import kipi.composeapp.generated.resources.font_size
import kipi.composeapp.generated.resources.select_color
import kipi.composeapp.generated.resources.selected_color
import kipi.composeapp.generated.resources.text_settings
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

@Composable
fun AddEditNoteScreen(
    noteColor: Int,
) {
    val viewModel: AddEditNoteViewModel = koinViewModel()
    val titleState = viewModel.noteTitle.value
    val contentState = viewModel.noteContent.value
    val uiState by viewModel.uiState.collectAsState()
    val selectedCustomColor by viewModel.selectedCustomColor.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState(16f)
    val textColor by viewModel.textColor.collectAsState(Color.Black.toArgb())
    val isDialogVisible by viewModel.isColorDialogVisible.collectAsState(false)
    val isTextDialogVisible by viewModel.isFontDialogVisible.collectAsState(false)
    val fontFamily by viewModel.fontFamily.collectAsState()
    val colorExpanded by viewModel.colorExpanded.collectAsState()
    val noteDefaultColors by viewModel.defaultColors.collectAsState()
    val hasSelection by viewModel.hasSelection.collectAsState()
    val activeStyles by viewModel.activeStyles.collectAsState()

    val noteBackgroundAnimatable = remember {
        Animatable(
            Color(if (noteColor != -1) noteColor else viewModel.noteColor.value)
        )
    }
    val scope = rememberCoroutineScope()

    if (isDialogVisible) {
        ColorPickerDialog(
            onColorChanged = { color ->
                color.toArgb().let {
                    scope.launch {
                        noteBackgroundAnimatable.animateTo(
                            targetValue = Color(it),
                            animationSpec = tween(
                                durationMillis = 500
                            )
                        )
                    }
                    viewModel.onEvent(AddEditNoteEvent.ChangeColor(it))
                }
                viewModel.setSelectedCustomColor(color)
            },
            onDismissRequest = {
                viewModel.toggleColorDialogVisibility()
            },
            color = selectedCustomColor ?: Color(viewModel.noteColor.value)
        )
    }

    if (isTextDialogVisible) {
        TextSettingsDialog(
            onDismissRequest = viewModel::toggleFontDialogVisibility,
            initialFontColor = Color(textColor),
            initialFontSize = fontSize,
            initialFontFamily = fontFamily,
            onFontSizeChange = {
                viewModel.onEvent(AddEditNoteEvent.ChangeFontSize(it))
            },
            onFontColorChange = {
                viewModel.onEvent(AddEditNoteEvent.ChangeTextColor(it.toArgb()))
            },
            onFontFamilyChange = {
                viewModel.onEvent(AddEditNoteEvent.ChangeFontFamily(it))
            }
        )
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box {
                    ColorCircle(
                        Color(viewModel.noteColor.value),
                        borderColor = MaterialTheme.colorScheme.primary,
                        onClick = { viewModel.setColorExpanded(true) }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = stringResource(Res.string.select_color),
                            modifier = Modifier.align(Alignment.Center),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    DropdownMenu(
                        expanded = colorExpanded,
                        onDismissRequest = { viewModel.setColorExpanded(false) },
                    ) {
                        noteDefaultColors.forEach { color ->
                            DropdownMenuItem(
                                onClick = {
                                    scope.launch {
                                        viewModel.onEvent(AddEditNoteEvent.ChangeColor(color.toArgb()))
                                        viewModel.setColorExpanded(false)
                                        noteBackgroundAnimatable.animateTo(
                                            targetValue = color,
                                            animationSpec = tween(
                                                durationMillis = 500
                                            )
                                        )
                                    }
                                },
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .background(color, shape = CircleShape)
                                                .border(
                                                    1.dp,
                                                    Color.Black.copy(alpha = 0.2f),
                                                    CircleShape
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "#${
                                                color.toArgb().toUInt().toString(16).uppercase()
                                                    .padStart(8, '0')
                                            }",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
                ColorCircle(
                    selectedCustomColor ?: MaterialTheme.colorScheme.primary,
                    borderColor = MaterialTheme.colorScheme.inversePrimary,
                    onClick = viewModel::toggleColorDialogVisibility,
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.FormatColorFill,
                            contentDescription = stringResource(Res.string.select_color),
                            modifier = Modifier
                                .align(Alignment.Center),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                )
                ColorCircle(
                    MaterialTheme.colorScheme.primary,
                    borderColor = MaterialTheme.colorScheme.inversePrimary,
                    onClick = viewModel::toggleFontDialogVisibility,
                ) {
                    Icon(
                        imageVector = Icons.Filled.FormatColorText,
                        contentDescription = stringResource(Res.string.select_color),
                        modifier = Modifier
                            .align(Alignment.Center),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        },
        bottomBar = {
            RichTextToolbar(
                onStyleToggle = { style ->
                    viewModel.onEvent(AddEditNoteEvent.ToggleStyle(style))
                },
                activeStyles = activeStyles
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.onEvent(AddEditNoteEvent.SaveNote)
                },
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {

            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(noteBackgroundAnimatable.value, RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                TransparentHintTextField(
                    text = titleState.text,
                    hint = titleState.hint?.let { stringResource(it) } ?: "",
                    onValueChange = {
                        viewModel.onEvent(AddEditNoteEvent.EnteredTitle(it))
                    },
                    onFocusChange = {
                        viewModel.onEvent(AddEditNoteEvent.ChangeTitleFocus(it))
                    },
                    isHintVisible = titleState.isHintVisible,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = fontSize.sp,
                        color = Color(textColor),
                        fontWeight = FontWeight.Bold,
                        fontFamily = getFontFamily(fontFamily)
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                RichTextField(
                    text = contentState.text,
                    hint = contentState.hint,
                    isHintVisible = contentState.isHintVisible,
                    styles = uiState.styles,
                    onValueChange = { viewModel.onEvent(AddEditNoteEvent.EnteredContent(it)) },
                    onFocusChange = { viewModel.onEvent(AddEditNoteEvent.ChangeContentFocus(it)) },
                    onSelectionChange = { viewModel.updateSelection(it) },
                    textStyle = TextStyle(
                        fontSize = fontSize.sp,
                        color = Color(textColor),
                        fontWeight = FontWeight.Normal,
                        fontFamily = getFontFamily(fontFamily)
                    ),
                    modifier = Modifier.fillMaxHeight()
                )
            }
            Spacer(modifier = Modifier.height(90.dp))
        }
    }
}

@Composable
fun TextSettingsDialog(
    onDismissRequest: () -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onFontColorChange: (Color) -> Unit,
    onFontFamilyChange: (FontTypes) -> Unit,
    initialFontSize: Float,
    initialFontColor: Color,
    initialFontFamily: FontTypes
) {
    val fontOptions = FontTypes.entries.toList()
    var expanded by remember { mutableStateOf(false) }
    var selectedFont by remember { mutableStateOf(initialFontFamily) }
    val controller = rememberColorPickerController()
    Dialog(
        onDismissRequest = onDismissRequest,
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(10.dp))
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(Res.string.text_settings),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(20.dp))

            ComboInput(
                items = fontOptions,
                selected = selectedFont,
                onSelectedChange = {
                    selectedFont = it
                    onFontFamilyChange(it)
                },
                label = stringResource(Res.string.font_family)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(Res.string.font_size),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Column {
                Slider(
                    value = initialFontSize,
                    onValueChange = onFontSizeChange,
                    valueRange = 12f..36f,
                    steps = 24,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(text = ((initialFontSize * 100).roundToInt() / 100.0).toString())
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(Res.string.font_color),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            HsvColorPickerWithFeed(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                onColorChanged = onFontColorChange,
                color = initialFontColor,
                controller = controller
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComboInput(
    modifier: Modifier = Modifier,
    items: List<FontTypes>,
    selected: FontTypes,
    onSelectedChange: (FontTypes) -> Unit,
    label: String = "",
) {
    var expanded by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf(selected.value) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = {
            },
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            readOnly = true,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            item.value,
                            fontFamily = getFontFamily(item)
                        )
                    },
                    onClick = {
                        text = item.value
                        onSelectedChange(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ColorCircle(
    color: Color,
    borderColor: Color,
    onClick: () -> Unit,
    icon: (@Composable BoxScope.() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .shadow(15.dp, CircleShape)
            .clip(CircleShape)
            .background(color)
            .border(
                width = 3.dp,
                color = borderColor,
                shape = CircleShape
            )
            .clickable { onClick() }
    ) {
        icon?.invoke(this)
    }
}

@Composable
fun ColorPickerDialog(
    onColorChanged: (Color) -> Unit,
    onDismissRequest: () -> Unit,
    color: Color
) {
    val controller = rememberColorPickerController()
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        ),
    ) {
        Column(
            modifier = Modifier
                .size(300.dp, 400.dp)
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(10.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = stringResource(Res.string.select_color),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            HsvColorPickerWithFeed(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                onColorChanged = onColorChanged,
                color = color,
                controller = controller
            )
        }
    }
}

@Composable
fun HsvColorPickerWithFeed(
    modifier: Modifier = Modifier,
    onColorChanged: (Color) -> Unit,
    color: Color,
    controller: ColorPickerController
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        HsvColorPicker(
            modifier = modifier,
            initialColor = color,
            onColorChanged = { colorEnvelope ->
                onColorChanged(colorEnvelope.color)
            },
            drawDefaultWheelIndicator = true,
            controller = controller
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(Res.string.selected_color)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Preview da cor atual
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(color = color, shape = RoundedCornerShape(4.dp))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.onBackground,
                            shape = RoundedCornerShape(4.dp)
                        )
                )

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.onBackground,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .clickable { onColorChanged(Color.Black) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(Res.string.black_label),
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}