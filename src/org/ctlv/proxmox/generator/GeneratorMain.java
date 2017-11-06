package org.ctlv.proxmox.generator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.security.auth.login.LoginException;

import org.ctlv.proxmox.api.Constants;
import org.ctlv.proxmox.api.ProxmoxAPI;
import org.ctlv.proxmox.api.data.LXC;
import org.ctlv.proxmox.api.data.Node;
import org.json.JSONException;

public class GeneratorMain {
	
	static Random rndTime = new Random(new Date().getTime());
	public static int getNextEventPeriodic(int period) {
		return period;
	}
	public static int getNextEventUniform(int max) {
		return rndTime.nextInt(max);
	}
	public static int getNextEventExponential(int inv_lambda) {
		float next = (float) (- Math.log(rndTime.nextFloat()) * inv_lambda);
		return (int)next;
	}
	
	public static void main(String[] args) throws InterruptedException, LoginException, JSONException, IOException {
		
		
	
		long baseID = Constants.CT_BASE_ID;
		int lambda = 30;

		ProxmoxAPI api = new ProxmoxAPI();
		Random rndServer = new Random(new Date().getTime());
		Random rndRAM = new Random(new Date().getTime()); 
		
		long memAllowedOnServer1 = (long) (api.getNode(Constants.SERVER1).getMemory_total() * Constants.MAX_THRESHOLD);
		long memAllowedOnServer2 = (long) (api.getNode(Constants.SERVER2).getMemory_total() * Constants.MAX_THRESHOLD);
		
		//On boucle Ã  l'infini
		while (true) {
			Map<String, List<LXC>> myCTsPerServer = new HashMap<String, List<LXC>>();
			// 1. Calculer la quantité de RAM utilisée par mes CTs sur chaque serveur
			long memOnServer1 = 0;
			List<LXC> cts1 = api.getCTs(Constants.SERVER1);
			//System.out.println("Nombre de CTs "+Constants.SERVER1+":"+cts1.size());
			for (LXC lxc : cts1) {
				if(lxc.getName().contains(Constants.CT_BASE_NAME)) {
					if(myCTsPerServer.containsKey(Constants.SERVER1)) {
						List<LXC> mesCts1 = myCTsPerServer.get(Constants.SERVER1);
						mesCts1.add(lxc);
						myCTsPerServer.put(Constants.SERVER1,mesCts1);
					}
					else {
						List<LXC> mylxc = new ArrayList<LXC>();
						mylxc.add(lxc);
						myCTsPerServer.put(Constants.SERVER1,mylxc);
					}
					memOnServer1 += lxc.getMem();
				}
			}
			if(myCTsPerServer.containsKey(Constants.SERVER1)) {
				System.out.println("Nombre de CTs du groupe A2 sur "+Constants.SERVER1+" : "+myCTsPerServer.get(Constants.SERVER1).size());
			}
			else 
				System.out.println("Aucun CTs du groupe A2 sur "+Constants.SERVER1);
			
			long memOnServer2 = 0;
			List<LXC> cts2 = api.getCTs(Constants.SERVER2);
			//System.out.println("Nombre de CTs "+Constants.SERVER2+":"+cts2.size());
			for (LXC lxc : cts2) {
				if(lxc.getName().contains(Constants.CT_BASE_NAME)) {
					if(myCTsPerServer.containsKey(Constants.SERVER2)) {
						List<LXC> mesCts2 = myCTsPerServer.get(Constants.SERVER2);
						mesCts2.add(lxc);
						myCTsPerServer.put(Constants.SERVER2,mesCts2);
					}
					else {
						List<LXC> mylxc = new ArrayList<LXC>();
						mylxc.add(lxc);
						myCTsPerServer.put(Constants.SERVER2,mylxc);
					}
					memOnServer2 += lxc.getMem();
				}
			}
			if(myCTsPerServer.containsKey(Constants.SERVER2)) {
				System.out.println("Nombre de CTs du groupe A2 sur "+Constants.SERVER2+" : "+myCTsPerServer.get(Constants.SERVER2).size());
			}
			else 
				System.out.println("Aucun CTs du groupe A2 sur "+Constants.SERVER2);
			
			System.out.println("Mémoire CTs serveur 5 : "+memOnServer1+"/"+memAllowedOnServer1+"("+(((double)memOnServer1/((long) api.getNode(Constants.SERVER1).getMemory_total()))*100)+"%)");
			System.out.println("Mémoire CTs serveur 6 : "+memOnServer2+"/"+memAllowedOnServer2+"("+(((double)memOnServer2/((long) api.getNode(Constants.SERVER2).getMemory_total()))*100)+"%)");
			
			
			// Mémoire autorisée sur chaque serveur
			float memRatioOnServer1 = 0;
			memRatioOnServer1 = memAllowedOnServer1;		
			float memRatioOnServer2 = 0;
			memRatioOnServer2 = memAllowedOnServer2;	
			
			if (memOnServer1 < memRatioOnServer1 && memOnServer2 < memRatioOnServer2) {  // Exemple de condition de l'arrï¿½t de la gï¿½nï¿½ration de CTs
				
				// choisir un serveur alÃ©atoirement avec les ratios spÃ©cifiÃ©s 66% vs 33%
				String serverName;
				if (rndServer.nextFloat() < Constants.CT_CREATION_RATIO_ON_SERVER1)
					serverName = Constants.SERVER1;
				else
					serverName = Constants.SERVER2;
				
				int numeroCT = 0;
				//On rÃ©cupÃ¨re le nombre de CT de mon groupe et on ajoute 1 pour respecter la norme de nommage
				//si on a au moins un CT sur ce serveur
				if(myCTsPerServer.get(Constants.SERVER1)!=null) {
					numeroCT= myCTsPerServer.get(Constants.SERVER1).size();
				}
				if(myCTsPerServer.get(Constants.SERVER2)!=null) {
					numeroCT += myCTsPerServer.get(Constants.SERVER2).size();
				}
				numeroCT ++;
				
				System.out.println("Nouveau CT "+String.valueOf(baseID+numeroCT)+" sur "+serverName);
				// créer un conteneur sur ce serveur
				api.createCT(serverName, String.valueOf(baseID+numeroCT), Constants.CT_BASE_NAME+numeroCT, 512);
				
								
				// planifier la prochaine crÃ©ation
				int timeToWait = getNextEventPeriodic(lambda); // par exemple une loi expo d'une moyenne de 30sec
				
				// attendre jusqu'au prochain Ã©vÃ©nement
				Thread.sleep(1000 * timeToWait);
				
				//On lance le nouveau CT avant de changer
				LXC monCt = api.getCT(serverName, String.valueOf(baseID+numeroCT));
				//On attends de pouvoir lancer le CT
				System.out.println("status: "+monCt.getStatus());
				api.startCT(serverName, String.valueOf(baseID+numeroCT));
				
			}
			else {
				System.out.println("Servers are loaded, waiting ...");
				Thread.sleep(Constants.GENERATION_WAIT_TIME* 1000);
			}
		}
		
	}

}
