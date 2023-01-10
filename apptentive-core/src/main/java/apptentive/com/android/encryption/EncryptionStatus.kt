package apptentive.com.android.encryption

sealed class EncryptionStatus
object Encrypted : EncryptionStatus()
object NotEncrypted : EncryptionStatus()
object NoEncryptionStatus : EncryptionStatus() // No old encryption settings available, a fresh launch

fun Boolean.getEncryptionStatus(): EncryptionStatus = when (this) {
    true -> Encrypted
    false -> NotEncrypted
}
