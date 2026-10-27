#!/bin/bash
# Builds a signed and notarized Quorum Studio installer for one kind of Mac.
#
# What it does, in order:
#   1. builds QuorumStudio.app in build/, from the same files the Packages
#      project used to put together itself;
#   2. signs it from the inside out: the natives inside jars, the loose
#      natives, any app inside it, the JDK, and then the app;
#   3. builds the installer with Packages and signs it;
#   4. sends it to Apple to be notarized, and staples the ticket to it.
# It stops at the first step that fails.
#
# Usage:
#   ./BuildSignedInstaller.sh --arch arm64      (Apple Silicon)
#   ./BuildSignedInstaller.sh --arch x86_64     (Intel)
#
# Options:
#   --arch arm64|x86_64        which Macs the installer is for (required)
#   --jdk PATH                 the JDK folder to bundle. By default
#                              ../../Java/jdk for arm64, and
#                              ../../Java-INTEL/jdk for x86_64
#   --app-identity NAME        the "Developer ID Application" certificate.
#                              By default, the only one in the keychain
#   --installer-identity NAME  the "Developer ID Installer" certificate.
#                              By default, the only one in the keychain
#   --notary-profile NAME      the notarytool keychain profile (default
#                              QuorumNotary; see "Making the notary API key"
#                              in the plans)
#   --no-notarize              stop after signing the installer
#   --adhoc                    sign with no certificate, to test the build
#                              on this Mac. The installer isn't signed or
#                              notarized, and other Macs won't open it
#
# The version comes from the Packages project (QuorumStudio.pkgproj), so it
# is set in one place. The installer ends up in build/, named
# QuorumStudio-<version>-AppleSilicon.pkg or QuorumStudio-<version>-Intel.pkg.

set -e
set -o pipefail

SCRIPT_FOLDER="$(cd "$(dirname "$0")" && pwd)"
STUDIO_FOLDER="$(cd "$SCRIPT_FOLDER/../.." && pwd)"
PROJECT="$SCRIPT_FOLDER/QuorumStudio.pkgproj"
ENTITLEMENTS="$SCRIPT_FOLDER/QuorumStudio.entitlements"
BUILD_FOLDER="$SCRIPT_FOLDER/build"
APP="$BUILD_FOLDER/QuorumStudio.app"
CONTENTS="$APP/Contents"
APP_FILES="$CONTENTS/app"

ARCH=""
JDK=""
APP_IDENTITY=""
INSTALLER_IDENTITY=""
NOTARY_PROFILE="QuorumNotary"
NOTARIZE=true
ADHOC=false

Fail() {
    echo "" >&2
    echo "Failed: $1" >&2
    exit 1
}

Step() {
    echo ""
    echo "== $1"
}

while [ $# -gt 0 ]; do
    case "$1" in
        --arch) ARCH="$2"; shift 2 ;;
        --jdk) JDK="$2"; shift 2 ;;
        --app-identity) APP_IDENTITY="$2"; shift 2 ;;
        --installer-identity) INSTALLER_IDENTITY="$2"; shift 2 ;;
        --notary-profile) NOTARY_PROFILE="$2"; shift 2 ;;
        --no-notarize) NOTARIZE=false; shift ;;
        --adhoc) ADHOC=true; shift ;;
        -h|--help) sed -n '2,38p' "$0" | sed 's/^# \{0,1\}//'; exit 0 ;;
        *) Fail "Unknown option $1. Use --help to see the options." ;;
    esac
done

case "$ARCH" in
    arm64) ARCH_NAME="AppleSilicon"; DEFAULT_JDK="$STUDIO_FOLDER/Java/jdk" ;;
    x86_64) ARCH_NAME="Intel"; DEFAULT_JDK="$STUDIO_FOLDER/Java-INTEL/jdk" ;;
    *) Fail "Give --arch arm64 (Apple Silicon) or --arch x86_64 (Intel)." ;;
esac

if [ -z "$JDK" ]; then
    JDK="$DEFAULT_JDK"
fi

# ---------------------------------------------------------------------------
# Checks before building anything
# ---------------------------------------------------------------------------

Step "Checking what the build needs"

command -v packagesbuild > /dev/null || Fail "packagesbuild isn't installed. Install Packages from http://s.sudre.free.fr/Software/Packages/about.html"

VERSION="$(/usr/libexec/PlistBuddy -c "Print :PACKAGES:0:PACKAGE_SETTINGS:VERSION" "$PROJECT")" || Fail "Could not read the version from $PROJECT"
echo "Version: $VERSION (from the Packages project)"

JAVA="$JDK/Contents/Home/bin/java"
[ -f "$JAVA" ] || Fail "No JDK at $JDK (looked for $JAVA)."
JAVA_ARCHES="$(lipo -archs "$JAVA")"
case " $JAVA_ARCHES " in
    *" $ARCH "*) echo "JDK: $JDK ($JAVA_ARCHES)" ;;
    *) Fail "The JDK at $JDK is for $JAVA_ARCHES, not $ARCH." ;;
esac

# Finds the only identity of one kind in the keychain, such as
# "Developer ID Application". Prints nothing if there are none or several.
FindIdentity() {
    local kind="$1"
    local policy="$2"
    local found
    found="$(security find-identity -v -p "$policy" | sed -n "s/.*\"\($kind: .*\)\"/\1/p" | sort -u)"
    if [ "$(printf '%s\n' "$found" | grep -c .)" = "1" ]; then
        echo "$found"
    fi
}

if [ "$ADHOC" = true ]; then
    echo "Signing ad hoc (--adhoc): for testing on this Mac only."
    SIGN_OPTIONS=(--force --options runtime --timestamp=none --sign -)
    NOTARIZE=false
else
    if [ -z "$APP_IDENTITY" ]; then
        APP_IDENTITY="$(FindIdentity "Developer ID Application" codesigning)"
    fi
    [ -n "$APP_IDENTITY" ] || Fail "No single \"Developer ID Application\" certificate in the keychain. Install it, or name one with --app-identity."
    if [ -z "$INSTALLER_IDENTITY" ]; then
        INSTALLER_IDENTITY="$(FindIdentity "Developer ID Installer" basic)"
    fi
    [ -n "$INSTALLER_IDENTITY" ] || Fail "No single \"Developer ID Installer\" certificate in the keychain. Install it, or name one with --installer-identity."
    echo "Signing the app as: $APP_IDENTITY"
    echo "Signing the installer as: $INSTALLER_IDENTITY"
    SIGN_OPTIONS=(--force --options runtime --timestamp --sign "$APP_IDENTITY")

    if [ "$NOTARIZE" = true ]; then
        xcrun notarytool history --keychain-profile "$NOTARY_PROFILE" > /dev/null 2>&1 || Fail "The notary profile \"$NOTARY_PROFILE\" doesn't work. Make it with: xcrun notarytool store-credentials $NOTARY_PROFILE --key AuthKey_<KeyID>.p8 --key-id <KeyID> --issuer <IssuerID>"
        echo "Notarizing with the profile: $NOTARY_PROFILE"
    fi
fi

# ---------------------------------------------------------------------------
# 1. Build QuorumStudio.app
# ---------------------------------------------------------------------------

Step "Building QuorumStudio.app"

rm -rf "$APP"
mkdir -p "$CONTENTS/MacOS" "$CONTENTS/Resources" "$APP_FILES"

# The same files Packages skipped, plus Mac speech natives the library no
# longer uses (Mac.so's AppleSpeak replaced them). Notarization rejects the
# Carbon ones (they predate the 10.9 SDK, and one is PowerPC).
EXCLUDES=(
    --exclude .DS_Store
    --exclude .git --exclude .gitignore
    --exclude .svn --exclude CVS --exclude .cvsignore --exclude .cvspass
    --exclude .pbdevelopment
    --exclude 'libCarbonSpeakJNI*.jnilib'
    --exclude CocoaSpeechServer.app
)

# Copies a file or folder from the Studio project into the app.
CopyIn() {
    local from="$STUDIO_FOLDER/$1"
    local to="$APP_FILES/$2"
    [ -e "$from" ] || Fail "Missing $from"
    mkdir -p "$(dirname "$to")"
    if [ -d "$from" ]; then
        rsync -a "${EXCLUDES[@]}" "$from/" "$to/"
    else
        cp -p "$from" "$to"
    fi
}

cp "$SCRIPT_FOLDER/Info.plist" "$CONTENTS/Info.plist"
/usr/libexec/PlistBuddy \
    -c "Set :CFBundleName Quorum Studio $VERSION" \
    -c "Set :CFBundleShortVersionString $VERSION" \
    -c "Set :CFBundleVersion $VERSION" \
    "$CONTENTS/Info.plist"
cp "$STUDIO_FOLDER/Resources/Graphics/Interface/Quorum.icns" "$CONTENTS/Resources/"
cp "$SCRIPT_FOLDER/QuorumStudio" "$CONTENTS/MacOS/QuorumStudio"
chmod 755 "$CONTENTS/MacOS/QuorumStudio"

CopyIn Configuration Configuration
CopyIn External External
CopyIn Library Library
CopyIn Resources Resources
CopyIn Templates Templates
CopyIn Run/jni jni
CopyIn Run/libraries libraries
CopyIn Run/QuorumStudio.jar QuorumStudio.jar
CopyIn Run/QuorumStandardLibrary.jar QuorumStandardLibrary.jar
CopyIn Run/QuorumStandardPlugins.jar QuorumStandardPlugins.jar
mkdir -p "$APP_FILES/Java"
ditto "$JDK" "$APP_FILES/Java/jdk"

# The Mac game natives are one universal Mac.so now. The old Mac files
# (libGameEngineCPlugins.so and libGameEngineCPluginsArm.so) can linger in
# build folders, which add files but never remove them; nothing loads them
# on the Mac. Android's files of the same name aren't Mac code, so only the
# Mac ones go.
find "$APP_FILES" -type f -name 'libGameEngineCPlugins*.so' -print |
while IFS= read -r old; do
    if file -b "$old" | grep -q 'Mach-O'; then
        rm -f "$old"
        echo "  left out the old ${old#$APP_FILES/}"
    fi
done

# Studio runs games with the natives in jni/, and copies those in
# Library/Compiled/Run/jni into each project.
for needed in "jni/Mac.so" "Library/Compiled/Run/jni/Mac.so"; do
    [ -f "$APP_FILES/$needed" ] || Fail "There's no $needed. Studio's library copy or its Run folder is from before Mac.so: copy the current library into QuorumStudio/Library and build Studio again."
done

# Some JDK files are read-only, which stops signing and clearing them.
# Extended attributes (such as Finder information) make codesign fail, and
# the installer drops them anyway.
chmod -R u+w "$APP"
xattr -cr "$APP"

echo "Built $APP ($(du -sh "$APP" | cut -f1))"

# ---------------------------------------------------------------------------
# Check every native runs on the macOS the app says it needs
# ---------------------------------------------------------------------------

# True if a file is a Mach-O program or library. Static libraries (.a)
# are archives, not code to sign or check, and are skipped.
IsMachO() {
    case "$1" in
        *.a) return 1 ;;
    esac
    file -b "$1" | grep -q 'Mach-O'
}

# The newest macOS a Mach-O file needs, over all its architectures. Prints
# nothing for files too old to say (built before LC_BUILD_VERSION).
NeedsMacOS() {
    vtool -show-build "$1" 2>/dev/null | awk '$1 == "minos" { print $2 }' | sort -V | tail -1
}

# True if version $1 is newer than version $2.
IsNewer() {
    [ "$1" != "$2" ] && [ "$(printf '%s\n%s\n' "$1" "$2" | sort -V | tail -1)" = "$1" ]
}

Step "Checking each native's minimum macOS"
MIN_MACOS="$(/usr/libexec/PlistBuddy -c "Print :LSMinimumSystemVersion" "$CONTENTS/Info.plist")"
echo "The app says it needs macOS $MIN_MACOS (LSMinimumSystemVersion in Info.plist)."
TOO_NEW="$(find "$APP_FILES" -path "$APP_FILES/Java" -prune -o -type f \
        \( -name '*.dylib' -o -name '*.jnilib' -o -name '*.so' -o -perm +111 \) -print |
    while IFS= read -r candidate; do
        if IsMachO "$candidate"; then
            needs="$(NeedsMacOS "$candidate")"
            if [ -n "$needs" ] && IsNewer "$needs" "$MIN_MACOS"; then
                echo "  ${candidate#$APP_FILES/} needs macOS $needs"
            fi
        fi
    done)"
if [ -n "$TOO_NEW" ]; then
    echo "$TOO_NEW"
    Fail "These natives need a newer macOS than $MIN_MACOS. Rebuild them for $MIN_MACOS, or raise LSMinimumSystemVersion in Installer/Mac/Info.plist."
fi
echo "Every native runs on macOS $MIN_MACOS."

# ---------------------------------------------------------------------------
# 2. Sign, from the inside out
# ---------------------------------------------------------------------------

# Signs one Mach-O file. Programs get Java's entitlements; libraries don't
# take entitlements.
SignMachO() {
    if file -b "$1" | grep -q 'executable'; then
        codesign "${SIGN_OPTIONS[@]}" --entitlements "$ENTITLEMENTS" "$1"
    else
        codesign "${SIGN_OPTIONS[@]}" "$1"
    fi
}

# Signs every Mach-O file in a folder that could be one: libraries by name,
# and anything marked executable. Leaves out the folders given after it.
SignMachOIn() {
    local folder="$1"
    shift
    local prune=()
    local skip
    for skip in "$@"; do
        prune+=(-path "$skip" -prune -o)
    done
    find "$folder" "${prune[@]}" -type f \
        \( -name '*.dylib' -o -name '*.jnilib' -o -name '*.so' -o -perm +111 \) -print |
    while IFS= read -r candidate; do
        if IsMachO "$candidate"; then
            SignMachO "$candidate" 2> /dev/null || SignMachO "$candidate" || Fail "Could not sign $candidate"
            echo "  signed ${candidate#$APP/}"
        fi
    done
}

# Notarization looks inside jars, so the natives in them (LWJGL's,
# jSerialComm's) are unpacked, signed and put back, at the same paths.
SignJarNatives() {
    local jar="$1"
    local entries
    entries="$(unzip -Z1 "$jar" | grep -E '\.(dylib|jnilib|so)$' || true)"
    [ -n "$entries" ] || return 0

    local work
    work="$(mktemp -d)"
    local signed=()
    local entry
    while IFS= read -r entry; do
        unzip -q -o "$jar" "$entry" -d "$work"
        if IsMachO "$work/$entry"; then
            SignMachO "$work/$entry" 2> /dev/null || SignMachO "$work/$entry" || Fail "Could not sign $entry in $jar"
            signed+=("$entry")
        fi
    done <<< "$entries"

    if [ ${#signed[@]} -gt 0 ]; then
        if unzip -Z1 "$jar" | grep -qE '^META-INF/[^/]+\.(SF|RSA|DSA|EC)$'; then
            rm -rf "$work"
            Fail "$jar is a signed jar. Changing its natives would break its Java signature."
        fi
        (cd "$work" && zip -q -X "$jar" "${signed[@]}")
        echo "  signed ${#signed[@]} in ${jar#$APP/}"
    fi
    rm -rf "$work"
}

Step "Signing the natives inside jars"
find "$APP_FILES" -name '*.jar' -type f -not -path "$APP_FILES/Java/*" -print |
while IFS= read -r jar; do
    SignJarNatives "$jar"
done

Step "Signing apps inside Studio"
find "$APP_FILES" -name '*.app' -type d -not -path "$APP_FILES/Java/*" -prune -print |
while IFS= read -r inner; do
    SignMachOIn "$inner"
    codesign "${SIGN_OPTIONS[@]}" --entitlements "$ENTITLEMENTS" "$inner"
    echo "  signed ${inner#$APP/}"
done

Step "Signing the loose natives"
INNER_APPS=()
while IFS= read -r inner; do
    INNER_APPS+=("$inner")
done < <(find "$APP_FILES" -name '*.app' -type d -prune -print)
SignMachOIn "$APP_FILES" "$APP_FILES/Java" ${INNER_APPS[@]+"${INNER_APPS[@]}"}

Step "Signing the JDK"
SignMachOIn "$APP_FILES/Java/jdk" | tail -3
codesign "${SIGN_OPTIONS[@]}" "$APP_FILES/Java/jdk"
echo "  signed the JDK bundle"

Step "Signing QuorumStudio.app"
codesign "${SIGN_OPTIONS[@]}" --entitlements "$ENTITLEMENTS" "$APP"
codesign --verify --deep --strict --verbose=2 "$APP" || Fail "The app's signature doesn't check out."
if [ "$ADHOC" = false ]; then
    # Says "Unnotarized Developer ID" until Apple notarizes the installer.
    spctl -a -vv -t exec "$APP" 2>&1 || true
fi

# ---------------------------------------------------------------------------
# 3. Build and sign the installer
# ---------------------------------------------------------------------------

Step "Building the installer with Packages"
rm -f "$BUILD_FOLDER"/*.pkg
if [ "$ADHOC" = true ]; then
    packagesbuild "$PROJECT"
else
    packagesbuild --identity "$INSTALLER_IDENTITY" "$PROJECT"
fi

BUILT="$(ls -t "$BUILD_FOLDER"/*.pkg 2>/dev/null | head -1)"
[ -n "$BUILT" ] || Fail "Packages made no installer in $BUILD_FOLDER."
PKG="$BUILD_FOLDER/QuorumStudio-$VERSION-$ARCH_NAME.pkg"
mv "$BUILT" "$PKG"
echo "Installer: $PKG"

if [ "$ADHOC" = false ]; then
    pkgutil --check-signature "$PKG" || Fail "The installer isn't signed."
fi

# ---------------------------------------------------------------------------
# 4. Notarize and staple
# ---------------------------------------------------------------------------

if [ "$NOTARIZE" = true ]; then
    Step "Notarizing (this usually takes a few minutes)"
    RESULT="$(xcrun notarytool submit "$PKG" --keychain-profile "$NOTARY_PROFILE" --wait 2>&1)" || true
    echo "$RESULT"
    if ! echo "$RESULT" | grep -q 'status: Accepted'; then
        SUBMISSION="$(echo "$RESULT" | sed -n 's/^ *id: //p' | head -1)"
        Fail "Apple didn't accept it. See why with: xcrun notarytool log $SUBMISSION --keychain-profile $NOTARY_PROFILE"
    fi

    Step "Stapling the ticket to the installer"
    xcrun stapler staple "$PKG"
    spctl -a -vv -t install "$PKG"
fi

Step "Done"
echo "$PKG"
