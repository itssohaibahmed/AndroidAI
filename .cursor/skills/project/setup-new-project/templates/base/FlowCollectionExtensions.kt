package YOUR.PACKAGE.core.ui.extensions

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Template — copy to `:core-ui` …/extensions/
 * Replace YOUR.PACKAGE with applicationId root.
 *
 * State → collectWhenStarted; Effects → collectWhenCreated (typical MVI).
 */

inline fun Fragment.repeatWhen(
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline block: suspend () -> Unit,
) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.lifecycle.repeatOnLifecycle(lifecycleState) {
            block()
        }
    }
}

inline fun <T> Fragment.collectWhenCreated(
    flow: Flow<T>,
    crossinline block: suspend (T) -> Unit,
) {
    repeatWhen(lifecycleState = Lifecycle.State.CREATED) { flow.collect { block(it) } }
}

inline fun <T> Fragment.collectWhenStarted(
    flow: Flow<T>,
    crossinline block: suspend (T) -> Unit,
) {
    repeatWhen(lifecycleState = Lifecycle.State.STARTED) { flow.collect { block(it) } }
}

inline fun AppCompatActivity.repeatWhen(
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline block: suspend () -> Unit,
) {
    lifecycleScope.launch {
        lifecycle.repeatOnLifecycle(lifecycleState) {
            block()
        }
    }
}

inline fun <T> AppCompatActivity.collectWhenCreated(
    flow: Flow<T>,
    crossinline block: suspend (T) -> Unit,
) {
    repeatWhen(lifecycleState = Lifecycle.State.CREATED) { flow.collect { block(it) } }
}

inline fun <T> AppCompatActivity.collectWhenStarted(
    flow: Flow<T>,
    crossinline block: suspend (T) -> Unit,
) {
    repeatWhen(lifecycleState = Lifecycle.State.STARTED) { flow.collect { block(it) } }
}