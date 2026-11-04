package plugins.quorum.Libraries.Development.Versioning;

import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.errors.NoRemoteRepositoryException;

/*
 * Sorts what went wrong talking to a server into a few kinds, so Studio
 * can say it in plain words (GitErrorWording.quorum). The numbers are
 * GitConstants' ERROR_ constants.
 */
public class GitErrors {
    public static final int ERROR_NONE = 0;
    public static final int ERROR_NO_NETWORK = 1;
    public static final int ERROR_NOT_AUTHORIZED = 2;
    public static final int ERROR_NEEDS_CREDENTIALS = 3;
    public static final int ERROR_NO_REPOSITORY = 4;
    public static final int ERROR_NO_REMOTE = 5;
    public static final int ERROR_REFUSED = 6;
    public static final int ERROR_OTHER = 7;

    /*
     * Returns the kind of an exception from a fetch, pull or push.
     * hadCredentials says whether a username and password were given, which
     * tells "needs them" from "turned them down".
     */
    public static int Classify(Throwable error, boolean hadCredentials) {
        if(error == null) {
            return ERROR_NONE;
        }
        Throwable cause = error;
        while(cause != null) {
            if(cause instanceof UnknownHostException || cause instanceof SocketTimeoutException
                || cause instanceof ConnectException || cause instanceof NoRouteToHostException) {
                return ERROR_NO_NETWORK;
            }
            if(cause instanceof NoRemoteRepositoryException) {
                return ERROR_NO_REPOSITORY;
            }
            if(cause instanceof InvalidRemoteException) {
                return ERROR_NO_REMOTE;
            }
            cause = cause.getCause();
        }

        String text = Details(error).toLowerCase();
        if(text.contains("no credentialsprovider")) {
            return ERROR_NEEDS_CREDENTIALS;
        }
        if(text.contains("not authorized") || text.contains("authentication is required")
            || text.contains("auth fail") || text.contains("authentication failed")) {
            if(hadCredentials) {
                return ERROR_NOT_AUTHORIZED;
            }
            return ERROR_NEEDS_CREDENTIALS;
        }
        if(text.contains("unknownhost") || text.contains("timed out") || text.contains("connection refused")) {
            return ERROR_NO_NETWORK;
        }
        if(text.contains("repository not found") || text.contains("not found")) {
            return ERROR_NO_REPOSITORY;
        }
        if(text.contains("invalid remote")) {
            return ERROR_NO_REMOTE;
        }
        return ERROR_OTHER;
    }

    /*
     * JGit's own words, with each cause's message on its own line, for the
     * problem dialog's Show Details.
     */
    public static String Details(Throwable error) {
        StringBuilder builder = new StringBuilder();
        Throwable cause = error;
        while(cause != null) {
            String message = cause.getMessage();
            if(message != null && builder.indexOf(message) < 0) {
                if(builder.length() > 0) {
                    builder.append("\n");
                }
                builder.append(message);
            }
            cause = cause.getCause();
        }
        return builder.toString();
    }
}
