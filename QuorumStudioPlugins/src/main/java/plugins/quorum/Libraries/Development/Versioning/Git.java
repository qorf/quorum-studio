/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plugins.quorum.Libraries.Development.Versioning;

import com.github.difflib.algorithm.DiffException;
import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Chunk;
import com.github.difflib.patch.DeltaType;
import com.github.difflib.patch.Patch;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Calendar;
import java.util.Date;
import java.text.DateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.EmptyCommitException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.UnmergedPathsException;
import org.eclipse.jgit.errors.LockFailedException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.BranchConfig;
import org.eclipse.jgit.lib.BranchTrackingStatus;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.TrackingRefUpdate;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import quorum.Libraries.Containers.Array_;
import quorum.Libraries.Development.Versioning.CommitRequest_;
import quorum.Libraries.Development.Versioning.CommitResult;
import quorum.Libraries.Development.Versioning.CommitResult_;
import quorum.Libraries.Development.Versioning.DiffRequest_;
import quorum.Libraries.Development.Versioning.DiffResult_;
import quorum.Libraries.Development.Versioning.PullRequest_;
import quorum.Libraries.Development.Versioning.PullResult;
import quorum.Libraries.Development.Versioning.PullResult_;
import quorum.Libraries.Development.Versioning.PushResult;
import quorum.Libraries.Development.Versioning.PushResult_;
import quorum.Libraries.Development.Versioning.PushRequest;
import quorum.Libraries.Development.Versioning.PushRequest_;
import quorum.Libraries.Development.Versioning.AddResult;
import quorum.Libraries.Development.Versioning.AddResult_;
import quorum.Libraries.Development.Versioning.AddRequest;
import quorum.Libraries.Development.Versioning.AddRequest_;
import quorum.Libraries.Development.Versioning.CloneResult;
import quorum.Libraries.Development.Versioning.CloneResult_;
import quorum.Libraries.Development.Versioning.CloneRequest;
import quorum.Libraries.Development.Versioning.CloneRequest_;
import quorum.Libraries.Development.Versioning.DeleteRequest_;
import quorum.Libraries.Development.Versioning.DeleteResult;
import quorum.Libraries.Development.Versioning.DeleteResult_;
import quorum.Libraries.Development.Versioning.ReferenceUpdate;
import quorum.Libraries.Development.Versioning.ReferenceUpdate_;
import quorum.Libraries.Development.Versioning.Repository_;
import quorum.Libraries.Development.Versioning.StatusResult;
import quorum.Libraries.Development.Versioning.StatusResult_;
import quorum.Libraries.Development.Versioning.RemoteReferenceUpdate_;
import quorum.Libraries.Development.Versioning.RemoteReferenceUpdate;
import quorum.Libraries.Development.Versioning.VersionProgressMonitor_;
import quorum.Libraries.Development.Versioning.BranchListResult;
import quorum.Libraries.Development.Versioning.BranchListResult_;
import quorum.Libraries.Development.Versioning.GitBranch;
import quorum.Libraries.Development.Versioning.SwitchBranchRequest_;
import quorum.Libraries.Development.Versioning.SwitchBranchResult;
import quorum.Libraries.Development.Versioning.SwitchBranchResult_;
import quorum.Libraries.Development.Versioning.UnstageRequest_;
import quorum.Libraries.Development.Versioning.UnstageResult;
import quorum.Libraries.Development.Versioning.UnstageResult_;
import quorum.Libraries.Development.Versioning.ResetResult;
import quorum.Libraries.Development.Versioning.ResetResult_;
import quorum.Libraries.Language.Object_;
import quorum.Libraries.System.File_;
/**
 *
 * @author stefika
 */
public class Git {
    public java.lang.Object me_ = null;
    private static final String HEAD = "HEAD^{tree}";
    
    private List<DiffEntry> GetDifferenceBetweenIDs(org.eclipse.jgit.api.Git git, org.eclipse.jgit.lib.Repository repository,
        ObjectId id1, ObjectId id2) throws IOException, GitAPIException {
        ObjectReader reader = repository.newObjectReader();
        CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
        oldTreeIter.reset(reader, id1);
        CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
        newTreeIter.reset(reader, id2);
        List<DiffEntry> diffs= git.diff()
            .setNewTree(newTreeIter)
            .setOldTree(oldTreeIter)
            .call();
        return diffs;
    }
    
    private String[] ConvertQuorumFilesToPath(Array_ quorumFiles, org.eclipse.jgit.lib.Repository repository) {
        String[] values = new String[quorumFiles.GetSize()];
        String repoPath = repository.getDirectory().getParentFile().getAbsolutePath();
        for(int i = 0; i < quorumFiles.GetSize(); i++) {
            File_ file = (File_) quorumFiles.Get(i);
            String filePath = file.GetAbsolutePath();
            if(filePath.startsWith(repoPath)) { //otherwise it's not in the folder and can't possibly be in this repo
                String newPath = filePath.substring(repoPath.length() + 1);
                values[i] = newPath;
            }
        }
        return values;
    }
    
    public CloneResult_ Clone(CloneRequest_ request) {
        CloneResult_ result = new CloneResult();
        try {
            
            String repo = request.GetRemoteRepositoryLocation();
            String local = request.GetSaveLocation();
            
            if(repo == null || repo.length() == 0) {
                result.SetIsSuccessful(false);
                result.SetMessage("The remote location you are attempting to clone is empty or does not exist.");
                return result;
            }
            
            if(local == null || local.length() == 0) {
                result.SetIsSuccessful(false);
                result.SetMessage("The location on disk you are attempting to clone to is empty or does not exist.");
                return result;
            }
            URI uri = URI.create(repo);
            String name = new File(uri.getPath()).getName();
            String[] split = name.split("\\.");
            if(split == null || split.length == 0) {
                return result;
            }
            
            VersionProgressMonitor_ monitor = request.GetProgressMonitor();
            name = split[0];
            File file = new File(local + File.separatorChar + name);
            if(file.exists()) {
                result.SetIsSuccessful(false);
                result.SetMessage("The location you are attempting to clone to is not empty. Please delete the folder and try again.");
                return result;
            }
                
            CloneCommand clone = org.eclipse.jgit.api.Git.cloneRepository();
            clone.setURI( uri.toString() );
            
            if(monitor != null) {
                QuorumProgressMonitor qpm = new QuorumProgressMonitor();
                qpm.setMonitor(monitor);
                clone.setProgressMonitor(qpm);
            }
            clone.setDirectory(file);
            clone.setCloneAllBranches( true );
            if(request.GetUsername() != null && request.GetPassword() != null) {
                CredentialsProvider provider = new UsernamePasswordCredentialsProvider(request.GetUsername(), request.GetPassword());
                clone.setCredentialsProvider(provider);
            }
            org.eclipse.jgit.api.Git git = clone.call();
            git.close();
            return result;
        } catch (GitAPIException ex) {
            String message = ex.getMessage();
            result.SetMessage(message);
        } 
        return result;
    }
    
    public DeleteResult_ Delete(Repository_ quorumRepository, DeleteRequest_ request) {
        DeleteResult_ result = new DeleteResult();
        try {
            
            quorum.Libraries.Development.Versioning.Repository repo = (quorum.Libraries.Development.Versioning.Repository) quorumRepository;
            org.eclipse.jgit.lib.Repository repository = repo.plugin_.getRepository();
            org.eclipse.jgit.api.Git git = new org.eclipse.jgit.api.Git(repository);
            RmCommand rm = git.rm();
            
            Array_ quorumFiles = request.GetFiles();
            String[] paths = ConvertQuorumFilesToPath(quorumFiles, repository);
            for(int i = 0; i < paths.length; i++) {
                rm.addFilepattern(HandleFilePathOS(paths[i]));
            }
            
            DirCache call = rm.call(); //do we need this information?
            return result;
        } catch (GitAPIException ex) {
            Logger.getLogger(Git.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    /*
     * Stages files: new, changed and deleted ones (git add, then git add -u
     * for the deletions). A folder stages everything in it; ignored files
     * are skipped, as git does.
     */
    public AddResult_ Add(Repository_ quorumRepository,  AddRequest_ request) {
        AddResult result = new AddResult();
        try {
            quorum.Libraries.Development.Versioning.Repository repo = (quorum.Libraries.Development.Versioning.Repository) quorumRepository;
            org.eclipse.jgit.lib.Repository repository = repo.plugin_.getRepository();
            org.eclipse.jgit.api.Git git = new org.eclipse.jgit.api.Git(repository);

            List<String> paths = GetRequestPaths(request.GetFilesToAdd(), request.GetPaths(), repository);
            if(paths.isEmpty()) {
                result.message = "None of the files are in this project's repository.";
                return result;
            }

            // New and changed files.
            AddCommand add = git.add();
            for(String path : paths) {
                add.addFilepattern(path);
            }
            add.call();

            // Changed and deleted files git already has: without setUpdate,
            // AddCommand leaves a deleted file out.
            AddCommand update = git.add();
            update.setUpdate(true);
            for(String path : paths) {
                update.addFilepattern(path);
            }
            update.call();

            result.successful = true;
        } catch (GitAPIException | NoWorkTreeException ex) {
            result.message = ex.getMessage();
        }
        return result;
    }

    /*
     * Unstages files (git reset -- <paths>): the staged copy goes back to
     * the last commit's, and the file on disk keeps its changes. With no
     * commit yet there's nothing to go back to, so the files come out of
     * the index instead (git rm --cached), and stay on disk.
     */
    public UnstageResult_ Unstage(Repository_ quorumRepository, UnstageRequest_ request) {
        UnstageResult result = new UnstageResult();
        DirCache cache = null;
        try {
            quorum.Libraries.Development.Versioning.Repository repo = (quorum.Libraries.Development.Versioning.Repository) quorumRepository;
            org.eclipse.jgit.lib.Repository repository = repo.plugin_.getRepository();
            org.eclipse.jgit.api.Git git = new org.eclipse.jgit.api.Git(repository);

            List<String> paths = GetRequestPaths(request.GetFiles(), request.GetPaths(), repository);
            if(paths.isEmpty()) {
                result.message = "None of the files are in this project's repository.";
                return result;
            }
            boolean everything = paths.contains(".");

            ObjectId head = repository.resolve(Constants.HEAD);
            if(head == null) {
                if(everything) {
                    cache = repository.lockDirCache();
                    cache.clear();
                    cache.write();
                    cache.commit();
                    cache = null;
                } else {
                    RmCommand rm = git.rm();
                    rm.setCached(true);
                    for(String path : paths) {
                        rm.addFilepattern(path);
                    }
                    rm.call();
                }
            } else {
                // With no paths, reset puts the whole index back (a mixed
                // reset to HEAD), which is unstaging everything.
                ResetCommand reset = git.reset();
                if(!everything) {
                    for(String path : paths) {
                        reset.addPath(path);
                    }
                }
                reset.call();
            }
            result.successful = true;
        } catch (GitAPIException | IOException | NoWorkTreeException ex) {
            result.message = ex.getMessage();
        } finally {
            if(cache != null) {
                cache.unlock();
            }
        }
        return result;
    }

    /*
     * Puts one file back as it was at the last commit (git checkout HEAD --
     * <path>): the staged copy and the file on disk both. The changes are
     * gone; Studio asks first (Tree M4).
     */
    public ResetResult_ Reset(Repository_ quorumRepository, String path) {
        ResetResult result = new ResetResult();
        try {
            quorum.Libraries.Development.Versioning.Repository repo = (quorum.Libraries.Development.Versioning.Repository) quorumRepository;
            org.eclipse.jgit.lib.Repository repository = repo.plugin_.getRepository();
            org.eclipse.jgit.api.Git git = new org.eclipse.jgit.api.Git(repository);

            if(path == null || path.length() == 0 || path.equals(".")) {
                result.message = "Only one file can be reset at a time.";
                return result;
            }
            if(repository.resolve(Constants.HEAD) == null) {
                result.message = "There's no commit to reset to yet.";
                return result;
            }

            CheckoutCommand checkout = git.checkout();
            checkout.setStartPoint(Constants.HEAD);
            checkout.addPath(path.replace('\\', '/'));
            checkout.call();
            result.successful = true;
        } catch (GitAPIException | IOException | NoWorkTreeException ex) {
            result.message = ex.getMessage();
        }
        return result;
    }

    /*
     * The paths a request names, relative to the repository's folder with /
     * between folders, as JGit wants them. Files outside the repository
     * are left out. The repository's own folder becomes ".", everything.
     */
    private List<String> GetRequestPaths(Array_ files, Array_ paths, org.eclipse.jgit.lib.Repository repository) {
        List<String> values = new ArrayList<>();
        String workTree = repository.getWorkTree().getAbsolutePath().replace('\\', '/');
        if(files != null) {
            for(int i = 0; i < files.GetSize(); i++) {
                File_ file = (File_) files.Get(i);
                String filePath = file.GetAbsolutePath().replace('\\', '/');
                if(filePath.equals(workTree)) {
                    values.add(".");
                } else if(filePath.startsWith(workTree + "/")) {
                    values.add(filePath.substring(workTree.length() + 1));
                }
            }
        }
        if(paths != null) {
            for(int i = 0; i < paths.GetSize(); i++) {
                Object value = paths.Get(i);
                String path = null;
                if(value instanceof quorum.Libraries.Language.Types.Text_) {
                    path = ((quorum.Libraries.Language.Types.Text_) value).GetValue();
                } else if(value instanceof String) {
                    path = (String) value;
                }
                if(path != null && path.length() > 0) {
                    values.add(path.replace('\\', '/'));
                }
            }
        }
        return values;
    }
    
    /*
     * Commits what's staged. A commit needs a name and email, not a password
     * (Commit C4). The failure kinds are CommitResult's constants:
     * COMMITTED 0, NOTHING_STAGED 1, NO_NAME 2, LOCKED 3, CONFLICTS 4,
     * OTHER 5.
     */
    public CommitResult_ Commit(Repository_ quorumRepository,  CommitRequest_ request) {
        CommitResult result = new CommitResult();
        try {
            quorum.Libraries.Development.Versioning.Repository repo = (quorum.Libraries.Development.Versioning.Repository) quorumRepository;
            org.eclipse.jgit.lib.Repository repository = repo.plugin_.getRepository();
            org.eclipse.jgit.api.Git git = new org.eclipse.jgit.api.Git(repository);

            String name = "";
            String email = "";
            if(request.HasCredentials()) {
                name = request.GetName();
                email = request.GetEmail();
            }
            if(name == null || name.trim().isEmpty() || email == null || email.trim().isEmpty()) {
                result.failureKind = 2;
                return result;
            }

            CommitCommand commit = git.commit();
            commit.setAuthor(name, email);
            commit.setCommitter(name, email);
            commit.setMessage(request.GetMessage());
            commit.setAllowEmpty(false);
            RevCommit call = commit.call();
            result.successful = true;
            result.failureKind = 0;
            result.shortId = call.getId().abbreviate(7).name();
        } catch (EmptyCommitException ex) {
            SetCommitFailure(result, 1, ex);
        } catch (UnmergedPathsException ex) {
            SetCommitFailure(result, 4, ex);
        } catch (ConcurrentRefUpdateException ex) {
            SetCommitFailure(result, 3, ex);
        } catch (GitAPIException | JGitInternalException ex) {
            if(IsLockProblem(ex)) {
                SetCommitFailure(result, 3, ex);
            } else {
                SetCommitFailure(result, 5, ex);
            }
        }
        return result;
    }

    private void SetCommitFailure(CommitResult result, int kind, Throwable error) {
        result.successful = false;
        result.failureKind = kind;
        String message = error.getMessage();
        result.message = message == null ? "" : message;
        result.details = GitErrors.Details(error);
    }

    /*
     * Whether the repository was locked, such as by another git program
     * (Commit C6). JGit says so with a LockFailedException somewhere in the
     * causes, or only in the message.
     */
    private boolean IsLockProblem(Throwable error) {
        Throwable next = error;
        while(next != null) {
            if(next instanceof LockFailedException) {
                return true;
            }
            String message = next.getMessage();
            if(message != null && message.toLowerCase().contains("lock")) {
                return true;
            }
            next = next.getCause();
        }
        return false;
    }
    
    /*
     * Pushes the current branch to its server branch, or, the first time,
     * to a server branch of the same name, which it then tracks (git's
     * push -u). The ref spec is given here, so git's push.default setting
     * doesn't matter. It tries without a username and password when none
     * are saved; GitErrors says when the server wanted them. Never forced.
     */
    public PushResult_ Push(Repository_ quorumRepository,  PushRequest_ request) {
        PushResult result = new PushResult();
        try {
            quorum.Libraries.Development.Versioning.Repository repo = (quorum.Libraries.Development.Versioning.Repository) quorumRepository;
            org.eclipse.jgit.lib.Repository repository = repo.plugin_.getRepository();
            org.eclipse.jgit.api.Git git = new org.eclipse.jgit.api.Git(repository);

            String branch = repository.getBranch();
            Ref head = repository.exactRef(Constants.HEAD);
            if(branch == null || head == null || !head.isSymbolic()) {
                result.errorKind = GitErrors.ERROR_OTHER;
                result.details = "Not on a branch.";
                return result;
            }

            StoredConfig config = repository.getConfig();
            BranchConfig branchConfig = new BranchConfig(config, branch);
            String remote = branchConfig.getRemote();
            Set<String> remotes = repository.getRemoteNames();
            if(remote == null && remotes.contains(Constants.DEFAULT_REMOTE_NAME)) {
                remote = Constants.DEFAULT_REMOTE_NAME;
            } else if(remote == null && !remotes.isEmpty()) {
                remote = remotes.iterator().next();
            }
            if(remote == null) {
                result.errorKind = GitErrors.ERROR_NO_REMOTE;
                return result;
            }

            String merge = branchConfig.getMerge();
            boolean firstPush = merge == null;
            String destination = merge;
            if(firstPush) {
                destination = Constants.R_HEADS + branch;
            }
            result.remoteName = remote;
            result.remoteBranch = Repository.shortenRefName(destination);
            result.firstPush = firstPush;

            PushCommand push = git.push();
            push.setRemote(remote);
            push.setRefSpecs(new RefSpec(Constants.R_HEADS + branch + ":" + destination));
            if(request.HasCredentials()) {
                CredentialsProvider provider = new UsernamePasswordCredentialsProvider(request.GetUsername(), request.GetPassword());
                push.setCredentialsProvider(provider);
            }
            VersionProgressMonitor_ monitor = request.GetProgressMonitor();
            if(monitor != null) {
                QuorumProgressMonitor qpm = new QuorumProgressMonitor();
                qpm.setMonitor(monitor);
                push.setProgressMonitor(qpm);
            }

            boolean accepted = false;
            Array_ updates = result.GetUpdates();
            Iterator<org.eclipse.jgit.transport.PushResult> iterator = push.call().iterator();
            while(iterator.hasNext()) {
                org.eclipse.jgit.transport.PushResult gitPushResult = iterator.next();
                Collection<RemoteRefUpdate> remoteUpdates = gitPushResult.getRemoteUpdates();
                Iterator<RemoteRefUpdate> remoteIterator = remoteUpdates.iterator();
                while(remoteIterator.hasNext()) {
                    RemoteRefUpdate next = remoteIterator.next();
                    RemoteReferenceUpdate_ quorumUpdate = new RemoteReferenceUpdate();
                    updates.Add(quorumUpdate);
                    quorumUpdate.SetName(next.getRemoteName());
                    quorumUpdate.SetMessage(next.getMessage());
                    RemoteRefUpdate.Status status = next.getStatus();
                    if(null != status) switch (status) {
                        case AWAITING_REPORT:
                            quorumUpdate.SetStatus(0);
                            break;
                        case NON_EXISTING:
                            quorumUpdate.SetStatus(1);
                            result.errorKind = GitErrors.ERROR_REFUSED;
                            break;
                        case NOT_ATTEMPTED:
                            quorumUpdate.SetStatus(2);
                            break;
                        case OK:
                            quorumUpdate.SetStatus(3);
                            accepted = true;
                            break;
                        case REJECTED_NODELETE:
                            quorumUpdate.SetStatus(4);
                            result.errorKind = GitErrors.ERROR_REFUSED;
                            break;
                        case REJECTED_NONFASTFORWARD:
                            quorumUpdate.SetStatus(5);
                            result.rejectedByNewerCommits = true;
                            break;
                        case REJECTED_OTHER_REASON:
                            quorumUpdate.SetStatus(6);
                            result.errorKind = GitErrors.ERROR_REFUSED;
                            if(next.getMessage() != null) {
                                result.serverMessage = next.getMessage();
                            }
                            break;
                        case REJECTED_REMOTE_CHANGED:
                            quorumUpdate.SetStatus(7);
                            result.rejectedByNewerCommits = true;
                            break;
                        case UP_TO_DATE:
                            quorumUpdate.SetStatus(8);
                            accepted = true;
                            result.upToDate = true;
                            break;
                        default:
                            break;
                    }
                }
            }

            result.successful = accepted && !result.rejectedByNewerCommits
                && result.errorKind == GitErrors.ERROR_NONE;
            if(result.successful && firstPush) {
                config.setString(ConfigConstants.CONFIG_BRANCH_SECTION, branch, ConfigConstants.CONFIG_KEY_REMOTE, remote);
                config.setString(ConfigConstants.CONFIG_BRANCH_SECTION, branch, ConfigConstants.CONFIG_KEY_MERGE, destination);
                config.save();
            }
            result.pushTime = FormatFetchTime(System.currentTimeMillis());
        } catch (GitAPIException ex) {
            result.SetMessage(ex.getMessage());
            result.errorKind = GitErrors.Classify(ex, request.HasCredentials());
            result.details = GitErrors.Details(ex);
        } catch (IOException ex) {
            result.SetMessage(ex.getMessage());
            result.errorKind = GitErrors.ERROR_OTHER;
            result.details = GitErrors.Details(ex);
        }
        return result;
    }
    
    private void TransferConflictsToTextArray(Array_ quorumFiles, Map<String, int[][]> conflicts) {
        Iterator<String> iterator = conflicts.keySet().iterator();
        while(iterator.hasNext()) {
            String next = iterator.next();
            quorum.Libraries.Language.Types.Text_ text = new quorum.Libraries.Language.Types.Text();
            text.SetValue(next);
            quorumFiles.Add(text);
        }
    }
    
    /* At most this many incoming commits are listed (PullResult.commits). */
    private static final int MAX_LISTED_COMMITS = 50;

    public PullResult_ Pull(Repository_ quorumRepository, PullRequest_ request) {
        PullResult result = new PullResult();
        Array_ referenceUpdates = result.Get_Libraries_Development_Versioning_PullResult__updates_();
        try {
            quorum.Libraries.Development.Versioning.Repository repo = (quorum.Libraries.Development.Versioning.Repository) quorumRepository;
            org.eclipse.jgit.lib.Repository repository = repo.plugin_.getRepository();
            org.eclipse.jgit.api.Git git = new org.eclipse.jgit.api.Git(repository);

            // No server at all, or a branch that isn't on the server (L6):
            // known without contacting the server.
            if(repository.getRemoteNames().isEmpty()) {
                result.errorKind = GitErrors.ERROR_NO_REMOTE;
                return result;
            }
            String branch = repository.getBranch();
            BranchConfig branchConfig = new BranchConfig(repository.getConfig(), branch);
            String tracking = branchConfig.getRemoteTrackingBranch();
            if(tracking == null) {
                result.noUpstream = true;
                return result;
            }
            result.upstreamBranch = Repository.shortenRefName(tracking);

            ObjectId oldHeadTree = repository.resolve(HEAD);
            ObjectId oldHeadCommit = repository.resolve(Constants.HEAD);

            PullCommand pull = git.pull();
            // Merge, as git does by default (Pull question 3).
            pull.setRebase(false);
            // A public repository needs no username and password to pull.
            if(request.HasCredentials()) {
                CredentialsProvider provider = new UsernamePasswordCredentialsProvider(request.GetUsername(), request.GetPassword());
                pull.setCredentialsProvider(provider);
            }
            VersionProgressMonitor_ monitor = request.GetProgressMonitor();
            if(monitor != null) {
                QuorumProgressMonitor qpm = new QuorumProgressMonitor();
                qpm.setMonitor(monitor);
                pull.setProgressMonitor(qpm);
            }

            org.eclipse.jgit.api.PullResult call = pull.call();
            FetchResult fetchResult = call.getFetchResult();
            if(fetchResult != null) {
                result.SetMessage(fetchResult.getMessages());
                AddReferenceUpdates(fetchResult, referenceUpdates);
            }
            result.SetFetchedFrom(call.getFetchedFrom());
            result.SetSuccess(call.isSuccessful());

            MergeResult mergeResult = call.getMergeResult();
            result.mergeStatus = ConvertMergeStatus(mergeResult);
            if(mergeResult != null) {
                // L5: files changed here that the merge would overwrite.
                if(mergeResult.getFailingPaths() != null) {
                    ConvertSetToQuorumArray(mergeResult.getFailingPaths().keySet(), result.failingPaths);
                }
                if(mergeResult.getCheckoutConflicts() != null) {
                    ConvertSetToQuorumArray(new java.util.HashSet<String>(mergeResult.getCheckoutConflicts()), result.failingPaths);
                }
                // L7: each file of conflicts, and how many it has.
                Map<String, int[][]> conflicts = mergeResult.getConflicts();
                if(conflicts != null && !conflicts.isEmpty()) {
                    result.hasMergeConflicts = true;
                    Iterator<String> paths = conflicts.keySet().iterator();
                    while(paths.hasNext()) {
                        String path = paths.next();
                        quorum.Libraries.Language.Types.Text text = new quorum.Libraries.Language.Types.Text();
                        text.SetValue(path);
                        result.conflicts.Add(text);
                        quorum.Libraries.Language.Types.Integer count = new quorum.Libraries.Language.Types.Integer();
                        int[][] regions = conflicts.get(path);
                        count.SetValue(regions == null ? 0 : regions.length);
                        result.conflictCounts.Add(count);
                    }
                }
            }

            // The files the pull changed, for reloading tabs and for L9.
            ObjectId newHeadTree = repository.resolve(HEAD);
            if(oldHeadTree != null && newHeadTree != null && !oldHeadTree.equals(newHeadTree)) {
                AddChangedFiles(git, repository, oldHeadTree, newHeadTree, result);
            }

            // The commits that came in: on the server branch, not in the
            // old head.
            AddIncomingCommits(repository, tracking, oldHeadCommit, result);
        } catch (CanceledException ex) {
            result.cancelled = true;
        } catch (CheckoutConflictException ex) {
            // L5, when JGit says it with an exception rather than a result.
            result.mergeStatus = 4;
            if(ex.getConflictingPaths() != null) {
                ConvertSetToQuorumArray(new java.util.HashSet<String>(ex.getConflictingPaths()), result.failingPaths);
            }
        } catch (GitAPIException ex) {
            result.errorKind = GitErrors.Classify(ex, request.HasCredentials());
            result.details = GitErrors.Details(ex);
            if(ex.getMessage() != null) {
                result.SetMessage(ex.getMessage());
            }
        } catch (IOException | RevisionSyntaxException ex) {
            result.errorKind = GitErrors.ERROR_OTHER;
            result.details = GitErrors.Details(ex);
            if(ex.getMessage() != null) {
                result.SetMessage(ex.getMessage());
            }
        }
        return result;
    }

    /*
     * The merge's outcome as one of GitConstants' PULL_MERGE_ numbers.
     */
    private int ConvertMergeStatus(MergeResult mergeResult) {
        if(mergeResult == null || mergeResult.getMergeStatus() == null) {
            return -1;
        }
        switch(mergeResult.getMergeStatus()) {
            case ALREADY_UP_TO_DATE:
                return 0;
            case FAST_FORWARD:
            case FAST_FORWARD_SQUASHED:
                return 1;
            case MERGED:
            case MERGED_SQUASHED:
                return 2;
            case CONFLICTING:
                return 3;
            case FAILED:
            case CHECKOUT_CONFLICT:
                return 4;
            default:
                return 5;
        }
    }

    /*
     * The fetch's updates to the server branches, as before.
     */
    private void AddReferenceUpdates(FetchResult fetchResult, Array_ referenceUpdates) {
        Collection<TrackingRefUpdate> updates = fetchResult.getTrackingRefUpdates();
        if(updates == null) {
            return;
        }
        Iterator<TrackingRefUpdate> iterator = updates.iterator();
        while(iterator.hasNext()) {
            TrackingRefUpdate next = iterator.next();
            ReferenceUpdate_ update = new ReferenceUpdate();
            referenceUpdates.Add(update);
            update.SetLocalName(next.getLocalName());
            update.SetRemoteName(next.getRemoteName());

            if(null != next.getResult()) switch (next.getResult()) {
                case FAST_FORWARD:
                    update.Set_Libraries_Development_Versioning_ReferenceUpdate__result_(0);
                    break;
                case FORCED:
                    update.Set_Libraries_Development_Versioning_ReferenceUpdate__result_(1);
                    break;
                case IO_FAILURE:
                    update.Set_Libraries_Development_Versioning_ReferenceUpdate__result_(2);
                    break;
                case LOCK_FAILURE:
                    update.Set_Libraries_Development_Versioning_ReferenceUpdate__result_(3);
                    break;
                case NEW:
                    update.Set_Libraries_Development_Versioning_ReferenceUpdate__result_(4);
                    break;
                case NOT_ATTEMPTED:
                    update.Set_Libraries_Development_Versioning_ReferenceUpdate__result_(5);
                    break;
                case NO_CHANGE:
                    update.Set_Libraries_Development_Versioning_ReferenceUpdate__result_(6);
                    break;
                case REJECTED:
                    update.Set_Libraries_Development_Versioning_ReferenceUpdate__result_(7);
                    break;
                case REJECTED_CURRENT_BRANCH:
                    update.Set_Libraries_Development_Versioning_ReferenceUpdate__result_(8);
                    break;
                case REJECTED_MISSING_OBJECT:
                    update.Set_Libraries_Development_Versioning_ReferenceUpdate__result_(9);
                    break;
                case RENAMED:
                    update.Set_Libraries_Development_Versioning_ReferenceUpdate__result_(10);
                    break;
                default:
                    break;
            }
        }
    }

    /*
     * The files that differ between the old head and the new one. A
     * deleted file is reported by its old path: JGit's new path for a
     * deletion is /dev/null.
     */
    private void AddChangedFiles(org.eclipse.jgit.api.Git git, org.eclipse.jgit.lib.Repository repository,
        ObjectId oldTree, ObjectId newTree, PullResult result) throws IOException, GitAPIException {
        List<DiffEntry> diffs = GetDifferenceBetweenIDs(git, repository, oldTree, newTree);
        if(diffs == null) {
            return;
        }
        Iterator<DiffEntry> iterator = diffs.iterator();
        while(iterator.hasNext()) {
            DiffEntry next = iterator.next();
            DiffEntry.ChangeType type = next.getChangeType();
            String path = next.getNewPath();
            if(type == DiffEntry.ChangeType.DELETE) {
                path = next.getOldPath();
            }

            quorum.Libraries.Development.Versioning.DiffEntry entry = new quorum.Libraries.Development.Versioning.DiffEntry();
            entry.Set_Libraries_Development_Versioning_DiffEntry__location_(path);
            result.Get_Libraries_Development_Versioning_PullResult__diff_().Add(entry);
            if(null != type) switch (type) {
                case ADD:
                    entry.entryType = 0;
                    break;
                case COPY:
                    entry.entryType = 2;
                    break;
                case DELETE:
                    entry.entryType = 3;
                    break;
                case MODIFY:
                    entry.entryType = 1;
                    break;
                case RENAME:
                    entry.entryType = 4;
                    break;
                default:
                    break;
            }
        }
    }

    /*
     * The commits on the server branch that the old head didn't have,
     * newest first. At most MAX_LISTED_COMMITS are listed; commitCount has
     * them all.
     */
    private void AddIncomingCommits(org.eclipse.jgit.lib.Repository repository, String tracking,
        ObjectId oldHead, PullResult result) throws IOException {
        ObjectId upstream = repository.resolve(tracking);
        if(upstream == null) {
            return;
        }
        try (RevWalk walk = new RevWalk(repository)) {
            walk.markStart(walk.parseCommit(upstream));
            if(oldHead != null) {
                walk.markUninteresting(walk.parseCommit(oldHead));
            }
            int count = 0;
            for(RevCommit commit : walk) {
                count = count + 1;
                if(count <= MAX_LISTED_COMMITS) {
                    result.commits.Add(MakeCommitSummary(commit));
                }
            }
            result.commitCount = count;
        }
    }

    /*
     * One commit for the Team tab: its id, the first line of its message,
     * its author's name and when it was made ("10:51" today, else a date).
     * The same helper step 10 describes; made here, since Pull is built
     * first.
     */
    private quorum.Libraries.Development.Versioning.CommitSummary MakeCommitSummary(RevCommit commit) {
        quorum.Libraries.Development.Versioning.CommitSummary summary = new quorum.Libraries.Development.Versioning.CommitSummary();
        summary.id = commit.getName();
        summary.shortId = commit.getName().substring(0, 7);
        summary.message = commit.getShortMessage();
        summary.author = commit.getAuthorIdent().getName();
        summary.time = FormatFetchTime(commit.getCommitTime() * 1000L);
        return summary;
    }
    
    /* This assumes the directory is valid. */
    public String GetPathRelativeToDirectory(String directory, String path) {
        String substring = path.substring(directory.length() + 1);
        substring = HandleFilePathOS(substring);
        
        return substring;
    }
    
    
    public String HandleFilePathOS(String path) {
        String result = path;
        String os = System.getProperty("os.name");
        if(os.contains("Windows")) {
            result = result.replace("\\", "/");
        }
        return result;
    }
    
    /*
     * Splits text into its lines, as git counts them: a line break ends a
     * line, so a final line break doesn't make an empty line after it, but
     * blank lines before it are kept.
     */
    private static List<String> SplitIntoLines(String value) {
        List<String> lines = new ArrayList<>(Arrays.asList(value.split("\\r?\\n", -1)));
        if (!lines.isEmpty() && lines.get(lines.size() - 1).isEmpty()) {
            lines.remove(lines.size() - 1);
        }
        return lines;
    }

    /*
     * Adds an edit for one line of the text being edited. The types are
     * GitConstants' EDIT_INSERT 0, EDIT_CHANGE 1 and EDIT_EQUAL 3.
     */
    private static quorum.Libraries.Development.Versioning.DiffEdit AddEdit(quorum.Libraries.Development.Versioning.DiffResult result, int line, int type) {
        quorum.Libraries.Development.Versioning.DiffEdit edit = new quorum.Libraries.Development.Versioning.DiffEdit();
        edit.startLine = line;
        edit.endLine = line;
        edit.SetEditType(type);
        result.GetEdits().Add(edit);
        return edit;
    }

    /*
     * Lines joined by line feeds, as the editor keeps them.
     */
    private static String JoinLines(List<String> lines) {
        return String.join("\n", lines);
    }

    /*
     * Adds unchanged lines from a line up to, but not including, another,
     * and returns where they end.
     */
    private static int AddEqualLines(quorum.Libraries.Development.Versioning.DiffResult result, int from, int to) {
        int line = from;
        while (line < to) {
            AddEdit(result, line, 3);
            result.Set_Libraries_Development_Versioning_DiffResult__linesEqual_(
                result.Get_Libraries_Development_Versioning_DiffResult__linesEqual_() + 1);
            line = line + 1;
        }
        return line;
    }

    /*
     * Marks the last line added as having lines deleted after it. Lines
     * deleted from the top of the file have no line above them to mark.
     */
    private static quorum.Libraries.Development.Versioning.DiffEdit MarkDeletionBefore(quorum.Libraries.Development.Versioning.DiffResult result) {
        int size = result.GetEdits().GetSize();
        if (size > 0) {
            quorum.Libraries.Development.Versioning.DiffEdit edit =
                (quorum.Libraries.Development.Versioning.DiffEdit) result.GetEdits().Get(size - 1);
            edit.SetHasDeletion(true);
            return edit;
        }
        return null;
    }

    public DiffResult_ RequestDiff(Repository_ quorumRepository, DiffRequest_ request) {
        try {
            quorum.Libraries.Development.Versioning.Repository repo = (quorum.Libraries.Development.Versioning.Repository) quorumRepository;
            org.eclipse.jgit.lib.Repository repository = repo.plugin_.getRepository();
            File directory = repository.getDirectory();
            String filePath = request.GetFile().GetAbsolutePath();
            String read = request.GetCurrentText();//request.GetFile().Read();
            if(read == null) {
                read = request.GetFile().Read();
            }
            String relativePath = GetPathRelativeToDirectory(directory.getParent(), filePath);
            ObjectId headCommit = repository.resolve(Constants.HEAD);
            try (RevWalk revWalk = new RevWalk(repository)) {
                RevCommit commit = revWalk.parseCommit(headCommit);
                // and using commit's tree find the path
                RevTree tree = commit.getTree();
                String result = null;
                // now try to find a specific file
                try (TreeWalk treeWalk = new TreeWalk(repository)) {
                    treeWalk.addTree(tree);
                    treeWalk.setRecursive(true);
                    treeWalk.setFilter(PathFilter.create(relativePath));
                    if (!treeWalk.next()) {
                        revWalk.dispose();
                        return null;
                    }

                    ObjectId objectId = treeWalk.getObjectId(0);
                    ObjectLoader loader = repository.open(objectId);

                    
                    result = new String(loader.getBytes());
                }

                revWalk.dispose();
                if(result != null) {
                    List<String> resultList = SplitIntoLines(result);
                    List<String> readList = SplitIntoLines(read);

                    // The changes as blocks, each with its place in the
                    // committed file and in the text being edited.
                    Patch<String> patch = DiffUtils.diff(resultList, readList);

                    // One edit for each line of the text being edited, in
                    // order, since the editor's gutter finds a line's mark by
                    // its place in this list. A deleted line has no line of
                    // its own; it marks the line above where it was.
                    quorum.Libraries.Development.Versioning.DiffResult resultForFile = new quorum.Libraries.Development.Versioning.DiffResult();
                    int line = 0;
                    for (AbstractDelta<String> delta : patch.getDeltas()) {
                        Chunk<String> source = delta.getSource();
                        Chunk<String> target = delta.getTarget();
                        line = AddEqualLines(resultForFile, line, target.getPosition());

                        DeltaType type = delta.getType();
                        if (type == DeltaType.INSERT || type == DeltaType.CHANGE) {
                            // The new lines of a changed block are all
                            // marked changed, however many lines it had
                            // before.
                            int editType = 0;
                            if (type == DeltaType.CHANGE) {
                                editType = 1;
                            }
                            // Each line of the block knows the block's place
                            // and the lines it had at the last commit, so the
                            // editor can show them and put them back.
                            int blockStart = line;
                            int blockEnd = line + target.size() - 1;
                            String oldText = type == DeltaType.CHANGE ? JoinLines(source.getLines()) : "";
                            int oldLineCount = type == DeltaType.CHANGE ? source.size() : 0;
                            for (int i = 0; i < target.size(); i++) {
                                quorum.Libraries.Development.Versioning.DiffEdit edit = AddEdit(resultForFile, line, editType);
                                edit.SetBlockStart(blockStart);
                                edit.SetBlockEnd(blockEnd);
                                edit.SetOldText(oldText);
                                edit.SetOldLineCount(oldLineCount);
                                line = line + 1;
                            }
                            if (type == DeltaType.CHANGE) {
                                resultForFile.Set_Libraries_Development_Versioning_DiffResult__linesChanged_(
                                    resultForFile.Get_Libraries_Development_Versioning_DiffResult__linesChanged_() + target.size());
                                if (source.size() > target.size()) {
                                    // The block lost lines too: its last
                                    // line shows the deletion.
                                    MarkDeletionBefore(resultForFile);
                                    resultForFile.Set_Libraries_Development_Versioning_DiffResult__linesDeleted_(
                                        resultForFile.Get_Libraries_Development_Versioning_DiffResult__linesDeleted_() + source.size() - target.size());
                                }
                            } else {
                                resultForFile.Set_Libraries_Development_Versioning_DiffResult__linesInserted_(
                                    resultForFile.Get_Libraries_Development_Versioning_DiffResult__linesInserted_() + target.size());
                            }
                        } else if (type == DeltaType.DELETE) {
                            quorum.Libraries.Development.Versioning.DiffEdit above = MarkDeletionBefore(resultForFile);
                            if (above != null) {
                                above.SetDeletedText(JoinLines(source.getLines()));
                                above.SetDeletedLineCount(source.size());
                            }
                            resultForFile.Set_Libraries_Development_Versioning_DiffResult__linesDeleted_(
                                resultForFile.Get_Libraries_Development_Versioning_DiffResult__linesDeleted_() + source.size());
                        }
                    }
                    AddEqualLines(resultForFile, line, readList.size());
                    return resultForFile;
                }
        }   catch (DiffException ex) {
                Logger.getLogger(Git.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } catch (IncorrectObjectTypeException ex) {
            Logger.getLogger(Git.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RevisionSyntaxException ex) {
            Logger.getLogger(Git.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Git.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    
    
    public Repository_ OpenRepository(File_ file) {
        try {
            FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
            repositoryBuilder.setMustExist( true );
            String value = file.GetAbsolutePath();
            repositoryBuilder.setGitDir(new java.io.File(value));
            Repository repository = repositoryBuilder.build();
            
            quorum.Libraries.Development.Versioning.Repository repo = new quorum.Libraries.Development.Versioning.Repository();
            repo.plugin_.setRepository(repository);
            
            return repo;
        } catch (IOException ex) {
        }
        return null;
    }
    
    public Repository_ FindRepository(File_ file) {
        try {
            FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
            repositoryBuilder.setMustExist( true );
            String value = file.GetAbsolutePath();
            FileRepositoryBuilder findGitDir = repositoryBuilder.findGitDir(new java.io.File(value));
            findGitDir.setMustExist( true );
            File gitDir = findGitDir.getGitDir();
            if(gitDir == null) {
                return null;
            }
            Repository repository = findGitDir.build();
            
            quorum.Libraries.Development.Versioning.Repository repo = new quorum.Libraries.Development.Versioning.Repository();
            repo.plugin_.setRepository(repository);
            
            return repo;
        } catch (IOException ex) {
        }
        return null;
    }
    
    private void ConvertSetToQuorumArray(Set<String> items, Array_ array) {
        if(items == null || array == null) {
            return;
        }
        Iterator<String> iterator = items.iterator();
        while(iterator.hasNext()) {
            String next = iterator.next();
            quorum.Libraries.Language.Types.Text text = new quorum.Libraries.Language.Types.Text();
            text.SetValue(next);
            array.Add(text);
        }
    }
    
    public StatusResult_ GetStatus(Repository_ quorumRepository) {
        StatusResult result = new StatusResult();
        try {
            quorum.Libraries.Development.Versioning.Repository repo = (quorum.Libraries.Development.Versioning.Repository) quorumRepository;
            org.eclipse.jgit.lib.Repository repository = repo.plugin_.getRepository();
            
            org.eclipse.jgit.api.Git git = new org.eclipse.jgit.api.Git(repository);
            StatusCommand status = git.status();
            Status call = status.call();
            ConvertSetToQuorumArray(call.getAdded(), result.added);
            ConvertSetToQuorumArray(call.getChanged(), result.changed);
            ConvertSetToQuorumArray(call.getConflicting(), result.conflicting);
            ConvertSetToQuorumArray(call.getIgnoredNotInIndex(), result.ignoredNotInIndex);
            ConvertSetToQuorumArray(call.getMissing(), result.missing);
            ConvertSetToQuorumArray(call.getModified(), result.modified);
            ConvertSetToQuorumArray(call.getRemoved(), result.removed);
            ConvertSetToQuorumArray(call.getUncommittedChanges(), result.uncommittedChanges);
            ConvertSetToQuorumArray(call.getUntracked(), result.untracked);
            ConvertSetToQuorumArray(call.getUntrackedFolders(), result.untrackedFolders);
            result.isClean = call.isClean();
            result.hasUncommittedChanges = call.hasUncommittedChanges();
            FillBranchInformation(repository, result);
            result.successful = true;
        } catch(GitAPIException | NoWorkTreeException | IOException ex) {
            result.successful = false;
            if(ex.getMessage() != null) {
                result.message = ex.getMessage();
            }
        }
        return result;
    }

    /*
     * Fills in the branch, the server branch it tracks, how far ahead and
     * behind it is, the work tree's folder and when the server was last
     * fetched from. Ahead and behind compare with the last fetch; this
     * never contacts the server.
     */
    private void FillBranchInformation(Repository repository, StatusResult result) throws IOException {
        File workTree = repository.getWorkTree();
        result.workTreePath = workTree.getAbsolutePath().replace('\\', '/');

        String branch = repository.getBranch();
        if(branch == null) {
            return;
        }
        result.branch = branch;

        Ref head = repository.exactRef(Constants.HEAD);
        result.detached = head != null && !head.isSymbolic();

        Set<String> remotes = repository.getRemoteNames();
        result.hasRemote = !remotes.isEmpty();
        String remote = repository.getConfig().getString(
            ConfigConstants.CONFIG_BRANCH_SECTION, branch, ConfigConstants.CONFIG_KEY_REMOTE);
        if(remote == null && remotes.contains(Constants.DEFAULT_REMOTE_NAME)) {
            remote = Constants.DEFAULT_REMOTE_NAME;
        } else if(remote == null && result.hasRemote) {
            remote = remotes.iterator().next();
        }
        if(remote != null) {
            result.remoteName = remote;
        }

        BranchTrackingStatus tracking = BranchTrackingStatus.of(repository, branch);
        if(tracking != null) {
            result.hasUpstream = true;
            result.upstreamBranch = Repository.shortenRefName(tracking.getRemoteTrackingBranch());
            result.aheadCount = tracking.getAheadCount();
            result.behindCount = tracking.getBehindCount();
        }

        FillCommitsToPush(repository, result, tracking, remote);

        File fetchHead = new File(repository.getDirectory(), "FETCH_HEAD");
        if(fetchHead.exists()) {
            result.lastFetchTime = FormatFetchTime(fetchHead.lastModified());
        }
    }

    /*
     * "10:42" (in the computer's own time format) for today, or the date
     * for an older fetch.
     */
    private String FormatFetchTime(long milliseconds) {
        Date when = new Date(milliseconds);
        Calendar then = Calendar.getInstance();
        then.setTime(when);
        Calendar now = Calendar.getInstance();
        boolean today = then.get(Calendar.YEAR) == now.get(Calendar.YEAR)
            && then.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR);
        if(today) {
            return DateFormat.getTimeInstance(DateFormat.SHORT).format(when);
        }
        return DateFormat.getDateInstance(DateFormat.MEDIUM).format(when);
    }

    // The most commits listed in the Team tab's push groups.
    private static final int MAXIMUM_COMMITS_LISTED = 100;

    /*
     * Lists the commits a push would send, newest first. With a server
     * branch, they're the commits not on it (the same as the ahead count).
     * Without one (a new branch), they're the commits not on any of the
     * server's branches. As of the last fetch; this never contacts the
     * server.
     */
    private void FillCommitsToPush(Repository repository, StatusResult result,
            BranchTrackingStatus tracking, String remote) throws IOException {
        ObjectId head = repository.resolve(Constants.HEAD);
        if(head == null || remote == null) {
            return;
        }
        try (RevWalk walk = new RevWalk(repository)) {
            walk.markStart(walk.parseCommit(head));
            if(tracking != null) {
                ObjectId upstream = repository.resolve(tracking.getRemoteTrackingBranch());
                if(upstream != null) {
                    walk.markUninteresting(walk.parseCommit(upstream));
                }
            } else {
                List<Ref> serverBranches = repository.getRefDatabase().getRefsByPrefix(Constants.R_REMOTES + remote + "/");
                for(Ref ref : serverBranches) {
                    ObjectId id = ref.getObjectId();
                    if(id != null) {
                        walk.markUninteresting(walk.parseCommit(id));
                    }
                }
            }
            int count = 0;
            for(RevCommit commit : walk) {
                if(count < MAXIMUM_COMMITS_LISTED) {
                    result.commitsToPush.Add(MakeCommitSummary(commit));
                }
                count++;
            }
            result.commitsToPushCount = count;
        }
    }
    
    /*
     * Lists the branches on this computer and the server's branches that
     * have no branch of the same name here (Switch Branch, B1). Each gets
     * its last commit's first line and how long ago it was made.
     */
    public BranchListResult_ ListBranches(Repository_ quorumRepository) {
        BranchListResult result = new BranchListResult();
        try {
            quorum.Libraries.Development.Versioning.Repository repo = (quorum.Libraries.Development.Versioning.Repository) quorumRepository;
            Repository repository = repo.plugin_.getRepository();
            org.eclipse.jgit.api.Git git = new org.eclipse.jgit.api.Git(repository);

            String fullBranch = repository.getFullBranch();
            String current = repository.getBranch();
            result.currentBranch = current == null ? "" : current;
            result.detached = fullBranch == null || !fullBranch.startsWith(Constants.R_HEADS);

            List<Ref> refs = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
            Set<String> localNames = new HashSet<>();
            for (Ref ref : refs) {
                if (ref.getName().startsWith(Constants.R_HEADS)) {
                    localNames.add(Repository.shortenRefName(ref.getName()));
                }
            }

            long now = System.currentTimeMillis();
            try (RevWalk walk = new RevWalk(repository)) {
                for (Ref ref : refs) {
                    String refName = ref.getName();
                    boolean isLocal = refName.startsWith(Constants.R_HEADS);
                    boolean isServer = refName.startsWith(Constants.R_REMOTES);
                    // Only branches; and not the server's HEAD pointer.
                    if ((!isLocal && !isServer) || refName.endsWith("/" + Constants.HEAD)) {
                        continue;
                    }
                    // "main", or "origin/main" for a server branch.
                    String shortName = Repository.shortenRefName(refName);
                    String localName = shortName;
                    if (isServer) {
                        int slash = shortName.indexOf('/');
                        localName = slash >= 0 ? shortName.substring(slash + 1) : shortName;
                        // Already here as a local branch: not repeated.
                        if (localNames.contains(localName)) {
                            continue;
                        }
                    }

                    GitBranch branch = new GitBranch();
                    branch.branchName = shortName;
                    branch.localName = localName;
                    branch.fullName = refName;
                    branch.isLocal = isLocal;
                    branch.isCurrent = isLocal && refName.equals(fullBranch);
                    if (isLocal) {
                        String tracking = new BranchConfig(repository.getConfig(), shortName).getTrackingBranch();
                        if (tracking != null) {
                            branch.upstream = Repository.shortenRefName(tracking);
                        }
                    }
                    ObjectId id = ref.getObjectId();
                    if (id != null) {
                        RevCommit commit = walk.parseCommit(id);
                        branch.lastMessage = commit.getShortMessage();
                        branch.lastAge = AgeWords(now, commit.getCommitTime() * 1000L);
                    }

                    if (isLocal) {
                        result.localBranches.Add(branch);
                    } else {
                        result.serverBranches.Add(branch);
                    }
                }
            }
            result.successful = true;
        } catch (GitAPIException | IOException ex) {
            result.successful = false;
            result.message = ex.getMessage() == null ? "" : ex.getMessage();
        }
        return result;
    }

    /*
     * "today", "yesterday", "3 days ago", "1 week ago", "2 months ago".
     */
    private static String AgeWords(long now, long then) {
        long days = (now - then) / 86400000L;
        if (days <= 0) {
            return "today";
        }
        if (days == 1) {
            return "yesterday";
        }
        if (days < 7) {
            return days + " days ago";
        }
        if (days < 30) {
            long weeks = days / 7;
            return weeks == 1 ? "1 week ago" : weeks + " weeks ago";
        }
        if (days < 365) {
            long months = days / 30;
            return months == 1 ? "1 month ago" : months + " months ago";
        }
        long years = days / 365;
        return years == 1 ? "1 year ago" : years + " years ago";
    }

    /*
     * Switches to a branch, or creates one (and switches to it, unless the
     * request says not to). Uncommitted changes come along, as in git.
     * When git refuses because a change would be overwritten, the files
     * are listed and nothing changes (B3).
     */
    public SwitchBranchResult_ SwitchBranch(Repository_ quorumRepository, SwitchBranchRequest_ request) {
        SwitchBranchResult result = new SwitchBranchResult();
        String branchName = request.GetBranchName();
        result.branchName = branchName;
        try {
            quorum.Libraries.Development.Versioning.Repository repo = (quorum.Libraries.Development.Versioning.Repository) quorumRepository;
            Repository repository = repo.plugin_.getRepository();
            org.eclipse.jgit.api.Git git = new org.eclipse.jgit.api.Git(repository);

            if (request.IsCreating() && !request.IsSwitching()) {
                // git branch <name> <start>
                CreateBranchCommand create = git.branchCreate();
                create.setName(branchName);
                create.setStartPoint(request.GetStartPoint());
                if (request.IsTracking()) {
                    create.setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK);
                }
                create.call();
            } else {
                // git switch <name>, or git switch -c <name> <start>
                CheckoutCommand checkout = git.checkout();
                checkout.setName(branchName);
                if (request.IsCreating()) {
                    checkout.setCreateBranch(true);
                    checkout.setStartPoint(request.GetStartPoint());
                    if (request.IsTracking()) {
                        checkout.setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK);
                    }
                }
                checkout.call();
                org.eclipse.jgit.api.CheckoutResult checkoutResult = checkout.getResult();
                if (checkoutResult != null
                        && checkoutResult.getStatus() == org.eclipse.jgit.api.CheckoutResult.Status.CONFLICTS) {
                    AddTextToQuorumArray(checkoutResult.getConflictList(), result.conflictingFiles);
                    result.successful = false;
                    return result;
                }
            }
            result.successful = true;
        } catch (CheckoutConflictException ex) {
            AddTextToQuorumArray(ex.getConflictingPaths(), result.conflictingFiles);
            result.successful = false;
            result.message = ex.getMessage() == null ? "" : ex.getMessage();
        } catch (GitAPIException ex) {
            result.successful = false;
            result.message = ex.getMessage() == null ? "" : ex.getMessage();
        }
        return result;
    }

    private void AddTextToQuorumArray(List<String> items, Array_ array) {
        if (items == null || array == null) {
            return;
        }
        for (String next : items) {
            quorum.Libraries.Language.Types.Text text = new quorum.Libraries.Language.Types.Text();
            text.SetValue(next);
            array.Add(text);
        }
    }

    /*
     * Whether git accepts a branch name (B5). The friendlier messages are
     * worked out in Studio; this is the last word.
     */
    public boolean IsValidBranchName(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        return Repository.isValidRefName(Constants.R_HEADS + value);
    }

    public FileHeader GetFileHeader(DiffFormatter formatter, DiffEntry entry) {
        try {
            FileHeader fileHeader = formatter.toFileHeader(entry);
            return fileHeader;
        } catch (IOException ex) {
        }
        return null;
    }
    
    public Repository GetRepositoryNative(String value) {
        try {
            FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
            repositoryBuilder.setMustExist( true );
            repositoryBuilder.setGitDir(new java.io.File(value));
            Repository repository = repositoryBuilder.build();
            return repository;
        } catch (IOException ex) {
        }
        return null;
    }
    
    public List<DiffEntry> GetStatusEntries(Repository repository, DiffFormatter formatter) {
        try {
            ObjectId lastCommitId = repository.resolve(Constants.HEAD);
            AbstractTreeIterator commitTreeIterator = prepareTreeParser( repository,  lastCommitId );
            FileTreeIterator workTreeIterator = new FileTreeIterator(repository);
            List<DiffEntry> diffEntries = formatter.scan( commitTreeIterator, workTreeIterator );
            
            return diffEntries;
        } catch (IOException ex) {
        }
        return null;
    }
    
    private AbstractTreeIterator prepareTreeParser(Repository repository, ObjectId objectId) throws IOException {
        // from the commit we can build the tree which allows us to construct the TreeParser
        //noinspection Duplicates
        try (RevWalk walk = new RevWalk(repository)) {
            RevCommit commit = walk.parseCommit(objectId);
            RevTree tree = walk.parseTree(commit.getTree().getId());

            CanonicalTreeParser oldTreeParser = new CanonicalTreeParser();
            try (ObjectReader oldReader = repository.newObjectReader()) {
                oldTreeParser.reset(oldReader, tree.getId());
            }

            walk.dispose();

            return oldTreeParser;
        }
    }
}
