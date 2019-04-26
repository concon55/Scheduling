/**
 *
 * @author Connie Guan
 *
 */

import java.io.*;
import java.util.*;


public class Scheduling {
    
    static Scanner randomNums;
    static int hasBlock = 0; //to calculate IO utilization
    static int finishingTime;
    static boolean verbose = false;
    static int cycle;
    static int quantum;
    static Process selectedProcess = null; //the running process
    static boolean isFinished = false;
    
    //lists
    static Process[] processes; //list of all processes
    static ArrayList<Process> blockedList = new ArrayList<Process>();
    static ArrayList<Process> unstartedList = new ArrayList<Process>();
    static Queue<Process> readyQueue = new LinkedList<Process>();
    static ArrayList<Process> readyList = new ArrayList<Process>(); //for SJF
    
    //compare by ID, arrival time
    static CompareId compareId = new CompareId();
    static CompareArrival compareArr = new CompareArrival();
    
    //random number
    public static int randomOS(int U){
        return(1+randomNums.nextInt() % U);
    }
    
    //First Come First Served
    public static void fcfs(){
        if (verbose) { //detailed output
            System.out.println("This detailed printout gives the state and remaining burst for each process.");
            System.out.print("\nBefore cycle " + cycle + ": "); //print Before Cycle 0
            for (int i = 0; i < processes.length; i++) {
                System.out.print(processes[i].status + " " + processes[i].burst + " ");
            }
        }
        cycle++;
        
        //get ready and unstarted processes
        for (int i = 0; i < processes.length; i++) {
            if (processes[i].arrivalTime == 0) {
                processes[i].status = "ready";
                processes[i].burst = 0;
                readyQueue.add(processes[i]);
            } else {
                processes[i].status = "unstarted";
                processes[i].burst = 0;
                unstartedList.add(processes[i]);
            }
        }
        
        while(isFinished == false){
            if(!blockedList.isEmpty()){ //for io utilization
                hasBlock++;
            }
            
            //check if unstarted processes can be ready
            for(int i = 0; i<unstartedList.size(); i++){
                if(unstartedList.get(i).arrivalTime + 1 == cycle){
                    unstartedList.get(i).status = "ready";
                    readyQueue.add(unstartedList.get(i));
                }
            }
            
            //select ready process to run
            if(selectedProcess== null){
                selectedProcess = readyQueue.poll(); //select first process from ready queue
                if(selectedProcess != null){
                    selectedProcess.status = "running";
                    selectedProcess.burst = randomOS(selectedProcess.cpuBase);
                }
            }
            
            //if there is a selected process currently running, decrement bursts
            if(selectedProcess != null){
                selectedProcess.burst--;
                selectedProcess.remainingCPUBurst--;
            }
            
            if (verbose) { //print rest of detailed output
                System.out.print("\nBefore cycle " + cycle + ": ");
                for (int i = 0; i < processes.length; i++) {
                    if (processes[i].status.equals("running")) {
                        System.out.print(processes[i].status + " " + (1 + processes[i].burst) + " ");
                    } else {
                        System.out.print(processes[i].status+ " " + processes[i].burst + " ");
                    }
                }
            }
            
            //increment wait time for each ready process
            if(!readyQueue.isEmpty()){
                for(Process p: readyQueue){
                    p.waitTime++;
                }
            }
            
            //unblock
            if (!blockedList.isEmpty()) {
                int canAdd = 0;
                ArrayList<Process> addToReady = new ArrayList<Process>(); //array of blocked processes to be added to ready queue
                Process[] blockedArray = blockedList.toArray(new Process[0]);
                for (int i = 0; i < blockedArray.length; i++) {
                    blockedArray[i].burst--;
                    blockedArray[i].ioTime++;
                    if (blockedArray[i].burst == 0) { //blocked to ready
                        canAdd++;
                        blockedArray[i].status = "ready";
                        addToReady.add(blockedArray[i]);
                        blockedList.remove(blockedArray[i]);
                    }
                }
                if (canAdd == 1) { //if 1 process, just add it
                    readyQueue.add(addToReady.get(0));
                } else {
                    Collections.sort(addToReady, compareId); //if there are multiple, sort processes by ID
                    readyQueue.addAll(addToReady);
                }
            }
            
            if (selectedProcess != null) {
                if (selectedProcess.remainingCPUBurst == 0) { //check if process terminated, update accordingly
                    selectedProcess.status = "terminated";
                    selectedProcess.burst = 0;
                    selectedProcess.finishTime = cycle;
                    selectedProcess.turnTime = selectedProcess.finishTime - selectedProcess.arrivalTime;
                    selectedProcess = null;
                } else {
                    if (selectedProcess.burst <= 0) { //if process is still running but burst time is up, it becomes blocked
                        selectedProcess.status = "blocked";
                        selectedProcess.burst = randomOS(selectedProcess.ioBase);
                        blockedList.add(selectedProcess);
                        selectedProcess = null;
                    }
                }
            }
            cycle++;
            
            //check if all processes are terminated
            isFinished = true;
            for (int i = 0; i < processes.length; i++) {
                if (!processes[i].status.equals("terminated")) {
                    isFinished = false;
                }
            }
        }
        System.out.println();
        System.out.println();
        System.out.println("The scheduling algorithm used was First Come First Served\n");
    }
    
    
    //Round Robin
    public static void rr(int quantum){
        if (verbose) { //detailed output
            System.out.println("This detailed printout gives the state and remaining burst for each process.");
            System.out.print("\nBefore cycle " + cycle + ": "); //print Before Cycle 0
            for (int i = 0; i < processes.length; i++) {
                System.out.print(processes[i].status + " " + processes[i].burst + " ");
            }
        }
        cycle++;
        
        //get ready and unstarted processes
        for (int i = 0; i < processes.length; i++) {
            if (processes[i].arrivalTime == 0) {
                processes[i].status = "ready";
                processes[i].burst = 0;
                readyQueue.add(processes[i]);
            } else {
                processes[i].status = "unstarted";
                processes[i].burst = 0;
                unstartedList.add(processes[i]);
            }
        }
        
        while(isFinished == false){
            if(!blockedList.isEmpty()){ //for io utilization
                hasBlock++;
            }
            
            for(int i = 0; i<unstartedList.size(); i++){ //check if unstarted processes can be ready
                if(unstartedList.get(i).arrivalTime + 1 == cycle){
                    unstartedList.get(i).status = "ready";
                    readyQueue.add(unstartedList.get(i));
                }
            }
            
            //select ready process to run
            if(selectedProcess== null){
                selectedProcess = readyQueue.poll(); //select first process from ready queue
                if(selectedProcess != null){
                    selectedProcess.status = "running";
                    if(selectedProcess.burst == 0){
                        if (selectedProcess.remainingRun != 0){ //if still running, keep track of remaining burst time, will later be put in ready
                            selectedProcess.burst = selectedProcess.remainingRun;
                            selectedProcess.remainingRun = 0;
                        }else{
                            selectedProcess.burst = randomOS(selectedProcess.cpuBase);
                        }
                    }
                }
            }
            
            //if there is a selected process currently running
            if(selectedProcess != null){
                selectedProcess.burst--;
                selectedProcess.remainingCPUBurst--;
                selectedProcess.quantum--;
                quantum--;
            }
            
            if (verbose) { //print rest of detailed output
                System.out.print("\nBefore cycle " + cycle + ": ");
                for (int i = 0; i < processes.length; i++) {
                    if (processes[i].status.equals("running")) {
                        if (processes[i].burst != 0) {
                            System.out.print(processes[i].status + " " + (1 + processes[i].quantum)+ " "); //print quantum if burst not finished
                        } else {
                            System.out.print(processes[i].status + " " + (1 + processes[i].burst) + " ");
                        }
                    } else {
                        System.out.print(processes[i].status+ " " + processes[i].burst + " ");
                    }
                }
            }
            
            if(!readyQueue.isEmpty()){
                for(Process p: readyQueue){
                    p.waitTime++; //increment wait time for ready processes
                }
            }
            
            if (!blockedList.isEmpty()) {
                int canAdd = 0;
                ArrayList<Process> addToReady = new ArrayList<Process>(); //array of blocked processes to be added to ready queue
                Process[] blockedArray = blockedList.toArray(new Process[0]);
                for (int i = 0; i < blockedArray.length; i++) {
                    blockedArray[i].burst--; //increment wait time, decrement time available
                    blockedArray[i].ioTime++;
                    if (blockedArray[i].burst == 0) { //blocked to ready
                        canAdd ++;
                        blockedArray[i].status = "ready";
                        addToReady.add(blockedArray[i]);
                        blockedList.remove(blockedArray[i]);
                    }
                }
                if (canAdd == 1) { //if 1 process, just add it
                    readyList.add(addToReady.get(0));
                } else {
                    Collections.sort(addToReady, compareArr); //if there are multiple, sort processes by arrival time
                    readyList.addAll(addToReady);
                }
            }
            
            if (selectedProcess != null) {
                if (selectedProcess.remainingCPUBurst == 0) { //check if process terminated, update accordingly
                    selectedProcess.status = "terminated";
                    selectedProcess.burst = 0;
                    selectedProcess.finishTime = cycle;
                    selectedProcess.turnTime = selectedProcess.finishTime - selectedProcess.arrivalTime;
                    selectedProcess.quantum = 2;
                    selectedProcess = null;
                    quantum = 2;
                } else {
                    if (selectedProcess.burst <= 0) { //if process is still running but burst time is up, it becomes blocked
                        selectedProcess.status = "blocked";
                        selectedProcess.burst = randomOS(selectedProcess.ioBase);
                        selectedProcess.quantum = 2;
                        quantum = 2;
                        blockedList.add(selectedProcess);
                        selectedProcess = null;
                    }else{
                        if(quantum == 0){  //Preempt: if timer expires, put in ready
                            selectedProcess.status = "ready";
                            selectedProcess.remainingRun = selectedProcess.burst;
                            selectedProcess.burst = 0;
                            selectedProcess.quantum = 2;
                            readyList.add(selectedProcess);
                            selectedProcess = null;
                            quantum = 2;
                        }
                    }
                }
            }
            cycle++;
            
            //add to ready queue
            Collections.sort(readyList, compareArr); //sort by arrival time (process at front of ready list will be first to removed)
            for (int i = 0; i < readyList.size(); i++) {
                readyQueue.add(readyList.get(i));
            }
            readyList.clear();
            
            //check if all processes are terminated
            isFinished = true;
            for (int i = 0; i < processes.length; i++) {
                if (!processes[i].status.equals("terminated")) {
                    isFinished = false;
                }
            }
        }
        System.out.println();
        System.out.println();
        System.out.println("The scheduling algorithm used was Round Robin\n");
    }
    
    
    public static void uniprogrammed(){
        if (verbose) { //detailed output
            System.out.println("This detailed printout gives the state and remaining burst for each process.");
            System.out.print("\nBefore cycle " + cycle + ": "); //print Before Cycle 0
            for (int i = 0; i < processes.length; i++) {
                System.out.print(processes[i].status + " " + processes[i].burst + " ");
            }
        }
        cycle++;
        
        //get ready and unstarted processes
        for (int i = 0; i < processes.length; i++) {
            if (processes[i].arrivalTime == 0) {
                processes[i].status = "ready";
                processes[i].burst = 0;
                readyQueue.add(processes[i]);
            } else {
                processes[i].status = "unstarted";
                processes[i].burst = 0;
                unstartedList.add(processes[i]);
            }
        }
        
        while(isFinished == false){
            if(!blockedList.isEmpty()){ //for io utilization
                hasBlock++;
            }
            
            for(int i = 0; i<unstartedList.size(); i++){ //check if unstarted processes can be ready
                if(unstartedList.get(i).arrivalTime + 1 == cycle){
                    unstartedList.get(i).status = "ready";
                    readyQueue.add(unstartedList.get(i));
                }
            }
            
            //select ready process to run only if no process is blocked
            if(selectedProcess== null && blockedList.isEmpty()){
                selectedProcess = readyQueue.poll(); //select first process from ready queue
                if(selectedProcess != null){
                    selectedProcess.status = "running";
                    selectedProcess.burst = randomOS(selectedProcess.cpuBase);
                }
            }
            
            //if there is a selected process currently running
            if(selectedProcess != null){
                selectedProcess.burst--;
                selectedProcess.remainingCPUBurst--;
            }
            
            if (verbose) { //print rest of detailed output
                System.out.print("\nBefore cycle " + cycle + ": ");
                for (int i = 0; i < processes.length; i++) {
                    if (processes[i].status.equals("running")) {
                        System.out.print(processes[i].status + " " + (1 + processes[i].burst) + " ");
                    } else {
                        System.out.print(processes[i].status+ " " + processes[i].burst + " ");
                    }
                }
            }
            
            if(!readyQueue.isEmpty()){
                for(Process p: readyQueue){
                    p.waitTime++; //increment wait time for ready processes
                }
            }
            
            if (!blockedList.isEmpty()) {
                Process[] blockedArray = blockedList.toArray(new Process[0]);
                for(int i = 0; i < blockedArray.length; i++){
                    blockedArray[i].burst--; //increment wait time, decrement time available
                    blockedArray[i].ioTime++;
                    if (blockedArray[i].burst == 0) { //blocked to run
                        selectedProcess = blockedArray[i];
                        selectedProcess.burst = randomOS(selectedProcess.cpuBase);
                        selectedProcess.status = "running";
                        blockedList.remove(blockedArray[i]);
                    }
                }
            }
            
            if (selectedProcess != null) {
                if (selectedProcess.remainingCPUBurst == 0) { //check if process terminated, update accordingly
                    selectedProcess.status = "terminated";
                    selectedProcess.burst = 0;
                    selectedProcess.finishTime = cycle;
                    selectedProcess.turnTime = selectedProcess.finishTime - selectedProcess.arrivalTime;
                    selectedProcess = null;
                } else {
                    if (selectedProcess.burst <= 0) { //if process is still running but burst time is up, it becomes blocked
                        selectedProcess.status = "blocked";
                        selectedProcess.burst = randomOS(selectedProcess.ioBase);
                        blockedList.add(selectedProcess);
                        selectedProcess = null;
                    }
                }
            }
            cycle++;
            
            //check if all processes are terminated
            isFinished = true;
            for (int i = 0; i < processes.length; i++) {
                if (!processes[i].status.equals("terminated")) {
                    isFinished = false;
                }
            }
        }
        System.out.println();
        System.out.println();
        System.out.println("The scheduling algorithm used was Uniprocessor\n");
    }
    
    //Shortest Job First
    public static void sjf(){
        if (verbose) { //detailed output
            System.out.println("This detailed printout gives the state and remaining burst for each process.");
            System.out.print("\nBefore cycle " + cycle + ": "); //print Before Cycle 0
            for (int i = 0; i < processes.length; i++) {
                System.out.print(processes[i].status + " " + processes[i].burst + " ");
            }
        }
        cycle++;
        
        //get ready and unstarted processes
        for (int i = 0; i < processes.length; i++) {
            if (processes[i].arrivalTime == 0) {
                processes[i].status = "ready";
                processes[i].burst = 0;
                readyList.add(processes[i]);
            } else {
                processes[i].status = "unstarted";
                processes[i].burst = 0;
                unstartedList.add(processes[i]);
            }
        }
        
        while(isFinished == false){
            if(!blockedList.isEmpty()){ //for io utilization
                hasBlock++;
            }
            
            for(int i = 0; i<unstartedList.size(); i++){ //check if unstarted processes can be ready
                if(unstartedList.get(i).arrivalTime + 1 == cycle){
                    unstartedList.get(i).status = "ready";
                    readyList.add(unstartedList.get(i));
                }
            }
            
            //select process with shortest job to run
            if(selectedProcess == null){
                if(!readyList.isEmpty()){
                    int minCPU = 1000000000;
                    int runID = -1;
                    for(int i = 0; i < readyList.size(); i++){
                        int cpu = readyList.get(i).remainingCPUBurst;
                        if(cpu < minCPU){
                            minCPU = cpu;
                            runID = i;
                        }
                    }
                    selectedProcess = readyList.get(runID);
                    readyList.remove(runID);
                    if(selectedProcess != null){
                        selectedProcess.status = "running";
                        if(selectedProcess.burst == 0){
                            selectedProcess.burst = randomOS(selectedProcess.cpuBase);
                        }
                    }
                }
            }
            
            //if there is a selected process currently running
            if(selectedProcess != null){
                selectedProcess.burst--;
                selectedProcess.remainingCPUBurst--;
            }
            
            if (verbose) { //print rest of detailed output
                System.out.print("\nBefore cycle " + cycle + ": ");
                for (int i = 0; i < processes.length; i++) {
                    if (processes[i].status.equals("running")) {
                        System.out.print(processes[i].status + " " + (1 + processes[i].burst) + " ");
                    } else {
                        System.out.print(processes[i].status+ " " + processes[i].burst + " ");
                    }
                }
            }
            
            if(!readyList.isEmpty()){
                for(int i = 0; i < readyList.size(); i++){
                    readyList.get(i).waitTime++; //increment wait time for ready processes
                }
            }
            
            if (!blockedList.isEmpty()) {
                Process[] blockedArray = blockedList.toArray(new Process[0]);
                for (int i = 0; i < blockedArray.length; i++) {
                    blockedArray[i].burst--; //increment wait time, decrement time available
                    blockedArray[i].ioTime++;
                    if (blockedArray[i].burst == 0){ //blocked to ready
                        blockedArray[i].status = "ready";
                        readyList.add(blockedArray[i]);
                        blockedList.remove(blockedArray[i]);
                    }
                }
            }
            
            
            if (selectedProcess != null) {
                if (selectedProcess.remainingCPUBurst == 0) { //check if process terminated, update accordingly
                    selectedProcess.status = "terminated";
                    selectedProcess.burst = 0;
                    selectedProcess.finishTime = cycle;
                    selectedProcess.turnTime = selectedProcess.finishTime - selectedProcess.arrivalTime;
                    selectedProcess = null;
                } else {
                    if (selectedProcess.burst == 0) { //if process is still running but burst time is up, it becomes blocked
                        selectedProcess.status = "blocked";
                        selectedProcess.burst = randomOS(selectedProcess.ioBase);
                        blockedList.add(selectedProcess);
                        selectedProcess = null;
                    }
                }
                
            }
            
            cycle++;
            
            //check if all processes are terminated
            isFinished = true;
            for (int i = 0; i < processes.length; i++) {
                if (!processes[i].status.equals("terminated")) {
                    isFinished = false;
                }
            }
        }
        System.out.println();
        System.out.println();
        System.out.println("The scheduling algorithm used was Shortest Job First\n");
    }
    
    public static void main(String[] args) throws FileNotFoundException{
        //get random numbers file
        File randomNumsFile = new File("random-numbers.txt");
        randomNums = new Scanner (randomNumsFile);
        int numProcesses;
        
        //get input file
        File file = new File(args[0]);
        
        if (args.length == 1) {
            verbose = false;
        } else if (args.length == 2) {
            file = new File(args[1]);
            verbose = true;
        } else {
            System.out.println("Error: Invalid number of command line arguments.");
        }
        
        int a, b, c, io;
        
        //print original input
        Scanner fileInput = new Scanner(file);
        System.out.print("The original input was: ");
        numProcesses = fileInput.nextInt();
        System.out.print(numProcesses);
        
        ArrayList<Integer> arrivalTimes = new ArrayList<Integer>(numProcesses);
        
        for (int i = 0; i < numProcesses; i++) {
            a = fileInput.nextInt();
            b = fileInput.nextInt();
            c = fileInput.nextInt();
            io = fileInput.nextInt();
            System.out.print("  " +a + " " + b + " " + c + " " + io);
            arrivalTimes.add(a);
        }
        
        arrivalTimes.sort(null);
        
        fileInput.close();
        
        fileInput = new Scanner(file);
        processes = new Process[numProcesses];
        numProcesses = fileInput.nextInt();
        
        //sort and print processes by arrival time
        for (int i = 0; i < numProcesses; i++) {
            a = fileInput.nextInt();
            b = fileInput.nextInt();
            c = fileInput.nextInt();
            io = fileInput.nextInt();
            Process currentProcess = new Process(a, b, c, io);
            int index = arrivalTimes.indexOf(currentProcess.arrivalTime);
            processes[index] = currentProcess; //sort processes by arrival time
            arrivalTimes.set(arrivalTimes.indexOf(currentProcess.arrivalTime), -1); //index not overwritten
        }
        
        fileInput.close();
        
        System.out.print("\nThe (sorted) input is:  ");
        System.out.print(numProcesses + "  ");
        for (int i = 0; i < numProcesses; i++) {
            processes[i].id = i;
            System.out.print(processes[i].arrivalTime + " " + processes[i].cpuBase + " " + processes[i].cpuTime + " " + processes[i].ioBase + "  ");
        }
        System.out.println();
        System.out.println();
        
        Scanner in = new Scanner(System.in);
        String type;
        System.out.println("Please select a mode (fcfs, rr, uni, or sjf): ");
        type = in.next();
        
        if(type.equals("fcfs")){
            fcfs();
            printResults();
        }else if(type.equals("rr")){
            rr(2);
            printResults();
        }else if(type.equals("uni")){
            uniprogrammed();
            printResults();
        }else if(type.equals("sjf")){
            sjf();
            printResults();
        }else{
            System.out.println("Invalid mode.");
        }
        
        in.close();
    }
    
    public static void printResults(){
        //print process results
        for (int i = 0; i < processes.length; i++) {
            
            System.out.println("Process " + i + ":");
            System.out.print("\t" + "(A,B,C,IO) = (");
            System.out.print(processes[i].arrivalTime + ",");
            System.out.print(processes[i].cpuBase + ",");
            System.out.print(processes[i].cpuTime + ",");
            System.out.println(processes[i].ioBase + ")");
            System.out.println("\t" + "Finishing Time: " + processes[i].finishTime);
            System.out.println("\t" + "Turnaround Time: " + processes[i].turnTime);
            System.out.println("\t" + "I/O Time: " + processes[i].ioTime);
            System.out.println("\t" + "Waiting Time: " + processes[i].waitTime);
            System.out.println();
        }
        
        //print and calculate summary
        double cpuUtilization = 0;
        double ioUtilization = 0.0;
        double throughput;
        double avgTurnaround = 0;
        double avgWaiting = 0;
        
        for (int i = 0; i < processes.length; i++) {
            cpuUtilization += processes[i].cpuTime;
            avgTurnaround += processes[i].turnTime;
            avgWaiting += processes[i].waitTime;
        }
        
        cpuUtilization /= (cycle-1);
        ioUtilization = (double)hasBlock / (double)(cycle-1);
        throughput = (double)((100*processes.length)/(double)(cycle - 1));
        avgTurnaround /= processes.length;
        avgWaiting /= processes.length;
        
        System.out.println("Summary Data: ");
        System.out.println("\t" + "Finishing Time: " + (cycle-1));
        System.out.println("\t" + "CPU Utilization: " + cpuUtilization);
        System.out.println("\t" + "I/O Utilization: " + ioUtilization);
        System.out.println("\t" + "Throughput: " + throughput + " processes per hundred cycles.");
        System.out.println("\t" + "Average turnaround time: " + avgTurnaround);
        System.out.println("\t" + "Average waiting time: " + avgWaiting);
    }
}

//use to sort by process ID
class CompareId implements Comparator<Process> {
    public int compare(Process p1, Process p2) {
        if (p1.id > p2.id) {
            return 1;
        } else { // two processes will never have the same id
            return -1;
        }
    }
}

//use to sort by arrival time
class CompareArrival implements Comparator<Process> {
    public int compare(Process p1, Process p2) {
        if(p1.arrivalTime > p2.arrivalTime) {
            return 1;
        } else if(p1.arrivalTime < p2.arrivalTime) {
            return -1;
        } else if (p1.id > p2.id) {
            return 1;
        } else {
            return -1;
        }
    }
}
