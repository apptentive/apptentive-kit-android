package apptentive.com.android.encryption

import android.os.Build
import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
class KeyResolverFactory {
    companion object {
        fun getKeyResolver(): KeyResolver =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) KeyResolver23()
            else KeyResolverNoOp()
    }
}
