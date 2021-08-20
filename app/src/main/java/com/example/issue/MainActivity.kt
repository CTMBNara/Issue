package com.example.issue

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.issue.ui.theme.IssueTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IssueTheme { Content() }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Content() {
    val (n, setN) = remember { mutableStateOf(1) }

    val interactionSource = remember { MutableInteractionSource() }

    val focusManager = LocalFocusManager.current
    val focusRequester = FocusRequester.Default
    var focused by remember { mutableStateOf(false) }

    val draggableState = rememberDraggableState {
        if (!focused) focusRequester.requestFocus()

        val newValue = n - (it / 5).toInt()
        setN((newValue % 60 + 60) % 60)
    }

    val transition = updateTransition(focused, label = "focusTransition")
    val color by transition.animateColor(label = "focusTransition_color")
    { if (it) Color.Red else Color.Black }
    val background by transition.animateColor(label = "focusTransition_background")
    { if (it) Color(0xFFEEEEEE) else Color.Transparent }

    val animationBoxWrapper = @Composable { content: @Composable () -> Unit ->
        AnimatedContent(
            targetState = n,
            modifier = Modifier.draggable(
                state = draggableState,
                orientation = Orientation.Vertical
            ),
            transitionSpec = {
                slideAndFadeVertically(
                    when {
                        targetState == 0 && initialState == 59 -> SlideAndFadeDirection.BottomToTop
                        targetState == 59 && initialState == 0 -> SlideAndFadeDirection.TopToBottom
                        targetState > initialState -> SlideAndFadeDirection.BottomToTop
                        else -> SlideAndFadeDirection.TopToBottom
                    }
                )
            },
            contentAlignment = Alignment.Center
        ) { content() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { focusManager.clearFocus() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            border = BorderStroke(width = 1.dp, color = Color.Red),
            backgroundColor = background
        ) {
            BasicTextField(
                value = n.toString(),
                onValueChange = { setN(it.toIntOrNull() ?: 0) },
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .onFocusChanged { focused = it.isFocused },
                textStyle = TextStyle.Default.copy(
                    color = color,
                    textAlign = TextAlign.Center
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions { focusManager.clearFocus() },
                decorationBox = { animationBoxWrapper(it) }
            )
        }
    }
}

private enum class SlideAndFadeDirection {
    TopToBottom, BottomToTop
}

@ExperimentalAnimationApi
private fun <N> AnimatedContentScope<N>.slideAndFadeVertically(
    direction: SlideAndFadeDirection
): ContentTransform = slideInVertically({
    if (direction == SlideAndFadeDirection.BottomToTop) it else -it
}) + fadeIn() with slideOutVertically({
    if (direction == SlideAndFadeDirection.BottomToTop) -it else it
}) + fadeOut() using SizeTransform(clip = false)
