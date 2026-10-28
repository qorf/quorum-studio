# InstallerBuilder

InstallerBuilder makes Quorum Studio's installers:

| Installer | Made on | File |
|---|---|---|
| Mac, Apple Silicon | a Mac | `Installer/Build/<version>/Quorum-Studio-Mac-ARM.pkg` |
| Mac, Intel | a Mac | `Installer/Build/<version>/Quorum-Studio-Mac-INTEL.pkg` |
| Windows | Windows (signed or not), or a Mac (not signed) | `Installer/Build/<version>/Quorum-Studio-Windows.exe` |

Every installer for a version ends up in one folder, ready to upload.

It is a Quorum project. Open it in Quorum Studio and run it, and a window opens. In the window, choose the installers, choose whether they're for release (signed) or only for our own computers (**Local only**), and press **Build installers**. The window shows the progress and the end of the log. The whole log is in `InstallerBuilder.log` in this folder.

## Folders

| Folder | What's in it |
|---|---|
| `SourceCode` | The program. `InstallerBuilder.quorum` makes the installers; `Main.quorum` is the window. |
| `Installer/Mac` | What the Mac installers are made from: the Packages project (`QuorumStudio.pkgproj`), the app's `Info.plist`, the launcher script (`QuorumStudio`), Java's entitlements (`QuorumStudio.entitlements`), and the license the installer shows (`License.txt`, the same as Windows'). |
| `Installer/Windows` | What the Windows installer is made from: the NSIS scripts (`Run.nsi` for the `QuorumStudio.exe` launcher, `Installer.nsi` for the installer), the icon and the license. |
| `Installer/Build` | What's made. Git ignores it. `Build/<version>` has the finished installers; `Build/Work` has what's made on the way (the Mac app, the Windows launcher). |

Quorum Studio itself is built from the `QuorumStudio` folder beside this one. Its own `QuorumStudio/Installer` folder has the files these were copied from, and the old instructions; this README replaces them.

## The version

The version is this project's own version: **Project Version** in Quorum Studio's project properties for InstallerBuilder (saved in `Project/Project.qp`). Set it before each release. The window shows it at the top.

It's the only place the version is set. The build writes it into the Mac app (`Info.plist`), the Packages project, and the Windows installer's version information, and names the `Build/<version>` folder after it.

## 1. Build Quorum Studio (all computers)

The installers are made from Quorum Studio's `Run` and `Library` folders, so build Studio first, as before:

1. Open quorum-language in Quorum Studio.
2. Select `RunLibrary` as the main and run it.
3. Right-click quorum-language and copy the standard library to Quorum Studio.
4. Restart Quorum Studio.
5. Open the Quorum Studio project and Clean and Build.
6. Open the Quorum Debugger IntelliJ project and run the Gradle script.
7. Open the Quorum Studio IntelliJ project (`QuorumStudioPlugins`).
8. If in Quorum Studio 3.0 or below, uncomment the JDK 11 and JDK 8 lines, swapping them, run Gradle, then swap back to JDK 8. This ensures you have the right JDK in Gradle. In Quorum Studio 3.5 or above, this does not matter.
9. Run the Gradle script. To copy the JDK, run the `copyBuild` task, which must be done the first time.
10. Clean and Build Quorum Studio again. This has to be done a second time because the plugins need the dependencies.

The Mac build checks that the library copy is current: if Studio's `Run/jni` or `Library/Compiled/Run/jni` has no `Mac.so`, it stops and says to copy the library and build again.

## 2. Set up the computer (once)

### The JDKs

Each installer carries its own JDK. None of them are in git, so they're copied onto the building computer by hand.

| Installer | JDK folder |
|---|---|
| Mac, Apple Silicon | `QuorumStudio/Java/jdk` (arm64) |
| Mac, Intel | `QuorumStudio/Java-INTEL/jdk` (x86_64) |
| Windows, made on Windows | `QuorumStudio/Java/jdk` (the Windows JDK) |
| Windows, made on a Mac | `QuorumStudio/Java-WINDOWS/jdk`, or else the Windows JDK that AutoBuild downloads into `quorum-natives/download/jdk/windows` |

The JDKs come from the OpenJDK builds: `git clone https://YourUserName@bitbucket.org/stefika/openjdkbuild.git`, in the same folder as quorum-studio. On Windows, the Gradle `copyBuild` task in step 1 copies the JDK. The Mac build checks that the JDK is for the right kind of Mac before it starts.

### On a Mac

1. Install **Packages**: http://s.sudre.free.fr/Software/Packages/about.html. The build uses its `packagesbuild` command. You don't need to open the Packages project yourself; the build sets the version in it. **Don't add Studio's files to it by hand**, and close it in Packages before pulling: an old copy saved over a new one installs the old layout, and Studio won't start.
2. Install the **Xcode command line tools** (`xcode-select --install`), for `codesign`, `notarytool` and the others.
3. To make the Windows installer on a Mac too, install **NSIS**: `brew install makensis`.

### On Windows

1. Install **NSIS**, version 3.08 or later: https://nsis.sourceforge.io. Older versions can't sign the uninstaller.
2. For a signed installer, install the **Windows SDK**, for `signtool`. The build finds the newest one in `C:\Program Files (x86)\Windows Kits\10\bin`.

## 3. Make the installers

1. Set the version (above).
2. Open InstallerBuilder in Quorum Studio and run it.
3. Check the installers to make. A Mac shows both Mac installers and Windows; Windows shows Windows.
4. Check **Local only** for installers for our own computers, or leave it unchecked for a release (see Signing).
5. For a release, fill in the certificates, or leave them blank to have them found (see Signing).
6. Press **Build installers**.

On a Mac, the build runs as a second copy of the program, so the window keeps responding. On Windows it runs in the window's program, so the window doesn't respond until it's done. A Mac installer takes about five minutes, longer with notarizing; the Windows installer about a minute and a half.

The choices are saved in `Settings.json` here. A build can also run without the window, from a terminal in this folder, with those settings:

```
QUORUM_BUILD_INSTALLERS=yes java -jar Run/Default.jar
```

## Signing

An unsigned installer makes the computer warn that it's from an "unidentified developer" (Mac) or an "unknown publisher" (Windows). A release is signed so it doesn't.

### Local only

**Local only** makes installers for our own computers, with no certificates:

- **Mac:** everything is signed *ad hoc*, which is all Apple Silicon needs to run code, but not with a certificate, and it isn't notarized. Copied over a network or a USB drive, it installs with no warning. Downloaded in a browser, macOS asks first: right-click the installer and choose Open.
- **Windows:** nothing is signed. Windows warns about an unknown publisher.

A local build runs every signing step a release does, with ad hoc signatures, so it's also the way to test the build without certificates.

### Mac: what a release needs

1. **An Apple Developer Program membership** (paid). Only the team's **Account Holder** can create the certificates below.
2. **Two certificates**, each with its private key, in the building Mac's keychain:
   - **Developer ID Application: <Team> (<TEAMID>)**, which signs the app and everything in it;
   - **Developer ID Installer: <Team> (<TEAMID>)**, which signs the `.pkg`.

   Create them in Xcode → Settings → Accounts → Manage Certificates, or at https://developer.apple.com/account/resources/certificates. To use them on another Mac, export each from Keychain Access with its private key (a `.p12` file) and import it there.

   Check them with `security find-identity -v -p codesigning` (Application) and `security find-identity -v -p basic` (Installer). With one of each in the keychain, leave the fields blank and the build finds them. With more than one, type the full name, such as `Developer ID Application: Quorum Outreach and Research Foundation (ABCDE12345)`. The window lists the ones in the keychain.
3. **A notary profile**, so the build can send the installer to Apple. Make it once, with an App Store Connect API key:
   1. Sign in to https://appstoreconnect.apple.com as the Account Holder or an Admin.
   2. Users and Access → Integrations → App Store Connect API → Team Keys. The first time, request access; it's granted right away.
   3. Generate a key: any name (such as "Quorum notary"), access **Developer**.
   4. Note the **Issuer ID** (above the list) and the **Key ID**, and download `AuthKey_<KeyID>.p8`. **It can be downloaded only once.** Keep it somewhere private, never in a repository.
   5. On the building Mac, store it in the keychain:
      ```
      xcrun notarytool store-credentials QuorumNotary --key AuthKey_<KeyID>.p8 --key-id <KeyID> --issuer <IssuerID>
      ```
   6. Check it: `xcrun notarytool history --keychain-profile QuorumNotary` lists past submissions, with no error.

   The window's **Notary profile** is `QuorumNotary` unless you change it.

A release build signs the app from the inside out (the natives inside jars, the loose natives, the JDK, then the app), builds and signs the `.pkg`, sends it to Apple, and staples Apple's ticket to it. If Apple rejects it, the log gives the command that shows why:

```
xcrun notarytool log <id> --keychain-profile QuorumNotary
```

### Windows: what a release needs

A Windows release is signed on **Windows**, with `signtool`. A Windows installer made on a Mac can only be local only.

The code signing certificate is "Quorum Outreach and Research Foundation", from SSL.com. Its private key is on a **YubiKey**, a USB key, and never leaves it.

1. Plug the YubiKey into a USB port. If you haven't before, install the YubiKey minidriver first: https://www.ssl.com/how-to/using-your-code-signing-certificate/
2. You need the key's **PIN**. Finding it needs access to the certificate at SSL.com: https://www.ssl.com/how-to/access-your-yubikey-fips-pin-and-puk/
3. Leave **Code signing certificate name** blank to have signtool choose the certificate itself (`/a`). If it chooses the wrong one, or can't choose, type the certificate's subject name: `Quorum Outreach and Research Foundation`.
4. Press **Build installers**. Windows asks for the PIN when signtool first uses the key.

The build gives NSIS a signtool command (with SSL.com's timestamp server, so the signatures outlast the certificate), and NSIS signs three programs as it makes them:

1. the launcher, `QuorumStudio.exe`, before it's packed into the installer;
2. the uninstaller, `Uninstall.exe`, as it's made inside the installer. Before, nothing signed it, so uninstalling or updating showed "Publisher: Unknown";
3. the installer, `Quorum-Studio-Windows.exe`.

To check a signed installer, right-click it, choose Properties, and look for the **Digital Signatures** tab.

#### Signing by hand

An installer can also be signed after it's made, as before. This signs only that file, not the launcher or the uninstaller inside it.

- **With SSL Manager** (https://www.ssl.com/ssl-manager/): from the top menu, choose Code Signing → Sign & Codestamp Code Package. Choose the input file, and for the Code Signing Certificate, choose it from the store: "Quorum Outreach and Research Foundation" (you may need "more options" to find it). Leave the certificate password blank. Use SSL.com's timestamping service. Choose where the signed file goes, and give the YubiKey's PIN.
- **With signtool**, from the Windows SDK (the path depends on the SDK version):
  ```
  "C:\Program Files (x86)\Windows Kits\10\bin\10.0.22621.0\x86\signtool.exe" sign /fd sha256 /tr http://ts.ssl.com /td sha256 /a "C:\path\to\Quorum-Studio-Windows.exe"
  ```
  `/a` chooses the certificate itself. If that fails, name it with `/n "Quorum Outreach and Research Foundation"`.

### Testing signing without the real certificates

- **Mac:** use **Local only**. It runs every signing step with ad hoc signatures, and checks the app's signature at the end. Only the certificates and notarizing are skipped, and a Developer ID certificate can't be stood in for: Apple notarizes only what's signed with one.
- **Windows:** make a test certificate in PowerShell:
  ```
  New-SelfSignedCertificate -Type CodeSigningCert -Subject "CN=Quorum Test Signing" -CertStoreLocation Cert:\CurrentUser\My
  ```
  Then uncheck Local only, type `Quorum Test Signing` as the certificate name, and build. Everything is signed, but Windows still warns about an unknown publisher, since nobody vouches for a self-signed certificate. To have this computer trust it, export it from `certmgr.msc` (Personal → Certificates) without its private key, and import it into both Trusted Root Certification Authorities and Trusted Publishers. Remove it from those afterward.

## When something goes wrong

- **Studio says `./../app: No such file or directory`:** the installer put Studio's files in the old place, `Contents/MacOS`. The Packages project was probably replaced by an old copy (see the Mac setup above). Check that it installs only `../Build/Work/QuorumStudio.app`.
- **"There isn't exactly one Developer ID … certificate in the keychain":** install the certificate, or type its full name.
- **"The notary profile … doesn't work":** make the profile again (Mac, step 3).
- **"These natives need a newer macOS than …":** a native was built for a newer macOS than `LSMinimumSystemVersion` in `Installer/Mac/Info.plist`. Rebuild it for that macOS, or raise the setting.
- **"There's no Windows JDK":** see The JDKs.
- **NSIS errors on Windows about `!finalize` or `!uninstfinalize`:** NSIS is older than 3.08; install a newer one.
