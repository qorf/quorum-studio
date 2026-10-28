/*
    Quorum Studio's Mac launcher. It is the app's CFBundleExecutable,
    Contents/MacOS/QuorumStudio, built from this file by InstallerBuilder.

    It runs Studio the way this command does, from Contents/app:

        ./Java/jdk/Contents/Home/bin/java -XstartOnFirstThread -jar QuorumStudio.jar

    but inside this process. It loads the bundled JDK's libjli.dylib and
    calls JLI_Launch, which is what the JDK's own java program does. So the
    process macOS registers as Quorum Studio is the one running Studio, and
    VoiceOver, the Dock, Quit and Force Quit see one app.

    On the Mac, libjli calls this program's main a second time, on a new
    thread, and keeps the first thread for the window system. (It finds
    main by name, so main must stay a visible symbol.) main does the same
    work both times; JLI_Launch knows it has already started.
*/
#include <CoreFoundation/CoreFoundation.h>
#include <dlfcn.h>
#include <limits.h>
#include <mach-o/dyld.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

// JLI_Launch, from the JDK's src/java.base/share/native/libjli/java.h
// (JDK 9 and later, including 21). jboolean is an unsigned char and jint
// an int, as in the JDK's jni.h and jni_md.h.
typedef int (*JLILaunch)(int argc, char **argv,
                         int jargc, const char **jargv,
                         int appclassc, const char **appclassv,
                         const char *fullversion, const char *dotversion,
                         const char *pname, const char *lname,
                         unsigned char javaargs, unsigned char cpwildcard,
                         unsigned char javaw, int ergo_class);

// Says what went wrong on stderr and in an alert, and returns 1 for main.
static int Fail(const char *message, const char *detail) {
    if (detail == NULL) {
        detail = "";
    }
    fprintf(stderr, "Quorum Studio: %s %s\n", message, detail);

    char text[PATH_MAX * 2];
    snprintf(text, sizeof(text), "%s\n\n%s\n\nReinstalling Quorum Studio should fix this.", message, detail);
    CFStringRef title = CFSTR("Quorum Studio could not start");
    CFStringRef body = CFStringCreateWithCString(NULL, text, kCFStringEncodingUTF8);
    CFOptionFlags answer;
    CFUserNotificationDisplayAlert(0, kCFUserNotificationStopAlertLevel, NULL, NULL, NULL,
                                   title, body, NULL, NULL, NULL, &answer);
    if (body != NULL) {
        CFRelease(body);
    }
    return 1;
}

int main(int argc, char **argv) {
    (void) argc;

    // Where this program is: .../QuorumStudio.app/Contents/MacOS/QuorumStudio,
    // with any symbolic links resolved.
    char program[PATH_MAX];
    char contents[PATH_MAX];
    uint32_t size = sizeof(program);
    if (_NSGetExecutablePath(program, &size) != 0 || realpath(program, contents) == NULL) {
        return Fail("It could not find where it is installed.", NULL);
    }

    // Remove MacOS/QuorumStudio, leaving .../QuorumStudio.app/Contents.
    for (int i = 0; i < 2; i++) {
        char *slash = strrchr(contents, '/');
        if (slash == NULL) {
            return Fail("It could not find where it is installed.", program);
        }
        *slash = '\0';
    }

    // Studio's files are in Contents/app, and it expects to start there.
    char app[PATH_MAX];
    char jli[PATH_MAX];
    snprintf(app, sizeof(app), "%s/app", contents);
    snprintf(jli, sizeof(jli), "%s/Java/jdk/Contents/Home/lib/libjli.dylib", app);
    if (chdir(app) != 0) {
        return Fail("Its files are missing:", app);
    }

    void *library = dlopen(jli, RTLD_NOW | RTLD_GLOBAL);
    if (library == NULL) {
        return Fail("Its Java is missing or can't be loaded:", dlerror());
    }
    JLILaunch launch = (JLILaunch) dlsym(library, "JLI_Launch");
    if (launch == NULL) {
        return Fail("Its Java has no JLI_Launch:", jli);
    }

    // Exactly what the old script passed. Anything macOS passes in argv
    // (such as an old -psn_ argument) is left out, as the script did.
    char *javaArguments[] = {
        argv[0],
        "-XstartOnFirstThread",
        "-jar",
        "QuorumStudio.jar",
        NULL
    };

    // The same values the JDK's java program passes, apart from the version
    // texts, which only java -version prints (jpackage passes "" too).
    return launch(4, javaArguments,
                  0, NULL,          // no built-in Java arguments
                  0, NULL,          // no built-in classpath
                  "", "",           // full and dot version
                  "java", "java",   // program and launcher names, for messages
                  0,                // javaargs: no built-in Java arguments
                  1,                // cpwildcard: as java does
                  0,                // javaw: Windows only
                  0);               // ergo_class: the default
}
