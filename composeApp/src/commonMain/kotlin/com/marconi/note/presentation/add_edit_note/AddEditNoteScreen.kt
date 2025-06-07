package com.marconi.note.presentation.add_edit_note

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.navigation.NavController
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.marconi.note.domain.model.Note
import com.marconi.note.presentation.add_edit_note.components.TransparentHintTextField
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import noteapp.composeapp.generated.resources.Res
import noteapp.composeapp.generated.resources.font_color
import noteapp.composeapp.generated.resources.font_size
import noteapp.composeapp.generated.resources.select_color
import noteapp.composeapp.generated.resources.text_settings
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AddEditNoteScreen(
    navController: NavController,
    noteColor: Int,
) {
    val viewModel: AddEditNoteViewModel = koinViewModel()
    val titleState = viewModel.noteTitle.value
    val contentState = viewModel.noteContent.value
    val selectedCustomColor by viewModel.selectedCustomColor.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState(16f)
    val textColor by viewModel.textColor.collectAsState(Color.Black.toArgb())
    val isDialogVisible by viewModel.isColorDialogVisible.collectAsState(false)
    val isTextDialogVisible by viewModel.isFontDialogVisible.collectAsState(false)

    val noteBackgroundAnimatable = remember {
        Animatable(
            Color(if (noteColor != -1) noteColor else viewModel.noteColor.value)
        )
    }
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when(event) {
                is AddEditNoteViewModel.UiEvent.SaveNote -> {
                    navController.navigateUp()
                }
            }
        }
    }

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
            onFontSizeChange = {
                viewModel.onEvent(AddEditNoteEvent.ChangeFontSize(it))
            },
            onFontColorChange = {
                viewModel.onEvent(AddEditNoteEvent.ChangeTextColor(it.toArgb()))
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(noteBackgroundAnimatable.value, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Note.noteColors.forEach { color ->
                val colorInt = color.toArgb()
                ColorCircle(
                    color,
                    borderColor = if (viewModel.noteColor.value == colorInt) {
                        MaterialTheme.colorScheme.primary
                    } else Color.Transparent,
                    onClick = {
                        scope.launch {
                            noteBackgroundAnimatable.animateTo(
                                targetValue = Color(colorInt),
                                animationSpec = tween(
                                    durationMillis = 500
                                )
                            )
                        }
                        viewModel.onEvent(AddEditNoteEvent.ChangeColor(colorInt))
                    }
                )
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
        Spacer(modifier = Modifier.height(16.dp))
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
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        TransparentHintTextField(
            text = contentState.text,
            hint = contentState.hint?.let { stringResource(it) } ?: "",
            onValueChange = {
                viewModel.onEvent(AddEditNoteEvent.EnteredContent(it))
            },
            onFocusChange = {
                viewModel.onEvent(AddEditNoteEvent.ChangeContentFocus(it))
            },
            isHintVisible = contentState.isHintVisible,
            textStyle = TextStyle(
                fontSize = fontSize.sp,
                color = Color(textColor),
                fontWeight = FontWeight.Normal,

                ),
            modifier = Modifier.fillMaxHeight()
        )
    }
}

@Composable
fun TextSettingsDialog(
    onDismissRequest: () -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onFontColorChange: (Color) -> Unit,
    initialFontSize: Float,
    initialFontColor: Color
) {
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

            Text(
                text = stringResource(Res.string.font_size),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Slider(
                value = initialFontSize,
                onValueChange = onFontSizeChange,
                valueRange = 12f..36f,
                steps = 24,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(Res.string.font_color),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            HsvColorPicker(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                initialColor = initialFontColor,
                onColorChanged = { colorEnvelope ->
                    onFontColorChange(colorEnvelope.color)
                },
                drawDefaultWheelIndicator = true,
                controller = controller
            )
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

            HsvColorPicker(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                initialColor = color,
                onColorChanged = { colorEnvelope ->
                    onColorChanged(colorEnvelope.color)
                },
                drawDefaultWheelIndicator = true,
                controller = controller
            )
        }
    }
}