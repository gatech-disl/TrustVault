/**
 * Georgia Tech
 * DISL
 * 2016
 */
package core_lib;

import generator_lib.GeneratorOutput;

import java.util.LinkedList;
import java.util.ArrayList;

//import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;



public class Structure {
	public Globals GLOBALS;
	public Network nw;
	public LinkedList<Integer>[] user_link;
	//public Network nw;
	public User user;
	public int [][] map;
	
    public Structure(Globals GLOBALS){
    	this.GLOBALS= GLOBALS;
    	 user_link = new LinkedList[GLOBALS.NUM_USERS];
    	
    	for (int i=0;i<GLOBALS.NUM_USERS;i++){
    		user_link[i]=new LinkedList<Integer>();
    	}
    }
    
    public void Linkrelation(Network nw){

    	//int m=0;
    	for (int i=0;i<GLOBALS.NUM_USERS;i++){
    		//System.out.print(i+nw.getUser(i).isPreTrusted()+"\n");
    		
    		 if (nw.getUser(i).isPreTrusted()){
    			// int flg=0;
    			while (user_link[i].size()< 10){ //&& flg<nw.GLOBALS.NUM_USERS*2){
    				
    				int tmp= GLOBALS.RAND.nextInt(GLOBALS.NUM_USERS);
    				//if ((tmp!=i) && (user_link[tmp].size()<10)&&(nw.getUser(tmp).getModel()!=User.Behavior.USR_GOOD)){
    					if (tmp!=i && !user_link[i].contains(tmp)){
    				        user_link[i].add(tmp);	
    				    if(!user_link[tmp].contains(i))
    				        user_link[tmp].add(i);
    				}
    				/*if((tmp!=i)&&(user_link[tmp].size()<2)&&(nw.getUser(tmp).getModel()==User.Behavior.USR_GOOD)){
    					user_link[i].add(tmp);
    					user_link[tmp].add(i);
    			     }*/  
    				//flg++;
    			}
    		 
    		 }
    		 
//    		if (nw.getUser(i).getModel()!=User.Behavior.USR_GOOD){
//    			//int flg=0;
//    			while(user_link[i].size()<10){// && flg<nw.GLOBALS.NUM_USERS*2){
//    				int tmp=GLOBALS.RAND.nextInt(GLOBALS.NUM_USERS);
//    				//if ((tmp!=i) && (user_link[tmp].size()<10)&&(nw.getUser(tmp).getModel()!=User.Behavior.USR_GOOD)){
//    		          if(tmp!=i && !user_link[i].contains(tmp)){
//    				    user_link[i].add(tmp);
//    				    if(!user_link[tmp].contains(i))
//    		            user_link[tmp].add(i);
//    				}
//    				/*if((tmp!=i)&&(user_link[tmp].size()<2)&&(nw.getUser(tmp).getModel()==User.Behavior.USR_GOOD)){
//    					user_link[i].add(tmp);
//    					user_link[tmp].add(i);
//    			     }*/
//    				//flg++;
//    			   }
//    			}
    		 if (nw.getUser(i).getModel()==User.Behavior.USR_PURE){
     			//int flg=0;
     			while(user_link[i].size()< 10){// && flg<nw.GLOBALS.NUM_USERS*2){
     				int tmp=GLOBALS.RAND.nextInt(GLOBALS.NUM_USERS);
     				//if ((tmp!=i) && (user_link[tmp].size()<10)&&(nw.getUser(tmp).getModel()!=User.Behavior.USR_GOOD)){
     		          if(tmp!=i && !user_link[i].contains(tmp)){
     				    user_link[i].add(tmp);
     				    if(!user_link[tmp].contains(i))
     		            user_link[tmp].add(i);
     				}
     				/*if((tmp!=i)&&(user_link[tmp].size()<2)&&(nw.getUser(tmp).getModel()==User.Behavior.USR_GOOD)){
     					user_link[i].add(tmp);
     					user_link[tmp].add(i);
     			     }*/
     				//flg++;
     			   }
     			}
    		if (nw.getUser(i).getModel()==User.Behavior.USR_DISG){
    			//int flg=0;
    			while(user_link[i].size()< 10){// && flg<nw.GLOBALS.NUM_USERS*2){
    				int tmp=GLOBALS.RAND.nextInt(GLOBALS.NUM_USERS);
    				//if ((tmp!=i) && (user_link[tmp].size()<10)&&(nw.getUser(tmp).getModel()!=User.Behavior.USR_GOOD)){
    		          if(tmp!=i && !user_link[i].contains(tmp)){
    				    user_link[i].add(tmp);
    				    if(!user_link[tmp].contains(i))
    		            user_link[tmp].add(i);
    				}
    				/*if((tmp!=i)&&(user_link[tmp].size()<2)&&(nw.getUser(tmp).getModel()==User.Behavior.USR_GOOD)){
    					user_link[i].add(tmp);
    					user_link[tmp].add(i);
    			     }*/
    				//flg++;
    			   }
    			}
    		
    		if (nw.getUser(i).getModel()==User.Behavior.USR_GOOD){
    			//int flg=0;
    			while(user_link[i].size()< 2){// && flg<nw.GLOBALS.NUM_USERS*2){
    				int tmp=GLOBALS.RAND.nextInt(GLOBALS.NUM_USERS);
    				//if ((tmp!=i) && (user_link[tmp].size()<10)&&(nw.getUser(tmp).getModel()!=User.Behavior.USR_GOOD)){
    		          if(tmp!=i && !user_link[i].contains(tmp)){
    				      user_link[i].add(tmp);
    				    if(!user_link[tmp].contains(i))
    		              user_link[tmp].add(i);
    				}
    				/*if((tmp!=i)&&(user_link[tmp].size()<2)&&(nw.getUser(tmp).getModel()==User.Behavior.USR_GOOD)){
    					user_link[i].add(tmp);
    					user_link[tmp].add(i);
    			     }*/
    				//flg++;
    			}
    			}
    	}
    	
    	
    	for (int i=0;i<GLOBALS.NUM_USERS;i++){
    	   System.out.print(i+".neighbours= "+user_link[i].size()+"\n");
    	   System.out.println("\n");
    	   
    	   	}
    	
    
    	
    	  map=new int[GLOBALS.NUM_USERS][GLOBALS.NUM_USERS];
    	for(int i=0;i<GLOBALS.NUM_USERS;i++){
       		for (int j=0;j<GLOBALS.NUM_USERS;j++){
       			if(i==j)
       				map[i][j]=0;
       			else if(user_link[i].contains(j))
       				map[i][j]=1;
       			else
       				map[i][j]=-1;       			   			
       		}
       	}
    	
    
    
    }
    
    
    public void showmatrix (){
    	for (int i=0;i<GLOBALS.NUM_USERS;i++){
    		for(int j=0;j<GLOBALS.NUM_USERS;j++){
    			System.out.print(map[i][j]+" ");
    		}
    		System.out.print(";\n");
    	}
    	
    }
    
    public int pathlength(int start, int end, Network nw ){
    	//int pathleng=0;
    	int len = map.length;
    	//System.out.println("len= "+len);
    	
    	 
        int[] pathMileage = new int[len];
        boolean[] isDefined = new boolean[len];
        ArrayList<ArrayList<Integer>> pathWay = new ArrayList<ArrayList<Integer>>();
 
        for (int cursor = 0; cursor < len; cursor++) {
            pathMileage[cursor] = map[start][cursor];
            pathWay.add(new ArrayList<Integer>());
            //if(nw.getUser(cursor).isPreTrusted()){
               if (pathMileage[cursor] >= 0) {
                   pathWay.get(cursor).add(start);
                   pathWay.get(cursor).add(cursor);
               }
            //}
           /* else{
            	 if (pathMileage[cursor] >= 0 && nw.GLOBALS.RAND.nextDouble()<uniformdistribution(cursor, nw)) {
                     pathWay.get(cursor).add(start);
                     pathWay.get(cursor).add(cursor);
                 }
            	 
            }*/
            isDefined[cursor] = false;
        }
 
        pathMileage[start] = 0;
        isDefined[start] = true;
 
 
        int addUpCount = 1;
 
        while (addUpCount < len) {
            int turnMinMileage = -1;
            int turnMinPoint = -1;
 
            for (int cusor = 0; cusor < len; cusor++) {
                if (isDefined[cusor]) {
                    continue;
                }
                if (turnMinMileage == -1 && pathMileage[cusor] >=0) {
                    turnMinMileage = pathMileage[cusor];
                    turnMinPoint = cusor;
                } else if (turnMinMileage >= 0 && pathMileage[cusor] >=0
                        && pathMileage[cusor] < turnMinMileage) {
                    turnMinMileage = pathMileage[cusor];
                    turnMinPoint = cusor;
                }
            }
 
            if (turnMinMileage == -1) {
                break;
            }
 
            isDefined[turnMinPoint] = true;
            addUpCount++;
 
            for (int cursor = 0; cursor < len; cursor++) {
                if (isDefined[cursor]) {
                    continue;
                }
            /* if(!nw.getUser(cursor).isPreTrusted()){
                 if (pathMileage[turnMinPoint] != -1
                        && map[turnMinPoint][cursor] != -1 && nw.GLOBALS.RAND.nextDouble()<uniformdistribution(cursor, nw)) {
                	// System.out.println(" cut point 1!");
                    int newLen = pathMileage[turnMinPoint]
                            + map[turnMinPoint][cursor];
                    if (newLen < pathMileage[cursor]
                            || pathMileage[cursor] == -1) {
                        pathMileage[cursor] = newLen;
                        copyPathWay(pathWay.get(turnMinPoint), pathWay
                                .get(cursor), cursor);
                    }
                 }
                } 
              */
             // else{
                	if (pathMileage[turnMinPoint] != -1
                            && map[turnMinPoint][cursor] != -1) {
                	//	System.out.println("cut point 2!");
                        int newLen = pathMileage[turnMinPoint]
                                + map[turnMinPoint][cursor];
                        if (newLen < pathMileage[cursor]
                                || pathMileage[cursor] == -1) {
                            pathMileage[cursor] = newLen;
                            copyPathWay(pathWay.get(turnMinPoint), pathWay
                                    .get(cursor), cursor);
                        }
                    }	
                	
              //  }
            }
       }
        LinkedList<Integer> rs = new LinkedList<Integer>();
        rs.addAll(pathWay.get(end));
        rs.add(0, pathMileage[end]);
       // System.out.print("\n"+rs+"\n");
        
       if (pathMileage[end]>0)
          return pathMileage[end];
       else 
    	   return Integer.MAX_VALUE;
    }
    
    
  private static void copyPathWay(ArrayList<Integer> source,
        ArrayList<Integer> destiny, int addPoint) {
    while (!destiny.isEmpty()) {
        destiny.remove(0);
    }
    destiny.addAll(source);
    destiny.add(addPoint);
    }
  
  private static double uniformdistribution(int user, Network nw){
      double probability;
      double L=0, U=nw.GLOBALS.NUM_USERS;	  
    		  probability = (user-L+1)/(U-L);
	 // System.out.println( "user="+user+", probability= "+probability);
	  return probability;
	  
  }
}
    
