package YOUR.PACKAGE.core.ui.base.sheet

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * Template — copy to `:core-ui` …/base/sheet/
 * Replace YOUR.PACKAGE with applicationId root.
 */
open class ParentSheetDismissal : BottomSheetDialogFragment() {

    var dismissCallback: (() -> Unit)? = null

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismissCallback?.invoke()
    }
}

fun AppCompatActivity.safeShow(sheet: BottomSheetDialogFragment, tag: String) {
    if (!supportFragmentManager.isStateSaved) {
        sheet.show(supportFragmentManager, tag)
    }
}

fun Fragment.safeShow(sheet: BottomSheetDialogFragment, tag: String) {
    if (isAdded && !childFragmentManager.isStateSaved) {
        sheet.show(childFragmentManager, tag)
    }
}

fun BottomSheetDialogFragment.safeDismiss() {
    if (isAdded) {
        dismissAllowingStateLoss()
    }
}
