/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plugins.quorum.Libraries.Development.Versioning;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import quorum.Libraries.Development.Versioning.DiffRequest;
import quorum.Libraries.Development.Versioning.DiffRequest_;
import quorum.Libraries.Development.Versioning.DiffResult;
import quorum.Libraries.Development.Versioning.DiffResult_;
import quorum.Libraries.Development.Versioning.Repository_;
import quorum.Libraries.Development.Versioning.StatusResult;
import quorum.Libraries.Development.Versioning.StatusResult_;
import quorum.Libraries.System.File_;

/**
 *
 * @author stefika
 */
public class Git {
    public java.lang.Object me_ = null;
    
    public DiffResult_ RequestDiff(Repository_ repository, DiffRequest_ request) {
        
        quorum.Libraries.Development.Versioning.Repository repo = (quorum.Libraries.Development.Versioning.Repository) repository;
        org.eclipse.jgit.lib.Repository plugin = repo.plugin_.getRepository();
        File directory = plugin.getDirectory();
        
        DiffFormatter formatter = new DiffFormatter( System.out );
        formatter.setRepository(plugin);
        List<DiffEntry> status = GetStatusEntries(plugin, formatter);
        for( DiffEntry entry : status ) {
                String newPath = directory.getParentFile().getAbsolutePath() + File.separatorChar + entry.getNewPath();
                
                //System.out.println(newPath);
                if(newPath != null && newPath.compareTo(request.GetFile().GetAbsolutePath()) == 0) {
                    quorum.Libraries.Development.Versioning.DiffResult resultForFile = new quorum.Libraries.Development.Versioning.DiffResult();
                    //qEntry.SetLocation(newPath);
                    
                    FileHeader fileHeader = GetFileHeader(formatter, entry);
                    if(fileHeader != null) {
                        EditList edits = fileHeader.toEditList();
                        Iterator<Edit> iterator = edits.iterator();
                        while(iterator.hasNext()) {
                            Edit edit = iterator.next();
                            quorum.Libraries.Development.Versioning.DiffEdit res = new quorum.Libraries.Development.Versioning.DiffEdit();
                            DiffEntry.ChangeType change = entry.getChangeType();
                            if(null != change) switch (change) {
                                case ADD:
                                    break;
                                case MODIFY:
                                    break;
                                case DELETE:
                                    break;
                                default:
                                    break;
                            }
                            
                            res.startLine = edit.getBeginA();
                            res.endLine = edit.getEndB();
                            resultForFile.GetEdits().Add(res);
                        }
                        return resultForFile;
                    }
                }
                
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
