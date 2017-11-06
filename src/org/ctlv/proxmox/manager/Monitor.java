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

public class Monitor implements Runnable {

	Analyzer analyzer;
	ProxmoxAPI api;
	
	public Monitor(ProxmoxAPI api, Analyzer analyzer) {
		this.api = api;
		this.analyzer = analyzer;
	}
	

	@Override
	public void run() {
		List<LXC> mesCTs = new ArrayList<LXC>();
		List<LXC> CTs = new ArrayList<LXC>();
		Map<String, List<LXC>> myCTsPerServer = new HashMap<String, List<LXC>>();
		while(true) {
			System.out.println("Nouveau cycle monitor");
			// Récupérer les données sur les serveurs
			myCTsPerServer.clear();
			mesCTs.clear();
			CTs.clear();
			try {
				CTs = api.getCTs(Constants.SERVER1);
				for(LXC lxc : CTs) {
					if(lxc.getName().contains(Constants.CT_BASE_NAME)) {
						mesCTs.add(lxc);
					}
				}
				myCTsPerServer.put(Constants.SERVER1, mesCTs);
				mesCTs.clear();
				
				CTs = api.getCTs(Constants.SERVER2);
				for(LXC lxc : CTs) {
					if(lxc.getName().contains(Constants.CT_BASE_NAME)) {
						mesCTs.add(lxc);
					}
				}
				myCTsPerServer.put(Constants.SERVER2, mesCTs);
				
			} catch (LoginException | JSONException | IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			// Lancer l'anlyse
			try {
				analyzer.analyze(myCTsPerServer);
			} catch (LoginException | JSONException | IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			
			// attendre une certaine période
			try {
				Thread.sleep(Constants.MONITOR_PERIOD * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
}
