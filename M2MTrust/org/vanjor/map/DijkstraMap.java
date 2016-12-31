/**
 * Georgia Tech
 * DISL
 * 2016
 */
//package core_lib;

package org.vanjor.map;

import java.util.ArrayList;
import java.util.LinkedList;
 
/**
 * Dijkstra最短路径搜寻算法Java实现
 * @author Vanjor
 */
public class DijkstraMap {
 
    /**
     * 实现Dijkstra最短路径搜寻算法
     * @param map
     *        输入，为图矩阵，可以有向或无向图，距离为-1表示无穷远，一非负值表示一个实际距离
     *        map[i][i]自身距离初始化为0，
     *        起始节点从0开始
     * @param start
     *        出发节点
     * @param end
     *        目标节点
     * @return
     *        LinkedList<Integer> ，第一位表示总里程，-1表示无穷远，随后表示从start到end的路径节点
     */
    public static LinkedList<Integer> getShortest(int[][] map, int start,
            int end) {
 
        int len = map.length;
 
        // 对应pathMileage[i]为当前start到i的最短距离
        int[] pathMileage = new int[len];
        boolean[] isDefined = new boolean[len];
        ArrayList<ArrayList<Integer>> pathWay = new ArrayList<ArrayList<Integer>>();
 
        // 初始化里程计数与是否确定为最短里程的标记量
        for (int cursor = 0; cursor < len; cursor++) {
            pathMileage[cursor] = map[start][cursor];
            pathWay.add(new ArrayList<Integer>());
            if (pathMileage[cursor] >= 0) {
                pathWay.get(cursor).add(start);
                pathWay.get(cursor).add(cursor);
            }
            isDefined[cursor] = false;
        }
 
        // 初始化对应 start->start的里程距离为0，并且确定为start->start最短里程
        pathMileage[start] = 0;
        isDefined[start] = true;
 
        // 将len长度的点，分类两组，一组是确定好了start到该点最短距离的点集，
        // 对应isDefined标记为true， 另一组为尚未确定，对应isDefined标记为true
        // 其中addUpCount为对以确定好最短距离点集的计数
 
        int addUpCount = 1;
 
        // 当节点全部确定最短距离后，即退出循环检查
        while (addUpCount < len) {
            // 每一轮次，待确定最短距离集中的最小值
            int turnMinMileage = -1;
            // 对应最小值的pathMileage行值
            int turnMinPoint = -1;
 
            for (int cusor = 0; cusor < len; cusor++) {
                // 如果对应的节点cusor已经确定最短距离则不参与排序搜寻
                if (isDefined[cusor]) {
                    continue;
                }
                // 在当前未确定最短距离的节点集中找出最短距离turnMinMileage
                // 以及对应节点isDefined
                if (turnMinMileage == -1 && pathMileage[cusor] >=0) {
                    // 对遇到第一个不是无穷远的距离点，将对应的值覆盖初始化点
                    turnMinMileage = pathMileage[cusor];
                    turnMinPoint = cusor;
                } else if (turnMinMileage >= 0 && pathMileage[cusor] >=0
                        && pathMileage[cusor] < turnMinMileage) {
                    // 找到更小的点，进行覆盖
                    turnMinMileage = pathMileage[cusor];
                    turnMinPoint = cusor;
                }
            }
 
            // 如果当前轮次，再也找不到距离非无穷远的未确定远点，
            // 则证明剩下的点为非连通点，也可同样退出while循环检查
            if (turnMinMileage == -1) {
                break;
            }
 
            // 为当前所找到的最短距离点标记为已确定
            isDefined[turnMinPoint] = true;
            // 已确定点+1计数
            addUpCount++;
 
            // 遍历查看，对于新加点turnKeyPoint，
            // 有没有可能改进原始的start到各点的最短距离
            for (int cursor = 0; cursor < len; cursor++) {
                // 已确定的就不在考虑中
                if (isDefined[cursor]) {
                    continue;
                }
                // 当pathMileage以登记的start->turnKeyPoint以及
                // turnKeyPoint—>cursor的距离都非无限远，则可以考虑比较
                if (pathMileage[turnMinPoint] != -1
                        && map[turnMinPoint][cursor] != -1) {
                    // 待考虑的新加节点turnKeyPoint组成的路径start->..->turnKeyPoint->cursor；
                    int newLen = pathMileage[turnMinPoint]
                            + map[turnMinPoint][cursor];
                    // 作比较，若小则覆盖,或者原来距离为无穷远也覆盖
                    if (newLen < pathMileage[cursor]
                            || pathMileage[cursor] == -1) {
                        pathMileage[cursor] = newLen;
                        copyPathWay(pathWay.get(turnMinPoint), pathWay
                                .get(cursor), cursor);
                    }
                }
            }
        }
        LinkedList<Integer> rs = new LinkedList<Integer>();
        rs.addAll(pathWay.get(end));
        rs.add(0, pathMileage[end]);
        return rs;
    }
 
    private static void copyPathWay(ArrayList<Integer> source,
            ArrayList<Integer> destiny, int addPoint) {
        while (!destiny.isEmpty()) {
            destiny.remove(0);
        }
        destiny.addAll(source);
        destiny.add(addPoint);
    }
 
    //初始化图
    public static int[][] generateMap() {
        int[][] map = new int[6][6];
       /* map[0][5] = 100;
        map[0][4] = 30;
        map[0][2] = 10;
        map[1][2] = 5;
        map[2][3] = 50;
        map[2][5] = 20;
        map[3][5] = 10;
        map[4][5] = 60;
        map[4][3] = 20;
        map[5][3] = 10;
        */
        map[0][5] = 1;
        map[0][4] = 1;
        map[0][2] = 1;
        map[1][2] = 1;
        map[2][3] = 1;
        map[2][5] = 1;
        map[3][5] = 1;
        map[4][5] = 1;
        map[4][3] = 1;
        map[5][3] = 1;
        int len = map.length;
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < len; j++) {
                if (i == j) {
                    map[i][j] = 0;
                } else if (map[i][j] == 0) {
                    map[i][j] = -1;
                }
            }
        }
        return map;
    }
 
    public static void main(String[] args) {
        System.out.println(getShortest(generateMap(), 0, 5));
        System.out.println(Integer.MAX_VALUE);
        
        double[] trust= new double[3];
        trust[0]=0.2;
        trust[1]=0.6;
        trust[2]=0.4;
        double t;
        
        for (int i=0;i<trust.length;i++)
			System.out.println(trust[i]);
        
		for(int i=1;i<=trust.length-1;i++) { 
		   for(int j=0;j<trust.length-i;j++) { 
	        	if(trust[j]>trust[j+1])  { 
		              t=trust[j]; 
		              trust[j]=trust[j+1]; 
		              trust[j+1]=t; 
		           } 
		      } 
		}
		
		for (int i=0;i<trust.length;i++)
			System.out.println(trust[i]);
    }
}
