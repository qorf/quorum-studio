/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plugins.quorum.Libraries.Concurrency;

/**
 *
 * @author stefika
 */
public interface ProcessCoordinator {
    public void SendOutput(String value);
    public void SendInput(String value);
}
