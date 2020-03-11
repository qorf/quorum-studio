/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plugins.quorum.Libraries.Development.Versioning;

/**
 *
 * @author stefika
 */
public class Repository {
    public java.lang.Object me_ = null;
    private org.eclipse.jgit.lib.Repository repository = null;

    /**
     * @return the repository
     */
    public org.eclipse.jgit.lib.Repository getRepository() {
        return repository;
    }

    /**
     * @param repository the repository to set
     */
    public void setRepository(org.eclipse.jgit.lib.Repository repository) {
        this.repository = repository;
    }
}
