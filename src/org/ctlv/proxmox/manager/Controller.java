package org.ctlv.proxmox.manager;

import java.io.IOException;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.ctlv.proxmox.api.Constants;
import org.ctlv.proxmox.api.ProxmoxAPI;
import org.ctlv.proxmox.api.data.LXC;
import org.json.JSONException;

public class Controller {

	ProxmoxAPI api;
	public Controller(ProxmoxAPI api){
		this.api = api;
	}
	
	// migrer un conteneur du serveur "srcServer" vers le serveur "dstServer"
	public void migrateFromTo(String srcServer, String dstServer) throws LoginException, JSONException, IOException  {
		List<LXC> cts;
		cts = api.getCTs(srcServer);
		int indice = 0;
		String firstCTID = null;
		boolean CTFound = false;
		while(indice<cts.size() && CTFound == false) {
			if(cts.get(indice).getName().contains(Constants.CT_BASE_NAME)) {
				CTFound = true;
				firstCTID = cts.get(indice).getVmid();
			}
		}
		if(firstCTID != null) {
			System.out.println("Migrating CT : "+firstCTID+" from "+srcServer+" to "+dstServer);
			api.migrateCT(srcServer,firstCTID, dstServer);
		}
	}

	// arrêter le plus vieux conteneur sur le serveur "server"
	public void offLoad(String server) throws LoginException, JSONException, IOException {
		//On récupère les CTs de ce serveur
		List<LXC> cts;
		cts = api.getCTs(server);
		
		String olderCTID = null;
		long longestUptime = 0,currentUptime;
		
		//On récupère l'ID du plus ancien
		for (LXC lxc : cts) {
			//On récupère seulement ceux de mon binome
			if(lxc.getName().contains(Constants.CT_BASE_NAME)) {
				currentUptime = lxc.getUptime();
				if(currentUptime>longestUptime) {
					longestUptime = currentUptime;
					olderCTID = lxc.getVmid();
				}
			}
		}
		
		//Et on l'arrete
		if(olderCTID != null) {
			System.out.println("Stopping CT : "+olderCTID+" on server "+server);
			api.stopCT(server, olderCTID);
		}		
	}

}
