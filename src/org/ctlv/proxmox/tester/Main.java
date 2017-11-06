package org.ctlv.proxmox.tester;

import java.io.IOException;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.ctlv.proxmox.api.ProxmoxAPI;
import org.ctlv.proxmox.api.data.LXC;
import org.ctlv.proxmox.api.data.Node;
import org.json.JSONException;

public class Main {

	public static void main(String[] args) throws LoginException, JSONException, IOException {

		ProxmoxAPI api = new ProxmoxAPI();		
		
		//Récupération d'un Node
		Node serveur = api.getNode("srv-px6");
		
		//On affiche certaines variable du node
		/*System.out.println("CPU serveur : "+serveur.getCpu());
		System.out.println("Mémoire utilisé Serveur : "+serveur.getMemory_used());
		System.out.println("Mémoire libre serveur : "+serveur.getMemory_free());*/
		
		//Création  d'un CT sur le serveur 6
		//api.createCT("srv-px6", "12501", "ct-tpgei-ctlv-A25-ct1", 512);
		
		//Démarrage du CT
		api.startCT("srv-px6", "12501");
		
		//On récupère la liste des CTs
		System.out.println("Serveur 5");
		List<LXC> cts = api.getCTs("srv-px5");
		for (LXC lxc : cts) {
			System.out.println(lxc.getName());
		}
		
		System.out.println("Serveur 6");
		List<LXC> cts2 = api.getCTs("srv-px6");
		for (LXC lxc : cts2) {
			System.out.println(lxc.getName());
		}
		
		//On récupère le CT qu'on vient de créer
		//LXC monCt = api.getCT("srv-px5", "12501");
		
		//On affiche quelques données du CT
		/*System.out.println("CT : "+monCt.getName());
		System.out.println("CT : "+monCt.getVmid());
		System.out.println("Status : "+monCt.getStatus());
		System.out.println("CPU : "+monCt.getCpu());
		System.out.println("Mémoire : "+monCt.getMem());
		System.out.println("Disque dur lecture : "+monCt.getDiskread()+" et écriture "+monCt.getDiskwrite());
		System.out.println("Hébergé sur le serveur "+monCt.getNetin());*/
		
		
	}

}
