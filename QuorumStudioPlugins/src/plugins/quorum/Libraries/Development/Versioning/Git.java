/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plugins.quorum.Libraries.Development.Versioning;

import com.github.difflib.algorithm.DiffException;
import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.ReceiveCommand;
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
import quorum.Libraries.Development.Versioning.ReferenceUpdate;
import quorum.Libraries.Development.Versioning.ReferenceUpdate_;
import quorum.Libraries.Development.Versioning.Repository_;
import quorum.Libraries.Development.Versioning.StatusResult;
import quorum.Libraries.Development.Versioning.StatusResult_;
import quorum.Libraries.Development.Versioning.RemoteReferenceUpdate_;
import quorum.Libraries.Development.Versioning.RemoteReferenceUpdate;
import quorum.Libraries.Development.Versioning.VersionProgressMonitor_;
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
            
            Path path = Paths.get(repo). getFileName();
            
            String name = path.toString();
            String[] split = name.split("\\.");
            if(split == null || split.length == 0) {
                return result;
            }
            
            VersionProgressMonitor_ monitor = request.GetProgressMonitor();
            name = split[0];
            File file = new File(local + File.separatorChar + name);
            CloneCommand clone = org.eclipse.jgit.api.Git.cloneRepository();
            clone.setURI( repo );
            
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
    
    public AddResult_ Add(Repository_ quorumRepository,  AddRequest_ request) {
        AddResult_ result = new AddResult();
        try {
            
            quorum.Libraries.Development.Versioning.Repository repo = (quorum.Libraries.Development.Versioning.Repository) quorumRepository;
            org.eclipse.jgit.lib.Repository repository = repo.plugin_.getRepository();
            org.eclipse.jgit.api.Git git = new org.eclipse.jgit.api.Git(repository);
            AddCommand add = git.add();
            
            Array_ quorumFiles = request.GetFilesToAdd();
            String[] paths = ConvertQuorumFilesToPath(quorumFiles, repository);
            for(int i = 0; i < paths.length; i++) {
                add.addFilepattern(HandleFilePathOS(paths[i]));
            }
            
            DirCache call = add.call(); //do we need this information?
            return result;
        } catch (GitAPIException ex) {
            Logger.getLogger(Git.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    public CommitResult_ Commit(Repository_ quorumRepository,  CommitRequest_ request) {
        CommitResult_ result = new CommitResult();
        try {
            quorum.Libraries.Development.Versioning.Repository repo = (quorum.Libraries.Development.Versioning.Repository) quorumRepository;
            org.eclipse.jgit.lib.Repository repository = repo.plugin_.getRepository();
            org.eclipse.jgit.api.Git git = new org.eclipse.jgit.api.Git(repository);
            
            if(request.HasCredentials()) {
                CredentialsProvider provider = new UsernamePasswordCredentialsProvider(request.GetUsername(), request.GetPassword());
                CommitCommand commit = git.commit();
                commit.setCredentialsProvider(provider);
                commit.setCommitter(request.GetName(), request.GetEmail());
                commit.setMessage(request.GetMessage());
                RevCommit call = commit.call();
                String message = call.getFullMessage();
                result.SetMessage(message);
            }
        } catch (GitAPIException ex) {
            result.SetMessage(ex.getMessage());
        }
        return result;
    }
    
    public PushResult_ Push(Repository_ quorumRepository,  PushRequest_ request) {
        PushResult_ result = new PushResult();
        try {
            quorum.Libraries.Development.Versioning.Repository repo = (quorum.Libraries.Development.Versioning.Repository) quorumRepository;
            org.eclipse.jgit.lib.Repository repository = repo.plugin_.getRepository();
            org.eclipse.jgit.api.Git git = new org.eclipse.jgit.api.Git(repository);
            
            if(request.HasCredentials()) {
                CredentialsProvider provider = new UsernamePasswordCredentialsProvider(request.GetUsername(), request.GetPassword());
                PushCommand push = git.push();
                push.setCredentialsProvider(provider);
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
                                break;
                            case NOT_ATTEMPTED:
                                 quorumUpdate.SetStatus(2);
                                break;
                            case OK:
                                 quorumUpdate.SetStatus(3);
                                break;
                            case REJECTED_NODELETE:
                                 quorumUpdate.SetStatus(4);
                                break;
                            case REJECTED_NONFASTFORWARD:
                                 quorumUpdate.SetStatus(5);
                                break;
                            case REJECTED_OTHER_REASON:
                                 quorumUpdate.SetStatus(6);
                                break;
                            case REJECTED_REMOTE_CHANGED:
                                 quorumUpdate.SetStatus(7);
                                break;
                            case UP_TO_DATE:
                                 quorumUpdate.SetStatus(8);
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        } catch (GitAPIException ex) {
            result.SetMessage(ex.getMessage());
        }
        return result;
    }
    
    public PullResult_ Pull(Repository_ quorumRepository, PullRequest_ request) {
        PullResult_ result = new PullResult();
        Array_ referenceUpdates = result.Get_Libraries_Development_Versioning_PullResult__updates_();
        try {
            quorum.Libraries.Development.Versioning.Repository repo = (quorum.Libraries.Development.Versioning.Repository) quorumRepository;
            org.eclipse.jgit.lib.Repository repository = repo.plugin_.getRepository();
            org.eclipse.jgit.api.Git git = new org.eclipse.jgit.api.Git(repository);

            ObjectId oldHead = repository.resolve(HEAD);
            if(request.HasCredentials()) {
                CredentialsProvider provider = new UsernamePasswordCredentialsProvider(request.GetUsername(), request.GetPassword());
                PullCommand pull = git.pull();
                pull.setCredentialsProvider(provider);

                org.eclipse.jgit.api.PullResult call = pull.call();
                FetchResult fetchResult = call.getFetchResult();
                String messages = fetchResult.getMessages();
                boolean successful = call.isSuccessful();
                
                if(successful) {
                    ObjectId head = repository.resolve(HEAD);
                    List<DiffEntry> diffs = GetDifferenceBetweenIDs(git, repository, oldHead, head);
                    if(diffs != null) {
                        Iterator<DiffEntry> iterator = diffs.iterator();
                        while(iterator.hasNext()) {
                            DiffEntry next = iterator.next();
                            DiffEntry.ChangeType type = next.getChangeType();
                            String newPath = next.getNewPath();
                            
                            quorum.Libraries.Development.Versioning.DiffEntry entry = new quorum.Libraries.Development.Versioning.DiffEntry();
                            entry.Set_Libraries_Development_Versioning_DiffEntry__location_(newPath);
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
                }
                
                result.SetSuccess(successful);
                Collection<TrackingRefUpdate> updates = fetchResult.getTrackingRefUpdates();
                Iterator<TrackingRefUpdate> iterator = updates.iterator();
                while(iterator != null && iterator.hasNext()) {
                    TrackingRefUpdate next = iterator.next();
                    String local = next.getLocalName();
                    String remote = next.getRemoteName();
                    ReferenceUpdate_ update = new ReferenceUpdate();
                    referenceUpdates.Add(update);
                    update.SetLocalName(local);
                    update.SetRemoteName(remote);
                    
                    ReceiveCommand receive = next.asReceiveCommand();
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
                String fetchedFrom = call.getFetchedFrom();
                result.SetFetchedFrom(fetchedFrom);
                result.SetMessage(messages);
            }
        } catch (GitAPIException ex) {
            result.SetMessage(ex.getMessage());
        } catch (IncorrectObjectTypeException ex) {
            result.SetMessage(ex.getMessage());
        } catch (RevisionSyntaxException ex) {
            result.SetMessage(ex.getMessage());
        } catch (IOException ex) {
            result.SetMessage(ex.getMessage());
        }
        return result;
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
                    DiffRowGenerator generator = DiffRowGenerator.create()
                    .showInlineDiffs(true)
                    .mergeOriginalRevised(true)
                    .inlineDiffByWord(true)
                    .oldTag(f -> "~")      //introduce markdown style for strikethrough
                    .newTag(f -> "**")     //introduce markdown style for bold
                    .build();
            
                    List<String> resultList = Arrays.asList(StringDiff.ConvertToList(result)); 
                    List<String> readList = Arrays.asList(StringDiff.ConvertToList(read));
                    
                    //compute the differences for two test texts.
                    List<DiffRow> rows = generator.generateDiffRows(resultList, readList);
                    
                    quorum.Libraries.Development.Versioning.DiffResult resultForFile = new quorum.Libraries.Development.Versioning.DiffResult();
                    Iterator<DiffRow> iterator = rows.iterator();
                    
                    int line = 0;
                    while(iterator.hasNext()) {
                        DiffRow row = iterator.next();
                        DiffRow.Tag tag = row.getTag();
                        quorum.Libraries.Development.Versioning.DiffEdit res = new quorum.Libraries.Development.Versioning.DiffEdit();
                        if(null != tag) switch (tag) {
                            case CHANGE:
                                res.SetEditType(1);
                                break;
                            case DELETE:
                                res.SetEditType(2);
                                break;
                            case INSERT:
                                res.SetEditType(0);
                                break;
                            case EQUAL:
                                res.SetEditType(3);
                                break;
                            default:
                                break;
                        }
                        
                        res.startLine = line;
                        res.endLine = line;
                        line = line + 1;
                        resultForFile.GetEdits().Add(res);
                    }
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
    
    public StatusResult_ GetStatus(quorum.Libraries.System.File_ file) {
        StatusResult result = new StatusResult();
        //"/Users/stefika/Repositories/quorum-language/.git"
        Repository repo = GetRepositoryNative(file.GetAbsolutePath());
        DiffFormatter formatter = new DiffFormatter( System.out );
        formatter.setRepository(repo);
        
        List<DiffEntry> status = GetStatusEntries(repo, formatter);
        
        for( DiffEntry entry : status ) {
                String newPath = entry.getNewPath();
                quorum.Libraries.Development.Versioning.DiffEntry qEntry = new quorum.Libraries.Development.Versioning.DiffEntry();
                qEntry.SetLocation(newPath);
                
                FileHeader fileHeader = GetFileHeader(formatter, entry);
                if(fileHeader != null) {
                    EditList edits = fileHeader.toEditList();
                    Iterator<Edit> iterator = edits.iterator();
                    while(iterator.hasNext()) {
                        Edit edit = iterator.next();
                       // quorum.Libraries.Development.Versioning.DiffResult res = new quorum.Libraries.Development.Versioning.DiffResult();
                       // res.startLine = edit.getBeginA();
                       // res.endLine = edit.getEndA();
                       // qEntry.results.Add(res);
                    }
                }
            }
        
        
        return result;
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
