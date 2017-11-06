package org.ctlv.proxmox.manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.LoginException;

import org.ctlv.proxmox.api.Constants;
import org.ctlv.proxmox.api.ProxmoxAPI;
import org.ctlv.proxmox.api.data.LXC;
import org.json.JSONException;

public class Analyzer {
	ProxmoxAPI api;
	Controller controller;
	
	public Analyzer(ProxmoxAPI api, Controller controller) {
		this.api = api;
		this.controller = controller;
	}
	
	public void analyze(Map<String, List<LXC>> myCTsPerServer) throws LoginException, JSONException, IOException  {
		
		// Calculer la quantité de RAM utilisée par mes CTs sur chaque serveur
		long memOnServer1 = 0;
		for (LXC lxc : myCTsPerServer.get(Constants.SERVER1)) {
			memOnServer1 += lxc.getMem();
		}
		
		long memOnServer2 = 0;
		for (LXC lxc : myCTsPerServer.get(Constants.SERVER2)) {
			memOnServer2 += lxc.getMem();
		}
		
		// Mémoire autorisée sur chaque serveur
		long memMigrationAllowedOnServer1 = (long) (api.getNode(Constants.SERVER1).getMemory_total() * Constants.MIGRATION_THRESHOLD);
		long memMigrationAllowedOnServer2 = (long) (api.getNode(Constants.SERVER2).getMemory_total() * Constants.MIGRATION_THRESHOLD);
		long memStopAllowedOnServer1 = (long) (api.getNode(Constants.SERVER1).getMemory_total() * Constants.DROPPING_THRESHOLD);
		long memStopAllowedOnServer2 = (long) (api.getNode(Constants.SERVER2).getMemory_total() * Constants.DROPPING_THRESHOLD);		
		
		// Analyze et Actions
		System.out.println("Début de l'analyze");
		if(memOnServer1>memMigrationAllowedOnServer1) {
			controller.migrateFromTo(Constants.SERVER1, Constants.SERVER2);
		}
		else if(memOnServer2>memMigrationAllowedOnServer2) {
			controller.migrateFromTo(Constants.SERVER2, Constants.SERVER1);
		}
		if(memOnServer1>memStopAllowedOnServer1) {
			controller.offLoad(Constants.SERVER1);
		}
		if(memOnServer2>memStopAllowedOnServer2) {
			controller.offLoad(Constants.SERVER2);
		}
		System.out.println("Fin de l'analyze");
	}		
}

