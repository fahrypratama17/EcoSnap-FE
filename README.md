# EcoSnap Android

Kotlin / Jetpack Compose. Branch `12-feat/admin` adds the EcoSnap Mitra screens from the supplied reference: home, waste weight calculator, and deposit QR.

## Build without an emulator

Open this directory in Android Studio or use PowerShell:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat assembleDebug testDebugUnitTest --console=plain
```

`local.properties` must point to the local Android SDK (`sdk.dir=...`). It is ignored by Git. The project uses SDK 37, Gradle 9.5 and JDK 25. The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Try the Mitra flow

1. Finish or skip onboarding. On login, select **Mitra**, enter a valid email format and a nonempty password, then select **Masuk**. Authentication is still a local demo, as in the existing user flow; role selection is not server authorization.
2. Open **Kalkulator Sampah**. Enter `2,50` using the keypad: the estimate is **250 points**. The demo rate is **100 points/kg**; input supports two decimal places and weights from 0 through 9,999.99 kg. QR generation requires positive weight.
3. Select **Generate QR**. The code encodes a unique transaction ID, weight, points, expiry, and an explicit demo marker. It uses [ZXing Core](https://github.com/zxing/zxing) and can be decoded by a QR reader.
4. Switch tabs: the draft and current QR stay available. Changing the weight discards the old preview. **Buat Ulang QR** creates a new ID and five-minute deadline. Expired QR images are hidden. **Selesai** clears the local draft and returns home; it does not award points.
5. The profile icon offers logout. Notification and profile dialogs describe the current demo state.

Compose `@Preview` functions are included in each screen for Android Studio preview without running a device. All three screens scroll for small displays and larger text sizes.

## Backend integration remaining

The frontend does not submit deposits, authorize staff, confirm scans, or credit balances. Replace the local `DepositQr` creation with a backend-issued transaction/token and server expiry; use authenticated role information at login and server transaction status for confirmation. The current `ecosnap.deposit.demo` JSON is a preview contract, not a live claim token. There is no simulated success or automatic point credit.

Unit tests cover keypad limits, decimal arithmetic, point conversion, expiry, regeneration, and actual QR encode/decode with the displayed green/white colors. [Robolectric](https://robolectric.org/) Compose tests exercise navigation and saved-state restoration without a device, and write static screen captures to `app/build/reports/mitra-preview/`.
