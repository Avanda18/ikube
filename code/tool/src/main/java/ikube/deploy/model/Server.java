package ikube.deploy.model;

import ikube.deploy.action.IAction;
import net.schmizz.sshj.SSHClient;

import java.util.Collection;

/**
 * @author Michael Couck
 * @since 18-06-13
 * @version 01.00
 */
public class Server {

    private transient SSHClient sshExec;

	private String ip;
	private String username;
	private String password;

	private Collection<IAction> actions;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Collection<IAction> getActions() {
		return actions;
	}

	public void setActions(Collection<IAction> actions) {
		this.actions = actions;
	}

    public SSHClient getSshExec() {
        return sshExec;
    }

    public void setSshExec(SSHClient sshExec) {
        this.sshExec = sshExec;
    }
}